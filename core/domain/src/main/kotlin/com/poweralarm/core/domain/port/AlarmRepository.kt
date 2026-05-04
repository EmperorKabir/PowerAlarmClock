package com.poweralarm.core.domain.port

import com.poweralarm.core.domain.model.Alarm
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    fun observeAll(): Flow<List<Alarm>>
    suspend fun all(): List<Alarm>
    suspend fun byId(id: Long): Alarm?
    suspend fun save(alarm: Alarm): Long
    suspend fun delete(id: Long)
    suspend fun setEnabled(id: Long, enabled: Boolean)
}
