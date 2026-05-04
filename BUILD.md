# Build Instructions

## Prereqs
- JDK 21 (Temurin or Oracle).
- Android Studio Hedgehog 2024.x or newer (or Gradle 8.10.2+ standalone).
- Android SDK 35 (`compileSdk = 35`, `targetSdk = 35`, `minSdk = 26`).

## First-time setup
- Clone: `git clone https://github.com/EmperorKabir/PowerAlarmClock.git`
- Open the project in Android Studio. The IDE will download the Gradle wrapper jar on first sync (`gradle/wrapper/gradle-wrapper.jar`).
- Or run: `gradle wrapper --gradle-version=8.10.2` from the project root to generate the wrapper jar.

## Common tasks
- `./gradlew :app:assembleDebug` — debug APK.
- `./gradlew :app:installDebug` — install on a connected device.
- `./gradlew testDebugUnitTest` — unit tests.
- `./gradlew detekt ktlintCheck` — static analysis.

## API keys
Per CLAUDE.md no secrets in repo. Provide via `local.properties`:

```
OPENWEATHER_API_KEY=...
GOOGLE_DIRECTIONS_KEY=...
SPOTIFY_CLIENT_ID=...
FITBIT_CLIENT_ID=...
HUE_APP_KEY=...
```

The build script reads these and exposes them as `BuildConfig` constants only in the relevant integration modules.

## CI
- `ci.yml.template` at repo root holds the GitHub Actions workflow.
- Move it to `.github/workflows/ci.yml` from the GitHub web UI (or with a PAT that has `workflow` scope).
