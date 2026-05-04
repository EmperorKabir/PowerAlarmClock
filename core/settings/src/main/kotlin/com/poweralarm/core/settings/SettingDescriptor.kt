package com.poweralarm.core.settings

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

    data class BoolSetting(
        override val id: String,
        override val groupPath: String,
        override val label: String,
        override val helpText: String = "",
        override val default: Boolean,
        override val featureFlag: String? = null,
        override val dependsOn: List<String> = emptyList(),
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
    ) : SettingDescriptor<String>()

    data class ColorSetting(
        override val id: String,
        override val groupPath: String,
        override val label: String,
        override val helpText: String = "",
        override val default: String, // hex `#RRGGBB` or `#AARRGGBB`
        override val featureFlag: String? = null,
        override val dependsOn: List<String> = emptyList(),
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
    ) : SettingDescriptor<String>()

    data class JsonSetting(
        override val id: String,
        override val groupPath: String,
        override val label: String,
        override val helpText: String = "",
        override val default: String, // JSON-encoded default
        override val featureFlag: String? = null,
        override val dependsOn: List<String> = emptyList(),
    ) : SettingDescriptor<String>()
}
