package com.zarnth.savr.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BookmarkCard(
    imageUrl: String?,
    title: String?,
    description: String?,
    modifier: Modifier = Modifier,
    photoClickUrl: (String) -> Unit,
    bodyClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable {
                bodyClick()
            }
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(4.dp)
    ) {


        if (!imageUrl.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
                    .clip(MaterialTheme.shapes.extraLarge)

                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceContainerHigh,
                                MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        )
                    )
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            photoClickUrl(imageUrl)
                        }
                )

                // gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.3f)
                                )
                            )
                        )
                )
            }
        }


        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {

            if (!title.isNullOrBlank()) {
                Text(
                    text = title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            (description.takeUnless { it.isNullOrBlank() } ?: title)?.let {
                Text(
                    text = it,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

