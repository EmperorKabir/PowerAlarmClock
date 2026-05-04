package com.poweralarm.integrations.tasker

import android.content.Context
import android.content.Intent

/**
 * Broadcasts intents to Tasker (or any user-configured action). Each entry in
 * [actions] is a fully-qualified Android action string; [variables] are added as
 * extras for Tasker's `%PARn`-style consumption.
 */
class TaskerBroadcaster(private val context: Context) {

    fun broadcast(actions: List<String>, variables: Map<String, String> = emptyMap()) {
        actions.forEach { action ->
            val intent = Intent(action).apply {
                variables.forEach { (k, v) -> putExtra(k, v) }
            }
            context.sendBroadcast(intent)
        }
    }
}
