package com.aibudgetplanner.app.data.repository

import com.aibudgetplanner.app.domain.model.StatementImportResult
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatementImportRepository @Inject constructor() {
    private val client = OkHttpClient()
    private val baseUrl = "http://10.0.2.2:8000"

    @Throws(IOException::class)
    fun importStatement(fileName: String, mimeType: String?, bytes: ByteArray): StatementImportResult {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                name = "file",
                filename = fileName,
                body = bytes.toRequestBody(mimeType?.toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url("$baseUrl/statements/import")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Import failed: ${response.code}")
            }

            val payload = response.body?.string().orEmpty()
            val json = JSONObject(payload)
            return StatementImportResult(
                statementType = json.getString("statement_type"),
                importedCount = json.getInt("imported_count"),
                duplicateCount = json.getInt("duplicate_count"),
                totalParsedCount = json.getInt("total_parsed_count"),
                autoCategorizedCount = json.getInt("auto_categorized_count")
            )
        }
    }
}
