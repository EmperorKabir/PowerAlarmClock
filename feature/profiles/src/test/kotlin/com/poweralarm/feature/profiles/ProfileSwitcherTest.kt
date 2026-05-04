package com.poweralarm.feature.profiles

import com.google.common.truth.Truth.assertThat
import com.poweralarm.core.domain.model.Alarm
import org.junit.jupiter.api.Test

class ProfileSwitcherTest {

    @Test
    fun `enables alarms whose profile matches active`() {
        val a = Alarm(id = 1, hour = 7, minute = 0, profileId = "work", enabled = false)
        val b = Alarm(id = 2, hour = 9, minute = 0, profileId = "holiday", enabled = true)
        val out = ProfileSwitcher.apply(listOf(a, b), "work", emptyMap())
        assertThat(out[0].enabled).isTrue()
        assertThat(out[1].enabled).isFalse()
    }

    @Test
    fun `membership map allows additional profile ids`() {
        val a = Alarm(id = 1, hour = 7, minute = 0, profileId = "weekday", enabled = false)
        val out = ProfileSwitcher.apply(listOf(a), "work", mapOf("work" to setOf("weekday")))
        assertThat(out[0].enabled).isTrue()
    }
}
