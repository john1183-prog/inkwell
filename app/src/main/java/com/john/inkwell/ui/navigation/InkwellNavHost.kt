package com.john.inkwell.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.john.inkwell.data.InkwellRepository
import com.john.inkwell.data.UserPreferences
import com.john.inkwell.data.drive.DriveSyncManager
import com.john.inkwell.ui.history.DayDetailScreen
import com.john.inkwell.ui.history.HistoryScreen
import com.john.inkwell.ui.search.SearchScreen
import com.john.inkwell.ui.settings.SettingsScreen
import com.john.inkwell.ui.today.TodayScreen

@Composable
fun InkwellNavHost(
    repository: InkwellRepository,
    preferences: UserPreferences,
    driveSyncManager: DriveSyncManager,
    onSignInClick: () -> Unit
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = backStackEntry?.destination
                Destination.bottomBarItems.forEach { destination ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Today.route,
            modifier = androidx.compose.ui.Modifier.padding(padding)
        ) {
            composable(Destination.Today.route) {
                TodayScreen(repository = repository, preferences = preferences)
            }
            composable(Destination.History.route) {
                HistoryScreen(repository = repository) { date ->
                    navController.navigate(dayDetailRoute(date))
                }
            }
            composable(
                DAY_DETAIL_ROUTE,
                arguments = listOf(navArgument("date") { type = NavType.StringType })
            ) { backStackEntry ->
                val date = backStackEntry.arguments?.getString("date").orEmpty()
                DayDetailScreen(repository = repository, date = date, onBack = { navController.popBackStack() })
            }
            composable(Destination.Search.route) {
                SearchScreen(repository = repository)
            }
            composable(Destination.Settings.route) {
                SettingsScreen(
                    preferences = preferences,
                    driveSyncManager = driveSyncManager,
                    onSignInClick = onSignInClick
                )
            }
        }
    }
}
