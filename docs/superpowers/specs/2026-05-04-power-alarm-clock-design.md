# Power Alarm Clocks — Design Spec

- Date: 2026-05-04
- Author: Kabir Bhasin (specced via Claude Code + Superpowers v5.0.7)
- Status: Approved-by-spec (user pre-authorised proceed)
- Companion docs: `FEATURES.md` (50-feature registry), `docs/superpowers/plans/2026-05-04-power-alarm-clock.md` (implementation plan)

## 1. Goal

- Production-grade Android alarm clock targeting power users.
- Universal variable allowance: every threshold, interval, behaviour, UI metric exposed via `core-settings` registry — no hardcoded constants.
- 50 advanced behavioural features (30 mandated + 20 power-user additions; see `FEATURES.md`).
- Dynamic theming engine: default teal-on-black palette, fully overridable hex + typography.
- Responsive: phones / foldables / tablets / Chromebooks / Wear OS companion.

## 2. Platform & Tech Stack

- Language: Kotlin 2.0+ (K2 compiler).
- JDK: 21 (`org.gradle.java.installations.auto-detect=true`).
- minSdk: 26 (Android 8.0); targetSdk: 35 (Android 15); compileSdk: 35.
- UI: Jetpack Compose (BOM 2026.04.x) + Material 3 + `material3-adaptive` for window-class adaptation.
- DI: Hilt 2.52+.
- Persistence: Room 2.7 (KSP) with optional SQLCipher 4.6 for the encrypted-sync use case.
- Preferences: `androidx.datastore:datastore-preferences` (settings registry backing store).
- Async: Kotlin Coroutines 1.9, Flow, `WorkManager 2.10` for periodic feeds, `androidx.work:work-multiprocess`.
- Alarm scheduling: `AlarmManager.setAlarmClock()` + `USE_FULL_SCREEN_INTENT` + foreground service (`foregroundServiceType="mediaPlayback|specialUse"`).
- Networking: Retrofit 2.11 + OkHttp 5 + kotlinx-serialization-json.
- Camera/ML: CameraX 1.4 + ML Kit Face Detection.
- Audio: ExoPlayer (media3 1.4) for streaming + fade ramps; AudioManager for legacy bus.
- Casting: `androidx.mediarouter` + Cast SDK; AirPlay via `airplay-android-sender` shim; Alexa via Amazon AVS REST.
- Build: Gradle 8.10 with version catalog `gradle/libs.versions.toml`.
- Static analysis: Ktlint, Detekt, Android Lint baseline.
- Testing: JUnit 5, Turbine, MockK, Roborazzi (screenshot), Espresso, Compose UI test.

## 3. Module Topology

```
:app                              (entry point, Hilt graph root, MainActivity, RingerActivity)
:core:domain                      (entities, use-cases, pure Kotlin)
:core:data                        (Room DB, repos, DataStore, sync)
:core:settings                    (SettingsRegistry, SettingDescriptor, dynamic UI binder)
:core:ui                          (theme engine, Compose components, adaptive scaffolds)
:core:scheduler                   (AlarmManager wrappers, exact-alarm permission flow)
:core:audio                       (ExoPlayer ringer, fade curves, route forcing)
:core:permissions                 (runtime permission orchestrator)
:core:logging                     (structured logs, weekly digest)
:feature:alarm-list
:feature:alarm-edit
:feature:ringer                   (full-screen ringer, dismissal UI, cognitive load)
:feature:settings                 (registry-driven settings UI)
:feature:themes                   (palette + typography editor)
:feature:statistics               (wake-up forensics, dismissal heatmap)
:feature:profiles                 (Work/Holiday/Travel personas)
:feature:wear                     (Wear OS data-layer pairing)
:integrations:spotify
:integrations:drive               (drive.file scope only)
:integrations:fitbit
:integrations:weather             (OpenWeather provider iface + alternates)
:integrations:airquality          (OpenAQ / WAQI)
:integrations:traffic             (Google Maps Directions)
:integrations:tfl                 (London Transport API)
:integrations:calendar            (CalendarContract + ICS parser)
:integrations:holidays            (Nager.Date or government feeds)
:integrations:tasker              (intent broadcaster)
:integrations:cast                (Cast/AirPlay/Alexa adapter)
:integrations:emergency           (USGS + CAP alerts)
:integrations:smarthome           (Hue/LIFX/Matter)
:integrations:nfc
:integrations:rest-api            (local LAN REST server, Ktor)
```

