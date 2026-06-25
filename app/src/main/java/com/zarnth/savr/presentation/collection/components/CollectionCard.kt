package com.zarnth.savr.presentation.collection.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.zarnth.savr.R
import com.zarnth.savr.domain.model.Collection

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationApi::class)
@Composable
fun CollectionCard(
    collection: Collection,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val cardModifier = if (isSelected) {
        modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(MaterialTheme.shapes.extraLarge)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.extraLarge
            )
            .combinedClickable(
                onClick = onLongClick,
                onLongClick = onLongClick
            )
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(6.dp)
    } else {
        modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(MaterialTheme.shapes.extraLarge)
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) onLongClick() else onClick()
                },
                onLongClick = onLongClick
            )
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(6.dp)
    }

    Column(modifier = cardModifier) {

        PhotoCollage(
            imageUrls = collection.previewUrls,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.2f)
                .clip(MaterialTheme.shapes.extraLarge)
        )

        Text(
            text = collection.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 8.dp)
        )

        val countText = "${collection.bookmarkCount} bookmark${if (collection.bookmarkCount != 1) "s" else ""}"
        Text(
            text = countText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
        )
    }
}

@Composable
private fun PhotoCollage(
    imageUrls: List<String>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        when (imageUrls.size) {
            0 -> NoPhotosPlaceholder()
            1 -> SinglePhoto(imageUrls[0])
            2 -> TwoPhotoCollage(imageUrls[0], imageUrls[1])
            3 -> ThreePhotoCollage(imageUrls[0], imageUrls[1], imageUrls[2])
            else -> FourPhotoCollage(imageUrls[0], imageUrls[1], imageUrls[2], imageUrls[3])
        }
    }
}

@Composable
private fun NoPhotosPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                        MaterialTheme.colorScheme.surfaceContainerLow
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.bookmark_one),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun SinglePhoto(url: String) {
    AsyncImage(
        model = url,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun TwoPhotoCollage(url1: String, url2: String) {
    Row(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = url1,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        )
        Spacer(modifier = Modifier.width(1.dp))
        AsyncImage(
            model = url2,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        )
    }
}

@Composable
private fun ThreePhotoCollage(url1: String, url2: String, url3: String) {
    Row(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = url1,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        )
        Spacer(modifier = Modifier.width(1.dp))
        Column(modifier = Modifier.weight(1f)) {
            AsyncImage(
                model = url2,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(1.dp))
            AsyncImage(
                model = url3,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FourPhotoCollage(url1: String, url2: String, url3: String, url4: String) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.weight(1f)) {
            AsyncImage(
                model = url1,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            )
            Spacer(modifier = Modifier.width(1.dp))
            AsyncImage(
                model = url2,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(1.dp))
        Row(modifier = Modifier.weight(1f)) {
            AsyncImage(
                model = url3,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            )
            Spacer(modifier = Modifier.width(1.dp))
            AsyncImage(
                model = url4,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            )
        }
    }
}
