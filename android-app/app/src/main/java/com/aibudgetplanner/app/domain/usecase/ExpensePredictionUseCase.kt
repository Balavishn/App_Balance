package com.aibudgetplanner.app.domain.usecase

import com.aibudgetplanner.app.ai.SpendingPredictor
import com.aibudgetplanner.app.domain.model.BudgetSnapshot
import com.aibudgetplanner.app.domain.model.PredictionResult
import java.time.LocalDate
import javax.inject.Inject

class ExpensePredictionUseCase @Inject constructor(
    private val spendingPredictor: SpendingPredictor
) {
    operator fun invoke(snapshot: BudgetSnapshot): PredictionResult {
        val inference = spendingPredictor.predictMonthEndSpend(snapshot)
        val projectedSpending = inference.predictedSpending
        val projectedSavings = snapshot.salary - snapshot.totalFixedExpenses - projectedSpending

        val utilization = if (snapshot.availableBudget <= 0.0) 1.0 else projectedSpending / snapshot.availableBudget
        val riskLevel = when {
            utilization > 1.0 -> "High"
            utilization > 0.85 -> "Medium"
            else -> "Low"
        }

        return PredictionResult(
            predictedSpending = projectedSpending,
            predictedSavings = projectedSavings,
            riskLevel = riskLevel,
            modelUsed = inference.modelUsed
        )
    }
}
