from datetime import date

from fastapi.testclient import TestClient
from sqlmodel import SQLModel, create_engine

import app.core.db as db
from app.main import app


def _client_with_test_db(tmp_path) -> TestClient:
    db_path = tmp_path / "e2e_workflow.db"
    test_engine = create_engine(f"sqlite:///{db_path}", echo=False)
    db.engine = test_engine
    SQLModel.metadata.create_all(test_engine)
    return TestClient(app)


def test_end_to_end_user_budget_flow(tmp_path):
    client = _client_with_test_db(tmp_path)

    register = client.post(
        "/register",
        json={"name": "E2E User", "email": "e2e-user@example.com", "password": "StrongPass123"},
    )
    assert register.status_code == 200

    update_profile = client.put(
        "/profile",
        params={"user_id": 1},
        json={"salary": 100000, "savings_goal": 25000},
    )
    assert update_profile.status_code == 200

    add_fixed = client.post(
        "/fixed-expenses",
        params={"user_id": 1},
        json={
            "name": "Rent",
            "category": "Housing",
            "amount": 25000,
            "due_date": 5,
            "is_recurring": True,
        },
    )
    assert add_fixed.status_code == 200

    add_expense = client.post(
        "/expenses",
        params={"user_id": 1},
        json={
            "category": "FOOD",
            "amount": 1500,
            "date": date.today().isoformat(),
            "description": "Groceries",
        },
    )
    assert add_expense.status_code == 200

    monthly = client.get(
        "/reports/monthly",
        params={"user_id": 1, "month": date.today().month, "year": date.today().year},
    )
    assert monthly.status_code == 200
    monthly_data = monthly.json()
    assert monthly_data["count"] >= 1
    assert monthly_data["total"] >= 1500

    yearly = client.get(
        "/reports/yearly",
        params={"user_id": 1, "year": date.today().year},
    )
    assert yearly.status_code == 200
    assert yearly.json()["count"] >= 1

    predict = client.post("/predict", json={"user_id": 1, "month_progress": 0.5})
    assert predict.status_code == 200
    assert "predicted_spending" in predict.json()

    recommend = client.post(
        "/recommendations",
        json={
            "user_id": 1,
            "remaining_budget": 30000,
            "current_spending": 1500,
            "month_progress": 0.5,
            "past_average_daily": 2000,
        },
    )
    assert recommend.status_code == 200
    assert "suggestion" in recommend.json()
