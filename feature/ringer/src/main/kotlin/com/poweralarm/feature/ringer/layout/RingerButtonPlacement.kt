package com.poweralarm.feature.ringer.layout

import com.poweralarm.core.domain.model.RingerLayoutPolicy
import kotlin.random.Random

/**
 * Pure-math placement: each button anchor is `(xFrac, yFrac, scale)` ∈ unit cube
 * scaled by [policy]. Hit-test still respects 48 dp minimum at the consuming
 * Composable layer.
 */
data class RingerButtonPlacement(val xFrac: Float, val yFrac: Float, val scale: Float) {
    companion object {
        fun fixed(xFrac: Float = 0.5f, yFrac: Float = 0.5f, scale: Float = 1f) =
            RingerButtonPlacement(xFrac, yFrac, scale)
    }
}

object PlacementGenerator {

    fun dismiss(policy: RingerLayoutPolicy, random: Random = Random.Default): RingerButtonPlacement =
        generate(policy, isDismiss = true, random)

    fun snooze(policy: RingerLayoutPolicy, random: Random = Random.Default): RingerButtonPlacement =
        generate(policy, isDismiss = false, random)

    private fun generate(policy: RingerLayoutPolicy, isDismiss: Boolean, random: Random): RingerButtonPlacement {
        if (policy.accessibilityMode) return RingerButtonPlacement.fixed(yFrac = if (isDismiss) 0.7f else 0.3f)
        if (!policy.cognitiveLoadRandomized) {
            return RingerButtonPlacement.fixed(
                yFrac = if (isDismiss) 0.7f else 0.3f,
                scale = baseScale(if (isDismiss) policy.dismissSize else policy.snoozeSize),
            )
        }
        val x = randomBetween(policy.randomXMin, policy.randomXMax, random)
        val y = randomBetween(policy.randomYMin, policy.randomYMax, random)
        val s = randomBetween(policy.randomScaleMin, policy.randomScaleMax, random)
        return RingerButtonPlacement(x, y, s)
    }

    private fun randomBetween(min: Float, max: Float, random: Random): Float {
        val lo = minOf(min, max)
        val hi = maxOf(min, max)
        if (lo == hi) return lo
        return lo + random.nextFloat() * (hi - lo)
    }

    private fun baseScale(sizeMode: String): Float = when (sizeMode) {
        "small" -> 0.7f
        "large" -> 1.3f
        else -> 1f
    }
}
