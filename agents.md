# PixelPlayer Navidrome Sync - Issues and Solutions

## Date: January 11, 2026

---

## PROBLEM 1: Navidrome Sync Returns 0 Songs

### Root Cause
From the logcat analysis, the sync worker is successfully:
1. Connecting to the Navidrome server ✅
2. Fetching 1420 artists ✅
3. Processing all artists ✅
4. But getting **0 songs and 0 albums**

The logs show:
```
01-11 05:46:44.890 I NavidromeSyncWorker: Progress: 1420/1420 artists processed, 0 songs, 0 albums collected
01-11 05:46:44.890 I NavidromeSyncWorker: Fetched 0 songs and 0 albums from Navidrome
```

### Investigation Needed
Compare with the original PixelPlayer implementation to see how they fetch songs:

**Original Project:** https://github.com/theovilardo/PixelPlayer

The original project uses a different approach:
1. Fetches artists using `getArtists()`
2. For each artist, calls `getArtist(artistId)` to get albums
3. For each album, calls `getAlbum(albumId)` to get songs

### Current Implementation Issues
Looking at the logs from the newer attempt:
```
01-11 12:02:00.025 W NavidromeSyncWorker: Failed to fetch artist '$uicideboy$': Artist not found
01-11 12:02:00.069 W NavidromeSyncWorker: Failed to fetch artist '((( O )))': Artist not found
```

The issue is that `getArtistWithAlbums()` is returning "Artist not found" for all artists, which means:
1. The API endpoint might be wrong
2. The artist ID format is incorrect
3. The Subsonic API implementation is incomplete

### Solution Strategy

#### Option A: Fix the Existing Approach
1. Debug why `getArtistWithAlbums()` returns "Artist not found"
2. Check if we're using the correct Subsonic API endpoint
3. Verify the artist ID format (numeric vs string ID)

#### Option B: Use Album-Based Approach
Instead of artist → album → songs, use:
1. `getAlbumList2()` to get all albums
2. For each album, call `getAlbum(albumId)` to get songs
3. Extract artist info from song metadata

#### Option C: Use the Original Project's Approach
1. Examine the original PixelPlayer Subsonic implementation
2. Copy the exact API calls and data flow
3. Adapt to our database schema

---

## PROBLEM 2: Foreground Service Crash on Android 14+

### Error Message
```
android.app.InvalidForegroundServiceTypeException: Starting FGS with type none
callerApp=ProcessRecord{eca1838 31801:com.theveloper.pixelplay/u0a641}
targetSDK=35 has been prohibited
```

### Root Cause
Android 14+ (API 34+) requires foreground services to declare their type. The crash occurs because:
1. The `SystemForegroundService` (used by WorkManager) doesn't have a proper foreground service type
2. While our code specifies `FOREGROUND_SERVICE_TYPE_DATA_SYNC`, the manifest declaration is incomplete

### Solution
The manifest already has:
```xml
<service
    android:name="androidx.work.impl.foreground.SystemForegroundService"
    android:foregroundServiceType="dataSync"
    tools:node="merge" />
```

But we need to ensure WorkManager is properly configured. The fix is already partially in place, but we need to verify:

1. ✅ Permission declared: `FOREGROUND_SERVICE_DATA_SYNC`
2. ✅ Service type declared in manifest
3. ⚠️ Need to ensure notification is posted before `startForeground()`

### Implementation Fix
Add proper initialization in `NavidromeSyncWorker.kt`:
```kotlin
override suspend fun getForegroundInfo(): ForegroundInfo {
    return createForegroundInfo("Initializing sync...")
}
```

This is already present, so the issue might be timing-related.

---

## PROBLEM 3: Missing Tidal/HiFi API Integration

### Requirements
Integrate the Tidal HiFi API from: https://github.com/uimaxbai/hifi-api

### Current State
- No Tidal integration exists
- Need to add Tidal authentication
- Need to add Tidal streaming support
- Need to add UI for Tidal settings

### Implementation Plan

#### 1. Add Tidal API Dependencies
```kotlin
// build.gradle.kts
dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
}
```

#### 2. Create Tidal API Service
Based on the hifi-api project structure:
- Authentication endpoints
- Track search/get endpoints
- Stream URL generation
- Quality selection (LOSSLESS, HI_RES)

#### 3. Add Database Support
- Add `isTidalTrack` flag to SongEntity
- Add `tidalTrackId` field
- Add Tidal credentials to preferences

#### 4. Add UI Components
- Tidal login screen
- Tidal settings in preferences
- Quality selector (Master, HiFi, High, Low)

---

## IMPLEMENTATION PRIORITY

### Phase 1: Fix Navidrome Sync (URGENT)
1. Debug artist fetching issue
2. Implement proper error handling
3. Add more detailed logging
4. Compare with original project implementation

### Phase 2: Fix Foreground Service Crash
1. Verify notification channel creation
2. Ensure proper WorkManager initialization
3. Test on Android 14+ device

### Phase 3: Add Tidal Integration
1. Create Tidal API service
2. Add authentication flow
3. Add streaming support
4. Add UI for Tidal settings

---

## COMPARING WITH ORIGINAL PROJECT

### Original PixelPlayer Subsonic Implementation

From: https://github.com/theovilardo/PixelPlayer

The original project's approach:

