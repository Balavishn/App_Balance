package com.aibudgetplanner.app.ui.screen.fixedexpense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aibudgetplanner.app.data.local.entity.FixedExpenseEntity
import com.aibudgetplanner.app.data.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FixedExpenseViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val categories = listOf(
        "Rent",
        "EMI",
        "Insurance",
        "Internet",
        "School Fees",
        "Subscriptions",
        "Other"
    )

    private val _uiState = MutableStateFlow(FixedExpenseUiState())
    val uiState: StateFlow<FixedExpenseUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(availableCategories = categories, category = categories.last()) }
    }

    init {
        viewModelScope.launch {
            budgetRepository.observeFixedExpenses("local-user").collectLatest { expenses ->
                _uiState.update { it.copy(expenses = expenses) }
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value) }
    fun onCategoryChange(value: String) = _uiState.update { it.copy(category = value) }
    fun onAmountChange(value: String) = _uiState.update { it.copy(amount = value) }
    fun onDueDateChange(value: String) = _uiState.update { it.copy(dueDate = value) }
    fun onRecurringChange(value: Boolean) = _uiState.update { it.copy(isRecurring = value) }

    fun saveFixedExpense() {
        val state = _uiState.value
        val amount = state.amount.toDoubleOrNull() ?: return
        val dueDate = state.dueDate.toIntOrNull()?.coerceIn(1, 31) ?: 1

        viewModelScope.launch {
            val entity = FixedExpenseEntity(
                expenseId = state.editingExpenseId ?: 0,
                userId = "local-user",
                name = state.name.ifBlank { "Untitled" },
                category = state.category.ifBlank { "Other" },
                amount = amount,
                dueDate = dueDate,
                isRecurring = state.isRecurring
            )

            if (state.editingExpenseId == null) {
                budgetRepository.addFixedExpense(entity)
            } else {
                budgetRepository.updateFixedExpense(entity)
            }

            _uiState.update {
                it.copy(
                    name = "",
                    category = categories.last(),
                    amount = "",
                    dueDate = "1",
                    isRecurring = true,
                    editingExpenseId = null
                )
            }
        }
    }

    fun startEdit(expense: FixedExpenseEntity) {
        _uiState.update {
            it.copy(
                name = expense.name,
                category = expense.category,
                amount = expense.amount.toString(),
                dueDate = expense.dueDate.toString(),
                isRecurring = expense.isRecurring,
                editingExpenseId = expense.expenseId
            )
        }
    }

    fun cancelEdit() {
        _uiState.update {
            it.copy(
                name = "",
                category = categories.last(),
                amount = "",
                dueDate = "1",
                isRecurring = true,
                editingExpenseId = null
            )
        }
    }

    fun deleteFixedExpense(expense: FixedExpenseEntity) {
        viewModelScope.launch {
            budgetRepository.deleteFixedExpense(expense)
        }
    }
}

data class FixedExpenseUiState(
    val name: String = "",
    val category: String = "Other",
    val amount: String = "",
    val dueDate: String = "1",
    val isRecurring: Boolean = true,
    val editingExpenseId: Long? = null,
    val availableCategories: List<String> = emptyList(),
    val expenses: List<FixedExpenseEntity> = emptyList()
)
