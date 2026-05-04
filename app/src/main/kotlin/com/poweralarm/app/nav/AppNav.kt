package com.poweralarm.app.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

object Routes {
    const val LIST = "alarms"
    const val EDIT = "alarms/edit/{id}"
    const val SETTINGS = "settings"
    const val THEMES = "themes"
    const val STATS = "stats"
    const val PROFILES = "profiles"
}

@Composable
fun AppNavHost(
    nav: NavHostController = rememberNavController(),
    listScreen: @Composable (NavHostController) -> Unit,
    editScreen: @Composable (NavHostController, Long) -> Unit,
    settingsScreen: @Composable () -> Unit,
    themesScreen: @Composable () -> Unit,
    statsScreen: @Composable () -> Unit,
) {
    NavHost(navController = nav, startDestination = Routes.LIST) {
        composable(Routes.LIST) { listScreen(nav) }
        composable(Routes.EDIT) { backStack ->
            val id = backStack.arguments?.getString("id")?.toLongOrNull() ?: 0L
            editScreen(nav, id)
        }
        composable(Routes.SETTINGS) { settingsScreen() }
        composable(Routes.THEMES) { themesScreen() }
        composable(Routes.STATS) { statsScreen() }
    }
}
