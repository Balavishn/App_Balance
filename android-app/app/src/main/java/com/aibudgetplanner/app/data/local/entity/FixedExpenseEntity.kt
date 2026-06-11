package com.aibudgetplanner.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fixed_expense")
data class FixedExpenseEntity(
    @PrimaryKey(autoGenerate = true) val expenseId: Long = 0,
    val userId: String,
    val name: String,
    val category: String,
    val amount: Double,
    val dueDate: Int,
    val isRecurring: Boolean
)
