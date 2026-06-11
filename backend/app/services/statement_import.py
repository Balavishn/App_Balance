from __future__ import annotations

from dataclasses import dataclass
from datetime import date, datetime
from io import BytesIO
import re
from typing import Iterable

import pandas as pd
from pypdf import PdfReader
from sqlmodel import Session, select

from app.models.tables import Expense, User
from app.schemas.imports import StatementImportResponse

CATEGORY_KEYWORDS = {
    "food": ["restaurant", "food", "cafe", "coffee", "swiggy", "zomato", "dining"],
    "travel": ["uber", "ola", "taxi", "metro", "bus", "train", "flight", "airport"],
    "shopping": ["amazon", "flipkart", "myntra", "shopping", "mall", "store"],
    "bills": ["electric", "bill", "gas", "water", "internet", "phone", "mobile", "broadband"],
    "entertainment": ["netflix", "spotify", "prime", "movie", "cinema", "game"],
    "medical": ["hospital", "clinic", "pharmacy", "medicine", "medical", "apollo"],
    "education": ["school", "college", "course", "udemy", "coursera", "education"],
    "investments": ["sip", "mutual fund", "stock", "investment", "broker", "nse", "bse"],
}


@dataclass
class ParsedTransaction:
    transaction_date: date
    amount: float
    description: str
    category: str
    merchant: str


_DATE_PATTERNS = [
    "%Y-%m-%d",
    "%d-%m-%Y",
    "%d/%m/%Y",
    "%m/%d/%Y",
    "%d-%m-%y",
    "%d/%m/%y",
]


def import_statement(file_name: str, file_bytes: bytes, session: Session) -> StatementImportResponse:
    statement_type = _detect_statement_type(file_name)
    transactions = _parse_transactions(statement_type, file_bytes)
    user = _get_or_create_default_user(session)
    existing_keys = _build_existing_keys(session, user.user_id)

    imported_count = 0
    duplicate_count = 0
    auto_categorized_count = 0

    for transaction in transactions:
        key = _transaction_key(transaction)
        if key in existing_keys:
            duplicate_count += 1
            continue

        if transaction.category == "Other":
            auto_categorized_count += 1

        session.add(
            Expense(
                user_id=user.user_id,
                category=transaction.category,
                amount=transaction.amount,
                date=transaction.transaction_date,
                description=f"{transaction.merchant} | {transaction.description}".strip(" |"),
            )
        )
        existing_keys.add(key)
        imported_count += 1

    session.commit()

    return StatementImportResponse(
        statement_type=statement_type,
        imported_count=imported_count,
        duplicate_count=duplicate_count,
        total_parsed_count=len(transactions),
        auto_categorized_count=auto_categorized_count,
    )


def _get_or_create_default_user(session: Session) -> User:
    default_email = "local@aibudgetplanner.app"
    user = session.exec(select(User).where(User.email == default_email)).first()
    if user:
        return user

    user = User(
        name="Local User",
        email=default_email,
        password_hash="",
        salary=0.0,
        savings_goal=0.0,
    )
    session.add(user)
    session.commit()
    session.refresh(user)
    return user


def _build_existing_keys(session: Session, user_id: int) -> set[tuple[str, float, str, str]]:
    existing_expenses = session.exec(select(Expense).where(Expense.user_id == user_id)).all()
    return {
        _expense_key(expense.date, expense.amount, expense.category, expense.description)
        for expense in existing_expenses
    }


def _expense_key(expense_date: date, amount: float, category: str, description: str) -> tuple[str, float, str, str]:
    return (
        expense_date.isoformat(),
        round(float(amount), 2),
        category.strip().lower(),
        description.strip().lower(),
    )


def _transaction_key(transaction: ParsedTransaction) -> tuple[str, float, str, str]:
    return _expense_key(transaction.transaction_date, transaction.amount, transaction.category, transaction.description)


def _detect_statement_type(file_name: str) -> str:
    lower_name = file_name.lower()
    if lower_name.endswith(".csv"):
        return "csv"
    if lower_name.endswith(".xlsx") or lower_name.endswith(".xls"):
        return "excel"
    if lower_name.endswith(".pdf"):
        return "pdf"
    return "unknown"


