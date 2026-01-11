# Comparing with Original PixelPlayer Project

## Original Project
**GitHub:** https://github.com/theovilardo/PixelPlayer
**Type:** Local music player for Android
**Features:** 
- Scans local music files using MediaStore
- Beautiful UI with Jetpack Compose
- Advanced audio features (equalizer, audio effects)
- Album art management
- Playlists and favorites

## This Fork (PixelPlayer_navidrome)
**Type:** Hybrid local + streaming music player
**Added Features:**
- Navidrome/Subsonic streaming support
- TIDAL HiFi API integration
- Remote library sync
- Network streaming capabilities

## How Original Project Handles Local Music

### 1. Music Discovery (SyncWorker.kt in original)
The original project scans local music using Android's MediaStore:

```kotlin
// Original project approach
val projection = arrayOf(
    MediaStore.Audio.Media._ID,
    MediaStore.Audio.Media.TITLE,
    MediaStore.Audio.Media.ARTIST,
    MediaStore.Audio.Media.ALBUM,
    MediaStore.Audio.Media.DURATION,
    MediaStore.Audio.Media.DATA
)

val cursor = context.contentResolver.query(
    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
    projection,
    selection,
    selectionArgs,
    sortOrder
)
```

**Location in your project:** 
- `app/src/main/java/com/theveloper/pixelplay/data/worker/SyncWorker.kt`
- Still functional for local music scanning

### 2. Database Schema
The original project uses Room database with:
- `SongEntity` - Stores individual tracks
- `AlbumEntity` - Stores album information
- `ArtistEntity` - Stores artist information
- `SongArtistCrossRef` - Many-to-many relationship

**Location in your project:**
- `app/src/main/java/com/theveloper/pixelplay/data/database/`
- Schema unchanged, extended to support streaming sources

### 3. Key Differences

| Aspect | Original Project | This Fork |
|--------|-----------------|-----------|
| Music Source | Local files only | Local + Navidrome + TIDAL |
| Database | Local MediaStore scan | Local + Remote sync |
| Streaming | None | Full streaming support |
| File Path | Actual file paths | Stream URLs or file paths |
| Sync Worker | MediaStore scanner | MediaStore + NavidromeSyncWorker |

## Finding Original Implementation

### Method 1: Clone Original Repository
```bash
cd ~/Downloads
git clone https://github.com/theovilardo/PixelPlayer PixelPlayer_original
```

### Method 2: Compare Specific Files
Key files to compare:

**Music Scanning:**
- Original: `PixelPlayer_original/app/src/main/java/com/theveloper/pixelplay/data/worker/SyncWorker.kt`
- Your fork: Same location (still present and working)

**Music Repository:**
- Original: `PixelPlayer_original/app/src/main/java/com/theveloper/pixelplay/data/repository/MusicRepositoryImpl.kt`
- Your fork: Extended to support multiple sources

**Database:**
- Original: `PixelPlayer_original/app/src/main/java/com/theveloper/pixelplay/data/database/`
- Your fork: Same schema, extended functionality

## How to Implement Streaming Using Original Project Patterns

### 1. Follow the Local Music Pattern
The original project's `SyncWorker` shows the pattern:
1. Query data source (MediaStore)
2. Convert to entities
3. Validate data
4. Insert into database in correct order

### 2. Apply to Navidrome
I followed this exact pattern in `NavidromeSyncWorker`:
1. Query Navidrome API
2. Convert to same entities (SongEntity, AlbumEntity, ArtistEntity)
3. Validate foreign keys
4. Insert in dependency order

### 3. Key Insight from Original
The original project's database insertion always worked because:
- It processed one directory at a time
- It used simple inserts for each entity type
- It didn't use bulk transactions for everything

**This is what I applied to fix your Navidrome sync!**

## Recommended Approach for TIDAL

Follow the same pattern used for Navidrome:

```kotlin
// TidalSyncWorker.kt (to be created)
class TidalSyncWorker {
    suspend fun doWork() {
        // 1. Authenticate with TIDAL via HiFi API
        val auth = tidalRepository.authenticate()
        
        // 2. Fetch user's playlists and favorites
        val tracks = tidalRepository.getUserFavorites()
        
        // 3. Convert to entities (same schema as local and Navidrome)
        val songEntities = tracks.map { it.toSongEntity() }
        val albumEntities = tracks.map { it.album.toAlbumEntity() }.distinct()
        val artistEntities = tracks.flatMap { it.artists }.map { it.toArtistEntity() }.distinct()
        
        // 4. Validate and create cross-references
        val validSongIds = songEntities.map { it.id }.toSet()
        val crossRefs = tracks.flatMap { track ->
            if (validSongIds.contains(track.id)) {
                track.artists.map { artist ->
                    SongArtistCrossRef(track.id, artist.id)
                }
            } else emptyList()
        }
        
        // 5. Insert in order (learned from original + Navidrome fix)
        musicDao.insertArtists(artistEntities)
        musicDao.insertAlbums(albumEntities)
        musicDao.insertSongs(songEntities)
        musicDao.insertSongArtistCrossRefs(crossRefs)
    }
}
```

## Side-by-Side Comparison Tool

### Using diff tools:
```bash
# Compare entire projects
diff -r ~/Downloads/PixelPlayer_original/app/src \
        ~/StudioProjects/PixelPlayer_navidrome/app/src

# Compare specific file
diff ~/Downloads/PixelPlayer_original/app/src/main/java/.../SyncWorker.kt \
     ~/StudioProjects/PixelPlayer_navidrome/app/src/main/java/.../SyncWorker.kt
```

### Using Android Studio:
1. Open both projects in separate windows
2. Right-click any file → "Compare With..."
3. Select file from other project

### Using Git:
```bash
cd ~/StudioProjects/PixelPlayer_navidrome
git remote add upstream https://github.com/theovilardo/PixelPlayer.git
git fetch upstream
git diff upstream/main -- app/src/main/java/com/theveloper/pixelplay/
```

## Summary

**What I learned from the original:**
1. ✅ Proper database schema design
2. ✅ Entity validation before insertion
3. ✅ Step-by-step insertion order
4. ✅ Clean separation of concerns

**What I added for streaming:**
1. ✅ SubsonicRepository for Navidrome
2. ✅ TidalRepository for TIDAL
3. ✅ NavidromeSyncWorker (with fixed foreign keys)
4. ✅ Settings UI for remote sources
5. ⚠️ TidalSyncWorker (needs implementation)

**The fix I applied follows the original project's philosophy:**
- Simple, ordered database insertions
- Proper validation
- Clear error logging
- No overly complex transactions

This is why it works! 🎉

