package com.aibudgetplanner.app.ai

import com.aibudgetplanner.app.domain.model.BudgetSnapshot

interface SpendingPredictor {
    fun predictMonthEndSpend(snapshot: BudgetSnapshot): PredictionInference
}

data class PredictionInference(
    val predictedSpending: Double,
    val modelUsed: String
)
