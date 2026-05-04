package com.poweralarm.core.domain.usecase

import com.poweralarm.core.domain.model.SnoozePolicy

data class SnoozeState(val countSoFar: Int = 0, val nextDurationMin: Int = 0)

class SnoozeEngine {

    /** Computes the duration of the *next* snooze, or null if max-count reached. */
    fun next(policy: SnoozePolicy, state: SnoozeState): SnoozeState? {
        if (state.countSoFar >= policy.maxCount) return null
        val raw = policy.baseMin - state.countSoFar * policy.decrementMin
        val clamped = raw.coerceAtLeast(policy.floorMin)
        return state.copy(countSoFar = state.countSoFar + 1, nextDurationMin = clamped)
    }

    /** Maps a swipe direction or device orientation to a duration override.
     *  [gestureMapJson] is a flat `{"up": 5, "down": 15, "left": 20, "right": 1}` shape. */
    fun applyGesture(policy: SnoozePolicy, gesture: String): Int? {
        val map = parseGestureMap(policy.gestureMapJson)
        return map[gesture]
    }

    private fun parseGestureMap(json: String): Map<String, Int> {
        // Permissive parser to avoid pulling kotlinx-serialization here. Format: `{"up":5,"down":15}`.
        val trimmed = json.trim().removeSurrounding("{", "}")
        if (trimmed.isBlank()) return emptyMap()
        return trimmed.split(",")
            .mapNotNull { entry ->
                val parts = entry.split(":")
                if (parts.size != PAIR_SIZE) return@mapNotNull null
                val key = parts[0].trim().trim('"')
                val value = parts[1].trim().toIntOrNull() ?: return@mapNotNull null
                key to value
            }
            .toMap()
    }

    private companion object {
        const val PAIR_SIZE = 2
    }
}
