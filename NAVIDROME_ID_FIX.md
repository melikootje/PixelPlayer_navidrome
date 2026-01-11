# 🎯 NAVIDROME ID FIX - Albums Returning 0 Songs

## Problem Identified

The sync was **successfully fetching the album list**, but when trying to get individual album details, **all albums returned "Album not found"** errors, resulting in **0 songs being imported**.

### Root Cause

Navidrome uses **string IDs** like `"ae2c6e4d-1234-5678-9abc-def012345678"` for albums, but the app was:

1. Converting string IDs to Long: `id.toLongOrNull() ?: id.hashCode().toLong()`
2. Losing the original string ID
3. Trying to call `getAlbum("123456789")` instead of `getAlbum("ae2c6e4d-1234-5678-9abc-def012345678")`
4. Navidrome couldn't find albums with numeric IDs → **Album not found**

## ✅ Fix Applied

### 1. Added `subsonicId` field to Album model
**File:** `app/src/main/java/com/theveloper/pixelplay/data/model/LibraryModels.kt`
```kotlin
data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val year: Int,
    val albumArtUriString: String?,
    val songCount: Int,
    val subsonicId: String? = null // NEW: Original Subsonic/Navidrome string ID
)
```

### 2. Preserved original string ID in repository
**File:** `app/src/main/java/com/theveloper/pixelplay/data/repository/SubsonicRepository.kt`

Both `SubsonicAlbum.toAlbum()` and `AlbumResult.toAlbum()` now save the original ID:
```kotlin
return Album(
    id = id.toLongOrNull() ?: id.hashCode().toLong(),
    // ...other fields...
    subsonicId = id // ← Preserve the original Subsonic string ID
)
```

### 3. Used correct ID in sync worker
**File:** `app/src/main/java/com/theveloper/pixelplay/data/worker/NavidromeSyncWorker.kt`
```kotlin
// BEFORE: Used the converted Long id
val albumDetailResult = subsonicRepository.getAlbum(album.id.toString())

// AFTER: Use the original Subsonic string ID
val albumId = album.subsonicId ?: album.id.toString()
val albumDetailResult = subsonicRepository.getAlbum(albumId)
```

### 4. Updated database schema
**Files:**
- `app/src/main/java/com/theveloper/pixelplay/data/database/AlbumEntity.kt` - Added `subsonicId` column
- `app/src/main/java/com/theveloper/pixelplay/data/database/PixelPlayDatabase.kt` - Incremented version to 12

## 📦 How to Build & Test

### 1. Build the APK
```bash
cd /Users/meliko/StudioProjects/PixelPlayer_navidrome
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./gradlew clean assembleRelease
```

### 2. Install on device
```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

### 3. Test the sync
```bash
# Start watching logs
adb logcat -s NavidromeSyncWorker SubsonicRepository

# In the app:
# 1. Settings → Navidrome/Subsonic
# 2. Make sure server details are correct
# 3. Click "Sync Library from Navidrome"
```

## 🎉 Expected Results

### Before Fix (What you were seeing):
```
I NavidromeSyncWorker: Fetched 500 albums from Navidrome
E NavidromeSyncWorker: Failed to fetch album '"Ballast der Republik"': Album not found
E NavidromeSyncWorker: Failed to fetch album '"Heroes" (2017 Remaster)': Album not found
... (ALL albums failed) ...
I NavidromeSyncWorker: Fetched 0 songs and 500 albums from Navidrome
I NavidromeSyncWorker: Navidrome sync completed successfully in 25197ms. Synced 0 songs, 500 albums, 1420 artists
```

### After Fix (What you should see):
```
I NavidromeSyncWorker: Fetched 1420 artists from Navidrome
I NavidromeSyncWorker: Progress: 10/1420 artists processed, 143 songs, 23 albums collected
I NavidromeSyncWorker: Progress: 20/1420 artists processed, 312 songs, 47 albums collected
I NavidromeSyncWorker: Artist 'Metallica' → Album 'Master of Puppets' has 8 songs
I NavidromeSyncWorker: Artist 'Pink Floyd' → Album 'Dark Side of the Moon' has 10 songs
...
I NavidromeSyncWorker: Progress: 1420/1420 artists processed, 15432 songs, 2145 albums collected
I NavidromeSyncWorker: Navidrome sync completed successfully in 62000ms. Synced 15432 songs, 2145 albums, 1420 artists
```

## 🐛 Troubleshooting

### If albums still return 0 songs:

1. **Check the logs for the API call:**
   ```bash
   adb logcat -s SubsonicRepository:D *:S
   ```
   Look for lines like:
   ```
   D SubsonicRepository: getAlbum(ae2c6e4d-...): Album 'X' has Y songs in response
   ```

2. **Verify the album ID format:**
   Enable verbose logging to see what IDs are being used:
   ```kotlin
   Log.d(TAG, "Fetching album with subsonicId='${album.subsonicId}' (fallback id='${album.id}')")
   ```

3. **Check Navidrome API directly:**
   ```bash
   # Test if Navidrome returns songs for a specific album
   curl "http://100.69.51.245:4533/rest/getAlbum?u=meliko&t=TOKEN&s=SALT&id=ALBUM_ID&v=1.16.1&c=PixelPlayer&f=json"
   ```

### If build fails with database migration error:

The database version was incremented from 11 → 12. If you see schema errors:
```bash
# Clear the app data
adb shell pm clear com.theveloper.pixelplay

# Reinstall
adb install -r app/build/outputs/apk/release/app-release.apk
```

## 📝 Summary

The fix ensures that **Navidrome's original string IDs are preserved** throughout the app, so when we make API calls like `getAlbum()`, we use the **correct ID format** that Navidrome expects.

**Files Changed:**
1. ✅ `LibraryModels.kt` - Added `subsonicId: String?` field
2. ✅ `SubsonicRepository.kt` - Populate `subsonicId` when converting API responses
3. ✅ `NavidromeSyncWorker.kt` - Use `subsonicId` for API calls
4. ✅ `AlbumEntity.kt` - Database entity with `subsonicId` column
5. ✅ `PixelPlayDatabase.kt` - Bumped version to 12

This should now correctly sync **all your songs from Navidrome**! 🎵

