package com.aibudgetplanner.app.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aibudgetplanner.app.data.repository.FirebaseSyncManager
import com.aibudgetplanner.app.data.repository.PendingSyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val firebaseSyncManager: FirebaseSyncManager,
    private val pendingSyncRepository: PendingSyncRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return runCatching {
            pendingSyncRepository.replayPending(firebaseSyncManager)
            firebaseSyncManager.sync("local-user")
            Result.success()
        }.getOrElse {
            Result.retry()
        }
    }
}
