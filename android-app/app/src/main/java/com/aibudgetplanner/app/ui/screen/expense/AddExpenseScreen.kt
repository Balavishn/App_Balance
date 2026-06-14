package com.aibudgetplanner.app.ui.screen.expense

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
import com.aibudgetplanner.app.domain.model.ExpenseCategory

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.foundation.layout.PaddingValues

@Composable
fun AddExpenseScreen(
    uiState: ExpenseUiState,
    onAmountChange: (String) -> Unit,
    onCategoryChange: (ExpenseCategory) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPaymentMethodChange: (String) -> Unit,
    onSave: () -> Unit,
    onViewHistory: () -> Unit,
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
        Text(text = "Add Expense", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = state.amount,
            onValueChange = onAmountChange,
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.category.name,
            onValueChange = { input ->
                onCategoryChange(
                    ExpenseCategory.entries.firstOrNull { it.name.equals(input, ignoreCase = true) }
                        ?: ExpenseCategory.OTHER
                )
            },
            label = { Text("Category") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.description,
            onValueChange = onDescriptionChange,
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.paymentMethod,
            onValueChange = onPaymentMethodChange,
            label = { Text("Payment Method") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
            Text("Save")
        }

        Button(onClick = onViewHistory, modifier = Modifier.fillMaxWidth()) {
            Text("View Expense History")
        }
    }
}
