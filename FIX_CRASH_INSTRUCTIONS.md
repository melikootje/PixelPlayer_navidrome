cd /Users/meliko/StudioProjects/PixelPlayer_navidrome

# Set Java 17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# Clean and rebuild
./gradlew clean assembleRelease

# Install the new APK
~/Library/Android/sdk/platform-tools/adb install -r app/build/outputs/apk/release/app-release.apk# Fix for App Crash on Release Build

## Problem Identified

Your app crashes immediately on launch with this error:
```
java.lang.ClassCastException
at com.theveloper.pixelplay.di.AppModule.provideSubsonicApiService
```

**Root Cause:** ProGuard/R8 was incorrectly obfuscating your Retrofit API service interfaces and dependency injection qualifiers, causing a ClassCastException when Dagger/Hilt tried to inject the `SubsonicApiService`.

## Solution Applied

I've updated `/app/proguard-rules.pro` with the following critical fixes:

### 1. Keep Retrofit Service Interfaces
```proguard
# Keep all Retrofit service interfaces
-keep interface com.theveloper.pixelplay.data.remote.** { *; }
-keep class com.theveloper.pixelplay.data.remote.** { *; }

# Keep Subsonic/Navidrome API classes
-keep interface com.theveloper.pixelplay.data.network.** { *; }
-keep class com.theveloper.pixelplay.data.network.** { *; }
```

### 2. Keep Custom DI Qualifiers
```proguard
# Keep all custom qualifiers
-keep @interface javax.inject.Qualifier
-keep @javax.inject.Qualifier @interface *
-keep @interface com.theveloper.pixelplay.di.DeezerRetrofit
-keep @interface com.theveloper.pixelplay.di.FastOkHttpClient
-keep @interface com.theveloper.pixelplay.di.SubsonicRetrofit
-keepattributes *Annotation*
```

### 3. Keep DI Modules
```proguard
# Keep all DI modules and their provider methods
-keep @dagger.Module class * { *; }
-keep class com.theveloper.pixelplay.di.** { *; }
-keepclassmembers class com.theveloper.pixelplay.di.** {
    @dagger.Provides *;
    @javax.inject.Inject *;
}
```

### 4. Enhanced Retrofit Rules
```proguard
# Keep Retrofit and OkHttp classes used in DI
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keepclassmembers,allowobfuscation class * {
  @retrofit2.http.* <methods>;
}

# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items)
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
```

## Steps to Rebuild and Test

### 1. Clean Build
```bash
cd /Users/meliko/StudioProjects/PixelPlayer_navidrome
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./gradlew clean
```

### 2. Build Release APK
```bash
./gradlew assembleRelease
```

### 3. Install on Device
```bash
~/Library/Android/sdk/platform-tools/adb install -r app/build/outputs/apk/release/app-release.apk
```

### 4. Check Logs (if still crashes)
```bash
# Clear logs
~/Library/Android/sdk/platform-tools/adb logcat -c

# Launch the app, then monitor logs
~/Library/Android/sdk/platform-tools/adb logcat -v time | grep -A 50 "FATAL EXCEPTION"
```

## What Changed in proguard-rules.pro

The ProGuard configuration now:
- ✅ Keeps all Retrofit service interfaces (prevents type erasure)
- ✅ Keeps all custom `@Qualifier` annotations (preserves DI qualifiers)
- ✅ Keeps all `@Module` and `@Provides` annotated classes/methods
- ✅ Preserves Kotlin coroutine Continuation classes (for suspend functions)
- ✅ Keeps Subsonic API network package classes

## Alternative: Test with Debug Build First

If you want to verify the app logic works before dealing with ProGuard:

```bash
./gradlew assembleDebug
~/Library/Android/sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Debug builds don't use ProGuard/R8, so they won't have this issue.

## Expected Result

After rebuilding with the updated ProGuard rules, the release APK should:
- ✅ Launch without crashing
- ✅ Properly inject `SubsonicApiService` via Dagger/Hilt
- ✅ No more `ClassCastException` errors

## If It Still Crashes

If you still get a crash after rebuilding, run the logcat command and share the new error. The ProGuard rules might need further refinement based on the specific error.

---

**Summary:** The ProGuard rules have been fixed to prevent R8 from stripping critical type information from your Retrofit services and DI qualifiers. Rebuild the app and test!

