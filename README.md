# Qwirkle Score Tracker

A native Android score companion for Qwirkle. It keeps score, shows whose turn it is, and automatically restores an unfinished game when the app is reopened.

## Features

- 2–6 named players
- Current-player highlighting and automatic turn rotation
- Numeric score entry plus quick score buttons
- Dedicated **QWIRKLE +6** shortcut
- Pass / zero-score turns
- Undo the most recent turn
- Recent turn history
- End-game winner/tie summary and final scores
- Automatic local save/resume using Android SharedPreferences
- No account, network connection, ads, or analytics

## Build

The project targets Android API 35 and uses Java 17 with Android Gradle Plugin 8.7.3.

```bash
gradle testDebugUnitTest
gradle assembleDebug
```

The debug APK is produced at:

`app/build/outputs/apk/debug/app-debug.apk`

GitHub Actions runs unit tests and builds the APK for pull requests to `main`.

## Notes

This is an unofficial companion score tracker. Qwirkle® is a trademark of its respective owner; this project is not affiliated with or endorsed by the trademark owner.
