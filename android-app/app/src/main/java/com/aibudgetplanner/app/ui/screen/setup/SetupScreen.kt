package com.aibudgetplanner.app.ui.screen.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.foundation.layout.PaddingValues

@Composable
fun SetupScreen(
    uiState: SetupUiState,
    onSalaryChange: (String) -> Unit,
    onSavingsGoalChange: (String) -> Unit,
    onSalaryDateChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onGoalsChange: (String) -> Unit,
    onContinue: () -> Unit,
    contentPadding: PaddingValues
) {
    val state = uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Setup Your Budget", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = state.salary,
            onValueChange = onSalaryChange,
            label = { Text("Monthly Salary") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.savingsGoal,
            onValueChange = onSavingsGoalChange,
            label = { Text("Monthly Savings Goal") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.salaryDate,
            onValueChange = onSalaryDateChange,
            label = { Text("Salary Date") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.currency,
            onValueChange = onCurrencyChange,
            label = { Text("Currency") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.financialGoals,
            onValueChange = onGoalsChange,
            label = { Text("Financial Goals") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text("Continue")
        }
    }
}
