# A.D.A. Android App - Build Tutorial

This guide walks you through compiling the A.D.A. Android app into an installable APK file.

---

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

### Required Software

| Software | Version | Download |
|----------|---------|----------|
| **Java Development Kit (JDK)** | 17+ | [Download JDK 17](https://adoptium.net/temurin/releases/?version=17) |
| **Android Studio** | Hedgehog (2023.1.1) or newer | [Download Android Studio](https://developer.android.com/studio) |
| **Git** | Any recent version | [Download Git](https://git-scm.com/downloads) |

### System Requirements

- **OS**: Windows 10/11, macOS 11+, or Linux (Ubuntu 20.04+)
- **RAM**: 8GB minimum (16GB recommended)
- **Disk Space**: 10GB free
- **Internet**: Required for downloading dependencies

---

## 🔐 Step 1: Set Up Firebase (Required)

The app uses Firebase for push notifications. You need a `google-services.json` file.

### Option A: Use Your Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **Add Project** or select an existing project
3. Click **Add App** → **Android**
4. Enter package name: `com.ada.android`
5. Download `google-services.json`
6. Save it for Step 4

### Option B: Use Placeholder (For Testing Only)

If you just want to test the build without Firebase:

```bash
cd android_app/app
cp google-services.json.template google-services.json
```

> ⚠️ **Warning**: Push notifications won't work with the placeholder file.

---

## 📥 Step 2: Clone the Repository

Open a terminal and run:

```bash
# Clone the repository
git clone https://github.com/budgierless/ada_app.git

# Navigate to Android project
cd ada_app/android_app
```

---

## 🔧 Step 3: Set Up Gradle Wrapper

The project needs Gradle to build. If the wrapper files are missing:

### Windows (PowerShell)

```powershell
# Check if Gradle is installed
gradle --version

# If not installed, download from https://gradle.org/releases/
# After installing, run:
gradle wrapper --gradle-version 8.4
```

### macOS/Linux (Terminal)

```bash
# Install Gradle via Homebrew (macOS)
brew install gradle

# Or via SDKMAN (cross-platform)
sdk install gradle 8.4

# Generate wrapper
gradle wrapper --gradle-version 8.4
```

After this step, you should have:
- `gradlew` (Unix script)
- `gradlew.bat` (Windows script)
- `gradle/wrapper/gradle-wrapper.jar`

---

## 📄 Step 4: Add Firebase Configuration

Copy your `google-services.json` file:

```bash
# Copy to app directory
cp /path/to/your/google-services.json app/google-services.json
```

Verify it exists:
```bash
ls -la app/google-services.json
```

---

## 🏗️ Step 5: Build the APK

### Option A: Command Line Build (Recommended)

#### Debug Build (for testing)

```bash
# Windows
.\gradlew.bat assembleDebug

# macOS/Linux
./gradlew assembleDebug
```

**Output location:**
```
app/build/outputs/apk/debug/app-debug.apk
```

#### Release Build (for distribution)

```bash
# Windows
.\gradlew.bat assembleRelease

# macOS/Linux
./gradlew assembleRelease
```

**Output location:**
```
app/build/outputs/apk/release/app-release-unsigned.apk
```

### Option B: Android Studio Build

1. Open Android Studio
2. Click **File** → **Open**
3. Navigate to `ada_app/android_app` and click **OK**
4. Wait for Gradle sync to complete
5. Click **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**
6. Click "locate" in the notification to find the APK

---

## 📱 Step 6: Install the APK

### Via USB (Debug Build)

1. Enable **Developer Options** on your Android device:
   - Go to **Settings** → **About Phone**
   - Tap **Build Number** 7 times

2. Enable **USB Debugging**:
   - Go to **Settings** → **Developer Options**
   - Toggle **USB Debugging** ON

3. Connect device via USB

4. Install APK:
   ```bash
   # Windows
   .\gradlew.bat installDebug
   
   # macOS/Linux
   ./gradlew installDebug
   ```

### Via File Transfer

1. Copy APK to device via USB/email/cloud storage
2. Open file manager on device
3. Navigate to APK location
4. Tap to install
5. If prompted, allow **Install from unknown sources**

---

## 🔑 Step 7: Sign for Release (Optional)

For Play Store or production distribution, you need a signed APK.

### Create Keystore

```bash
keytool -genkey -v -keystore ada-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias ada
```

Follow the prompts to set passwords and certificate info.

### Configure Signing in build.gradle.kts

Add to `app/build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../ada-release-key.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "your_password"
            keyAlias = "ada"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "your_password"
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

### Build Signed APK

```bash
# Windows
.\gradlew.bat assembleRelease

# macOS/Linux
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

---

## 🔄 Useful Gradle Commands

| Command | Description |
|---------|-------------|
| `./gradlew assembleDebug` | Build debug APK |
| `./gradlew assembleRelease` | Build release APK |
| `./gradlew installDebug` | Build and install on device |
| `./gradlew clean` | Clean build directory |
| `./gradlew test` | Run unit tests |
| `./gradlew connectedAndroidTest` | Run instrumentation tests |
| `./gradlew dependencies` | List all dependencies |
| `./gradlew signingReport` | Show signing info |

---

## ❓ Troubleshooting

### Error: "google-services.json not found"

```
File google-services.json is missing. The Google Services Plugin cannot function without it.
```

**Solution**: Copy Firebase config file to `app/google-services.json`

---

### Error: "SDK location not found"

```
SDK location not found. Define location with sdk.dir in local.properties
```

**Solution**: Create `local.properties`:

```properties
# Windows
sdk.dir=C:\\Users\\YourName\\AppData\\Local\\Android\\Sdk

# macOS
sdk.dir=/Users/YourName/Library/Android/sdk

# Linux
sdk.dir=/home/YourName/Android/Sdk
```

---

### Error: "Java version mismatch"

```
Android Gradle plugin requires Java 17 to run
```

**Solution**: 
1. Install JDK 17
2. Set JAVA_HOME:
   ```bash
   # Windows (PowerShell)
   $env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.9.9-hotspot"
   
   # macOS/Linux
   export JAVA_HOME=$(/usr/libexec/java_home -v 17)
   ```

---

### Error: "Gradle sync failed"

**Solution**:
1. Click **File** → **Invalidate Caches / Restart**
2. Delete `.gradle` and `build` folders
3. Re-sync project

---

### Error: "minSdk 33 required"

```
The device requires API level 33 but app has minSdk 21
```

**Solution**: This app requires Android 13+. Use a device/emulator with Android 13 or newer.

---

## 📊 Build Output Summary

| Build Type | Location | Signed | Minified |
|-----------|----------|--------|----------|
| Debug | `app/build/outputs/apk/debug/app-debug.apk` | Debug key | No |
| Release (unsigned) | `app/build/outputs/apk/release/app-release-unsigned.apk` | No | Yes |
| Release (signed) | `app/build/outputs/apk/release/app-release.apk` | Yes | Yes |

---

## 📞 Support

If you encounter issues:

1. Check the [Troubleshooting](#-troubleshooting) section
2. Review Android Studio's **Build** output window
3. Check `app/build/outputs/logs/` for detailed logs

---

**A.D.A.** - *Advanced Digital Assistant*
