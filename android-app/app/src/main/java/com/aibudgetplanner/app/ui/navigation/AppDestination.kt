package com.aibudgetplanner.app.ui.navigation

sealed class AppDestination(val route: String) {
    data object Setup : AppDestination("setup")
    data object Dashboard : AppDestination("dashboard")
    data object FixedExpenses : AppDestination("fixed_expenses")
    data object AddExpense : AppDestination("add_expense")
    data object ExpenseHistory : AppDestination("expense_history")
    data object Reports : AppDestination("reports")
    data object StatementImport : AppDestination("statement_import")
    data object Insights : AppDestination("insights")
    data object Profile : AppDestination("profile")
}
