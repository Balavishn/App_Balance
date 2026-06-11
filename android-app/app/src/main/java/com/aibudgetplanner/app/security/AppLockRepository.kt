package com.aibudgetplanner.app.security

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

private val Context.lockDataStore by preferencesDataStore(name = "app_lock_preferences")

@Singleton
class AppLockRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val pinHashKey = stringPreferencesKey("pin_hash")
    private val lockEnabledKey = booleanPreferencesKey("lock_enabled")

    val hasPin: Flow<Boolean> = context.lockDataStore.data.map { prefs ->
        !prefs[pinHashKey].isNullOrBlank()
    }

    val isLockEnabled: Flow<Boolean> = context.lockDataStore.data.map { prefs ->
        prefs[lockEnabledKey] ?: true
    }

    suspend fun savePin(pin: String) {
        context.lockDataStore.edit { prefs ->
            prefs[pinHashKey] = hash(pin)
            prefs[lockEnabledKey] = true
        }
    }

    suspend fun verifyPin(pin: String): Boolean {
        val value = context.lockDataStore.data.map { it[pinHashKey] ?: "" }.first()
        return value.isNotBlank() && value == hash(pin)
    }

    suspend fun setLockEnabled(enabled: Boolean) {
        context.lockDataStore.edit { prefs ->
            prefs[lockEnabledKey] = enabled
        }
    }

    private fun hash(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return digest.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }
}
