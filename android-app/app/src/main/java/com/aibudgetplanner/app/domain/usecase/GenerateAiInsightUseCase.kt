package com.aibudgetplanner.app.domain.usecase

import com.aibudgetplanner.app.domain.model.AiInsight
import com.aibudgetplanner.app.domain.model.BudgetSnapshot
import javax.inject.Inject

class GenerateAiInsightUseCase @Inject constructor() {
    operator fun invoke(snapshot: BudgetSnapshot): AiInsight {
        val predictedSpend = snapshot.totalSpentThisMonth + (snapshot.dailyBudget * snapshot.remainingDays)
        val budgetUtilization = if (snapshot.availableBudget <= 0.0) 1.0 else snapshot.totalSpentThisMonth / snapshot.availableBudget

        val riskLevel = when {
            budgetUtilization > 1.0 -> "High"
            budgetUtilization > 0.85 -> "Medium"
            else -> "Low"
        }

        val warning = when {
            budgetUtilization > 1.0 -> "You are overspending. Reduce variable spending today to protect your savings goal."
            budgetUtilization > 0.85 -> "Spending is close to limit. Focus on essentials for the rest of this month."
            else -> "No major risk detected."
        }

        val suggestion = when {
            budgetUtilization > 1.0 -> "Cut discretionary categories immediately and cap daily spend to remaining budget."
            budgetUtilization > 0.85 -> "Reduce food and shopping categories by 10% for the rest of month."
            else -> "Allocate extra surplus toward your monthly savings goal."
        }

        val score = calculateHealthScore(snapshot)
        val category = when {
            score >= 90 -> "Excellent"
            score >= 70 -> "Good"
            score >= 50 -> "Average"
            else -> "Needs Improvement"
        }

        return AiInsight(
            riskLevel = riskLevel,
            warning = warning,
            suggestion = suggestion,
            predictedMonthEndSpend = predictedSpend,
            financialHealthScore = score,
            financialHealthCategory = category
        )
    }

    private fun calculateHealthScore(snapshot: BudgetSnapshot): Int {
        val savingsRatio = if (snapshot.salary > 0.0) snapshot.savingsGoal / snapshot.salary else 0.0
        val expenseRatio = if (snapshot.salary > 0.0) snapshot.totalSpentThisMonth / snapshot.salary else 1.0
        val adherence = if (snapshot.availableBudget > 0.0) (snapshot.remainingBudget / snapshot.availableBudget).coerceAtLeast(0.0) else 0.0

        val raw = (savingsRatio * 40.0) + ((1.0 - expenseRatio.coerceAtMost(1.0)) * 35.0) + (adherence * 25.0)
        return raw.toInt().coerceIn(0, 100)
    }
}
