package com.poweralarm.core.data.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "dismissal_events")
data class DismissalEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val alarmId: Long,
    val firedAt: Long,
    val dismissedAt: Long?,
    val snoozeCount: Int,
    val requirementsCompletedJson: String,
    val motionMs2: Float?,
    val locationLat: Float?,
    val locationLng: Float?,
    val weatherSummary: String?,
    val trafficDelaySec: Int?,
)

@Dao
interface DismissalEventDao {
    @Insert
    suspend fun insert(event: DismissalEventEntity): Long

    @Query("SELECT * FROM dismissal_events ORDER BY firedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<DismissalEventEntity>>

    @Query("SELECT * FROM dismissal_events WHERE firedAt >= :since ORDER BY firedAt")
    suspend fun since(since: Long): List<DismissalEventEntity>
}
