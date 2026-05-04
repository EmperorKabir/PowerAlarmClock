package com.poweralarm.core.domain.usecase

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Evaluates a shift rotation: takes JSON like
 *   `[{"on":4},{"off":4}]`
 * (a list of segments) plus an anchor date, and returns whether [date] is an "on" day.
 */
object ShiftPatternEvaluator {

    private val json = Json { ignoreUnknownKeys = true }

    fun isOn(patternJson: String, anchorIso: String, date: LocalDate): Boolean {
        if (patternJson.isBlank() || anchorIso.isBlank()) return true
        val anchor = try { LocalDate.parse(anchorIso) } catch (_: Throwable) { return true }
        val tree = try { json.parseToJsonElement(patternJson) } catch (_: Throwable) { return true }
        val segments = (tree as? JsonArray) ?: return true
        val parsed = segments.mapNotNull { seg ->
            val obj = (seg as? kotlinx.serialization.json.JsonObject) ?: return@mapNotNull null
            val on = (obj["on"] as? JsonPrimitive)?.intOrNull
            val off = (obj["off"] as? JsonPrimitive)?.intOrNull
            when {
                on != null -> SegmentKind.ON to on
                off != null -> SegmentKind.OFF to off
                else -> null
            }
        }
        if (parsed.isEmpty()) return true
        val cycleLen = parsed.sumOf { it.second }
        if (cycleLen <= 0) return true
        val daysSinceAnchor = ChronoUnit.DAYS.between(anchor, date).toInt()
        if (daysSinceAnchor < 0) return true
        var idx = daysSinceAnchor % cycleLen
        for ((kind, length) in parsed) {
            if (idx < length) return kind == SegmentKind.ON
            idx -= length
        }
        return true
    }

    private enum class SegmentKind { ON, OFF }

    private val JsonPrimitive.intOrNull: Int? get() = content.toIntOrNull()
}
