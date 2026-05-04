# Power Alarm Clocks — Feature Registry

- Source of truth for every behavioural feature.
- Every numeric / string / boolean flag listed here MUST be wired through `core-settings` as a `SettingDescriptor` — no hardcoded values in feature code.
- Group key in brackets `[group]` maps to the settings UI section.

## A. Mandated 30 (per spec)

1. **Region holiday skipping** `[holidays]` — pulls public + bank holidays for region; default region `London, United Kingdom (en-GB)`. Variables: `regionTag`, `holidayProvider`, `skipBankHolidays`, `skipPublicHolidays`, `customHolidayCalendarUri`.
2. **Date-range disable** `[schedule]` — disable months / arbitrary `(start,end)` brackets. Variables: `disabledRanges[]`.
3. **Temporary suspension** `[schedule]` — skip next N firings without delete. Variables: `skipNextOccurrences`.
4. **Snooze decreasing duration + max count** `[snooze]` — Variables: `snoozeBaseMinutes`, `snoozeDecrementMinutes`, `snoozeFloorMinutes`, `snoozeMaxCount`.
5. **Wearable light-sleep wake (Fitbit)** `[wearable]` — pull intraday sleep stages, fire during light phase within `(targetTime − wakeWindowMin, targetTime)`. Variables: `fitbitEnabled`, `wakeWindowMin`, `lightSleepProvider`.
6. **Cognitive captcha dismissal** `[dismiss.cognitive]` — probability / statistics / logic. Variables: `cognitiveEnabled`, `problemDomains[]`, `difficulty`, `requiredCorrect`, `failurePenaltyAddSnooze`.
7. **Progressive volume escalation** `[audio.volume]` — curve over interval. Variables: `volumeCurve` (linear/exp/custom JSON points), `volumeRampMin`, `volumeStartPct`, `volumeEndPct`.
8. **Pre-alarm gentle wake** `[audio.preAlarm]` — faint cue 15–30 min prior. Variables: `preAlarmEnabled`, `preAlarmLeadMin`, `preAlarmVolumePct`, `preAlarmSourceUri`.
9. **Calendar-driven shifts** `[calendar]` — advance for early events. Variables: `calendarShiftEnabled`, `calendarIds[]`, `eventLookaheadMin`, `commuteBufferMin`.
10. **Motion-fallback secondary alarm** `[failsafe.motion]` — hi-vol re-fire if no motion N min post-dismissal. Variables: `motionFallbackEnabled`, `motionWindowMin`, `motionThresholdMs2`, `fallbackVolumePct`.
11. **Irregular shift rotation** `[schedule.shifts]` — non-standard cycles (e.g. 4-on/4-off). Variables: `shiftPatternJson`, `shiftAnchorDate`.
12. **Weather advance** `[weather]` — advance if precipitation/freeze. Variables: `weatherEnabled`, `weatherProvider`, `precipitationAdvanceMin`, `freezeAdvanceMin`, `precipitationThresholdMm`, `freezeThresholdC`.
13. **Traffic API advance** `[traffic]` — real-time commute. Variables: `trafficEnabled`, `trafficProvider`, `originLat/Lng`, `destLat/Lng`, `maxAdvanceMin`.
14. **Smart-home / Tasker intent broadcast** `[automation]` — on dismissal. Variables: `intentActionsOnDismiss[]`, `taskerVariablesJson`.
15. **Strict-mode editing lock** `[security]` — block edit/disable < threshold hours. Variables: `editLockEnabled`, `editLockHours`.
16. **Forced screen lockout** `[ringer.lockout]` — block OS nav until dismissed. Variables: `lockoutEnabled`, `lockoutAllowedActions[]`.
17. **Custom snooze gestures** `[snooze.gesture]` — swipe directions / orientations → durations. Variables: `gestureMapJson`.
18. **Streaming fallback to local file** `[audio.fallback]` — Variables: `streamFallbackEnabled`, `streamTimeoutSec`, `localFallbackUri`, `fallbackVolumePct`.
19. **Geofence disable away from home** `[geofence]` — Variables: `homeLat/Lng`, `homeRadiusM`, `geofenceMode` (disable/keep).
20. **Bedtime usage penalty** `[bedtime.penalty]` — advance morning by interval if screen-on past bedtime. Variables: `bedtimeStart`, `screenOnGraceMin`, `morningAdvanceMin`.
21. **Low-battery failsafe early fire** `[failsafe.battery]` — Variables: `batteryFailsafeEnabled`, `batteryThresholdPct`, `requireUncharged`, `earlyFireMin`.
22. **Sub-alarm chain** `[schedule.chain]` — sequential linked alarms with offsets. Variables: `chainOffsetsMin[]`, `chainTones[]`.
23. **Auto-dismiss on sustained motion before fire** `[failsafe.preMotion]` — Variables: `preMotionEnabled`, `preMotionWindowMin`, `preMotionThresholdMs2`.
24. **`.ICS` import for skip days** `[calendar.ics]` — Variables: `icsSourceUris[]`, `skipMatchingSummariesRegex`.
25. **Hardware button override** `[ringer.buttons]` — disable vol/power snooze. Variables: `disableVolumeButtons`, `disablePowerButton`, `disableLockHomeRecents`.
26. **Network state toggle** `[network]` — disable on activation, restore on dismiss. Variables: `networkToggleMode` (none/wifi/data/all), `restoreOnDismiss`.
27. **Polyphasic templates** `[schedule.polyphasic]` — Uberman / Everyman / Dymaxion presets. Variables: `polyphasicTemplate`, `polyphasicAnchorTime`, `templateOverrides`.
28. **Cloud config sync** `[sync]` — encrypted alarm config to user cloud. Variables: `syncProvider` (drive/dropbox/webdav), `syncEncryptionKeyAlias`, `autoSyncIntervalMin`.
29. **Audio fade-out 10 s on dismiss** `[audio.fadeOut]` — Variables: `fadeOutSec` default 10, `fadeOutCurve`.
30. **Cross-ecosystem speaker casting** `[cast]` — transient one-off LAN permission grant; force-cast to Cast / AirPlay / Alexa, bypassing local routing. Variables: `castTargets[]` (deviceType, host, port), `castFallbackToLocalSec`, `oneShotPermissionScope`.

