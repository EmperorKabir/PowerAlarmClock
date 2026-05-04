# Power Alarm Clocks — Project Instructions

- Apply the global rules in `~\CLAUDE.md` (max compression, bullet lists, Context7 for libs).
- Plus, project-specific rules below take precedence in this directory.

## Variable Allowance Mandate

- Every numeric / boolean / string / colour / interval / threshold MUST be exposed as a `SettingDescriptor` in `:core:settings`.
- Hardcoded literals outside `:core:settings` are forbidden (Detekt rule `NoHardcodedAlarmConstant` enforces).
- The settings UI is generated from `SettingsRegistry`. Adding a feature flag = registering a descriptor.

## Architecture Discipline

- Multi-module Gradle Clean+MVVM. Modules MUST NOT depend upward; integrations depend only on `:core:domain` ports.
- New external service → new `:integrations:<name>` module. Feature modules expose Compose entry points + DI bindings only.
- All UI uses Compose Material 3 Adaptive + `WindowSizeClass`; no fixed pixel layouts.

## Theming

- Default palette is `teal_black` (primary `#00C2B8`, surface `#000000`).
- Theme tokens are `ColorSetting` / `StringSetting` descriptors; runtime hot-reload via `ThemeRepository`.
- Typography uses Google Fonts via `androidx.compose.ui.text.googlefonts` with bundled fallbacks.

## Alarm Engine

- `AlarmManager.setAlarmClock` for primary fire, `setExact` for motion fallback, `setExactAndAllowWhileIdle` for emergency override.
- `BootReceiver` reschedules every enabled alarm on `BOOT_COMPLETED` / `LOCKED_BOOT_COMPLETED` / `MY_PACKAGE_REPLACED` / `TIMEZONE_CHANGED`.
- Ringer flow: `AlarmReceiver → RingerForegroundService → RingerActivity` with full-screen intent + `setShowWhenLocked(true)` + `setTurnScreenOn(true)`.

## Permissions

- Drive scope is **strictly `drive.file`**; selection via Google Picker for compliance.
- Runtime permissions requested at first feature use, not bundled at launch.
- Cloud-sync payloads encrypted client-side (AES-GCM, key wrapped by Android Keystore).

## Workflow

- Use Context7 (`mcp__context7__*`) for any external library / API question — even well-known ones.
- TDD per `docs/superpowers/plans/2026-05-04-power-alarm-clock.md`: failing test → minimal impl → green → commit.
- Frequent commits, conventional-commit prefixes (`feat`, `fix`, `chore`, `refactor`, `test`, `docs`, `ci`).
- `./gradlew detekt ktlintCheck testDebugUnitTest` before every push.

## Out-of-Scope (YAGNI v1)

- iOS / desktop / web ports.
- Server-side LLM gateway (the cognitive-captcha LLM remains opt-in pointing at user-supplied endpoint).
- Multi-tenant sharing.
