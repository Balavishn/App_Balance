from datetime import date

from fastapi import APIRouter, Depends, File, HTTPException, UploadFile
from sqlmodel import Session, select

from app.core.db import get_session
from app.core.security import create_access_token, hash_password, verify_password
from app.models.tables import Expense, FixedExpense, User
from app.schemas.requests import (
    ExpenseCreateRequest,
    FinancialScoreRequest,
    FixedExpenseCreateRequest,
    LoginRequest,
    PredictionRequest,
    ProfileUpdateRequest,
    RecommendationRequest,
    RegisterRequest,
)
from app.schemas.responses import MessageResponse, TokenResponse
from app.schemas.imports import StatementImportResponse
from app.services.ai_service import financial_score, predict_spending, recommend
from app.services.statement_import import import_statement

router = APIRouter()


@router.post("/register", response_model=MessageResponse)
def register(payload: RegisterRequest, session: Session = Depends(get_session)):
    exists = session.exec(select(User).where(User.email == payload.email)).first()
    if exists:
        raise HTTPException(status_code=400, detail="Email already registered")

    user = User(
        name=payload.name,
        email=payload.email,
        password_hash=hash_password(payload.password),
    )
    session.add(user)
    session.commit()
    return MessageResponse(message="User registered")


@router.post("/login", response_model=TokenResponse)
def login(payload: LoginRequest, session: Session = Depends(get_session)):
    user = session.exec(select(User).where(User.email == payload.email)).first()
    if not user or not verify_password(payload.password, user.password_hash):
        raise HTTPException(status_code=401, detail="Invalid credentials")

    token = create_access_token(str(user.user_id))
    return TokenResponse(access_token=token)


@router.post("/logout", response_model=MessageResponse)
def logout():
    return MessageResponse(message="Logged out")


@router.get("/profile")
def get_profile(user_id: int, session: Session = Depends(get_session)):
    user = session.get(User, user_id)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return user


@router.put("/profile")
def update_profile(payload: ProfileUpdateRequest, user_id: int, session: Session = Depends(get_session)):
    user = session.get(User, user_id)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    user.salary = payload.salary
    user.savings_goal = payload.savings_goal
    session.add(user)
    session.commit()
    session.refresh(user)
    return user


@router.post("/expenses")
def add_expense(payload: ExpenseCreateRequest, user_id: int, session: Session = Depends(get_session)):
    expense = Expense(
        user_id=user_id,
        category=payload.category,
        amount=payload.amount,
        date=payload.date,
        description=payload.description,
    )
    session.add(expense)
    session.commit()
    session.refresh(expense)
    return expense


@router.get("/expenses")
def list_expenses(user_id: int, session: Session = Depends(get_session)):
    return session.exec(select(Expense).where(Expense.user_id == user_id)).all()


@router.put("/expenses/{expense_id}")
def update_expense(expense_id: int, payload: ExpenseCreateRequest, session: Session = Depends(get_session)):
    expense = session.get(Expense, expense_id)
    if not expense:
        raise HTTPException(status_code=404, detail="Expense not found")

    expense.category = payload.category
    expense.amount = payload.amount
    expense.date = payload.date
    expense.description = payload.description
    session.add(expense)
    session.commit()
    session.refresh(expense)
    return expense


@router.delete("/expenses/{expense_id}")
def delete_expense(expense_id: int, session: Session = Depends(get_session)):
    expense = session.get(Expense, expense_id)
    if not expense:
        raise HTTPException(status_code=404, detail="Expense not found")

    session.delete(expense)
    session.commit()
    return MessageResponse(message="Expense deleted")


@router.post("/fixed-expenses")
def add_fixed_expense(payload: FixedExpenseCreateRequest, user_id: int, session: Session = Depends(get_session)):
    fixed_expense = FixedExpense(
        user_id=user_id,
        name=payload.name,
        category=payload.category,
        amount=payload.amount,
        due_date=payload.due_date,
        is_recurring=payload.is_recurring,
    )
    session.add(fixed_expense)
    session.commit()
    session.refresh(fixed_expense)
    return fixed_expense


@router.get("/fixed-expenses")
def list_fixed_expenses(user_id: int, session: Session = Depends(get_session)):
    return session.exec(select(FixedExpense).where(FixedExpense.user_id == user_id)).all()


@router.put("/fixed-expenses/{expense_id}")
def update_fixed_expense(
    expense_id: int,
    payload: FixedExpenseCreateRequest,
    session: Session = Depends(get_session),
):
    fixed_expense = session.get(FixedExpense, expense_id)
    if not fixed_expense:
        raise HTTPException(status_code=404, detail="Fixed expense not found")

    fixed_expense.name = payload.name
    fixed_expense.category = payload.category
    fixed_expense.amount = payload.amount
    fixed_expense.due_date = payload.due_date
    fixed_expense.is_recurring = payload.is_recurring

    session.add(fixed_expense)
    session.commit()
    session.refresh(fixed_expense)
    return fixed_expense


@router.delete("/fixed-expenses/{expense_id}")
def delete_fixed_expense(expense_id: int, session: Session = Depends(get_session)):
    fixed_expense = session.get(FixedExpense, expense_id)
    if not fixed_expense:
        raise HTTPException(status_code=404, detail="Fixed expense not found")

    session.delete(fixed_expense)
    session.commit()
    return MessageResponse(message="Fixed expense deleted")


@router.post("/statements/import", response_model=StatementImportResponse)
async def import_bank_statement(
    file: UploadFile = File(...),
    session: Session = Depends(get_session),
):
    contents = await file.read()
    try:
        return import_statement(file.filename, contents, session)
    except Exception as exc:
        raise HTTPException(status_code=400, detail=f"Unable to import statement: {exc}") from exc


@router.get("/reports/monthly")
def monthly_report(user_id: int, month: int, year: int, session: Session = Depends(get_session)):
    expenses = session.exec(select(Expense).where(Expense.user_id == user_id)).all()
    filtered = [e for e in expenses if e.date.month == month and e.date.year == year]
    total = sum(item.amount for item in filtered)
    by_category: dict[str, float] = {}
    for item in filtered:
        by_category[item.category] = by_category.get(item.category, 0.0) + item.amount
    return {"total": total, "category_breakdown": by_category, "count": len(filtered)}


@router.get("/reports/yearly")
def yearly_report(user_id: int, year: int, session: Session = Depends(get_session)):
    expenses = session.exec(select(Expense).where(Expense.user_id == user_id)).all()
    filtered = [e for e in expenses if e.date.year == year]
    total = sum(item.amount for item in filtered)
    return {"year": year, "total": total, "count": len(filtered)}


@router.post("/predict")
def predict(payload: PredictionRequest, session: Session = Depends(get_session)):
    user = session.get(User, payload.user_id)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    expenses = session.exec(select(Expense).where(Expense.user_id == payload.user_id)).all()
    return predict_spending(expenses, user.salary)


@router.post("/recommendations")
def get_recommendations(payload: RecommendationRequest):
    return recommend(
        current_spending=payload.current_spending,
        remaining_budget=payload.remaining_budget,
        month_progress=payload.month_progress,
        past_average_daily=payload.past_average_daily,
    )


@router.post("/financial-score")
def get_financial_score(payload: FinancialScoreRequest):
    return financial_score(
        salary=payload.salary,
        savings_goal=payload.savings_goal,
        expenses=payload.expenses,
        debt=payload.debt,
    )
