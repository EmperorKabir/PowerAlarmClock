package com.poweralarm.app.shell

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Adaptive shell. Switches the primary nav surface based on [windowSize]:
 *   compact  → bottom NavigationBar
 *   medium   → side NavigationRail
 *   expanded → PermanentNavigationDrawer (always-on side panel)
 *
 * Each destination's content is provided by the caller as a slot keyed by the
 * [Destination] enum, so we don't need a NavController for top-level switching.
 */
@Composable
fun AppShell(
    windowSize: WindowSizeClass,
    content: @Composable (Destination, PaddingValues) -> Unit,
) {
    var current by remember { mutableStateOf(Destination.ALARMS) }

    when (windowSize.widthSizeClass) {
        WindowWidthSizeClass.Compact -> CompactShell(current, { current = it }, content)
        WindowWidthSizeClass.Medium -> MediumShell(current, { current = it }, content)
        else -> ExpandedShell(current, { current = it }, content)
    }
}

@Composable
private fun CompactShell(
    current: Destination,
    onSelect: (Destination) -> Unit,
    content: @Composable (Destination, PaddingValues) -> Unit,
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                Destination.entries.forEach { dest ->
                    NavigationBarItem(
                        selected = current == dest,
                        onClick = { onSelect(dest) },
                        icon = {
                            Icon(
                                imageVector = if (current == dest) dest.selected else dest.unselected,
                                contentDescription = dest.label,
                            )
                        },
                        label = { Text(dest.label, style = MaterialTheme.typography.labelMedium) },
                    )
                }
            }
        },
    ) { padding -> content(current, padding) }
}

@Composable
private fun MediumShell(
    current: Destination,
    onSelect: (Destination) -> Unit,
    content: @Composable (Destination, PaddingValues) -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        NavigationRail {
            Destination.entries.forEach { dest ->
                NavigationRailItem(
                    selected = current == dest,
                    onClick = { onSelect(dest) },
                    icon = {
                        Icon(
                            imageVector = if (current == dest) dest.selected else dest.unselected,
                            contentDescription = dest.label,
                        )
                    },
                    label = { Text(dest.label) },
                )
            }
        }
        Scaffold { padding -> content(current, padding) }
    }
}

@Composable
private fun ExpandedShell(
    current: Destination,
    onSelect: (Destination) -> Unit,
    content: @Composable (Destination, PaddingValues) -> Unit,
) {
    PermanentNavigationDrawer(
        drawerContent = {
            PermanentDrawerSheet(modifier = Modifier.padding(end = 8.dp)) {
                Destination.entries.forEach { dest ->
                    NavigationDrawerItem(
                        selected = current == dest,
                        onClick = { onSelect(dest) },
                        icon = {
                            Icon(
                                imageVector = if (current == dest) dest.selected else dest.unselected,
                                contentDescription = dest.label,
                            )
                        },
                        label = { Text(dest.label) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                }
            }
        },
    ) {
        Scaffold { padding -> content(current, padding) }
    }
}

@Suppress("UnusedPrivateMember")
private val DRAWER_VAL_UNUSED = DrawerValue.Closed
@Suppress("UnusedPrivateMember")
private val MODAL_DRAWER_UNUSED: @Composable () -> Unit = { ModalDrawerSheet { } }
