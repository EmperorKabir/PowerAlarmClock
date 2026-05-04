package com.poweralarm.feature.ringer

import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.poweralarm.core.ui.theme.PowerAlarmTheme

class RingerActivity : ComponentActivity() {

    private var disableVolume = false
    private var disablePower = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyShowOverKeyguardFlags()
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        setContent {
            PowerAlarmTheme {
                RingerScreen(alarmId = alarmId)
            }
        }
    }

    fun configureButtonOverrides(disableVolumeButtons: Boolean, disablePowerButton: Boolean) {
        disableVolume = disableVolumeButtons
        disablePower = disablePowerButton
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val swallow = when (event.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_MUTE -> disableVolume
            KeyEvent.KEYCODE_POWER -> disablePower
            else -> false
        }
        return if (swallow) true else super.dispatchKeyEvent(event)
    }

    private fun applyShowOverKeyguardFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            )
        }
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
        )
    }

    companion object {
        const val EXTRA_ALARM_ID = "alarmId"
    }
}
