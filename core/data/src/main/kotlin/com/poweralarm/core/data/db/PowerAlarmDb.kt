package com.poweralarm.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        AlarmEntity::class,
        DismissalEventEntity::class,
        RotationHistoryEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class PowerAlarmDb : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun dismissalEventDao(): DismissalEventDao
    abstract fun rotationHistoryDao(): RotationHistoryDao

    companion object {
        const val NAME = "power_alarm.db"
    }
}
