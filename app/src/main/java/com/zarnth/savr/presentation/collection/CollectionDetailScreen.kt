package com.zarnth.savr.presentation.collection

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zarnth.savr.openChromeTab
import com.zarnth.savr.presentation.collection.components.CollectionCard
import com.zarnth.savr.presentation.collection.components.CollectionInputSheet
import com.zarnth.savr.presentation.collection.components.SubCollectionListCard
import com.zarnth.savr.presentation.home.components.BookmarkCard
import com.zarnth.savr.presentation.home.components.BookmarkListItem
import com.zarnth.savr.presentation.home.components.BookmarkPreviewSheet
import com.zarnth.savr.presentation.home.components.LoadingProgress
import com.zarnth.savr.presentation.search.SearchResults
import com.zarnth.savr.domain.model.Bookmark
import com.zarnth.savr.presentation.setting.TapAction
import com.zarnth.savr.presentation.setting.ViewMode

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CollectionDetailScreen(
    collectionId: Long,
    onNavigateToSubCollection: (Long) -> Unit = {},
    tapAction: TapAction = TapAction.SHOW_PREVIEW,
    viewMode: ViewMode = ViewMode.GRID,
    viewModel: CollectionViewModel,
    searchResults: List<Bookmark>? = null,
    searchQuery: String = ""
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardManager = LocalClipboard.current
    val detailData = state.detailDataById[collectionId] ?: CollectionDetailData(isLoading = true)
    val detailItems = detailData.detailItems
    val gridState = rememberLazyStaggeredGridState()
    val listState = rememberLazyListState()
    val itemCount = detailItems.size
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

    LaunchedEffect(state.duplicateToastKey) {
        if (state.duplicateToastKey > 0) {
            Toast.makeText(context, "Already in this collection", Toast.LENGTH_SHORT).show()
            viewModel.onEvent(CollectionEvents.CollectionDuplicateToastShown)
        }
    }

    BackHandler(enabled = state.isDetailSelectionMode) {
        viewModel.onEvent(CollectionEvents.ClearDetailSelection)
    }

    LaunchedEffect(collectionId) {
        if (state.selectedCollection?.id != collectionId) {
            viewModel.onEvent(CollectionEvents.RestoreCollectionDetail(collectionId))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
        if (searchResults != null) {
            SearchResults(
                results = searchResults,
                query = searchQuery,
                viewMode = viewMode,
                selectedIds = state.detailSelectedIds,
                isSelectionMode = state.isDetailSelectionMode,
                isLoading = detailData.isLoading,
                onBodyClick = { item ->
                    when (tapAction) {
                        TapAction.OPEN_BROWSER -> item.url?.let { openChromeTab(it, context) }
                        TapAction.COPY_LINK -> item.url?.let { clipboardManager.nativeClipboard.text = it }
                        TapAction.SHOW_PREVIEW -> viewModel.onEvent(CollectionEvents.ShowDetailBodySheet(item))
                    }
                },
                onPhotoClick = { Log.d("CollectionDetail", "Photo click: $it") },
                onLongClick = { viewModel.onEvent(CollectionEvents.ToggleDetailSelection(it)) }
            )
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                if (detailItems.isEmpty() && !detailData.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No bookmarks in this collection",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                            items = detailItems,
                            key = { row ->
                                when (row) {
                                    is CollectionDetailItem.BookmarkRow -> row.bookmark.id
                                    is CollectionDetailItem.SubCollectionRow -> "c_${row.collection.id}"
                                }
                            }
                        ) { row ->
                            when (row) {
                                is CollectionDetailItem.BookmarkRow -> {
                                    val item = row.bookmark
                                    BookmarkCard(
                                        modifier = Modifier.animateItem(),
                                        imageUrl = item.imageUrl,
                                        title = item.title,
                                        description = item.description,
                                        photoClickUrl = { Log.d("CollectionDetail", "Photo click: $it") },
                                        bodyClick = {
                                            when (tapAction) {
                                                TapAction.OPEN_BROWSER -> item.url?.let { openChromeTab(it, context) }
                                                TapAction.COPY_LINK -> item.url?.let { clipboardManager.nativeClipboard.text = it }
                                                TapAction.SHOW_PREVIEW -> viewModel.onEvent(CollectionEvents.ShowDetailBodySheet(item))
                                            }
                                        },
                                        onLongClick = { viewModel.onEvent(CollectionEvents.ToggleDetailSelection(item.id)) },
                                        isSelected = item.id in state.detailSelectedIds,
                                        isSelectionMode = state.isDetailSelectionMode,
                                        isPinned = item.isPinned,
                                        onPinToggle = { viewModel.onEvent(CollectionEvents.TogglePinInCollection(item.id)) },
                                        url = item.url
                                    )
                                }
                                is CollectionDetailItem.SubCollectionRow -> {
                                    val sub = row.collection
                                    CollectionCard(
                                        collection = sub,
                                        onClick = {
                                            viewModel.onEvent(CollectionEvents.SelectCollection(sub))
                                            onNavigateToSubCollection(sub.id)
                                        },
                                        onLongClick = { viewModel.onEvent(CollectionEvents.ShowDeleteCollectionDialog(sub)) },
                                        onRenameClick = { viewModel.onEvent(CollectionEvents.ShowRenameDialog(sub)) },
                                        modifier = Modifier.animateItem()
                                    )
                                }
                            }
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
                            items = detailItems,
                            key = { row ->
                                when (row) {
                                    is CollectionDetailItem.BookmarkRow -> row.bookmark.id
                                    is CollectionDetailItem.SubCollectionRow -> "c_${row.collection.id}"
                                }
                            }
                        ) { row ->
                            when (row) {
                                is CollectionDetailItem.BookmarkRow -> {
                                    val item = row.bookmark
                                    BookmarkListItem(
                                        modifier = Modifier.animateItem(),
                                imageUrl = item.imageUrl,
                                title = item.title,
                                description = item.description,
                                photoClickUrl = { Log.d("CollectionDetail", "Photo click: $it") },
                                bodyClick = {
                                    when (tapAction) {
                                        TapAction.OPEN_BROWSER -> item.url?.let { openChromeTab(it, context) }
                                        TapAction.COPY_LINK -> item.url?.let { clipboardManager.nativeClipboard.text = it }
                                        TapAction.SHOW_PREVIEW -> viewModel.onEvent(CollectionEvents.ShowDetailBodySheet(item))
                                    }
                                },
                                onLongClick = { viewModel.onEvent(CollectionEvents.ToggleDetailSelection(item.id)) },
                                isSelected = item.id in state.detailSelectedIds,
                                isSelectionMode = state.isDetailSelectionMode,
                                isPinned = item.isPinned,
                                onPinToggle = { viewModel.onEvent(CollectionEvents.TogglePinInCollection(item.id)) },
                                url = item.url
                            )
                                }
                                is CollectionDetailItem.SubCollectionRow -> {
                                    val sub = row.collection
                                    SubCollectionListCard(
                                        collection = sub,
                                        onClick = {
                                            viewModel.onEvent(CollectionEvents.SelectCollection(sub))
                                            onNavigateToSubCollection(sub.id)
                                        },
                                        onLongClick = { viewModel.onEvent(CollectionEvents.ShowDeleteCollectionDialog(sub)) },
                                        onRenameClick = { viewModel.onEvent(CollectionEvents.ShowRenameDialog(sub)) },
                                        modifier = Modifier.animateItem()
                                    )
                                }
                            }
                        }
                    }
                }
                LoadingProgress(detailData.isLoading)
            }
        }
        }
    }

    CollectionInputSheet(
        showBottomSheet = state.showCreateSubCollectionDialog,
        onDismissRequest = { viewModel.onEvent(CollectionEvents.HideCreateDialog) },
        value = state.inputName,
        onTextChange = { viewModel.onEvent(CollectionEvents.InputNameChanged(it)) },
        onSaveClick = { viewModel.onEvent(CollectionEvents.CreateSubCollection) },
        title = "New Sub-collection",
        placeHolderText = "Sub-collection name",
        buttonText = "Create"
    )

    CollectionInputSheet(
        showBottomSheet = state.isRenameDialogVisible,
        onDismissRequest = { viewModel.onEvent(CollectionEvents.HideRenameDialog) },
        value = state.inputName,
        onTextChange = { viewModel.onEvent(CollectionEvents.InputNameChanged(it)) },
        onSaveClick = { viewModel.onEvent(CollectionEvents.RenameCollection) },
        title = "Rename Collection",
        placeHolderText = "New collection name",
        buttonText = "Save"
    )

    state.deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(CollectionEvents.HideDeleteCollectionDialog) },
            title = { Text("Delete \"${target.name}\"?") },
            text = { Text("This will also delete everything inside it.") },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(CollectionEvents.ConfirmDeleteCollection) }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(CollectionEvents.HideDeleteCollectionDialog) }) {
                    Text("Cancel")
                }
            }
        )
    }
    BookmarkPreviewSheet(
        showBottomSheet = state.isDetailBodySheet,
        onDismissRequest = { viewModel.onEvent(CollectionEvents.DismissDetailBodySheet) },
        openInBrowser = {
            state.tempBookmark?.url?.let { openChromeTab(url = it, context = context) }
        },
        copyLinkButtonClick = {
            state.tempBookmark?.url?.let {
                clipboardManager.nativeClipboard.text = it
            }
        },
        onEditClick = {
            state.tempBookmark?.let { viewModel.onEvent(CollectionEvents.ShowEditBookmarkSheet(it)) }
        }
    )
}
