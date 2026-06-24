package com.zarnth.savr.presentation.setting.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

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
                text = "Your bookmarks will be automatically saved whenever they change.\n\n" +
                        "File location:\n" +
                        "Downloads/Savr/savr_autobackup.json\n\n" +
                        "This file is visible in your file manager and " +
                        "persists even if you uninstall the app.\n\n" +
                        "To restore your data later, go to Settings \u2192 Data \u2192 " +
                        "Restore bookmarks and select the auto-backup file.",
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
