package com.zarnth.savr.presentation.setting.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

@Composable
fun AutoBackupInfoDialog(
    onEnable: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Enable Auto Backup?",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = buildAnnotatedString {
                    append("Automatically backs up your bookmarks whenever they change.\n\n")
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                        append("Saved to:\n")
                    }
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                        append("Downloads/Savr/savr_autobackup.json")
                    }
                    append("\n\n")
                    append("The file stays even if you uninstall the app. ")
                    append("Use Restore in Settings to load it later.")
                },
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onEnable) {
                Text("Enable")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
