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


def test_parse_pdf_robustness():
    from unittest.mock import MagicMock, patch
    import app.services.statement_import as si
    
    mock_page1 = MagicMock()
    mock_page1.extract_text.return_value = (
        "14/06/2026 Swiggy order -150.00 45200.00\n"
        "15/06/2026 Amazon Pay 1200.00 DR\n"
        "16/06/2026 Salary Credit 50000\n"
        "Ref 987654321012 Uber ride 250.50\n"
    )
    
    mock_reader = MagicMock()
    mock_reader.pages = [mock_page1]
    
    with patch("app.services.statement_import.PdfReader", return_value=mock_reader):
        txs = si._parse_pdf(b"mock_bytes")
        
        assert len(txs) == 4
        
        # Swiggy
        assert txs[0].merchant == "Swiggy order"
        assert txs[0].amount == 150.0
        assert txs[0].category == "Food"
        assert txs[0].transaction_date == date(2026, 6, 14)
        
        # Amazon
        assert txs[1].merchant == "Amazon Pay"
        assert txs[1].amount == 1200.0
        assert txs[1].category == "Shopping"
        assert txs[1].transaction_date == date(2026, 6, 15)
        
        # Salary
        assert txs[2].merchant == "Salary Credit"
        assert txs[2].amount == 50000.0
        assert txs[2].transaction_date == date(2026, 6, 16)
        
        # Uber
        assert txs[3].merchant == "Uber ride"
        assert txs[3].amount == 250.50
        assert txs[3].category == "Travel"

