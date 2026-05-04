package com.poweralarm.core.settings

sealed class ValidationResult {
    data object Ok : ValidationResult()
    data class Invalid(val reason: String) : ValidationResult()
}

object Validators {
    private val HEX_COLOR = Regex("^#([0-9A-Fa-f]{6}|[0-9A-Fa-f]{8})$")

    fun validate(descriptor: SettingDescriptor<*>, raw: Any?): ValidationResult = when (descriptor) {
        is SettingDescriptor.BoolSetting -> if (raw is Boolean) ValidationResult.Ok else invalid(descriptor, raw, "Boolean")

        is SettingDescriptor.IntSetting -> when {
            raw !is Int -> invalid(descriptor, raw, "Int")
            raw !in descriptor.range -> ValidationResult.Invalid("${descriptor.id}: $raw out of ${descriptor.range}")
            else -> ValidationResult.Ok
        }

        is SettingDescriptor.FloatSetting -> when {
            raw !is Float -> invalid(descriptor, raw, "Float")
            raw < descriptor.rangeStart || raw > descriptor.rangeEndInclusive ->
                ValidationResult.Invalid("${descriptor.id}: $raw out of [${descriptor.rangeStart}, ${descriptor.rangeEndInclusive}]")
            else -> ValidationResult.Ok
        }

        is SettingDescriptor.StringSetting -> when {
            raw !is String -> invalid(descriptor, raw, "String")
            descriptor.pattern != null && !Regex(descriptor.pattern).matches(raw) ->
                ValidationResult.Invalid("${descriptor.id}: '$raw' fails pattern ${descriptor.pattern}")
            else -> ValidationResult.Ok
        }

        is SettingDescriptor.ColorSetting -> when {
            raw !is String -> invalid(descriptor, raw, "String")
            !HEX_COLOR.matches(raw) -> ValidationResult.Invalid("${descriptor.id}: '$raw' is not a #RRGGBB or #AARRGGBB hex")
            else -> ValidationResult.Ok
        }

        is SettingDescriptor.EnumSetting -> when {
            raw !is String -> invalid(descriptor, raw, "String")
            raw !in descriptor.choices -> ValidationResult.Invalid("${descriptor.id}: '$raw' not in ${descriptor.choices}")
            else -> ValidationResult.Ok
        }

        is SettingDescriptor.JsonSetting -> when {
            raw !is String -> invalid(descriptor, raw, "String")
            !looksLikeJson(raw) -> ValidationResult.Invalid("${descriptor.id}: not parseable JSON")
            else -> ValidationResult.Ok
        }
    }

    private fun invalid(d: SettingDescriptor<*>, raw: Any?, expected: String): ValidationResult.Invalid =
        ValidationResult.Invalid("${d.id}: expected $expected got ${raw?.javaClass?.simpleName ?: "null"}")

    private fun looksLikeJson(s: String): Boolean {
        val t = s.trim()
        return (t.startsWith("{") && t.endsWith("}")) ||
            (t.startsWith("[") && t.endsWith("]")) ||
            t == "null" || t == "true" || t == "false" ||
            t.toDoubleOrNull() != null ||
            (t.startsWith("\"") && t.endsWith("\""))
    }
}
