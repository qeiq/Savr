package com.zarnth.savr.presentation.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
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
fun HomeScreen(viewModel: HomeViewModel = koinViewModel()) {

    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardManager = LocalClipboard.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
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