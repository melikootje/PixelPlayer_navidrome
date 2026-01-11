# 📍 Where to Configure Settings in PixelPlayer

## 🎯 Integration Status Summary

| Feature | Status | Configuration Location |
|---------|--------|------------------------|
| **Local Music** | ✅ Fully Working | Settings → Music Management |
| **Navidrome/Subsonic** | ✅ Fully Working | Settings → Server Settings |
| **Tidal HiFi API** | ⚠️ Partially Built | NOT CONFIGURABLE YET |

### ⚠️ Important: Tidal is NOT Ready to Use
The Tidal HiFi API has backend code (API models, service, repository) but:
- ❌ No settings UI exists
- ❌ No way to configure server URL/credentials
- ❌ No sync worker to fetch your library
- ❌ No playback support for streaming

**You can only configure Navidrome right now. Tidal requires completing Phases 1-4 in IMPLEMENTATION_SUMMARY.md**

---

## 🎯 Quick Answer (For What Actually Works)

All settings are configured **within the app** through the Settings screen. No manual file editing required!

---

## 📱 How to Access Settings

### Step 1: Open the App
Launch PixelPlayer on your Android device

### Step 2: Navigate to Settings
- Tap the **hamburger menu** (☰) or settings icon
- Select **"Settings"**

### Step 3: Select Category
You'll see several categories:
- 🎵 **Music Management** - Library folders and scanning
- 🎨 **Appearance** - Themes and visual styles  
- ▶️ **Playback** - Audio behavior
- ☁️ **Server Settings** - Navidrome/Subsonic configuration ⭐
- 🤖 **AI Integration** - Gemini API settings
- 🔧 **Developer Options** - Experimental features
- 🎚️ **Equalizer** - Audio adjustments
- ℹ️ **About** - App information

---

## ☁️ Navidrome/Subsonic Configuration

### Where: Settings → **Server Settings**

This is where you configure your Navidrome server connection.

### Required Fields:

#### 1. **Enable Navidrome/Subsonic** (Toggle)
- Turn this ON to enable server streaming
- When OFF, all other server options are disabled

#### 2. **Server URL** (Text Input)
- Enter your Navidrome server address
- Format: `http://your-server-ip:port` or `https://your-domain.com`
- Examples:
  - `http://192.168.1.100:4533`
  - `http://100.69.51.245:4533` (from your logs)
  - `https://music.mydomain.com`

#### 3. **Username** (Text Input)
- Your Navidrome login username
- Example: `meliko` (from your logs)

#### 4. **Password** (Password Input)
- Your Navidrome login password
- Hidden by default for security

#### 5. **Test Connection** (Button)
- After entering details, tap this to verify connection
- Shows success/failure message
- **Important:** Test before syncing!

#### 6. **Sync Library** (Button)
- Appears when connection is enabled
- Starts downloading your music library from Navidrome
- Shows progress notification (after implementing notification system)
- Can take several minutes for large libraries

---

## 🎵 Tidal Configuration Status

### ⚠️ **PARTIALLY IMPLEMENTED** ⚠️

The Tidal HiFi API integration has been **partially built** but is **NOT yet functional** in the UI:

#### ✅ What's Been Created:
1. **API Models** (`TidalModels.kt`) - Data classes for Tidal responses
2. **API Service** (`TidalApiService.kt`) - Retrofit interface for HiFi API endpoints
3. **Repository** (`TidalRepository.kt`) - Business logic for Tidal operations

#### ❌ What's Still Missing:
1. **Settings UI** - No configuration screen exists yet
2. **User Preferences** - No DataStore keys for Tidal settings
3. **Sync Worker** - No background worker to fetch Tidal library
4. **Playback Integration** - MusicService doesn't handle Tidal streams yet
5. **Database Support** - `musicSource` column not fully integrated

### 🚧 To Actually Use Tidal, You Need:

#### Phase 1: Add DataStore Keys
Edit `UserPreferencesRepository.kt` and add:
```kotlin
val TIDAL_SERVER_URL = stringPreferencesKey("tidal_server_url")
val TIDAL_USERNAME = stringPreferencesKey("tidal_username")
val TIDAL_PASSWORD = stringPreferencesKey("tidal_password")
val TIDAL_ENABLED = booleanPreferencesKey("tidal_enabled")
```

#### Phase 2: Add Settings UI
Edit `SettingsCategoryScreen.kt` in the `SERVER` category section and add:
- Tidal enable toggle
- HiFi API server URL input (default: `http://localhost:3000`)
- Username/password inputs
- Test connection button
- Sync library button

#### Phase 3: Create Tidal Sync Worker
Create `TidalSyncWorker.kt` similar to `NavidromeSyncWorker.kt` to:
- Fetch user's playlists
- Fetch favorite tracks
- Convert to database entities
- Insert with `musicSource = "TIDAL"`

#### Phase 4: Update MusicService
Modify playback logic to check `song.musicSource`:
- If `TIDAL`, fetch stream URL via `TidalRepository.getStreamUrl()`
- Play the returned URL with ExoPlayer

### 📝 Current Recommendation:

**DO NOT configure Tidal settings yet** - the functionality is not complete. Complete the phases above first, or stick with Navidrome which is fully working.

