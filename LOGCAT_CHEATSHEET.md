# ADB Logcat Quick Reference for PixelPlayer

## TL;DR - Most Useful Commands

### 🚀 Quick Start - Monitor Your App
```bash
~/Library/Android/sdk/platform-tools/adb logcat -c && \
~/Library/Android/sdk/platform-tools/adb logcat --pid=$(~/Library/Android/sdk/platform-tools/adb shell pidof -s com.theveloper.pixelplay) -v color
```
**Run this, then launch the app**

### ⚠️ Check for Crashes
```bash
~/Library/Android/sdk/platform-tools/adb logcat *:E | grep -A 20 "FATAL"
```

### 🔍 Find Navidrome Issues
```bash
~/Library/Android/sdk/platform-tools/adb logcat | grep -i "navidrome\|subsonic\|sync"
```

---

## Setup Once (Optional but Recommended)

Add to `~/.zshrc`:
```bash
# ADB shortcuts
export PATH="$PATH:$HOME/Library/Android/sdk/platform-tools"
alias logapp='adb logcat --pid=$(adb shell pidof -s com.theveloper.pixelplay) -v color'
alias logerr='adb logcat *:E'
alias logclear='adb logcat -c'
```

Then run: `source ~/.zshrc`

Now you can use:
- `logapp` instead of the long command
- `logerr` to see only errors
- `logclear` to clear logs

---

## Common Scenarios

### Scenario 1: App Crashes on Launch
```bash
# Step 1: Clear logs
~/Library/Android/sdk/platform-tools/adb logcat -c

# Step 2: Monitor for crashes
~/Library/Android/sdk/platform-tools/adb logcat *:E | grep "FATAL"

# Step 3: Launch the app

# You'll see the crash with full stack trace
```

### Scenario 2: Navidrome Sync Not Working
```bash
# Monitor sync worker
~/Library/Android/sdk/platform-tools/adb logcat | grep "NavidromeSyncWorker\|SubsonicRepository"

# Then tap "Sync Library" in the app
```

### Scenario 3: Connection Test Fails
```bash
# See network errors
~/Library/Android/sdk/platform-tools/adb logcat | grep -i "connection\|network\|subsonic"

# Then tap "Test Connection"
```

### Scenario 4: Save Everything for Later
```bash
~/Library/Android/sdk/platform-tools/adb logcat > ~/Desktop/app_logs_$(date +%Y%m%d_%H%M%S).txt

# Reproduce the issue, then Ctrl+C
# Check the file on your Desktop
```

---

## Log Priority Levels

| Level | Symbol | Meaning | Example |
|-------|--------|---------|---------|
| Verbose | V | Everything | `logcat *:V` |
| Debug | D | Debug info | `logcat *:D` |
| Info | I | Normal info | `logcat *:I` |
| Warning | W | Warnings | `logcat *:W` |
| Error | E | Errors | `logcat *:E` |
| Fatal | F | Crashes | `logcat *:F` |

---

## What to Look For

### ✅ Good Signs
```
I/NavidromeSyncWorker: Starting Navidrome library sync...
I/NavidromeSyncWorker: Fetched 150 albums from Navidrome
I/NavidromeSyncWorker: Navidrome sync completed successfully
```

### ❌ Bad Signs
```
E/AndroidRuntime: FATAL EXCEPTION: main
E/SubsonicRepository: Connection test failed: java.net.UnknownHostException
E/NavidromeSyncWorker: Failed to fetch artists: Server returned: 401
W/System.err: java.lang.ClassCastException
```

---

## Pro Tips

1. **Always clear logs first:** `adb logcat -c`
2. **Use grep for filtering:** `adb logcat | grep "search_term"`
3. **Save logs when reporting bugs:** Better than screenshots
4. **Monitor in real-time:** Start logcat BEFORE triggering the issue
5. **Use timestamps:** `adb logcat -v time` helps understand timing

---

## Troubleshooting Logcat Itself

### "adb: command not found"
```bash
# Use full path
~/Library/Android/sdk/platform-tools/adb

# OR add to PATH permanently in ~/.zshrc:
export PATH="$PATH:$HOME/Library/Android/sdk/platform-tools"
source ~/.zshrc
```

### "error: no devices/emulators found"
```bash
# Check connection
~/Library/Android/sdk/platform-tools/adb devices

# If empty, reconnect your device or restart adb:
~/Library/Android/sdk/platform-tools/adb kill-server
~/Library/Android/sdk/platform-tools/adb start-server
```

### Too Much Output
```bash
# Filter to just errors
adb logcat *:E

# OR filter to just your app
adb logcat --pid=$(adb shell pidof -s com.theveloper.pixelplay)
```

---

## Quick Copy-Paste Commands

```bash
# Monitor app only (most useful)
~/Library/Android/sdk/platform-tools/adb logcat -c && ~/Library/Android/sdk/platform-tools/adb logcat --pid=$(~/Library/Android/sdk/platform-tools/adb shell pidof -s com.theveloper.pixelplay) -v color

# Errors only
~/Library/Android/sdk/platform-tools/adb logcat *:E

# Crashes only  
~/Library/Android/sdk/platform-tools/adb logcat *:E | grep "FATAL"

# Navidrome logs
~/Library/Android/sdk/platform-tools/adb logcat | grep -i "navidrome"

# Save to file
~/Library/Android/sdk/platform-tools/adb logcat > ~/Desktop/logs.txt

# Clear logs
~/Library/Android/sdk/platform-tools/adb logcat -c
```

---

**Remember:** Start logcat BEFORE reproducing the issue for best results!

