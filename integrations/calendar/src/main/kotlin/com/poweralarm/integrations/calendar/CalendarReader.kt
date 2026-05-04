package com.poweralarm.integrations.calendar

import android.content.ContentUris
import android.content.Context
import android.provider.CalendarContract
import java.time.Instant

data class CalendarEvent(val calendarId: Long, val title: String, val startMs: Long, val endMs: Long)

class CalendarReader(private val context: Context) {

    fun upcoming(calendarIds: List<Long>, fromMs: Long, toMs: Long): List<CalendarEvent> {
        val cr = context.contentResolver
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon()
            .let { ContentUris.appendId(it, fromMs); ContentUris.appendId(it, toMs); it.build() }
        val selection = if (calendarIds.isEmpty()) null else
            "${CalendarContract.Instances.CALENDAR_ID} IN (${calendarIds.joinToString(",")})"
        val projection = arrayOf(
            CalendarContract.Instances.CALENDAR_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
        )
        val results = mutableListOf<CalendarEvent>()
        cr.query(uri, projection, selection, null, "${CalendarContract.Instances.BEGIN} ASC")?.use { cursor ->
            while (cursor.moveToNext()) {
                results += CalendarEvent(
                    calendarId = cursor.getLong(0),
                    title = cursor.getString(1) ?: "",
                    startMs = cursor.getLong(2),
                    endMs = cursor.getLong(3),
                )
            }
        }
        return results
    }

    fun earliestEventOffsetMin(calendarIds: List<Long>, lookaheadMin: Int): Int {
        val now = Instant.now().toEpochMilli()
        val until = now + lookaheadMin * MIN_TO_MS
        val first = upcoming(calendarIds, now, until).firstOrNull() ?: return 0
        return ((first.startMs - now) / MIN_TO_MS).toInt()
    }

    private companion object { const val MIN_TO_MS = 60_000L }
}
