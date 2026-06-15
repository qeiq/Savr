package com.zarnth.savr.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay

@Composable
fun AppNavHost(
    currentTab: Int,
    onTabChange: (Int) -> Unit,
    homeScreen: @Composable () -> Unit,
    searchScreen: @Composable () -> Unit,
    settingsScreen: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val backStacks = remember {
        listOf(
            mutableStateListOf(AppRoute.Home),
            mutableStateListOf(AppRoute.Search),
            mutableStateListOf(AppRoute.Settings)
        )
    }

    val currentBackStack = backStacks[currentTab]

    BackHandler(enabled = currentTab != 0 || currentBackStack.size > 1) {
        when {
            currentBackStack.size > 1 -> currentBackStack.removeLastOrNull()
            currentTab != 0 -> onTabChange(0)
        }
    }

    NavDisplay(
        modifier = modifier,
        backStack = currentBackStack,
        onBack = { currentBackStack.removeLastOrNull() },
        entryProvider = { route ->
            when (route) {
                AppRoute.Home -> NavEntry(route) { homeScreen() }
                AppRoute.Search -> NavEntry(route) { searchScreen() }
                AppRoute.Settings -> NavEntry(route) { settingsScreen() }
            }
        }
    )
}
