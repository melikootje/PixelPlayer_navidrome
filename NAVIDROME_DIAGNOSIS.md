# Navidrome Sync Diagnosis Guide

## Your Issue
You're clicking the "Sync Library from Navidrome" button but seeing "zero syncing" happen. The logs show:
```
W NavidromeSyncWorker: Navidrome/Subsonic is not enabled. Skipping sync.
```

But you said **Navidrome WAS turned ON in settings**. This is a data persistence issue.

## What I Fixed

### 1. Added Detailed Logging (NavidromeSyncWorker.kt)
The worker now logs ALL configuration settings at startup:
```kotlin
Log.i(TAG, "Configuration check:")
Log.i(TAG, "  - Subsonic enabled: $isEnabled")
Log.i(TAG, "  - Server URL: ${if (serverUrl.isBlank()) "[EMPTY]" else serverUrl}")
Log.i(TAG, "  - Username: ${if (username.isBlank()) "[EMPTY]" else username}")
```

### 2. Fixed Memory Leak (SettingsCategoryScreen.kt)
The sync button was creating uncancelled Flow collectors on every click.

## Next Steps - Diagnose Your Issue

After rebuilding the app with these changes, click the sync button and check logcat for:

```
I NavidromeSyncWorker: ========================================
I NavidromeSyncWorker: Starting Navidrome library sync...
I NavidromeSyncWorker: ========================================
I NavidromeSyncWorker: Configuration check:
I NavidromeSyncWorker:   - Subsonic enabled: ???
I NavidromeSyncWorker:   - Server URL: ???
I NavidromeSyncWorker:   - Username: ???
```

## Possible Causes & Solutions

### Scenario 1: Logs show "Subsonic enabled: false"
**Problem:** The toggle in UI shows ON but DataStore has false  
**Cause:** DataStore write didn't complete or got reverted  
**Solution:**
1. Toggle the switch OFF, wait 2 seconds
2. Toggle the switch ON, wait 2 seconds
3. Try sync again

### Scenario 2: Logs show "Server URL: [EMPTY]"
**Problem:** Server URL wasn't saved to DataStore  
**Cause:** Form didn't persist the value  
**Solution:**
1. Re-enter your server URL
2. Click away from the text field (to trigger onValueChange)
3. Wait 2 seconds before clicking sync

### Scenario 3: Logs show "Connection test FAILED"
**Problem:** Can't reach your Navidrome server  
**Possible causes:**
- Server URL is wrong (check port, protocol http vs https)
- Server is not running  
- Network firewall blocking connection
- Credentials are incorrect

### Scenario 4: Everything looks correct but still fails
**Problem:** There's a bug in the SubsonicRepository  
**Next step:** Check if the "Test Connection" button works

## Test Connection Button

Before trying to sync, use the "Test Connection" button:
1. It should show "Testing..." 
2. Then either "Connection successful!" or an error message
3. If test connection works but sync fails, there's a bug in the sync logic

## DataStore Persistence Check

To verify settings are actually saved:
1. Enable Navidrome and enter all details
2. Force close the app completely
3. Reopen the app
4. Go back to Navidrome settings
5. Check if switch is still ON and fields still have values

If values are gone after restart → DataStore not persisting (serious bug)

## Debugging Commands

Run app with verbose logcat:
```bash
adb logcat -s NavidromeSyncWorker:* WM-WorkerWrapper:* WM-SystemJobScheduler:*
```

This will show only the relevant logs for debugging the sync worker.

## Summary

The changes I made add comprehensive logging so you can see EXACTLY why the sync is failing. Once you rebuild and run, the logs will tell us whether:

- ✅ The toggle state isn't persisting to DataStore
- ✅ The connection test is failing
- ✅ The sync is actually running but returning no results
- ✅ There's a bug in how the settings are being read

**Please run the app after rebuilding and share the new logs starting with "========================================". That will tell us the exact issue.**