## B. Power-User Additions (20)

31. **Multi-domain captcha library** `[dismiss.cognitive]` — algebra / integrals / Bayesian / sequences / SQL parsing / regex matching. Variables: `problemBank`, `dynamicLLMProblems`, `llmEndpoint`.
32. **Voice passphrase dismissal** `[dismiss.voice]` — on-device speaker-verification + transcription. Variables: `voiceEnabled`, `enrolledPhraseHash`, `voiceMatchConfidence`.
33. **NFC-tag dismissal** `[dismiss.nfc]` — tap a registered tag (e.g. kitchen). Variables: `requiredTagUid`, `nfcEnabled`.
34. **QR-code dismissal** `[dismiss.qr]` — scan a code physically located elsewhere. Variables: `requiredQrPayloadHash`, `qrEnabled`.
35. **Step-count dismissal** `[dismiss.steps]` — N steps via pedometer. Variables: `stepGoal`, `stepWindowMin`.
36. **Eyes-open selfie dismissal** `[dismiss.face]` — front camera + ML Kit Face Detection (eye-open probability ≥ threshold). Variables: `faceEnabled`, `eyesOpenThreshold`, `attemptsMax`.
37. **Civil-twilight anchored alarm** `[schedule.solar]` — fire at sunrise ± offset using lat/lng. Variables: `solarAnchor` (sunrise/sunset/civilDawn/nauticalDawn), `solarOffsetMin`.
38. **Prayer-time / Adhan alarms** `[schedule.adhan]` — five daily prayers via lat/lng + computation method. Variables: `adhanEnabled`, `calculationMethod` (MWL/ISNA/Egyptian/UmmAlQura/Karachi/Tehran/Jafari), `madhab`, `adhanToneUri`.
39. **Smart-light pre-alarm gradient** `[smartlight]` — Hue / LIFX / Matter ramp brightness from 0 → target over N min. Variables: `lightProvider`, `lightTargets[]`, `rampStartLeadMin`, `rampCurve`, `targetKelvin`.
40. **Smart-plug appliance trigger** `[smartplug]` — turn on coffee maker / heater N min pre-alarm. Variables: `plugTargets[]`, `plugLeadMin`, `plugAutoOffMin`.
41. **Per-alarm DND profile** `[dnd]` — flip Do-Not-Disturb on activation, restore on dismiss. Variables: `dndModeOnFire`, `dndAllowList[]`.
42. **Air-quality-triggered advance** `[airquality]` — advance if overnight PM2.5 / NO₂ spike. Variables: `aqiProvider`, `aqiAdvanceThresholdPm25`, `aqiAdvanceMin`.
43. **Emergency-broadcast override channel** `[emergency]` — USGS earthquake feed + national CAP alerts force-fire. Variables: `emergencyEnabled`, `emergencyMagnitudeMin`, `emergencyRadiusKm`, `emergencyToneUri`.
44. **Profile switching (persona)** `[profiles]` — Work / Holiday / Travel / Custom; bulk enables a labeled subset. Variables: `activeProfile`, `profileDefinitionsJson`.
45. **Wear OS companion** `[wearable.wear]` — set / snooze / dismiss from watch; falls back to phone. Variables: `wearCompanionEnabled`, `wearAuthToken`.
46. **Local-LAN REST API** `[api]` — token-protected HTTPS endpoint on `0.0.0.0:port` for cross-device alarm control. Variables: `apiEnabled`, `apiPort`, `apiTokenHash`, `apiBindInterface`.
47. **Driving-ETA destination alarm** `[traffic.eta]` — Maps Directions arrival-by → wake time. Variables: `etaDestLat/Lng`, `etaArriveBy`, `etaPrepBufferMin`.
48. **TfL Tube/Bus disruption advance** `[traffic.tfl]` — London transit live disruption shifts wake. Variables: `tflLines[]`, `tflStops[]`, `tflMaxAdvanceMin`.
49. **Rotating tone library** `[audio.rotation]` — never repeat tone within N days. Variables: `rotationPoolUris[]`, `rotationCooldownDays`.
50. **Distress-code silent SOS** `[security.distress]` — alternate dismiss code triggers silent SOS to contacts + location ping. Variables: `distressCodeHash`, `sosContacts[]`, `sosMessageTemplate`, `sosLocationPing`.

