package com.poweralarm.integrations.calendar

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Minimal RFC 5545 subset parser. Extracts (DTSTART, SUMMARY) from VEVENT blocks.
 * Sufficient for skip-day matching; not a full iCalendar implementation.
 */
object IcsParser {

    fun parseEvents(text: String): List<IcsEvent> {
        val events = mutableListOf<IcsEvent>()
        var inEvent = false
        var dtStart: LocalDate? = null
        var summary: String? = null
        text.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            when {
                line == "BEGIN:VEVENT" -> { inEvent = true; dtStart = null; summary = null }
                line == "END:VEVENT" -> {
                    if (inEvent && dtStart != null) events += IcsEvent(dtStart!!, summary.orEmpty())
                    inEvent = false
                }
                inEvent && line.startsWith("DTSTART") -> dtStart = parseDate(line.substringAfter(':'))
                inEvent && line.startsWith("SUMMARY") -> summary = line.substringAfter(':')
            }
        }
        return events
    }

    fun matchingDates(text: String, summaryRegex: String): Set<LocalDate> {
        val pattern = if (summaryRegex.isBlank()) null else Regex(summaryRegex)
        return parseEvents(text).filter { ev -> pattern == null || pattern.containsMatchIn(ev.summary) }
            .map { it.date }
            .toSet()
    }

    private fun parseDate(value: String): LocalDate? = try {
        when {
            value.contains("T") -> LocalDate.parse(value.substringBefore('T'), BASIC)
            else -> LocalDate.parse(value, BASIC)
        }
    } catch (_: Throwable) {
        null
    }

    private val BASIC = DateTimeFormatter.ofPattern("yyyyMMdd")
}

data class IcsEvent(val date: LocalDate, val summary: String)