def _parse_transactions(statement_type: str, file_bytes: bytes) -> list[ParsedTransaction]:
    if statement_type == "csv":
        return _parse_tabular(pd.read_csv(BytesIO(file_bytes)))
    if statement_type == "excel":
        return _parse_tabular(pd.read_excel(BytesIO(file_bytes)))
    if statement_type == "pdf":
        return _parse_pdf(file_bytes)
    raise ValueError("Unsupported statement type")


def _parse_tabular(frame: pd.DataFrame) -> list[ParsedTransaction]:
    normalized_columns = {column.lower().strip(): column for column in frame.columns}
    rows: list[ParsedTransaction] = []

    for _, raw_row in frame.iterrows():
        row = {str(key).lower().strip(): value for key, value in raw_row.to_dict().items()}
        description = str(_pick_value(row, "description", "narration", "details", "transaction", "remarks", default="")).strip()
        merchant = str(_pick_value(row, "merchant", "payee", "name", default="")).strip() or _extract_merchant(description)
        amount = _parse_amount(_pick_value(row, "amount", "debit", "credit", "withdrawal", "deposit", default=0.0))
        transaction_date = _parse_date(_pick_value(row, "date", "transaction date", "value date", "posted date", default=date.today()))
        category = _infer_category(f"{description} {merchant}")

        if amount == 0.0:
            continue

        rows.append(
            ParsedTransaction(
                transaction_date=transaction_date,
                amount=abs(amount),
                description=description or merchant,
                category=category,
                merchant=merchant,
            )
        )

    return rows


def _parse_pdf(file_bytes: bytes) -> list[ParsedTransaction]:
    reader = PdfReader(BytesIO(file_bytes))
    extracted_text = "\n".join(page.extract_text() or "" for page in reader.pages)
    rows: list[ParsedTransaction] = []

    for line in extracted_text.splitlines():
        stripped = line.strip()
        if not stripped:
            continue

        amount_match = re.search(r"(-?\d{1,3}(?:,\d{3})*(?:\.\d{2})?)$", stripped)
        if not amount_match:
            continue

        amount = _parse_amount(amount_match.group(1))
        date_match = _find_date_in_text(stripped)
        transaction_date = date_match or date.today()
        description = re.sub(r"\s+", " ", stripped[: amount_match.start()]).strip()
        merchant = _extract_merchant(description)
        category = _infer_category(f"{description} {merchant}")

        if amount == 0.0:
            continue

        rows.append(
            ParsedTransaction(
                transaction_date=transaction_date,
                amount=abs(amount),
                description=description or merchant,
                category=category,
                merchant=merchant,
            )
        )

    return rows


def _pick_value(row: dict[str, object], *names: str, default: object) -> object:
    for name in names:
        if name in row and row[name] not in (None, "", "nan"):
            return row[name]
    return default


def _parse_amount(value: object) -> float:
    if value is None:
        return 0.0
    text = str(value).replace(",", "").replace("₹", "").strip()
    if not text:
        return 0.0
    sign = -1.0 if text.startswith("-") else 1.0
    text = text.lstrip("+-")
    try:
        return sign * float(text)
    except ValueError:
        return 0.0


def _parse_date(value: object) -> date:
    if isinstance(value, date):
        return value
    if isinstance(value, datetime):
        return value.date()
    text = str(value).strip()
    for pattern in _DATE_PATTERNS:
        try:
            return datetime.strptime(text, pattern).date()
        except ValueError:
            continue
    return date.today()


def _find_date_in_text(value: str) -> date | None:
    date_patterns = [
        r"\b\d{4}-\d{2}-\d{2}\b",
        r"\b\d{2}[/-]\d{2}[/-]\d{4}\b",
        r"\b\d{2}[/-]\d{2}[/-]\d{2}\b",
    ]
    for pattern in date_patterns:
        match = re.search(pattern, value)
        if not match:
            continue
        parsed = _parse_date(match.group(0))
        if parsed:
            return parsed
    return None


def _extract_merchant(description: str) -> str:
    cleaned = re.sub(r"\s+", " ", description).strip()
    if not cleaned:
        return "Unknown Merchant"
    merchant_tokens = cleaned.split(" ")[:3]
    return " ".join(merchant_tokens)


def _infer_category(text: str) -> str:
    lowered = text.lower()
    for category, keywords in CATEGORY_KEYWORDS.items():
        if any(keyword in lowered for keyword in keywords):
            return category.title()
    return "Other"
