# Navidrome Sync Issue - Debugging Guide

## Current Problem

The Navidrome sync is connecting successfully and fetching 1420 artists, but **0 songs** and **0 albums** are being collected.

### What's Working ✅
- Connection to Navidrome server
- Ping/authentication
- Fetching artist list (1420 artists)
- Processing all artists (no crashes)

### What's NOT Working ❌
- Getting albums for each artist
- Getting songs from albums

## Root Cause Analysis

Looking at the logs, there are two possible issues:

### Issue 1: Artists have no albums in Navidrome
If the Subsonic API is returning artists with `albumCount=0` or empty album lists, then this is correct behavior - there simply aren't albums associated with those artists in Navidrome.

### Issue 2: Album API calls are failing silently
The earlier logs showed errors like:
```
E NavidromeSyncWorker: Failed to fetch album '"Ballast der Republik"': Album not found
```

This suggests the album IDs from the artist API response don't match what the album API expects.

## Debugging Steps

### Step 1: Check what Navidrome is actually returning

Test your Navidrome API directly:

```bash
# Replace with your server details
SERVER="http://100.69.51.245:4533"
USERNAME="meliko"
PASSWORD="your_password"

# Generate salt and token (you'll need to do this properly with MD5)
# For testing, you can get this from the app logs or use Subsonic API tester

# Test getting an artist
curl "$SERVER/rest/getArtist?u=$USERNAME&t=$TOKEN&s=$SALT&v=1.16.1&c=PixelPlayer&id=<ARTIST_ID>"

# Test getting artists
curl "$SERVER/rest/getArtists?u=$USERNAME&t=$TOKEN&s=$SALT&v=1.16.1&c=PixelPlayer"
```

### Step 2: Check Navidrome logs

On your Navidrome server, check the logs to see if the API requests are even reaching it:
```bash
# If running in Docker:
docker logs navidrome

# If running as systemd service:
journalctl -u navidrome -f

# Look for lines showing API requests like:
# [INFO] GET /rest/getArtist
# [INFO] GET /rest/getAlbum
```

### Step 3: Enable verbose logging in the app

I've already added detailed logging. Now you need to filter for DEBUG level logs:

```bash
# Watch all NavidromeSyncWorker logs including DEBUG
adb logcat NavidromeSyncWorker:D *:S

# Or watch both repositories
adb logcat NavidromeSyncWorker:D SubsonicRepository:D *:S
```

You should see logs like:
```
D NavidromeSyncWorker: Artist 'AC/DC' (id=123) returned 15 albums
D NavidromeSyncWorker: Artist 'Metallica' (id=456) returned 0 albums - skipping
D SubsonicRepository: getArtistWithAlbums(123): Artist 'AC/DC' has 15 albums in API response
D SubsonicRepository:   Albums: 'Back In Black' (id=abc-123), 'Highway to Hell' (id=abc-456), ...
```

### Step 4: Check if the issue is with specific album IDs

The Subsonic API uses string IDs (like "al-123" or "abc-def-ghi"), not numeric IDs. The app should be preserving these in the `subsonicId` field.

Check the logs for album fetch attempts:
```bash
adb logcat | grep "Fetching album"
```

You should see:
```
D NavidromeSyncWorker:   Fetching album 'Back In Black' with subsonicId='al-123' (numeric id=123456789)
D SubsonicRepository: getAlbum(al-123): Album 'Back In Black' has 10 songs
```

If you see errors like "Album not found", the IDs don't match.

## Possible Solutions

### Solution 1: Use getMusicDirectory instead of getAlbum

Navidrome/Subsonic has multiple ways to get song lists:
- `getAlbum(id)` - Get album by ID
- `getMusicDirectory(id)` - Get directory/album contents (sometimes more reliable)
- `getAlbumList2()` - Get albums by various criteria

We might need to switch the approach.

### Solution 2: Use artist ID instead of album ID

Some Subsonic implementations use the artist ID to list all songs for that artist, bypassing albums entirely.

### Solution 3: Check Navidrome configuration

Your Navidrome server might need:
- Library scan/re-index
- Correct music folder permissions
- Proper metadata in your music files

## Next Steps

1. **Run the sync with DEBUG logging enabled**
   ```bash
   # Clear logcat first
   adb logcat -c
   
   # Start logging
   adb logcat NavidromeSyncWorker:D SubsonicRepository:D *:S > sync_debug.log
   
   # Trigger sync in the app
   # Let it run for a bit
   # Stop logging (Ctrl+C)
   ```

2. **Look for these patterns in sync_debug.log:**
   - How many artists return albums > 0?
   - Are album IDs being logged?
   - Are there any "failed to fetch" errors?

3. **Check Navidrome web UI:**
   - Login to Navidrome web interface
   - Browse to an artist
   - Check if albums show up
   - Note the URL/ID format used

4. **Test specific API endpoints:**
   Use a tool like Postman or curl to test:
   - GET /rest/getArtists - Should return your artists
   - GET /rest/getArtist?id=<ARTIST_ID> - Should return artist with albums
   - GET /rest/getAlbum?id=<ALBUM_ID> - Should return album with songs

## Share Debug Info

If you need help, please provide:
1. Full logcat output from a sync attempt with DEBUG logging
2. Navidrome version: Check Settings → About in Navidrome web UI
3. Sample output from testing the API directly (see Step 1)
4. Whether albums show up in Navidrome's web interface

## Quick Test

To quickly test if the issue is with your Navidrome setup:

1. **In Navidrome web UI:** Can you see artists, albums, and play songs?
2. **Using Subsonic API tester:** There are online Subsonic API testing tools
3. **Try a different Subsonic client:** Try DSub or Ultrasonic Android app to see if they can fetch music

If other clients work but PixelPlayer doesn't, the issue is in the app code. If no clients work, the issue is with your Navidrome setup.

