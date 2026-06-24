package com.zarnth.savr.presentation.setting.components

import android.content.Intent
import android.os.Build
import android.provider.DocumentsContract
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zarnth.savr.R
import com.zarnth.savr.presentation.setting.ExportState
import com.zarnth.savr.presentation.setting.ImportState
import com.zarnth.savr.presentation.setting.SettingEvents
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import com.zarnth.savr.presentation.setting.SettingState
import com.zarnth.savr.presentation.setting.SettingViewModel

@Composable
fun DataSection(
    state: SettingState,
    viewModel: SettingViewModel,
    importLauncher: ActivityResultLauncher<Intent>
) {
    Spacer(Modifier.height(12.dp))
    SectionHeader("Data")
    SettingItem(
        icon = R.drawable.backup_db,
        title = "Backup bookmarks",
        subtitle = "Save as JSON",
        onClick = {
            if (state.exportState !is ExportState.Loading) {
                viewModel.onEvent(SettingEvents.ExportData)
            }
        }
    )
    Spacer(Modifier.height(4.dp))
    SettingItem(
        icon = R.drawable.import_icon,
        title = "Restore bookmarks",
        subtitle = if (state.importState is ImportState.Loading) "Importing..." else "Import from JSON",
        onClick = {
            if (state.importState !is ImportState.Loading) {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/json"
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        putExtra(DocumentsContract.EXTRA_INITIAL_URI, DocumentsContract.buildDocumentUri(
                            "com.android.externalstorage.documents",
                            "primary:Download/Savr"
                        ))
                    }
                }
                importLauncher.launch(intent)
            }
        }
    )
    Spacer(Modifier.height(4.dp))
    SettingItem(
        icon = R.drawable.backup_icon,
        title = "Auto backup",
        subtitle = when {
            state.autoBackupEnabled && state.lastBackupTimeText.isNotEmpty() -> state.lastBackupTimeText
            state.autoBackupEnabled -> "No backup yet"
            else -> "Off"
        },
        trailing = {
            Switch(
                checked = state.autoBackupEnabled,
                onCheckedChange = { viewModel.onEvent(SettingEvents.ToggleAutoBackup(it)) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        onClick = { viewModel.onEvent(SettingEvents.ToggleAutoBackup(!state.autoBackupEnabled)) }
    )
}
