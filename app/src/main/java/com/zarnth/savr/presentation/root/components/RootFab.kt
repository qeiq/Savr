package com.zarnth.savr.presentation.root.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.zarnth.savr.R
import com.zarnth.savr.presentation.collection.CollectionEvents
import com.zarnth.savr.presentation.collection.CollectionState
import com.zarnth.savr.presentation.collection.CollectionViewModel
import com.zarnth.savr.presentation.home.HomeEvents
import com.zarnth.savr.presentation.home.HomeState

@Composable
fun RootFab(
    currentTab: Int,
    homeState: HomeState,
    collectionState: CollectionState,
    isSearching: Boolean = false,
    onHomeFabClick: () -> Unit,
    onCollectionFabClick: () -> Unit
) {
    if (isSearching || homeState.isSelectionMode || collectionState.isSelectionMode ||
        collectionState.isDetailSelectionMode
    ) return

    when (currentTab) {
        0 -> FloatingActionButton(onClick = onHomeFabClick) {
            Icon(
                painterResource(R.drawable.add_icons),
                contentDescription = null,
                modifier = Modifier.size(26.dp)
            )
        }

        1 -> if (collectionState.selectedCollection == null) {
            FloatingActionButton(onClick = onCollectionFabClick) {
                Icon(
                    painterResource(R.drawable.add_icons),
                    contentDescription = null,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}
