package com.zarnth.savr.presentation.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.material3.TopAppBarDefaults
import com.zarnth.savr.domain.model.SortOrder
import com.zarnth.savr.navigation.AppNavHost
import com.zarnth.savr.presentation.collection.CollectionDetailScreen
import com.zarnth.savr.presentation.collection.CollectionEvents
import com.zarnth.savr.presentation.collection.CollectionScreen
import com.zarnth.savr.presentation.collection.CollectionViewModel
import com.zarnth.savr.presentation.collection.components.CollectionPickerSheet
import com.zarnth.savr.presentation.home.HomeEvents
import com.zarnth.savr.presentation.home.HomeScreen
import com.zarnth.savr.presentation.home.HomeViewModel
import com.zarnth.savr.presentation.root.components.DefaultTopBar
import com.zarnth.savr.presentation.root.components.RootBottomBar
import com.zarnth.savr.presentation.root.components.RootFab
import com.zarnth.savr.presentation.root.components.SearchTopBar
import com.zarnth.savr.presentation.root.components.SelectionTopBar
import com.zarnth.savr.presentation.search.SearchViewModel
import com.zarnth.savr.presentation.setting.SettingScreen
import com.zarnth.savr.presentation.setting.SettingViewModel
import com.zarnth.savr.presentation.setting.components.RadioOptionSheet
import com.zarnth.savr.ui.theme.SavrTheme
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootScreen(
    sharedUrl: String? = null,
    viewModel: HomeViewModel = koinViewModel(),
    collectionViewModel: CollectionViewModel = koinViewModel(),
    settingViewModel: SettingViewModel = koinViewModel(),
    searchViewModel: SearchViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val collectionState by collectionViewModel.state.collectAsState()
    val settingState by settingViewModel.state.collectAsState()
    val searchState by searchViewModel.state.collectAsState()
    var currentTab by rememberSaveable { mutableIntStateOf(0) }
    var isSearching by remember { mutableStateOf(false) }
    var isCollectionSearching by remember { mutableStateOf(false) }
    var collectionSearchQuery by remember { mutableStateOf("") }
    val pendingSharedUrl = remember { mutableStateOf(sharedUrl) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val collectionSearchResults = remember(
        collectionState.collectionBookmarks,
        collectionSearchQuery,
        isCollectionSearching
    ) {
        if (!isCollectionSearching) null
        else if (collectionSearchQuery.isBlank()) collectionState.collectionBookmarks
        else collectionState.collectionBookmarks.filter {
            it.title?.contains(collectionSearchQuery, ignoreCase = true) ?: false
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    LaunchedEffect(isSearching) {
        if (isSearching) focusRequester.requestFocus()
    }

    LaunchedEffect(isCollectionSearching) {
        if (isCollectionSearching) focusRequester.requestFocus()
    }

    val showTopBarActions =
        !state.isSelectionMode && !collectionState.isSelectionMode &&
        !collectionState.isDetailSelectionMode && !isSearching && !isCollectionSearching

    SavrTheme(themeMode = settingState.themeMode, dynamicColor = settingState.dynamicColor) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .background(MaterialTheme.colorScheme.surface),
            bottomBar = {
                if (showTopBarActions) {
                    RootBottomBar(
                        currentTab = currentTab,
                        onTabChange = { currentTab = it }
                    )
                }
            },
            topBar = {
                when {
                    state.isSelectionMode && !isSearching && !isCollectionSearching -> {
                        SelectionTopBar(
                            title = "Selected ${state.selectedIds.size}",
                            isAllSelected = state.bookmarkData.isNotEmpty() &&
                                state.selectedIds.size == state.bookmarkData.size,
                            onClose = { viewModel.homeEvents(HomeEvents.ClearSelection) },
                            onSelectAll = { viewModel.homeEvents(HomeEvents.SelectAll) },
                            onDeselectAll = { viewModel.homeEvents(HomeEvents.DeselectAll) },
                            onDelete = { viewModel.homeEvents(HomeEvents.DeleteSelected) },
                            onAddToCollection = { viewModel.homeEvents(HomeEvents.ShowCollectionPicker) },
                            scrollBehavior = scrollBehavior
                        )
                    }

                    collectionState.isSelectionMode && !isSearching && !isCollectionSearching -> {
                        SelectionTopBar(
                            title = "Selected ${collectionState.selectedIds.size}",
                            isAllSelected = collectionState.collections.isNotEmpty() &&
                                collectionState.selectedIds.size == collectionState.collections.size,
                            onClose = { collectionViewModel.onEvent(CollectionEvents.ClearSelection) },
                            onSelectAll = { collectionViewModel.onEvent(CollectionEvents.SelectAll) },
                            onDeselectAll = { collectionViewModel.onEvent(CollectionEvents.DeselectAll) },
                            onDelete = { collectionViewModel.onEvent(CollectionEvents.DeleteSelected) },
                            scrollBehavior = scrollBehavior
                        )
                    }

                    collectionState.isDetailSelectionMode && !isSearching && !isCollectionSearching -> {
                        SelectionTopBar(
                            title = "Selected ${collectionState.detailSelectedIds.size}",
                            isAllSelected = collectionState.collectionBookmarks.isNotEmpty() &&
                                collectionState.detailSelectedIds.size == collectionState.collectionBookmarks.size,
                            onClose = { collectionViewModel.onEvent(CollectionEvents.ClearDetailSelection) },
                            onSelectAll = { collectionViewModel.onEvent(CollectionEvents.SelectAllDetail) },
                            onDeselectAll = { collectionViewModel.onEvent(CollectionEvents.DeselectAllDetail) },
                            onDelete = {
                                val id = collectionState.selectedCollection?.id ?: return@SelectionTopBar
                                collectionViewModel.onEvent(CollectionEvents.RemoveSelectedFromCollection(id))
                            },
                            scrollBehavior = scrollBehavior
                        )
                    }

                    isSearching -> {
                        SearchTopBar(
                            query = searchState.searchQuery,
                            onQueryChange = { searchViewModel.onQueryChange(it) },
                            placeholder = "Search bookmarks\u2026",
                            onClose = {
                                isSearching = false
                                searchViewModel.onQueryChange("")
                                focusManager.clearFocus()
                            },
                            focusRequester = focusRequester
                        )
                    }

                    isCollectionSearching -> {
                        SearchTopBar(
                            query = collectionSearchQuery,
                            onQueryChange = { collectionSearchQuery = it },
                            placeholder = "Search in collection\u2026",
                            onClose = {
                                isCollectionSearching = false
                                collectionSearchQuery = ""
                                focusManager.clearFocus()
                            },
                            focusRequester = focusRequester
                        )
                    }

                    else -> {
                        val showSortButton =
                            currentTab == 0 || (currentTab == 1 && collectionState.selectedCollection != null)
                        val showSearchButton =
                            currentTab == 0 || (currentTab == 1 && collectionState.selectedCollection != null)
                        DefaultTopBar(
                            currentTab = currentTab,
                            showSearchButton = showSearchButton,
                            showSortButton = showSortButton,
                            scrollBehavior = scrollBehavior,
                            onSearchClick = {
                                if (currentTab == 0) {
                                    isSearching = true
                                    searchViewModel.onQueryChange("")
                                    viewModel.homeEvents(HomeEvents.ClearSelection)
                                } else {
                                    isCollectionSearching = true
                                    collectionSearchQuery = ""
                                    collectionViewModel.onEvent(CollectionEvents.ClearDetailSelection)
                                }
                            },
                            onSortClick = {
                                if (currentTab == 0) {
                                    viewModel.homeEvents(HomeEvents.ShowSortSheet)
                                } else {
                                    collectionViewModel.onEvent(CollectionEvents.ShowSortSheet)
                                }
                            }
                        )
                    }
                }
            },
            floatingActionButton = {
                RootFab(
                    currentTab = currentTab,
                    homeState = state,
                    collectionState = collectionState,
                    onHomeFabClick = { viewModel.homeEvents(HomeEvents.FabClick) },
                    onCollectionFabClick = { collectionViewModel.onEvent(CollectionEvents.ShowCreateDialog) }
                )
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
                    HomeScreen(
                        sharedUrl = url,
                        tapAction = settingState.tapAction,
                        viewMode = settingState.viewMode,
                        searchResults = if (isSearching) searchState.searchResults else null,
                        searchQuery = searchState.searchQuery
                    )
                },
                collectionsScreen = { navigateToDetail -> CollectionScreen(onCollectionClick = navigateToDetail) },
                collectionDetailScreen = { collectionId ->
                    CollectionDetailScreen(
                        collectionId = collectionId,
                        tapAction = settingState.tapAction,
                        viewMode = settingState.viewMode,
                        viewModel = collectionViewModel,
                        searchResults = collectionSearchResults,
                        searchQuery = collectionSearchQuery
                    )
                },
                settingsScreen = { SettingScreen(viewModel = settingViewModel) }
            )

            BackHandler(enabled = isSearching) {
                isSearching = false
                searchViewModel.onQueryChange("")
            }

            BackHandler(enabled = isCollectionSearching) {
                isCollectionSearching = false
                collectionSearchQuery = ""
            }
        }

        if (state.showCollectionPicker) {
            CollectionPickerSheet(
                collections = state.collections,
                onSelectCollection = { viewModel.homeEvents(HomeEvents.AddToCollection(it)) },
                onDismiss = { viewModel.homeEvents(HomeEvents.HideCollectionPicker) }
            )
        }

        if (state.showSortSheet) {
            RadioOptionSheet(
                title = "Sort by",
                options = listOf(
                    "Date added (newest first)" to SortOrder.DATE_NEWEST,
                    "Date added (oldest first)" to SortOrder.DATE_OLDEST,
                    "Title (A-Z)" to SortOrder.TITLE_ASC,
                    "Title (Z-A)" to SortOrder.TITLE_DESC
                ),
                current = state.sortOrder,
                onSelect = { viewModel.homeEvents(HomeEvents.SetSortOrder(it)) },
                onDismiss = { viewModel.homeEvents(HomeEvents.HideSortSheet) }
            )
        }

        if (collectionState.showSortSheet) {
            RadioOptionSheet(
                title = "Sort by",
                options = listOf(
                    "Date added (newest first)" to SortOrder.DATE_NEWEST,
                    "Date added (oldest first)" to SortOrder.DATE_OLDEST,
                    "Title (A-Z)" to SortOrder.TITLE_ASC,
                    "Title (Z-A)" to SortOrder.TITLE_DESC
                ),
                current = collectionState.sortOrder,
                onSelect = { collectionViewModel.onEvent(CollectionEvents.SetSortOrder(it)) },
                onDismiss = { collectionViewModel.onEvent(CollectionEvents.HideSortSheet) }
            )
        }
    }
}
