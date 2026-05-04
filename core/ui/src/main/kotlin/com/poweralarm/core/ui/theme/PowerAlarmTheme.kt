package com.poweralarm.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Default palette is teal-on-black per spec §6.
 * All values overridable at runtime via [ThemeRepository]; this stub will be replaced
 * by the registry-driven implementation in Phase 2.
 */
object PowerAlarmDefaults {
    val Primary: Color = Color(0xFF00C2B8)
    val Secondary: Color = Color(0xFF0F4C4A)
    val Surface: Color = Color(0xFF000000)
    val Background: Color = Color(0xFF000000)
    val OnPrimary: Color = Color(0xFF000000)
    val OnSurface: Color = Color(0xFFE0F2F1)
    val Error: Color = Color(0xFFFF5252)
}

@Composable
fun PowerAlarmTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = PowerAlarmDefaults.Primary,
            secondary = PowerAlarmDefaults.Secondary,
            surface = PowerAlarmDefaults.Surface,
            background = PowerAlarmDefaults.Background,
            onPrimary = PowerAlarmDefaults.OnPrimary,
            onSurface = PowerAlarmDefaults.OnSurface,
            error = PowerAlarmDefaults.Error,
        )
    } else {
        lightColorScheme(
            primary = PowerAlarmDefaults.Primary,
            secondary = PowerAlarmDefaults.Secondary,
        )
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
