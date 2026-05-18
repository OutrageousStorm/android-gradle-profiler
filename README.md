# 📊 Android Gradle Profiler

Analyze Gradle build times in Android projects. Find slow tasks, parallel build opportunities, and optimization tips.

## Usage

```bash
./gradlew build --profile
java -jar profiler.jar build/reports/profile/

# Or scan an existing profile
java -jar profiler.jar path/to/profile-TIMESTAMP.html
```

## Outputs

- **Critical path:** Which tasks are blocking overall build time
- **Task breakdown:** Time per task, sorted by duration
- **Parallel opportunities:** Which tasks can run concurrently
- **Dependency graph:** Task dependency visualization
- **Recommendations:** Specific optimizations for your build

## Common slowdowns

1. **Compilation** — Use KSP over Kapt, enable parallel compilation
2. **Resource processing** — Disable unused densities
3. **APK packaging** — Use minSdkVersion ≥ 21 (better compression)
4. **Variant selection** — Build fewer variants in debug
5. **Plugin count** — Remove unused plugins
