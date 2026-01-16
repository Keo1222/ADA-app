# A.D.A. Android Application

**Advanced Digital Assistant - Android Native Client**

[![Android](https://img.shields.io/badge/Android-13%2B-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.10-purple.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Compose-2024.05-blue.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)](LICENSE)

## Overview

The A.D.A. Android app is a native Kotlin application that provides a mobile interface to the A.D.A. cognitive AI system. Built with Jetpack Compose for a modern, responsive UI, it supports:

- 🔐 Biometric authentication (fingerprint/face)
- 🎤 Voice enrollment and recognition
- 📹 Face capture for identity verification
- 🔔 Real-time push notifications via adaBASE
- 🌐 WebSocket connectivity for live consciousness updates
- 💬 Natural language conversation interface

## System Requirements

| Requirement | Version |
|------------|---------|
| Android | 13+ (API 33+) |
| Min SDK | 33 (Tiramisu) |
| Target SDK | 34 |
| Kotlin | 1.9.10 |
| Java | 17 |

> **Note:** This app requires Android 13 or newer. It is not compatible with older Android versions.

## Architecture

```
android_app/
├── app/
│   ├── build.gradle.kts          # Module build config & dependencies
│   ├── proguard-rules.pro        # ProGuard obfuscation rules
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           ├── java/com/ada/android/
│           │   ├── ADAApplication.kt        # Application entry, notification channels
│           │   ├── MainActivity.kt          # Main activity, permission handling
│           │   ├── auth/
│           │   │   └── ADAAuthManager.kt    # Biometric & token authentication
│           │   ├── data/
│           │   │   └── ADAPreferences.kt    # Encrypted SharedPreferences
│           │   ├── network/
│           │   │   └── ADAServerManager.kt  # HTTP/WebSocket server communication
│           │   ├── services/
│           │   │   └── ADAFirebaseMessagingService.kt  # FCM push notifications
│           │   └── ui/
│           │       ├── auth/
│           │       │   └── AuthScreen.kt    # Login/registration screens
│           │       ├── main/
│           │       │   └── MainScreen.kt    # Main conversation interface
│           │       └── theme/
│           │           └── Theme.kt         # Material3 theming
│           └── res/                         # Layouts, drawables, strings
├── build.gradle.kts              # Project-level build config
├── gradle.properties             # Gradle settings
└── settings.gradle.kts           # Module settings
```

## Server Configuration

The Android app connects to the A.D.A. server using specific ports:

| Service | Port | Description |
|---------|------|-------------|
| Main Server | 5112 | Primary API endpoint for Android |
| Android Proxy | 5113 | Android-specific proxy/gateway |
| Consciousness | 5003 | Shared consciousness WebSocket |

> **Port Comparison:**
> - Android: 5112 (main), 5113 (proxy)
> - iOS: 5001 (main), 5002 (proxy)
> - Both share consciousness port 5003

### Environment Variables

Server configuration is defined in `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "SERVER_IP", "\"62.30.154.250\"")
buildConfigField("int", "MAIN_SERVER_PORT", "5112")
buildConfigField("int", "ANDROID_PROXY_PORT", "5113")
buildConfigField("int", "CONSCIOUSNESS_PORT", "5003")
buildConfigField("String", "ANDROID_AUTH_KEY", "\"ada_neural_pathway_key\"")
```

Access in code via `BuildConfig`:

```kotlin
val serverUrl = "http://${BuildConfig.SERVER_IP}:${BuildConfig.MAIN_SERVER_PORT}"
```

## Push Notifications

The app uses **adaBASE** for push notification routing instead of direct Firebase FCM:

1. App registers device token with adaBASE server
2. adaBASE stores device info and handles notification delivery
3. FCM is still used as transport layer, but adaBASE manages routing

### Notification Channels

| Channel ID | Name | Priority |
|-----------|------|----------|
| `ada_notifications` | A.D.A. | Default |
| `ada_urgent` | Urgent Notifications | High |
| `ada_voice` | Voice Messages | Default |

### Android 13+ Permissions

Android 13+ requires runtime permission for notifications:

```kotlin
// MainActivity.kt handles this automatically
private fun requestNotificationPermissionIfNeeded() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
```

## Building the App

### Prerequisites

1. **Android Studio** (Hedgehog 2023.1.1 or newer recommended)
2. **JDK 17** (bundled with Android Studio)
3. **Android SDK** (API 33+)
4. **Firebase project** with `google-services.json`

### Setup Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/budgierless/ada_app.git
   cd ada_app/android_app
   ```

2. Place Firebase config file:
   ```bash
   # Copy google-services.json to app directory
   cp /path/to/google-services.json app/
   ```

3. Open in Android Studio:
   ```bash
   # Or open android_app folder directly in Android Studio
   studio .
   ```

4. Sync Gradle and build:
   - Click **Sync Now** when prompted
   - Select **Build > Make Project**

### Build Commands (Command Line)

```bash
# Debug build
./gradlew assembleDebug

# Release build (requires signing config)
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test

# Generate APK
./gradlew build
```

### APK Output Locations

| Build Type | Location |
|-----------|----------|
| Debug | `app/build/outputs/apk/debug/app-debug.apk` |
| Release | `app/build/outputs/apk/release/app-release.apk` |

## Dependencies

### Core Libraries

| Library | Version | Purpose |
|---------|---------|---------|
| AndroidX Core | 1.13.1 | Core Android functionality |
| Compose BOM | 2024.05.00 | UI framework |
| Material3 | - | Material Design 3 |
| Navigation Compose | 2.7.7 | In-app navigation |

### Networking

| Library | Version | Purpose |
|---------|---------|---------|
| Retrofit | 2.11.0 | HTTP client |
| OkHttp | 4.12.0 | HTTP transport |
| Socket.IO Client | 2.1.0 | WebSocket/real-time |

### Security & Auth

| Library | Version | Purpose |
|---------|---------|---------|
| Biometric | 1.2.0-alpha05 | Fingerprint/face auth |
| Security Crypto | 1.1.0-alpha06 | Encrypted preferences |

### Media

| Library | Version | Purpose |
|---------|---------|---------|
| CameraX | 1.3.4 | Camera for face capture |
| Media | 1.7.0 | Audio recording |

### Firebase

| Library | Version | Purpose |
|---------|---------|---------|
| Firebase BOM | 33.1.0 | Firebase dependency management |
| Firebase Messaging | - | Push notifications |
| Firebase Analytics | - | Usage analytics |

## Security Features

### Encrypted Storage

User credentials and tokens are stored using `EncryptedSharedPreferences`:

```kotlin
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val encryptedPrefs = EncryptedSharedPreferences.create(
    context,
    "ada_secure_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

### Biometric Authentication

The app supports:
- Fingerprint recognition
- Face unlock (device-dependent)
- Class 3 (Strong) biometric authentication

### Network Security

- All connections use HTTPS in production
- Certificate pinning can be enabled
- Auth tokens transmitted via secure headers

## Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumentation Tests
```bash
./gradlew connectedAndroidTest
```

### Manual Testing Checklist

- [ ] App launches on Android 13+ device
- [ ] Notification permission requested on first launch
- [ ] Biometric authentication works
- [ ] Server connection established
- [ ] Push notifications received
- [ ] Voice recording works
- [ ] Camera capture works

## Troubleshooting

### Common Issues

1. **Build fails with "google-services.json not found"**
   - Ensure `google-services.json` is in `app/` directory
   - File should come from Firebase Console

2. **Notification permission denied**
   - User can enable in Settings > Apps > A.D.A. > Notifications

3. **Server connection fails**
   - Verify server IP and ports in `build.gradle.kts`
   - Check firewall allows ports 5112, 5113, 5003

4. **Biometric not available**
   - Ensure device has enrolled fingerprint/face
   - Check Android security settings

### Debug Logging

Enable verbose logging:

```kotlin
// In ADAServerManager.kt
private const val DEBUG = true
if (DEBUG) Log.d(TAG, "Message")
```

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2025-01 | Initial release with Android 13+ support |

## Related Components

- **iOS App:** `ADA_IOS_App/` - Swift/SwiftUI iOS client
- **Server:** `server/` - Python backend with Flask
- **adaBASE:** `server/ada_base/` - Push notification server
- **Plugins:** `server/plugins/android_integration.py`

## License

Proprietary - All rights reserved

---

**A.D.A.** - *Advanced Digital Assistant*
