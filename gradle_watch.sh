#!/bin/bash
# gradle_watch.sh -- Auto-rebuild and profile on file changes
# Usage: ./gradle_watch.sh [module]

MODULE="${1:-.}"
echo "👁️  Watching $MODULE for changes..."

while true; do
    find "$MODULE" -name "*.kt" -o -name "*.xml" -o -name "build.gradle.kts" | \
    entr bash -c "echo '🔨 Building...' && \
                  ./gradlew assemble 2>&1 | tee build.log | \
                  python3 profiler.py build.log"
done
