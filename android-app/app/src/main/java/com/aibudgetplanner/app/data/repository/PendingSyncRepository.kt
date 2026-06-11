package com.aibudgetplanner.app.data.repository

import com.aibudgetplanner.app.data.local.dao.PendingSyncOperationDao
import com.aibudgetplanner.app.data.local.entity.ExpenseEntity
import com.aibudgetplanner.app.data.local.entity.FixedExpenseEntity
import com.aibudgetplanner.app.data.local.entity.PendingSyncOperationEntity
import com.aibudgetplanner.app.data.local.entity.UserProfileEntity
import com.aibudgetplanner.app.data.sync.SyncOperationType
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PendingSyncRepository @Inject constructor(
    private val pendingSyncOperationDao: PendingSyncOperationDao
) {
    suspend fun enqueueProfileUpsert(profile: UserProfileEntity) {
        enqueue(
            SyncOperationType.UPSERT_PROFILE,
            JSONObject()
                .put("userId", profile.userId)
                .put("salary", profile.salary)
                .put("salaryDate", profile.salaryDate)
                .put("monthlySavingsGoal", profile.monthlySavingsGoal)
                .put("currency", profile.currency)
                .put("createdDate", profile.createdDate)
                .put("financialGoals", profile.financialGoals)
                .put("updatedAt", profile.updatedAt)
                .toString()
        )
    }

    suspend fun enqueueExpenseUpsert(expense: ExpenseEntity) {
        enqueue(
            SyncOperationType.UPSERT_EXPENSE,
            JSONObject()
                .put("expenseId", expense.expenseId)
                .put("userId", expense.userId)
                .put("date", expense.date)
                .put("category", expense.category)
                .put("amount", expense.amount)
                .put("description", expense.description)
                .put("paymentMethod", expense.paymentMethod)
                .put("updatedAt", expense.updatedAt)
                .toString()
        )
    }

    suspend fun enqueueExpenseDelete(expense: ExpenseEntity) {
        enqueue(
            SyncOperationType.DELETE_EXPENSE,
            JSONObject()
                .put("expenseId", expense.expenseId)
                .put("userId", expense.userId)
                .toString()
        )
    }

    suspend fun enqueueFixedExpenseUpsert(expense: FixedExpenseEntity) {
        enqueue(
            SyncOperationType.UPSERT_FIXED_EXPENSE,
            JSONObject()
                .put("expenseId", expense.expenseId)
                .put("userId", expense.userId)
                .put("name", expense.name)
                .put("category", expense.category)
                .put("amount", expense.amount)
                .put("dueDate", expense.dueDate)
                .put("isRecurring", expense.isRecurring)
                .put("updatedAt", expense.updatedAt)
                .toString()
        )
    }

    suspend fun enqueueFixedExpenseDelete(expense: FixedExpenseEntity) {
        enqueue(
            SyncOperationType.DELETE_FIXED_EXPENSE,
            JSONObject()
                .put("expenseId", expense.expenseId)
                .put("userId", expense.userId)
                .toString()
        )
    }

    suspend fun replayPending(firebaseSyncManager: FirebaseSyncManager, batchSize: Int = 200): Int {
        val pending = pendingSyncOperationDao.getPending(limit = batchSize)
        var replayed = 0
        for (operation in pending) {
            val ok = runCatching {
                replayOperation(firebaseSyncManager, operation)
            }.isSuccess

            if (ok) {
                pendingSyncOperationDao.deleteById(operation.id)
                replayed += 1
            } else {
                pendingSyncOperationDao.update(
                    operation.copy(retryCount = operation.retryCount + 1, lastError = "Replay failed")
                )
            }
        }
        return replayed
    }

    private suspend fun replayOperation(
        firebaseSyncManager: FirebaseSyncManager,
        operation: PendingSyncOperationEntity
    ) {
        val payload = JSONObject(operation.payloadJson)
        when (operation.operationType) {
            SyncOperationType.UPSERT_PROFILE -> {
                firebaseSyncManager.upsertRemoteProfile(
                    userId = payload.getString("userId"),
                    profile = UserProfileEntity(
                        userId = payload.getString("userId"),
                        salary = payload.getDouble("salary"),
                        salaryDate = payload.getInt("salaryDate"),
                        monthlySavingsGoal = payload.getDouble("monthlySavingsGoal"),
                        currency = payload.getString("currency"),
                        createdDate = payload.getLong("createdDate"),
                        financialGoals = payload.optString("financialGoals"),
                        updatedAt = payload.getLong("updatedAt")
                    )
                )
            }

            SyncOperationType.UPSERT_EXPENSE -> {
                firebaseSyncManager.upsertRemoteExpense(
                    userId = payload.getString("userId"),
                    expense = ExpenseEntity(
                        expenseId = payload.getLong("expenseId"),
                        userId = payload.getString("userId"),
                        date = payload.getLong("date"),
                        category = payload.getString("category"),
                        amount = payload.getDouble("amount"),
                        description = payload.getString("description"),
                        paymentMethod = payload.getString("paymentMethod"),
                        updatedAt = payload.getLong("updatedAt")
                    )
                )
            }

            SyncOperationType.DELETE_EXPENSE -> {
                firebaseSyncManager.deleteRemoteExpense(
                    userId = payload.getString("userId"),
                    expenseId = payload.getLong("expenseId")
                )
            }

            SyncOperationType.UPSERT_FIXED_EXPENSE -> {
                firebaseSyncManager.upsertRemoteFixedExpense(
                    userId = payload.getString("userId"),
                    expense = FixedExpenseEntity(
                        expenseId = payload.getLong("expenseId"),
                        userId = payload.getString("userId"),
                        name = payload.getString("name"),
                        category = payload.getString("category"),
                        amount = payload.getDouble("amount"),
                        dueDate = payload.getInt("dueDate"),
                        isRecurring = payload.getBoolean("isRecurring"),
                        updatedAt = payload.getLong("updatedAt")
                    )
                )
            }

            SyncOperationType.DELETE_FIXED_EXPENSE -> {
                firebaseSyncManager.deleteRemoteFixedExpense(
                    userId = payload.getString("userId"),
                    expenseId = payload.getLong("expenseId")
                )
            }
        }
    }

    private suspend fun enqueue(operationType: String, payloadJson: String) {
        pendingSyncOperationDao.insert(
            PendingSyncOperationEntity(operationType = operationType, payloadJson = payloadJson)
        )
    }
}
