package com.aibudgetplanner.app.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aibudgetplanner.app.data.repository.BudgetRepository
import com.aibudgetplanner.app.domain.model.BudgetSnapshot
import com.aibudgetplanner.app.domain.model.PredictionResult
import com.aibudgetplanner.app.domain.usecase.ExpensePredictionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val expensePredictionUseCase: ExpensePredictionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                budgetRepository.observeBudgetSnapshot("local-user"),
                budgetRepository.observeExpenses("local-user")
            ) { snapshot, _ ->
                val prediction = snapshot?.let(expensePredictionUseCase::invoke)
                DashboardUiState(snapshot = snapshot, prediction = prediction)
            }.collectLatest { state ->
                _uiState.value = state
            }
        }
    }
}

data class DashboardUiState(
    val snapshot: BudgetSnapshot? = null,
    val prediction: PredictionResult? = null
)
