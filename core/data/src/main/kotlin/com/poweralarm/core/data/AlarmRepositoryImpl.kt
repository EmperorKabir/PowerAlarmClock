package com.poweralarm.core.data

import com.poweralarm.core.data.db.AlarmDao
import com.poweralarm.core.data.db.AlarmEntity
import com.poweralarm.core.domain.model.Alarm
import com.poweralarm.core.domain.model.AudioPlan
import com.poweralarm.core.domain.model.AutomationHooks
import com.poweralarm.core.domain.model.Condition
import com.poweralarm.core.domain.model.DismissalRequirement
import com.poweralarm.core.domain.model.DomainJson
import com.poweralarm.core.domain.model.Recurrence
import com.poweralarm.core.domain.model.RingerLayoutPolicy
import com.poweralarm.core.domain.model.SnoozePolicy
import com.poweralarm.core.domain.model.TimezoneMode
import com.poweralarm.core.domain.port.AlarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer

class AlarmRepositoryImpl(private val dao: AlarmDao) : AlarmRepository {

    override fun observeAll(): Flow<List<Alarm>> = dao.observeAll().map { it.map(::toDomain) }
    override suspend fun all(): List<Alarm> = dao.all().map(::toDomain)
    override suspend fun byId(id: Long): Alarm? = dao.byId(id)?.let(::toDomain)

    override suspend fun save(alarm: Alarm): Long {
        val now = System.currentTimeMillis()
        val entity = toEntity(alarm, now)
        return if (alarm.id == 0L) dao.insert(entity) else { dao.update(entity); alarm.id }
    }

    override suspend fun delete(id: Long) = dao.deleteById(id)
    override suspend fun setEnabled(id: Long, enabled: Boolean) = dao.setEnabled(id, enabled, System.currentTimeMillis())

    private fun toDomain(e: AlarmEntity): Alarm = Alarm(
        id = e.id,
        label = e.label,
        hour = e.hour,
        minute = e.minute,
        timezoneMode = if (e.timezoneMode == "fixed") TimezoneMode.Fixed else TimezoneMode.Device,
        timezoneId = e.timezoneId,
        enabled = e.enabled,
        profileId = e.profileId,
        recurrence = DomainJson.decodeFromString(Recurrence.serializer(), e.recurrenceJson),
        conditions = DomainJson.decodeFromString(ListSerializer(Condition.serializer()), e.conditionsJson),
        dismissalRequirements = DomainJson.decodeFromString(
            ListSerializer(DismissalRequirement.serializer()),
            e.dismissalRequirementsJson,
        ),
        audioPlan = DomainJson.decodeFromString(AudioPlan.serializer(), e.audioPlanJson),
        snoozePolicy = DomainJson.decodeFromString(SnoozePolicy.serializer(), e.snoozePolicyJson),
        ringerLayout = DomainJson.decodeFromString(RingerLayoutPolicy.serializer(), e.ringerLayoutJson),
        automation = DomainJson.decodeFromString(AutomationHooks.serializer(), e.automationJson),
        metadataJson = e.metadataJson,
    )

    private fun toEntity(a: Alarm, updatedAt: Long): AlarmEntity = AlarmEntity(
        id = a.id,
        label = a.label,
        hour = a.hour,
        minute = a.minute,
        timezoneMode = if (a.timezoneMode is TimezoneMode.Fixed) "fixed" else "device",
        timezoneId = a.timezoneId,
        enabled = a.enabled,
        profileId = a.profileId,
        recurrenceJson = DomainJson.encodeToString(Recurrence.serializer(), a.recurrence),
        conditionsJson = DomainJson.encodeToString(ListSerializer(Condition.serializer()), a.conditions),
        dismissalRequirementsJson = DomainJson.encodeToString(
            ListSerializer(DismissalRequirement.serializer()),
            a.dismissalRequirements,
        ),
        audioPlanJson = DomainJson.encodeToString(AudioPlan.serializer(), a.audioPlan),
        snoozePolicyJson = DomainJson.encodeToString(SnoozePolicy.serializer(), a.snoozePolicy),
        ringerLayoutJson = DomainJson.encodeToString(RingerLayoutPolicy.serializer(), a.ringerLayout),
        automationJson = DomainJson.encodeToString(AutomationHooks.serializer(), a.automation),
        metadataJson = a.metadataJson,
        updatedAt = updatedAt,
    )
}
