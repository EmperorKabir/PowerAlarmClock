package com.poweralarm.core.settings

/**
 * User-facing categorisation. The settings UI groups entries by [SettingsCategory]
 * with friendly section headings, instead of by raw [groupPath].
 */
enum class SettingsCategory(val title: String, val tagline: String, val emoji: String) {
    WAKE_UP("Wake up gently", "Volume ramps, pre-alarm cues, smart light", "🌅"),
    DONT_OVERSLEEP("Don't let me oversleep", "Motion, battery, geofence failsafes", "⛑️"),
    SLEEP_QUALITY("Sleep & schedule", "Recurrence, sleep stages, polyphasic, holidays", "😴"),
    SOUND("Sounds I love", "Spotify, Drive, local rotation, fade-out", "🎵"),
    TRAVEL("Travel & timezones", "Home zone, travel detection, ETA", "✈️"),
    MOTIVATION("Wake-up tasks", "Captcha, voice, NFC, QR, steps, selfie", "🧠"),
    AUTOMATION("Smart triggers", "Tasker, DND, network, smart home", "⚡"),
    BACKUP("Backup & devices", "Cloud sync, REST API, Wear OS, Cast", "☁️"),
    SECURITY("Lock-down", "Edit lock, distress code, lockout", "🔒"),
    APPEARANCE("Look & feel", "Theme, typography, layout density", "🎨"),
    ADVANCED("Advanced", "Power-user knobs and developer hooks", "🛠️"),
    ;

    companion object {
        fun fromGroupPath(group: String): SettingsCategory = when (group.substringBefore('.')) {
            "audio" -> SOUND
            "snooze", "schedule", "calendar", "wearable" -> SLEEP_QUALITY
            "holidays" -> SLEEP_QUALITY
            "geofence", "traffic", "weather", "airquality" -> TRAVEL
            "dismiss" -> MOTIVATION
            "automation", "smartlight", "smartplug", "dnd", "network" -> AUTOMATION
            "sync", "api", "cast", "emergency", "profiles" -> BACKUP
            "security" -> SECURITY
            "theme" -> APPEARANCE
            "ui" -> APPEARANCE
            "ringer" -> WAKE_UP
            "failsafe" -> DONT_OVERSLEEP
            "bedtime" -> DONT_OVERSLEEP
            "onboarding" -> ADVANCED
            else -> ADVANCED
        }
    }
}

/**
 * Authoritative descriptor for any user-facing variable.
 * Adding a new feature variable means adding a [SettingDescriptor] to the registry —
 * never editing UI code. The settings UI is generated from this registry.
 */
sealed class SettingDescriptor<T : Any> {
    abstract val id: String
    abstract val groupPath: String
    abstract val label: String
    abstract val helpText: String
    abstract val default: T
    abstract val featureFlag: String?
    abstract val dependsOn: List<String>
    abstract val category: SettingsCategory
    abstract val advanced: Boolean

    data class BoolSetting(
        override val id: String,
        override val groupPath: String,
        override val label: String,
        override val helpText: String = "",
        override val default: Boolean,
        override val featureFlag: String? = null,
        override val dependsOn: List<String> = emptyList(),
        override val category: SettingsCategory = SettingsCategory.fromGroupPath(groupPath),
        override val advanced: Boolean = false,
    ) : SettingDescriptor<Boolean>()

    data class IntSetting(
        override val id: String,
        override val groupPath: String,
        override val label: String,
        override val helpText: String = "",
        override val default: Int,
        val range: IntRange,
        override val featureFlag: String? = null,
        override val dependsOn: List<String> = emptyList(),
        override val category: SettingsCategory = SettingsCategory.fromGroupPath(groupPath),
        override val advanced: Boolean = false,
    ) : SettingDescriptor<Int>()

    data class FloatSetting(
        override val id: String,
        override val groupPath: String,
        override val label: String,
        override val helpText: String = "",
        override val default: Float,
        val rangeStart: Float,
        val rangeEndInclusive: Float,
        override val featureFlag: String? = null,
        override val dependsOn: List<String> = emptyList(),
        override val category: SettingsCategory = SettingsCategory.fromGroupPath(groupPath),
        override val advanced: Boolean = true,
    ) : SettingDescriptor<Float>()

    data class StringSetting(
        override val id: String,
        override val groupPath: String,
        override val label: String,
        override val helpText: String = "",
        override val default: String,
        val pattern: String? = null,
        override val featureFlag: String? = null,
        override val dependsOn: List<String> = emptyList(),
        override val category: SettingsCategory = SettingsCategory.fromGroupPath(groupPath),
        override val advanced: Boolean = false,
    ) : SettingDescriptor<String>()

    data class ColorSetting(
        override val id: String,
        override val groupPath: String,
        override val label: String,
        override val helpText: String = "",
        override val default: String, // hex `#RRGGBB` or `#AARRGGBB`
        override val featureFlag: String? = null,
        override val dependsOn: List<String> = emptyList(),
        override val category: SettingsCategory = SettingsCategory.fromGroupPath(groupPath),
        override val advanced: Boolean = false,
    ) : SettingDescriptor<String>()

    data class EnumSetting(
        override val id: String,
        override val groupPath: String,
        override val label: String,
        override val helpText: String = "",
        override val default: String,
        val choices: List<String>,
        override val featureFlag: String? = null,
        override val dependsOn: List<String> = emptyList(),
        override val category: SettingsCategory = SettingsCategory.fromGroupPath(groupPath),
        override val advanced: Boolean = false,
    ) : SettingDescriptor<String>()

    data class JsonSetting(
        override val id: String,
        override val groupPath: String,
        override val label: String,
        override val helpText: String = "",
        override val default: String, // JSON-encoded default
        override val featureFlag: String? = null,
        override val dependsOn: List<String> = emptyList(),
        override val category: SettingsCategory = SettingsCategory.fromGroupPath(groupPath),
        override val advanced: Boolean = true,
    ) : SettingDescriptor<String>()
}
