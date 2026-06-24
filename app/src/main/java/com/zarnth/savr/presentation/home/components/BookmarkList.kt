package com.zarnth.savr.presentation.home.components

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.unit.dp
import com.zarnth.savr.domain.model.Bookmark
import com.zarnth.savr.presentation.home.HomeEvents
import com.zarnth.savr.presentation.home.HomeViewModel
import com.zarnth.savr.presentation.setting.TapAction

@Composable
fun BookmarkList(
    items: List<Bookmark>,
    listState: LazyListState,
    selectedIds: Set<Long>,
    isSelectionMode: Boolean,
    tapAction: TapAction,
    context: Context,
    clipboard: Clipboard,
    viewModel: HomeViewModel
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(items = items, key = { it.id }) { item ->
            BookmarkListItem(
                modifier = Modifier.animateItem(),
                imageUrl = item.imageUrl,
                title = item.title,
                description = item.description,
                photoClickUrl = { viewModel.homeEvents(HomeEvents.PreviewImageClick(url = it)) },
                bodyClick = { handleTap(item, tapAction, context, clipboard, viewModel) },
                onLongClick = { viewModel.homeEvents(HomeEvents.ToggleSelection(item.id)) },
                isSelected = item.id in selectedIds,
                isSelectionMode = isSelectionMode,
                url = item.url
            )
        }
    }
}
