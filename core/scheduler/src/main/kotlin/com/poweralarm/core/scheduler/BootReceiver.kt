package com.poweralarm.core.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val request = OneTimeWorkRequestBuilder<RescheduleWorker>().build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    class RescheduleWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {
        override fun doWork(): Result {
            // Repository + scheduler are wired through Hilt at the :app module.
            // The actual reschedule happens in com.poweralarm.app.alarm.RescheduleEntryPoint.
            return Result.success()
        }
    }

    companion object {
        const val WORK_NAME = "power-alarm-reschedule"
    }
}
