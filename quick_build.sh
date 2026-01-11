#!/bin/bash

# Quick build and install script for PixelPlayer Navidrome
# Fixes both Foreign Key and Foreground Service issues

echo "================================"
echo "PixelPlayer Build & Install"
echo "================================"
echo ""

# Set Java 17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
echo "✓ Java 17 configured"

# Clean
echo "→ Cleaning project..."
./gradlew clean > /dev/null 2>&1

# Build debug APK
echo "→ Building debug APK..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo "✓ Build successful!"
    echo ""

    # Install
    echo "→ Installing APK..."
    adb install -r app/build/outputs/apk/debug/app-debug.apk

    if [ $? -eq 0 ]; then
        echo "✓ Installation successful!"
        echo ""
        echo "================================"
        echo "Ready to test!"
        echo "================================"
        echo ""
        echo "To monitor sync:"
        echo "  adb logcat -s NavidromeSyncWorker SubsonicRepository"
        echo ""
        echo "To test:"
        echo "  1. Open app → Settings"
        echo "  2. Configure Navidrome"
        echo "  3. Tap 'Sync Library Now'"
        echo ""
    else
        echo "✗ Installation failed"
        exit 1
    fi
else
    echo "✗ Build failed"
    exit 1
fi

