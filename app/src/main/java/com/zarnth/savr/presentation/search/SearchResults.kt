package com.zarnth.savr.presentation.search

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zarnth.savr.domain.model.Bookmark
import com.zarnth.savr.presentation.home.components.BookmarkCard
import com.zarnth.savr.presentation.home.components.BookmarkListItem
import com.zarnth.savr.presentation.home.components.LoadingProgress
import com.zarnth.savr.presentation.setting.ViewMode

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SearchResults(
    results: List<Bookmark>,
    query: String,
    viewMode: ViewMode,
    selectedIds: Set<Long>,
    isSelectionMode: Boolean,
    isLoading: Boolean,
    onBodyClick: (Bookmark) -> Unit,
    onPhotoClick: (String) -> Unit,
    onLongClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyStaggeredGridState()
    val listState = rememberLazyListState()

    LaunchedEffect(query) {
        if (results.isNotEmpty()) {
            if (viewMode == ViewMode.GRID) gridState.scrollToItem(0)
            else listState.scrollToItem(0)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (query.isNotEmpty() && results.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No results for \"$query\"",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (query.isEmpty()) {
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
                        text = "Type to search",
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
                    items = results,
                    key = { it.id }
                ) { item ->
                    BookmarkCard(
                        modifier = Modifier.animateItem(),
                        imageUrl = item.imageUrl,
                        title = item.title,
                        description = item.description,
                        photoClickUrl = { onPhotoClick(it) },
                        bodyClick = { onBodyClick(item) },
                        onLongClick = { onLongClick(item.id) },
                        isSelected = item.id in selectedIds,
                        isSelectionMode = isSelectionMode,
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
                    items = results,
                    key = { it.id }
                ) { item ->
                    BookmarkListItem(
                        modifier = Modifier.animateItem(),
                        imageUrl = item.imageUrl,
                        title = item.title,
                        description = item.description,
                        photoClickUrl = { onPhotoClick(it) },
                        bodyClick = { onBodyClick(item) },
                        onLongClick = { onLongClick(item.id) },
                        isSelected = item.id in selectedIds,
                        isSelectionMode = isSelectionMode,
                        url = item.url
                    )
                }
            }
        }
        LoadingProgress(isLoading)
    }
}
