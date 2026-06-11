package com.aibudgetplanner.app.sms

import com.aibudgetplanner.app.data.local.entity.ExpenseEntity
import com.aibudgetplanner.app.data.repository.BudgetRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsExpenseProcessor @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val smsDedupStore: SmsDedupStore
) {
    suspend fun handleSms(message: String): Boolean {
        val candidate = SmsExpenseParser.parseTransaction(message) ?: return false
        val hash = message.trim().lowercase().hashCode().toString()
        if (smsDedupStore.contains(hash)) return false

        budgetRepository.addExpense(
            ExpenseEntity(
                userId = "local-user",
                date = System.currentTimeMillis(),
                category = candidate.category.name,
                amount = candidate.amount,
                description = candidate.description,
                paymentMethod = candidate.paymentMethod
            )
        )
        smsDedupStore.add(hash)
        return true
    }
}
