package com.traveldispatcher.app.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.traveldispatcher.app.data.model.EmailSettings

class KeystoreManager(context: Context) {

    private val sharedPreferences: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                "secure_travel_dispatcher_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (_: Exception) {
            // Fallback to standard private preferences if keystore is unavailable on some emulators
            context.getSharedPreferences("fallback_travel_dispatcher_prefs", Context.MODE_PRIVATE)
        }
    }

    fun saveEmailSettings(settings: EmailSettings) {
        sharedPreferences.edit()
            .putString(KEY_GMAIL_ADDRESS, settings.gmailAddress.trim())
            .putString(KEY_SENDER_NAME, settings.senderDisplayName.trim())
            .putString(KEY_REPLY_TO, settings.replyToEmail.trim())
            .apply {
                // Only update password if provided
                if (settings.appPassword.isNotBlank()) {
                    putString(KEY_APP_PASSWORD, settings.appPassword.trim())
                }
            }
            .apply()
    }

    fun getEmailSettings(): EmailSettings {
        return EmailSettings(
            gmailAddress = sharedPreferences.getString(KEY_GMAIL_ADDRESS, "") ?: "",
            appPassword = sharedPreferences.getString(KEY_APP_PASSWORD, "") ?: "",
            senderDisplayName = sharedPreferences.getString(KEY_SENDER_NAME, "Travel Dispatcher") ?: "Travel Dispatcher",
            replyToEmail = sharedPreferences.getString(KEY_REPLY_TO, "") ?: ""
        )
    }

    fun hasPasswordStored(): Boolean {
        return !sharedPreferences.getString(KEY_APP_PASSWORD, "").isNullOrBlank()
    }

    fun clearEmailSettings() {
        sharedPreferences.edit().clear().apply()
    }

    companion object {
        private const val KEY_GMAIL_ADDRESS = "gmail_address"
        private const val KEY_APP_PASSWORD = "app_password"
        private const val KEY_SENDER_NAME = "sender_display_name"
        private const val KEY_REPLY_TO = "reply_to_email"
    }
}