## Settings Variable Allowance Mandate

- Every entry above has at least one variable; the variable set is the authoritative knob list.
- `core-settings` ships a `SettingsRegistry` enumerating every key with: `id`, `group`, `label`, `type`, `default`, `range/choices`, `dependsOn`, `featureFlag`.
- The Settings UI is rendered from the registry — adding a feature variable means adding a row in the registry, never editing UI code.
- `ThemeRegistry` mirrors this for hex codes, typography choices, density, motion duration, corner radius, etc.

## Cross-cutting Variable Surfaces

- **Theming** `[theme]`: `primaryHex`, `secondaryHex`, `tertiaryHex`, `surfaceHex`, `errorHex`, `onPrimaryHex`, `typographyFamily`, `displayFontUri`, `bodyFontUri`, `cornerRadiusDp`, `densityScale`, `motionDurationMs`, `darkModeAuto`, `useDynamicColor`, `defaultPaletteId="teal_black"`.
- **Ringer UI** `[ringer.layout]`: `dismissBtnSizeMode` (small/large/random), `snoozeBtnSizeMode`, `cognitiveLoadEnabled`, `randomXRange`, `randomYRange`, `randomScaleMin`, `randomScaleMax`, `regenOnEachInstantiation`.
- **Window adaptation** `[ui.responsive]`: breakpoints driven by Compose `WindowSizeClass`; orientation/foldable handled adaptively — no fixed pixel layouts.
