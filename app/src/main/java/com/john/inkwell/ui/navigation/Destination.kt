package com.john.inkwell.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Destination(val route: String, val label: String, val icon: ImageVector) {
    object Today : Destination("today", "Today", Icons.Filled.Today)
    object History : Destination("history", "History", Icons.Filled.History)
    object Search : Destination("search", "Search", Icons.Filled.Search)
    object Settings : Destination("settings", "Settings", Icons.Filled.Settings)

    companion object {
        val bottomBarItems = listOf(Today, History, Search, Settings)
    }
}

/** History drills into a specific day; route carries the date as an argument. */
const val DAY_DETAIL_ROUTE = "day/{date}"
fun dayDetailRoute(date: String) = "day/$date"
