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
    collectionsScreen: @Composable (onNavigateToDetail: (Long) -> Unit) -> Unit,
    collectionDetailScreen: @Composable (collectionId: Long, onNavigateToSubCollection: (Long) -> Unit) -> Unit,
    settingsScreen: @Composable (onNavigateToCrashLogs: () -> Unit) -> Unit,
    crashLogScreen: @Composable () -> Unit,
    onRestoreCollection: (collectionId: Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val backStacks: List<MutableList<AppRoute>> = remember {
        listOf(
            mutableStateListOf<AppRoute>(AppRoute.Home),
            mutableStateListOf<AppRoute>(AppRoute.Collections),
            mutableStateListOf<AppRoute>(AppRoute.Settings)
        )
    }

    val currentBackStack = backStacks[currentTab]

    BackHandler(enabled = currentTab != 0 && currentBackStack.size <= 1) {
        onTabChange(0)
    }

    NavDisplay(
        modifier = modifier,
        backStack = currentBackStack,
        onBack = {
            currentBackStack.removeLastOrNull()
            // Keep the section ViewModel in sync so the top bar always shows the
            // collection that is now at the top of the back stack (deterministic,
            // not reliant on Compose LaunchedEffect timing after a back-press).
            (currentBackStack.lastOrNull() as? AppRoute.CollectionDetail)?.let {
                onRestoreCollection(it.collectionId)
            }
        },
        entryProvider = { route ->
            when (route) {
                is AppRoute.Home -> NavEntry(route) {
                    homeScreen()
                }
                is AppRoute.Collections -> NavEntry(route) {
                    collectionsScreen { id -> currentBackStack.add(AppRoute.CollectionDetail(id)) }
                }
                is AppRoute.CollectionDetail -> NavEntry(route) {
                    collectionDetailScreen(route.collectionId) { id ->
                        currentBackStack.add(AppRoute.CollectionDetail(id))
                    }
                }
                is AppRoute.Settings -> NavEntry(route) {
                    settingsScreen { currentBackStack.add(AppRoute.CrashLogs) }
                }
                is AppRoute.CrashLogs -> NavEntry(route) { crashLogScreen() }
            }
        }
    )
}