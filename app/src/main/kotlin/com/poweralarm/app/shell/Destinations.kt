package com.poweralarm.app.shell

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccessAlarm
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class Destination(
    val route: String,
    val label: String,
    val selected: ImageVector,
    val unselected: ImageVector,
) {
    ALARMS("alarms", "Alarms", Icons.Filled.AccessAlarm, Icons.Outlined.AccessAlarm),
    PROFILES("profiles", "Profiles", Icons.Filled.PeopleAlt, Icons.Outlined.PeopleAlt),
    INSIGHTS("insights", "Insights", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    SETTINGS("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
    THEME("theme", "Theme", Icons.Filled.Palette, Icons.Outlined.Palette),
    ;

    companion object {
        fun fromRoute(route: String?): Destination = entries.firstOrNull { route?.startsWith(it.route) == true } ?: ALARMS
    }
}
