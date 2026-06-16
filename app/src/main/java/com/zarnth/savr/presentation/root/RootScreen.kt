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
import androidx.compose.runtime.remember
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
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootScreen(
    sharedUrl: String? = null,
    viewModel: HomeViewModel = koinViewModel(),
    collectionViewModel: CollectionViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val collectionState by collectionViewModel.state.collectAsState()
    var currentTab by remember { mutableIntStateOf(0) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

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
                        IconButton(
                            onClick = {
                                collectionState.selectedCollection?.id?.let {
                                    collectionViewModel.onEvent(CollectionEvents.RemoveSelectedFromCollection(it))
                                }
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
                LargeTopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = { Text(if (currentTab == 0) "Savr Bookmarks" else bottomAppBarItems[currentTab].title) }
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
            homeScreen = { HomeScreen(sharedUrl = sharedUrl) },
            collectionsScreen = { navigateToDetail -> CollectionScreen(onCollectionClick = navigateToDetail) },
            collectionDetailScreen = { collectionId ->
                CollectionDetailScreen(collectionId = collectionId, viewModel = collectionViewModel)
            },
            settingsScreen = { SettingScreen() }
        )
    }

    if (state.showCollectionPicker) {
        CollectionPickerSheet(
            collections = state.collections,
            onSelectCollection = { viewModel.homeEvents(HomeEvents.AddToCollection(it)) },
            onDismiss = { viewModel.homeEvents(HomeEvents.HideCollectionPicker) }
        )
    }
}