### Where Settings Would Be (After Implementation):
Settings → **Server Settings** → Tidal section with:
- **Enable Tidal** toggle
- **HiFi API Server URL** - Your HiFi API server URL
- **Tidal Username** - Your Tidal account username  
- **Tidal Password** - Your Tidal account password
- **Test Connection** - Verify HiFi API is reachable
- **Sync Tidal Library** - Download your playlists/favorites
- **Stream Quality** - Choose quality (LOW/HIGH/LOSSLESS/HI_RES)

---

## 💾 Where Settings Are Stored

### Internal Storage Location:
Settings are stored in **Android DataStore** at:
```
/data/data/com.theveloper.pixelplay/files/datastore/user_preferences.pb
```

### You Cannot Edit This Directly
- DataStore is a binary format (Protocol Buffers)
- Must be modified through the app UI
- Stored locally on device (not in repository)

### Key Names in Code:
If you're curious about the internal structure:
```kotlin
// Navidrome Settings Keys (in UserPreferencesRepository.kt)
SUBSONIC_ENABLED = "subsonic_enabled"
SUBSONIC_SERVER_URL = "subsonic_server_url"
SUBSONIC_USERNAME = "subsonic_username"
SUBSONIC_PASSWORD = "subsonic_password"
SUBSONIC_USE_LOCAL_CACHE = "subsonic_use_local_cache"
```

---

## 🔧 Code Files Involved

### UI Files (Where You See the Settings):
- **`SettingsCategoryScreen.kt`** (lines 456-543)
  - Contains the Server Settings UI
  - Text inputs for URL, username, password
  - Test connection and sync buttons

### ViewModel (Business Logic):
- **`SettingsViewModel.kt`**
  - Handles saving/loading settings
  - Methods: `setSubsonicEnabled()`, `setSubsonicServerUrl()`, etc.

### Repository (Data Storage):
- **`UserPreferencesRepository.kt`**
  - Manages DataStore persistence
  - Provides Flow objects for reactive updates

### Worker (Syncing):
- **`NavidromeSyncWorker.kt`**
  - Reads settings from repository
  - Connects to Navidrome server
  - Downloads library data

---

## 📝 Configuration Workflow

### First-Time Setup:

```
1. Install APK on device
   └─ adb install -r app/build/outputs/apk/release/app-release.apk

2. Clear app data (if reinstalling)
   └─ adb shell pm clear com.theveloper.pixelplay

3. Launch PixelPlayer

4. Go to Settings → Server Settings

5. Enable Navidrome toggle

6. Enter Server URL
   └─ Example: http://100.69.51.245:4533

7. Enter Username
   └─ Example: meliko

8. Enter Password
   └─ Your Navidrome password

9. Tap "Test Connection"
   └─ Wait for success message

10. Tap "Sync Library"
    └─ Wait for sync to complete (watch logs or notification)

11. Browse your Navidrome music in the app!
```

---

## 🚨 Important Notes

### ⚠️ Clear App Data After Database Changes
If you've modified the database schema or are experiencing issues:
```bash
adb shell pm clear com.theveloper.pixelplay
```
This removes all settings and cached data. You'll need to reconfigure.

### 🔒 Password Security
- Passwords are stored in DataStore (encrypted on device)
- Never committed to Git repository
- Each device stores its own credentials

### 🌐 Network Access
- Device must be able to reach your Navidrome server
- Check firewall settings if connection fails
- Use IP address if hostname doesn't resolve

### 📊 Sync Progress
Currently, sync progress is only visible in logs:
```bash
adb logcat -s NavidromeSyncWorker:*
```

After implementing Phase 1 (Sync Notifications), you'll see progress in Android notifications.

---

## 🐛 Troubleshooting

### "Connection Failed" Error
1. Verify server URL is correct and reachable
2. Check server is running: Open URL in browser
3. Verify credentials are correct
4. Check device has internet/network access
5. Ensure no firewall blocking connection

### "Foreign Key Constraint Failed" Error
This was the bug that was just fixed! If you still see it:
1. Clear app data: `adb shell pm clear com.theveloper.pixelplay`
2. Reinstall latest APK
3. Reconfigure settings

### Sync Taking Forever
- Normal for large libraries (1000+ artists = 1-2 minutes)
- Watch logs to confirm it's progressing
- If stuck at 0%, check connection and credentials

### Can't Find Settings
- Make sure app is fully installed
- Try restarting the app
- Check you're on the latest build

---

## 📚 Related Documentation

- **`IMPLEMENTATION_SUMMARY.md`** - Full implementation status and next steps
- **`agents.md`** - Detailed fix documentation and troubleshooting
- **`NAVIDROME_DIAGNOSIS.md`** - Original bug analysis

---

## 🎯 Summary

**Where do I configure Navidrome?**
→ **In the app:** Settings → Server Settings

**Where is the config file?**
→ **There isn't one!** Settings are in DataStore (binary format)

**Can I edit settings manually?**
→ **No.** Must use the app UI

**Where do I set the Tidal API URL?**
→ **Not yet implemented.** Will be in Settings → Server Settings after Phase 5

**How do I test my configuration?**
→ Use the **"Test Connection"** button in Server Settings

---

*Last Updated: January 11, 2025*  
*For the latest changes, see IMPLEMENTATION_SUMMARY.md*

