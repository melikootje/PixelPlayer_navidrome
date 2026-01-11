#!/bin/bash

# PixelPlayer Build Script with Java 17
# This script builds the release APK with the correct Java version

echo "======================================"
echo "PixelPlayer Release Build Script"
echo "======================================"
echo ""

# Set Java 17
echo "Setting Java 17..."
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
java -version

echo ""
echo "Building release APK..."
echo ""

cd /Users/meliko/StudioProjects/PixelPlayer_navidrome

# Clean and build
./gradlew clean assembleRelease

if [ $? -eq 0 ]; then
    echo ""
    echo "======================================"
    echo "✅ BUILD SUCCESSFUL!"
    echo "======================================"
    echo ""
    echo "APK Location:"
    echo "  app/build/outputs/apk/release/app-release.apk"
    echo ""
    echo "To install:"
    echo "  ~/Library/Android/sdk/platform-tools/adb install -r app/build/outputs/apk/release/app-release.apk"
    echo ""
else
    echo ""
    echo "======================================"
    echo "❌ BUILD FAILED"
    echo "======================================"
    echo ""
    echo "Check the error messages above."
    echo ""
    exit 1
fi

