package com.aibudgetplanner.app.domain.model

data class StatementImportResult(
    val statementType: String,
    val importedCount: Int,
    val duplicateCount: Int,
    val totalParsedCount: Int,
    val autoCategorizedCount: Int
)
