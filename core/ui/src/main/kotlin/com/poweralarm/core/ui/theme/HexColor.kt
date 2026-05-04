package com.poweralarm.core.ui.theme

import androidx.compose.ui.graphics.Color

object HexColor {
    private val PATTERN = Regex("^#([0-9A-Fa-f]{6}|[0-9A-Fa-f]{8})$")

    fun parse(hex: String, fallback: Color = Color.Black): Color {
        if (!PATTERN.matches(hex)) return fallback
        val raw = hex.removePrefix("#")
        val argb = when (raw.length) {
            6 -> 0xFF_00_00_00.toInt() or raw.toInt(16)
            8 -> raw.toLong(16).toInt()
            else -> return fallback
        }
        return Color(argb)
    }

    fun isValid(hex: String): Boolean = PATTERN.matches(hex)
}
