from datetime import date

from fastapi.testclient import TestClient
from sqlmodel import SQLModel, create_engine

import app.core.db as db
from app.main import app


def _create_client_with_test_db() -> TestClient:
    import os
    if os.path.exists("./test_budget_planner.db"):
        try:
            os.remove("./test_budget_planner.db")
        except Exception:
            pass
    test_engine = create_engine("sqlite:///./test_budget_planner.db", echo=False)
    db.engine = test_engine
    SQLModel.metadata.create_all(test_engine)
    return TestClient(app)


def test_register_and_login_flow():
    client = _create_client_with_test_db()

    register_payload = {
        "name": "Test User",
        "email": "testuser@example.com",
        "password": "StrongPassword123",
    }
    register_response = client.post("/register", json=register_payload)
    assert register_response.status_code == 200

    login_response = client.post(
        "/login",
        json={"email": "testuser@example.com", "password": "StrongPassword123"},
    )
    assert login_response.status_code == 200
    assert "access_token" in login_response.json()


def test_expense_crud_flow():
    client = _create_client_with_test_db()

    client.post(
        "/register",
        json={"name": "Expense User", "email": "expense@example.com", "password": "Pass12345"},
    )

    expense_payload = {
        "category": "FOOD",
        "amount": 250.0,
        "date": date.today().isoformat(),
        "description": "Lunch",
    }

    create_response = client.post("/expenses", params={"user_id": 1}, json=expense_payload)
    assert create_response.status_code == 200
    expense_id = create_response.json()["expense_id"]

    list_response = client.get("/expenses", params={"user_id": 1})
    assert list_response.status_code == 200
    assert len(list_response.json()) >= 1

    update_response = client.put(f"/expenses/{expense_id}", json={**expense_payload, "amount": 300.0})
    assert update_response.status_code == 200
    assert update_response.json()["amount"] == 300.0

    delete_response = client.delete(f"/expenses/{expense_id}")
    assert delete_response.status_code == 200


def test_financial_score_endpoint():
    client = _create_client_with_test_db()

    response = client.post(
        "/financial-score",
        json={
            "salary": 100000,
            "savings_goal": 25000,
            "expenses": 40000,
            "debt": 10000,
        },
    )

    assert response.status_code == 200
    data = response.json()
    assert 0 <= data["score"] <= 100
    assert data["category"] in {"Excellent", "Good", "Average", "Needs Improvement"}
