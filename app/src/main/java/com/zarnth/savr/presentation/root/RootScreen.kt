package com.zarnth.savr.presentation.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.zarnth.savr.R
import com.zarnth.savr.navigation.AppNavHost
import com.zarnth.savr.presentation.collection.CollectionDetailScreen
import com.zarnth.savr.presentation.collection.CollectionEvents
import com.zarnth.savr.presentation.collection.CollectionScreen
import com.zarnth.savr.presentation.collection.CollectionViewModel
import com.zarnth.savr.presentation.collection.components.CollectionPickerSheet
import com.zarnth.savr.presentation.home.HomeEvents
import com.zarnth.savr.presentation.home.HomeScreen
import com.zarnth.savr.presentation.home.HomeViewModel
import com.zarnth.savr.presentation.setting.SettingScreen
import com.zarnth.savr.presentation.setting.SettingViewModel
import com.zarnth.savr.presentation.setting.SortOrder
import com.zarnth.savr.presentation.setting.components.SortSheet
import com.zarnth.savr.ui.theme.SavrTheme
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootScreen(
    sharedUrl: String? = null,
    viewModel: HomeViewModel = koinViewModel(),
    collectionViewModel: CollectionViewModel = koinViewModel(),
    settingViewModel: SettingViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val collectionState by collectionViewModel.state.collectAsState()
    val settingState by settingViewModel.state.collectAsState()
    var currentTab by rememberSaveable { mutableIntStateOf(0) }
    val pendingSharedUrl = remember { mutableStateOf(sharedUrl) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    SavrTheme(themeMode = settingState.themeMode, dynamicColor = settingState.dynamicColor) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .background(MaterialTheme.colorScheme.surface),
            bottomBar = {
                if (!state.isSelectionMode && !collectionState.isSelectionMode && !collectionState.isDetailSelectionMode) {
                    BottomAppBar {
                        bottomAppBarItems.forEachIndexed { index, item ->
                            val isClicked = currentTab == index
                            NavigationBarItem(
                                label = { Text(item.title) },
                                selected = isClicked,
                                onClick = { currentTab = index },
                                icon = {
                                    Icon(
                                        painter = painterResource(if (isClicked) item.iconFilled else item.icon),
                                        contentDescription = item.title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            },
            topBar = {
                if (state.isSelectionMode) {
                    LargeTopAppBar(
                        scrollBehavior = scrollBehavior,
                        title = { Text("Selected ${state.selectedIds.size}") },
                        navigationIcon = {
                            IconButton(onClick = { viewModel.homeEvents(HomeEvents.ClearSelection) }) {
                                Icon(
                                    painter = painterResource(R.drawable.close_icon),
                                    contentDescription = "Clear selection"
                                )
                            }
                        },
                        actions = {
                            val allBookmarksSelected = state.bookmarkData.isNotEmpty() && state.selectedIds.size == state.bookmarkData.size
                            if (allBookmarksSelected) {
                                IconButton(onClick = { viewModel.homeEvents(HomeEvents.DeselectAll) }) {
                                    Icon(
                                        painter = painterResource(R.drawable.deselct_all),
                                        contentDescription = "Deselect all",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            } else {
                                IconButton(onClick = { viewModel.homeEvents(HomeEvents.SelectAll) }) {
                                    Icon(
                                        painter = painterResource(R.drawable.selectall_icon),
                                        contentDescription = "Select all",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            IconButton(onClick = { viewModel.homeEvents(HomeEvents.ShowCollectionPicker) }) {
                                Icon(
                                    painter = painterResource(R.drawable.bookmark_one),
                                    contentDescription = "Add to collection",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            IconButton(onClick = { viewModel.homeEvents(HomeEvents.DeleteSelected) }) {
                                Icon(
                                    painter = painterResource(R.drawable.delete_icon),
                                    contentDescription = "Delete selected"
                                )
                            }
                        }
                    )
                } else if (collectionState.isSelectionMode) {
                    LargeTopAppBar(
                        scrollBehavior = scrollBehavior,
                        title = { Text("Selected ${collectionState.selectedIds.size}") },
                        navigationIcon = {
                            IconButton(onClick = { collectionViewModel.onEvent(CollectionEvents.ClearSelection) }) {
                                Icon(
                                    painter = painterResource(R.drawable.close_icon),
                                    contentDescription = "Clear selection"
                                )
                            }
                        },
                        actions = {
                            val allCollectionsSelected = collectionState.collections.isNotEmpty() && collectionState.selectedIds.size == collectionState.collections.size
                            if (allCollectionsSelected) {
                                IconButton(onClick = { collectionViewModel.onEvent(CollectionEvents.DeselectAll) }) {
                                    Icon(
                                        painter = painterResource(R.drawable.deselct_all),
                                        contentDescription = "Deselect all",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            } else {
                                IconButton(onClick = { collectionViewModel.onEvent(CollectionEvents.SelectAll) }) {
                                    Icon(
                                        painter = painterResource(R.drawable.selectall_icon),
                                        contentDescription = "Select all",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            IconButton(onClick = { collectionViewModel.onEvent(CollectionEvents.DeleteSelected) }) {
                                Icon(
                                    painter = painterResource(R.drawable.delete_icon),
                                    contentDescription = "Delete selected"
                                )
                            }
                        }
                    )
                } else if (collectionState.isDetailSelectionMode) {
                    LargeTopAppBar(
                        scrollBehavior = scrollBehavior,
                        title = { Text("Selected ${collectionState.detailSelectedIds.size}") },
                        navigationIcon = {
                            IconButton(onClick = { collectionViewModel.onEvent(CollectionEvents.ClearDetailSelection) }) {
                                Icon(
                                    painter = painterResource(R.drawable.close_icon),
                                    contentDescription = "Clear selection"
                                )
                            }
                        },
                        actions = {
                            val allDetailSelected = collectionState.collectionBookmarks.isNotEmpty() && collectionState.detailSelectedIds.size == collectionState.collectionBookmarks.size
                            if (allDetailSelected) {
                                IconButton(onClick = { collectionViewModel.onEvent(CollectionEvents.DeselectAllDetail) }) {
                                    Icon(
                                        painter = painterResource(R.drawable.deselct_all),
                                        contentDescription = "Deselect all",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            } else {
                                IconButton(onClick = { collectionViewModel.onEvent(CollectionEvents.SelectAllDetail) }) {
                                    Icon(
                                        painter = painterResource(R.drawable.selectall_icon),
                                        contentDescription = "Select all",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    val id = collectionState.selectedCollection?.id ?: return@IconButton
                                    collectionViewModel.onEvent(CollectionEvents.RemoveSelectedFromCollection(id))
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.delete_icon),
                                    contentDescription = "Remove from collection"
                                )
                            }
                        }
                    )
                } else {
                    val showSortButton = currentTab == 0 || (currentTab == 1 && collectionState.selectedCollection != null)
                    LargeTopAppBar(
                        scrollBehavior = scrollBehavior,
                        title = { Text(if (currentTab == 0) "Savr Bookmarks" else bottomAppBarItems[currentTab].title) },
                        actions = {
                            if (showSortButton) {
                                IconButton(onClick = {
                                    if (currentTab == 0) {
                                        viewModel.homeEvents(HomeEvents.ShowSortSheet)
                                    } else {
                                        collectionViewModel.onEvent(CollectionEvents.ShowSortSheet)
                                    }
                                }) {
                                    Icon(
                                        painter = painterResource(R.drawable.filter_icon),
                                        contentDescription = "Sort",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    )
                }
            },
            floatingActionButton = {
                if (!state.isSelectionMode && !collectionState.isSelectionMode && !collectionState.isDetailSelectionMode) {
                    when (currentTab) {
                        0 -> FloatingActionButton(
                            onClick = { viewModel.homeEvents(HomeEvents.FabClick) }
                        ) {
                            Icon(
                                painterResource(R.drawable.add_icons),
                                contentDescription = null,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        1 -> if (collectionState.selectedCollection == null) {
                            FloatingActionButton(
                                onClick = { collectionViewModel.onEvent(CollectionEvents.ShowCreateDialog) }
                            ) {
                                Icon(
                                    painterResource(R.drawable.add_icons),
                                    contentDescription = null,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            AppNavHost(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                currentTab = currentTab,
                onTabChange = { currentTab = it },
                homeScreen = {
                    val url = pendingSharedUrl.value
                    if (url != null) pendingSharedUrl.value = null
                    HomeScreen(sharedUrl = url, tapAction = settingState.tapAction, viewMode = settingState.viewMode)
                },
                collectionsScreen = { navigateToDetail -> CollectionScreen(onCollectionClick = navigateToDetail) },
                collectionDetailScreen = { collectionId ->
                    CollectionDetailScreen(collectionId = collectionId, tapAction = settingState.tapAction, viewMode = settingState.viewMode, viewModel = collectionViewModel)
                },
                settingsScreen = { SettingScreen(viewModel = settingViewModel) }
            )
        }

        if (state.showCollectionPicker) {
            CollectionPickerSheet(
                collections = state.collections,
                onSelectCollection = { viewModel.homeEvents(HomeEvents.AddToCollection(it)) },
                onDismiss = { viewModel.homeEvents(HomeEvents.HideCollectionPicker) }
            )
        }

        if (state.showSortSheet) {
            SortSheet(
                current = state.sortOrder,
                onSelect = { viewModel.homeEvents(HomeEvents.SetSortOrder(it)) },
                onDismiss = { viewModel.homeEvents(HomeEvents.HideSortSheet) }
            )
        }

        if (collectionState.showSortSheet) {
            SortSheet(
                current = collectionState.sortOrder,
                onSelect = { collectionViewModel.onEvent(CollectionEvents.SetSortOrder(it)) },
                onDismiss = { collectionViewModel.onEvent(CollectionEvents.HideSortSheet) }
            )
        }
    }
}