- Inter-module rule: integrations depend only on `:core:domain` ports (interfaces) — no upward deps.
- Feature modules expose `@Composable` entry points + DI bindings only.

## 4. Domain Model

- `Alarm`(id, label, baseTime: LocalTime, recurrence: Recurrence, profileId, enabled, conditions: List<Condition>, dismissalRequirements: List<DismissalRequirement>, audioPlan: AudioPlan, snoozePolicy: SnoozePolicy, ringerLayout: RingerLayoutPolicy, automationHooks: AutomationHooks, metadataJson)
- `Recurrence`: sealed — `Once`, `Daily`, `Weekly`, `ShiftPattern`, `Polyphasic`, `SolarAnchored`, `Adhan`, `Chained`, `Cron`.
- `Condition`: sealed — `HolidaySkip`, `DateRangeDisable`, `Geofence`, `WeatherAdvance`, `TrafficAdvance`, `AqiAdvance`, `TflDisruption`, `CalendarShift`, `IcsSkip`, `BedtimePenalty`, `TempSuspension(skipNext)`.
- `DismissalRequirement`: sealed — `TapButton`, `Cognitive(domain, difficulty)`, `Voice`, `Nfc(uid)`, `Qr(payloadHash)`, `Steps(n)`, `EyesOpenSelfie`, `MotionSustained`, `Distress(altCode)`.
- `AudioPlan`: source (`Local`, `SpotifyTrack`, `SpotifyPlaylist`, `SpotifyPodcast`, `DriveFile`, `Url`), preAlarm: PreAlarmCue?, volumeCurve, fadeOutSec, fallbackUri, castTargets, rotationPoolId.
- `SnoozePolicy`: baseMin, decrementMin, floorMin, maxCount, gestureMap.
- `RingerLayoutPolicy`: dismissSize, snoozeSize, cognitiveLoadRandomized, randomBounds, regenOnEachInstantiation.
- `AutomationHooks`: onFireIntents[], onDismissIntents[], dndProfile, networkToggle, smartLightPlan, smartPlugPlan.

## 5. Settings Registry (variable allowance)

- Single source: `:core:settings` ships `SettingsRegistry` exposing `Flow<List<SettingDescriptor<*>>>`.
- `SettingDescriptor<T>`(id, groupPath, label, helpText, type: Boolean|Int|Long|Float|String|Enum|Json|Color|Uri, default, validator, dependencies, featureFlag).
- Values stored in DataStore; per-alarm overrides stored in `Alarm.metadataJson` keyed by setting id.
- Settings Compose UI iterates the registry → renders editors generically (`@Composable fun renderDescriptor(d)`).
- Adding a feature flag = registering a descriptor; never editing the UI.
- Theme tokens are descriptors of `type=Color` / `type=String(font)` and are observed by `:core:ui` ThemeProvider.

## 6. Theming Engine

- Default palette `teal_black`: primary `#00C2B8`, secondary `#0F4C4A`, surface `#000000`, background `#000000`, onPrimary `#000000`, error `#FF5252`.
- ThemeRepository → MutableStateFlow<ThemeState>; CompositionLocalProvider injects `MaterialTheme(colorScheme, typography, shapes)`.
- User-supplied hex codes validated (regex `^#([0-9A-Fa-f]{6}|[0-9A-Fa-f]{8})$`); palette presets shipped + user-savable.
- Typography: Google Fonts via `androidx.compose.ui.text.googlefonts` + downloadable runtime fallback to bundled fonts.
- Dynamic colour (Material You) optional toggle, off by default (we own the brand).

