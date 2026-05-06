package com.notilog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.notilog.ui.navigation.NotilogNavGraph
import com.notilog.ui.navigation.Screen
import com.notilog.ui.theme.GlassSurface
import com.notilog.ui.theme.GradientBackground
import com.notilog.ui.theme.LocalGlassTokens
import com.notilog.ui.theme.NotilogTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotilogTheme {
                NotilogApp()
            }
        }
    }
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotilogApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Feed, "Feed", Icons.Default.Refresh),
        BottomNavItem(Screen.Filters, "Filters", Icons.Default.List),
        BottomNavItem(Screen.Rules, "Rules", Icons.Default.Star),
        BottomNavItem(Screen.Insights, "Insights", Icons.Default.Info)
    )

    val showBottomBar = currentDestination?.route?.let { route ->
        route != Screen.Settings.route && !route.startsWith("detail") && route != Screen.Blacklist.route
    } ?: true

    GradientBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            bottomBar = {
                if (showBottomBar) {
                    GlassSurface(
                        cornerRadius = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {
                        NavigationBar(
                            containerColor = Color.Transparent,
                            tonalElevation = 0.dp,
                            modifier = Modifier.height(80.dp)
                        ) {
                            bottomNavItems.forEach { item ->
                                val isSelected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        navController.navigate(item.screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { 
                                        Icon(
                                            item.icon, 
                                            contentDescription = item.label,
                                            tint = if (isSelected) 
                                                MaterialTheme.colorScheme.onSecondaryContainer 
                                            else 
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        ) 
                                    },
                                    label = { 
                                        Text(
                                            item.label,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isSelected)
                                                MaterialTheme.colorScheme.onSecondaryContainer
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        ) 
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            NotilogNavGraph(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
