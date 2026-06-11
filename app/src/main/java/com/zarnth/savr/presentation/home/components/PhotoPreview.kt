package com.zarnth.savr.presentation.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage

@Composable
fun PhotoPreview(
    isDialog: Boolean = false,
    onDismissRequest: () -> Unit = {},
    imageURL: String = "",
) {
    if (isDialog) {
        Dialog(onDismissRequest = { onDismissRequest.invoke() }) {
            Box(
                modifier = Modifier
                    .wrapContentSize(),

                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imageURL,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }


}