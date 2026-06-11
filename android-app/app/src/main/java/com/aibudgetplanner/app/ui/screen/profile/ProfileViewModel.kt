package com.aibudgetplanner.app.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aibudgetplanner.app.data.repository.FirebaseSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firebaseSyncManager: FirebaseSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun syncNow() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, errorMessage = null) }
            runCatching {
                firebaseSyncManager.sync("local-user")
            }.onSuccess { summary ->
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        lastSyncSummary = "Uploaded ${summary.uploaded}, Downloaded ${summary.downloaded}, Conflicts ${summary.conflictsResolved} (${summary.strategy})"
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        errorMessage = error.message ?: "Sync failed"
                    )
                }
            }
        }
    }
}

data class ProfileUiState(
    val isSyncing: Boolean = false,
    val lastSyncSummary: String? = null,
    val errorMessage: String? = null
)
