package com.aibudgetplanner.app.ui.screen.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aibudgetplanner.app.data.local.entity.ExpenseEntity
import com.aibudgetplanner.app.data.repository.BudgetRepository
import com.aibudgetplanner.app.domain.model.ExpenseCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    fun onAmountChange(value: String) = _uiState.update { it.copy(amount = value) }
    fun onCategoryChange(value: ExpenseCategory) = _uiState.update { it.copy(category = value) }
    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }
    fun onPaymentMethodChange(value: String) = _uiState.update { it.copy(paymentMethod = value) }

    fun saveExpense(onComplete: () -> Unit) {
        val state = _uiState.value
        val amount = state.amount.toDoubleOrNull() ?: return

        viewModelScope.launch {
            budgetRepository.addExpense(
                ExpenseEntity(
                    userId = "local-user",
                    date = System.currentTimeMillis(),
                    category = state.category.name,
                    amount = amount,
                    description = state.description,
                    paymentMethod = state.paymentMethod
                )
            )
            _uiState.value = ExpenseUiState()
            onComplete()
        }
    }
}

data class ExpenseUiState(
    val amount: String = "",
    val category: ExpenseCategory = ExpenseCategory.OTHER,
    val description: String = "",
    val paymentMethod: String = "UPI"
)
