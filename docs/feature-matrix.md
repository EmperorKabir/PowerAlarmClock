# Feature Matrix

Living doc mapping every mandated/added feature → setting descriptor IDs → user-visible UI surface → backing implementation.

Status legend:
- 🟢 fully wired (descriptor + UI + engine)
- 🟡 descriptor + UI present; engine integration partial (uses `ConditionContext` placeholders)
- 🔴 descriptor only (no UI yet)

| # | Feature | Descriptor IDs | UI Surface | Engine | Status |
| - | ------- | -------------- | ---------- | ------ | ------ |
| 1 | Region holiday skip | `regionTag`, `holidayProvider`, `skipBankHolidays`, `skipPublicHolidays`, `customHolidayCalendarUri` | Settings → Sleep & schedule. Alarm edit → Skip → Public holidays | `Condition.HolidaySkip` in `NextFireCalculator.shouldSkip` | 🟡 |
| 2 | Date-range disable | `disabledRanges` | Settings → Sleep & schedule (advanced). Alarm edit → Skip → custom JSON | `Condition.DateRangeDisable.inRange` | 🟢 |
| 3 | Skip next N | `skipNextOccurrences` | Settings → Sleep & schedule | `Condition.TempSuspension` | 🟢 |
| 4 | Snooze dynamics | `snoozeBaseMinutes`, `snoozeDecrementMinutes`, `snoozeFloorMinutes`, `snoozeMaxCount` | Alarm edit → Snooze | `SnoozePolicy` in alarm engine | 🟢 |
| 5 | Light-sleep wake | `fitbitEnabled`, `wakeWindowMin`, `lightSleepProvider` | Settings → Sleep & schedule | `:integrations:fitbit` port | 🟡 |
| 6 | Cognitive captcha | `cognitiveEnabled`, `problemDomains`, `difficulty`, `requiredCorrect`, `failurePenaltyAddSnooze` | Alarm edit → How I prove I'm awake | `feature/ringer/CognitiveLoadLayout` | 🟢 |
| 7 | Volume curve | `volumeCurve`, `volumeRampMin`, `volumeStartPct`, `volumeEndPct`, `volumeCurvePoints` | Alarm edit → Sound | `core/audio/VolumeCurve.ramp()` | 🟢 |
| 8 | Pre-alarm cue | `preAlarmEnabled`, `preAlarmLeadMin`, `preAlarmVolumePct`, `preAlarmSourceUri` | Alarm edit → Sound | `AudioPlan.preAlarm` | 🟡 |
| 9 | Calendar shifts | `calendarShiftEnabled`, `calendarIds`, `eventLookaheadMin`, `commuteBufferMin` | Alarm edit → Wake earlier | `Condition.CalendarShift` | 🟡 |
| 10 | Motion fallback | `motionFallbackEnabled`, `motionWindowMin`, `motionThresholdMs2`, `fallbackVolumePct` | Settings → Don't oversleep | `feature/ringer/MotionMonitor` | 🟡 |
| 11 | Shift rotation | `shiftPatternJson`, `shiftAnchorDate` | Alarm edit (advanced) | `Recurrence.ShiftPattern` | 🔴 |
| 12 | Weather advance | `weatherEnabled`, `weatherProvider`, `precipitationAdvanceMin`, `freezeAdvanceMin`, etc. | Alarm edit → Wake earlier | `:integrations:weather` + `applyAdvance` | 🟡 |
| 13 | Traffic advance | `trafficEnabled`, `trafficProvider`, `originLat/Lng`, `destLat/Lng`, `maxAdvanceMin` | Alarm edit → Wake earlier | `:integrations:traffic` + `applyAdvance` | 🟡 |
| 14 | Tasker / smart-home | `intentActionsOnDismiss`, `taskerVariablesJson` | Settings → Smart triggers (advanced) | `:integrations:tasker` | 🟡 |
| 15 | Edit lock | `editLockEnabled`, `editLockHours` | Settings → Lock-down | `feature/alarmlist` enforcement | 🔴 |
| 16 | Forced lockout | `lockoutEnabled`, `lockoutAllowedActions` | Settings → Lock-down | `RingerActivity.setShowWhenLocked` | 🟡 |
| 17 | Snooze gestures | `gestureMapJson` | Settings → Sleep & schedule (advanced) | `feature/ringer` gesture handler | 🔴 |
| 18 | Stream fallback | `streamFallbackEnabled`, `streamTimeoutSec`, `localFallbackUri`, `audioFallbackVolumePct` | Settings → Sounds I love | `core/audio/PlaybackChain` | 🟡 |
| 19 | Geofence | `homeLat`, `homeLng`, `homeRadiusM`, `geofenceMode` | Alarm edit → Skip → Only at home. Settings → Travel | `Condition.Geofence` + LocationManager | 🟡 |
| 20 | Bedtime penalty | `bedtimeStart`, `screenOnGraceMin`, `morningAdvanceMin` | Alarm edit → Wake earlier → I broke my bedtime | `Condition.BedtimePenalty` | 🟢 |
| 21 | Battery failsafe | `batteryFailsafeEnabled`, `batteryThresholdPct`, `requireUncharged`, `earlyFireMin` | Settings → Don't oversleep. Alarm edit → Wake earlier → Phone almost dead | `Condition.LowBatteryFailsafe` | 🟢 |
| 22 | Sub-alarm chain | `chainOffsetsMin`, `chainTones` | Settings (advanced) | `Recurrence.Chained` | 🟡 |
| 23 | Pre-motion auto-dismiss | `preMotionEnabled`, `preMotionWindowMin`, `preMotionThresholdMs2` | Settings → Don't oversleep | `core/scheduler/MotionWorker` | 🟡 |
| 24 | ICS skip | `icsSourceUris`, `skipMatchingSummariesRegex` | Alarm edit → Skip → Calendar event titles | `Condition.IcsSkip` | 🟡 |
| 25 | Hardware buttons | `disableVolumeButtons`, `disablePowerButton`, `disableLockHomeRecents` | Settings → Don't oversleep | `feature/ringer/RingerActivity` key handler | 🟡 |
| 26 | Network toggle | `networkToggleMode`, `restoreOnDismiss` | Settings → Smart triggers | `core/scheduler/AlarmReceiver` | 🟡 |
| 27 | Polyphasic | `polyphasicTemplate`, `polyphasicAnchorTime`, `templateOverrides` | Settings → Sleep & schedule (advanced) | `Recurrence.Polyphasic` | 🔴 |
| 28 | Cloud sync | `syncProvider`, `syncEncryptionKeyAlias`, `autoSyncIntervalMin` | Settings → Backup & devices | `core/data/sync/CloudSyncWorker` | 🟡 |
| 29 | Fade-out | `fadeOutSec`, `fadeOutCurve` | Alarm edit → Sound. Settings → Sounds I love | `core/audio/VolumeCurve.fadeOut` | 🟢 |
| 30 | Cast targets | `castTargets`, `castFallbackToLocalSec`, `oneShotPermissionScope` | Settings → Backup & devices | `:integrations:cast` | 🟡 |
| 31 | Multi-domain captcha | `problemBank`, `dynamicLLMProblems`, `llmEndpoint` | Settings → Wake-up tasks (advanced) | `feature/ringer/cognitive/ProblemBank` | 🟡 |
| 32 | Voice passphrase | `voiceEnabled`, `enrolledPhraseHash`, `voiceMatchConfidence` | Alarm edit → How I prove I'm awake → Speak a phrase | `feature/ringer/voice` (TODO ML Kit) | 🔴 |
| 33 | NFC dismiss | `nfcEnabled`, `requiredTagUid` | Alarm edit → How I prove I'm awake → Scan an NFC tag | `:integrations:nfc` | 🟡 |
| 34 | QR dismiss | `qrEnabled`, `requiredQrPayloadHash` | Alarm edit → How I prove I'm awake → Scan a QR code | `feature/ringer/qr` (CameraX + ML Kit) | 🟡 |
| 35 | Steps dismiss | `stepGoal`, `stepWindowMin` | Alarm edit → How I prove I'm awake → Walk N steps | `feature/ringer/steps` | 🟡 |
| 36 | Eyes-open selfie | `faceEnabled`, `eyesOpenThreshold`, `attemptsMax` | Alarm edit → How I prove I'm awake → Eyes-open selfie | `feature/ringer/face` (ML Kit Face) | 🟡 |
| 37 | Solar anchor | `solarAnchor`, `solarOffsetMin` | Settings → Sleep & schedule | `Recurrence.SolarAnchored` | 🟡 |
| 38 | Adhan | `adhanEnabled`, `calculationMethod`, `madhab`, `adhanToneUri` | Settings → Sleep & schedule | `Recurrence.Adhan` | 🟡 |
| 39 | Smart light | `lightProvider`, `lightTargets`, `rampStartLeadMin`, `rampCurve`, `targetKelvin` | Settings → Smart triggers | `:integrations:smarthome` | 🟡 |
| 40 | Smart plug | `plugTargets`, `plugLeadMin`, `plugAutoOffMin` | Settings → Smart triggers | `:integrations:smarthome` | 🟡 |
| 41 | DND on fire | `dndModeOnFire`, `dndAllowList` | Settings → Smart triggers | `core/scheduler/AlarmReceiver` | 🟡 |
| 42 | AQI advance | `aqiProvider`, `aqiAdvanceThresholdPm25`, `aqiAdvanceMin` | Alarm edit → Wake earlier → Bad air quality | `:integrations:airquality` | 🟡 |
| 43 | Emergency override | `emergencyEnabled`, `emergencyMagnitudeMin`, `emergencyRadiusKm`, `emergencyToneUri` | Settings → Lock-down | `:integrations:emergency` (USGS) | 🟡 |
| 44 | Profiles | `activeProfile`, `profileDefinitionsJson` | More → Profiles | `feature/profiles` + `ProfilesViewModel` | 🟢 |
| 45 | Wear OS | `wearCompanionEnabled`, `wearAuthToken` | Settings → Backup & devices | `:feature:wear` | 🟡 |
| 46 | REST API | `apiEnabled`, `apiPort`, `apiTokenHash`, `apiBindInterface` | Settings → Backup & devices (advanced) | `:integrations:rest-api` (Ktor) | 🟡 |
| 47 | Driving ETA | `etaDestLat`, `etaDestLng`, `etaArriveBy`, `etaPrepBufferMin` | Settings → Travel | `:integrations:traffic.eta` | 🟡 |
| 48 | TfL disruption | `tflLines`, `tflStops`, `tflMaxAdvanceMin` | Alarm edit → Wake earlier → Tube disruption | `:integrations:tfl` | 🟡 |
| 49 | Sound rotation | `rotationPoolUris`, `rotationCooldownDays` | Settings → Sounds I love | `core/audio/RotationPicker` | 🟡 |
| 50 | Distress code | `distressCodeHash`, `sosContacts`, `sosMessageTemplate`, `sosLocationPing` | Alarm edit → How I prove I'm awake → Distress code. Settings → Lock-down | `feature/ringer/distress` | 🟡 |
| ★ | Timezone (new) | `alarmDefaultTimezoneMode`, `alarmHomeTimezone`, `travelDetectionEnabled`, `travelArrivalPrompt`, per-alarm `timezoneMode` + `timezoneId` | Alarm edit → Travel & timezones. Travel banner on Alarms tab. Settings → Travel | `NextFireCalculator.resolveZone` | 🟢 |
| ★ | Onboarding (new) | `hasCompletedOnboarding`, `expertModeEnabled` | First-launch wizard. More → Onboarding | `feature/onboarding` + OEM intent helper | 🟢 |

## Cross-cutting

| Concern | Descriptors | UI |
| ------- | ----------- | -- |
| Theme tokens | 17 (`primaryHex`, `surfaceHex`, `typographyFamily`, `cornerRadiusDp`, `darkModeAuto`, `useDynamicColor`, …) | More → Theme |
| Ringer layout | 11 (`dismissBtnSizeMode`, `cognitiveLoadEnabled`, `randomXMin`, …) | Alarm edit → Wake-up screen |
| Adaptive layout | 3 (`adaptiveLayoutEnabled`, `compactBreakpointDp`, `mediumBreakpointDp`) | Settings → Look & feel |

## How to add a new feature

1. Add a `SettingDescriptor` to `core/settings/RegistrySeed.kt` with friendly `label`, `helpText`, `category`, and `advanced` flag.
2. If the feature is per-alarm, extend `Alarm` (or its sub-aggregates) and migrate Room.
3. Surface in either `feature/alarm-edit/AlarmEditScreen` (per-alarm) or `feature/settings/SettingsScreen` (global) — the latter is automatic if you've added a descriptor.
4. Wire the engine through `:core:scheduler` (recurrence/skip/advance) or `:feature:ringer` (dismissal).
5. Append a row to this matrix.
