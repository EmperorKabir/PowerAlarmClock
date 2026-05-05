package com.poweralarm.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        AlarmEntity::class,
        DismissalEventEntity::class,
        RotationHistoryEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class PowerAlarmDb : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun dismissalEventDao(): DismissalEventDao
    abstract fun rotationHistoryDao(): RotationHistoryDao

    companion object {
        const val NAME = "power_alarm.db"

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE alarms ADD COLUMN timezoneMode TEXT NOT NULL DEFAULT 'device'")
                db.execSQL("ALTER TABLE alarms ADD COLUMN timezoneId TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
