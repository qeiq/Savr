package com.zarnth.savr.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.zarnth.savr.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkPreviewSheet(
    showBottomSheet: Boolean,
    onDismissRequest: () -> Unit,
    imageURL: String,
    title: String?
) {
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            dragHandle = null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    AsyncImage(
                        model = imageURL,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(MaterialTheme.shapes.extraLarge),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(MaterialTheme.shapes.extraLarge)
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                            )
                    )
                    Text(
                        text = title.orEmpty(),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = {}) { Text("Open") }
                    Button(onClick = {}) { Text("Share") }
                    Button(onClick = {}) { Text("Delete") }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}