package com.notilog.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.notilog.ui.detail.DetailScreen
import com.notilog.ui.feed.FeedScreen
import com.notilog.ui.feed.FeedViewModel
import com.notilog.ui.groups.GroupsScreen
import com.notilog.ui.settings.BlacklistScreen
import com.notilog.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Feed : Screen("feed")
    object Groups : Screen("groups")
    object Settings : Screen("settings")
    object Blacklist : Screen("blacklist")
    object Detail : Screen("detail/{systemId}/{tag}") {
        fun createRoute(systemId: Int, tag: String?) = "detail/$systemId/${tag ?: "null"}"
    }
}

@Composable
fun NotilogNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Feed.route,
    modifier: Modifier = Modifier
) {
    var pendingCategory by remember { mutableStateOf<String?>(null) }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Feed.route) {
            val feedViewModel: FeedViewModel = hiltViewModel()
            pendingCategory?.let { category ->
                LaunchedEffect(category) {
                    feedViewModel.setCategory(category)
                    pendingCategory = null
                }
            }
            FeedScreen(
                onNotificationClick = { systemId, tag ->
                    navController.navigate(Screen.Detail.createRoute(systemId, tag))
                },
                navController = navController
            )
        }
        composable(Screen.Groups.route) {
            GroupsScreen(
                onCategoryClick = { category ->
                    pendingCategory = category
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Feed.route) { inclusive = false }
                    }
                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onManageBlacklist = {
                    navController.navigate(Screen.Blacklist.route)
                }
            )
        }
        composable(Screen.Blacklist.route) {
            BlacklistScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("systemId") { type = NavType.IntType },
                navArgument("tag") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val systemId = backStackEntry.arguments?.getInt("systemId") ?: 0
            val tag = backStackEntry.arguments?.getString("tag").let {
                if (it == "null") null else it
            }
            DetailScreen(
                systemId = systemId,
                tag = tag,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
