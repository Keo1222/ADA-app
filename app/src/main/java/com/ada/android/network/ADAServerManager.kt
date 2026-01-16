package com.ada.android.network

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.ada.android.BuildConfig
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * A.D.A. Server Manager
 * Handles HTTP API calls and WebSocket connections to A.D.A. server
 * Equivalent to iOS ADAServerManager.swift
 */
class ADAServerManager(private val context: Context) {
    
    // Connection state
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _lastHeartbeat = MutableStateFlow<Long?>(null)
    val lastHeartbeat: StateFlow<Long?> = _lastHeartbeat.asStateFlow()
    
    private val _serverVersion = MutableStateFlow<String?>(null)
    val serverVersion: StateFlow<String?> = _serverVersion.asStateFlow()
    
    private val _latencyMs = MutableStateFlow(0)
    val latencyMs: StateFlow<Int> = _latencyMs.asStateFlow()
    
    val isConnected: Boolean
        get() = _connectionState.value == ConnectionState.CONNECTED
    
    // Server URLs
    val httpServerUrl: String = "http://${BuildConfig.SERVER_IP}:${BuildConfig.ANDROID_PROXY_PORT}"
    val mainServerUrl: String = "http://${BuildConfig.SERVER_IP}:${BuildConfig.MAIN_SERVER_PORT}"
    val wsServerUrl: String = "ws://${BuildConfig.SERVER_IP}:${BuildConfig.MAIN_SERVER_PORT}"
    
    // Auth
    private var authToken: String? = null
    private var fcmToken: String? = null
    
    // HTTP Client with timeouts for older Android compatibility
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    
    // Socket.IO client
    private var socket: Socket? = null
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 5
    private val mainHandler = Handler(Looper.getMainLooper())
    
    /**
     * Configure with authentication token
     */
    fun configure(authToken: String?, fcmToken: String?) {
        this.authToken = authToken
        this.fcmToken = fcmToken
    }
    
