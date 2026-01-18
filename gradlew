#!/bin/bash
#
# Gradle wrapper script for A.D.A. Android App
# Downloads and runs Gradle if not already installed
#

# Determine the script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
APP_NAME="Gradle"
GRADLE_VERSION="8.4"
GRADLE_DIST_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"

# Check if Gradle wrapper jar exists
WRAPPER_JAR="$SCRIPT_DIR/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$WRAPPER_JAR" ]; then
    echo "Gradle wrapper not found. Please run:"
    echo "  gradle wrapper --gradle-version ${GRADLE_VERSION}"
    echo ""
    echo "Or download from: ${GRADLE_DIST_URL}"
    echo ""
    echo "Alternative: Use Android Studio to build the project."
    exit 1
fi

# Run Gradle
exec java -Xmx2048m -Dfile.encoding=UTF-8 \
    -classpath "$WRAPPER_JAR" \
    org.gradle.wrapper.GradleWrapperMain "$@"
