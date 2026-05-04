package com.poweralarm.feature.ringer.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.poweralarm.core.domain.model.RingerLayoutPolicy
import kotlin.random.Random

/**
 * Renders dismiss + snooze buttons positioned per [policy].
 * Re-keyed by [instanceKey] so every alarm instantiation re-rolls placements.
 */
@Composable
fun CognitiveLoadLayout(
    policy: RingerLayoutPolicy,
    instanceKey: Any,
    dismiss: @Composable (Modifier) -> Unit,
    snooze: @Composable (Modifier) -> Unit,
) {
    val random = remember(instanceKey) {
        if (policy.regenOnEachInstantiation) Random(System.nanoTime()) else Random(instanceKey.hashCode())
    }
    val dismissPlacement = remember(instanceKey) { PlacementGenerator.dismiss(policy, random) }
    val snoozePlacement = remember(instanceKey) { PlacementGenerator.snooze(policy, random) }

    BoxWithConstraints {
        val w = maxWidth
        val h = maxHeight
        val baseSize = MIN_HIT_DP
        Box(
            Modifier
                .offset(x = w * dismissPlacement.xFrac - baseSize / 2, y = h * dismissPlacement.yFrac - baseSize / 2)
                .scale(dismissPlacement.scale)
                .size(baseSize),
        ) { dismiss(Modifier) }

        Box(
            Modifier
                .offset(x = w * snoozePlacement.xFrac - baseSize / 2, y = h * snoozePlacement.yFrac - baseSize / 2)
                .scale(snoozePlacement.scale)
                .size(baseSize),
        ) { snooze(Modifier) }
    }
}

private val MIN_HIT_DP = 96.dp
