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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeParseException
import javax.inject.Inject

@HiltViewModel
class ExpenseHistoryViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseHistoryUiState())
    val uiState: StateFlow<ExpenseHistoryUiState> = _uiState.asStateFlow()

    private var allExpenses: List<ExpenseEntity> = emptyList()

    init {
        viewModelScope.launch {
            budgetRepository.observeExpenses("local-user").collectLatest { expenses ->
                allExpenses = expenses
                applyFilters()
            }
        }
    }

    fun onSearchQueryChange(value: String) {
        _uiState.update { it.copy(searchQuery = value) }
        applyFilters()
    }

    fun onCategoryFilterChange(value: String) {
        _uiState.update { it.copy(categoryFilter = value) }
        applyFilters()
    }

    fun onDateFilterChange(value: String) {
        _uiState.update { it.copy(dateFilter = value) }
        applyFilters()
    }

    fun startEdit(expense: ExpenseEntity) {
        _uiState.update {
            it.copy(
                editingExpenseId = expense.expenseId,
                editAmount = expense.amount.toString(),
                editDescription = expense.description
            )
        }
    }

    fun onEditAmountChange(value: String) = _uiState.update { it.copy(editAmount = value) }

    fun onEditDescriptionChange(value: String) = _uiState.update { it.copy(editDescription = value) }

    fun saveEdit() {
        val state = _uiState.value
        val editId = state.editingExpenseId ?: return
        val amount = state.editAmount.toDoubleOrNull() ?: return

        val existing = allExpenses.firstOrNull { it.expenseId == editId } ?: return
        val updated = existing.copy(amount = amount, description = state.editDescription)

        viewModelScope.launch {
            budgetRepository.updateExpense(updated)
            cancelEdit()
        }
    }

    fun cancelEdit() {
        _uiState.update { it.copy(editingExpenseId = null, editAmount = "", editDescription = "") }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            budgetRepository.deleteExpense(expense)
        }
    }

    private fun applyFilters() {
        val state = _uiState.value
        val categoryFilter = state.categoryFilter
        val search = state.searchQuery.trim().lowercase()
        val dateFilterEpoch = parseDateFilter(state.dateFilter)

        val filtered = allExpenses.filter { expense ->
            val matchesCategory = categoryFilter == "ALL" || expense.category == categoryFilter
            val matchesSearch =
                search.isBlank() || expense.description.lowercase().contains(search) || expense.paymentMethod.lowercase().contains(search)
            val matchesDate = dateFilterEpoch == null || isSameDay(expense.date, dateFilterEpoch)
            matchesCategory && matchesSearch && matchesDate
        }

        _uiState.update { it.copy(expenses = filtered) }
    }

    private fun parseDateFilter(value: String): Long? {
        if (value.isBlank()) return null
        return try {
            LocalDate.parse(value)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        } catch (_: DateTimeParseException) {
            null
        }
    }

    private fun isSameDay(epoch1: Long, epoch2: Long): Boolean {
        val day1 = Instant.ofEpochMilli(epoch1).atZone(ZoneId.systemDefault()).toLocalDate()
        val day2 = Instant.ofEpochMilli(epoch2).atZone(ZoneId.systemDefault()).toLocalDate()
        return day1 == day2
    }
}

data class ExpenseHistoryUiState(
    val searchQuery: String = "",
    val categoryFilter: String = "ALL",
    val dateFilter: String = "",
    val expenses: List<ExpenseEntity> = emptyList(),
    val editingExpenseId: Long? = null,
    val editAmount: String = "",
    val editDescription: String = "",
    val availableCategories: List<String> = listOf("ALL") + ExpenseCategory.entries.map { it.name }
)
