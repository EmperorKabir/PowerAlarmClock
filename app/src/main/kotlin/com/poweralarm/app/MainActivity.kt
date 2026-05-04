package com.poweralarm.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.poweralarm.app.nav.AppNavHost
import com.poweralarm.app.nav.Routes
import com.poweralarm.core.domain.model.Alarm
import com.poweralarm.core.ui.theme.LocalThemeState
import com.poweralarm.core.ui.theme.PowerAlarmTheme
import com.poweralarm.core.ui.theme.ThemeRepository
import com.poweralarm.feature.alarmedit.AlarmEditScreen
import com.poweralarm.feature.alarmlist.AlarmListScreen
import com.poweralarm.feature.settings.SettingsScreen
import com.poweralarm.feature.statistics.StatisticsScreen
import com.poweralarm.feature.statistics.StatisticsState
import com.poweralarm.feature.themes.ThemeEditor
import com.poweralarm.core.settings.SettingsRegistry
import com.poweralarm.core.settings.SettingsStore
import com.poweralarm.core.ui.theme.ThemeRole
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var themeRepository: ThemeRepository
    @Inject lateinit var registry: SettingsRegistry
    @Inject lateinit var store: SettingsStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by themeRepository.observe().collectAsState(initial = LocalThemeState.current)
            PowerAlarmTheme(state = state) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppContent(registry, store, themeRepository)
                }
            }
        }
    }
}

@Composable
private fun AppContent(
    registry: SettingsRegistry,
    store: SettingsStore,
    themeRepository: ThemeRepository,
) {
    AppNavHost(
        listScreen = { nav ->
            AlarmListScreen(
                alarms = emptyList(),
                onToggle = { _, _ -> },
                onClick = { id -> nav.navigate(Routes.EDIT.replace("{id}", id.toString())) },
                onAdd = { nav.navigate(Routes.EDIT.replace("{id}", "0")) },
            )
        },
        editScreen = { _, id -> AlarmEditScreen(initial = Alarm(id = id, hour = 7, minute = 0), onSave = {}) },
        settingsScreen = { SettingsScreen(descriptors = registry.all(), onChange = { _, _ -> }) },
        themesScreen = {
            ThemeEditor(
                initial = ThemeRole.values().associateWith { "#000000" },
                typographyFamily = "Inter",
                onColorChanged = { _, _ -> },
                onTypographyChanged = {},
            )
        },
        statsScreen = {
            StatisticsScreen(
                state = StatisticsState(
                    totalAlarmsThisWeek = 0,
                    avgSecondsToDismiss = 0,
                    mostUsedRequirement = null,
                    rows = emptyList(),
                ),
            )
        },
    )
}
