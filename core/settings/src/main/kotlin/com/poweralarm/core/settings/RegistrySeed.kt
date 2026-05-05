@file:Suppress("LargeClass", "LongMethod", "MaxLineLength")

package com.poweralarm.core.settings

import com.poweralarm.core.settings.SettingDescriptor.BoolSetting
import com.poweralarm.core.settings.SettingDescriptor.ColorSetting
import com.poweralarm.core.settings.SettingDescriptor.EnumSetting
import com.poweralarm.core.settings.SettingDescriptor.FloatSetting
import com.poweralarm.core.settings.SettingDescriptor.IntSetting
import com.poweralarm.core.settings.SettingDescriptor.JsonSetting
import com.poweralarm.core.settings.SettingDescriptor.StringSetting
import com.poweralarm.core.settings.SettingsCategory.ADVANCED
import com.poweralarm.core.settings.SettingsCategory.APPEARANCE
import com.poweralarm.core.settings.SettingsCategory.AUTOMATION
import com.poweralarm.core.settings.SettingsCategory.BACKUP
import com.poweralarm.core.settings.SettingsCategory.DONT_OVERSLEEP
import com.poweralarm.core.settings.SettingsCategory.MOTIVATION
import com.poweralarm.core.settings.SettingsCategory.SECURITY
import com.poweralarm.core.settings.SettingsCategory.SLEEP_QUALITY
import com.poweralarm.core.settings.SettingsCategory.SOUND
import com.poweralarm.core.settings.SettingsCategory.TRAVEL
import com.poweralarm.core.settings.SettingsCategory.WAKE_UP

/**
 * Seeds [InMemorySettingsRegistry] with every variable enumerated in FEATURES.md.
 * Adding a new feature variable means adding one line here — never editing settings UI.
 *
 * Every entry includes:
 *   - friendly user-facing [label]
 *   - plain-English [helpText] — what does this actually do?
 *   - [category] for grouping in the human-friendly Settings screen
 *   - [advanced] flag — power-user knobs hidden behind a master toggle
 */
@RegistryConstant
object RegistrySeed {

    fun build(): InMemorySettingsRegistry = InMemorySettingsRegistry(allDescriptors())