## 7. Responsive UI Framework

- All scaffolds use `androidx.compose.material3.adaptive.Scaffold` + `WindowSizeClass`.
- Three list-detail layouts: compact (single pane), medium (list + detail bottom sheet), expanded (two-pane).
- All ringer surfaces use `BoxWithConstraints` + percentage offsets; the cognitive-load randomizer expresses `(x,y,scale)` as fractions of available bounds, never px.
- Foldables: `WindowInfoTracker` from `androidx.window` for hinge awareness.
- Orientation: free; ringer permits both landscape and portrait by computing button bounds reactively.
- Density: user-overridable density scale in `[theme]` group.

## 8. Alarm Engine

- Single boot-time `AlarmRescheduleReceiver` (BOOT_COMPLETED, TIMEZONE_CHANGED, MY_PACKAGE_REPLACED, LOCKED_BOOT_COMPLETED).
- `Scheduler.scheduleNextFor(alarmId)` computes the next fire instant by:
  1. Resolve recurrence next time.
  2. Apply skip conditions (holiday / date-range / temporary suspension / ICS / geofence).
  3. Apply advance conditions (weather, traffic, AQI, calendar, TfL, bedtime penalty, low battery).
  4. Apply solar / adhan adjustments.
  5. Apply chain offsets.
  6. Plug result into `AlarmManager.setAlarmClock(AlarmClockInfo(target, showIntent))`.
- Pre-alarm schedule produced as a sibling alarm at `target − preAlarmLeadMin`.
- Motion-fallback scheduled as one-shot `setExact()` at `dismissTime + motionWindowMin`.
- Background feed refresh via `WorkManager` periodic workers (weather/traffic/AQI/holidays/ICS/sync), all keyed by their own settings.

## 9. Ringer Flow

```
AlarmManager fires → AlarmReceiver → RingerForegroundService
  ├─ acquire WakeLock (SCREEN_BRIGHT)
  ├─ start ExoPlayer (selected source; fallback on stream failure)
  ├─ start cast adapter (one-shot LAN permission grant if cast enabled)
  ├─ broadcast DND/network/light/plug pre-fire intents
  └─ launch RingerActivity (FLAG_TURN_SCREEN_ON | FLAG_KEEP_SCREEN_ON | FLAG_SHOW_WHEN_LOCKED + setShowWhenLocked + setTurnScreenOn + fullScreenIntent)
RingerActivity:
  • renders DismissalRequirement sequence
  • if cognitiveLoadEnabled, regenerate (x,y,scale) on every recomposition for dismiss/snooze
  • intercepts hardware buttons (volume/power) when configured
  • emits forensics events to :core:logging
On dismiss:
  • play 10s fade-out via ExoPlayer volume ramp
  • broadcast onDismiss intents
  • restore network/DND/lights
  • schedule motion fallback if applicable
```

## 10. Cognitive-Load Randomizer

- Bounds: dismissal/snooze button anchor expressed as `(xFrac, yFrac, scale)` ∈ `[0,1]×[0,1]×[scaleMin,scaleMax]`.
- Regenerated via `remember(alarmInstanceId, recompositionTrigger) { random… }` keyed on recomposition trigger so each instantiation re-rolls.
- Hit-test still respects 48 dp minimum even at random scale.
- Settings: `randomXRange`, `randomYRange`, `randomScaleMin/Max`, `regenOnEachInstantiation`, `dismissBtnSizeMode (small/large/random)`, `snoozeBtnSizeMode`.

## 11. Audio Source Integrations

- Spotify: Spotify Android SDK (App Remote) for playback control + Web API for browse (PKCE OAuth). Supports Track / Playlist / Podcast URIs.
- Google Drive: REST v3 with OAuth scope **strictly `drive.file`**; the in-app picker uses Google Picker API to obtain file IDs without broader scope.
- Local: SAF (`ACTION_OPEN_DOCUMENT_TREE`) so we own a persistent URI permission to a chosen folder; user may also pick single files.
- All sources expose a uniform `AudioSourcePort` returning a `MediaSource` for ExoPlayer.

