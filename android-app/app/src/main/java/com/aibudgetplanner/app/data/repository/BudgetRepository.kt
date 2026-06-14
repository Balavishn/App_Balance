package com.aibudgetplanner.app.data.repository

import com.aibudgetplanner.app.data.local.dao.ExpenseDao
import com.aibudgetplanner.app.data.local.dao.FixedExpenseDao
import com.aibudgetplanner.app.data.local.dao.UserProfileDao
import com.aibudgetplanner.app.data.local.entity.ExpenseEntity
import com.aibudgetplanner.app.data.local.entity.FixedExpenseEntity
import com.aibudgetplanner.app.data.local.entity.UserProfileEntity
import com.aibudgetplanner.app.data.sync.SyncScheduler
import com.aibudgetplanner.app.domain.model.BudgetSnapshot
import com.aibudgetplanner.app.domain.usecase.BudgetEngineUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepository @Inject constructor(
    private val userProfileDao: UserProfileDao,
    private val fixedExpenseDao: FixedExpenseDao,
    private val expenseDao: ExpenseDao,
    private val budgetEngineUseCase: BudgetEngineUseCase,
    private val pendingSyncRepository: PendingSyncRepository,
    private val syncScheduler: SyncScheduler
) {
    fun observeBudgetSnapshot(userId: String): Flow<BudgetSnapshot?> {
        val fromDate = LocalDate.now().withDayOfMonth(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        return combine(
            userProfileDao.observeProfile(),
            fixedExpenseDao.observeTotalByUser(userId),
            expenseDao.observeSpentFrom(userId, fromDate)
        ) { profile, fixedTotal, spentThisMonth ->
            profile?.toBudgetSnapshot(fixedTotal = fixedTotal, spentThisMonth = spentThisMonth)
        }
    }

    fun observeProfile(): Flow<UserProfileEntity?> = userProfileDao.observeProfile()

    suspend fun upsertProfile(profile: UserProfileEntity) {
        val updated = profile.copy(updatedAt = System.currentTimeMillis())
        userProfileDao.upsert(updated)
        pendingSyncRepository.enqueueProfileUpsert(updated)
        syncScheduler.requestImmediateSync()
    }

    fun observeExpenses(userId: String): Flow<List<ExpenseEntity>> = expenseDao.observeByUser(userId)

    fun observeFixedExpenses(userId: String): Flow<List<FixedExpenseEntity>> =
        fixedExpenseDao.observeByUser(userId)

    suspend fun addExpense(expense: ExpenseEntity) {
        val updated = expense.copy(updatedAt = System.currentTimeMillis())
        val insertedId = expenseDao.insert(updated)
        pendingSyncRepository.enqueueExpenseUpsert(updated.copy(expenseId = insertedId))
        syncScheduler.requestImmediateSync()
    }

    suspend fun updateExpense(expense: ExpenseEntity) {
        val updated = expense.copy(updatedAt = System.currentTimeMillis())
        expenseDao.update(updated)
        pendingSyncRepository.enqueueExpenseUpsert(updated)
        syncScheduler.requestImmediateSync()
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        expenseDao.delete(expense)
        pendingSyncRepository.enqueueExpenseDelete(expense)
        syncScheduler.requestImmediateSync()
    }

    suspend fun addFixedExpense(expense: FixedExpenseEntity) {
        val updated = expense.copy(updatedAt = System.currentTimeMillis())
        val insertedId = fixedExpenseDao.insert(updated)
        pendingSyncRepository.enqueueFixedExpenseUpsert(updated.copy(expenseId = insertedId))
        syncScheduler.requestImmediateSync()
    }

    suspend fun updateFixedExpense(expense: FixedExpenseEntity) {
        val updated = expense.copy(updatedAt = System.currentTimeMillis())
        fixedExpenseDao.update(updated)
        pendingSyncRepository.enqueueFixedExpenseUpsert(updated)
        syncScheduler.requestImmediateSync()
    }

    suspend fun deleteFixedExpense(expense: FixedExpenseEntity) {
        fixedExpenseDao.delete(expense)
        pendingSyncRepository.enqueueFixedExpenseDelete(expense)
        syncScheduler.requestImmediateSync()
    }

    private fun UserProfileEntity.toBudgetSnapshot(
        fixedTotal: Double,
        spentThisMonth: Double
    ): BudgetSnapshot {
        val today = LocalDate.now()
        val daysInMonth = today.lengthOfMonth()
        val remainingDays = (daysInMonth - today.dayOfMonth + 1).coerceAtLeast(1)

        val availableBudget = budgetEngineUseCase.calculateAvailableBudget(
            salary = salary,
            fixedExpenses = fixedTotal,
            savingsGoal = monthlySavingsGoal
        )
        val remainingBudget = budgetEngineUseCase.calculateRemainingBudget(
            availableBudget = availableBudget,
            spentThisMonth = spentThisMonth
        )
        val dailyBudget = budgetEngineUseCase.calculateDailyBudget(
            availableBudget = availableBudget,
            spentThisMonth = spentThisMonth,
            remainingDays = remainingDays
        )
        val savingsProgress = budgetEngineUseCase.calculateSavingsProgress(
            salary = salary,
            fixedExpenses = fixedTotal,
            spentThisMonth = spentThisMonth,
            savingsGoal = monthlySavingsGoal
        )

        return BudgetSnapshot(
            salary = salary,
            savingsGoal = monthlySavingsGoal,
            totalFixedExpenses = fixedTotal,
            totalSpentThisMonth = spentThisMonth,
            availableBudget = availableBudget,
            remainingBudget = remainingBudget,
            dailyBudget = dailyBudget,
            remainingDays = remainingDays,
            savingsProgress = savingsProgress,
            currency = currency
        )
    }
}
