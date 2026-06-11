package com.aibudgetplanner.app.ui.screen.statementimport

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aibudgetplanner.app.data.repository.StatementImportRepository
import com.aibudgetplanner.app.domain.model.StatementImportResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class StatementImportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: StatementImportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatementImportUiState())
    val uiState: StateFlow<StatementImportUiState> = _uiState.asStateFlow()

    fun pickAndImport(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, result = null) }
            try {
                val bytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        inputStream.readBytes()
                    } ?: throw IllegalArgumentException("Unable to read selected file")
                }
                val fileName = queryDisplayName(uri) ?: "statement.csv"
                val mimeType = context.contentResolver.getType(uri)
                val result = withContext(Dispatchers.IO) {
                    repository.importStatement(fileName, mimeType, bytes)
                }
                _uiState.update { it.copy(isLoading = false, result = result, selectedFileName = fileName) }
            } catch (error: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Import failed") }
            }
        }
    }

    fun clearResult() {
        _uiState.update { it.copy(result = null, errorMessage = null) }
    }

    private fun queryDisplayName(uri: Uri): String? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                cursor.getString(nameIndex)
            } else {
                null
            }
        }
    }
}

data class StatementImportUiState(
    val isLoading: Boolean = false,
    val selectedFileName: String? = null,
    val result: StatementImportResult? = null,
    val errorMessage: String? = null
)