## 12. Cross-Ecosystem Casting

- `CastAdapter` interface with implementations: `GoogleCastAdapter`, `AirPlayAdapter`, `AlexaAdapter`.
- One-shot LAN permission grant: at fire time we request `NEARBY_WIFI_DEVICES` + temporary multicast lock (`WifiManager.MulticastLock`) and release after dismissal.
- Force-cast bypass: route audio via `MediaRouter` selection prior to ExoPlayer start; if cast fails within `castFallbackToLocalSec`, fall back to local speaker.

## 13. Permissions Inventory

- `SCHEDULE_EXACT_ALARM`, `USE_EXACT_ALARM`, `USE_FULL_SCREEN_INTENT`, `POST_NOTIFICATIONS`.
- `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, `FOREGROUND_SERVICE_SPECIAL_USE`.
- `RECEIVE_BOOT_COMPLETED`, `WAKE_LOCK`, `DISABLE_KEYGUARD` (only used to overlay; we never bypass user lock).
- `ACCESS_FINE_LOCATION`, `ACCESS_BACKGROUND_LOCATION` (for geofence + solar/adhan).
- `BODY_SENSORS`, `ACTIVITY_RECOGNITION` (steps).
- `RECORD_AUDIO`, `CAMERA` (voice + selfie dismissal).
- `NFC`.
- `BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN` (smart-light, wearable BT path).
- `ACCESS_NETWORK_STATE`, `CHANGE_WIFI_STATE`, `CHANGE_NETWORK_STATE` (network toggle, casting).
- `READ_CALENDAR`.
- `MANAGE_EXTERNAL_STORAGE`: NOT requested. We use SAF.
- All requested at the moment of feature first-use, not bundled at launch.

## 14. Security & Privacy

- All third-party tokens stored via Android Keystore-encrypted DataStore.
- Distress code path is non-interruptible from foreground UI; SOS dispatched via WorkManager with `BACKOFF_LINEAR`.
- Cloud-sync payloads encrypted client-side (AES-GCM) with a key wrapped by Android Keystore.
- No analytics SDK by default; opt-in event log only.

## 15. Testing Strategy

- TDD per implementation plan. Unit tests for every use-case in `:core:domain`.
- Property tests (Kotest) for Scheduler next-fire calculation under interactions of conditions.
- Roborazzi screenshot tests for theme rendering at dark/light × small/medium/expanded.
- Instrumented tests for Ringer (Espresso + UiAutomator for full-screen intent flow).

## 16. Out-of-Scope (YAGNI for v1)

- iOS / desktop ports.
- Voice assistant (Alexa skill / Google Assistant action) beyond cast target.
- Multi-tenant sharing.
- Server-side LLM gateway (cognitive captcha LLM remains opt-in pointing at user-supplied endpoint).

## 17. Risks & Mitigations

- **OEM aggressive battery savers** (Xiaomi, Huawei) kill exact alarms → mitigation: detect manufacturer, surface guidance dialog, and offer foreground "watchdog" service.
- **Spotify Free** users cannot trigger track playback → fallback to preview URLs only with banner explanation.
- **Drive `drive.file` scope** restricts to files created/opened by the app → use Google Picker for compliant file selection.
- **Cast race conditions** — implement explicit timeout + local fallback.
- **Cognitive captcha accessibility** — settings include `accessibilityMode` that swaps random-position dismissal for a high-contrast static layout.

## 18. Acceptance Criteria

- Building the project with `./gradlew :app:assembleDebug` succeeds on Android Studio Hedgehog 2024.x+ / JDK 21.
- 100% of features in `FEATURES.md` mapped to a task in the implementation plan.
- No hardcoded numeric / boolean / string thresholds outside `SettingsRegistry` (enforced by Detekt rule + lint).
- All UI verified at compact/medium/expanded WindowSizeClass + portrait/landscape with no clipping (Roborazzi snapshots committed).
- Default palette renders teal-on-black; user-configured hex palettes apply hot-reload.
