package com.aibudgetplanner.app.ui.screen.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aibudgetplanner.app.domain.model.AiInsight

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.foundation.layout.PaddingValues

@Composable
fun InsightsScreen(
    insight: AiInsight?,
    contentPadding: PaddingValues
) {
    val state = insight

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = "AI Insights", style = MaterialTheme.typography.headlineMedium)

        if (state == null) {
            Text("No insights yet. Add profile and expense data.")
            return
        }

        Text("Risk Score: ${state.riskLevel}")
        Text("Warning: ${state.warning}")
        Text("Suggestion: ${state.suggestion}")
        Text("Predicted Spend: ${state.predictedMonthEndSpend}")
        Text("Financial Health Score: ${state.financialHealthScore}")
        Text("Financial Health Category: ${state.financialHealthCategory}")
    }
}
