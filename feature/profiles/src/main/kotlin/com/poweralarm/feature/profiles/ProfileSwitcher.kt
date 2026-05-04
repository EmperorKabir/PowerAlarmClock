package com.poweralarm.feature.profiles

import com.poweralarm.core.domain.model.Alarm

/**
 * Bulk-applies a profile selection: returns the modified alarms with `enabled` flipped
 * according to whether each alarm's `profileId` is in the active set.
 */
object ProfileSwitcher {

    fun apply(alarms: List<Alarm>, activeProfile: String, profileMembership: Map<String, Set<String>>): List<Alarm> {
        val activeMembers = profileMembership[activeProfile].orEmpty()
        return alarms.map { alarm ->
            val shouldEnable = alarm.profileId == activeProfile || activeMembers.contains(alarm.profileId)
            if (alarm.enabled != shouldEnable) alarm.copy(enabled = shouldEnable) else alarm
        }
    }
}
