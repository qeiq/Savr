package com.zarnth.savr.presentation.root.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.zarnth.savr.R
import com.zarnth.savr.presentation.collection.CollectionState
import com.zarnth.savr.presentation.home.HomeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RootFab(
    currentTab: Int,
    homeState: HomeState,
    collectionState: CollectionState,
    isSearching: Boolean = false,
    onHomeFabClick: () -> Unit,
    onCollectionFabClick: () -> Unit,
    onCollectionAddBookmarkClick: () -> Unit,
    onCollectionAddSubCollectionClick: () -> Unit = {}
) {
    if (isSearching || homeState.isSelectionMode || collectionState.isSelectionMode ||
        collectionState.isDetailSelectionMode
    ) return

    when (currentTab) {
        0 -> FloatingActionButton(
            onClick = onHomeFabClick,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(
                painterResource(R.drawable.add_icons),
                contentDescription = "Add bookmark",
                modifier = Modifier.size(26.dp)
            )
        }

        1 -> if (collectionState.selectedCollection == null) {
            FloatingActionButton(
                onClick = onCollectionFabClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    painterResource(R.drawable.add_icons),
                    contentDescription = "Create collection",
                    modifier = Modifier.size(26.dp)
                )
            }
        } else {
            SpeedDialFab(
                onAddBookmark = onCollectionAddBookmarkClick,
                onAddSubCollection = onCollectionAddSubCollectionClick
            )
        }
    }
}

@Composable
private fun SpeedDialFab(
    onAddBookmark: () -> Unit,
    onAddSubCollection: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation = animateFloatAsState(
        targetValue = if (expanded) 135f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "fab_rotation"
    )

    Box(contentAlignment = Alignment.BottomEnd) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SpeedDialItem(
                visible = expanded,
                index = 0,
                label = "Sub Collection",
                iconRes = R.drawable.folder_add,
                onClick = {
                    expanded = false
                    onAddSubCollection()
                }
            )

            SpeedDialItem(
                visible = expanded,
                index = 1,
                label = "Bookmark",
                iconRes = R.drawable.bookmark_add,
                onClick = {
                    expanded = false
                    onAddBookmark()
                }
            )

            FloatingActionButton(
                onClick = { expanded = !expanded },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Icon(
                    painterResource(R.drawable.add_icons),
                    contentDescription = if (expanded) "Close menu" else "Add to collection",
                    modifier = Modifier
                        .size(26.dp)
                        .graphicsLayer { rotationZ = rotation.value }
                )
            }
        }
    }
}

@Composable
private fun SpeedDialItem(
    visible: Boolean,
    index: Int,
    label: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    val scale = remember { Animatable(0f) }

    LaunchedEffect(visible) {
        if (visible) {
            delay(index * 50L)
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        } else {
            scale.snapTo(0f)
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(120)) + slideInVertically(tween(200, easing = FastOutSlowInEasing)) { it / 2 },
        exit = fadeOut(tween(80)) + slideOutVertically(tween(120)) { it / 2 }
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            shadowElevation = 4.dp,
            modifier = Modifier.scale(scale.value)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    painterResource(iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}