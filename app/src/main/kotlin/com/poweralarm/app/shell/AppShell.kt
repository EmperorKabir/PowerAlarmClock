package com.poweralarm.app.shell

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
 * What the host renders for a given selection.
 *  - [Destination] is one of the 3 primary tabs (Alarms / Insights / More)
 *  - [MorePage] is non-null only when the user has drilled into a page from the More tab,
 *    OR when the wider shell wants to render the page directly (Medium / Expanded show
 *    Profiles, Theme, Settings, Onboarding inline).
 */
@Composable
fun AppShell(
    windowSize: WindowSizeClass,
    content: @Composable (Destination, MorePage?, PaddingValues, (MorePage) -> Unit) -> Unit,
) {
    var current by remember { mutableStateOf(Destination.ALARMS) }
    var subPage by remember { mutableStateOf<MorePage?>(null) }

    val onSelect: (Destination) -> Unit = { current = it; subPage = null }
    val onSelectSub: (MorePage) -> Unit = {
        current = if (windowSize.widthSizeClass == WindowWidthSizeClass.Compact) Destination.MORE else current
        subPage = it
    }

    when (windowSize.widthSizeClass) {
        WindowWidthSizeClass.Compact -> CompactShell(current, subPage, onSelect, onSelectSub, content)
        WindowWidthSizeClass.Medium -> MediumShell(current, subPage, onSelect, onSelectSub, content)
        else -> ExpandedShell(current, subPage, onSelect, onSelectSub, content)
    }
}

@Composable
private fun CompactShell(
    current: Destination,
    subPage: MorePage?,
    onSelect: (Destination) -> Unit,
    onSelectSub: (MorePage) -> Unit,
    content: @Composable (Destination, MorePage?, PaddingValues, (MorePage) -> Unit) -> Unit,
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
    ) { padding -> content(current, subPage, padding, onSelectSub) }
}

@Composable
private fun MediumShell(
    current: Destination,
    subPage: MorePage?,
    onSelect: (Destination) -> Unit,
    onSelectSub: (MorePage) -> Unit,
    content: @Composable (Destination, MorePage?, PaddingValues, (MorePage) -> Unit) -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        NavigationRail {
            Destination.entries.filter { it != Destination.MORE }.forEach { dest ->
                NavigationRailItem(
                    selected = current == dest && subPage == null,
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
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            MorePage.entries.forEach { page ->
                NavigationRailItem(
                    selected = subPage == page,
                    onClick = { onSelectSub(page) },
                    icon = { Text(page.emoji, style = MaterialTheme.typography.titleMedium) },
                    label = { Text(page.label) },
                )
            }
        }
        Scaffold { padding -> content(current, subPage, padding, onSelectSub) }
    }
}

@Composable
private fun ExpandedShell(
    current: Destination,
    subPage: MorePage?,
    onSelect: (Destination) -> Unit,
    onSelectSub: (MorePage) -> Unit,
    content: @Composable (Destination, MorePage?, PaddingValues, (MorePage) -> Unit) -> Unit,
) {
    PermanentNavigationDrawer(
        drawerContent = {
            PermanentDrawerSheet(modifier = Modifier.padding(end = 8.dp)) {
                Destination.entries.filter { it != Destination.MORE }.forEach { dest ->
                    NavigationDrawerItem(
                        selected = current == dest && subPage == null,
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
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp))
                MorePage.entries.forEach { page ->
                    NavigationDrawerItem(
                        selected = subPage == page,
                        onClick = { onSelectSub(page) },
                        icon = { Text(page.emoji, style = MaterialTheme.typography.titleMedium) },
                        label = { Text(page.label) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                }
            }
        },
    ) {
        Scaffold { padding -> content(current, subPage, padding, onSelectSub) }
    }
}
