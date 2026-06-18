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
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zarnth.savr.openChromeTab
import com.zarnth.savr.presentation.home.components.BookmarkCard
import com.zarnth.savr.presentation.home.components.BookmarkPreviewSheet
import com.zarnth.savr.presentation.home.components.HomeInputSheet
import com.zarnth.savr.presentation.home.components.LoadingProgress
import com.zarnth.savr.presentation.home.components.PhotoPreview
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    sharedUrl: String? = null,
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

    BackHandler(enabled = state.isSelectionMode) {
        viewModel.homeEvents(HomeEvents.ClearSelection)
    }

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
        } else {
            LazyVerticalStaggeredGrid(
                modifier = Modifier.fillMaxSize(),
                columns = StaggeredGridCells.Adaptive(160.dp),
                contentPadding = PaddingValues(8.dp),
                verticalItemSpacing = 6.dp,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(
                    items = state.bookmarkData.reversed(),
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
                            viewModel.homeEvents(HomeEvents.BookmarkPreviewClick(item))
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