package com.aibudgetplanner.app.ui.screen.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aibudgetplanner.app.data.local.entity.ExpenseEntity
import com.aibudgetplanner.app.data.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            budgetRepository.observeExpenses("local-user").collectLatest { expenses ->
                _uiState.update {
                    it.copy(
                        dailyTotal = calculateDailyTotal(expenses),
                        weeklyTotal = calculateWeeklyTotal(expenses),
                        monthlyTotal = calculateMonthlyTotal(expenses),
                        yearlyTotal = calculateYearlyTotal(expenses),
                        categoryTotals = categoryTotals(expenses),
                        monthlyTrend = monthlyTrend(expenses)
                    )
                }
            }
        }
    }

    private fun calculateDailyTotal(expenses: List<ExpenseEntity>): Double {
        val today = LocalDate.now()
        return expenses.filter { toLocalDate(it.date) == today }.sumOf { it.amount }
    }

    private fun calculateWeeklyTotal(expenses: List<ExpenseEntity>): Double {
        val today = LocalDate.now()
        val weekStart = today.minusDays(6)
        return expenses.filter {
            val date = toLocalDate(it.date)
            !date.isBefore(weekStart) && !date.isAfter(today)
        }.sumOf { it.amount }
    }

    private fun calculateMonthlyTotal(expenses: List<ExpenseEntity>): Double {
        val now = YearMonth.now()
        return expenses.filter {
            val date = toLocalDate(it.date)
            YearMonth.of(date.year, date.month) == now
        }.sumOf { it.amount }
    }

    private fun calculateYearlyTotal(expenses: List<ExpenseEntity>): Double {
        val year = LocalDate.now().year
        return expenses.filter { toLocalDate(it.date).year == year }.sumOf { it.amount }
    }

    private fun categoryTotals(expenses: List<ExpenseEntity>): List<CategoryTotal> {
        return expenses.groupBy { it.category }
            .map { (category, items) -> CategoryTotal(category, items.sumOf { it.amount }) }
            .sortedByDescending { it.total }
    }

    private fun monthlyTrend(expenses: List<ExpenseEntity>): List<MonthlyTotal> {
        val today = LocalDate.now()
        val months = (0..5).map { YearMonth.from(today.minusMonths(it.toLong())) }.reversed()

        return months.map { month ->
            val total = expenses.filter {
                val date = toLocalDate(it.date)
                YearMonth.of(date.year, date.month) == month
            }.sumOf { it.amount }
            MonthlyTotal(month = month.toString(), total = total)
        }
    }

    private fun toLocalDate(epochMillis: Long): LocalDate {
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    }
}

data class ReportsUiState(
    val dailyTotal: Double = 0.0,
    val weeklyTotal: Double = 0.0,
    val monthlyTotal: Double = 0.0,
    val yearlyTotal: Double = 0.0,
    val categoryTotals: List<CategoryTotal> = emptyList(),
    val monthlyTrend: List<MonthlyTotal> = emptyList()
)

data class CategoryTotal(
    val category: String,
    val total: Double
)

data class MonthlyTotal(
    val month: String,
    val total: Double
)
