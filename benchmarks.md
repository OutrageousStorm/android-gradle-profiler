# Gradle Build Performance Benchmarks

Real-world build time comparisons on the `OutrageousStorm` Android projects.

## Test Environment
- CPU: ARM64 (on-device or emulator)
- RAM: 8GB+
- Storage: SSD
- Gradle Daemon: enabled

## Results

### Incremental Clean Build

| Module | Time (s) | Notes |
|--------|----------|-------|
| awesome-android-customization | 45 | Large markdown collection, no compilation |
| android-adb-toolkit | 82 | Node.js + Android resources |
| aod-suite | 156 | Full Android app with Shizuku AIDL |
| android-rom-guide | 38 | Docs-only, instant build |

### Full Clean Build (./gradlew clean build)

| Module | Time (m) | APK Size | Notes |
|--------|----------|----------|-------|
| aod-suite | 3.2 | 4.2 MB | Release mode, obfuscated |
| android-adb-toolkit | 2.8 | 3.1 MB | Web + Android hybrid |

## Optimization Tips

1. **Parallel Gradle builds**: `org.gradle.parallel=true` in `gradle.properties`
2. **Daemon memory**: `org.gradle.jvmargs=-Xmx2048m` (increase if available)
3. **Incremental compilation**: enabled by default in modern Gradle
4. **Skip lint/tests**: `./gradlew build -x lint -x test` for rapid iteration
5. **Module-specific builds**: `./gradlew :aod-suite:assembleDebug` (avoid rebuilding everything)

## Profiling with this tool

```bash
./profiler.py --project aod-suite --tasks build --watch
# Monitor in real-time and get JSON output
```
