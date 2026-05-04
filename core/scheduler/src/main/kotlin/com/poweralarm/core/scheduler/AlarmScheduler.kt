package com.poweralarm.core.scheduler

import android.app.AlarmManager
import android.app.AlarmManager.AlarmClockInfo
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.poweralarm.core.domain.model.Alarm
import java.time.ZonedDateTime

class AlarmScheduler(
    private val context: Context,
    private val calculator: NextFireCalculator = NextFireCalculator(),
) {
    private val am: AlarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(alarm: Alarm, ctx: ConditionContext = ConditionContext()): ZonedDateTime? {
        val next = calculator.nextFire(alarm, ctx) ?: run { cancel(alarm.id); return null }
        val firePi = firePendingIntent(alarm.id)
        val showPi = showPendingIntent(alarm.id)
        am.setAlarmClock(AlarmClockInfo(next.toInstant().toEpochMilli(), showPi), firePi)
        return next
    }

    fun cancel(alarmId: Long) {
        am.cancel(firePendingIntent(alarmId))
    }

    private fun firePendingIntent(alarmId: Long): PendingIntent {
        val intent = Intent(ACTION_FIRE).apply {
            setPackage(context.packageName)
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
        return PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun showPendingIntent(alarmId: Long): PendingIntent {
        val intent = Intent(ACTION_SHOW).apply {
            setPackage(context.packageName)
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
        return PendingIntent.getActivity(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val ACTION_FIRE = "com.poweralarm.action.FIRE"
        const val ACTION_SHOW = "com.poweralarm.action.SHOW"
        const val EXTRA_ALARM_ID = "alarmId"
    }
}
