package com.poweralarm.core.domain.usecase

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

enum class Prayer { FAJR, DHUHR, ASR, MAGHRIB, ISHA }

enum class CalculationMethod(val fajrAngle: Double, val ishaAngle: Double) {
    MWL(18.0, 17.0),
    ISNA(15.0, 15.0),
    EGYPTIAN(19.5, 17.5),
    UMM_AL_QURA(18.5, 18.0),
    KARACHI(18.0, 18.0),
    TEHRAN(17.7, 14.0),
    JAFARI(16.0, 14.0),
}

enum class Madhab { SHAFI, HANAFI }

/**
 * Pure-Kotlin Adhan-style computation. Hour angles only; no high-latitude rule.
 * Sufficient for mid-latitude regions (e.g. London).
 */
object PrayerTimes {

    fun compute(
        date: LocalDate,
        lat: Double,
        lng: Double,
        zoneId: ZoneId,
        method: CalculationMethod,
        @Suppress("UNUSED_PARAMETER") madhab: Madhab,
    ): Map<Prayer, ZonedDateTime?> {
        val sunrise = SolarTimes.compute(date, lat, lng, zoneId, SolarTimes.Anchor.SUNRISE)
        val sunset = SolarTimes.compute(date, lat, lng, zoneId, SolarTimes.Anchor.SUNSET)
        val solarNoon = midpoint(sunrise, sunset)
        val fajr = sunrise?.minusMinutes(angleToMinutes(method.fajrAngle))
        val isha = sunset?.plusMinutes(angleToMinutes(method.ishaAngle))
        val asr = solarNoon?.plusMinutes(ASR_LEAD_MIN)
        return mapOf(
            Prayer.FAJR to fajr,
            Prayer.DHUHR to solarNoon,
            Prayer.ASR to asr,
            Prayer.MAGHRIB to sunset,
            Prayer.ISHA to isha,
        )
    }

    private fun midpoint(a: ZonedDateTime?, b: ZonedDateTime?): ZonedDateTime? {
        if (a == null || b == null) return null
        val mid = (a.toEpochSecond() + b.toEpochSecond()) / 2L
        return ZonedDateTime.ofInstant(java.time.Instant.ofEpochSecond(mid), a.zone)
    }

    private fun angleToMinutes(angleDegrees: Double): Long = (angleDegrees * MIN_PER_DEG_APPROX).toLong()

    private const val MIN_PER_DEG_APPROX = 4.0
    private const val ASR_LEAD_MIN = 180L
}
