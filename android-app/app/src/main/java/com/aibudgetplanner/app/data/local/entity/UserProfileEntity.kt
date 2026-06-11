package com.aibudgetplanner.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val userId: String,
    val salary: Double,
    val salaryDate: Int,
    val monthlySavingsGoal: Double,
    val currency: String,
    val createdDate: Long,
    val financialGoals: String
)
