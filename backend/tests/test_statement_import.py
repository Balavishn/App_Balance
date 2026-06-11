from datetime import date

import pandas as pd
from fastapi.testclient import TestClient
from sqlmodel import SQLModel, create_engine

import app.core.db as db
from app.main import app
from app.services.statement_import import _parse_tabular


def _client_with_test_db(tmp_path) -> TestClient:
    db_path = tmp_path / "statement_import.db"
    test_engine = create_engine(f"sqlite:///{db_path}", echo=False)
    db.engine = test_engine
    SQLModel.metadata.create_all(test_engine)
    return TestClient(app)


def test_parse_tabular_fixture_maps_category_and_amount():
    frame = pd.DataFrame(
        [
            {
                "Date": "2026-01-10",
                "Description": "Swiggy order #A1",
                "Amount": "-450.50",
                "Merchant": "Swiggy",
            },
            {
                "Date": "11/01/2026",
                "Description": "Amazon shopping",
                "Amount": "1200",
                "Merchant": "Amazon",
            },
        ]
    )

    rows = _parse_tabular(frame)

    assert len(rows) == 2
    assert rows[0].category == "Food"
    assert rows[0].amount == 450.50
    assert rows[0].transaction_date == date(2026, 1, 10)
    assert rows[1].category == "Shopping"


def test_statement_import_endpoint_handles_duplicates(tmp_path):
    import os
    from sqlmodel import Session

    db_path = tmp_path / "statement_dedup.db"
    test_engine = create_engine(f"sqlite:///{db_path}", echo=False)
    db.engine = test_engine
    SQLModel.metadata.create_all(test_engine)
    client = TestClient(app)

    csv_body = (
        "date,description,amount,merchant\n"
        "2026-01-10,Swiggy order #A1,-450.50,Swiggy\n"
    )

    first = client.post(
        "/statements/import",
        files={"file": ("statement.csv", csv_body.encode("utf-8"), "text/csv")},
    )
    assert first.status_code == 200
    first_data = first.json()
    assert first_data["statement_type"] == "csv"
    assert first_data["imported_count"] == 1
    assert first_data["duplicate_count"] == 0

    second = client.post(
        "/statements/import",
        files={"file": ("statement.csv", csv_body.encode("utf-8"), "text/csv")},
    )
    assert second.status_code == 200
    second_data = second.json()
    assert second_data["imported_count"] == 0
    assert second_data["duplicate_count"] == 1
