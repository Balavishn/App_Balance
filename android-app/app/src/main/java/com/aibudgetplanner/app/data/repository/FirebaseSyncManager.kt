package com.aibudgetplanner.app.data.repository

import com.aibudgetplanner.app.data.local.dao.ExpenseDao
import com.aibudgetplanner.app.data.local.dao.FixedExpenseDao
import com.aibudgetplanner.app.data.local.dao.UserProfileDao
import com.aibudgetplanner.app.data.local.entity.ExpenseEntity
import com.aibudgetplanner.app.data.local.entity.FixedExpenseEntity
import com.aibudgetplanner.app.data.local.entity.UserProfileEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseSyncManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userProfileDao: UserProfileDao,
    private val fixedExpenseDao: FixedExpenseDao,
    private val expenseDao: ExpenseDao
) {
    companion object {
        internal fun shouldUseLocalVersion(localUpdatedAt: Long, remoteUpdatedAt: Long?): Boolean {
            return remoteUpdatedAt == null || localUpdatedAt >= remoteUpdatedAt
        }
    }

    suspend fun upsertRemoteProfile(userId: String, profile: UserProfileEntity) {
        firestore
            .collection("users")
            .document(userId)
            .collection("profile")
            .document("main")
            .set(RemoteUserProfile.from(profile))
            .await()
    }

    suspend fun upsertRemoteExpense(userId: String, expense: ExpenseEntity) {
        firestore
            .collection("users")
            .document(userId)
            .collection("expenses")
            .document(expense.expenseId.toString())
            .set(RemoteExpense.from(expense))
            .await()
    }

    suspend fun deleteRemoteExpense(userId: String, expenseId: Long) {
        firestore
            .collection("users")
            .document(userId)
            .collection("expenses")
            .document(expenseId.toString())
            .delete()
            .await()
    }

    suspend fun upsertRemoteFixedExpense(userId: String, expense: FixedExpenseEntity) {
        firestore
            .collection("users")
            .document(userId)
            .collection("fixed_expenses")
            .document(expense.expenseId.toString())
            .set(RemoteFixedExpense.from(expense))
            .await()
    }

    suspend fun deleteRemoteFixedExpense(userId: String, expenseId: Long) {
        firestore
            .collection("users")
            .document(userId)
            .collection("fixed_expenses")
            .document(expenseId.toString())
            .delete()
            .await()
    }

    suspend fun sync(userId: String = "local-user"): SyncSummary {
        var uploaded = 0
        var downloaded = 0
        var conflicts = 0

        val userRoot = firestore.collection("users").document(userId)

        val localProfile = userProfileDao.getByUser(userId)
        if (localProfile != null) {
            val remoteProfileDoc = userRoot.collection("profile").document("main").get().await()
            val remote = remoteProfileDoc.toObject(RemoteUserProfile::class.java)

            if (shouldUseLocalVersion(localProfile.updatedAt, remote?.updatedAt)) {
                userRoot.collection("profile").document("main").set(RemoteUserProfile.from(localProfile)).await()
                uploaded += 1
            } else {
                userProfileDao.upsert(remote.toEntity())
                downloaded += 1
                conflicts += 1
            }
        }

        val localFixed = fixedExpenseDao.getByUser(userId)
        val remoteFixedSnapshot = userRoot.collection("fixed_expenses").get().await()
        val remoteFixedMap = remoteFixedSnapshot.documents.associateBy { it.id }
        val localFixedMap = localFixed.associateBy { it.expenseId.toString() }

        for (local in localFixed) {
            val docId = local.expenseId.toString()
            val remote = remoteFixedMap[docId]?.toObject(RemoteFixedExpense::class.java)
            if (shouldUseLocalVersion(local.updatedAt, remote?.updatedAt)) {
                userRoot.collection("fixed_expenses").document(docId).set(RemoteFixedExpense.from(local)).await()
                uploaded += 1
            } else {
                fixedExpenseDao.insert(remote.toEntity())
                downloaded += 1
                conflicts += 1
            }
        }

        for ((docId, document) in remoteFixedMap) {
            if (docId in localFixedMap) continue
            val remote = document.toObject(RemoteFixedExpense::class.java) ?: continue
            fixedExpenseDao.insert(remote.toEntity())
            downloaded += 1
        }

        val localExpenses = expenseDao.getByUser(userId)
        val remoteExpenseSnapshot = userRoot.collection("expenses").get().await()
        val remoteExpenseMap = remoteExpenseSnapshot.documents.associateBy { it.id }
        val localExpenseMap = localExpenses.associateBy { it.expenseId.toString() }

        for (local in localExpenses) {
            val docId = local.expenseId.toString()
            val remote = remoteExpenseMap[docId]?.toObject(RemoteExpense::class.java)
            if (shouldUseLocalVersion(local.updatedAt, remote?.updatedAt)) {
                userRoot.collection("expenses").document(docId).set(RemoteExpense.from(local)).await()
                uploaded += 1
            } else {
                expenseDao.insert(remote.toEntity())
                downloaded += 1
                conflicts += 1
            }
        }

        for ((docId, document) in remoteExpenseMap) {
            if (docId in localExpenseMap) continue
            val remote = document.toObject(RemoteExpense::class.java) ?: continue
            expenseDao.insert(remote.toEntity())
            downloaded += 1
        }

        return SyncSummary(
            uploaded = uploaded,
            downloaded = downloaded,
            conflictsResolved = conflicts,
            strategy = "Last write wins by updatedAt"
        )
    }
}

