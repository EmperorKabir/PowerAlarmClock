package com.poweralarm.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Alarm(
    val id: Long = 0L,
    val label: String = "",
    val hour: Int,
    val minute: Int,
    val recurrence: Recurrence = Recurrence.Once,
    val profileId: String = "default",
    val enabled: Boolean = true,
    val conditions: List<Condition> = emptyList(),
    val dismissalRequirements: List<DismissalRequirement> = emptyList(),
    val audioPlan: AudioPlan = AudioPlan(),
    val snoozePolicy: SnoozePolicy = SnoozePolicy(),
    val ringerLayout: RingerLayoutPolicy = RingerLayoutPolicy(),
    val automation: AutomationHooks = AutomationHooks(),
    val metadataJson: String = "{}",
)

@Serializable
sealed class Recurrence {
    @Serializable data object Once : Recurrence()
    @Serializable data object Daily : Recurrence()
    @Serializable data class Weekly(val daysOfWeek: Set<Int>) : Recurrence()
    @Serializable data class ShiftPattern(val patternJson: String, val anchorIso: String) : Recurrence()
    @Serializable data class Polyphasic(val templateId: String, val anchorTime: String) : Recurrence()
    @Serializable data class SolarAnchored(val anchor: String, val offsetMin: Int) : Recurrence()
    @Serializable data class Adhan(val prayer: String, val method: String, val madhab: String) : Recurrence()
    @Serializable data class Chained(val parentId: Long, val offsetMin: Int) : Recurrence()
    @Serializable data class Cron(val expr: String) : Recurrence()
}

@Serializable
sealed class Condition {
    @Serializable data class HolidaySkip(val regionTag: String, val provider: String) : Condition()
    @Serializable data class DateRangeDisable(val startIso: String, val endIso: String) : Condition()
    @Serializable data class TempSuspension(val skipNext: Int) : Condition()
    @Serializable data class CalendarShift(val calendarIds: List<String>, val lookaheadMin: Int, val bufferMin: Int) : Condition()
    @Serializable data class WeatherAdvance(
        val provider: String,
        val precipMin: Int,
        val freezeMin: Int,
        val precipThresholdMm: Float,
        val freezeThresholdC: Float,
    ) : Condition()
    @Serializable data class TrafficAdvance(
        val provider: String,
        val originLat: Float,
        val originLng: Float,
        val destLat: Float,
        val destLng: Float,
        val maxAdvanceMin: Int,
    ) : Condition()
    @Serializable data class AqiAdvance(val provider: String, val pm25Threshold: Float, val advanceMin: Int) : Condition()
    @Serializable data class TflDisruption(val lines: List<String>, val stops: List<String>, val maxAdvanceMin: Int) : Condition()
    @Serializable data class IcsSkip(val sourceUris: List<String>, val summaryRegex: String) : Condition()
    @Serializable data class Geofence(val homeLat: Float, val homeLng: Float, val radiusM: Int, val mode: String) : Condition()
    @Serializable data class BedtimePenalty(val bedtime: String, val graceMin: Int, val advanceMin: Int) : Condition()
    @Serializable data class LowBatteryFailsafe(val thresholdPct: Int, val requireUncharged: Boolean, val earlyFireMin: Int) : Condition()
    @Serializable data class EmergencyOverride(val magnitudeMin: Float, val radiusKm: Int) : Condition()
}

@Serializable
sealed class DismissalRequirement {
    @Serializable data object TapButton : DismissalRequirement()
    @Serializable data class Cognitive(val domains: List<String>, val difficulty: String, val requiredCorrect: Int) : DismissalRequirement()
    @Serializable data class Voice(val phraseHash: String, val matchConfidence: Float) : DismissalRequirement()
    @Serializable data class Nfc(val tagUid: String) : DismissalRequirement()
    @Serializable data class Qr(val payloadHash: String) : DismissalRequirement()
    @Serializable data class Steps(val goal: Int, val windowMin: Int) : DismissalRequirement()
    @Serializable data class EyesOpenSelfie(val threshold: Float, val attemptsMax: Int) : DismissalRequirement()
    @Serializable data class MotionSustained(val windowSec: Int, val thresholdMs2: Float) : DismissalRequirement()
    @Serializable data class Distress(val altCodeHash: String) : DismissalRequirement()
}

@Serializable
data class AudioPlan(
    val source: AudioSource = AudioSource.SystemDefault,
    val preAlarm: PreAlarmCue? = null,
    val volumeCurve: String = "linear",
    val volumeRampMin: Int = 5,
    val volumeStartPct: Int = 10,
    val volumeEndPct: Int = 100,
    val fadeOutSec: Int = 10,
    val fadeOutCurve: String = "linear",
    val streamFallbackEnabled: Boolean = true,
    val streamTimeoutSec: Int = 10,
    val localFallbackUri: String = "",
    val rotationPoolId: String? = null,
    val castTargetIds: List<String> = emptyList(),
)

@Serializable
sealed class AudioSource {
    @Serializable data object SystemDefault : AudioSource()
    @Serializable data class Local(val uri: String) : AudioSource()
    @Serializable data class Url(val url: String) : AudioSource()
    @Serializable data class SpotifyTrack(val uri: String) : AudioSource()
    @Serializable data class SpotifyPlaylist(val uri: String) : AudioSource()
    @Serializable data class SpotifyPodcast(val uri: String) : AudioSource()
    @Serializable data class DriveFile(val fileId: String, val mimeType: String) : AudioSource()
}

@Serializable
data class PreAlarmCue(val leadMin: Int, val volumePct: Int, val sourceUri: String)

@Serializable
data class SnoozePolicy(
    val baseMin: Int = 9,
    val decrementMin: Int = 1,
    val floorMin: Int = 1,
    val maxCount: Int = 5,
    val gestureMapJson: String = "{}",
)

@Serializable
data class RingerLayoutPolicy(
    val dismissSize: String = "large",
    val snoozeSize: String = "small",
    val cognitiveLoadRandomized: Boolean = false,
    val randomXMin: Float = 0.05f,
    val randomXMax: Float = 0.95f,
    val randomYMin: Float = 0.10f,
    val randomYMax: Float = 0.90f,
    val randomScaleMin: Float = 0.6f,
    val randomScaleMax: Float = 1.4f,
    val regenOnEachInstantiation: Boolean = true,
    val accessibilityMode: Boolean = false,
)

@Serializable
data class AutomationHooks(
    val onFireIntents: List<String> = emptyList(),
    val onDismissIntents: List<String> = emptyList(),
    val dndModeOnFire: String = "off",
    val networkToggleMode: String = "none",
    val restoreOnDismiss: Boolean = true,
    val smartLightTargetIds: List<String> = emptyList(),
    val smartPlugTargetIds: List<String> = emptyList(),
    val rampStartLeadMin: Int = 30,
)
