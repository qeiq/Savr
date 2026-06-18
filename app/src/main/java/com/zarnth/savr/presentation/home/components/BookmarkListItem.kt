package com.zarnth.savr.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BookmarkListItem(
    imageUrl: String?,
    title: String?,
    description: String?,
    modifier: Modifier = Modifier,
    photoClickUrl: (String) -> Unit,
    bodyClick: () -> Unit,
    onLongClick: () -> Unit,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    url: String
) {
    val host = url.toUri().host.orEmpty()
    val cleanHost = if (host.startsWith("www.")) host.removePrefix("www.") else host

    var imageFailed by remember { mutableStateOf(false) }
    var faviconFailed by remember { mutableStateOf(false) }
    var imageAspectRatio by remember { mutableStateOf(1f) }
    val hasImage = !imageUrl.isNullOrBlank() && !imageFailed

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
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
    } else {
        modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(MaterialTheme.shapes.extraLarge)
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) onLongClick() else bodyClick()
                },
                onLongClick = onLongClick
            )
            .background(MaterialTheme.colorScheme.surfaceContainer)
    }

    Row(
        modifier = cardModifier
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Left: text content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Favicon + host
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (faviconFailed) {
                        Text(
                            text = cleanHost.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.labelSmall
                        )
                    } else {
                        AsyncImage(
                            model = "https://t0.gstatic.com/faviconV2?client=SOCIAL&type=FAVICON&fallback_opts=TYPE,SIZE,URL&url=http://${host}&size=128",
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(20.dp)
                                .clip(MaterialTheme.shapes.extraSmall),
                            onError = { faviconFailed = true }
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = cleanHost,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }

            Spacer(Modifier.height(2.dp))

            // Title
            if (!title.isNullOrBlank()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Description
            val desc = description.takeUnless { it.isNullOrBlank() } ?: title
            if (desc != null) {
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                )
            }

            // Fallback: if no title and no description, show host as main text
            if (title.isNullOrBlank() && desc == null) {
                Text(
                    text = url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Right: image thumbnail
        if (hasImage) {
            Spacer(Modifier.width(14.dp))
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .width(120.dp)
                    .aspectRatio(imageAspectRatio)
                    .clip(MaterialTheme.shapes.large)
                    .clickable {
                        if (isSelectionMode) onLongClick() else photoClickUrl(imageUrl)
                    },
                onError = { imageFailed = true },
                onSuccess = { state ->
                    val size = state.painter.intrinsicSize
                    if (size.width.isFinite() && size.width > 0 && size.height > 0) {
                        imageAspectRatio = size.width / size.height
                    }
                }
            )
        }
    }
}
