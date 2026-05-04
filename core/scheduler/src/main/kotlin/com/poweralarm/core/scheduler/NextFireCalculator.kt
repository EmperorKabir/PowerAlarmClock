package com.poweralarm.core.scheduler

import com.poweralarm.core.domain.model.Alarm
import com.poweralarm.core.domain.model.Condition
import com.poweralarm.core.domain.model.Recurrence
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

/**
 * Pure-Kotlin reduction pipeline:
 *   recurrence → skip filters → advance accumulators → solar/adhan → chain offsets.
 *
 * External-data conditions (weather, traffic, AQI, TfL) are gated by [conditionContext]
 * which the caller populates from cached worker output. This keeps the calculator
 * deterministic for testing.
 */
class NextFireCalculator(
    private val now: () -> ZonedDateTime = { ZonedDateTime.now() },
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) {

    fun nextFire(alarm: Alarm, ctx: ConditionContext = ConditionContext()): ZonedDateTime? {
        if (!alarm.enabled) return null

        var candidate = baseNext(alarm) ?: return null

        var iterations = 0
        while (iterations < MAX_ITERATIONS) {
            iterations++
            val skip = shouldSkip(alarm, candidate, ctx)
            if (skip != null) {
                candidate = bumpToNext(alarm, candidate)
                continue
            }
            candidate = applyAdvance(alarm, candidate, ctx)
            return candidate
        }
        return null
    }

    private fun baseNext(alarm: Alarm): ZonedDateTime? {
        val nowZdt = now().withZoneSameInstant(zoneId)
        val time = LocalTime.of(alarm.hour, alarm.minute)
        val today = nowZdt.toLocalDate().atTime(time).atZone(zoneId)
        val baseToday = if (today.isAfter(nowZdt)) today else today.plusDays(1)

        return when (val r = alarm.recurrence) {
            is Recurrence.Once -> if (today.isAfter(nowZdt)) today else null
            is Recurrence.Daily -> baseToday
            is Recurrence.Weekly -> nextWeekly(nowZdt, time, r.daysOfWeek)
            is Recurrence.Cron -> baseToday
            is Recurrence.SolarAnchored, is Recurrence.Adhan,
            is Recurrence.Polyphasic, is Recurrence.ShiftPattern,
            is Recurrence.Chained,
            -> baseToday
        }
    }

    private fun nextWeekly(nowZdt: ZonedDateTime, time: LocalTime, days: Set<Int>): ZonedDateTime? {
        if (days.isEmpty()) return null
        for (offset in 0..LOOKAHEAD_DAYS) {
            val date = nowZdt.toLocalDate().plusDays(offset.toLong())
            if (date.dayOfWeek.value % WEEK in days || date.dayOfWeek.value in days) {
                val candidate = LocalDateTime.of(date, time).atZone(zoneId)
                if (candidate.isAfter(nowZdt)) return candidate
            }
        }
        return null
    }

    private fun shouldSkip(alarm: Alarm, candidate: ZonedDateTime, ctx: ConditionContext): String? {
        alarm.conditions.forEach { c ->
            when (c) {
                is Condition.HolidaySkip -> if (ctx.holidays.contains(candidate.toLocalDate())) return "holiday"
                is Condition.DateRangeDisable -> if (inRange(candidate.toLocalDate(), c.startIso, c.endIso)) return "dateRange"
                is Condition.TempSuspension -> if (ctx.skipNextRemaining > 0) return "tempSuspension"
                is Condition.IcsSkip -> if (ctx.icsSkipDates.contains(candidate.toLocalDate())) return "icsSkip"
                is Condition.Geofence -> if (c.mode == "disable" && ctx.awayFromHome) return "geofence"
                else -> Unit
            }
        }
        return null
    }

    private fun applyAdvance(alarm: Alarm, candidate: ZonedDateTime, ctx: ConditionContext): ZonedDateTime {
        var advanceMin = 0
        alarm.conditions.forEach { c ->
            when (c) {
                is Condition.WeatherAdvance -> {
                    if (ctx.precipitationMm >= c.precipThresholdMm) advanceMin += c.precipMin
                    if (ctx.minTempC <= c.freezeThresholdC) advanceMin += c.freezeMin
                }
                is Condition.TrafficAdvance -> advanceMin += minOf(ctx.trafficDelayMin, c.maxAdvanceMin)
                is Condition.AqiAdvance -> if (ctx.pm25 >= c.pm25Threshold) advanceMin += c.advanceMin
                is Condition.TflDisruption -> if (ctx.tflDisrupted) advanceMin += c.maxAdvanceMin
                is Condition.CalendarShift -> if (ctx.earliestEventOffsetMin > 0) advanceMin += c.bufferMin
                is Condition.BedtimePenalty -> if (ctx.bedtimePenaltyTriggered) advanceMin += c.advanceMin
                is Condition.LowBatteryFailsafe -> if (ctx.lowBatteryFailsafeTriggered) advanceMin += c.earlyFireMin
                else -> Unit
            }
        }
        return candidate.minusMinutes(advanceMin.toLong())
    }

    private fun bumpToNext(alarm: Alarm, candidate: ZonedDateTime): ZonedDateTime = when (alarm.recurrence) {
        is Recurrence.Daily, is Recurrence.SolarAnchored, is Recurrence.Adhan -> candidate.plusDays(1)
        is Recurrence.Weekly -> candidate.plusDays(1)
        is Recurrence.Once -> candidate.plusDays(LOOKAHEAD_DAYS.toLong())
        is Recurrence.Cron -> candidate.plusDays(1)
        is Recurrence.Polyphasic, is Recurrence.ShiftPattern, is Recurrence.Chained -> candidate.plusDays(1)
    }

    private fun inRange(date: LocalDate, startIso: String, endIso: String): Boolean = try {
        val s = LocalDate.parse(startIso)
        val e = LocalDate.parse(endIso)
        !date.isBefore(s) && !date.isAfter(e)
    } catch (_: Throwable) {
        false
    }

    @Suppress("UnusedPrivateMember")
    private fun nextDayOfWeek(date: LocalDate, dow: DayOfWeek): LocalDate = date.with(TemporalAdjusters.nextOrSame(dow))

    companion object {
        private const val LOOKAHEAD_DAYS = 14
        private const val WEEK = 7
        private const val MAX_ITERATIONS = 60
    }
}

data class ConditionContext(
    val holidays: Set<LocalDate> = emptySet(),
    val icsSkipDates: Set<LocalDate> = emptySet(),
    val skipNextRemaining: Int = 0,
    val awayFromHome: Boolean = false,
    val precipitationMm: Float = 0f,
    val minTempC: Float = 99f,
    val trafficDelayMin: Int = 0,
    val pm25: Float = 0f,
    val tflDisrupted: Boolean = false,
    val earliestEventOffsetMin: Int = 0,
    val bedtimePenaltyTriggered: Boolean = false,
    val lowBatteryFailsafeTriggered: Boolean = false,
)
