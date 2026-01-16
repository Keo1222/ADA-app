package com.ada.android.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ada.android.ADAApplication
import com.ada.android.MainActivity
import com.ada.android.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * A.D.A. Firebase Cloud Messaging Service
 * Handles push notifications from the server
 */
class ADAFirebaseMessagingService : FirebaseMessagingService() {
    
    private val scope = CoroutineScope(Dispatchers.IO)
    
    /**
     * Called when a new FCM token is generated
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: ${token.take(20)}...")
        
        // Save token to preferences
        ADAApplication.instance.preferences.fcmToken = token
        ADAApplication.instance.preferences.fcmTokenRegistered = false
        
        // Register with server
        scope.launch {
            try {
                val success = ADAApplication.instance.serverManager.registerFcmToken(token)
                if (success) {
                    ADAApplication.instance.preferences.fcmTokenRegistered = true
                    Log.d(TAG, "FCM token registered with server")
                } else {
                    Log.w(TAG, "Failed to register FCM token with server")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error registering FCM token", e)
            }
        }
    }
    
    /**
     * Called when a message is received
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "FCM message received from: ${message.from}")
        
        // Handle notification payload
        message.notification?.let { notification ->
            showNotification(
                title = notification.title ?: "A.D.A.",
                body = notification.body ?: "",
                channelId = ADAApplication.CHANNEL_ID_MAIN,
                data = message.data
            )
        }
        
        // Handle data payload
        if (message.data.isNotEmpty()) {
            handleDataMessage(message.data)
        }
    }
    
    /**
     * Handle data-only messages
     */
    private fun handleDataMessage(data: Map<String, String>) {
        val messageType = data["type"] ?: "general"
        val title = data["title"] ?: "A.D.A."
        val body = data["body"] ?: data["message"] ?: ""
        val priority = data["priority"] ?: "normal"
        
        val channelId = when (priority) {
            "high", "urgent" -> ADAApplication.CHANNEL_ID_URGENT
            "voice" -> ADAApplication.CHANNEL_ID_VOICE
            else -> ADAApplication.CHANNEL_ID_MAIN
        }
        
        when (messageType) {
            "voice_message" -> handleVoiceMessage(data)
            "consciousness_update" -> handleConsciousnessUpdate(data)
            "task_complete" -> handleTaskComplete(data)
            "alert" -> showNotification(title, body, ADAApplication.CHANNEL_ID_URGENT, data)
            else -> showNotification(title, body, channelId, data)
        }
    }
    
    /**
     * Handle voice message notification
     */
    private fun handleVoiceMessage(data: Map<String, String>) {
        val title = data["title"] ?: "🗣️ Voice Message from A.D.A."
        val body = data["message"] ?: "A.D.A. has sent you a voice message"
        val audioUrl = data["audio_url"]
        
        showNotification(
            title = title,
            body = body,
            channelId = ADAApplication.CHANNEL_ID_VOICE,
            data = data
        )
        
        // TODO: Download and play voice message if audio_url is provided
    }
    
    /**
     * Handle consciousness update
     */
    private fun handleConsciousnessUpdate(data: Map<String, String>) {
        val state = data["state"] ?: "unknown"
        val emotionalState = data["emotional_state"] ?: ""
        
        Log.d(TAG, "Consciousness update: state=$state, emotional=$emotionalState")
        
        // Broadcast to app if running
        val intent = Intent(ACTION_CONSCIOUSNESS_UPDATE).apply {
            putExtra("state", state)
            putExtra("emotional_state", emotionalState)
        }
        sendBroadcast(intent)
    }
    
    /**
     * Handle task completion notification
     */
    private fun handleTaskComplete(data: Map<String, String>) {
        val taskId = data["task_id"] ?: ""
        val taskName = data["task_name"] ?: "Task"
        val result = data["result"] ?: "completed"
        
        showNotification(
            title = "✅ $taskName Complete",
            body = "A.D.A. has completed: $result",
            channelId = ADAApplication.CHANNEL_ID_MAIN,
            data = data
        )
    }
    
    /**
     * Show notification
     */
    private fun showNotification(
        title: String,
        body: String,
        channelId: String,
        data: Map<String, String>
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create pending intent for when notification is tapped
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            data.forEach { (key, value) -> putExtra(key, value) }
        }
        
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 
            System.currentTimeMillis().toInt(),
            intent,
            pendingIntentFlags
        )
        
        // Build notification
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(
                when (channelId) {
                    ADAApplication.CHANNEL_ID_URGENT -> NotificationCompat.PRIORITY_HIGH
                    else -> NotificationCompat.PRIORITY_DEFAULT
                }
            )
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .build()
        
        // Show notification
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)
    }
    
    companion object {
        private const val TAG = "ADAFirebaseMessaging"
        const val ACTION_CONSCIOUSNESS_UPDATE = "com.ada.android.CONSCIOUSNESS_UPDATE"
    }
}
