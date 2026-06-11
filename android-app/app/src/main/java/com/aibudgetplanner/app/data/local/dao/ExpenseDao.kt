package com.aibudgetplanner.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aibudgetplanner.app.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: ExpenseEntity): Long

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Delete
    suspend fun delete(expense: ExpenseEntity)

    @Query("SELECT * FROM expense WHERE userId = :userId ORDER BY date DESC")
    fun observeByUser(userId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expense WHERE userId = :userId AND date >= :fromDate")
    fun observeSpentFrom(userId: String, fromDate: Long): Flow<Double>

    @Query("SELECT * FROM expense WHERE userId = :userId")
    suspend fun getByUser(userId: String): List<ExpenseEntity>
}
