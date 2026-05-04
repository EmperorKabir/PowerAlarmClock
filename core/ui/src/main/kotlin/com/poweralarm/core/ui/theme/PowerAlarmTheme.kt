package com.poweralarm.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object PowerAlarmDefaults {
    val Primary: Color = Color(0xFF00C2B8)
    val Secondary: Color = Color(0xFF0F4C4A)
    val Tertiary: Color = Color(0xFF26A69A)
    val Surface: Color = Color(0xFF000000)
    val Background: Color = Color(0xFF000000)
    val OnPrimary: Color = Color(0xFF000000)
    val OnSurface: Color = Color(0xFFE0F2F1)
    val Error: Color = Color(0xFFFF5252)
}

val LocalThemeState = staticCompositionLocalOf {
    ThemeState(
        primaryHex = "#00C2B8",
        secondaryHex = "#0F4C4A",
        tertiaryHex = "#26A69A",
        surfaceHex = "#000000",
        backgroundHex = "#000000",
        errorHex = "#FF5252",
        onPrimaryHex = "#000000",
        onSurfaceHex = "#E0F2F1",
        typographyFamily = "Inter",
        cornerRadiusDp = 16,
        densityScale = 1f,
        motionDurationMs = 300,
        darkModeAuto = true,
        useDynamicColor = false,
    )
}

@Composable
fun PowerAlarmTheme(
    state: ThemeState = LocalThemeState.current,
    darkTheme: Boolean = if (state.darkModeAuto) isSystemInDarkTheme() else true,
    content: @Composable () -> Unit,
) {
    val primary = HexColor.parse(state.primaryHex, PowerAlarmDefaults.Primary)
    val secondary = HexColor.parse(state.secondaryHex, PowerAlarmDefaults.Secondary)
    val tertiary = HexColor.parse(state.tertiaryHex, PowerAlarmDefaults.Tertiary)
    val surface = HexColor.parse(state.surfaceHex, PowerAlarmDefaults.Surface)
    val background = HexColor.parse(state.backgroundHex, PowerAlarmDefaults.Background)
    val error = HexColor.parse(state.errorHex, PowerAlarmDefaults.Error)
    val onPrimary = HexColor.parse(state.onPrimaryHex, PowerAlarmDefaults.OnPrimary)
    val onSurface = HexColor.parse(state.onSurfaceHex, PowerAlarmDefaults.OnSurface)

    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = primary,
            secondary = secondary,
            tertiary = tertiary,
            surface = surface,
            background = background,
            error = error,
            onPrimary = onPrimary,
            onSurface = onSurface,
        )
    } else {
        lightColorScheme(primary = primary, secondary = secondary, tertiary = tertiary)
    }

    val shapes = Shapes(
        extraSmall = RoundedCornerShape((state.cornerRadiusDp / 4).dp),
        small = RoundedCornerShape((state.cornerRadiusDp / 2).dp),
        medium = RoundedCornerShape(state.cornerRadiusDp.dp),
        large = RoundedCornerShape((state.cornerRadiusDp * 1.5).dp),
        extraLarge = RoundedCornerShape((state.cornerRadiusDp * 2).dp),
    )

    CompositionLocalProvider(LocalThemeState provides state) {
        MaterialTheme(colorScheme = scheme, shapes = shapes, typography = Typography(), content = content)
    }
}
