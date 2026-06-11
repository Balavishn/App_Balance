from calendar import monthrange
from collections import defaultdict
from datetime import date, timedelta

import numpy as np
from sklearn.ensemble import RandomForestRegressor

from app.schemas.responses import FinancialScoreResponse, PredictionResponse, RecommendationResponse

try:
    from xgboost import XGBRegressor
except Exception:
    XGBRegressor = None


def _feature_vector(day: date) -> list[int]:
    return [day.day, day.month, day.weekday(), 1 if day.weekday() >= 5 else 0]


def predict_spending(expenses: list, salary: float) -> PredictionResponse:
    today = date.today()
    month_start = today.replace(day=1)
    month_end_day = monthrange(today.year, today.month)[1]
    current_total = float(sum(item.amount for item in expenses if item.date >= month_start))

    daily_totals: dict[date, float] = defaultdict(float)
    for item in expenses:
        daily_totals[item.date] += float(item.amount)

    historical_days = sorted(daily_totals.keys())
    remaining_days = [
        date(today.year, today.month, d)
        for d in range(today.day + 1, month_end_day + 1)
    ]

    if len(historical_days) < 7:
        elapsed = max(today.day, 1)
        avg_daily = current_total / elapsed
        projected = current_total + (avg_daily * len(remaining_days))
        model_used = "Heuristic Average"
        confidence = 0.45
    else:
        x_train = np.array([_feature_vector(day) for day in historical_days])
        y_train = np.array([daily_totals[day] for day in historical_days])
        x_future = np.array([_feature_vector(day) for day in remaining_days]) if remaining_days else np.array([])

        rf_model = RandomForestRegressor(n_estimators=200, random_state=42)
        rf_model.fit(x_train, y_train)
        rf_future = rf_model.predict(x_future).sum() if len(x_future) else 0.0

        if XGBRegressor is not None:
            xgb_model = XGBRegressor(
                n_estimators=200,
                max_depth=5,
                learning_rate=0.05,
                objective="reg:squarederror",
                subsample=0.9,
                colsample_bytree=0.9,
                random_state=42,
            )
            xgb_model.fit(x_train, y_train)
            xgb_future = xgb_model.predict(x_future).sum() if len(x_future) else 0.0
            projected = current_total + ((rf_future + xgb_future) / 2.0)
            model_used = "RandomForest + XGBoost"
            confidence = min(0.95, 0.6 + (len(historical_days) / 300.0))
        else:
            projected = current_total + rf_future
            model_used = "RandomForest"
            confidence = min(0.9, 0.58 + (len(historical_days) / 350.0))

    predicted_savings = salary - projected

    risk = "Low"
    if projected > salary:
        risk = "High"
    elif projected > salary * 0.85:
        risk = "Medium"

    return PredictionResponse(
        predicted_spending=round(projected, 2),
        predicted_savings=round(predicted_savings, 2),
        risk_level=risk,
        model_used=model_used,
        confidence=round(confidence, 2),
    )


def recommend(
    current_spending: float,
    remaining_budget: float,
    month_progress: float,
    past_average_daily: float,
) -> RecommendationResponse:
    projected_spending = current_spending if month_progress <= 0 else current_spending / month_progress

    if remaining_budget < 0:
        return RecommendationResponse(
            warning="Overspending detected.",
            suggestion="Reduce discretionary categories immediately and cap daily variable spend.",
            predicted_spending=round(projected_spending, 2),
        )

    if projected_spending > (current_spending + remaining_budget):
        return RecommendationResponse(
            warning="Spending is accelerating.",
            suggestion="Reduce food and shopping spend by 10-15% for the rest of month.",
            predicted_spending=round(projected_spending, 2),
        )

    if past_average_daily > 0 and (current_spending / max(1.0, month_progress * 30.0)) > (past_average_daily * 1.2):
        return RecommendationResponse(
            warning="Current daily spending exceeds historical trend.",
            suggestion="Set stricter daily cap until trend normalizes.",
            predicted_spending=round(projected_spending, 2),
        )

    return RecommendationResponse(
        warning="Budget is stable.",
        suggestion="Continue at current pace and allocate surplus to savings.",
        predicted_spending=round(projected_spending, 2),
    )


def financial_score(salary: float, savings_goal: float, expenses: float, debt: float) -> FinancialScoreResponse:
    savings_ratio = savings_goal / salary if salary > 0 else 0
    expense_ratio = expenses / salary if salary > 0 else 1
    debt_ratio = debt / salary if salary > 0 else 1

    score = int((savings_ratio * 40) + ((1 - min(expense_ratio, 1)) * 35) + ((1 - min(debt_ratio, 1)) * 25))
    score = max(0, min(100, score))

    if score >= 90:
        category = "Excellent"
    elif score >= 70:
        category = "Good"
    elif score >= 50:
        category = "Average"
    else:
        category = "Needs Improvement"

    return FinancialScoreResponse(score=score, category=category)
