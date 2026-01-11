#!/bin/bash

# Build and install script with enhanced logging
echo "======================================"
echo "Building PixelPlayer with enhanced logging..."
echo "======================================"

cd /Users/meliko/StudioProjects/PixelPlayer_navidrome

# Set Java 17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

echo "Using Java: $JAVA_HOME"
echo ""

# Build release APK
echo "Building release APK..."
./gradlew assembleRelease

if [ $? -eq 0 ]; then
    echo ""
    echo "======================================"
    echo "✅ BUILD SUCCESSFUL!"
    echo "======================================"
    echo ""
    echo "APK location: app/build/outputs/apk/release/app-release.apk"
    echo ""
    echo "To install:"
    echo "  adb install -r app/build/outputs/apk/release/app-release.apk"
    echo ""
    echo "To watch logs:"
    echo "  adb logcat -s NavidromeSyncWorker:* SubsonicRepository:*"
    echo ""
else
    echo ""
    echo "======================================"
    echo "❌ BUILD FAILED"
    echo "======================================"
    exit 1
fi

