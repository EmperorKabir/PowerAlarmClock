package com.poweralarm.app.shell

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.AccessAlarm
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Top-level destinations shown in the primary nav surface (bottom-bar / rail / drawer).
 * Sub-pages (Profiles / Theme / Settings / Onboarding) are reachable via the More tab
 * on compact devices, or rendered as section groups on Medium/Expanded shells.
 */
enum class Destination(
    val route: String,
    val label: String,
    val selected: ImageVector,
    val unselected: ImageVector,
) {
    ALARMS("alarms", "Alarms", Icons.Filled.AccessAlarm, Icons.Outlined.AccessAlarm),
    INSIGHTS("insights", "Insights", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    MORE("more", "More", Icons.Filled.Menu, Icons.Outlined.Menu),
    ;

    companion object {
        fun fromRoute(route: String?): Destination = entries.firstOrNull { route?.startsWith(it.route) == true } ?: ALARMS
    }
}

/**
 * Pages within the More tab — surfaced as cards on compact, listed alongside top-level
 * destinations on rail/drawer shells so power users on big screens get them one click away.
 */
enum class MorePage(val label: String, val tagline: String, val emoji: String) {
    PROFILES("Profiles", "Default · Work · Holiday · Travel", "👥"),
    THEME("Theme", "Colours, fonts, dark mode, Material You", "🎨"),
    SETTINGS("Settings", "Every preference, plain English", "⚙️"),
    ONBOARDING("Onboarding", "Re-run the first-time setup wizard", "🚀"),
}
