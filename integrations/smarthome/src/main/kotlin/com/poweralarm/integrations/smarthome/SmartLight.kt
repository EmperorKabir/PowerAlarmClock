package com.poweralarm.integrations.smarthome

interface SmartLightController {
    suspend fun setBrightness(targetId: String, brightness: Float, kelvin: Int)
    suspend fun off(targetId: String)
}

/** Pure-math ramp planner: produces (timeMs, brightness 0..1) tuples for a curve. */
object LightRampPlanner {
    fun plan(durationMin: Int, curve: String, steps: Int = DEFAULT_STEPS): List<Pair<Long, Float>> {
        if (durationMin <= 0 || steps <= 0) return emptyList()
        val totalMs = durationMin * MIN_TO_MS
        return (0..steps).map { i ->
            val p = i.toFloat() / steps
            val v = when (curve) {
                "linear" -> p
                "exponential" -> p * p
                "log" -> kotlin.math.sqrt(p)
                else -> p
            }
            (totalMs * i / steps) to v.coerceIn(0f, 1f)
        }
    }

    private const val MIN_TO_MS = 60_000L
    private const val DEFAULT_STEPS = 20
}
