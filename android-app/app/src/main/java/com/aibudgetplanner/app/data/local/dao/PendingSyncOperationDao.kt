package com.aibudgetplanner.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.aibudgetplanner.app.data.local.entity.PendingSyncOperationEntity

@Dao
interface PendingSyncOperationDao {
    @Insert
    suspend fun insert(operation: PendingSyncOperationEntity)

    @Update
    suspend fun update(operation: PendingSyncOperationEntity)

    @Query("SELECT * FROM pending_sync_operation ORDER BY createdAt ASC LIMIT :limit")
    suspend fun getPending(limit: Int = 200): List<PendingSyncOperationEntity>

    @Query("DELETE FROM pending_sync_operation WHERE id = :id")
    suspend fun deleteById(id: Long)
}
