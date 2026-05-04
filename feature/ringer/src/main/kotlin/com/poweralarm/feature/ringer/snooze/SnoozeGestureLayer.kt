package com.poweralarm.feature.ringer.snooze

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs

enum class SnoozeDirection { UP, DOWN, LEFT, RIGHT }

@Composable
fun SnoozeGestureLayer(
    modifier: Modifier = Modifier,
    onSwipe: (SnoozeDirection) -> Unit,
    content: @Composable () -> Unit,
) {
    val total = remember { mutableStateOf(Offset.Zero) }
    Box(
        modifier = modifier.pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { total.value = Offset.Zero },
                onDragEnd = {
                    val (dx, dy) = total.value.x to total.value.y
                    val direction = classify(dx, dy) ?: return@detectDragGestures
                    onSwipe(direction)
                },
                onDrag = { _, drag -> total.value += drag },
            )
        },
    ) { content() }
}

internal fun classify(dx: Float, dy: Float, minMagnitude: Float = MIN_SWIPE_PX): SnoozeDirection? {
    if (abs(dx) < minMagnitude && abs(dy) < minMagnitude) return null
    return if (abs(dx) > abs(dy)) {
        if (dx > 0) SnoozeDirection.RIGHT else SnoozeDirection.LEFT
    } else {
        if (dy > 0) SnoozeDirection.DOWN else SnoozeDirection.UP
    }
}

private const val MIN_SWIPE_PX = 80f
