package com.poweralarm.core.audio

import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Pure-math volume curves. All inputs are normalized: progress ∈ [0,1] → volume ∈ [0,1].
 */
object VolumeCurve {
    fun linear(progress: Float): Float = progress.coerceIn(0f, 1f)

    fun exponential(progress: Float, base: Float = 4f): Float {
        val p = progress.coerceIn(0f, 1f)
        return ((base.pow(p) - 1f) / (base - 1f)).coerceIn(0f, 1f)
    }

    fun log(progress: Float): Float {
        val p = progress.coerceIn(0f, 1f)
        if (p == 0f) return 0f
        return (ln(1 + p * (E - 1f)) / 1f).coerceIn(0f, 1f)
    }

    fun stepped(progress: Float, steps: Int = 5): Float {
        val p = progress.coerceIn(0f, 1f)
        val s = max(1, steps)
        return (kotlin.math.floor(p * s) / s).coerceIn(0f, 1f)
    }

    fun apply(curve: String, progress: Float): Float = when (curve.lowercase()) {
        "linear" -> linear(progress)
        "exponential" -> exponential(progress)
        "log" -> log(progress)
        "stepped" -> stepped(progress)
        else -> linear(progress)
    }

    fun ramp(
        startPct: Int,
        endPct: Int,
        durationMs: Long,
        elapsedMs: Long,
        curve: String,
    ): Float {
        if (durationMs <= 0) return endPct / PERCENT
        val progress = (elapsedMs.toFloat() / durationMs).coerceIn(0f, 1f)
        val curved = apply(curve, progress)
        val s = startPct / PERCENT
        val e = endPct / PERCENT
        return (s + curved * (e - s)).coerceIn(0f, 1f)
    }

    fun fadeOut(volume: Float, elapsedMs: Long, fadeMs: Long, curve: String): Float {
        if (fadeMs <= 0) return 0f
        val progress = (elapsedMs.toFloat() / fadeMs).coerceIn(0f, 1f)
        val factor = 1f - apply(curve, progress)
        return (volume * factor).coerceIn(0f, 1f)
    }

    private const val E = 2.718281828f
    private const val PERCENT = 100f

    @Suppress("UnusedPrivateMember")
    private fun clamp(x: Float): Float = min(1f, max(0f, x))
}
