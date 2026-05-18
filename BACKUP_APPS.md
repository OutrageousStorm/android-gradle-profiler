# Backup & Restore Apps

Best practices for backing up and restoring Android apps and data.

## App backup tools

| Tool | Method | Root? | Data backup? |
|------|--------|-------|------------|
| **Backup Restore** (F-Droid) | APK + settings export | No | Limited |
| **Swift Backup** | APK + app data | Yes | Yes |
| **Titanium Backup** | Full backup with encryption | Yes | Yes |
| **Seedvault** | System backup (Pixel/GrapheneOS) | No | Yes |
| **adb backup** | Via ADB | No | Partial |

## Quick ADB backup
```bash
# Backup all apps to encrypted archive
adb backup -apk -shared -all -f backup.ab

# Restore
adb restore backup.ab
```

## Batch export APKs
```bash
adb shell pm list packages -3 | while read pkg; do
  pkg="${pkg#package:}"
  path=$(adb shell pm path "$pkg" | cut -d: -f2)
  adb pull "$path" "${pkg}.apk"
done
```

See: [android-backup-vault](https://github.com/OutrageousStorm/android-backup-vault)
