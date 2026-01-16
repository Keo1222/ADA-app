package com.ada.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.ada.android.data.ADAPreferences
import com.ada.android.network.ADAServerManager

/**
 * A.D.A. Android Application
 * Main application class for initialization
 */
class ADAApplication : Application() {
    
    lateinit var preferences: ADAPreferences
        private set
    
    lateinit var serverManager: ADAServerManager
        private set
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Initialize preferences
        preferences = ADAPreferences(this)
        
        // Initialize server manager
        serverManager = ADAServerManager(this)
        
        // Create notification channels
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        // Notification channels are required for Android 8.0 (API 26) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            
            // Main notification channel
            val mainChannel = NotificationChannel(
                CHANNEL_ID_MAIN,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.notification_channel_description)
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(mainChannel)
            
            // High priority channel for urgent notifications
            val urgentChannel = NotificationChannel(
                CHANNEL_ID_URGENT,
                "Urgent Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent notifications from A.D.A."
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(urgentChannel)
            
            // Voice message channel
            val voiceChannel = NotificationChannel(
                CHANNEL_ID_VOICE,
                "Voice Messages",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Voice messages from A.D.A."
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(voiceChannel)
        }
    }
    
    companion object {
        const val CHANNEL_ID_MAIN = "ada_notifications"
        const val CHANNEL_ID_URGENT = "ada_urgent"
        const val CHANNEL_ID_VOICE = "ada_voice"
        
        lateinit var instance: ADAApplication
            private set
    }
}
