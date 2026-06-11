package com.aibudgetplanner.app.domain.model

data class PredictionResult(
    val predictedSpending: Double,
    val predictedSavings: Double,
    val riskLevel: String,
    val modelUsed: String
)
