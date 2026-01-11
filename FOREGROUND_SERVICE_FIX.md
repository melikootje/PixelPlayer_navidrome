# Navidrome Sync Crash Fix

## Issue
App crashed when starting Navidrome sync with:
```
android.app.InvalidForegroundServiceTypeException: Starting FGS with type none
```

## Root Cause
Android 14+ (API 34+) requires foreground services to explicitly declare their type. The WorkManager's SystemForegroundService was starting without a proper foreground service type declared.

## Solution

### 1. AndroidManifest.xml Changes

#### Added Permission:
```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC"/>
```

#### Added Service Declaration:
```xml
<service
    android:name="androidx.work.impl.foreground.SystemForegroundService"
    android:foregroundServiceType="dataSync"
    tools:node="merge" />
```

### 2. NavidromeSyncWorker.kt Changes

Updated `createForegroundInfo()` to specify the service type:

```kotlin
private fun createForegroundInfo(progress: String): ForegroundInfo {
    // ... notification setup ...
    
    // Specify foreground service type for Android 14+ (API 34+)
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ForegroundInfo(
            1001, 
            notification,
            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    } else {
        ForegroundInfo(1001, notification)
    }
}
```

## Testing

1. Clean and rebuild:
```bash
./gradlew clean assembleDebug
```

2. Install:
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

3. Trigger Navidrome sync from Settings

4. Verify no crash and check logcat:
```bash
adb logcat -s NavidromeSyncWorker
```

## Expected Behavior
✅ Sync starts without crash
✅ Notification shows "Syncing Music Library" with progress
✅ Foreign key constraints satisfied (from previous fix)
✅ All data synced successfully

## Date Fixed
January 11, 2026

