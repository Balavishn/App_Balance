from pydantic import BaseModel


class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"


class MessageResponse(BaseModel):
    message: str


class PredictionResponse(BaseModel):
    predicted_spending: float
    predicted_savings: float
    risk_level: str
    model_used: str
    confidence: float


class RecommendationResponse(BaseModel):
    warning: str
    suggestion: str
    predicted_spending: float


class FinancialScoreResponse(BaseModel):
    score: int
    category: str
