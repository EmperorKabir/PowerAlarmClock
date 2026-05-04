# Power Alarm Clocks Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a production-grade Android alarm clock with 50 advanced features, full variable allowance, dynamic theming, and responsive UI.

**Architecture:** Multi-module Gradle (Kotlin DSL) Clean+MVVM, Compose Material 3 adaptive UI, Hilt DI, Room+DataStore persistence, AlarmManager-driven scheduler with WorkManager-backed feed refreshers, port/adapter integrations for every external service.

**Tech Stack:** Kotlin 2.0 / JDK 21 / Gradle 8.10 / Compose BOM 2026.04 / Material 3 Adaptive / Hilt 2.52 / Room 2.7 / Media3 1.4 / CameraX 1.4 / ML Kit / Retrofit 2.11 / OkHttp 5 / Ktor server (LAN API) / Coroutines 1.9 / WorkManager 2.10 / kotlinx-serialization-json / Roborazzi / Kotest / Turbine / MockK.

---

## Phase 0 — Repository & Tooling

### Task 0.1: Repo bootstrap

**Files:**
- Create: `.gitignore`, `.editorconfig`, `gradle/libs.versions.toml`, `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`
- Create: `README.md`, `CLAUDE.md`, `LICENSE`

- [ ] Generate Android `.gitignore` (Android Studio defaults + `/build`, `.idea/`, `local.properties`, `*.keystore`, `app/release/`).
- [ ] Write `gradle/libs.versions.toml` with version catalog covering every dep listed in spec §2.
- [ ] Configure `settings.gradle.kts`: `pluginManagement { repositories { gradlePluginPortal(); google(); mavenCentral() } }`, `dependencyResolutionManagement { RepositoriesMode.FAIL_ON_PROJECT_REPOS }` and include every module from spec §3.
- [ ] Write root `build.gradle.kts` with shared `tasks.register("clean")` + Detekt + Ktlint plugins.
- [ ] Write `gradle.properties`: `kotlin.code.style=official`, `org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=1g`, `android.useAndroidX=true`, `kotlin.incremental=true`, `org.gradle.parallel=true`, `org.gradle.caching=true`.
- [ ] Commit: `chore: bootstrap repo, version catalog, gradle settings`.

### Task 0.2: CI scaffolding

**Files:**
- Create: `.github/workflows/ci.yml`, `.github/workflows/lint.yml`, `config/detekt/detekt.yml`, `config/detekt/baseline.xml`

- [ ] CI workflow runs `./gradlew assembleDebug testDebugUnitTest detekt ktlintCheck lint`.
- [ ] Cache Gradle + Konan via `actions/cache`.
- [ ] Detekt rule `NoHardcodedAlarmConstant` enforces variable-allowance mandate (custom rule referencing forbidden literals outside `:core:settings`).
- [ ] Commit: `chore(ci): add lint+test workflows`.

## Phase 1 — Core Settings (variable allowance)

### Task 1.1: SettingDescriptor model

**Files:**
- Create: `core/settings/src/main/kotlin/com/poweralarm/core/settings/SettingDescriptor.kt`
- Test: `core/settings/src/test/kotlin/com/poweralarm/core/settings/SettingDescriptorTest.kt`

- [ ] Write failing tests: serialization round-trip; type validation; default fallback; dependency resolution.
- [ ] Implement `sealed class SettingDescriptor<T>` with subclasses Bool/Int/Long/Float/String/Color/Uri/Enum/Json + `validator: (T) -> ValidationResult`.
- [ ] Run tests → green.
- [ ] Commit: `feat(core-settings): SettingDescriptor model`.

### Task 1.2: SettingsRegistry + DataStore backing

**Files:**
- Create: `core/settings/src/main/kotlin/com/poweralarm/core/settings/SettingsRegistry.kt`
- Create: `core/settings/src/main/kotlin/com/poweralarm/core/settings/SettingsStore.kt`
- Test: `core/settings/src/test/kotlin/com/poweralarm/core/settings/SettingsStoreTest.kt`

