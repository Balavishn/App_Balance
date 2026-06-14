package com.aibudgetplanner.app.ui.screen.reports

import android.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

import androidx.compose.foundation.layout.PaddingValues

@Composable
fun ReportsScreen(
    uiState: ReportsUiState,
    contentPadding: PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(text = "Reports", style = MaterialTheme.typography.headlineMedium)
        }

        item {
            SummaryCard("Daily Report", uiState.dailyTotal)
        }
        item {
            SummaryCard("Weekly Report", uiState.weeklyTotal)
        }
        item {
            SummaryCard("Monthly Report", uiState.monthlyTotal)
        }
        item {
            SummaryCard("Yearly Report", uiState.yearlyTotal)
        }

        item {
            Text("Expense by Category", style = MaterialTheme.typography.titleMedium)
            CategoryPieChart(uiState.categoryTotals)
        }

        item {
            Text("Budget Utilization by Category", style = MaterialTheme.typography.titleMedium)
            CategoryBarChart(uiState.categoryTotals)
        }

        item {
            Text("Monthly Trend", style = MaterialTheme.typography.titleMedium)
            MonthlyTrendLineChart(uiState.monthlyTrend)
        }

        item {
            Text("Category Analysis", style = MaterialTheme.typography.titleMedium)
        }

        items(uiState.categoryTotals) { category ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = category.category, style = MaterialTheme.typography.titleMedium)
                    Text(text = "Total: ${category.total}")
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: Double) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = value.toString(), style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun CategoryPieChart(categoryTotals: List<CategoryTotal>) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        factory = { context -> PieChart(context) },
        update = { chart ->
            val entries = categoryTotals.map { PieEntry(it.total.toFloat(), it.category) }
            val dataSet = PieDataSet(entries, "Categories").apply {
                colors = listOf(
                    Color.parseColor("#0057D8"),
                    Color.parseColor("#0B8C5A"),
                    Color.parseColor("#F59E0B"),
                    Color.parseColor("#EF4444"),
                    Color.parseColor("#2563EB"),
                    Color.parseColor("#10B981")
                )
                valueTextColor = Color.WHITE
                valueTextSize = 12f
            }
            chart.data = PieData(dataSet)
            chart.description.isEnabled = false
            chart.centerText = "Categories"
            chart.legend.isEnabled = true
            chart.invalidate()
        }
    )
}

@Composable
private fun CategoryBarChart(categoryTotals: List<CategoryTotal>) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        factory = { context -> BarChart(context) },
        update = { chart ->
            val entries = categoryTotals.mapIndexed { index, total ->
                BarEntry(index.toFloat(), total.total.toFloat())
            }
            val labels = categoryTotals.map { it.category }
            val dataSet = BarDataSet(entries, "Category Spend").apply {
                color = Color.parseColor("#0057D8")
                valueTextSize = 11f
            }
            chart.data = BarData(dataSet)
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            chart.description.isEnabled = false
            chart.axisRight.isEnabled = false
            chart.legend.isEnabled = false
            chart.invalidate()
        }
    )
}

@Composable
private fun MonthlyTrendLineChart(monthlyTrend: List<MonthlyTotal>) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        factory = { context -> LineChart(context) },
        update = { chart ->
            val entries = monthlyTrend.mapIndexed { index, point ->
                Entry(index.toFloat(), point.total.toFloat())
            }
            val labels = monthlyTrend.map { it.month }
            val dataSet = LineDataSet(entries, "Monthly Spend").apply {
                color = Color.parseColor("#0B8C5A")
                lineWidth = 2.5f
                setCircleColor(Color.parseColor("#0B8C5A"))
                setDrawValues(false)
            }
            chart.data = LineData(dataSet)
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            chart.description.isEnabled = false
            chart.axisRight.isEnabled = false
            chart.legend.isEnabled = false
            chart.invalidate()
        }
    )
}
