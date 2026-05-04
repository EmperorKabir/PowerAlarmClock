package com.poweralarm.core.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val label: String,
    val hour: Int,
    val minute: Int,
    val enabled: Boolean,
    val profileId: String,
    val recurrenceJson: String,
    val conditionsJson: String,
    val dismissalRequirementsJson: String,
    val audioPlanJson: String,
    val snoozePolicyJson: String,
    val ringerLayoutJson: String,
    val automationJson: String,
    val metadataJson: String,
    val updatedAt: Long,
)
