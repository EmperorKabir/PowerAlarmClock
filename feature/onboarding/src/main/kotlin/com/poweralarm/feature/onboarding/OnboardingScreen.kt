package com.poweralarm.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessAlarm
import androidx.compose.material.icons.outlined.BatteryStd
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * 5-page first-run wizard. The host app passes lambdas for permission requests
 * and the final-profile pick so this module stays independent of Hilt + repos.
 */
@Composable
fun OnboardingScreen(
    onRequestNotifications: () -> Unit = {},
    onRequestExactAlarm: () -> Unit = {},
    onRequestBatteryExemption: () -> Unit = {},
    onConnectSpotify: () -> Unit = {},
    onConnectDrive: () -> Unit = {},
    onPickProfile: (String) -> Unit = {},
    onCreateFirstAlarm: () -> Unit = {},
    onFinish: () -> Unit = {},
) {
    val pages = listOf(
        OnboardingPage.Welcome,
        OnboardingPage.Notifications,
        OnboardingPage.Reliability,
        OnboardingPage.Connect,
        OnboardingPage.Profile,
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    var profile by remember { mutableStateOf("default") }

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                pages.indices.forEach { i ->
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == i) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == i) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline,
                            )
                            .padding(end = 4.dp),
                    )
                    Box(modifier = Modifier.size(8.dp))
                }
                Box(modifier = Modifier.weight(1f))
                if (pagerState.currentPage < pages.lastIndex) {
                    OutlinedButton(onClick = onFinish) { Text("Skip") }
                    Box(modifier = Modifier.size(8.dp))
                    Button(onClick = {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }) { Text("Next") }
                } else {
                    ExtendedFloatingActionButton(
                        onClick = {
                            onPickProfile(profile)
                            onCreateFirstAlarm()
                            onFinish()
                        },
                        text = { Text("Get started") },
                        icon = { Icon(Icons.Outlined.AccessAlarm, null) },
                    )
                }
            }
        },
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) { idx ->
            when (pages[idx]) {
                OnboardingPage.Welcome -> WelcomePage()
                OnboardingPage.Notifications -> PermissionPage(
                    icon = Icons.Outlined.Notifications,
                    title = "Stay reachable while you sleep",
                    body = "Power Alarm Clock needs to post notifications and take over the screen when an alarm fires. Without these, alarms can be silenced by the system.",
                    primaryCta = "Allow notifications",
                    onPrimary = onRequestNotifications,
                )
                OnboardingPage.Reliability -> ReliabilityPage(
                    onRequestExact = onRequestExactAlarm,
                    onRequestBattery = onRequestBatteryExemption,
                )
                OnboardingPage.Connect -> ConnectPage(
                    onSpotify = onConnectSpotify,
                    onDrive = onConnectDrive,
                )
                OnboardingPage.Profile -> ProfilePage(
                    selected = profile,
                    onSelect = { profile = it },
                )
            }
        }
    }
}

private enum class OnboardingPage { Welcome, Notifications, Reliability, Connect, Profile }

@Composable
private fun WelcomePage() {
    PageScaffold(icon = Icons.Outlined.AccessAlarm, title = "Welcome to Power Alarm Clock") {
        Text(
            "An alarm clock that actually wakes you up.",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            "• Wake earlier when traffic, weather or air quality demand it\n" +
                "• Solve a problem, scan a tag, or take a selfie before you can dismiss\n" +
                "• Use your Spotify, Google Drive, or local sounds\n" +
                "• Travel? Pin alarms to your home zone or follow the local clock\n" +
                "• Tablets, foldables, Wear OS — it adapts.",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun PermissionPage(
    icon: ImageVector,
    title: String,
    body: String,
    primaryCta: String,
    onPrimary: () -> Unit,
) {
    PageScaffold(icon = icon, title = title) {
        Text(body, style = MaterialTheme.typography.bodyLarge)
        Button(onClick = onPrimary) { Text(primaryCta) }
    }
}

@Composable
private fun ReliabilityPage(onRequestExact: () -> Unit, onRequestBattery: () -> Unit) {
    PageScaffold(icon = Icons.Outlined.BatteryStd, title = "Make sure alarms always fire") {
        Text(
            "Some Android phones aggressively kill background apps. To make sure your alarm rings on time, allow these permissions:",
            style = MaterialTheme.typography.bodyLarge,
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Schedule exact alarms", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "Required so alarms fire to-the-minute. Android 12+ asks per-app.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Button(onClick = onRequestExact) { Text("Open exact-alarm settings") }
            }
        }
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Disable battery optimisation", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "Stops your OEM (Samsung, Xiaomi, Oppo, Huawei, OnePlus…) from killing the alarm scheduler in the background.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Button(onClick = onRequestBattery) { Text("Open battery settings") }
            }
        }
    }
}

@Composable
private fun ConnectPage(onSpotify: () -> Unit, onDrive: () -> Unit) {
    PageScaffold(icon = Icons.Outlined.CloudQueue, title = "Optional: connect your music") {
        Text(
            "Wake up to a Spotify playlist or a track from Google Drive. Both are optional — you can skip and add them later.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            "Drive access is restricted to files you explicitly pick — the app never sees your whole drive.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedButton(onClick = onSpotify, modifier = Modifier.fillMaxWidth()) { Text("Connect Spotify") }
        OutlinedButton(onClick = onDrive, modifier = Modifier.fillMaxWidth()) { Text("Connect Google Drive") }
    }
}

@Composable
private fun ProfilePage(selected: String, onSelect: (String) -> Unit) {
    PageScaffold(icon = Icons.Outlined.PeopleAlt, title = "Pick a profile to start with") {
        Text(
            "Profiles let you swap whole sets of alarms in one tap — useful for work / weekends / travel. You can change this any time.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "default" to "🌅 Default — work week, fixed wake time",
                "work" to "💼 Work — calendar-aware, traffic-shifted",
                "holiday" to "🏖️ Holiday — alarms off but failsafes on",
                "travel" to "✈️ Travel — pinned to home zone, geofence-aware",
            ).forEach { (id, label) ->
                FilterChip(
                    selected = selected == id,
                    onClick = { onSelect(id) },
                    label = { Text(label) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun PageScaffold(icon: ImageVector, title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier.size(72.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(36.dp))
        }
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        content()
    }
}
