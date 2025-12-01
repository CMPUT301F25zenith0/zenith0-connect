# Zenith-Connect Setup Guide

This guide will help you set up and run the Zenith-Connect Android application on your local development environment.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Firebase Setup](#firebase-setup)
3. [Google Maps API Setup](#google-maps-api-setup)
4. [Project Configuration](#project-configuration)
5. [Building and Running](#building-and-running)
6. [Testing](#testing)
7. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Software

- **Android Studio** (Koala or later recommended, AGP 8.13.0 compatible)
  - Download from: https://developer.android.com/studio
  - Minimum version: Android Studio Giraffe (2022.3.1) or later
- **JDK 11** or later
  - Android Studio typically includes this, but verify in Project Structure
- **Android SDK** with the following:
  - Minimum SDK: 24 (Android 7.0)
  - Target SDK: 36
  - Compile SDK: 36
- **Gradle 8.13** (included via wrapper)
- **Git** (for version control)

### Android Device or Emulator

- Physical Android device (API 24+) with USB debugging enabled, OR
- Android Emulator (API 24+) configured in Android Studio

---

## Firebase Setup

### 1. Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project" or select existing project
3. Follow the setup wizard:
   - Enter project name: `zenithconnect-bf445` (or your preferred name)
   - Enable Google Analytics (optional)
   - Create project
4. **Note**: If using an existing project, ensure the project ID matches or update `google-services.json` accordingly

### 2. Add Android App to Firebase

1. In Firebase Console, click the Android icon to add an Android app
2. Enter package name: `com.example.connect`
3. Register app and download `google-services.json`
4. **Important**: Replace the existing `google-services.json` in `app/` directory with your downloaded file

### 3. Enable Firebase Services

Enable the following services in Firebase Console:

#### Firebase Authentication

1. Go to **Authentication** → **Sign-in method**
2. Enable **Email/Password** provider
3. Save changes

#### Cloud Firestore

1. Go to **Firestore Database**
2. Click **Create database**
3. Choose **Start in test mode** (for development) or **Production mode** (for production)
4. Select a location for your database
5. Click **Enable**

#### Firebase Storage (if needed)

1. Go to **Storage**
2. Click **Get started**
3. Start in test mode (for development)
4. Select a location

### 4. Configure Firestore Security Rules

Update your Firestore rules in Firebase Console → Firestore Database → Rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Add your security rules here
    // Example: Allow authenticated users to read/write
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

---

## Google Maps API Setup

### 1. Get Google Maps API Key

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. **Important**: Select the same project as your Firebase project (`zenithconnect-bf445` or your project name)
3. Navigate to **APIs & Services** → **Library**
4. Search for "Maps SDK for Android"
5. Click **Enable**
6. Also enable "Places API" if you plan to use location search features

### 2. Create API Key

1. Go to **APIs & Services** → **Credentials**
2. Click **Create Credentials** → **API Key**
3. Copy the generated API key

### 3. Configure API Key in Project

1. Open `app/src/main/AndroidManifest.xml`
2. Find the Google Maps API key meta-data:
   ```xml
   <meta-data
       android:name="com.google.android.geo.API_KEY"
       android:value="YOUR_API_KEY_HERE" />
   ```
3. Replace `YOUR_API_KEY_HERE` with your actual API key

### 4. Restrict API Key (Recommended for Production)

1. In Google Cloud Console → **Credentials**
2. Click on your API key
3. Under **API restrictions**, select **Restrict key**
4. Select **Maps SDK for Android**
5. Save changes

---

## Project Configuration

### 1. Clone/Download Project

```bash
git clone <repository-url>
cd code
```

### 2. Open Project in Android Studio

1. Launch Android Studio
2. Select **File** → **Open**
3. Navigate to the project directory (`code/`)
4. Click **OK**

### 3. Sync Gradle Files

1. Android Studio should automatically prompt to sync Gradle files
2. If not, click **File** → **Sync Project with Gradle Files**
3. Wait for dependencies to download (this may take several minutes)
4. **Note**: First sync may take 5-10 minutes depending on internet speed

### 4. Verify Configuration

Check the following files are properly configured:

- `app/google-services.json` - Should contain your Firebase project configuration
- `app/src/main/AndroidManifest.xml` - Should have your Google Maps API key
- `local.properties` - Should have your Android SDK path (auto-generated)

### 5. Update Package Name (if needed)

If you're using a different Firebase project:

1. Update `applicationId` in `app/build.gradle.kts`:
   ```kotlin
   applicationId = "com.example.connect"
   ```
2. Ensure it matches the package name in Firebase Console

---

## Building and Running

### 1. Connect Device or Start Emulator

#### Physical Device:

1. Enable **Developer Options** on your Android device:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
2. Enable **USB Debugging**:
   - Settings → Developer Options → USB Debugging
3. Connect device via USB
4. Allow USB debugging when prompted

#### Emulator:

1. Open **AVD Manager** in Android Studio (Tools → Device Manager)
2. Click **Create Device**
3. Select a device (e.g., Pixel 5)
4. Select a system image (API 24 or higher)
5. Finish setup and click **Play** button

### 2. Build Project

1. In Android Studio, click **Build** → **Make Project** (or press `Ctrl+F9` / `Cmd+F9`)
2. Wait for build to complete
3. Check for any errors in the **Build** tab

### 3. Run Application

1. Click the **Run** button (green play icon) or press `Shift+F10` / `Ctrl+R`
2. Select your device/emulator
3. Wait for the app to install and launch

### 4. First Launch

- The app will start with the main screen
- You can create an account or log in
- For admin access, ensure your Firebase user has `admin: true` in Firestore
- **Note**: The app uses a foreground service for notifications. On first launch, you may need to grant notification permissions (Android 13+)

---

## Testing

### Running Unit Tests

1. Right-click on `app/src/test/java` folder
2. Select **Run 'Tests in 'test''**
3. Or run individual test files:
   - Right-click on a test file → **Run**

### Running Instrumented Tests

1. Connect a device or start an emulator
2. Right-click on `app/src/androidTest/java` folder
3. Select **Run 'Tests in 'androidTest''**

### Known Test Configuration

The project uses Robolectric for unit tests with specific configurations:

- Robolectric SDK version: 28
- Firestore persistence disabled for tests

---

## Troubleshooting

### Common Issues

#### 1. Gradle Sync Failed

- **Solution**:
  - Check internet connection
  - Invalidate caches: **File** → **Invalidate Caches** → **Invalidate and Restart**
  - Check `gradle/wrapper/gradle-wrapper.properties` for correct Gradle version

#### 2. Firebase Connection Issues

- **Solution**:
  - Verify `google-services.json` is in `app/` directory
  - Check package name matches Firebase project
  - Ensure Firebase services are enabled in Firebase Console

#### 3. Google Maps Not Loading

- **Solution**:
  - Verify API key is correctly set in `AndroidManifest.xml`
  - Check Maps SDK is enabled in Google Cloud Console
  - Ensure API key restrictions allow your app's package name

#### 4. Build Errors Related to Dependencies

- **Solution**:
  - Clean project: **Build** → **Clean Project**
  - Rebuild: **Build** → **Rebuild Project**
  - Sync Gradle files again

#### 5. Permission Denied Errors

- **Solution**:
  - Check device/emulator has required permissions enabled
  - For location: Settings → Apps → Connect → Permissions → Location
  - For camera: Grant camera permission when prompted
  - For notifications (Android 13+): Settings → Apps → Connect → Permissions → Notifications
  - For notification listener service: Settings → Apps → Special app access → Notification access → Enable for Connect

#### 6. Firestore Rules Errors

- **Solution**:
  - Check Firestore security rules in Firebase Console
  - For development, use test mode rules
  - Ensure user is authenticated before accessing Firestore

#### 7. Robolectric Test Failures

- **Solution**:
  - Verify test configuration in `app/build.gradle.kts`
  - Check that `robolectric.enabledSdks` is set to 28
  - Ensure Firestore persistence is disabled for tests

#### 8. Notification Listener Service Not Working

- **Solution**:
  - Go to device Settings → Apps → Special app access → Notification access
  - Enable notification access for the Connect app
  - Restart the app after enabling
  - Note: This is required for the app to receive event notifications

### Getting Help

- Check Android Studio's **Build** tab for detailed error messages
- Review Firebase Console logs for backend issues
- Check device logcat: **View** → **Tool Windows** → **Logcat**

---

## Project Structure

```
code/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/connect/
│   │   │   │   ├── activities/      # Activity classes
│   │   │   │   ├── models/          # Data models
│   │   │   │   ├── network/         # Network services
│   │   │   │   ├── utils/           # Utility classes
│   │   │   │   └── constants/       # App constants
│   │   │   ├── res/                 # Resources (layouts, strings, etc.)
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                    # Unit tests
│   │   └── androidTest/             # Instrumented tests
│   ├── google-services.json         # Firebase config
│   └── build.gradle.kts            # App-level build config
├── build.gradle.kts                # Project-level build config
└── gradle/                         # Gradle wrapper files
```

---

## Additional Notes

### Development Environment

- **Java Version**: 11
- **Kotlin**: Used for build scripts (Gradle)
- **Gradle Version**: 8.13 (via wrapper)
- **Android Gradle Plugin (AGP)**: 8.13.0
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36
- **Compile SDK**: 36

### Key Dependencies

- Firebase Authentication (v24.0.1)
- Cloud Firestore (v26.0.2)
- Google Maps SDK (v18.2.0)
- Google Play Services Location (v21.0.1)
- Material Design Components (v1.13.0)
- ZXing Android Embedded (v4.3.0) - QR Code scanning
- Glide (v4.16.0) - Image loading
- Robolectric (v4.13) - Unit testing

### Important Files to Configure

1. `app/google-services.json` - Firebase configuration
2. `app/src/main/AndroidManifest.xml` - Google Maps API key
3. `app/build.gradle.kts` - App dependencies and configuration

---

## Next Steps

After setup:

1. Create a test account in the app
2. Explore the admin dashboard (if you have admin privileges)
3. Test event creation and management
4. Test QR code scanning functionality
5. Test map visualization features

---

**Last Updated**: 2025
**Project Version**: 3.0
