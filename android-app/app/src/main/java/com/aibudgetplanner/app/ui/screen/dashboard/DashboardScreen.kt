package com.aibudgetplanner.app.ui.screen.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onImportStatement: () -> Unit,
    contentPaddingTop: androidx.compose.ui.unit.Dp
) {
    val snapshot = uiState.snapshot
    val prediction = uiState.prediction

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = contentPaddingTop)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Dashboard", style = MaterialTheme.typography.headlineMedium)

        if (snapshot == null) {
            Text("Complete setup to view your budget metrics.")
            return
        }

        MetricCard(title = "Current Salary", value = snapshot.salary.toString())
        MetricCard(title = "Remaining Budget", value = snapshot.remainingBudget.toString())
        MetricCard(title = "Today Budget", value = snapshot.dailyBudget.toString())
        MetricCard(title = "Savings Goal Progress", value = "${snapshot.savingsProgress.toInt()}%")
        MetricCard(title = "Predicted Month End Spend", value = prediction?.predictedSpending?.toString() ?: "-")
        MetricCard(title = "Predicted Savings", value = prediction?.predictedSavings?.toString() ?: "-")
        MetricCard(title = "Prediction Risk", value = prediction?.riskLevel ?: "-")
        MetricCard(title = "Prediction Model", value = prediction?.modelUsed ?: "-")

        Button(onClick = onImportStatement, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Import Bank Statement")
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
