package com.ada.android.data

import android.content.Context
import android.os.Build
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * A.D.A. Preferences Manager
 * Secure storage for authentication tokens and user preferences
 * Uses EncryptedSharedPreferences on Android 6.0+ for security
 */
class ADAPreferences(context: Context) {
    
    private val prefs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // Use encrypted preferences on Android 6.0+
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } else {
        // Fallback to regular preferences on older devices
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    // Authentication
    var authToken: String?
        get() = prefs.getString(KEY_AUTH_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_AUTH_TOKEN, value).apply()
    
    var username: String?
        get() = prefs.getString(KEY_USERNAME, null)
        set(value) = prefs.edit().putString(KEY_USERNAME, value).apply()
    
    var userId: String?
        get() = prefs.getString(KEY_USER_ID, null)
        set(value) = prefs.edit().putString(KEY_USER_ID, value).apply()
    
    var hasExistingAccount: Boolean
        get() = prefs.getBoolean(KEY_HAS_ACCOUNT, false)
        set(value) = prefs.edit().putBoolean(KEY_HAS_ACCOUNT, value).apply()
    
    var biometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, value).apply()
    
    // FCM Token
    var fcmToken: String?
        get() = prefs.getString(KEY_FCM_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_FCM_TOKEN, value).apply()
    
    var fcmTokenRegistered: Boolean
        get() = prefs.getBoolean(KEY_FCM_REGISTERED, false)
        set(value) = prefs.edit().putBoolean(KEY_FCM_REGISTERED, value).apply()
    
    // User email
    var email: String?
        get() = prefs.getString(KEY_EMAIL, null)
        set(value) = prefs.edit().putString(KEY_EMAIL, value).apply()
    
    // Server configuration
    var serverUrl: String?
        get() = prefs.getString(KEY_SERVER_URL, null)
        set(value) = prefs.edit().putString(KEY_SERVER_URL, value).apply()
    
    /**
     * Check if user is logged in
     */
    val isLoggedIn: Boolean
        get() = !authToken.isNullOrEmpty() && !username.isNullOrEmpty()
    
    /**
     * Clear all authentication data (logout)
     */
    fun clearAuth() {
        prefs.edit().apply {
            remove(KEY_AUTH_TOKEN)
            remove(KEY_USER_ID)
            // Keep username and hasExistingAccount for re-login
            apply()
        }
    }
    
    /**
     * Clear all data (complete reset)
     */
    fun clearAll() {
        prefs.edit().clear().apply()
    }
    
    companion object {
        private const val PREFS_NAME = "ada_auth_prefs"
        
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USERNAME = "username"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_HAS_ACCOUNT = "has_existing_account"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_FCM_TOKEN = "fcm_token"
        private const val KEY_FCM_REGISTERED = "fcm_registered"
        private const val KEY_SERVER_URL = "server_url"
    }
}
