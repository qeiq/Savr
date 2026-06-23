package com.zarnth.savr.presentation.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zarnth.savr.domain.model.Bookmark
import com.zarnth.savr.openChromeTab
import com.zarnth.savr.presentation.home.components.BookmarkCard
import com.zarnth.savr.presentation.home.components.BookmarkListItem
import com.zarnth.savr.presentation.home.components.BookmarkPreviewSheet
import com.zarnth.savr.presentation.home.components.HomeInputSheet
import com.zarnth.savr.presentation.home.components.LoadingProgress
import com.zarnth.savr.presentation.home.components.PhotoPreview
import com.zarnth.savr.presentation.search.SearchResults
import com.zarnth.savr.presentation.setting.TapAction
import com.zarnth.savr.presentation.setting.ViewMode
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    sharedUrl: String? = null,
    tapAction: TapAction = TapAction.SHOW_PREVIEW,
    viewMode: ViewMode = ViewMode.GRID,
    searchResults: List<Bookmark>? = null,
    searchQuery: String = "",
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(sharedUrl) {
        if (sharedUrl != null) {
            viewModel.homeEvents(HomeEvents.OnTextFieldValueChange(sharedUrl))
            viewModel.homeEvents(HomeEvents.FabClick)
        }
    }
    val context = LocalContext.current
    val clipboardManager = LocalClipboard.current
    val gridState = rememberLazyStaggeredGridState()
    val listState = rememberLazyListState()
    val itemCount = state.bookmarkData.size
    var prevCount by rememberSaveable { mutableIntStateOf(itemCount) }

    LaunchedEffect(itemCount) {
        if (itemCount > prevCount && itemCount > 0) {
            if (viewMode == ViewMode.GRID) gridState.animateScrollToItem(0)
            else listState.animateScrollToItem(0)
        }
        prevCount = itemCount
    }

    LaunchedEffect(state.sortOrder) {
        if (itemCount > 0) {
            if (viewMode == ViewMode.GRID) gridState.scrollToItem(0)
            else listState.scrollToItem(0)
        }
    }

    BackHandler(enabled = state.isSelectionMode) {
        viewModel.homeEvents(HomeEvents.ClearSelection)
    }

    if (searchResults != null) {
        SearchResults(
            results = searchResults,
            query = searchQuery,
            viewMode = viewMode,
            selectedIds = state.selectedIds,
            isSelectionMode = state.isSelectionMode,
            isLoading = state.isLoading,
            onBodyClick = { item ->
                when (tapAction) {
                    TapAction.OPEN_BROWSER -> item.url?.let { openChromeTab(it, context) }
                    TapAction.COPY_LINK -> item.url?.let { clipboardManager.nativeClipboard.text = it }
                    TapAction.SHOW_PREVIEW -> viewModel.homeEvents(HomeEvents.BookmarkPreviewClick(item))
                }
            },
            onPhotoClick = { viewModel.homeEvents(HomeEvents.PreviewImageClick(url = it)) },
            onLongClick = { viewModel.homeEvents(HomeEvents.ToggleSelection(it)) }
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            if (state.bookmarkData.isEmpty() && !state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "┑(￣Д ￣)┍",
                            fontSize = 36.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "No bookmarks yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (viewMode == ViewMode.GRID) {
                LazyVerticalStaggeredGrid(
                    state = gridState,
                    modifier = Modifier.fillMaxSize(),
                    columns = StaggeredGridCells.Adaptive(160.dp),
                    contentPadding = PaddingValues(8.dp),
                    verticalItemSpacing = 6.dp,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(
                        items = state.bookmarkData,
                        key = { it.id }
                    ) { item ->
                        BookmarkCard(
                            modifier = Modifier.animateItem(),
                            imageUrl = item.imageUrl,
                            title = item.title,
                            description = item.description,
                            photoClickUrl = {
                                viewModel.homeEvents(HomeEvents.PreviewImageClick(url = it))
                                Log.d("Photo Dialog", "HomeScreen: $it")
                            },
                            bodyClick = {
                                when (tapAction) {
                                    TapAction.OPEN_BROWSER -> item.url?.let { openChromeTab(it, context) }
                                    TapAction.COPY_LINK -> item.url?.let { clipboardManager.nativeClipboard.text = it }
                                    TapAction.SHOW_PREVIEW -> viewModel.homeEvents(HomeEvents.BookmarkPreviewClick(item))
                                }
                            },
                            onLongClick = {
                                viewModel.homeEvents(HomeEvents.ToggleSelection(item.id))
                            },
                            isSelected = item.id in state.selectedIds,
                            isSelectionMode = state.isSelectionMode,
                            url = item.url
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(
                        items = state.bookmarkData,
                        key = { it.id }
                    ) { item ->
                        BookmarkListItem(
                            modifier = Modifier.animateItem(),
                            imageUrl = item.imageUrl,
                            title = item.title,
                            description = item.description,
                            photoClickUrl = {
                                viewModel.homeEvents(HomeEvents.PreviewImageClick(url = it))
                            },
                            bodyClick = {
                                when (tapAction) {
                                    TapAction.OPEN_BROWSER -> item.url?.let { openChromeTab(it, context) }
                                    TapAction.COPY_LINK -> item.url?.let { clipboardManager.nativeClipboard.text = it }
                                    TapAction.SHOW_PREVIEW -> viewModel.homeEvents(HomeEvents.BookmarkPreviewClick(item))
                                }
                            },
                            onLongClick = {
                                viewModel.homeEvents(HomeEvents.ToggleSelection(item.id))
                            },
                            isSelected = item.id in state.selectedIds,
                            isSelectionMode = state.isSelectionMode,
                            url = item.url
                        )
                    }
                }
            }
            LoadingProgress(state.isLoading)
        }
    }

    HomeInputSheet(
        state.isDialog,
        onDismissRequest = {
            viewModel.homeEvents(HomeEvents.OnDialogDismissClick)
        },
        value = state.inputUrl,
        onTextChange = {
            viewModel.homeEvents(HomeEvents.OnTextFieldValueChange(it))
        }, onSaveClick = {
            viewModel.homeEvents(HomeEvents.SaveBookmark)
        }
    )

    PhotoPreview(
        isDialog = state.isPhotoPreviewDialog,
        onDismissRequest = {
            viewModel.homeEvents(HomeEvents.PreviewImageDismissClick)
        },
        imageURL = state.dialogPhotoUrl
    )

    BookmarkPreviewSheet(
        showBottomSheet = state.isBodySheet,
        onDismissRequest = {
            viewModel.homeEvents(HomeEvents.BookmarkPreviewDismissClick)
        },
        openInBrowser = {
            state.tempBookmark?.url?.let { openChromeTab(url = it, context = context) }
        },
        copyLinkButtonClick = {
            state.tempBookmark?.url?.let {
                clipboardManager.nativeClipboard.text = it
            }
        }
    )

}