package com.aibudgetplanner.app.ui.screen.expense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aibudgetplanner.app.data.local.entity.ExpenseEntity
import java.time.Instant
import java.time.ZoneId

@Composable
fun ExpenseHistoryScreen(
    uiState: ExpenseHistoryUiState,
    onSearchChange: (String) -> Unit,
    onCategoryFilterChange: (String) -> Unit,
    onDateFilterChange: (String) -> Unit,
    onStartEdit: (ExpenseEntity) -> Unit,
    onEditAmountChange: (String) -> Unit,
    onEditDescriptionChange: (String) -> Unit,
    onSaveEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onDelete: (ExpenseEntity) -> Unit,
    contentPaddingTop: androidx.compose.ui.unit.Dp
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = contentPaddingTop)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = "Expense History", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchChange,
            label = { Text("Search description or payment") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.categoryFilter,
            onValueChange = onCategoryFilterChange,
            label = { Text("Category filter (ALL or category)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.dateFilter,
            onValueChange = onDateFilterChange,
            label = { Text("Date filter YYYY-MM-DD") },
            modifier = Modifier.fillMaxWidth()
        )

        if (uiState.editingExpenseId != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Edit Expense", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = uiState.editAmount,
                        onValueChange = onEditAmountChange,
                        label = { Text("Amount") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.editDescription,
                        onValueChange = onEditDescriptionChange,
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onSaveEdit) { Text("Save") }
                        Button(onClick = onCancelEdit) { Text("Cancel") }
                    }
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.expenses, key = { it.expenseId }) { expense ->
                ExpenseRow(
                    expense = expense,
                    onEdit = { onStartEdit(expense) },
                    onDelete = { onDelete(expense) }
                )
            }
        }
    }
}

@Composable
private fun ExpenseRow(
    expense: ExpenseEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val localDate = Instant.ofEpochMilli(expense.date).atZone(ZoneId.systemDefault()).toLocalDate()

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "${expense.category} - ${expense.amount}", style = MaterialTheme.typography.titleMedium)
                Text(text = "${expense.description} | ${expense.paymentMethod}")
                Text(text = localDate.toString())
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onEdit) { Text("Edit") }
                Button(onClick = onDelete) { Text("Delete") }
            }
        }
    }
}
