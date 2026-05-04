package com.poweralarm.core.ui.adaptive

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

enum class PaneMode { Compact, Medium, Expanded }

fun WindowSizeClass.paneMode(): PaneMode = when (widthSizeClass) {
    WindowWidthSizeClass.Compact -> PaneMode.Compact
    WindowWidthSizeClass.Medium -> PaneMode.Medium
    else -> PaneMode.Expanded
}

@Composable
fun PowerAlarmScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = content,
    )
}
