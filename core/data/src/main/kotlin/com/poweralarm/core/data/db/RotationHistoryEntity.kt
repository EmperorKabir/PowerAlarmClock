package com.poweralarm.core.data.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "rotation_history")
data class RotationHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val poolId: String,
    val toneUri: String,
    val playedAt: Long,
)

@Dao
interface RotationHistoryDao {
    @Insert
    suspend fun insert(entity: RotationHistoryEntity)

    @Query("SELECT toneUri FROM rotation_history WHERE poolId = :poolId AND playedAt >= :since ORDER BY playedAt DESC")
    suspend fun playedSince(poolId: String, since: Long): List<String>
}
