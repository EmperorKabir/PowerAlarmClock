package com.poweralarm.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.poweralarm.app.shell.AppShell
import com.poweralarm.app.shell.Destination
import com.poweralarm.app.shell.MoreLanding
import com.poweralarm.app.shell.MorePage
import com.poweralarm.app.shell.TravelBanner
import com.poweralarm.app.vm.AlarmEditViewModel
import com.poweralarm.app.vm.AlarmListViewModel
import com.poweralarm.app.vm.ProfilesViewModel
import com.poweralarm.app.vm.StatisticsViewModel
import com.poweralarm.core.settings.SettingsRegistry
import com.poweralarm.core.settings.SettingsStore
import com.poweralarm.core.ui.theme.LocalThemeState
import com.poweralarm.core.ui.theme.PowerAlarmTheme
import com.poweralarm.core.ui.theme.ThemeRepository
import com.poweralarm.core.ui.theme.ThemeRole
import com.poweralarm.core.ui.theme.ThemeState
import com.poweralarm.feature.alarmedit.AlarmEditScreen
import com.poweralarm.feature.alarmlist.AlarmListScreen
import com.poweralarm.feature.onboarding.OemBatteryIntents
import com.poweralarm.feature.onboarding.OnboardingScreen
import com.poweralarm.feature.profiles.ProfilesScreen
import com.poweralarm.feature.settings.SettingsScreen
import com.poweralarm.feature.statistics.StatisticsScreen
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

    private val listVm: AlarmListViewModel by viewModels()
    private val editVm: AlarmEditViewModel by viewModels()
    private val statsVm: StatisticsViewModel by viewModels()
    private val profilesVm: ProfilesViewModel by viewModels()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by themeRepository.observe().collectAsState(initial = LocalThemeState.current)
            val window = calculateWindowSizeClass(this)
            val scope = androidx.compose.runtime.rememberCoroutineScope()

            val onboardingComplete by store.observe<Boolean>("hasCompletedOnboarding")
                .collectAsState(initial = true)
            val homeZone by store.observe<String>("alarmHomeTimezone")
                .collectAsState(initial = "Europe/London")
            val travelEnabled by store.observe<Boolean>("travelDetectionEnabled")
                .collectAsState(initial = true)

            PowerAlarmTheme(state = state) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (!onboardingComplete) {
                        OnboardingHost(
                            onFinish = { scope.launch { runCatching { store.set("hasCompletedOnboarding", true) } } },
                            scope = scope,
                        )
                    } else {
                        AppShell(windowSize = window) { destination, subPage, padding, onSub ->
                            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                                if (travelEnabled && destination == Destination.ALARMS && subPage == null) {
                                    var dismissed by remember { mutableStateOf(false) }
                                    if (!dismissed) {
                                        TravelBanner(
                                            homeZone = homeZone,
                                            onPin = { dismissed = true },
                                            onKeep = { dismissed = true },
                                            onDismiss = { dismissed = true },
                                        )
                                    }
                                }
                                Box(modifier = Modifier.fillMaxSize()) {
                                    DestinationContent(
                                        destination = destination,
                                        subPage = subPage,
                                        onSubPage = onSub,
                                        registry = registry,
                                        store = store,
                                        themeState = state,
                                        scope = scope,
                                        listVm = listVm,
                                        editVm = editVm,
                                        statsVm = statsVm,
                                        profilesVm = profilesVm,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingHost(onFinish: () -> Unit, scope: CoroutineScope) {
    val ctx = LocalContext.current
    OnboardingScreen(
        onRequestNotifications = {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching { ctx.startActivity(intent) }
        },
        onRequestExactAlarm = { OemBatteryIntents.launchExactAlarm(ctx) },
        onRequestBatteryExemption = { OemBatteryIntents.launch(ctx) },
        onConnectSpotify = { /* delegated to integrations:spotify – TODO post-onboarding */ },
        onConnectDrive = {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://drive.google.com")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching { ctx.startActivity(intent) }
        },
        onPickProfile = { /* persisted via store later when wired */ },
        onCreateFirstAlarm = { /* host launches add-alarm flow */ },
        onFinish = onFinish,
    )
}

@Suppress("LongParameterList")
@Composable
private fun DestinationContent(
    destination: Destination,
    subPage: MorePage?,
    onSubPage: (MorePage) -> Unit,
    registry: SettingsRegistry,
    store: SettingsStore,
    themeState: ThemeState,
    scope: CoroutineScope,
    listVm: AlarmListViewModel,
    editVm: AlarmEditViewModel,
    statsVm: StatisticsViewModel,
    profilesVm: ProfilesViewModel,
) {
    when (destination) {
        Destination.ALARMS -> AlarmsContent(listVm, editVm)
        Destination.INSIGHTS -> {
            val s by statsVm.state.collectAsState()
            StatisticsScreen(state = s)
        }
        Destination.MORE -> when (subPage) {
            null -> MoreLanding(onOpen = onSubPage)
            MorePage.PROFILES -> ProfilesPanel(profilesVm)
            MorePage.THEME -> ThemePanel(store, themeState, scope)
            MorePage.SETTINGS -> SettingsPanel(registry, store, scope)
            MorePage.ONBOARDING -> ResetOnboardingPanel(store, scope)
        }
    }
}

@Composable
private fun AlarmsContent(listVm: AlarmListViewModel, editVm: AlarmEditViewModel) {
    val alarms by listVm.alarms.collectAsState()
    val editing by editVm.editing.collectAsState()
    val current = editing
    if (current != null) {
        AlarmEditScreen(
            initial = current,
            onSave = { editVm.save(it) },
            onCancel = { editVm.cancel() },
        )
    } else {
        AlarmListScreen(
            alarms = alarms,
            onToggle = { id, enabled -> listVm.setEnabled(id, enabled) },
            onClick = { id -> editVm.startEdit(id) },
            onAdd = { editVm.startNew() },
        )
    }
}

@Composable
private fun ProfilesPanel(vm: ProfilesViewModel) {
    val s by vm.state.collectAsState()
    ProfilesScreen(
        profiles = s.profiles,
        initialActive = s.active,
        onActivate = { vm.activate(it) },
    )
}

@Composable
private fun ThemePanel(store: SettingsStore, themeState: ThemeState, scope: CoroutineScope) {
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

@Composable
private fun SettingsPanel(registry: SettingsRegistry, store: SettingsStore, scope: CoroutineScope) {
    val expert by store.observe<Boolean>("expertModeEnabled").collectAsState(initial = false)
    SettingsScreen(
        descriptors = registry.all(),
        onChange = { id, value -> scope.launch { runCatching { store.set(id, value) } } },
        initialExpertMode = expert,
    )
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun ResetOnboardingPanel(store: SettingsStore, scope: CoroutineScope) {
    androidx.compose.material3.Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { androidx.compose.material3.Text("Re-run onboarding") },
            )
        },
    ) { padding ->
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
        ) {
            androidx.compose.material3.Text(
                "Re-display the first-run wizard to redo permissions, music links and profile picking.",
            )
            androidx.compose.material3.Button(onClick = {
                scope.launch { runCatching { store.set("hasCompletedOnboarding", false) } }
            }) {
                androidx.compose.material3.Text("Restart onboarding now")
            }
        }
    }
}
