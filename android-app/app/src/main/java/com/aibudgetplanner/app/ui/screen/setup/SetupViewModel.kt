package com.aibudgetplanner.app.ui.screen.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aibudgetplanner.app.data.local.entity.UserProfileEntity
import com.aibudgetplanner.app.data.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            budgetRepository.observeProfile().collect { profile ->
                if (profile != null) {
                    _uiState.update {
                        it.copy(
                            salary = if (profile.salary > 0) profile.salary.toInt().toString() else "",
                            savingsGoal = if (profile.monthlySavingsGoal > 0) profile.monthlySavingsGoal.toInt().toString() else "",
                            salaryDate = profile.salaryDate.toString(),
                            currency = profile.currency,
                            financialGoals = profile.financialGoals
                        )
                    }
                }
            }
        }
    }

    fun onSalaryChange(value: String) = _uiState.update { it.copy(salary = value) }
    fun onSavingsGoalChange(value: String) = _uiState.update { it.copy(savingsGoal = value) }
    fun onSalaryDateChange(value: String) = _uiState.update { it.copy(salaryDate = value) }
    fun onCurrencyChange(value: String) = _uiState.update { it.copy(currency = value) }
    fun onGoalsChange(value: String) = _uiState.update { it.copy(financialGoals = value) }

    fun saveProfile(onComplete: () -> Unit) {
        val state = _uiState.value
        val salary = state.salary.toDoubleOrNull() ?: 0.0
        val savingsGoal = state.savingsGoal.toDoubleOrNull() ?: 0.0
        val salaryDate = state.salaryDate.toIntOrNull() ?: 1

        viewModelScope.launch {
            budgetRepository.upsertProfile(
                UserProfileEntity(
                    userId = "local-user",
                    salary = salary,
                    salaryDate = salaryDate,
                    monthlySavingsGoal = savingsGoal,
                    currency = state.currency.ifBlank { "INR" },
                    createdDate = System.currentTimeMillis(),
                    financialGoals = state.financialGoals
                )
            )
            onComplete()
        }
    }
}

data class SetupUiState(
    val salary: String = "",
    val savingsGoal: String = "",
    val salaryDate: String = "1",
    val currency: String = "INR",
    val financialGoals: String = "Emergency fund, investments"
)
