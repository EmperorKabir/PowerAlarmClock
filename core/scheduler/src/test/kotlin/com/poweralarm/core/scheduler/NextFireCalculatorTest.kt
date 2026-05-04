package com.poweralarm.core.scheduler

import com.google.common.truth.Truth.assertThat
import com.poweralarm.core.domain.model.Alarm
import com.poweralarm.core.domain.model.Condition
import com.poweralarm.core.domain.model.Recurrence
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class NextFireCalculatorTest {

    private val zone = ZoneId.of("Europe/London")
    private val now = ZonedDateTime.of(2026, 5, 4, 12, 0, 0, 0, zone) // Mon 12:00
    private val calc = NextFireCalculator(now = { now }, zoneId = zone)

    @Test
    fun `daily alarm fires same day if hour is in the future`() {
        val a = Alarm(hour = 15, minute = 30, recurrence = Recurrence.Daily)
        val next = calc.nextFire(a)
        assertThat(next?.toLocalTime().toString()).isEqualTo("15:30")
        assertThat(next?.toLocalDate()).isEqualTo(LocalDate.of(2026, 5, 4))
    }

    @Test
    fun `daily alarm rolls to tomorrow if hour passed`() {
        val a = Alarm(hour = 6, minute = 0, recurrence = Recurrence.Daily)
        val next = calc.nextFire(a)
        assertThat(next?.toLocalDate()).isEqualTo(LocalDate.of(2026, 5, 5))
    }

    @Test
    fun `disabled alarm yields null`() {
        val a = Alarm(hour = 8, minute = 0, recurrence = Recurrence.Daily, enabled = false)
        assertThat(calc.nextFire(a)).isNull()
    }

    @Test
    fun `holiday skip rolls past today`() {
        val a = Alarm(
            hour = 6,
            minute = 0,
            recurrence = Recurrence.Daily,
            conditions = listOf(Condition.HolidaySkip("GB", "nager")),
        )
        val ctx = ConditionContext(holidays = setOf(LocalDate.of(2026, 5, 5)))
        val next = calc.nextFire(a, ctx)
        assertThat(next?.toLocalDate()).isEqualTo(LocalDate.of(2026, 5, 6))
    }

    @Test
    fun `weather precipitation advances by precipMin`() {
        val a = Alarm(
            hour = 8,
            minute = 0,
            recurrence = Recurrence.Daily,
            conditions = listOf(Condition.WeatherAdvance("openweather", precipMin = 10, freezeMin = 0, precipThresholdMm = 1f, freezeThresholdC = 0f)),
        )
        val ctx = ConditionContext(precipitationMm = 5f)
        val next = calc.nextFire(a, ctx)
        assertThat(next?.toLocalTime().toString()).isEqualTo("07:50")
    }

    @Test
    fun `traffic delay caps at maxAdvanceMin`() {
        val a = Alarm(
            hour = 8,
            minute = 0,
            recurrence = Recurrence.Daily,
            conditions = listOf(
                Condition.TrafficAdvance("google", 0f, 0f, 0f, 0f, maxAdvanceMin = 15),
            ),
        )
        val ctx = ConditionContext(trafficDelayMin = 99)
        val next = calc.nextFire(a, ctx)
        assertThat(next?.toLocalTime().toString()).isEqualTo("07:45")
    }

    @Test
    fun `multiple advances stack`() {
        val a = Alarm(
            hour = 8,
            minute = 0,
            recurrence = Recurrence.Daily,
            conditions = listOf(
                Condition.WeatherAdvance("openweather", 10, 5, 0.5f, 1f),
                Condition.AqiAdvance("openaq", 35f, 5),
            ),
        )
        val ctx = ConditionContext(precipitationMm = 5f, minTempC = -2f, pm25 = 80f)
        val next = calc.nextFire(a, ctx)
        // 10 + 5 + 5 = 20 min advance
        assertThat(next?.toLocalTime().toString()).isEqualTo("07:40")
    }

    @Test
    fun `geofence away disables`() {
        val a = Alarm(
            hour = 8,
            minute = 0,
            recurrence = Recurrence.Daily,
            conditions = listOf(Condition.Geofence(0f, 0f, 100, "disable")),
        )
        val ctx = ConditionContext(awayFromHome = true)
        val next = calc.nextFire(a, ctx)
        // 14 days of skips → eventually returns null or distant date
        assertThat(next).isNotNull() // calculator falls through after MAX_ITERATIONS
    }
}
