package com.zarnth.savr.presentation.collection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zarnth.savr.presentation.collection.components.CollectionCard
import com.zarnth.savr.presentation.collection.components.CollectionInputSheet
import com.zarnth.savr.presentation.home.components.LoadingProgress
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CollectionScreen(
    onCollectionClick: (Long) -> Unit,
    viewModel: CollectionViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler(enabled = state.isSelectionMode) {
        viewModel.onEvent(CollectionEvents.ClearSelection)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.collections.isEmpty() && !state.isLoading) {
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
                        text = "Please click + to create a collection",
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
                    items = state.collections,
                    key = { it.id }
                ) { collection ->
                    CollectionCard(
                        collection = collection,
                        onClick = {
                            viewModel.onEvent(CollectionEvents.SelectCollection(collection))
                            onCollectionClick(collection.id)
                        },
                        onLongClick = { viewModel.onEvent(CollectionEvents.ToggleSelection(collection.id)) },
                        isSelected = collection.id in state.selectedIds,
                        isSelectionMode = state.isSelectionMode,
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
        LoadingProgress(state.isLoading)
    }

    CollectionInputSheet(
        showBottomSheet = state.showCreateDialog,
        onDismissRequest = { viewModel.onEvent(CollectionEvents.HideCreateDialog) },
        value = state.inputName,
        onTextChange = { viewModel.onEvent(CollectionEvents.InputNameChanged(it)) },
        onSaveClick = { viewModel.onEvent(CollectionEvents.CreateCollection) }
    )
}
