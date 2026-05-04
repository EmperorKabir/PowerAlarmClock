package com.poweralarm.feature.ringer.cognitive

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import kotlin.random.Random

class CognitiveProblemBankTest {

    @Test
    fun `algebra problem yields correct integer answer`() {
        val p = CognitiveProblemBank.next("algebra", "medium", Random(42L))
        // Statement: Solve for x: ax + b = ax+b — solve back.
        val match = Regex("Solve for x: (\\d+)x \\+ (\\d+) = (\\d+)").find(p.statement) ?: error("regex")
        val (a, b, rhs) = match.destructured
        val expectedX = (rhs.toInt() - b.toInt()) / a.toInt()
        assertThat(p.isCorrect(expectedX.toString())).isTrue()
        assertThat(p.isCorrect((expectedX + 1).toString())).isFalse()
    }

    @Test
    fun `binomial problem accepts integer C n k`() {
        val p = CognitiveProblemBank.next("probability", "medium", Random(7L))
        // Brute-force every plausible (n,k) up to small bounds and find the one matching the statement.
        val match = Regex("C\\((\\d+),(\\d+)\\)").find(p.statement) ?: error("regex")
        val (n, k) = match.destructured
        val expected = binomial(n.toInt(), k.toInt())
        assertThat(p.isCorrect(expected.toString())).isTrue()
    }

    @Test
    fun `sequences problem advances arithmetically`() {
        val p = CognitiveProblemBank.next("sequences", "easy", Random(1L))
        val nums = Regex("(\\d+)").findAll(p.statement).map { it.value.toInt() }.toList()
        // First 4 numbers + step → next val. step = nums[1] - nums[0].
        val step = nums[1] - nums[0]
        val expected = nums[3] + step
        assertThat(p.isCorrect(expected.toString())).isTrue()
    }

    @Test
    fun `random domain returns problem of any kind without throwing`() {
        repeat(100) { CognitiveProblemBank.next(domain = null, difficulty = "medium", random = Random(it.toLong())) }
    }

    private fun binomial(n: Int, k: Int): Long {
        var result = 1L
        for (i in 0 until k) result = result * (n - i) / (i + 1)
        return result
    }
}