- [ ] Tests: read default when absent; write and re-read; Flow updates on write; per-alarm override precedence.
- [ ] Implement `SettingsRegistry.all(): List<SettingDescriptor<*>>` populated from a registry-builder DSL.
- [ ] `SettingsStore` wraps `DataStore<Preferences>`.
- [ ] Commit: `feat(core-settings): registry + DataStore-backed store`.

### Task 1.3: Seed registry with all 200+ keys

**Files:**
- Modify: `core/settings/src/main/kotlin/com/poweralarm/core/settings/RegistrySeed.kt`

- [ ] Add a registry entry for **every** variable enumerated in `FEATURES.md` (50 features × 1–6 vars each) plus theming, ringer layout, responsive UI sections.
- [ ] Test asserts `assertThat(registry.all().map { it.id }).containsAll(expectedIds)` with the expected list checked into a fixture file `registry-keys.txt`.
- [ ] Commit: `feat(core-settings): seed full registry per FEATURES.md`.

### Task 1.4: Detekt custom rule — no hardcoded thresholds

**Files:**
- Create: `config/detekt/rules/NoHardcodedAlarmConstant.kt`

- [ ] Rule flags numeric literals > 1 in non-test, non-`:core:settings` source roots unless annotated `@RegistryConstant`.
- [ ] Commit: `feat(detekt): enforce variable-allowance mandate`.

## Phase 2 — Theming Engine

### Task 2.1: Color & Typography tokens

**Files:**
- Create: `core/ui/src/main/kotlin/com/poweralarm/core/ui/theme/ThemeState.kt`
- Create: `core/ui/src/main/kotlin/com/poweralarm/core/ui/theme/PowerAlarmTheme.kt`
- Test: screenshot test in `core/ui/src/test`.

- [ ] Tests render a sample screen at default `teal_black` and at user-supplied `#FF00AA` primary; snapshot diff.
- [ ] Implement `PowerAlarmTheme` Composable that reads `ThemeRepository.state` and derives `MaterialTheme` slot values.
- [ ] Commit: `feat(core-ui): theme engine + teal_black default`.

### Task 2.2: Hex palette validation + persistence

**Files:**
- Create: `core/ui/src/main/kotlin/com/poweralarm/core/ui/theme/ThemeRepository.kt`
- Test: round-trip + invalid hex rejection.

- [ ] Tests for invalid hex (`gibberish`, `#FFF`, empty).
- [ ] Implement validation regex + `setHex(roleId, hex)` writing through `SettingsStore`.
- [ ] Commit: `feat(core-ui): hex palette validation + persistence`.

### Task 2.3: Typography picker

**Files:**
- Create: `core/ui/src/main/kotlin/com/poweralarm/core/ui/theme/TypographyEngine.kt`

- [ ] Wire `androidx.compose.ui.text.googlefonts` provider with offline fallback bundled fonts (Inter, JetBrains Mono).
- [ ] Setting `typographyFamily` accepts a Google Fonts family name.
- [ ] Commit: `feat(core-ui): typography engine`.

## Phase 3 — Persistence (Room) & Domain

### Task 3.1: Domain entities

**Files:** `core/domain/src/main/kotlin/com/poweralarm/core/domain/model/*.kt`

- [ ] Tests: serializer round-trip; equality semantics.
- [ ] Implement: `Alarm`, `Recurrence` (sealed), `Condition` (sealed), `DismissalRequirement` (sealed), `AudioPlan`, `SnoozePolicy`, `RingerLayoutPolicy`, `AutomationHooks`.
- [ ] Commit: `feat(core-domain): entities`.

### Task 3.2: Room schema + DAOs

**Files:** `core/data/src/main/kotlin/com/poweralarm/core/data/db/*.kt`

- [ ] Define `AlarmEntity` with embedded JSON for sealed-type fields (kotlinx-serialization).
- [ ] DAO methods: list, observe, byId, upsert, delete, all-enabled.
- [ ] Migration tests use Room MigrationTestHelper.
- [ ] Commit: `feat(core-data): Room schema + DAOs`.

### Task 3.3: AlarmRepository

