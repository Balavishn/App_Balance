package com.aibudgetplanner.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aibudgetplanner.app.data.local.dao.ExpenseDao
import com.aibudgetplanner.app.data.local.dao.FixedExpenseDao
import com.aibudgetplanner.app.data.local.dao.UserProfileDao
import com.aibudgetplanner.app.data.local.entity.ExpenseEntity
import com.aibudgetplanner.app.data.local.entity.FixedExpenseEntity
import com.aibudgetplanner.app.data.local.entity.UserProfileEntity

@Database(
    entities = [UserProfileEntity::class, FixedExpenseEntity::class, ExpenseEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun fixedExpenseDao(): FixedExpenseDao
    abstract fun expenseDao(): ExpenseDao
}
