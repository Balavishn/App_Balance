package com.aibudgetplanner.app.domain.model

data class BudgetSnapshot(
    val salary: Double,
    val savingsGoal: Double,
    val totalFixedExpenses: Double,
    val totalSpentThisMonth: Double,
    val availableBudget: Double,
    val remainingBudget: Double,
    val dailyBudget: Double,
    val remainingDays: Int,
    val savingsProgress: Double,
    val currency: String
)