**Files:** `core/data/src/main/kotlin/com/poweralarm/core/data/AlarmRepository.kt`

- [ ] Tests with in-memory Room DB; asserts mapping fidelity entity ↔ domain.
- [ ] Implement `AlarmRepository.observeAll(): Flow<List<Alarm>>`, `save(Alarm)`, `delete(id)`.
- [ ] Commit: `feat(core-data): AlarmRepository`.

## Phase 4 — Scheduler

### Task 4.1: NextFireCalculator

**Files:** `core/scheduler/src/main/kotlin/com/poweralarm/core/scheduler/NextFireCalculator.kt`
Test: `…/test/NextFireCalculatorTest.kt`

- [ ] Property tests cover combinatorial interactions of conditions: holiday × geofence × weather × calendar shift × bedtime penalty × low battery × solar × adhan × chained.
- [ ] Implement reduction pipeline: skip filters → shift accumulators → solar/adhan adjuster → chain offset projector.
- [ ] Commit: `feat(scheduler): next-fire calculator`.

### Task 4.2: AlarmManager bridge

**Files:** `core/scheduler/.../AlarmScheduler.kt`, `AlarmReceiver.kt`, `BootReceiver.kt`

- [ ] Tests with Robolectric AlarmManager shadow.
- [ ] `setAlarmClock` for primary, `setExact` for motion-fallback, `setExactAndAllowWhileIdle` for emergency override.
- [ ] BootReceiver iterates `AlarmRepository.all().filter { enabled }` and reschedules.
- [ ] Commit: `feat(scheduler): AlarmManager bridge`.

### Task 4.3: WorkManager feed refresh workers

**Files:** `core/scheduler/.../workers/{Weather,Traffic,Aqi,Holidays,Ics,Sync,Tfl}Worker.kt`

- [ ] Each worker keyed by its settings interval; constraints: `NetworkType.CONNECTED` (except `IcsWorker`).
- [ ] Tests with WorkManager testing utilities.
- [ ] Commit: `feat(scheduler): periodic feed refresh workers`.

## Phase 5 — Audio (core/audio)

### Task 5.1: AudioSourcePort + ExoPlayer ringer

**Files:** `core/audio/.../AudioSourcePort.kt`, `RingerEngine.kt`

- [ ] Adapters: LocalAdapter (SAF Uri), DriveAdapter (drive.file), SpotifyAdapter (App Remote), UrlAdapter.
- [ ] Tests: fade-in curve points; fade-out 10s ramp; fallback on stream error within timeout.
- [ ] Commit: `feat(core-audio): ringer engine + adapters`.

### Task 5.2: Cast adapters

**Files:** `integrations/cast/.../{GoogleCast,AirPlay,Alexa}Adapter.kt`

- [ ] Tests: timeout fallback; one-shot LAN multicast lock acquire/release.
- [ ] Wire `MediaRouter.selectRoute()` before `RingerEngine.start()`.
- [ ] Commit: `feat(integrations-cast): Cast/AirPlay/Alexa adapters`.

## Phase 6 — Ringer UI (full-screen + dismissal)

### Task 6.1: RingerForegroundService + RingerActivity

**Files:** `feature/ringer/.../RingerForegroundService.kt`, `RingerActivity.kt`, `RingerScreen.kt`

- [ ] Service `foregroundServiceType="mediaPlayback|specialUse"`.
- [ ] Activity sets `setShowWhenLocked(true)`, `setTurnScreenOn(true)`, `WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON`.
- [ ] Compose screen renders `DismissalRequirement` sequence.
- [ ] Commit: `feat(ringer): full-screen ringer plumbing`.

### Task 6.2: Cognitive-load randomized layout

**Files:** `feature/ringer/.../CognitiveLoadLayout.kt`

- [ ] Tests verify (a) random bounds within constraints, (b) regen on every instantiation, (c) 48dp accessibility floor.
- [ ] Implement `BoxWithConstraints` parent producing `(xFrac,yFrac,scale)` tuples per button, regenerated via `remember(alarmInstanceId)`.
- [ ] Commit: `feat(ringer): cognitive-load randomizer`.

