package com.poweralarm.core.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L)
        if (alarmId < 0) return

        val ringerService = Intent("com.poweralarm.action.RING").apply {
            setPackage(context.packageName)
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
        }
        context.startForegroundService(ringerService)
    }
}
