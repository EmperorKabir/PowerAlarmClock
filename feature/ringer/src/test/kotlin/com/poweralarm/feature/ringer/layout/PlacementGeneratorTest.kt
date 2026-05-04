package com.poweralarm.feature.ringer.layout

import com.google.common.truth.Truth.assertThat
import com.poweralarm.core.domain.model.RingerLayoutPolicy
import org.junit.jupiter.api.Test
import kotlin.random.Random

class PlacementGeneratorTest {

    @Test
    fun `random placements stay within configured fractional bounds`() {
        val policy = RingerLayoutPolicy(
            cognitiveLoadRandomized = true,
            randomXMin = 0.1f,
            randomXMax = 0.9f,
            randomYMin = 0.2f,
            randomYMax = 0.8f,
            randomScaleMin = 0.6f,
            randomScaleMax = 1.4f,
        )
        repeat(SAMPLE_COUNT) {
            val r = Random(it.toLong())
            val p = PlacementGenerator.dismiss(policy, r)
            assertThat(p.xFrac).isAtLeast(0.1f)
            assertThat(p.xFrac).isAtMost(0.9f)
            assertThat(p.yFrac).isAtLeast(0.2f)
            assertThat(p.yFrac).isAtMost(0.8f)
            assertThat(p.scale).isAtLeast(0.6f)
            assertThat(p.scale).isAtMost(1.4f)
        }
    }

    @Test
    fun `accessibility mode returns fixed positions`() {
        val policy = RingerLayoutPolicy(cognitiveLoadRandomized = true, accessibilityMode = true)
        val a = PlacementGenerator.dismiss(policy)
        val b = PlacementGenerator.dismiss(policy)
        assertThat(a).isEqualTo(b)
    }

    @Test
    fun `non-randomized respects size mode scale`() {
        val small = PlacementGenerator.dismiss(RingerLayoutPolicy(dismissSize = "small"))
        val large = PlacementGenerator.dismiss(RingerLayoutPolicy(dismissSize = "large"))
        assertThat(small.scale).isLessThan(large.scale)
    }

    @Test
    fun `regenOnEachInstantiation produces distinct values across seeds`() {
        val policy = RingerLayoutPolicy(cognitiveLoadRandomized = true, regenOnEachInstantiation = true)
        val a = PlacementGenerator.dismiss(policy, Random(1L))
        val b = PlacementGenerator.dismiss(policy, Random(2L))
        assertThat(a).isNotEqualTo(b)
    }

    private companion object {
        const val SAMPLE_COUNT = 200
    }
}
