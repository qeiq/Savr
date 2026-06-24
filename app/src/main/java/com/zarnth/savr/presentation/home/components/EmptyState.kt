package com.zarnth.savr.presentation.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmptyBookmarkState() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "┑(￣Д ￣)┍", fontSize = 36.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            text = "No bookmarks yet",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