data class SyncSummary(
    val uploaded: Int,
    val downloaded: Int,
    val conflictsResolved: Int,
    val strategy: String
)

data class RemoteUserProfile(
    val userId: String = "",
    val salary: Double = 0.0,
    val salaryDate: Int = 1,
    val monthlySavingsGoal: Double = 0.0,
    val currency: String = "INR",
    val createdDate: Long = 0L,
    val financialGoals: String = "",
    val updatedAt: Long = 0L
) {
    fun toEntity(): UserProfileEntity = UserProfileEntity(
        userId = userId,
        salary = salary,
        salaryDate = salaryDate,
        monthlySavingsGoal = monthlySavingsGoal,
        currency = currency,
        createdDate = createdDate,
        financialGoals = financialGoals,
        updatedAt = updatedAt
    )

    companion object {
        fun from(entity: UserProfileEntity): RemoteUserProfile = RemoteUserProfile(
            userId = entity.userId,
            salary = entity.salary,
            salaryDate = entity.salaryDate,
            monthlySavingsGoal = entity.monthlySavingsGoal,
            currency = entity.currency,
            createdDate = entity.createdDate,
            financialGoals = entity.financialGoals,
            updatedAt = entity.updatedAt
        )
    }
}

data class RemoteFixedExpense(
    val expenseId: Long = 0,
    val userId: String = "",
    val name: String = "",
    val category: String = "Other",
    val amount: Double = 0.0,
    val dueDate: Int = 1,
    val isRecurring: Boolean = true,
    val updatedAt: Long = 0L
) {
    fun toEntity(): FixedExpenseEntity = FixedExpenseEntity(
        expenseId = expenseId,
        userId = userId,
        name = name,
        category = category,
        amount = amount,
        dueDate = dueDate,
        isRecurring = isRecurring,
        updatedAt = updatedAt
    )

    companion object {
        fun from(entity: FixedExpenseEntity): RemoteFixedExpense = RemoteFixedExpense(
            expenseId = entity.expenseId,
            userId = entity.userId,
            name = entity.name,
            category = entity.category,
            amount = entity.amount,
            dueDate = entity.dueDate,
            isRecurring = entity.isRecurring,
            updatedAt = entity.updatedAt
        )
    }
}

data class RemoteExpense(
    val expenseId: Long = 0,
    val userId: String = "",
    val date: Long = 0L,
    val category: String = "OTHER",
    val amount: Double = 0.0,
    val description: String = "",
    val paymentMethod: String = "SMS",
    val updatedAt: Long = 0L
) {
    fun toEntity(): ExpenseEntity = ExpenseEntity(
        expenseId = expenseId,
        userId = userId,
        date = date,
        category = category,
        amount = amount,
        description = description,
        paymentMethod = paymentMethod,
        updatedAt = updatedAt
    )

    companion object {
        fun from(entity: ExpenseEntity): RemoteExpense = RemoteExpense(
            expenseId = entity.expenseId,
            userId = entity.userId,
            date = entity.date,
            category = entity.category,
            amount = entity.amount,
            description = entity.description,
            paymentMethod = entity.paymentMethod,
            updatedAt = entity.updatedAt
        )
    }
}
