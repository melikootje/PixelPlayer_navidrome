

#!/bin/bash
# Fast build script with optimizations

cd /Users/meliko/StudioProjects/PixelPlayer_navidrome

echo "🚀 Starting optimized build..."
echo ""

# Set Java 17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
echo "✓ Using Java: $JAVA_HOME"

# Stop old Gradle daemon to apply new settings
echo "✓ Stopping old Gradle daemon..."
./gradlew --stop 2>/dev/null

echo "✓ Building release APK (with optimizations)..."
echo ""

# Time the build
time ./gradlew assembleRelease

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ BUILD SUCCESSFUL!"
    echo ""
    echo "APK location:"
    ls -lh app/build/outputs/apk/release/app-release.apk
    echo ""
    echo "To install: adb install -r app/build/outputs/apk/release/app-release.apk"
else
    echo ""
    echo "❌ BUILD FAILED"
    echo ""
    echo "Check the error messages above"
fi

