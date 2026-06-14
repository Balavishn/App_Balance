package com.aibudgetplanner.app.ui.screen.statementimport

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aibudgetplanner.app.domain.model.StatementImportResult

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.foundation.layout.PaddingValues

@Composable
fun StatementImportScreen(
    uiState: StatementImportUiState,
    onPickFile: (android.net.Uri) -> Unit,
    onClear: () -> Unit,
    contentPadding: PaddingValues
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                onPickFile(uri)
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Import Bank Statement", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Supported formats: CSV, Excel, PDF")

        Button(
            onClick = {
                launcher.launch(
                    arrayOf(
                        "text/csv",
                        "application/pdf",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        "application/vnd.ms-excel"
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Choose Statement File")
        }

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        uiState.selectedFileName?.let {
            Text(text = "Selected file: $it")
        }

        uiState.errorMessage?.let { error ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(text = error, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.error)
            }
        }

        uiState.result?.let { result ->
            ImportResultCard(result = result)
            Button(onClick = onClear, modifier = Modifier.fillMaxWidth()) {
                Text("Clear Result")
            }
        }
    }
}

@Composable
private fun ImportResultCard(result: StatementImportResult) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = "Import Complete", style = MaterialTheme.typography.titleMedium)
            Text(text = "Statement type: ${result.statementType}")
            Text(text = "Parsed: ${result.totalParsedCount}")
            Text(text = "Imported: ${result.importedCount}")
            Text(text = "Duplicates skipped: ${result.duplicateCount}")
            Text(text = "Auto-categorized: ${result.autoCategorizedCount}")
        }
    }
}
