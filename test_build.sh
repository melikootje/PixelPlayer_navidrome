#!/bin/bash

# Simple build test script
cd "$(dirname "$0")"

echo "Cleaning project..."
./gradlew clean > /dev/null 2>&1

echo "Building debug APK..."
./gradlew assembleDebug 2>&1 | tee last_build.log

if [ $? -eq 0 ]; then
    echo "✅ BUILD SUCCESSFUL"
    ls -lh app/build/outputs/apk/debug/*.apk 2>/dev/null || echo "APK not found in expected location"
else
    echo "❌ BUILD FAILED"
    echo ""
    echo "Searching for Kotlin errors..."
    grep -E "e: |error:" last_build.log | head -20
fi

