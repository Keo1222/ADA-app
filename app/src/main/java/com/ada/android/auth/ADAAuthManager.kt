package com.ada.android.auth

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import androidx.fragment.app.FragmentActivity
import com.ada.android.ADAApplication
import com.ada.android.BuildConfig
import com.ada.android.R
import com.ada.android.data.ADAPreferences
import com.ada.android.network.ADAServerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * A.D.A. Authentication Manager
 * Handles login, registration, and biometric authentication
 * Equivalent to iOS ADAAuthManager.swift
 */
class ADAAuthManager(
    private val context: Context,
    private val preferences: ADAPreferences,
    private val serverManager: ADAServerManager
) {
    
    // Authentication state
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _currentUser = MutableStateFlow<ADAUser?>(null)
    val currentUser: StateFlow<ADAUser?> = _currentUser.asStateFlow()
    
    // Biometric availability
    val biometricAvailable: Boolean
        get() = checkBiometricAvailability()
    
    val hasExistingAccount: Boolean
        get() = preferences.hasExistingAccount
    
    init {
        // Try to restore session
        restoreSession()
    }
    
    /**
     * Check if biometric authentication is available
     */
    private fun checkBiometricAvailability(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }
    
    /**
     * Authenticate using biometrics (fingerprint or face)
     */
    fun authenticateWithBiometrics(
        activity: ComponentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(context)
        
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                // Restore session after biometric success
                restoreSession()
                if (_isAuthenticated.value) {
                    onSuccess()
                } else {
                    onError("Session expired. Please login with passcode.")
                }
            }
            
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                    errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    onError(errString.toString())
                }
            }
            
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Don't show error - user can retry
            }
        }
        
        // Cast ComponentActivity to FragmentActivity for BiometricPrompt
        val biometricPrompt = BiometricPrompt(activity as FragmentActivity, executor, callback)
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.biometric_prompt_title))
            .setSubtitle(context.getString(R.string.biometric_prompt_subtitle))
            .setDescription(context.getString(R.string.biometric_prompt_description))
            .setNegativeButtonText(context.getString(R.string.biometric_negative_button))
            .setConfirmationRequired(false)
            .build()
        
        biometricPrompt.authenticate(promptInfo)
    }
    
    /**
     * Login with username and passcode
     */
    suspend fun login(username: String, passcode: String): Boolean {
        _isLoading.value = true
        _errorMessage.value = null
        
        return withContext(Dispatchers.IO) {
            try {
                val loginData = mapOf(
                    "username" to username,
                    "password" to passcode
                )
                
                val response = serverManager.post("/api/auth/login", loginData)
                
                if (response != null && response.optBoolean("success", false)) {
                    // Extract token and user data
                    val token = response.optString("token", null)
                        ?: response.optJSONObject("data")?.optString("token", null)
                        ?: "session_token"
                    
                    val userData = response.optJSONObject("user")
                        ?: response.optJSONObject("data")
                    
                    handleSuccessfulAuth(token, userData, username)
                    true
                } else {
                    val error = response?.optJSONObject("error")?.optString("message")
                        ?: context.getString(R.string.error_login_failed)
                    _errorMessage.value = error
                    false
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: context.getString(R.string.error_connection)
                false
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Enroll new user account
     * NOTE: Accounts created via Android are assigned "user" role (not admin)
     */
    suspend fun enrollUser(enrollmentData: EnrollmentData): Boolean {
        _isLoading.value = true
        _errorMessage.value = null
        
        return withContext(Dispatchers.IO) {
            try {
                val registerData = mutableMapOf(
                    "username" to enrollmentData.username,
                    "password" to enrollmentData.passcode,
                    "email" to "${enrollmentData.username}@local.ada",
                    "role" to "user" // Normal user, not admin
                )
                
                // Add voice data if available
                enrollmentData.voiceData?.let { voice ->
                    registerData["voice_data"] = android.util.Base64.encodeToString(
                        voice, android.util.Base64.NO_WRAP
                    )
                }
                
                // Add face data if available  
                enrollmentData.faceData?.let { face ->
                    registerData["face_data"] = android.util.Base64.encodeToString(
                        face, android.util.Base64.NO_WRAP
                    )
                }
                
                val response = serverManager.post("/api/auth/register", registerData)
                
                if (response != null && response.optBoolean("success", false)) {
                    // Registration successful - now login
                    login(enrollmentData.username, enrollmentData.passcode)
                } else {
                    val error = response?.optJSONObject("error")?.optString("message")
                        ?: context.getString(R.string.error_enrollment_failed)
                    _errorMessage.value = error
                    false
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: context.getString(R.string.error_connection)
                false
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Handle successful authentication
     */
    private fun handleSuccessfulAuth(token: String, userData: JSONObject?, username: String) {
        // Save to preferences
        preferences.authToken = token
        preferences.username = username
        preferences.userId = userData?.optString("user_id") ?: java.util.UUID.randomUUID().toString()
        preferences.email = userData?.optString("email")
        preferences.hasExistingAccount = true
        preferences.biometricEnabled = biometricAvailable
        
        // Update state
        _currentUser.value = ADAUser(
            id = preferences.userId ?: "",
            username = username,
            email = preferences.email
        )
        _isAuthenticated.value = true
    }
    
    /**
     * Restore session from stored credentials
     */
    private fun restoreSession() {
        val token = preferences.authToken
        val username = preferences.username
        
        if (!token.isNullOrEmpty() && !username.isNullOrEmpty()) {
            _currentUser.value = ADAUser(
                id = preferences.userId ?: "",
                username = username,
                email = preferences.email
            )
            _isAuthenticated.value = true
        }
    }
    
    /**
     * Logout user
     */
    fun logout() {
        preferences.clearAuth()
        _currentUser.value = null
        _isAuthenticated.value = false
        _errorMessage.value = null
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
}

/**
 * User data class
 */
data class ADAUser(
    val id: String,
    val username: String,
    val email: String?
)

/**
 * Enrollment data class
 */
data class EnrollmentData(
    var username: String = "",
    var passcode: String = "",
    var confirmPasscode: String = "",
    var voiceData: ByteArray? = null,
    var voiceQualityMetrics: VoiceQualityMetrics? = null,
    var faceData: ByteArray? = null,
    var faceAnalysis: FaceAnalysis? = null,
    var agreedToTerms: Boolean = false
) {
    val isCredentialsValid: Boolean
        get() = username.length >= 3 && 
                passcode.length >= 8 && 
                passcode == confirmPasscode
    
    val isVoiceEnrolled: Boolean
        get() = voiceData != null
    
    val isFaceEnrolled: Boolean
        get() = faceData != null
    
    val isComplete: Boolean
        get() = isCredentialsValid && isVoiceEnrolled && isFaceEnrolled && agreedToTerms
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as EnrollmentData
        return username == other.username && passcode == other.passcode
    }
    
    override fun hashCode(): Int {
        return username.hashCode() * 31 + passcode.hashCode()
    }
}

data class VoiceQualityMetrics(
    val signalToNoiseRatio: Double = 0.0,
    val averageAmplitude: Double = 0.0,
    val recordingDuration: Double = 0.0,
    val sampleCount: Int = 0
) {
    val isGoodQuality: Boolean
        get() = signalToNoiseRatio > 15 && 
                averageAmplitude > 0.1 && 
                recordingDuration >= 3.0
}

data class FaceAnalysis(
    val hasFace: Boolean = false,
    val confidence: Double = 0.0,
    val faceCount: Int = 0,
    val isWellLit: Boolean = false,
    val isCentered: Boolean = false
) {
    val isGoodCapture: Boolean
        get() = hasFace && confidence > 0.8 && faceCount == 1 && isWellLit && isCentered
}
