package com.aibudgetplanner.app.data.repository

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FirebaseSyncManagerConflictPolicyTest {

    @Test
    fun shouldUseLocalVersion_whenRemoteMissing_returnsTrue() {
        assertTrue(FirebaseSyncManager.shouldUseLocalVersion(localUpdatedAt = 100L, remoteUpdatedAt = null))
    }

    @Test
    fun shouldUseLocalVersion_whenLocalIsNewer_returnsTrue() {
        assertTrue(FirebaseSyncManager.shouldUseLocalVersion(localUpdatedAt = 200L, remoteUpdatedAt = 100L))
    }

    @Test
    fun shouldUseLocalVersion_whenRemoteIsNewer_returnsFalse() {
        assertFalse(FirebaseSyncManager.shouldUseLocalVersion(localUpdatedAt = 100L, remoteUpdatedAt = 200L))
    }
}
