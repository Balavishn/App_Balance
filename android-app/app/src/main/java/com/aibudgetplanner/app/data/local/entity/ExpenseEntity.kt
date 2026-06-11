package com.aibudgetplanner.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val expenseId: Long = 0,
    val userId: String,
    val date: Long,
    val category: String,
    val amount: Double,
    val description: String,
    val paymentMethod: String,
    val updatedAt: Long = System.currentTimeMillis()
)