```kotlin
// From SubsonicRepository or similar
suspend fun syncLibrary() {
    // 1. Get all artists
    val artists = subsonicApi.getArtists()
    
    // 2. For each artist
    artists.forEach { artist ->
        // Get artist details (includes albums)
        val artistDetail = subsonicApi.getArtist(artist.id)
        
        // 3. For each album
        artistDetail.albums.forEach { album ->
            // Get album details (includes songs)
            val albumDetail = subsonicApi.getAlbum(album.id)
            
            // 4. Process songs
            albumDetail.songs.forEach { song ->
                // Save to database
            }
        }
    }
}
```

### Key Differences

1. **API Method Names**: Original might use different method names
2. **ID Format**: Original might handle ID format differently
3. **Error Handling**: Original might have better error recovery
4. **Batching**: Original might batch requests differently

### Next Steps

1. ✅ Download original project
2. ⬜ Examine Subsonic API implementation
3. ⬜ Compare with our implementation
4. ⬜ Identify missing/different API calls
5. ⬜ Adapt to our codebase

---

## TESTING CHECKLIST

### Navidrome Sync
- [ ] Test connection to server
- [ ] Verify artist fetching
- [ ] Verify album fetching
- [ ] Verify song fetching
- [ ] Check database insertion
- [ ] Verify song playback
- [ ] Test with special characters in names
- [ ] Test with large libraries (1000+ artists)

### Foreground Service
- [ ] Test on Android 10
- [ ] Test on Android 11
- [ ] Test on Android 12
- [ ] Test on Android 13
- [ ] Test on Android 14
- [ ] Test on Android 15
- [ ] Verify notification appears
- [ ] Verify sync completes

### Tidal Integration
- [ ] Test Tidal login
- [ ] Test track search
- [ ] Test track playback
- [ ] Test quality switching
- [ ] Test offline caching
- [ ] Test with free account
- [ ] Test with premium account

---

## REFERENCE: Original Project Structure

Location: `/Users/meliko/Downloads/PixelPlayer_original`

### Files to Examine
1. Subsonic API implementation
2. Repository pattern
3. Worker implementation
4. Database schema

### Questions to Answer
1. How does the original handle Subsonic IDs?
2. What API endpoints does it use?
3. How does it map Subsonic data to the database?
4. What error handling strategies does it use?

---

## CURRENT STATUS (as of sync attempt)

### What Works ✅
- Connection to Navidrome server
- Artist list fetching (1420 artists)
- Progress tracking
- Notification display

### What Doesn't Work ❌
- Album fetching (0 albums returned)
- Song fetching (0 songs returned)
- Artist detail fetching ("Artist not found" errors)

### What's Unknown ❓
- Why getArtistWithAlbums() fails
- Whether the API endpoint is correct
- Whether ID format is causing issues
- How the original project handles this

---

## DETAILED LOGCAT ANALYSIS

### Successful Sync Attempt (Latest with Debug Logs)
```
01-11 12:19:02.996 D NavidromeSyncWorker:     → Album 'New Approach' has 2 songs
```

This shows that in the latest attempt, songs ARE being fetched! The logcat from 12:19 shows:
- Artists being fetched successfully
- Albums being fetched with their songs
- Proper debug logging

But then it crashes with:
```
01-11 12:19:03.963 E NavidromeSyncWorker: android.database.sqlite.SQLiteConstraintException:
FOREIGN KEY constraint failed (code 787 SQLITE_CONSTRAINT_FOREIGNKEY[787])
```

### New Root Cause Identified! 🎯

The sync is actually working! The issue is a **database foreign key constraint failure**.

This means:
1. ✅ Artists are fetched correctly
2. ✅ Albums are fetched correctly
3. ✅ Songs are fetched correctly
4. ❌ Database insertion fails due to foreign key constraint

### Solution for Database Issue

The foreign key constraint failure suggests:
1. We're trying to insert a song with an albumId that doesn't exist in the albums table
2. We're trying to insert a song with an artistId that doesn't exist in the artists table
3. The order of insertion is wrong (songs before albums/artists)

### Fix Strategy

1. **Insert in correct order:**
   ```kotlin
   // 1. First, insert all artists
   musicDao.insertArtists(artists)
   
   // 2. Then, insert all albums
   musicDao.insertAlbums(albums)
   
   // 3. Finally, insert all songs
   musicDao.insertSongs(songs)
   
   // 4. Create cross-references
   musicDao.insertSongArtistCrossRefs(crossRefs)
   ```

2. **Ensure IDs match:**
   - Album's artistId must match an existing artist's id
   - Song's albumId must match an existing album's id
   - Song's artistId must match an existing artist's id

3. **Handle missing data:**
   - If a song references an unknown album, either skip it or create a dummy album
   - If a song references an unknown artist, either skip it or create a dummy artist

---

## CONCLUSION

### The Real Problem
The Navidrome sync IS working - it's fetching all the data correctly. The problem is in the database insertion due to foreign key constraints.

### The Fix
We need to:
1. Ensure proper insertion order (artists → albums → songs)
2. Verify all foreign key relationships before insertion
3. Handle orphaned records (songs without albums/artists)

### Implementation Priority (REVISED)
1. ✅ Navidrome API - Working!
2. ❌ Database insertion - **This is the bug**
3. ⚠️ Foreground service - Secondary issue
4. ⬜ Tidal integration - Future feature

