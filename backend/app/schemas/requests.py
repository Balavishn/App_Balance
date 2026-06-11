from datetime import date

from pydantic import BaseModel, EmailStr


class RegisterRequest(BaseModel):
    name: str
    email: EmailStr
    password: str


class LoginRequest(BaseModel):
    email: EmailStr
    password: str


class ProfileUpdateRequest(BaseModel):
    salary: float
    savings_goal: float


class ExpenseCreateRequest(BaseModel):
    category: str
    amount: float
    date: date
    description: str = ""


class FixedExpenseCreateRequest(BaseModel):
    name: str
    category: str
    amount: float
    due_date: int
    is_recurring: bool = True


class PredictionRequest(BaseModel):
    user_id: int
    month_progress: float = 0.0
    historical_window_days: int = 180


class RecommendationRequest(BaseModel):
    user_id: int
    remaining_budget: float
    current_spending: float
    month_progress: float = 0.0
    past_average_daily: float = 0.0


class FinancialScoreRequest(BaseModel):
    salary: float
    savings_goal: float
    expenses: float
    debt: float = 0.0
