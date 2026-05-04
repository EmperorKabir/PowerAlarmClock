package com.poweralarm.core.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.poweralarm.core.domain.model.SnoozePolicy
import org.junit.jupiter.api.Test

class SnoozeEngineTest {

    private val engine = SnoozeEngine()

    @Test
    fun `decreases by decrement each call`() {
        val policy = SnoozePolicy(baseMin = 9, decrementMin = 2, floorMin = 1, maxCount = 5)
        var s: SnoozeState? = SnoozeState()
        val durations = mutableListOf<Int>()
        repeat(5) {
            s = engine.next(policy, s!!)
            durations += s!!.nextDurationMin
        }
        assertThat(durations).containsExactly(9, 7, 5, 3, 1).inOrder()
    }

    @Test
    fun `clamps at floor`() {
        val policy = SnoozePolicy(baseMin = 5, decrementMin = 3, floorMin = 2, maxCount = 5)
        var s: SnoozeState? = SnoozeState()
        val durations = mutableListOf<Int>()
        repeat(4) { s = engine.next(policy, s!!); durations += s!!.nextDurationMin }
        assertThat(durations).containsExactly(5, 2, 2, 2).inOrder()
    }

    @Test
    fun `returns null after max count`() {
        val policy = SnoozePolicy(baseMin = 9, maxCount = 2)
        var s: SnoozeState? = SnoozeState()
        s = engine.next(policy, s!!)
        s = engine.next(policy, s!!)
        assertThat(engine.next(policy, s!!)).isNull()
    }

    @Test
    fun `gesture map applies override`() {
        val policy = SnoozePolicy(gestureMapJson = """{"up":5,"down":15,"left":20}""")
        assertThat(engine.applyGesture(policy, "up")).isEqualTo(5)
        assertThat(engine.applyGesture(policy, "down")).isEqualTo(15)
        assertThat(engine.applyGesture(policy, "missing")).isNull()
    }
}
