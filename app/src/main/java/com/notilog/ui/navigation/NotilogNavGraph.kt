package com.notilog.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

sealed class Screen(val route: String) {
    object Feed : Screen("feed")
    object Groups : Screen("groups")
    object Settings : Screen("settings")
    object Detail : Screen("detail/{systemId}/{tag}") {
        fun createRoute(systemId: Int, tag: String?) = "detail/$systemId/${tag ?: "null"}"
    }
}

@Composable
fun NotilogNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Feed.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Feed.route) {
            // FeedScreen(navController = navController)
        }
        composable(Screen.Groups.route) {
            // GroupsScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            // SettingsScreen(navController = navController)
        }
        composable(Screen.Detail.route) { _ ->
            // DetailScreen()
        }
    }
}