### Task 6.3: DismissalRequirement renderers

**Files:** one Composable per requirement subtype.

- [ ] TapButton, Cognitive (problem renderer + solver), Voice (record + on-device match), Nfc (NfcAdapter + foregroundDispatch), Qr (CameraX + ML Kit BarcodeScanning), Steps (SensorManager.TYPE_STEP_DETECTOR), EyesOpenSelfie (CameraX + ML Kit FaceDetection eye-open prob), Distress (alt code matching → silent SOS WorkManager job).
- [ ] Commit per renderer.

### Task 6.4: Hardware button override + lockout

**Files:** `feature/ringer/.../ButtonOverrideController.kt`, manifest activity flags.

- [ ] Override volume / power dispatch via `dispatchKeyEvent`.
- [ ] Lockout = pinned activity + DND priority + back/home press swallow.
- [ ] Commit: `feat(ringer): hardware button override + lockout`.

## Phase 7 — Snooze Dynamics

### Task 7.1: Snooze policy engine

**Files:** `core/domain/.../SnoozeEngine.kt`

- [ ] Tests for decreasing duration math, floor clamp, max-count enforcement, gesture-direction → duration mapping.
- [ ] Commit: `feat(snooze): policy engine`.

### Task 7.2: Gesture surface

**Files:** `feature/ringer/.../SnoozeGestureLayer.kt`

- [ ] Compose `pointerInput` detects swipe direction + device orientation (`SensorManager.TYPE_ORIENTATION`).
- [ ] Maps to `SnoozeEngine.applyGesture()`.
- [ ] Commit: `feat(snooze): gesture surface`.

## Phase 8 — Conditions (skip / advance)

One task per Condition subtype below — each follows: failing test → minimal impl → green → commit.

- [ ] **HolidaySkip** via `:integrations:holidays` (Nager.Date provider, default region `GB`).
- [ ] **DateRangeDisable**: pure date-range filter use-case.
- [ ] **TempSuspension**: counter decrement on each fire.
- [ ] **CalendarShift**: CalendarContract query within `eventLookaheadMin`.
- [ ] **WeatherAdvance**: weather provider (OpenWeather impl + iface; threshold mm / °C).
- [ ] **TrafficAdvance**: Maps Directions traffic-aware ETA delta.
- [ ] **AqiAdvance**: OpenAQ provider; PM2.5 threshold.
- [ ] **TflDisruption**: TfL Unified API line/stop status.
- [ ] **IcsSkip**: ICS parser (ical4j-android); regex match on SUMMARY.
- [ ] **Geofence** disable: `GeofencingClient` + home zone.
- [ ] **BedtimePenalty**: UsageStatsManager screen-on detection.
- [ ] **LowBatteryFailsafe**: BatteryManager observer; fire early threshold.
- [ ] **Polyphasic**: template generator → produces children Alarm rows.
- [ ] **ShiftPattern**: cycle JSON evaluator.
- [ ] **SolarAnchored**: SunriseSunsetCalculator (commons-suncalc).
- [ ] **Adhan**: prayer-times-kotlin lib using calculation method enum.
- [ ] **Chained**: chain offsets produce sibling alarms scheduled together.

## Phase 9 — Audio Sources Integrations

- [ ] **:integrations:spotify** — App Remote auth (PKCE), URI selection (Track/Playlist/Podcast), playback control. Free-tier degradation banner.
- [ ] **:integrations:drive** — REST v3 with `drive.file` scope; Google Picker flow for selection; cached file metadata in Room.
- [ ] Local SAF picker — persistent URI permissions; rotation pool seed.

## Phase 10 — Smart-Home & Automation

- [ ] **:integrations:smarthome** — Hue (CLIP v2), LIFX LAN, Matter via CHIP tool. Pre-alarm ramp scheduler in `SchedulerOrchestrator`.
- [ ] Smart plug subset: HS-100/Tapo + Matter sockets.
- [ ] **:integrations:tasker** — broadcast `net.dinglisch.android.tasker.ACTION_TASK` intents; configurable variables.
- [ ] **DND profile flip** — `NotificationManager.setInterruptionFilter`.
- [ ] **Network toggle** — `WifiManager.setWifiEnabled` (where API-permitted) + connectivity monitoring; cellular toggle via shell-permission helper if rooted, else surface guidance.

