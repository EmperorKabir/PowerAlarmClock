# Power Alarm Clocks

- Android alarm clock for power users — 50 advanced behavioural features, full variable allowance, dynamic theming, responsive UI.
- Status: **planning + scaffold complete; implementation pending.**
- Stack: Kotlin 2.0 / JDK 21 / Compose Material 3 Adaptive / Hilt / Room / WorkManager / AlarmManager / Media3 / CameraX / ML Kit.
- Default palette: teal `#00C2B8` on black `#000000`; user-overridable hex codes + Google Fonts typography.

## Layout

| Path | Role |
|---|---|
| `app/` | Application module, Hilt graph root, `MainActivity`, ringer wiring. |
| `core/domain` | Pure-Kotlin entities + use-cases. |
| `core/data` | Room + DataStore + AlarmRepository. |
| `core/settings` | `SettingsRegistry` — single source of truth for all user variables. |
| `core/ui` | Theme engine + adaptive Compose components. |
| `core/scheduler` | AlarmManager wrappers, BootReceiver, NextFireCalculator, WorkManager feed refreshers. |
| `core/audio` | ExoPlayer ringer, fade ramps, source adapters, cast routing. |
| `core/permissions` | Runtime permission orchestrator. |
| `core/logging` | Forensic event log + weekly digest. |
| `feature/*` | Per-screen Compose features. |
| `integrations/*` | One module per external service (Spotify, Drive, Fitbit, weather, traffic, TfL, holidays, calendar, ICS, Tasker, smart-home, cast, NFC, REST API, emergency). |

## Key Documents

- `FEATURES.md` — exhaustive 50-feature registry, each with its variable surface.
- `docs/superpowers/specs/2026-05-04-power-alarm-clock-design.md` — design spec.
- `docs/superpowers/plans/2026-05-04-power-alarm-clock.md` — phase-by-phase TDD implementation plan.

## Build (when fully implemented)

```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
./gradlew testDebugUnitTest
./gradlew detekt ktlintCheck
```

## CI

- `ci.yml.template` is the GitHub Actions workflow.
- Move it to `.github/workflows/ci.yml` once you have a PAT with the `workflow` scope, or commit it from the GitHub web UI.

## Variable Allowance Mandate

- Every numeric / boolean / string parameter MUST be a `SettingDescriptor` in `core/settings`.
- A custom Detekt rule `NoHardcodedAlarmConstant` enforces this in non-test code.
- Adding a feature variable = registering a descriptor; the settings UI is generated from the registry.
