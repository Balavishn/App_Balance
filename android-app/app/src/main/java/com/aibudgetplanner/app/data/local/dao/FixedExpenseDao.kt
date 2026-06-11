package com.aibudgetplanner.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.aibudgetplanner.app.data.local.entity.FixedExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FixedExpenseDao {
    @Insert
    suspend fun insert(expense: FixedExpenseEntity)

    @Update
    suspend fun update(expense: FixedExpenseEntity)

    @Delete
    suspend fun delete(expense: FixedExpenseEntity)

    @Query("SELECT * FROM fixed_expense WHERE userId = :userId")
    fun observeByUser(userId: String): Flow<List<FixedExpenseEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM fixed_expense WHERE userId = :userId")
    fun observeTotalByUser(userId: String): Flow<Double>
}
