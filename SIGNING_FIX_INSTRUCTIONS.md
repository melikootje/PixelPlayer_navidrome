# How to Fix the Signing Error

## The Problem
You're getting this error:
```
KeytoolException: Failed to read key key0 from store "/Users/meliko/Untitled.jks": 
keystore password was incorrect
```

This happens because:
1. You previously configured a release keystore (`Untitled.jks`) in Android Studio
2. The keystore file doesn't exist OR you entered the wrong password
3. Gradle is still trying to use this keystore configuration

## Solution: Use Debug Signing for Release Builds

I've already updated your `build.gradle.kts` file, but you need to clear cached configurations.

### Step 1: Clean All Build Artifacts

Run these commands in Terminal:

```bash
cd /Users/meliko/StudioProjects/PixelPlayer_navidrome

# Remove all build artifacts and caches
rm -rf app/build
rm -rf app/.gradle
rm -rf .gradle
rm -rf build

# Clean using Gradle
./gradlew clean --no-configuration-cache
```

### Step 2: Remove Android Studio Signing Configuration

1. **Open Android Studio**
2. Go to: **File → Project Structure** (or press Cmd+;)
3. Select **Modules → app**
4. Click on **Signing** tab
5. **Delete** any signing configurations you see (especially for "release")
6. Click **OK** to save

### Step 3: Delete IDE Cache (Important!)

In Android Studio:
1. Go to **File → Invalidate Caches...**
2. Check: 
   - ✅ Clear file system cache and Local History
   - ✅ Clear VCS Log caches and indexes
3. Click **Invalidate and Restart**

### Step 4: Build the Release APK

After Android Studio restarts:

```bash
cd /Users/meliko/StudioProjects/PixelPlayer_navidrome

# Build the release APK
./gradlew assembleRelease --no-configuration-cache
```

The APK will be at: `app/build/outputs/apk/release/app-release.apk`

---

## Alternative: Create a New Keystore with the Correct Password

If you want to use a proper release keystore instead of debug signing:

### Create New Keystore

```bash
cd /Users/meliko/StudioProjects/PixelPlayer_navidrome

keytool -genkey -v -keystore pixelplayer-release.keystore \
  -alias pixelplayer-key \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

When prompted, create a password and **REMEMBER IT**.

### Update build.gradle.kts

Replace the `signingConfigs` block with:

```kotlin
signingConfigs {
    create("release") {
        storeFile = file("pixelplayer-release.keystore")
        storePassword = "YOUR_STORE_PASSWORD_HERE"
        keyAlias = "pixelplayer-key"
        keyPassword = "YOUR_KEY_PASSWORD_HERE"
    }
}

buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
        signingConfig = signingConfigs.getByName("release")
    }
}
```

**Important:** Add `pixelplayer-release.keystore` to `.gitignore`!

---

## What I Changed in Your Code

In `/app/build.gradle.kts`, I modified the `signingConfigs` and `buildTypes` sections to:

1. Set `signingConfig = null` first to clear any IDE-configured keystores
2. Then set it to use the default debug signing: `signingConfig = signingConfigs.getByName("debug")`

This ensures the problematic `Untitled.jks` keystore is not used.

---

## Quick Fix Summary

**Run these 3 commands:**

```bash
cd /Users/meliko/StudioProjects/PixelPlayer_navidrome
rm -rf .gradle app/.gradle app/build build
./gradlew clean assembleRelease --no-configuration-cache
```

**Plus:**
- In Android Studio: File → Invalidate Caches → Invalidate and Restart

This should completely resolve the signing error! 🎉

