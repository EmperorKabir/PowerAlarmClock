package com.poweralarm.core.domain.usecase

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ShiftPatternEvaluatorTest {

    private val pattern = """[{"on":4},{"off":4}]"""
    private val anchor = "2026-05-04" // Monday

    @Test
    fun `four-on then four-off cycles correctly`() {
        val expected = listOf(
            // 0..3 ON, 4..7 OFF, 8..11 ON
            true, true, true, true,
            false, false, false, false,
            true, true, true, true,
        )
        expected.forEachIndexed { i, on ->
            val date = LocalDate.parse(anchor).plusDays(i.toLong())
            assertThat(ShiftPatternEvaluator.isOn(pattern, anchor, date)).named("day $i").isEqualTo(on)
        }
    }

    @Test
    fun `blank pattern defaults to on`() {
        val date = LocalDate.parse(anchor).plusDays(5)
        assertThat(ShiftPatternEvaluator.isOn("", anchor, date)).isTrue()
    }
}
