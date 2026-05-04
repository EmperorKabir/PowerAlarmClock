@file:Suppress("LargeClass", "LongMethod", "MaxLineLength")

package com.poweralarm.core.settings

import com.poweralarm.core.settings.SettingDescriptor.BoolSetting
import com.poweralarm.core.settings.SettingDescriptor.ColorSetting
import com.poweralarm.core.settings.SettingDescriptor.EnumSetting
import com.poweralarm.core.settings.SettingDescriptor.FloatSetting
import com.poweralarm.core.settings.SettingDescriptor.IntSetting
import com.poweralarm.core.settings.SettingDescriptor.JsonSetting
import com.poweralarm.core.settings.SettingDescriptor.StringSetting

/**
 * Seeds [InMemorySettingsRegistry] with every variable enumerated in FEATURES.md.
 * Adding a new feature variable means adding one line here — never editing settings UI.
 */
@RegistryConstant
object RegistrySeed {

    fun build(): InMemorySettingsRegistry = InMemorySettingsRegistry(allDescriptors())

    @Suppress("LongMethod")
    fun allDescriptors(): List<SettingDescriptor<*>> = buildList {
        // ── A. Mandated 30 ────────────────────────────────────────────────────────────────────
        // 1. Region holiday skipping
        add(EnumSetting("regionTag", "holidays", "Region", default = "GB", choices = COUNTRY_CODES))
        add(EnumSetting("holidayProvider", "holidays", "Provider", default = "nager", choices = listOf("nager", "calendarific", "ical")))
        add(BoolSetting("skipBankHolidays", "holidays", "Skip bank holidays", default = true))
        add(BoolSetting("skipPublicHolidays", "holidays", "Skip public holidays", default = true))
        add(StringSetting("customHolidayCalendarUri", "holidays", "Custom holiday calendar URI", default = ""))

        // 2. Date-range disable
        add(JsonSetting("disabledRanges", "schedule", "Disabled date ranges", default = "[]"))

        // 3. Temporary suspension
        add(IntSetting("skipNextOccurrences", "schedule", "Skip next N occurrences", default = 0, range = 0..365))

        // 4. Snooze dynamics
        add(IntSetting("snoozeBaseMinutes", "snooze", "Base snooze minutes", default = 9, range = 1..60))
        add(IntSetting("snoozeDecrementMinutes", "snooze", "Decrement per snooze", default = 1, range = 0..30))
        add(IntSetting("snoozeFloorMinutes", "snooze", "Floor minutes", default = 1, range = 1..30))
        add(IntSetting("snoozeMaxCount", "snooze", "Max snooze count", default = 5, range = 0..50))

        // 5. Fitbit light-sleep wake
        add(BoolSetting("fitbitEnabled", "wearable", "Use Fitbit sleep stages", default = false, featureFlag = "wearable.fitbit"))
        add(IntSetting("wakeWindowMin", "wearable", "Wake window (min)", default = 30, range = 5..90))
        add(EnumSetting("lightSleepProvider", "wearable", "Provider", default = "fitbit", choices = listOf("fitbit", "samsung", "garmin", "googlefit")))

        // 6. Cognitive captcha
        add(BoolSetting("cognitiveEnabled", "dismiss.cognitive", "Cognitive captcha required", default = false))
        add(JsonSetting("problemDomains", "dismiss.cognitive", "Domains", default = """["probability","statistics","logic"]"""))
        add(EnumSetting("difficulty", "dismiss.cognitive", "Difficulty", default = "medium", choices = listOf("easy", "medium", "hard", "evil")))
        add(IntSetting("requiredCorrect", "dismiss.cognitive", "Problems to solve", default = 1, range = 1..20))
        add(IntSetting("failurePenaltyAddSnooze", "dismiss.cognitive", "Failure adds snooze (min)", default = 0, range = 0..30))

        // 7. Progressive volume
        add(EnumSetting("volumeCurve", "audio.volume", "Volume curve", default = "linear", choices = listOf("linear", "exponential", "log", "stepped", "custom")))
        add(IntSetting("volumeRampMin", "audio.volume", "Ramp duration (min)", default = 5, range = 0..60))
        add(IntSetting("volumeStartPct", "audio.volume", "Start volume %", default = 10, range = 0..100))
        add(IntSetting("volumeEndPct", "audio.volume", "End volume %", default = 100, range = 0..100))
        add(JsonSetting("volumeCurvePoints", "audio.volume", "Custom curve points", default = "[]"))

        // 8. Pre-alarm gentle wake
        add(BoolSetting("preAlarmEnabled", "audio.preAlarm", "Pre-alarm cue", default = false))
        add(IntSetting("preAlarmLeadMin", "audio.preAlarm", "Lead time (min)", default = 20, range = 1..60))
        add(IntSetting("preAlarmVolumePct", "audio.preAlarm", "Volume %", default = 8, range = 1..50))
        add(StringSetting("preAlarmSourceUri", "audio.preAlarm", "Source URI", default = ""))

        // 9. Calendar shifts
        add(BoolSetting("calendarShiftEnabled", "calendar", "Shift for early events", default = false))
        add(JsonSetting("calendarIds", "calendar", "Calendar IDs", default = "[]"))
        add(IntSetting("eventLookaheadMin", "calendar", "Event lookahead (min)", default = 120, range = 15..720))
        add(IntSetting("commuteBufferMin", "calendar", "Commute buffer (min)", default = 30, range = 0..240))

        // 10. Motion fallback
        add(BoolSetting("motionFallbackEnabled", "failsafe.motion", "Re-fire if no motion", default = true))
        add(IntSetting("motionWindowMin", "failsafe.motion", "Window after dismiss (min)", default = 5, range = 1..60))
        add(FloatSetting("motionThresholdMs2", "failsafe.motion", "Threshold (m/s²)", default = 1.5f, rangeStart = 0.1f, rangeEndInclusive = 20f))
        add(IntSetting("fallbackVolumePct", "failsafe.motion", "Fallback volume %", default = 100, range = 0..100))

        // 11. Shift rotation
        add(JsonSetting("shiftPatternJson", "schedule.shifts", "Shift pattern JSON", default = "null"))
        add(StringSetting("shiftAnchorDate", "schedule.shifts", "Anchor date (ISO)", default = ""))

        // 12. Weather advance
        add(BoolSetting("weatherEnabled", "weather", "Advance for weather", default = false))
        add(EnumSetting("weatherProvider", "weather", "Provider", default = "openweather", choices = listOf("openweather", "metofficedatahub", "weatherapi")))
        add(IntSetting("precipitationAdvanceMin", "weather", "Precipitation advance (min)", default = 10, range = 0..60))
        add(IntSetting("freezeAdvanceMin", "weather", "Freeze advance (min)", default = 15, range = 0..60))
        add(FloatSetting("precipitationThresholdMm", "weather", "Precip threshold (mm)", default = 1.0f, rangeStart = 0f, rangeEndInclusive = 20f))
        add(FloatSetting("freezeThresholdC", "weather", "Freeze threshold (°C)", default = 1.0f, rangeStart = -30f, rangeEndInclusive = 10f))

        // 13. Traffic API
        add(BoolSetting("trafficEnabled", "traffic", "Advance for traffic", default = false))
        add(EnumSetting("trafficProvider", "traffic", "Provider", default = "google", choices = listOf("google", "tomtom", "mapbox", "here")))
        add(FloatSetting("originLat", "traffic", "Origin lat", default = 51.5074f, rangeStart = -90f, rangeEndInclusive = 90f))
        add(FloatSetting("originLng", "traffic", "Origin lng", default = -0.1278f, rangeStart = -180f, rangeEndInclusive = 180f))
        add(FloatSetting("destLat", "traffic", "Destination lat", default = 51.5074f, rangeStart = -90f, rangeEndInclusive = 90f))
        add(FloatSetting("destLng", "traffic", "Destination lng", default = -0.1278f, rangeStart = -180f, rangeEndInclusive = 180f))
        add(IntSetting("maxAdvanceMin", "traffic", "Max advance (min)", default = 30, range = 0..240))

        // 14. Tasker / smart-home
        add(JsonSetting("intentActionsOnDismiss", "automation", "Intents on dismiss", default = "[]"))
        add(JsonSetting("taskerVariablesJson", "automation", "Tasker variables", default = "{}"))

        // 15. Strict-mode editing lock
        add(BoolSetting("editLockEnabled", "security", "Edit lock", default = false))
        add(IntSetting("editLockHours", "security", "Lock window (hours)", default = 8, range = 1..72))

        // 16. Forced screen lockout
        add(BoolSetting("lockoutEnabled", "ringer.lockout", "Forced lockout", default = false))
        add(JsonSetting("lockoutAllowedActions", "ringer.lockout", "Allowed actions", default = """["dismiss","snooze"]"""))

        // 17. Custom snooze gestures
        add(JsonSetting("gestureMapJson", "snooze.gesture", "Gesture map", default = "{}"))

        // 18. Streaming fallback
        add(BoolSetting("streamFallbackEnabled", "audio.fallback", "Fallback on stream fail", default = true))
        add(IntSetting("streamTimeoutSec", "audio.fallback", "Stream timeout (sec)", default = 10, range = 1..120))
        add(StringSetting("localFallbackUri", "audio.fallback", "Local fallback URI", default = ""))
        add(IntSetting("audioFallbackVolumePct", "audio.fallback", "Fallback volume %", default = 100, range = 0..100))

        // 19. Geofence
        add(FloatSetting("homeLat", "geofence", "Home lat", default = 51.5074f, rangeStart = -90f, rangeEndInclusive = 90f))
        add(FloatSetting("homeLng", "geofence", "Home lng", default = -0.1278f, rangeStart = -180f, rangeEndInclusive = 180f))
        add(IntSetting("homeRadiusM", "geofence", "Home radius (m)", default = 200, range = 10..5_000))
        add(EnumSetting("geofenceMode", "geofence", "Mode when away", default = "disable", choices = listOf("disable", "keep", "delay")))

        // 20. Bedtime penalty
        add(StringSetting("bedtimeStart", "bedtime.penalty", "Bedtime (HH:mm)", default = "23:00", pattern = "^[0-2][0-9]:[0-5][0-9]$"))
        add(IntSetting("screenOnGraceMin", "bedtime.penalty", "Screen-on grace (min)", default = 30, range = 0..240))
        add(IntSetting("morningAdvanceMin", "bedtime.penalty", "Advance morning by (min)", default = 15, range = 0..120))

        // 21. Low-battery failsafe
        add(BoolSetting("batteryFailsafeEnabled", "failsafe.battery", "Low-battery failsafe", default = false))
        add(IntSetting("batteryThresholdPct", "failsafe.battery", "Threshold %", default = 15, range = 1..100))
        add(BoolSetting("requireUncharged", "failsafe.battery", "Only when not charging", default = true))
        add(IntSetting("earlyFireMin", "failsafe.battery", "Early fire (min)", default = 30, range = 1..240))

        // 22. Sub-alarm chain
        add(JsonSetting("chainOffsetsMin", "schedule.chain", "Chain offsets (min)", default = "[]"))
        add(JsonSetting("chainTones", "schedule.chain", "Chain tone URIs", default = "[]"))

        // 23. Pre-motion auto-dismiss
        add(BoolSetting("preMotionEnabled", "failsafe.preMotion", "Auto-dismiss on sustained motion", default = false))
        add(IntSetting("preMotionWindowMin", "failsafe.preMotion", "Pre-fire window (min)", default = 10, range = 1..30))
        add(FloatSetting("preMotionThresholdMs2", "failsafe.preMotion", "Threshold (m/s²)", default = 2f, rangeStart = 0.1f, rangeEndInclusive = 20f))

        // 24. ICS skips
        add(JsonSetting("icsSourceUris", "calendar.ics", "ICS source URIs", default = "[]"))
        add(StringSetting("skipMatchingSummariesRegex", "calendar.ics", "Skip-summary regex", default = ""))

        // 25. Hardware buttons
        add(BoolSetting("disableVolumeButtons", "ringer.buttons", "Disable volume buttons", default = false))
        add(BoolSetting("disablePowerButton", "ringer.buttons", "Disable power button", default = false))
        add(BoolSetting("disableLockHomeRecents", "ringer.buttons", "Block lock/home/recents", default = false))

        // 26. Network toggle
        add(EnumSetting("networkToggleMode", "network", "Network toggle on fire", default = "none", choices = listOf("none", "wifi", "data", "all")))
        add(BoolSetting("restoreOnDismiss", "network", "Restore on dismiss", default = true))

        // 27. Polyphasic
        add(EnumSetting("polyphasicTemplate", "schedule.polyphasic", "Template", default = "monophasic", choices = listOf("monophasic", "biphasic", "everyman", "uberman", "dymaxion", "custom")))
        add(StringSetting("polyphasicAnchorTime", "schedule.polyphasic", "Anchor time (HH:mm)", default = "23:00", pattern = "^[0-2][0-9]:[0-5][0-9]$"))
        add(JsonSetting("templateOverrides", "schedule.polyphasic", "Template overrides", default = "{}"))

        // 28. Cloud sync
        add(EnumSetting("syncProvider", "sync", "Provider", default = "drive", choices = listOf("drive", "dropbox", "webdav", "none")))
        add(StringSetting("syncEncryptionKeyAlias", "sync", "Keystore alias", default = "power-alarm-sync"))
        add(IntSetting("autoSyncIntervalMin", "sync", "Auto-sync interval (min)", default = 60, range = 5..1_440))

        // 29. Fade-out
        add(IntSetting("fadeOutSec", "audio.fadeOut", "Fade-out (sec)", default = 10, range = 0..60))
        add(EnumSetting("fadeOutCurve", "audio.fadeOut", "Curve", default = "linear", choices = listOf("linear", "exponential", "log")))

        // 30. Cast targets
        add(JsonSetting("castTargets", "cast", "Cast targets", default = "[]"))
        add(IntSetting("castFallbackToLocalSec", "cast", "Fallback to local (sec)", default = 10, range = 1..60))
        add(EnumSetting("oneShotPermissionScope", "cast", "One-shot LAN scope", default = "fire", choices = listOf("fire", "session", "off")))

        // ── B. Power-user additions 20 ────────────────────────────────────────────────────────
        // 31. Multi-domain captcha
        add(JsonSetting("problemBank", "dismiss.cognitive", "Problem bank", default = """["algebra","integrals","bayesian","sequences","sql","regex"]"""))
        add(BoolSetting("dynamicLLMProblems", "dismiss.cognitive", "LLM-generated", default = false))
        add(StringSetting("llmEndpoint", "dismiss.cognitive", "LLM endpoint URL", default = ""))

        // 32. Voice
        add(BoolSetting("voiceEnabled", "dismiss.voice", "Voice passphrase", default = false))
        add(StringSetting("enrolledPhraseHash", "dismiss.voice", "Enrolled phrase hash", default = ""))
        add(FloatSetting("voiceMatchConfidence", "dismiss.voice", "Match confidence", default = 0.85f, rangeStart = 0.5f, rangeEndInclusive = 1f))

        // 33. NFC
        add(BoolSetting("nfcEnabled", "dismiss.nfc", "NFC tag dismissal", default = false))
        add(StringSetting("requiredTagUid", "dismiss.nfc", "Required tag UID", default = ""))

        // 34. QR
        add(BoolSetting("qrEnabled", "dismiss.qr", "QR scan dismissal", default = false))
        add(StringSetting("requiredQrPayloadHash", "dismiss.qr", "Required QR payload hash", default = ""))

        // 35. Steps
        add(IntSetting("stepGoal", "dismiss.steps", "Step goal", default = 30, range = 1..1_000))
        add(IntSetting("stepWindowMin", "dismiss.steps", "Step window (min)", default = 5, range = 1..30))

        // 36. Selfie
        add(BoolSetting("faceEnabled", "dismiss.face", "Eyes-open selfie", default = false))
        add(FloatSetting("eyesOpenThreshold", "dismiss.face", "Eyes-open threshold", default = 0.7f, rangeStart = 0.1f, rangeEndInclusive = 1f))
        add(IntSetting("attemptsMax", "dismiss.face", "Max attempts", default = 5, range = 1..30))

        // 37. Solar anchor
        add(EnumSetting("solarAnchor", "schedule.solar", "Anchor", default = "sunrise", choices = listOf("sunrise", "sunset", "civilDawn", "civilDusk", "nauticalDawn", "nauticalDusk", "astronomicalDawn", "astronomicalDusk")))
        add(IntSetting("solarOffsetMin", "schedule.solar", "Offset (min)", default = 0, range = -180..180))

        // 38. Adhan
        add(BoolSetting("adhanEnabled", "schedule.adhan", "Adhan alarms", default = false))
        add(EnumSetting("calculationMethod", "schedule.adhan", "Method", default = "MWL", choices = listOf("MWL", "ISNA", "Egyptian", "UmmAlQura", "Karachi", "Tehran", "Jafari")))
        add(EnumSetting("madhab", "schedule.adhan", "Madhab", default = "shafi", choices = listOf("shafi", "hanafi")))
        add(StringSetting("adhanToneUri", "schedule.adhan", "Adhan tone URI", default = ""))

        // 39. Smart light
        add(EnumSetting("lightProvider", "smartlight", "Provider", default = "hue", choices = listOf("hue", "lifx", "matter", "tasker")))
        add(JsonSetting("lightTargets", "smartlight", "Light targets", default = "[]"))
        add(IntSetting("rampStartLeadMin", "smartlight", "Ramp start lead (min)", default = 30, range = 0..120))
        add(EnumSetting("rampCurve", "smartlight", "Ramp curve", default = "exponential", choices = listOf("linear", "exponential", "log")))
        add(IntSetting("targetKelvin", "smartlight", "Target Kelvin", default = 4_500, range = 2_000..6_500))

        // 40. Smart plug
        add(JsonSetting("plugTargets", "smartplug", "Plug targets", default = "[]"))
        add(IntSetting("plugLeadMin", "smartplug", "Lead (min)", default = 5, range = 0..120))
        add(IntSetting("plugAutoOffMin", "smartplug", "Auto-off (min)", default = 30, range = 0..240))

        // 41. DND
        add(EnumSetting("dndModeOnFire", "dnd", "DND on fire", default = "priority", choices = listOf("off", "priority", "alarms", "none", "all")))
        add(JsonSetting("dndAllowList", "dnd", "DND allow list", default = "[]"))

        // 42. AQI
        add(EnumSetting("aqiProvider", "airquality", "Provider", default = "openaq", choices = listOf("openaq", "waqi", "purpleair", "iqair")))
        add(FloatSetting("aqiAdvanceThresholdPm25", "airquality", "PM2.5 advance threshold", default = 35f, rangeStart = 0f, rangeEndInclusive = 500f))
        add(IntSetting("aqiAdvanceMin", "airquality", "Advance (min)", default = 15, range = 0..120))

        // 43. Emergency
        add(BoolSetting("emergencyEnabled", "emergency", "Emergency override", default = false))
        add(FloatSetting("emergencyMagnitudeMin", "emergency", "Min magnitude", default = 5.0f, rangeStart = 0f, rangeEndInclusive = 10f))
        add(IntSetting("emergencyRadiusKm", "emergency", "Radius (km)", default = 200, range = 1..5_000))
        add(StringSetting("emergencyToneUri", "emergency", "Tone URI", default = ""))

        // 44. Profiles
        add(EnumSetting("activeProfile", "profiles", "Active profile", default = "default", choices = listOf("default", "work", "holiday", "travel")))
        add(JsonSetting("profileDefinitionsJson", "profiles", "Profile definitions", default = "{}"))

        // 45. Wear OS
        add(BoolSetting("wearCompanionEnabled", "wearable.wear", "Wear OS companion", default = false))
        add(StringSetting("wearAuthToken", "wearable.wear", "Wear auth token", default = ""))

        // 46. REST API
        add(BoolSetting("apiEnabled", "api", "Local LAN API", default = false))
        add(IntSetting("apiPort", "api", "Port", default = 8443, range = 1_024..65_535))
        add(StringSetting("apiTokenHash", "api", "Bearer token hash", default = ""))
        add(EnumSetting("apiBindInterface", "api", "Bind interface", default = "lan", choices = listOf("lan", "loopback", "all")))

        // 47. Driving ETA
        add(FloatSetting("etaDestLat", "traffic.eta", "ETA dest lat", default = 51.5074f, rangeStart = -90f, rangeEndInclusive = 90f))
        add(FloatSetting("etaDestLng", "traffic.eta", "ETA dest lng", default = -0.1278f, rangeStart = -180f, rangeEndInclusive = 180f))
        add(StringSetting("etaArriveBy", "traffic.eta", "Arrive by (HH:mm)", default = "09:00", pattern = "^[0-2][0-9]:[0-5][0-9]$"))
        add(IntSetting("etaPrepBufferMin", "traffic.eta", "Prep buffer (min)", default = 30, range = 0..240))

        // 48. TfL
        add(JsonSetting("tflLines", "traffic.tfl", "TfL lines", default = """["central","piccadilly","jubilee"]"""))
        add(JsonSetting("tflStops", "traffic.tfl", "TfL stops", default = "[]"))
        add(IntSetting("tflMaxAdvanceMin", "traffic.tfl", "Max advance (min)", default = 20, range = 0..120))

        // 49. Rotation
        add(JsonSetting("rotationPoolUris", "audio.rotation", "Rotation pool URIs", default = "[]"))
        add(IntSetting("rotationCooldownDays", "audio.rotation", "Cooldown (days)", default = 7, range = 1..365))

        // 50. Distress
        add(StringSetting("distressCodeHash", "security.distress", "Distress code hash", default = ""))
        add(JsonSetting("sosContacts", "security.distress", "SOS contacts", default = "[]"))
        add(StringSetting("sosMessageTemplate", "security.distress", "SOS message", default = "I may be in trouble. Last location: {{location}}"))
        add(BoolSetting("sosLocationPing", "security.distress", "Include location ping", default = true))

        // ── Cross-cutting: Theme ──────────────────────────────────────────────────────────────
        add(EnumSetting("defaultPaletteId", "theme", "Default palette", default = "teal_black", choices = listOf("teal_black", "deep_violet", "amber_dusk", "mono", "custom")))
        add(ColorSetting("primaryHex", "theme", "Primary", default = "#00C2B8"))
        add(ColorSetting("secondaryHex", "theme", "Secondary", default = "#0F4C4A"))
        add(ColorSetting("tertiaryHex", "theme", "Tertiary", default = "#26A69A"))
        add(ColorSetting("surfaceHex", "theme", "Surface", default = "#000000"))
        add(ColorSetting("backgroundHex", "theme", "Background", default = "#000000"))
        add(ColorSetting("errorHex", "theme", "Error", default = "#FF5252"))
        add(ColorSetting("onPrimaryHex", "theme", "On-primary", default = "#000000"))
        add(ColorSetting("onSurfaceHex", "theme", "On-surface", default = "#E0F2F1"))
        add(StringSetting("typographyFamily", "theme", "Typography family", default = "Inter"))
        add(StringSetting("displayFontUri", "theme", "Display font URI", default = ""))
        add(StringSetting("bodyFontUri", "theme", "Body font URI", default = ""))
        add(IntSetting("cornerRadiusDp", "theme", "Corner radius (dp)", default = 16, range = 0..64))
        add(FloatSetting("densityScale", "theme", "Density scale", default = 1f, rangeStart = 0.75f, rangeEndInclusive = 1.5f))
        add(IntSetting("motionDurationMs", "theme", "Motion duration (ms)", default = 300, range = 0..2_000))
        add(BoolSetting("darkModeAuto", "theme", "Auto dark mode", default = true))
        add(BoolSetting("useDynamicColor", "theme", "Material You dynamic color", default = false))

        // ── Cross-cutting: Ringer layout ──────────────────────────────────────────────────────
        add(EnumSetting("dismissBtnSizeMode", "ringer.layout", "Dismiss button size", default = "large", choices = listOf("small", "large", "random")))
        add(EnumSetting("snoozeBtnSizeMode", "ringer.layout", "Snooze button size", default = "small", choices = listOf("small", "large", "random")))
        add(BoolSetting("cognitiveLoadEnabled", "ringer.layout", "Cognitive-load randomized layout", default = false))
        add(FloatSetting("randomXMin", "ringer.layout", "Random X min frac", default = 0.05f, rangeStart = 0f, rangeEndInclusive = 1f))
        add(FloatSetting("randomXMax", "ringer.layout", "Random X max frac", default = 0.95f, rangeStart = 0f, rangeEndInclusive = 1f))
        add(FloatSetting("randomYMin", "ringer.layout", "Random Y min frac", default = 0.10f, rangeStart = 0f, rangeEndInclusive = 1f))
        add(FloatSetting("randomYMax", "ringer.layout", "Random Y max frac", default = 0.90f, rangeStart = 0f, rangeEndInclusive = 1f))
        add(FloatSetting("randomScaleMin", "ringer.layout", "Random scale min", default = 0.6f, rangeStart = 0.3f, rangeEndInclusive = 2f))
        add(FloatSetting("randomScaleMax", "ringer.layout", "Random scale max", default = 1.4f, rangeStart = 0.3f, rangeEndInclusive = 2f))
        add(BoolSetting("regenOnEachInstantiation", "ringer.layout", "Regenerate on each instantiation", default = true))
        add(BoolSetting("accessibilityMode", "ringer.layout", "Accessibility static layout", default = false))

        // ── Cross-cutting: Responsive UI ──────────────────────────────────────────────────────
        add(BoolSetting("adaptiveLayoutEnabled", "ui.responsive", "Adaptive layout", default = true))
        add(IntSetting("compactBreakpointDp", "ui.responsive", "Compact breakpoint (dp)", default = 600, range = 320..1_200))
        add(IntSetting("mediumBreakpointDp", "ui.responsive", "Medium breakpoint (dp)", default = 840, range = 600..1_600))
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