    @Suppress("LongMethod")
    fun allDescriptors(): List<SettingDescriptor<*>> = buildList {
        // ── Onboarding gating (new in Phase E) ────────────────────────────────────────────────
        add(BoolSetting("hasCompletedOnboarding", "onboarding", "Onboarding complete", helpText = "Internal flag — true after first-run wizard finishes.", default = false, category = ADVANCED, advanced = true))
        add(BoolSetting("expertModeEnabled", "onboarding", "Expert mode", helpText = "Show advanced power-user settings throughout the app.", default = false, category = ADVANCED))

        // ── Travel & timezones (Phase A) ──────────────────────────────────────────────────────
        add(EnumSetting("alarmDefaultTimezoneMode", "travel", "Default timezone for new alarms", helpText = "‘Device’ = use whatever zone your phone is currently in. ‘Home zone’ = always fire at the time in your home zone, even abroad.", default = "device", choices = listOf("device", "home"), category = TRAVEL))
        add(StringSetting("alarmHomeTimezone", "travel", "Home timezone", helpText = "IANA zone, e.g. Europe/London or America/New_York.", default = "Europe/London", category = TRAVEL))
        add(BoolSetting("travelDetectionEnabled", "travel", "Detect when I'm abroad", helpText = "Show a banner when your device is in a different timezone from your home zone, with a one-tap action to convert your alarms.", default = true, category = TRAVEL))
        add(EnumSetting("travelArrivalPrompt", "travel", "When I arrive somewhere new", helpText = "Decide ahead of time what should happen to your alarms when you cross timezones.", default = "ask", choices = listOf("ask", "keep_local", "keep_home"), category = TRAVEL))

        // ── A. Mandated 30 ────────────────────────────────────────────────────────────────────
        // 1. Region holiday skipping
        add(EnumSetting("regionTag", "holidays", "My country", helpText = "Used to look up public holidays so alarms can skip them.", default = "GB", choices = COUNTRY_CODES, category = SLEEP_QUALITY))
        add(EnumSetting("holidayProvider", "holidays", "Holiday source", helpText = "Where to fetch the holiday list from.", default = "nager", choices = listOf("nager", "calendarific", "ical"), category = SLEEP_QUALITY, advanced = true))
        add(BoolSetting("skipBankHolidays", "holidays", "Skip bank holidays", helpText = "Don't fire alarms on bank holidays.", default = true, category = SLEEP_QUALITY))
        add(BoolSetting("skipPublicHolidays", "holidays", "Skip public holidays", helpText = "Don't fire alarms on national public holidays.", default = true, category = SLEEP_QUALITY))
        add(StringSetting("customHolidayCalendarUri", "holidays", "Personal holiday calendar (URL)", helpText = "Optional .ics URL of dates to skip — e.g. an annual-leave calendar from work.", default = "", category = SLEEP_QUALITY, advanced = true))

        // 2. Date-range disable
        add(JsonSetting("disabledRanges", "schedule", "Disabled date ranges", helpText = "JSON list of date ranges in which all alarms are off. Useful for holidays or sick leave.", default = "[]", category = SLEEP_QUALITY, advanced = true))

        // 3. Temporary suspension
        add(IntSetting("skipNextOccurrences", "schedule", "Skip the next N occurrences", helpText = "0 = don't skip. 1 = skip tomorrow's. 7 = skip the next week, etc.", default = 0, range = 0..365, category = SLEEP_QUALITY))

        // 4. Snooze dynamics
        add(IntSetting("snoozeBaseMinutes", "snooze", "Snooze length (minutes)", helpText = "How long the first snooze lasts.", default = 9, range = 1..60, category = SLEEP_QUALITY))
        add(IntSetting("snoozeDecrementMinutes", "snooze", "Make each snooze shorter by", helpText = "If 1, each successive snooze shrinks by a minute. Set to 0 to keep them all the same.", default = 1, range = 0..30, category = SLEEP_QUALITY))
        add(IntSetting("snoozeFloorMinutes", "snooze", "Shortest possible snooze", helpText = "Once snoozes shrink to this value, they stop shrinking.", default = 1, range = 1..30, category = SLEEP_QUALITY))
        add(IntSetting("snoozeMaxCount", "snooze", "Max number of snoozes", helpText = "After this many, the alarm refuses to snooze again.", default = 5, range = 0..50, category = SLEEP_QUALITY))

        // 5. Fitbit light-sleep wake
        add(BoolSetting("fitbitEnabled", "wearable", "Wake on light sleep (Fitbit)", helpText = "Use Fitbit's sleep stages so the alarm fires while you're already in light sleep — feels less jarring.", default = false, featureFlag = "wearable.fitbit", category = SLEEP_QUALITY))
        add(IntSetting("wakeWindowMin", "wearable", "Light-sleep wake window (minutes)", helpText = "How early the alarm is allowed to fire if it catches you in light sleep.", default = 30, range = 5..90, category = SLEEP_QUALITY))
        add(EnumSetting("lightSleepProvider", "wearable", "Sleep tracker", helpText = "Which device's sleep data to use.", default = "fitbit", choices = listOf("fitbit", "samsung", "garmin", "googlefit"), category = SLEEP_QUALITY))

        // 6. Cognitive captcha
        add(BoolSetting("cognitiveEnabled", "dismiss.cognitive", "Solve a problem to dismiss", helpText = "Force yourself awake by solving a maths or logic problem.", default = false, category = MOTIVATION))
        add(JsonSetting("problemDomains", "dismiss.cognitive", "Problem types", helpText = "JSON list of topics, e.g. probability, statistics, logic.", default = """["probability","statistics","logic"]""", category = MOTIVATION, advanced = true))
        add(EnumSetting("difficulty", "dismiss.cognitive", "Difficulty", helpText = "Easier problems are quicker; ‘evil’ is genuinely hard.", default = "medium", choices = listOf("easy", "medium", "hard", "evil"), category = MOTIVATION))
        add(IntSetting("requiredCorrect", "dismiss.cognitive", "Problems to solve", helpText = "How many you need to get right before the alarm dismisses.", default = 1, range = 1..20, category = MOTIVATION))
        add(IntSetting("failurePenaltyAddSnooze", "dismiss.cognitive", "Wrong answer penalty (minutes)", helpText = "Add this many minutes of snooze for each wrong answer.", default = 0, range = 0..30, category = MOTIVATION))

        // 7. Progressive volume
        add(EnumSetting("volumeCurve", "audio.volume", "How the volume ramps up", helpText = "Linear = even climb. Exponential / log = subtler at first then snappy. Stepped = jumps in chunks.", default = "linear", choices = listOf("linear", "exponential", "log", "stepped", "custom"), category = WAKE_UP))
        add(IntSetting("volumeRampMin", "audio.volume", "Ramp duration (minutes)", helpText = "How long the volume takes to climb from start to full.", default = 5, range = 0..60, category = WAKE_UP))
        add(IntSetting("volumeStartPct", "audio.volume", "Starting volume %", helpText = "How loud the alarm begins.", default = 10, range = 0..100, category = WAKE_UP))
        add(IntSetting("volumeEndPct", "audio.volume", "Final volume %", helpText = "How loud the alarm ends up.", default = 100, range = 0..100, category = WAKE_UP))
        add(JsonSetting("volumeCurvePoints", "audio.volume", "Custom curve points", helpText = "JSON points if you've picked the ‘custom’ curve.", default = "[]", category = WAKE_UP, advanced = true))

        // 8. Pre-alarm gentle wake
        add(BoolSetting("preAlarmEnabled", "audio.preAlarm", "Pre-alarm cue", helpText = "Play a gentle sound a few minutes before the real alarm so you wake more naturally.", default = false, category = WAKE_UP))
        add(IntSetting("preAlarmLeadMin", "audio.preAlarm", "How many minutes before", helpText = "Lead time for the gentle pre-alarm cue.", default = 20, range = 1..60, category = WAKE_UP))
        add(IntSetting("preAlarmVolumePct", "audio.preAlarm", "Pre-alarm volume %", helpText = "Keep this low — you're nudging, not ringing.", default = 8, range = 1..50, category = WAKE_UP))
        add(StringSetting("preAlarmSourceUri", "audio.preAlarm", "Pre-alarm sound (URI)", helpText = "Audio URI for the cue. Leave empty for system default.", default = "", category = WAKE_UP, advanced = true))

        // 9. Calendar shifts
        add(BoolSetting("calendarShiftEnabled", "calendar", "Wake earlier for early calendar events", helpText = "If you've got a meeting at 7, wake earlier so you've got time.", default = false, category = SLEEP_QUALITY))
        add(JsonSetting("calendarIds", "calendar", "Calendar IDs to scan", helpText = "JSON list. Leave empty to scan all.", default = "[]", category = SLEEP_QUALITY, advanced = true))
        add(IntSetting("eventLookaheadMin", "calendar", "Event lookahead (minutes)", helpText = "How far ahead to scan for early events.", default = 120, range = 15..720, category = SLEEP_QUALITY))
        add(IntSetting("commuteBufferMin", "calendar", "Commute buffer (minutes)", helpText = "Add this many minutes on top of the event start as prep time.", default = 30, range = 0..240, category = SLEEP_QUALITY))

        // 10. Motion fallback
        add(BoolSetting("motionFallbackEnabled", "failsafe.motion", "Re-fire if you don't move", helpText = "If you dismiss but never actually get up, the alarm fires again.", default = true, category = DONT_OVERSLEEP))
        add(IntSetting("motionWindowMin", "failsafe.motion", "Wait this long before re-firing", helpText = "If no motion is detected within this window after dismissing.", default = 5, range = 1..60, category = DONT_OVERSLEEP))
        add(FloatSetting("motionThresholdMs2", "failsafe.motion", "Motion sensitivity (m/s²)", helpText = "Lower is more sensitive. 1.5 ≈ shifting in bed counts.", default = 1.5f, rangeStart = 0.1f, rangeEndInclusive = 20f, category = DONT_OVERSLEEP, advanced = true))
        add(IntSetting("fallbackVolumePct", "failsafe.motion", "Fallback volume %", helpText = "How loud the re-fire is.", default = 100, range = 0..100, category = DONT_OVERSLEEP))

        // 11. Shift rotation
        add(JsonSetting("shiftPatternJson", "schedule.shifts", "Shift pattern", helpText = "JSON describing your shift cycle (e.g. 4-on-4-off).", default = "null", category = SLEEP_QUALITY, advanced = true))
        add(StringSetting("shiftAnchorDate", "schedule.shifts", "Shift cycle starts on", helpText = "ISO date the cycle anchors against.", default = "", category = SLEEP_QUALITY, advanced = true))

        // 12. Weather advance
        add(BoolSetting("weatherEnabled", "weather", "Wake earlier in bad weather", helpText = "If rain or freezing temperatures are forecast, the alarm fires earlier so you have extra commute time.", default = false, category = TRAVEL))
        add(EnumSetting("weatherProvider", "weather", "Weather source", helpText = "Where forecast data comes from.", default = "openweather", choices = listOf("openweather", "metofficedatahub", "weatherapi"), category = TRAVEL, advanced = true))
        add(IntSetting("precipitationAdvanceMin", "weather", "Earlier when it rains (minutes)", helpText = "Wake this many minutes earlier when rain is expected.", default = 10, range = 0..60, category = TRAVEL))
        add(IntSetting("freezeAdvanceMin", "weather", "Earlier when freezing (minutes)", helpText = "Wake this many minutes earlier on freezing mornings — defrosting takes time.", default = 15, range = 0..60, category = TRAVEL))
        add(FloatSetting("precipitationThresholdMm", "weather", "Rain threshold (mm)", helpText = "Only count rain heavier than this.", default = 1.0f, rangeStart = 0f, rangeEndInclusive = 20f, category = TRAVEL, advanced = true))
        add(FloatSetting("freezeThresholdC", "weather", "Freeze threshold (°C)", helpText = "Only count temperatures at or below this.", default = 1.0f, rangeStart = -30f, rangeEndInclusive = 10f, category = TRAVEL, advanced = true))

        // 13. Traffic API
        add(BoolSetting("trafficEnabled", "traffic", "Wake earlier in heavy traffic", helpText = "Check live traffic and shift the alarm earlier when there's a delay.", default = false, category = TRAVEL))
        add(EnumSetting("trafficProvider", "traffic", "Traffic provider", helpText = "Which routing service to use.", default = "google", choices = listOf("google", "tomtom", "mapbox", "here"), category = TRAVEL, advanced = true))
        add(FloatSetting("originLat", "traffic", "Home latitude", helpText = "Where your commute starts.", default = 51.5074f, rangeStart = -90f, rangeEndInclusive = 90f, category = TRAVEL, advanced = true))
        add(FloatSetting("originLng", "traffic", "Home longitude", helpText = "Where your commute starts.", default = -0.1278f, rangeStart = -180f, rangeEndInclusive = 180f, category = TRAVEL, advanced = true))
        add(FloatSetting("destLat", "traffic", "Work latitude", helpText = "Where your commute ends.", default = 51.5074f, rangeStart = -90f, rangeEndInclusive = 90f, category = TRAVEL, advanced = true))
        add(FloatSetting("destLng", "traffic", "Work longitude", helpText = "Where your commute ends.", default = -0.1278f, rangeStart = -180f, rangeEndInclusive = 180f, category = TRAVEL, advanced = true))
        add(IntSetting("maxAdvanceMin", "traffic", "Max earlier wake (minutes)", helpText = "Cap the traffic-based advance at this many minutes so you're not woken hours early.", default = 30, range = 0..240, category = TRAVEL))

        // 14. Tasker / smart-home
        add(JsonSetting("intentActionsOnDismiss", "automation", "Run these actions when dismissed", helpText = "JSON list of intents/URLs. Power-user feature.", default = "[]", category = AUTOMATION, advanced = true))
        add(JsonSetting("taskerVariablesJson", "automation", "Tasker variables", helpText = "Variables passed to Tasker when the alarm fires.", default = "{}", category = AUTOMATION, advanced = true))

        // 15. Strict-mode editing lock
        add(BoolSetting("editLockEnabled", "security", "Lock alarm editing", helpText = "Once on, alarms can't be edited again for a while — stops you from sleepy-tampering with tomorrow's wake-up.", default = false, category = SECURITY))
        add(IntSetting("editLockHours", "security", "Lock window (hours)", helpText = "How long alarms stay locked after the lock turns on.", default = 8, range = 1..72, category = SECURITY))

        // 16. Forced screen lockout
        add(BoolSetting("lockoutEnabled", "ringer.lockout", "Block other apps while ringing", helpText = "Take over the screen completely so you have to deal with the alarm before doing anything else.", default = false, category = SECURITY))
        add(JsonSetting("lockoutAllowedActions", "ringer.lockout", "Allowed actions during lockout", helpText = "JSON list, e.g. [\"dismiss\",\"snooze\"].", default = """["dismiss","snooze"]""", category = SECURITY, advanced = true))

        // 17. Custom snooze gestures
        add(JsonSetting("gestureMapJson", "snooze.gesture", "Snooze gestures", helpText = "Map gestures (swipe, double-tap) to snooze actions.", default = "{}", category = SLEEP_QUALITY, advanced = true))

        // 18. Streaming fallback
        add(BoolSetting("streamFallbackEnabled", "audio.fallback", "Fall back if streaming fails", helpText = "If Spotify or a URL won't play, switch to a local backup so the alarm still rings.", default = true, category = SOUND))
        add(IntSetting("streamTimeoutSec", "audio.fallback", "Streaming timeout (seconds)", helpText = "How long to wait before deciding the stream isn't going to start.", default = 10, range = 1..120, category = SOUND))
        add(StringSetting("localFallbackUri", "audio.fallback", "Backup sound (URI)", helpText = "Local file used if streaming fails.", default = "", category = SOUND))
        add(IntSetting("audioFallbackVolumePct", "audio.fallback", "Backup volume %", helpText = "Volume for the local fallback.", default = 100, range = 0..100, category = SOUND))

        // 19. Geofence
        add(FloatSetting("homeLat", "geofence", "Home latitude", helpText = "Used by ‘only ring at home’ rules.", default = 51.5074f, rangeStart = -90f, rangeEndInclusive = 90f, category = TRAVEL, advanced = true))
        add(FloatSetting("homeLng", "geofence", "Home longitude", helpText = "Used by ‘only ring at home’ rules.", default = -0.1278f, rangeStart = -180f, rangeEndInclusive = 180f, category = TRAVEL, advanced = true))
        add(IntSetting("homeRadiusM", "geofence", "Home radius (m)", helpText = "How big the geofence is around home.", default = 200, range = 10..5_000, category = TRAVEL))
        add(EnumSetting("geofenceMode", "geofence", "When I'm not at home", helpText = "Disable = silent. Keep = still rings. Delay = ring later.", default = "disable", choices = listOf("disable", "keep", "delay"), category = TRAVEL))

        // 20. Bedtime penalty
        add(StringSetting("bedtimeStart", "bedtime.penalty", "My bedtime (HH:mm)", helpText = "If you're still on your screen after this, the morning alarm fires earlier.", default = "23:00", pattern = "^[0-2][0-9]:[0-5][0-9]$", category = DONT_OVERSLEEP))
        add(IntSetting("screenOnGraceMin", "bedtime.penalty", "Bedtime grace (minutes)", helpText = "How many minutes past bedtime are tolerated before the penalty kicks in.", default = 30, range = 0..240, category = DONT_OVERSLEEP))
        add(IntSetting("morningAdvanceMin", "bedtime.penalty", "Penalty advance (minutes)", helpText = "How many minutes earlier tomorrow's alarm fires when you broke bedtime.", default = 15, range = 0..120, category = DONT_OVERSLEEP))

        // 21. Low-battery failsafe
        add(BoolSetting("batteryFailsafeEnabled", "failsafe.battery", "Low-battery failsafe", helpText = "If your phone is going to be dead by morning, ring earlier while it still has charge.", default = false, category = DONT_OVERSLEEP))
        add(IntSetting("batteryThresholdPct", "failsafe.battery", "Battery threshold (%)", helpText = "Below this %, fire early.", default = 15, range = 1..100, category = DONT_OVERSLEEP))
        add(BoolSetting("requireUncharged", "failsafe.battery", "Only when not charging", helpText = "Don't trip the failsafe if you've plugged in.", default = true, category = DONT_OVERSLEEP))
        add(IntSetting("earlyFireMin", "failsafe.battery", "How much earlier (minutes)", helpText = "Bring the alarm forward by this many minutes when battery is low.", default = 30, range = 1..240, category = DONT_OVERSLEEP))

        // 22. Sub-alarm chain
        add(JsonSetting("chainOffsetsMin", "schedule.chain", "Chained alarm offsets (minutes)", helpText = "JSON list of follow-on alarms after this one, e.g. [5, 10, 15].", default = "[]", category = SLEEP_QUALITY, advanced = true))
        add(JsonSetting("chainTones", "schedule.chain", "Chained alarm tones", helpText = "JSON list of tone URIs aligned with the offsets above.", default = "[]", category = SLEEP_QUALITY, advanced = true))

        // 23. Pre-motion auto-dismiss
        add(BoolSetting("preMotionEnabled", "failsafe.preMotion", "Auto-dismiss if I'm already up", helpText = "If you've been moving for a while before the alarm, skip ringing and consider it dismissed.", default = false, category = DONT_OVERSLEEP))
        add(IntSetting("preMotionWindowMin", "failsafe.preMotion", "Look back this many minutes", helpText = "How far back to check for sustained motion.", default = 10, range = 1..30, category = DONT_OVERSLEEP))
        add(FloatSetting("preMotionThresholdMs2", "failsafe.preMotion", "Pre-motion sensitivity (m/s²)", helpText = "Higher = needs vigorous movement.", default = 2f, rangeStart = 0.1f, rangeEndInclusive = 20f, category = DONT_OVERSLEEP, advanced = true))

        // 24. ICS skips
        add(JsonSetting("icsSourceUris", "calendar.ics", "ICS calendar URLs", helpText = "JSON list of .ics URLs whose events let the alarm skip a day.", default = "[]", category = SLEEP_QUALITY, advanced = true))
        add(StringSetting("skipMatchingSummariesRegex", "calendar.ics", "Skip events matching (regex)", helpText = "Only skip if event title matches this pattern.", default = "", category = SLEEP_QUALITY, advanced = true))

        // 25. Hardware buttons
        add(BoolSetting("disableVolumeButtons", "ringer.buttons", "Block volume buttons", helpText = "Prevents silencing the alarm by mashing volume keys.", default = false, category = DONT_OVERSLEEP))
        add(BoolSetting("disablePowerButton", "ringer.buttons", "Block power button", helpText = "Prevents silencing by pressing power.", default = false, category = DONT_OVERSLEEP))
        add(BoolSetting("disableLockHomeRecents", "ringer.buttons", "Block home/recents/lock", helpText = "Locks the alarm to the screen while ringing.", default = false, category = DONT_OVERSLEEP))

        // 26. Network toggle
        add(EnumSetting("networkToggleMode", "network", "Toggle network when alarm fires", helpText = "Useful if you sleep in airplane mode but want connectivity restored at wake-up.", default = "none", choices = listOf("none", "wifi", "data", "all"), category = AUTOMATION))
        add(BoolSetting("restoreOnDismiss", "network", "Restore network on dismiss", helpText = "Put network back the way it was once you dismiss.", default = true, category = AUTOMATION))

        // 27. Polyphasic
        add(EnumSetting("polyphasicTemplate", "schedule.polyphasic", "Sleep template", helpText = "Polyphasic sleep templates. Mono = one block at night.", default = "monophasic", choices = listOf("monophasic", "biphasic", "everyman", "uberman", "dymaxion", "custom"), category = SLEEP_QUALITY, advanced = true))
        add(StringSetting("polyphasicAnchorTime", "schedule.polyphasic", "Anchor time", helpText = "When the schedule's anchor sleep starts (HH:mm).", default = "23:00", pattern = "^[0-2][0-9]:[0-5][0-9]$", category = SLEEP_QUALITY, advanced = true))
        add(JsonSetting("templateOverrides", "schedule.polyphasic", "Template overrides", helpText = "JSON: tweaks to the chosen template.", default = "{}", category = SLEEP_QUALITY, advanced = true))

        // 28. Cloud sync
        add(EnumSetting("syncProvider", "sync", "Cloud backup", helpText = "Where to back your alarms up. Drive selection uses Google Picker so the app only sees the file you pick.", default = "drive", choices = listOf("drive", "dropbox", "webdav", "none"), category = BACKUP))
        add(StringSetting("syncEncryptionKeyAlias", "sync", "Encryption key alias", helpText = "Android Keystore alias used to wrap the AES-GCM key.", default = "power-alarm-sync", category = BACKUP, advanced = true))
        add(IntSetting("autoSyncIntervalMin", "sync", "Auto-sync interval (minutes)", helpText = "How often to push changes up.", default = 60, range = 5..1_440, category = BACKUP))

        // 29. Fade-out
        add(IntSetting("fadeOutSec", "audio.fadeOut", "Fade-out (seconds)", helpText = "Smoothly drop the volume when you dismiss instead of cutting hard.", default = 10, range = 0..60, category = SOUND))
        add(EnumSetting("fadeOutCurve", "audio.fadeOut", "Fade-out curve", helpText = "Linear/exponential/log shape for the fade.", default = "linear", choices = listOf("linear", "exponential", "log"), category = SOUND, advanced = true))

        // 30. Cast targets
        add(JsonSetting("castTargets", "cast", "Cast targets", helpText = "Speakers/TVs the alarm can ring through.", default = "[]", category = BACKUP, advanced = true))
        add(IntSetting("castFallbackToLocalSec", "cast", "Cast fallback (seconds)", helpText = "If the cast device doesn't pick up in time, ring the phone instead.", default = 10, range = 1..60, category = BACKUP))
        add(EnumSetting("oneShotPermissionScope", "cast", "Cast LAN scope", helpText = "How long network casting permission lasts.", default = "fire", choices = listOf("fire", "session", "off"), category = BACKUP, advanced = true))

        // ── B. Power-user additions 20 ────────────────────────────────────────────────────────
        // 31. Multi-domain captcha
        add(JsonSetting("problemBank", "dismiss.cognitive", "Problem bank", helpText = "JSON list of all problem topics this app can pull from.", default = """["algebra","integrals","bayesian","sequences","sql","regex"]""", category = MOTIVATION, advanced = true))
        add(BoolSetting("dynamicLLMProblems", "dismiss.cognitive", "Use an LLM for fresh problems", helpText = "Generate fresh problems on the fly using your own LLM endpoint (no data leaves to a third party).", default = false, category = MOTIVATION, advanced = true))
        add(StringSetting("llmEndpoint", "dismiss.cognitive", "LLM endpoint URL", helpText = "Your private LLM API URL.", default = "", category = MOTIVATION, advanced = true))

        // 32. Voice
        add(BoolSetting("voiceEnabled", "dismiss.voice", "Speak a passphrase to dismiss", helpText = "Forces you to vocalise — hard to do half-asleep.", default = false, category = MOTIVATION))
        add(StringSetting("enrolledPhraseHash", "dismiss.voice", "Enrolled phrase (hash)", helpText = "Hash of your enrolled phrase. Set when you enroll.", default = "", category = MOTIVATION, advanced = true))
        add(FloatSetting("voiceMatchConfidence", "dismiss.voice", "Voice match strictness", helpText = "Higher = stricter match required.", default = 0.85f, rangeStart = 0.5f, rangeEndInclusive = 1f, category = MOTIVATION))

        // 33. NFC
        add(BoolSetting("nfcEnabled", "dismiss.nfc", "Tap an NFC tag to dismiss", helpText = "Place an NFC sticker in the bathroom — you have to leave bed to dismiss.", default = false, category = MOTIVATION))
        add(StringSetting("requiredTagUid", "dismiss.nfc", "Required tag UID", helpText = "Set when you scan & enroll the tag.", default = "", category = MOTIVATION, advanced = true))

        // 34. QR
        add(BoolSetting("qrEnabled", "dismiss.qr", "Scan a QR code to dismiss", helpText = "Print a QR code, stick it somewhere annoying.", default = false, category = MOTIVATION))
        add(StringSetting("requiredQrPayloadHash", "dismiss.qr", "Required QR (hash)", helpText = "Hash of the enrolled QR payload.", default = "", category = MOTIVATION, advanced = true))

        // 35. Steps
        add(IntSetting("stepGoal", "dismiss.steps", "Step goal to dismiss", helpText = "Walk this many steps to silence the alarm.", default = 30, range = 1..1_000, category = MOTIVATION))
        add(IntSetting("stepWindowMin", "dismiss.steps", "Step window (minutes)", helpText = "How long you have to hit the goal.", default = 5, range = 1..30, category = MOTIVATION))

        // 36. Selfie
        add(BoolSetting("faceEnabled", "dismiss.face", "Take an eyes-open selfie", helpText = "Front camera verifies your eyes are open.", default = false, category = MOTIVATION))
        add(FloatSetting("eyesOpenThreshold", "dismiss.face", "Eyes-open strictness", helpText = "Higher = needs eyes more clearly open.", default = 0.7f, rangeStart = 0.1f, rangeEndInclusive = 1f, category = MOTIVATION))
        add(IntSetting("attemptsMax", "dismiss.face", "Max attempts", helpText = "How many tries before we give up and call it dismissed.", default = 5, range = 1..30, category = MOTIVATION))

        // 37. Solar anchor
        add(EnumSetting("solarAnchor", "schedule.solar", "Solar anchor", helpText = "Anchor alarm time to a solar event instead of a fixed clock time.", default = "sunrise", choices = listOf("sunrise", "sunset", "civilDawn", "civilDusk", "nauticalDawn", "nauticalDusk", "astronomicalDawn", "astronomicalDusk"), category = SLEEP_QUALITY))
        add(IntSetting("solarOffsetMin", "schedule.solar", "Solar offset (minutes)", helpText = "Negative = before, positive = after the solar event.", default = 0, range = -180..180, category = SLEEP_QUALITY))

        // 38. Adhan
        add(BoolSetting("adhanEnabled", "schedule.adhan", "Adhan (prayer-time) alarms", helpText = "Generate alarms at prayer times using a calculation method and madhab.", default = false, category = SLEEP_QUALITY))
        add(EnumSetting("calculationMethod", "schedule.adhan", "Calculation method", helpText = "Standard adhan calculation methods.", default = "MWL", choices = listOf("MWL", "ISNA", "Egyptian", "UmmAlQura", "Karachi", "Tehran", "Jafari"), category = SLEEP_QUALITY))
        add(EnumSetting("madhab", "schedule.adhan", "Madhab", helpText = "Asr juristic method.", default = "shafi", choices = listOf("shafi", "hanafi"), category = SLEEP_QUALITY))
        add(StringSetting("adhanToneUri", "schedule.adhan", "Adhan tone (URI)", helpText = "Optional custom recitation.", default = "", category = SLEEP_QUALITY, advanced = true))

        // 39. Smart light
        add(EnumSetting("lightProvider", "smartlight", "Smart-light provider", helpText = "Which ecosystem to talk to.", default = "hue", choices = listOf("hue", "lifx", "matter", "tasker"), category = AUTOMATION))
        add(JsonSetting("lightTargets", "smartlight", "Light targets", helpText = "JSON list of light IDs to ramp up.", default = "[]", category = AUTOMATION, advanced = true))
        add(IntSetting("rampStartLeadMin", "smartlight", "Light ramp start (minutes early)", helpText = "Begin warming the lights this many minutes before the alarm.", default = 30, range = 0..120, category = AUTOMATION))
        add(EnumSetting("rampCurve", "smartlight", "Light ramp shape", helpText = "Linear, exponential or log brightness curve.", default = "exponential", choices = listOf("linear", "exponential", "log"), category = AUTOMATION, advanced = true))
        add(IntSetting("targetKelvin", "smartlight", "Final colour temperature (K)", helpText = "2000 = candle. 6500 = daylight.", default = 4_500, range = 2_000..6_500, category = AUTOMATION))

        // 40. Smart plug
        add(JsonSetting("plugTargets", "smartplug", "Smart plug targets", helpText = "JSON list of plug IDs (e.g. coffee machine).", default = "[]", category = AUTOMATION, advanced = true))
        add(IntSetting("plugLeadMin", "smartplug", "Plug start (minutes early)", helpText = "Switch the plug on this many minutes before the alarm.", default = 5, range = 0..120, category = AUTOMATION))
        add(IntSetting("plugAutoOffMin", "smartplug", "Auto-off (minutes)", helpText = "Turn the plug back off after this long.", default = 30, range = 0..240, category = AUTOMATION))

        // 41. DND
        add(EnumSetting("dndModeOnFire", "dnd", "Do Not Disturb when ringing", helpText = "Silence other notifications while the alarm is going off.", default = "priority", choices = listOf("off", "priority", "alarms", "none", "all"), category = AUTOMATION))
        add(JsonSetting("dndAllowList", "dnd", "DND allow list", helpText = "JSON list of contacts who can break through DND.", default = "[]", category = AUTOMATION, advanced = true))

        // 42. AQI
        add(EnumSetting("aqiProvider", "airquality", "Air quality provider", helpText = "Which AQI source to use.", default = "openaq", choices = listOf("openaq", "waqi", "purpleair", "iqair"), category = TRAVEL, advanced = true))
        add(FloatSetting("aqiAdvanceThresholdPm25", "airquality", "PM2.5 threshold (µg/m³)", helpText = "Wake earlier when air quality is worse than this.", default = 35f, rangeStart = 0f, rangeEndInclusive = 500f, category = TRAVEL, advanced = true))
        add(IntSetting("aqiAdvanceMin", "airquality", "Earlier when air is bad (minutes)", helpText = "How much earlier to wake on bad-AQI mornings.", default = 15, range = 0..120, category = TRAVEL))

        // 43. Emergency
        add(BoolSetting("emergencyEnabled", "emergency", "Emergency override", helpText = "Force-fire all alarms if a major nearby earthquake (USGS) is detected.", default = false, category = SECURITY))
        add(FloatSetting("emergencyMagnitudeMin", "emergency", "Min magnitude", helpText = "Only trigger above this earthquake magnitude.", default = 5.0f, rangeStart = 0f, rangeEndInclusive = 10f, category = SECURITY))
        add(IntSetting("emergencyRadiusKm", "emergency", "Radius (km)", helpText = "Only trigger if epicentre within this distance.", default = 200, range = 1..5_000, category = SECURITY))
        add(StringSetting("emergencyToneUri", "emergency", "Emergency tone (URI)", helpText = "Optional override tone for emergency fires.", default = "", category = SECURITY, advanced = true))

        // 44. Profiles
        add(EnumSetting("activeProfile", "profiles", "Active profile", helpText = "Which set of alarms is currently in effect.", default = "default", choices = listOf("default", "work", "holiday", "travel"), category = BACKUP))
        add(JsonSetting("profileDefinitionsJson", "profiles", "Profile definitions", helpText = "JSON of all profiles. Edited via the Profiles screen.", default = "{}", category = BACKUP, advanced = true))

        // 45. Wear OS
        add(BoolSetting("wearCompanionEnabled", "wearable.wear", "Wear OS companion", helpText = "Mirror alarms onto a Wear OS watch + receive haptic dismissal.", default = false, category = BACKUP))
        add(StringSetting("wearAuthToken", "wearable.wear", "Wear auth token", helpText = "Token used by the watch app.", default = "", category = BACKUP, advanced = true))

        // 46. REST API
        add(BoolSetting("apiEnabled", "api", "Local LAN API", helpText = "Run a tiny server on your LAN so you can manage alarms from your laptop.", default = false, category = BACKUP, advanced = true))
        add(IntSetting("apiPort", "api", "API port", helpText = "Network port for the LAN API.", default = 8443, range = 1_024..65_535, category = BACKUP, advanced = true))
        add(StringSetting("apiTokenHash", "api", "API token (hash)", helpText = "Bearer token hash. Set during enrollment.", default = "", category = BACKUP, advanced = true))
        add(EnumSetting("apiBindInterface", "api", "Bind interface", helpText = "Which network interface to listen on.", default = "lan", choices = listOf("lan", "loopback", "all"), category = BACKUP, advanced = true))

        // 47. Driving ETA
        add(FloatSetting("etaDestLat", "traffic.eta", "ETA destination latitude", helpText = "Where you need to arrive.", default = 51.5074f, rangeStart = -90f, rangeEndInclusive = 90f, category = TRAVEL, advanced = true))
        add(FloatSetting("etaDestLng", "traffic.eta", "ETA destination longitude", helpText = "Where you need to arrive.", default = -0.1278f, rangeStart = -180f, rangeEndInclusive = 180f, category = TRAVEL, advanced = true))
        add(StringSetting("etaArriveBy", "traffic.eta", "Arrive by (HH:mm)", helpText = "Target arrival time.", default = "09:00", pattern = "^[0-2][0-9]:[0-5][0-9]$", category = TRAVEL))
        add(IntSetting("etaPrepBufferMin", "traffic.eta", "Get-ready buffer (minutes)", helpText = "Time you need between alarm and leaving the house.", default = 30, range = 0..240, category = TRAVEL))

        // 48. TfL
        add(JsonSetting("tflLines", "traffic.tfl", "TfL lines I depend on", helpText = "JSON list of London Underground lines.", default = """["central","piccadilly","jubilee"]""", category = TRAVEL, advanced = true))
        add(JsonSetting("tflStops", "traffic.tfl", "TfL stops I depend on", helpText = "JSON list of stop IDs.", default = "[]", category = TRAVEL, advanced = true))
        add(IntSetting("tflMaxAdvanceMin", "traffic.tfl", "Earlier when TfL disrupted (minutes)", helpText = "Cap on advance triggered by TfL disruption.", default = 20, range = 0..120, category = TRAVEL))

        // 49. Rotation
        add(JsonSetting("rotationPoolUris", "audio.rotation", "Sound rotation pool", helpText = "JSON list of audio URIs to rotate through so you don't habituate.", default = "[]", category = SOUND, advanced = true))
        add(IntSetting("rotationCooldownDays", "audio.rotation", "Rotation cooldown (days)", helpText = "Don't reuse the same sound for this many days.", default = 7, range = 1..365, category = SOUND))

        // 50. Distress
        add(StringSetting("distressCodeHash", "security.distress", "Distress code (hash)", helpText = "Alternative ‘fake dismiss’ code that silently triggers SOS.", default = "", category = SECURITY, advanced = true))
        add(JsonSetting("sosContacts", "security.distress", "SOS contacts", helpText = "Numbers/emails to alert when distress code used.", default = "[]", category = SECURITY, advanced = true))
        add(StringSetting("sosMessageTemplate", "security.distress", "SOS message template", helpText = "Use {{location}} for a Maps link.", default = "I may be in trouble. Last location: {{location}}", category = SECURITY, advanced = true))
        add(BoolSetting("sosLocationPing", "security.distress", "Include location in SOS", helpText = "Attach current location to the SOS message.", default = true, category = SECURITY))

        // ── Cross-cutting: Theme ──────────────────────────────────────────────────────────────
        add(EnumSetting("defaultPaletteId", "theme", "Theme palette", helpText = "Pick a colour scheme. Custom uses your hex overrides below.", default = "teal_black", choices = listOf("teal_black", "deep_violet", "amber_dusk", "mono", "custom"), category = APPEARANCE))
        add(ColorSetting("primaryHex", "theme", "Primary colour", helpText = "Main accent colour.", default = "#00C2B8", category = APPEARANCE))
        add(ColorSetting("secondaryHex", "theme", "Secondary colour", helpText = "Used for less-prominent buttons/accents.", default = "#0F4C4A", category = APPEARANCE, advanced = true))
        add(ColorSetting("tertiaryHex", "theme", "Tertiary colour", helpText = "Used for highlights and chart accents.", default = "#26A69A", category = APPEARANCE, advanced = true))
        add(ColorSetting("surfaceHex", "theme", "Surface colour", helpText = "Card and panel background.", default = "#000000", category = APPEARANCE))
        add(ColorSetting("backgroundHex", "theme", "Background colour", helpText = "Behind everything.", default = "#000000", category = APPEARANCE))
        add(ColorSetting("errorHex", "theme", "Error colour", helpText = "Used for warnings and destructive actions.", default = "#FF5252", category = APPEARANCE, advanced = true))
        add(ColorSetting("onPrimaryHex", "theme", "Text on primary", helpText = "Text colour on top of primary.", default = "#000000", category = APPEARANCE, advanced = true))
        add(ColorSetting("onSurfaceHex", "theme", "Text on surface", helpText = "Body text colour.", default = "#E0F2F1", category = APPEARANCE))
        add(StringSetting("typographyFamily", "theme", "Typography family", helpText = "Google Font family name. Falls back to bundled Inter if unavailable.", default = "Inter", category = APPEARANCE))
        add(StringSetting("displayFontUri", "theme", "Display font URI", helpText = "Optional custom font for headings.", default = "", category = APPEARANCE, advanced = true))
        add(StringSetting("bodyFontUri", "theme", "Body font URI", helpText = "Optional custom font for body text.", default = "", category = APPEARANCE, advanced = true))
        add(IntSetting("cornerRadiusDp", "theme", "Corner radius (dp)", helpText = "How rounded cards & buttons look.", default = 16, range = 0..64, category = APPEARANCE))
        add(FloatSetting("densityScale", "theme", "Density scale", helpText = "Smaller = tighter; larger = roomier.", default = 1f, rangeStart = 0.75f, rangeEndInclusive = 1.5f, category = APPEARANCE, advanced = true))
        add(IntSetting("motionDurationMs", "theme", "Motion duration (ms)", helpText = "Speed of animations.", default = 300, range = 0..2_000, category = APPEARANCE, advanced = true))
        add(BoolSetting("darkModeAuto", "theme", "Auto dark mode", helpText = "Match the system dark/light setting.", default = true, category = APPEARANCE))
        add(BoolSetting("useDynamicColor", "theme", "Material You dynamic colour", helpText = "Pull colours from your wallpaper (Android 12+).", default = false, category = APPEARANCE))

        // ── Cross-cutting: Ringer layout ──────────────────────────────────────────────────────
        add(EnumSetting("dismissBtnSizeMode", "ringer.layout", "Dismiss button size", helpText = "Random = changes every fire, harder to muscle-memory dismiss.", default = "large", choices = listOf("small", "large", "random"), category = WAKE_UP))
        add(EnumSetting("snoozeBtnSizeMode", "ringer.layout", "Snooze button size", helpText = "Snooze can be smaller to reduce accidental snoozing.", default = "small", choices = listOf("small", "large", "random"), category = WAKE_UP))
        add(BoolSetting("cognitiveLoadEnabled", "ringer.layout", "Randomise wake-up screen layout", helpText = "Buttons appear at different positions/sizes each time so you actually have to look.", default = false, category = WAKE_UP))
        add(FloatSetting("randomXMin", "ringer.layout", "Random X min (fraction)", helpText = "Left bound for randomisation, 0–1.", default = 0.05f, rangeStart = 0f, rangeEndInclusive = 1f, category = WAKE_UP, advanced = true))
        add(FloatSetting("randomXMax", "ringer.layout", "Random X max (fraction)", helpText = "Right bound for randomisation, 0–1.", default = 0.95f, rangeStart = 0f, rangeEndInclusive = 1f, category = WAKE_UP, advanced = true))
        add(FloatSetting("randomYMin", "ringer.layout", "Random Y min (fraction)", helpText = "Top bound for randomisation, 0–1.", default = 0.10f, rangeStart = 0f, rangeEndInclusive = 1f, category = WAKE_UP, advanced = true))
        add(FloatSetting("randomYMax", "ringer.layout", "Random Y max (fraction)", helpText = "Bottom bound for randomisation, 0–1.", default = 0.90f, rangeStart = 0f, rangeEndInclusive = 1f, category = WAKE_UP, advanced = true))
        add(FloatSetting("randomScaleMin", "ringer.layout", "Random scale min", helpText = "Smallest button size during randomisation.", default = 0.6f, rangeStart = 0.3f, rangeEndInclusive = 2f, category = WAKE_UP, advanced = true))
        add(FloatSetting("randomScaleMax", "ringer.layout", "Random scale max", helpText = "Largest button size during randomisation.", default = 1.4f, rangeStart = 0.3f, rangeEndInclusive = 2f, category = WAKE_UP, advanced = true))
        add(BoolSetting("regenOnEachInstantiation", "ringer.layout", "Regenerate layout each fire", helpText = "If off, layout stays consistent within a session.", default = true, category = WAKE_UP, advanced = true))
        add(BoolSetting("accessibilityMode", "ringer.layout", "Accessibility mode (static layout)", helpText = "Disables randomisation. Use this if you have motor or visual difficulties.", default = false, category = APPEARANCE))

        // ── Cross-cutting: Responsive UI ──────────────────────────────────────────────────────
        add(BoolSetting("adaptiveLayoutEnabled", "ui.responsive", "Adaptive layout", helpText = "Automatically reflow for tablets, foldables and Chromebooks.", default = true, category = APPEARANCE))
        add(IntSetting("compactBreakpointDp", "ui.responsive", "Compact breakpoint (dp)", helpText = "Below this width: phone bottom-bar layout.", default = 600, range = 320..1_200, category = APPEARANCE, advanced = true))
        add(IntSetting("mediumBreakpointDp", "ui.responsive", "Medium breakpoint (dp)", helpText = "Above compact, below this: foldable rail layout.", default = 840, range = 600..1_600, category = APPEARANCE, advanced = true))
    }

    private val COUNTRY_CODES = listOf(
        "GB", "US", "CA", "IE", "AU", "NZ", "DE", "FR", "ES", "IT", "NL", "SE", "NO", "DK", "FI",
        "BE", "AT", "CH", "PT", "PL", "CZ", "HU", "GR", "TR", "JP", "KR", "CN", "HK", "SG", "MY",
        "TH", "ID", "PH", "VN", "IN", "PK", "BD", "AE", "SA", "EG", "ZA", "NG", "KE", "MX", "BR",
        "AR", "CL", "CO", "PE", "RU", "UA",
    )
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class RegistryConstant
