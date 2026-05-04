package com.poweralarm.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.poweralarm.app.shell.AppShell
import com.poweralarm.app.shell.Destination
import com.poweralarm.core.domain.model.Alarm
import com.poweralarm.core.settings.SettingsRegistry
import com.poweralarm.core.settings.SettingsStore
import com.poweralarm.core.ui.theme.LocalThemeState
import com.poweralarm.core.ui.theme.PowerAlarmTheme
import com.poweralarm.core.ui.theme.ThemeRepository
import com.poweralarm.core.ui.theme.ThemeRole
import com.poweralarm.core.ui.theme.ThemeState
import com.poweralarm.feature.alarmedit.AlarmEditScreen
import com.poweralarm.feature.alarmlist.AlarmListScreen
import com.poweralarm.feature.profiles.ProfilesScreen
import com.poweralarm.feature.settings.SettingsScreen
import com.poweralarm.feature.statistics.StatisticsScreen
import com.poweralarm.feature.statistics.StatisticsState
import com.poweralarm.feature.themes.ThemeEditor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var themeRepository: ThemeRepository
    @Inject lateinit var registry: SettingsRegistry
    @Inject lateinit var store: SettingsStore

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by themeRepository.observe().collectAsState(initial = LocalThemeState.current)
            val window = calculateWindowSizeClass(this)
            val scope = rememberCoroutineScope()
            PowerAlarmTheme(state = state) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppShell(windowSize = window) { destination, padding ->
                        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                            DestinationContent(
                                destination = destination,
                                registry = registry,
                                store = store,
                                themeState = state,
                                scope = scope,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DestinationContent(
    destination: Destination,
    registry: SettingsRegistry,
    store: SettingsStore,
    themeState: ThemeState,
    scope: CoroutineScope,
) {
    var editingAlarm by remember { mutableStateOf<Alarm?>(null) }

    when (destination) {
        Destination.ALARMS -> {
            val current = editingAlarm
            if (current != null) {
                AlarmEditScreen(
                    initial = current,
                    onSave = { editingAlarm = null },
                    onCancel = { editingAlarm = null },
                )
            } else {
                AlarmListScreen(
                    alarms = emptyList(),
                    onToggle = { _, _ -> },
                    onClick = { id -> editingAlarm = Alarm(id = id, hour = 7, minute = 0) },
                    onAdd = { editingAlarm = Alarm(id = 0, hour = 7, minute = 0) },
                )
            }
        }
        Destination.PROFILES -> ProfilesScreen()
        Destination.INSIGHTS -> StatisticsScreen(
            state = StatisticsState(
                totalAlarmsThisWeek = 0,
                avgSecondsToDismiss = 0,
                mostUsedRequirement = null,
                rows = emptyList(),
            ),
        )
        Destination.SETTINGS -> SettingsScreen(
            descriptors = registry.all(),
            onChange = { id, value ->
                scope.launch { runCatching { store.set(id, value) } }
            },
        )
        Destination.THEME -> {
            val initialColors = mapOf(
                ThemeRole.PRIMARY to themeState.primaryHex,
                ThemeRole.SECONDARY to themeState.secondaryHex,
                ThemeRole.TERTIARY to themeState.tertiaryHex,
                ThemeRole.SURFACE to themeState.surfaceHex,
                ThemeRole.BACKGROUND to themeState.backgroundHex,
                ThemeRole.ERROR to themeState.errorHex,
                ThemeRole.ON_PRIMARY to themeState.onPrimaryHex,
                ThemeRole.ON_SURFACE to themeState.onSurfaceHex,
            )
            ThemeEditor(
                initial = initialColors,
                typographyFamily = themeState.typographyFamily,
                onColorChanged = { role, hex ->
                    scope.launch { runCatching { store.set(role.descriptorId, hex) } }
                },
                onTypographyChanged = { fam ->
                    scope.launch { runCatching { store.set("typographyFamily", fam) } }
                },
            )
        }
    }
}
