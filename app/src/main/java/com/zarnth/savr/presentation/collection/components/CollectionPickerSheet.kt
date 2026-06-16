package com.zarnth.savr.presentation.collection.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zarnth.savr.R
import com.zarnth.savr.domain.model.Collection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionPickerSheet(
    collections: List<Collection>,
    onSelectCollection: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 4.dp, top = 8.dp)
            ) {
                Text(
                    text = "Add to Collection",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }

            if (collections.isEmpty()) {
                Text(
                    text = "No collections yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)
                )
            } else {
                LazyColumn {
                    items(collections, key = { it.id }) { collection ->
                        ListItem(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.extraLarge)
                                .clickable { onSelectCollection(collection.id) },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            headlineContent = {
                                Text(
                                    text = collection.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = "${collection.bookmarkCount} bookmark${if (collection.bookmarkCount != 1) "s" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            leadingContent = {
                                Icon(
                                    painter = painterResource(R.drawable.bookmark_one),
                                    tint = MaterialTheme.colorScheme.surfaceTint,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
