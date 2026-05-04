package com.poweralarm.core.domain.usecase

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/**
 * NOAA-style solar position math for sunrise / sunset / civil twilight.
 * Adequate for alarm anchoring; not a science-grade ephemeris.
 */
object SolarTimes {

    enum class Anchor {
        SUNRISE, SUNSET, CIVIL_DAWN, CIVIL_DUSK, NAUTICAL_DAWN, NAUTICAL_DUSK, ASTRO_DAWN, ASTRO_DUSK,
    }

    fun compute(date: LocalDate, lat: Double, lng: Double, zoneId: ZoneId, anchor: Anchor): ZonedDateTime? {
        val zenith = when (anchor) {
            Anchor.SUNRISE, Anchor.SUNSET -> SUNRISE_ZENITH
            Anchor.CIVIL_DAWN, Anchor.CIVIL_DUSK -> CIVIL_ZENITH
            Anchor.NAUTICAL_DAWN, Anchor.NAUTICAL_DUSK -> NAUTICAL_ZENITH
            Anchor.ASTRO_DAWN, Anchor.ASTRO_DUSK -> ASTRONOMICAL_ZENITH
        }
        val isRising = anchor in setOf(
            Anchor.SUNRISE, Anchor.CIVIL_DAWN, Anchor.NAUTICAL_DAWN, Anchor.ASTRO_DAWN,
        )
        val n = date.dayOfYear
        val lngHour = lng / DEG_PER_HOUR
        val t = if (isRising) n + (HOURS_RISE - lngHour) / HOURS_DAY
        else n + (HOURS_SET - lngHour) / HOURS_DAY
        val M = MEAN_ANOM_BASE * t - MEAN_ANOM_OFFSET
        val L = bound360(M + ECC_TERM_1 * sin(M.toRad()) + ECC_TERM_2 * sin((TWO * M).toRad()) + LONG_OFFSET)
        var RA = bound360(Math.toDegrees(Math.atan(OBLIQ_RA * Math.tan(L.toRad()))))
        val Lq = (Math.floor(L / QUAD) * QUAD)
        val RAq = (Math.floor(RA / QUAD) * QUAD)
        RA = (RA + (Lq - RAq)) / DEG_PER_HOUR
        val sinDec = OBLIQ_DEC_SIN * sin(L.toRad())
        val cosDec = cos(asin(sinDec))
        val cosH = (cos(zenith.toRad()) - sinDec * sin(lat.toRad())) / (cosDec * cos(lat.toRad()))
        if (cosH > 1.0 || cosH < -1.0) return null
        var H = if (isRising) BOUND_HOURS - Math.toDegrees(acos(cosH)) / DEG_PER_HOUR
        else Math.toDegrees(acos(cosH)) / DEG_PER_HOUR
        val T = H + RA - DAY_FACTOR * t - SOLAR_OFFSET
        val UT = bound24(T - lngHour)
        val hour = UT.toInt()
        val minute = ((UT - hour) * MIN_PER_HOUR).toInt()
        return ZonedDateTime.of(date, java.time.LocalTime.of(hour, minute), ZoneId.of("UTC"))
            .withZoneSameInstant(zoneId)
    }

    private fun bound360(x: Double): Double = ((x % FULL_CIRCLE) + FULL_CIRCLE) % FULL_CIRCLE
    private fun bound24(x: Double): Double = ((x % HOURS_DAY) + HOURS_DAY) % HOURS_DAY
    private fun Double.toRad(): Double = this * PI / HALF_CIRCLE

    private const val SUNRISE_ZENITH = 90.833
    private const val CIVIL_ZENITH = 96.0
    private const val NAUTICAL_ZENITH = 102.0
    private const val ASTRONOMICAL_ZENITH = 108.0
    private const val DEG_PER_HOUR = 15.0
    private const val HOURS_DAY = 24.0
    private const val HOURS_RISE = 6.0
    private const val HOURS_SET = 18.0
    private const val MEAN_ANOM_BASE = 0.9856
    private const val MEAN_ANOM_OFFSET = 3.289
    private const val ECC_TERM_1 = 1.916
    private const val ECC_TERM_2 = 0.020
    private const val LONG_OFFSET = 282.634
    private const val OBLIQ_RA = 0.91764
    private const val OBLIQ_DEC_SIN = 0.39782
    private const val QUAD = 90.0
    private const val FULL_CIRCLE = 360.0
    private const val HALF_CIRCLE = 180.0
    private const val TWO = 2.0
    private const val BOUND_HOURS = 24.0
    private const val DAY_FACTOR = 0.06571
    private const val SOLAR_OFFSET = 6.622
    private const val MIN_PER_HOUR = 60
}
