package com.aibudgetplanner.app.sms

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsDedupStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val preferences = context.getSharedPreferences("sms_dedup_store", Context.MODE_PRIVATE)
    private val key = "recent_hashes_serialized"
    private val delimiter = "|"
    private val maxEntries = 300

    @Synchronized
    fun contains(hash: String): Boolean {
        return currentHashes().contains(hash)
    }

    @Synchronized
    fun add(hash: String) {
        val updated = currentHashes().toMutableList()
        updated.remove(hash)
        updated.add(hash)

        if (updated.size > maxEntries) {
            val removeCount = updated.size - maxEntries
            repeat(removeCount) { updated.removeAt(0) }
        }

        preferences.edit().putString(key, updated.joinToString(delimiter)).apply()
    }

    private fun currentHashes(): List<String> {
        val raw = preferences.getString(key, "").orEmpty()
        if (raw.isBlank()) return emptyList()
        return raw.split(delimiter).filter { it.isNotBlank() }
    }
}
