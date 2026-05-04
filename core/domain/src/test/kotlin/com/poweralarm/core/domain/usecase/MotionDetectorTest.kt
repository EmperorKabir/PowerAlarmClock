package com.poweralarm.core.domain.usecase

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class MotionDetectorTest {

    @Test
    fun `triggers after consecutive over-threshold samples`() {
        val det = MotionDetector(thresholdMs2 = 1.0f, requiredHits = 3)
        repeat(2) { assertThat(det.observe(15f, 0f, 0f)).isFalse() }
        assertThat(det.observe(15f, 0f, 0f)).isTrue()
    }

    @Test
    fun `resets streak when magnitude drops below threshold`() {
        val det = MotionDetector(thresholdMs2 = 1.0f, requiredHits = 3)
        det.observe(15f, 0f, 0f)
        det.observe(15f, 0f, 0f)
        det.observe(0f, 0f, 0f) // resets
        assertThat(det.observe(15f, 0f, 0f)).isFalse()
    }
}
