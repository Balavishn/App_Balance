package com.aibudgetplanner.app.domain.model

data class AiInsight(
    val riskLevel: String,
    val warning: String,
    val suggestion: String,
    val predictedMonthEndSpend: Double,
    val financialHealthScore: Int,
    val financialHealthCategory: String
)
