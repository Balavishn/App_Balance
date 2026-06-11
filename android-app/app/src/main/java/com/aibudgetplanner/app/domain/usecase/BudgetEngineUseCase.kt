package com.aibudgetplanner.app.domain.usecase

import javax.inject.Inject

class BudgetEngineUseCase @Inject constructor() {

    fun calculateAvailableBudget(salary: Double, fixedExpenses: Double, savingsGoal: Double): Double {
        return salary - fixedExpenses - savingsGoal
    }

    fun calculateDailyBudget(availableBudget: Double, spentThisMonth: Double, remainingDays: Int): Double {
        val remainingBudget = availableBudget - spentThisMonth
        return if (remainingDays <= 0) remainingBudget else remainingBudget / remainingDays
    }

    fun calculateRemainingBudget(availableBudget: Double, spentThisMonth: Double): Double {
        return availableBudget - spentThisMonth
    }

    fun calculateSavingsProgress(salary: Double, savingsGoal: Double): Double {
        if (salary <= 0.0) return 0.0
        return (savingsGoal / salary) * 100.0
    }
}
