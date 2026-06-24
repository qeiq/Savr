package com.zarnth.savr.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zarnth.savr.domain.model.Bookmark
import com.zarnth.savr.openChromeTab
import com.zarnth.savr.presentation.home.components.BookmarkGrid
import com.zarnth.savr.presentation.home.components.BookmarkList
import com.zarnth.savr.presentation.home.components.BookmarkPreviewSheet
import com.zarnth.savr.presentation.home.components.EmptyBookmarkState
import com.zarnth.savr.presentation.home.components.HomeInputSheet
import com.zarnth.savr.presentation.home.components.LoadingProgress
import com.zarnth.savr.presentation.home.components.PhotoPreview
import com.zarnth.savr.presentation.home.components.handleTap
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
    val context = LocalContext.current
    val clipboard = LocalClipboard.current

    LaunchedEffect(sharedUrl) {
        if (sharedUrl != null) {
            viewModel.homeEvents(HomeEvents.OnTextFieldValueChange(sharedUrl))
            viewModel.homeEvents(HomeEvents.FabClick)
        }
    }

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
            onBodyClick = { item -> handleTap(item, tapAction, context, clipboard, viewModel) },
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
                    EmptyBookmarkState()
                }
            } else if (viewMode == ViewMode.GRID) {
                BookmarkGrid(
                    items = state.bookmarkData,
                    gridState = gridState,
                    selectedIds = state.selectedIds,
                    isSelectionMode = state.isSelectionMode,
                    tapAction = tapAction,
                    context = context,
                    clipboard = clipboard,
                    viewModel = viewModel
                )
            } else {
                BookmarkList(
                    items = state.bookmarkData,
                    listState = listState,
                    selectedIds = state.selectedIds,
                    isSelectionMode = state.isSelectionMode,
                    tapAction = tapAction,
                    context = context,
                    clipboard = clipboard,
                    viewModel = viewModel
                )
            }
            LoadingProgress(state.isLoading)
        }
    }

    HomeInputSheet(
        state.isDialog,
        onDismissRequest = { viewModel.homeEvents(HomeEvents.OnDialogDismissClick) },
        value = state.inputUrl,
        onTextChange = { viewModel.homeEvents(HomeEvents.OnTextFieldValueChange(it)) },
        onSaveClick = { viewModel.homeEvents(HomeEvents.SaveBookmark) }
    )

    PhotoPreview(
        isDialog = state.isPhotoPreviewDialog,
        onDismissRequest = { viewModel.homeEvents(HomeEvents.PreviewImageDismissClick) },
        imageURL = state.dialogPhotoUrl
    )

    BookmarkPreviewSheet(
        showBottomSheet = state.isBodySheet,
        onDismissRequest = { viewModel.homeEvents(HomeEvents.BookmarkPreviewDismissClick) },
        openInBrowser = { state.tempBookmark?.url?.let { openChromeTab(url = it, context = context) } },
        copyLinkButtonClick = { state.tempBookmark?.url?.let { clipboard.nativeClipboard.text = it } }
    )
}
