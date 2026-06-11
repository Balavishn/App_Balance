package com.aibudgetplanner.app.security

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import net.sqlcipher.database.SQLiteDatabase
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabasePassphraseProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val keyName = "db_passphrase"

    fun providePassphrase(): ByteArray {
        val sharedPreferences = securePreferences()
        val encoded = sharedPreferences.getString(keyName, null)
            ?: generateAndStorePassphrase(sharedPreferences)

        val decoded = Base64.decode(encoded, Base64.DEFAULT)
        val passphraseText = Base64.encodeToString(decoded, Base64.NO_WRAP)
        return SQLiteDatabase.getBytes(passphraseText.toCharArray())
    }

    private fun generateAndStorePassphrase(sharedPreferences: android.content.SharedPreferences): String {
        val randomBytes = ByteArray(32)
        SecureRandom().nextBytes(randomBytes)
        val encoded = Base64.encodeToString(randomBytes, Base64.NO_WRAP)
        sharedPreferences.edit().putString(keyName, encoded).apply()
        return encoded
    }

    private fun securePreferences(): android.content.SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            "secure_storage",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}
