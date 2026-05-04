package com.poweralarm.core.logging

import java.time.Instant

data class ForensicEvent(
    val tsEpochMs: Long,
    val kind: String,
    val alarmId: Long?,
    val payload: Map<String, String>,
)

interface ForensicLog {
    fun record(kind: String, alarmId: Long? = null, payload: Map<String, String> = emptyMap())
    fun events(sinceMs: Long = 0L): List<ForensicEvent>
}

class InMemoryForensicLog : ForensicLog {
    private val events = mutableListOf<ForensicEvent>()
    override fun record(kind: String, alarmId: Long?, payload: Map<String, String>) {
        events += ForensicEvent(Instant.now().toEpochMilli(), kind, alarmId, payload)
    }
    override fun events(sinceMs: Long): List<ForensicEvent> = events.filter { it.tsEpochMs >= sinceMs }.toList()
}
