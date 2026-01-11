# Navidrome Sync Issue - Solution

## Problem Identified

The app connects to Navidrome successfully but doesn't show any music in the UI because:

1. **No Sync Mechanism**: The app has a `SubsonicRepository` that can fetch data from Navidrome, but there's NO worker or mechanism that actually syncs this data to the local database.

2. **SyncWorker Only Handles Local Files**: The existing `SyncWorker` only syncs music from MediaStore (device storage), NOT from Navidrome/Subsonic servers.

3. **MusicRepository Doesn't Populate from Navidrome**: The `MusicRepositoryImpl` checks if Subsonic is enabled but never actually fetches and stores the Navidrome library data.

## Solution

I've created a `NavidromeSyncWorker` that:
- Fetches all artists, albums, and songs from Navidrome
- Converts them to local database entities
- Stores them in the local Room database

However, there are some CRITICAL issues that need to be addressed:

### Issues to Fix

1. **SongEntity Structure Mismatch**:
   - Navidrome songs have stream URLs, not local file paths
   - `SongEntity` expects `contentUriString` (local MediaStore URI) and `filePath`
   - Need to adapt the entity to support remote URLs

2. **No UI Trigger for Sync**:
   - The settings screen has a "Test Connection" button but NO "Sync Library" button
   - Users have no way to trigger the Navidrome sync

3. **Playback Issue**:
   - ExoPlayer expects local URIs or proper HTTP URLs
   - Need to modify the player to handle Navidrome stream URLs

## Quick Fix - Manual Approach

Since implementing the full solution requires significant changes, here's what you need to do NOW:

### Option 1: Add a Sync Button to Settings (Recommended)

Add a button in the Navidrome settings section that triggers the sync:

```kotlin
Button(
    onClick = {
        // Trigger NavidromeSyncWorker
        val workManager = WorkManager.getInstance(context)
        workManager.enqueueUniqueWork(
            "NavidromeSync",
            ExistingWorkPolicy.REPLACE,
            NavidromeSyncWorker.navidromeSyncWork()
        )
    }
) {
    Text("Sync Library from Navidrome")
}
```

### Option 2: Auto-Sync When Connection Succeeds

Modify the "Test Connection" logic to automatically trigger sync when successful.

## Files Created

1. ✅ `/app/src/main/java/com/theveloper/pixelplay/data/worker/NavidromeSyncWorker.kt`
   - Basic implementation
   - **Needs refinement** for URL handling

## Files That Need Modification

1. ❌ `SongEntity.kt` - Add support for remote URLs
2. ❌ `SettingsCategoryScreen.kt` - Add sync button
3. ❌ `SubsonicModels.kt` - Ensure proper URL conversion
4. ❌ Player implementation - Handle streaming URLs

## Current Status

- ✅ Connection to Navidrome works
- ✅ Can fetch artist/album/song data
- ❌ Data not being synced to local DB (no trigger)
- ❌ URLs need proper handling for playback

## Next Steps

Tell me which approach you prefer:
1. **Quick Fix**: Add a manual sync button (takes 5 minutes)
2. **Full Solution**: Implement auto-sync + proper URL handling (takes longer but better UX)

The sync button is the fastest way to see your Navidrome library in the app!

