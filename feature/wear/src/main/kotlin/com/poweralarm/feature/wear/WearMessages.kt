package com.poweralarm.feature.wear

object WearMessages {
    const val PATH_SET_ALARM = "/poweralarm/set"
    const val PATH_SNOOZE = "/poweralarm/snooze"
    const val PATH_DISMISS = "/poweralarm/dismiss"
    const val PATH_LIST_ALARMS = "/poweralarm/list"
}

data class WearAlarmMessage(val alarmId: Long, val token: String)