## Phase 11 — Wearable & Sensors

- [ ] **Fitbit integration** — Web API OAuth + intraday sleep stages; light-sleep window selection.
- [ ] **Step dismissal** — `Sensor.TYPE_STEP_DETECTOR` accumulator.
- [ ] **Motion fallback / pre-motion auto-dismiss** — accelerometer windowed integrator.
- [ ] **Wear OS companion** (`:feature:wear`) — Wear Data Layer messages: `set_alarm`, `snooze`, `dismiss`.

## Phase 12 — Identity, Geofence, Emergency

- [ ] **Geofence** — GMS `GeofencingClient` registered on app start.
- [ ] **Solar / Adhan** lat/lng resolution via `LocationManager` last-known-location, cached; setting overrides allowed.
- [ ] **:integrations:emergency** — USGS earthquake feed (`earthquake.usgs.gov/fdsnws/event/1/query`) + national CAP IPAWS where available; magnitude/radius gates.

## Phase 13 — Sync & Profiles

- [ ] **Cloud sync worker** — encrypts alarm DB snapshot with AES-GCM, writes to selected provider (Drive/Dropbox/WebDAV); schedule via WorkManager periodic interval setting.
- [ ] **Profile switching** (`:feature:profiles`) — persona enabling subsets in bulk.

## Phase 14 — Local LAN REST API

- [ ] **:integrations:rest-api** — Ktor server bound on `apiBindInterface`; HTTPS via in-app generated cert; bearer token from settings.
- [ ] Endpoints: `GET /alarms`, `POST /alarms`, `PUT /alarms/{id}`, `DELETE /alarms/{id}`, `POST /alarms/{id}/snooze`, `POST /alarms/{id}/dismiss`.
- [ ] Foreground service hosts the server when enabled.

## Phase 15 — Statistics & Forensics

- [ ] **:feature:statistics** — Room table `dismissal_event(alarmId, fireAt, dismissedAt, requirementsCompleted, snoozeCount, motionMs2, location, weather, traffic)`.
- [ ] Compose dashboard with charts; weekly digest WorkManager that pushes a notification summary.

## Phase 16 — Settings UI (registry-driven)

- [ ] **:feature:settings** — `SettingsScreen` reads `SettingsRegistry`, groups by `groupPath`, renders editors generically.
- [ ] Per-alarm override sheet uses same renderer.
- [ ] Search across descriptors by id/label/help.

## Phase 17 — Theme Editor

- [ ] **:feature:themes** — palette grid + hex input + Google Fonts family picker; live preview surface.
- [ ] Save/load palette presets.

## Phase 18 — Alarm List & Edit

- [ ] **:feature:alarm-list** — adaptive list-detail layout; FAB add; swipe actions.
- [ ] **:feature:alarm-edit** — form rendered from registry overlay; condition/dismissal builders.

## Phase 19 — End-to-End Verification

- [ ] Roborazzi suite at compact / medium / expanded × portrait / landscape × dark / light.
- [ ] UI test: full-screen ringer launches over keyguard.
- [ ] Property test: 10k random `Alarm` × `Condition` combos compile & schedule deterministically.
- [ ] CI green.
- [ ] Release tag `v0.1.0`.

## Self-Review Checklist (run after writing tasks)

- [ ] Every feature in `FEATURES.md` cross-referenced to a Phase 8/9/10/etc. task.
- [ ] No "TBD" / "later" tokens.
- [ ] Type names consistent across phases.
- [ ] All tasks include explicit files + commit message.

## Execution Handoff

Plan complete and saved to `docs/superpowers/plans/2026-05-04-power-alarm-clock.md`. Two execution options:

1. **Subagent-Driven (recommended)** — fresh subagent per task, review between tasks.
2. **Inline Execution** — execute tasks in this session via `executing-plans`.

User: choose execution mode in next message.
