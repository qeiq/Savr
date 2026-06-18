package com.zarnth.savr.presentation.collection

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zarnth.savr.openChromeTab
import com.zarnth.savr.presentation.home.components.BookmarkCard
import com.zarnth.savr.presentation.home.components.BookmarkPreviewSheet
import com.zarnth.savr.presentation.home.components.LoadingProgress
import com.zarnth.savr.presentation.setting.TapAction

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CollectionDetailScreen(
    collectionId: Long,
    tapAction: TapAction = TapAction.SHOW_PREVIEW,
    viewModel: CollectionViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardManager = LocalClipboard.current

    BackHandler(enabled = state.isDetailSelectionMode) {
        viewModel.onEvent(CollectionEvents.ClearDetailSelection)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.backToCollections()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.collectionBookmarks.isEmpty() && !state.isDetailLoading) {
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
        } else {
            LazyVerticalStaggeredGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp),
                columns = StaggeredGridCells.Adaptive(160.dp),
                contentPadding = PaddingValues(8.dp),
                verticalItemSpacing = 6.dp,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(
                    items = state.collectionBookmarks.reversed(),
                    key = { it.id }
                ) { item ->
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
                        url = item.url
                    )
                }
            }
        }
        LoadingProgress(state.isDetailLoading)
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
        }
    )
}
