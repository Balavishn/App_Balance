package com.aibudgetplanner.app.ui.navigation

import androidx.compose.foundation.layout.calculateTopPadding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aibudgetplanner.app.ui.screen.dashboard.DashboardScreen
import com.aibudgetplanner.app.ui.screen.dashboard.DashboardViewModel
import com.aibudgetplanner.app.ui.screen.expense.AddExpenseScreen
import com.aibudgetplanner.app.ui.screen.expense.ExpenseHistoryScreen
import com.aibudgetplanner.app.ui.screen.expense.ExpenseHistoryViewModel
import com.aibudgetplanner.app.ui.screen.expense.ExpenseViewModel
import com.aibudgetplanner.app.ui.screen.fixedexpense.FixedExpenseViewModel
import com.aibudgetplanner.app.ui.screen.fixedexpense.FixedExpensesScreen
import com.aibudgetplanner.app.ui.screen.insights.InsightsScreen
import com.aibudgetplanner.app.ui.screen.insights.InsightsViewModel
import com.aibudgetplanner.app.ui.screen.profile.ProfileScreen
import com.aibudgetplanner.app.ui.screen.reports.ReportsScreen
import com.aibudgetplanner.app.ui.screen.reports.ReportsViewModel
import com.aibudgetplanner.app.ui.screen.statementimport.StatementImportScreen
import com.aibudgetplanner.app.ui.screen.statementimport.StatementImportViewModel
import com.aibudgetplanner.app.ui.screen.setup.SetupScreen
import com.aibudgetplanner.app.ui.screen.setup.SetupViewModel

@Composable
fun AIBudgetNavHost() {
    val navController = rememberNavController()

    val bottomDestinations = listOf(
        AppDestination.Dashboard,
        AppDestination.FixedExpenses,
        AppDestination.AddExpense,
        AppDestination.ExpenseHistory,
        AppDestination.Reports,
        AppDestination.Insights,
        AppDestination.Profile
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            if (currentDestination?.route != AppDestination.Setup.route) {
                NavigationBar {
                    bottomDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true,
                            onClick = { navController.navigate(destination.route) },
                            label = { Text(destination.route.replaceFirstChar { it.uppercase() }) },
                            icon = {}
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Setup.route
        ) {
            composable(AppDestination.Setup.route) {
                val viewModel: SetupViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()
                SetupScreen(
                    uiState = uiState,
                    onSalaryChange = viewModel::onSalaryChange,
                    onSavingsGoalChange = viewModel::onSavingsGoalChange,
                    onSalaryDateChange = viewModel::onSalaryDateChange,
                    onCurrencyChange = viewModel::onCurrencyChange,
                    onGoalsChange = viewModel::onGoalsChange,
                    onContinue = {
                        viewModel.saveProfile {
                            navController.navigate(AppDestination.Dashboard.route) {
                                popUpTo(AppDestination.Setup.route) { inclusive = true }
                            }
                        }
                    },
                    contentPaddingTop = paddingValues.calculateTopPadding()
                )
            }
            composable(AppDestination.Dashboard.route) {
                val viewModel: DashboardViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()
                DashboardScreen(
                    uiState = uiState,
                    onImportStatement = { navController.navigate(AppDestination.StatementImport.route) },
                    contentPaddingTop = paddingValues.calculateTopPadding()
                )
            }
            composable(AppDestination.AddExpense.route) {
                val viewModel: ExpenseViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()
                AddExpenseScreen(
                    uiState = uiState,
                    onAmountChange = viewModel::onAmountChange,
                    onCategoryChange = viewModel::onCategoryChange,
                    onDescriptionChange = viewModel::onDescriptionChange,
                    onPaymentMethodChange = viewModel::onPaymentMethodChange,
                    onSave = { viewModel.saveExpense { navController.navigate(AppDestination.Dashboard.route) } },
                    onViewHistory = { navController.navigate(AppDestination.ExpenseHistory.route) },
                    contentPaddingTop = paddingValues.calculateTopPadding()
                )
            }
            composable(AppDestination.ExpenseHistory.route) {
                val viewModel: ExpenseHistoryViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()
                ExpenseHistoryScreen(
                    uiState = uiState,
                    onSearchChange = viewModel::onSearchQueryChange,
                    onCategoryFilterChange = viewModel::onCategoryFilterChange,
                    onDateFilterChange = viewModel::onDateFilterChange,
                    onStartEdit = viewModel::startEdit,
                    onEditAmountChange = viewModel::onEditAmountChange,
                    onEditDescriptionChange = viewModel::onEditDescriptionChange,
                    onSaveEdit = viewModel::saveEdit,
                    onCancelEdit = viewModel::cancelEdit,
                    onDelete = viewModel::deleteExpense,
                    contentPaddingTop = paddingValues.calculateTopPadding()
                )
            }
            composable(AppDestination.FixedExpenses.route) {
                val viewModel: FixedExpenseViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()
                FixedExpensesScreen(
                    uiState = uiState,
                    onNameChange = viewModel::onNameChange,
                    onCategoryChange = viewModel::onCategoryChange,
                    onAmountChange = viewModel::onAmountChange,
                    onDueDateChange = viewModel::onDueDateChange,
                    onRecurringChange = viewModel::onRecurringChange,
                    onSave = viewModel::saveFixedExpense,
                    onStartEdit = viewModel::startEdit,
                    onCancelEdit = viewModel::cancelEdit,
                    onDelete = viewModel::deleteFixedExpense,
                    contentPaddingTop = paddingValues.calculateTopPadding()
                )
            }
            composable(AppDestination.Insights.route) {
                val viewModel: InsightsViewModel = hiltViewModel()
                val insight by viewModel.insight.collectAsState()
                InsightsScreen(
                    insight = insight,
                    contentPaddingTop = paddingValues.calculateTopPadding()
                )
            }
            composable(AppDestination.Reports.route) {
                val viewModel: ReportsViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()
                ReportsScreen(
                    uiState = uiState,
                    contentPaddingTop = paddingValues.calculateTopPadding()
                )
            }
            composable(AppDestination.StatementImport.route) {
                val viewModel: StatementImportViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()
                StatementImportScreen(
                    uiState = uiState,
                    onPickFile = viewModel::pickAndImport,
                    onClear = viewModel::clearResult,
                    contentPaddingTop = paddingValues.calculateTopPadding()
                )
            }
            composable(AppDestination.Profile.route) {
                ProfileScreen()
            }
        }
    }
}
