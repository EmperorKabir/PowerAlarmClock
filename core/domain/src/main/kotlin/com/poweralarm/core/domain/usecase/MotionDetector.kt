package com.poweralarm.core.domain.usecase

import kotlin.math.sqrt

/**
 * Pure-math sliding-window motion detector. Caller feeds raw accelerometer triples;
 * returns whether the magnitude window has exceeded [thresholdMs2] for at least
 * [requiredHits] consecutive samples.
 */
class MotionDetector(
    private val thresholdMs2: Float,
    private val requiredHits: Int = DEFAULT_REQUIRED_HITS,
) {
    private var consecutive = 0

    fun observe(x: Float, y: Float, z: Float): Boolean {
        val magnitude = sqrt(x * x + y * y + z * z) - GRAVITY
        consecutive = if (magnitude >= thresholdMs2) consecutive + 1 else 0
        return consecutive >= requiredHits
    }

    fun reset() { consecutive = 0 }

    private companion object {
        const val GRAVITY = 9.81f
        const val DEFAULT_REQUIRED_HITS = 5
    }
}
