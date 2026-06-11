from datetime import date
from typing import Optional

from sqlmodel import Field, SQLModel


class User(SQLModel, table=True):
    user_id: Optional[int] = Field(default=None, primary_key=True)
    name: str
    email: str = Field(index=True, unique=True)
    password_hash: str
    salary: float = 0.0
    savings_goal: float = 0.0


class FixedExpense(SQLModel, table=True):
    expense_id: Optional[int] = Field(default=None, primary_key=True)
    user_id: int = Field(index=True)
    name: str
    category: str = "Other"
    amount: float
    due_date: int = 1
    is_recurring: bool = True


class Expense(SQLModel, table=True):
    expense_id: Optional[int] = Field(default=None, primary_key=True)
    user_id: int = Field(index=True)
    category: str
    amount: float
    date: date
    description: str = ""


class Prediction(SQLModel, table=True):
    prediction_id: Optional[int] = Field(default=None, primary_key=True)
    user_id: int = Field(index=True)
    month: str
    predicted_spend: float


class Recommendation(SQLModel, table=True):
    recommendation_id: Optional[int] = Field(default=None, primary_key=True)
    user_id: int = Field(index=True)
    message: str
