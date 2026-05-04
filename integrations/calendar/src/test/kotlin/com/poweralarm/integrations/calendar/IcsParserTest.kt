package com.poweralarm.integrations.calendar

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class IcsParserTest {

    private val sample = """
        BEGIN:VCALENDAR
        BEGIN:VEVENT
        DTSTART:20260525
        SUMMARY:Spring bank holiday
        END:VEVENT
        BEGIN:VEVENT
        DTSTART:20260601T090000Z
        SUMMARY:Vacation start
        END:VEVENT
        END:VCALENDAR
    """.trimIndent()

    @Test
    fun `parses date-only and datetime DTSTART`() {
        val events = IcsParser.parseEvents(sample)
        assertThat(events).hasSize(2)
        assertThat(events[0].date).isEqualTo(LocalDate.of(2026, 5, 25))
        assertThat(events[1].date).isEqualTo(LocalDate.of(2026, 6, 1))
    }

    @Test
    fun `regex filter narrows results`() {
        val matches = IcsParser.matchingDates(sample, summaryRegex = "(?i)vacation")
        assertThat(matches).containsExactly(LocalDate.of(2026, 6, 1))
    }

    @Test
    fun `blank regex returns all`() {
        val matches = IcsParser.matchingDates(sample, summaryRegex = "")
        assertThat(matches).hasSize(2)
    }
}
