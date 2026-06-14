package com.aibudgetplanner.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun AIBudgetNavHost(
    viewModel: AIBudgetNavViewModel = hiltViewModel()
) {
    val navState by viewModel.navigationState.collectAsState()

    if (navState is AIBudgetNavViewModel.NavigationState.Loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val startRoute = when (navState) {
        AIBudgetNavViewModel.NavigationState.Dashboard -> AppDestination.Dashboard.route
        else -> AppDestination.Setup.route
    }

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
            startDestination = startRoute
        ) {
            composable(AppDestination.Setup.route) {
                val setupViewModel: SetupViewModel = hiltViewModel()
                val uiState by setupViewModel.uiState.collectAsState()
                SetupScreen(
                    uiState = uiState,
                    onSalaryChange = setupViewModel::onSalaryChange,
                    onSavingsGoalChange = setupViewModel::onSavingsGoalChange,
                    onSalaryDateChange = setupViewModel::onSalaryDateChange,
                    onCurrencyChange = setupViewModel::onCurrencyChange,
                    onGoalsChange = setupViewModel::onGoalsChange,
                    onContinue = {
                        setupViewModel.saveProfile {
                            viewModel.checkProfile() // Recheck profile to update navigation state
                            navController.navigate(AppDestination.Dashboard.route) {
                                popUpTo(AppDestination.Setup.route) { inclusive = true }
                            }
                        }
                    },
                    contentPadding = paddingValues
                )
            }
            composable(AppDestination.Dashboard.route) {
                val dashboardViewModel: DashboardViewModel = hiltViewModel()
                val uiState by dashboardViewModel.uiState.collectAsState()
                DashboardScreen(
                    uiState = uiState,
                    onImportStatement = { navController.navigate(AppDestination.StatementImport.route) },
                    contentPadding = paddingValues
                )
            }
            composable(AppDestination.AddExpense.route) {
                val expenseViewModel: ExpenseViewModel = hiltViewModel()
                val uiState by expenseViewModel.uiState.collectAsState()
                AddExpenseScreen(
                    uiState = uiState,
                    onAmountChange = expenseViewModel::onAmountChange,
                    onCategoryChange = expenseViewModel::onCategoryChange,
                    onDescriptionChange = expenseViewModel::onDescriptionChange,
                    onPaymentMethodChange = expenseViewModel::onPaymentMethodChange,
                    onSave = { expenseViewModel.saveExpense { navController.navigate(AppDestination.Dashboard.route) } },
                    onViewHistory = { navController.navigate(AppDestination.ExpenseHistory.route) },
                    contentPadding = paddingValues
                )
            }
            composable(AppDestination.ExpenseHistory.route) {
                val historyViewModel: ExpenseHistoryViewModel = hiltViewModel()
                val uiState by historyViewModel.uiState.collectAsState()
                ExpenseHistoryScreen(
                    uiState = uiState,
                    onSearchChange = historyViewModel::onSearchQueryChange,
                    onCategoryFilterChange = historyViewModel::onCategoryFilterChange,
                    onDateFilterChange = historyViewModel::onDateFilterChange,
                    onStartEdit = historyViewModel::startEdit,
                    onEditAmountChange = historyViewModel::onEditAmountChange,
                    onEditDescriptionChange = historyViewModel::onEditDescriptionChange,
                    onSaveEdit = historyViewModel::saveEdit,
                    onCancelEdit = historyViewModel::cancelEdit,
                    onDelete = historyViewModel::deleteExpense,
                    contentPadding = paddingValues
                )
            }
            composable(AppDestination.FixedExpenses.route) {
                val fixedExpenseViewModel: FixedExpenseViewModel = hiltViewModel()
                val uiState by fixedExpenseViewModel.uiState.collectAsState()
                FixedExpensesScreen(
                    uiState = uiState,
                    onNameChange = fixedExpenseViewModel::onNameChange,
                    onCategoryChange = fixedExpenseViewModel::onCategoryChange,
                    onAmountChange = fixedExpenseViewModel::onAmountChange,
                    onDueDateChange = fixedExpenseViewModel::onDueDateChange,
                    onRecurringChange = fixedExpenseViewModel::onRecurringChange,
                    onSave = fixedExpenseViewModel::saveFixedExpense,
                    onStartEdit = fixedExpenseViewModel::startEdit,
                    onCancelEdit = fixedExpenseViewModel::cancelEdit,
                    onDelete = fixedExpenseViewModel::deleteFixedExpense,
                    contentPadding = paddingValues
                )
            }
            composable(AppDestination.Insights.route) {
                val insightsViewModel: InsightsViewModel = hiltViewModel()
                val insight by insightsViewModel.insight.collectAsState()
                InsightsScreen(
                    insight = insight,
                    contentPadding = paddingValues
                )
            }
            composable(AppDestination.Reports.route) {
                val reportsViewModel: ReportsViewModel = hiltViewModel()
                val uiState by reportsViewModel.uiState.collectAsState()
                ReportsScreen(
                    uiState = uiState,
                    contentPadding = paddingValues
                )
            }
            composable(AppDestination.StatementImport.route) {
                val importViewModel: StatementImportViewModel = hiltViewModel()
                val uiState by importViewModel.uiState.collectAsState()
                StatementImportScreen(
                    uiState = uiState,
                    onPickFile = importViewModel::pickAndImport,
                    onClear = importViewModel::clearResult,
                    contentPadding = paddingValues
                )
            }
            composable(AppDestination.Profile.route) {
                ProfileScreen(
                    contentPadding = paddingValues
                )
            }
        }
    }
}

