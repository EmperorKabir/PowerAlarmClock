package com.poweralarm.core.audio

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class VolumeCurveTest {

    @Test
    fun `linear curve at endpoints`() {
        assertThat(VolumeCurve.linear(0f)).isEqualTo(0f)
        assertThat(VolumeCurve.linear(1f)).isEqualTo(1f)
    }

    @Test
    fun `exponential is monotonic and bounded`() {
        var prev = -0.001f
        for (i in 0..10) {
            val v = VolumeCurve.exponential(i / 10f)
            assertThat(v).isAtLeast(prev)
            assertThat(v).isAtMost(1f)
            prev = v
        }
    }

    @Test
    fun `ramp respects start and end percent`() {
        val v0 = VolumeCurve.ramp(startPct = 10, endPct = 100, durationMs = 1000, elapsedMs = 0, curve = "linear")
        val v1 = VolumeCurve.ramp(startPct = 10, endPct = 100, durationMs = 1000, elapsedMs = 1000, curve = "linear")
        assertThat(v0).isWithin(1e-4f).of(0.10f)
        assertThat(v1).isWithin(1e-4f).of(1.00f)
    }

    @Test
    fun `ramp clamps elapsed beyond duration`() {
        val v = VolumeCurve.ramp(0, 100, 1000, 9_999, "linear")
        assertThat(v).isEqualTo(1f)
    }

    @Test
    fun `fade out reaches zero`() {
        val v = VolumeCurve.fadeOut(volume = 1f, elapsedMs = 10_000, fadeMs = 10_000, curve = "linear")
        assertThat(v).isEqualTo(0f)
    }

    @Test
    fun `stepped quantizes`() {
        val v = VolumeCurve.stepped(0.45f, steps = 5)
        // floor(0.45*5)/5 = 2/5 = 0.4
        assertThat(v).isWithin(1e-4f).of(0.4f)
    }
}
