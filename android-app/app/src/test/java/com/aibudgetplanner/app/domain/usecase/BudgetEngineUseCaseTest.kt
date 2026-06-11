package com.aibudgetplanner.app.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class BudgetEngineUseCaseTest {

    private val useCase = BudgetEngineUseCase()

    @Test
    fun calculateAvailableBudget_returnsExpected() {
        val result = useCase.calculateAvailableBudget(
            salary = 100000.0,
            fixedExpenses = 30000.0,
            savingsGoal = 20000.0
        )

        assertEquals(50000.0, result, 0.0001)
    }

    @Test
    fun calculateDailyBudget_handlesRemainingDays() {
        val result = useCase.calculateDailyBudget(
            availableBudget = 60000.0,
            spentThisMonth = 15000.0,
            remainingDays = 15
        )

        assertEquals(3000.0, result, 0.0001)
    }

    @Test
    fun calculateSavingsProgress_handlesZeroSalary() {
        val result = useCase.calculateSavingsProgress(
            salary = 0.0,
            savingsGoal = 10000.0
        )

        assertEquals(0.0, result, 0.0001)
    }
}
