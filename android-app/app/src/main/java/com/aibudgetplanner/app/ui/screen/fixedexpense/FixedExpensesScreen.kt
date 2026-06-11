package com.aibudgetplanner.app.ui.screen.fixedexpense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aibudgetplanner.app.data.local.entity.FixedExpenseEntity

@Composable
fun FixedExpensesScreen(
    uiState: FixedExpenseUiState,
    onNameChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onDueDateChange: (String) -> Unit,
    onRecurringChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onStartEdit: (FixedExpenseEntity) -> Unit,
    onCancelEdit: () -> Unit,
    onDelete: (FixedExpenseEntity) -> Unit,
    contentPaddingTop: androidx.compose.ui.unit.Dp
) {
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = contentPaddingTop)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = "Fixed Expenses", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = uiState.name,
            onValueChange = onNameChange,
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.category,
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = { categoryMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Select Category")
        }
        DropdownMenu(
            expanded = categoryMenuExpanded,
            onDismissRequest = { categoryMenuExpanded = false }
        ) {
            uiState.availableCategories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        onCategoryChange(category)
                        categoryMenuExpanded = false
                    }
                )
            }
        }
        OutlinedTextField(
            value = uiState.amount,
            onValueChange = onAmountChange,
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.dueDate,
            onValueChange = onDueDateChange,
            label = { Text("Due Date (1-31)") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Recurring")
            Switch(checked = uiState.isRecurring, onCheckedChange = onRecurringChange)
        }

        Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
            Text(if (uiState.editingExpenseId == null) "Add Fixed Expense" else "Update Fixed Expense")
        }

        if (uiState.editingExpenseId != null) {
            Button(onClick = onCancelEdit, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel Edit")
            }
        }

        Spacer(modifier = Modifier.padding(2.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.expenses, key = { it.expenseId }) { expense ->
                FixedExpenseRow(
                    expense = expense,
                    onEdit = { onStartEdit(expense) },
                    onDelete = { onDelete(expense) }
                )
            }
        }
    }
}

@Composable
private fun FixedExpenseRow(expense: FixedExpenseEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = expense.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${expense.category} | ${expense.amount} | Due ${expense.dueDate} | ${if (expense.isRecurring) "Recurring" else "One-time"}"
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onEdit) {
                    Text("Edit")
                }
                Button(onClick = onDelete) {
                    Text("Delete")
                }
            }
        }
    }
}
