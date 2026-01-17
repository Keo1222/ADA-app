package com.ada.android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import com.ada.android.auth.ADAAuthManager
import com.ada.android.ui.auth.AuthScreen
import com.ada.android.ui.main.MainScreen
import com.ada.android.ui.theme.ADATheme

/**
 * A.D.A. Android Main Activity
 * Entry point for the Android application
 * 
 * Compatible with Android 13+ (API 33+)
 */
class MainActivity : ComponentActivity() {
    
    private lateinit var authManager: ADAAuthManager
    
    // Android 13+ notification permission request launcher
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Notification permission granted, FCM will work
            android.util.Log.d("MainActivity", "Notification permission granted")
        } else {
            // Permission denied - notifications will not work
            android.util.Log.w("MainActivity", "Notification permission denied")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request notification permission for Android 13+
        requestNotificationPermissionIfNeeded()
        
        val app = application as ADAApplication
        authManager = ADAAuthManager(
            context = this,
            preferences = app.preferences,
            serverManager = app.serverManager
        )
        
        setContent {
            ADATheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val isAuthenticated by authManager.isAuthenticated.collectAsState()
                    
                    if (isAuthenticated) {
                        MainScreen(
                            authManager = authManager,
                            serverManager = app.serverManager
                        )
                    } else {
                        AuthScreen(
                            authManager = authManager,
                            activity = this@MainActivity
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Request notification permission for Android 13+ (API 33+)
     * This is required for FCM push notifications to work on Android 13+
     */
    private fun requestNotificationPermissionIfNeeded() {
        // Only needed for Android 13+ (TIRAMISU = API 33)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                    android.util.Log.d("MainActivity", "Notification permission already granted")
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Explain to user why we need this permission
                    // For now, just request - could show a dialog first
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Request the permission
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}
