package com.aibudgetplanner.app.domain.usecase

import com.aibudgetplanner.app.domain.model.BudgetSnapshot
import com.aibudgetplanner.app.domain.model.PredictionResult
import java.time.LocalDate
import javax.inject.Inject

class ExpensePredictionUseCase @Inject constructor() {
    operator fun invoke(snapshot: BudgetSnapshot): PredictionResult {
        val daysElapsed = LocalDate.now().dayOfMonth.coerceAtLeast(1)
        val remainingFutureDays = (snapshot.remainingDays - 1).coerceAtLeast(0)
        val averageDailySpend = snapshot.totalSpentThisMonth / daysElapsed
        val projectedSpending = snapshot.totalSpentThisMonth + (averageDailySpend * remainingFutureDays)
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
            modelUsed = "On-device trend model"
        )
    }
}
