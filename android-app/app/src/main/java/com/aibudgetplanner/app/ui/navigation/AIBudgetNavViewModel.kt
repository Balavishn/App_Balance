package com.aibudgetplanner.app.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aibudgetplanner.app.data.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AIBudgetNavViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    sealed interface NavigationState {
        data object Loading : NavigationState
        data object SetupRequired : NavigationState
        data object Dashboard : NavigationState
    }

    private val _navigationState = MutableStateFlow<NavigationState>(NavigationState.Loading)
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()

    init {
        checkProfile()
    }

    fun checkProfile() {
        viewModelScope.launch {
            _navigationState.value = NavigationState.Loading
            val snapshot = budgetRepository.observeBudgetSnapshot("local-user").first()
            if (snapshot != null) {
                _navigationState.value = NavigationState.Dashboard
            } else {
                _navigationState.value = NavigationState.SetupRequired
            }
        }
    }
}