    /**
     * Connect to server (WebSocket)
     */
    fun connect() {
        if (_connectionState.value == ConnectionState.CONNECTING) return
        _connectionState.value = ConnectionState.CONNECTING
        
        try {
            val options = IO.Options().apply {
                transports = arrayOf("websocket")
                reconnection = true
                reconnectionAttempts = maxReconnectAttempts
                reconnectionDelay = 1000
                timeout = 30000
                
                // Add auth headers
                extraHeaders = mutableMapOf(
                    "X-API-Key" to listOf(BuildConfig.ANDROID_AUTH_KEY)
                )
                authToken?.let {
                    extraHeaders["Authorization"] = listOf("Bearer $it")
                }
            }
            
            socket = IO.socket(wsServerUrl, options)
            
            socket?.apply {
                on(Socket.EVENT_CONNECT) {
                    mainHandler.post {
                        _connectionState.value = ConnectionState.CONNECTED
                        _lastHeartbeat.value = System.currentTimeMillis()
                        reconnectAttempts = 0
                        Log.d(TAG, "Socket.IO connected")
                    }
                }
                
                on(Socket.EVENT_DISCONNECT) {
                    mainHandler.post {
                        _connectionState.value = ConnectionState.DISCONNECTED
                        Log.d(TAG, "Socket.IO disconnected")
                    }
                }
                
                on(Socket.EVENT_CONNECT_ERROR) { args ->
                    val error = args.firstOrNull()?.toString() ?: "Unknown error"
                    mainHandler.post {
                        _connectionState.value = ConnectionState.ERROR
                        Log.e(TAG, "Socket.IO connection error: $error")
                    }
                }
                
                // A.D.A. specific events
                on("ada_response") { args ->
                    val data = args.firstOrNull()
                    Log.d(TAG, "A.D.A. response: $data")
                    // Broadcast to listeners
                }
                
                on("consciousness_update") { args ->
                    val data = args.firstOrNull()
                    Log.d(TAG, "Consciousness update: $data")
                }
                
                on("emotion_state") { args ->
                    val data = args.firstOrNull()
                    Log.d(TAG, "Emotion state: $data")
                }
                
                connect()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Socket.IO setup error", e)
            _connectionState.value = ConnectionState.ERROR
        }
    }
    
    /**
     * Disconnect from server
     */
    fun disconnect() {
        socket?.disconnect()
        socket?.close()
        socket = null
        _connectionState.value = ConnectionState.DISCONNECTED
        reconnectAttempts = 0
    }
    
    /**
     * Check server health
     */
    suspend fun checkServerHealth(): Boolean = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()
            val response = get("/api/health")
            val latency = (System.currentTimeMillis() - startTime).toInt()
            
            _latencyMs.value = latency
            _lastHeartbeat.value = System.currentTimeMillis()
            
            if (response != null) {
                _serverVersion.value = response.optString("version", null)
            }
            
            response != null
        } catch (e: Exception) {
            Log.e(TAG, "Health check failed", e)
            false
        }
    }
    
    /**
     * GET request
     */
    suspend fun get(endpoint: String): JSONObject? = withContext(Dispatchers.IO) {
        val url = "$httpServerUrl$endpoint"
        val request = Request.Builder()
            .url(url)
            .addHeader("Accept", "application/json")
            .addHeader("X-API-Key", BuildConfig.ANDROID_AUTH_KEY)
            .apply {
                authToken?.let { addHeader("Authorization", "Bearer $it") }
            }
            .get()
            .build()
        
        executeRequest(request)
    }
    
    /**
     * POST request with JSON body
     */
    suspend fun post(endpoint: String, body: Map<String, Any>): JSONObject? = withContext(Dispatchers.IO) {
        val url = "$httpServerUrl$endpoint"
        val jsonBody = JSONObject(body).toString()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        
        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .addHeader("X-API-Key", BuildConfig.ANDROID_AUTH_KEY)
            .apply {
                authToken?.let { addHeader("Authorization", "Bearer $it") }
            }
            .post(jsonBody.toRequestBody(mediaType))
            .build()
        
        executeRequest(request)
    }
    
    /**
     * Execute HTTP request
     */
    private suspend fun executeRequest(request: Request): JSONObject? = 
        suspendCancellableCoroutine { continuation ->
            val startTime = System.currentTimeMillis()
            
            httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "Request failed: ${request.url}", e)
                    if (continuation.isActive) {
                        continuation.resumeWithException(e)
                    }
                }
                
                override fun onResponse(call: Call, response: Response) {
                    val latency = (System.currentTimeMillis() - startTime).toInt()
                    _latencyMs.value = latency
                    
                    try {
                        val body = response.body?.string()
                        if (body != null) {
                            val json = JSONObject(body)
                            if (continuation.isActive) {
                                continuation.resume(json)
                            }
                        } else {
                            if (continuation.isActive) {
                                continuation.resume(null)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Response parsing failed", e)
                        if (continuation.isActive) {
                            continuation.resumeWithException(e)
                        }
                    } finally {
                        response.close()
                    }
                }
            })
            
            continuation.invokeOnCancellation {
                // Cancel the request if coroutine is cancelled
            }
        }
    
    /**
     * Send message to A.D.A.
     */
    suspend fun sendMessage(message: String): JSONObject? {
        val body = mapOf(
            "message" to message,
            "user_id" to (authToken ?: "android_user")
        )
        return post("/api/chat", body)
    }
    
    /**
     * Execute command on server
     */
    suspend fun executeCommand(command: String, parameters: Map<String, Any> = emptyMap()): JSONObject? {
        val body = parameters.toMutableMap()
        body["command"] = command
        return post("/api/android/command", body)
    }
    
    /**
     * Get consciousness state
     */
    suspend fun getConsciousnessState(): JSONObject? {
        return get("/api/android/consciousness/state")
    }
    
    /**
     * Get emotion state
     */
    suspend fun getEmotionState(): JSONObject? {
        return get("/api/android/emotion/state")
    }
    
    /**
     * Get system status
     */
    suspend fun getSystemStatus(): JSONObject? {
        return get("/api/android/system/status")
    }
    
    /**
     * Register FCM token for push notifications
     */
    suspend fun registerFcmToken(token: String): Boolean {
        this.fcmToken = token
        
        val body = mapOf(
            "device_token" to token,
            "platform" to "android",
            "app_version" to BuildConfig.VERSION_NAME,
            "device_info" to mapOf(
                "sdk_version" to android.os.Build.VERSION.SDK_INT,
                "device_model" to android.os.Build.MODEL,
                "manufacturer" to android.os.Build.MANUFACTURER
            )
        )
        
        val response = post("/api/android/push/register", body)
        return response?.optBoolean("success", false) ?: false
    }
    
    /**
     * Unregister FCM token
     */
    suspend fun unregisterFcmToken(): Boolean {
        val token = fcmToken ?: return true
        
        val body = mapOf("device_token" to token)
        post("/api/android/push/unregister", body)
        fcmToken = null
        return true
    }
    
    companion object {
        private const val TAG = "ADAServerManager"
    }
}

/**
 * Connection state enum
 */
enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}
