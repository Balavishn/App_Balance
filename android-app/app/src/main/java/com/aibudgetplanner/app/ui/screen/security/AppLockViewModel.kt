package com.aibudgetplanner.app.ui.screen.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aibudgetplanner.app.security.AppLockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppLockViewModel @Inject constructor(
    private val appLockRepository: AppLockRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppLockUiState())
    val uiState: StateFlow<AppLockUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(appLockRepository.hasPin, appLockRepository.isLockEnabled) { hasPin, lockEnabled ->
                Pair(hasPin, lockEnabled)
            }.collect { (hasPin, lockEnabled) ->
                _uiState.update {
                    it.copy(
                        hasPin = hasPin,
                        lockEnabled = lockEnabled,
                        unlocked = if (!lockEnabled) true else it.unlocked
                    )
                }
            }
        }
    }

    fun onPinChange(value: String) {
        _uiState.update { it.copy(pinInput = value.take(6), errorMessage = null) }
    }

    fun submitPin() {
        val pin = _uiState.value.pinInput
        if (pin.length < 4) {
            _uiState.update { it.copy(errorMessage = "PIN must be at least 4 digits") }
            return
        }

        viewModelScope.launch {
            if (!_uiState.value.hasPin) {
                appLockRepository.savePin(pin)
                _uiState.update { it.copy(unlocked = true, pinInput = "", errorMessage = null) }
            } else {
                val valid = appLockRepository.verifyPin(pin)
                if (valid) {
                    _uiState.update { it.copy(unlocked = true, pinInput = "", errorMessage = null) }
                } else {
                    _uiState.update { it.copy(errorMessage = "Invalid PIN") }
                }
            }
        }
    }

    fun onBiometricSuccess() {
        _uiState.update { it.copy(unlocked = true, errorMessage = null) }
    }
}

data class AppLockUiState(
    val lockEnabled: Boolean = true,
    val hasPin: Boolean = false,
    val unlocked: Boolean = false,
    val pinInput: String = "",
    val errorMessage: String? = null
)
