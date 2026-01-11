#!/bin/bash

# Comprehensive build, install, and debug script for Navidrome sync
echo "======================================"
echo "PixelPlayer - Navidrome Debug Build"
echo "======================================"
echo ""

cd /Users/meliko/StudioProjects/PixelPlayer_navidrome

# Set Java 17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
echo "Using Java: $JAVA_HOME"
echo ""

# Clean build
echo "Step 1/4: Cleaning previous build..."
./gradlew clean > /dev/null 2>&1

# Build release APK
echo "Step 2/4: Building APK..."
./gradlew assembleRelease

if [ $? -ne 0 ]; then
    echo ""
    echo "❌ BUILD FAILED"
    exit 1
fi

echo ""
echo "✅ Build successful!"
echo ""

# Check if device is connected
echo "Step 3/4: Checking for connected device..."
adb devices | grep -w "device" > /dev/null

if [ $? -ne 0 ]; then
    echo "❌ No Android device connected"
    echo "   Please connect your device and enable USB debugging"
    exit 1
fi

echo "✅ Device connected"
echo ""

# Install APK
echo "Step 4/4: Installing APK..."
adb install -r app/build/outputs/apk/release/app-release.apk

if [ $? -ne 0 ]; then
    echo ""
    echo "❌ Installation failed"
    exit 1
fi

echo ""
echo "======================================"
echo "✅ Installation Complete!"
echo "======================================"
echo ""
echo "Next Steps:"
echo ""
echo "1. Clear the log buffer:"
echo "   adb logcat -c"
echo ""
echo "2. Start watching logs with DEBUG level:"
echo "   adb logcat NavidromeSyncWorker:D SubsonicRepository:D *:S"
echo ""
echo "3. In the app:"
echo "   - Go to Settings → Navidrome/Subsonic"
echo "   - Make sure it's enabled"
echo "   - Click 'Sync Library from Navidrome'"
echo ""
echo "4. Save the logs:"
echo "   adb logcat -d > navidrome_sync_$(date +%Y%m%d_%H%M%S).log"
echo ""
echo "======================================"
echo "Quick Start (all-in-one):"
echo "======================================"
echo ""
echo "Run this in a new terminal:"
echo ""
echo "adb logcat -c && adb logcat NavidromeSyncWorker:D SubsonicRepository:D *:S"
echo ""
echo "Then trigger the sync in the app!"
echo ""

