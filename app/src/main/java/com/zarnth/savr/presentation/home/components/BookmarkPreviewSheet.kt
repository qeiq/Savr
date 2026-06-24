package com.zarnth.savr.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import android.os.Build
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
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
    openInBrowser: () -> Unit,
    copyLinkButtonClick: () -> Unit
) {
    val context = LocalContext.current
    if (!showBottomSheet) return

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        ListItem(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
                .clickable {
                    openInBrowser()
                    onDismissRequest()
                },
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            headlineContent = {
                Text("Open In Browser")
            },
            leadingContent = {
                Icon(
                    painter = painterResource(R.drawable.open_in_browser),
                    contentDescription = "Copy Link"
                )
            }
        )
        ListItem(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
                .clickable {
                    copyLinkButtonClick()
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                        Toast.makeText(context, "Link copied", Toast.LENGTH_SHORT).show()
                    }
                    onDismissRequest()
                },
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            headlineContent = {
                Text("Copy Link")
            },
            leadingContent = {
                Icon(
                    painter = painterResource(R.drawable.copy_icon),
                    contentDescription = "Copy Link"
                )
            }
        )


        Spacer(Modifier.height(24.dp))
    }
}