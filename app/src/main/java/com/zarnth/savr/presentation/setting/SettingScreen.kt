package com.zarnth.savr.presentation.setting

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.zarnth.savr.R
import com.zarnth.savr.openChromeTab
import com.zarnth.savr.presentation.home.components.LoadingProgress
import com.zarnth.savr.presentation.setting.components.SectionHeader
import com.zarnth.savr.presentation.setting.components.SettingItem
import com.zarnth.savr.presentation.setting.components.TapActionSheet
import com.zarnth.savr.presentation.setting.components.ThemeSheet
import com.zarnth.savr.presentation.setting.components.ViewModeSheet
import com.zarnth.savr.ui.theme.ThemeMode
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    viewModel: SettingViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val themeLabel = when (state.themeMode) {
        ThemeMode.LIGHT -> "Light"
        ThemeMode.DARK -> "Dark"
        ThemeMode.SYSTEM -> "System"
    }

    val tapActionLabel = when (state.tapAction) {
        TapAction.SHOW_PREVIEW -> "Preview"
        TapAction.OPEN_BROWSER -> "Open in browser"
        TapAction.COPY_LINK -> "Copy link"
    }

    val versionName = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }.getOrDefault("")

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            val json = (state.exportState as? ExportState.Ready)?.json
            if (json != null) {
                context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
            }
        }
        viewModel.onEvent(SettingEvents.DismissExport)
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val json =
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: ""
            if (json.isNotBlank()) viewModel.onEvent(SettingEvents.ImportData(json))
        }
    }

    LaunchedEffect(state.exportState) {
        if (state.exportState is ExportState.Ready) {
            exportLauncher.launch("savr_backup.json")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            SectionHeader("Theme")
            SettingItem(
                icon = R.drawable.dark_m_icon,
                title = "Theme",
                subtitle = themeLabel,
                onClick = { viewModel.onEvent(SettingEvents.ShowThemeSheet) }
            )
            if (state.isDynamicColorSupported) {
                Spacer(Modifier.height(4.dp))
                SettingItem(
                    icon = R.drawable.dynamic_one,
                    title = "Dynamic color",
                    subtitle = if (state.dynamicColor) "On" else "Off",
                    trailing = {
                        Switch(
                            checked = state.dynamicColor,
                            onCheckedChange = { viewModel.onEvent(SettingEvents.ToggleDynamicColor(it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    },
                    onClick = { viewModel.onEvent(SettingEvents.ToggleDynamicColor(!state.dynamicColor)) }
                )
            }
            Spacer(Modifier.height(12.dp))
            SectionHeader("General")
            SettingItem(
                icon = R.drawable.touch_icon,
                title = "Default tap action",
                subtitle = tapActionLabel,
                onClick = { viewModel.onEvent(SettingEvents.ShowTapActionSheet) }
            )
            Spacer(Modifier.height(4.dp))
            SettingItem(
                icon = if (state.viewMode == ViewMode.GRID) R.drawable.grid_icon else R.drawable.list_icon,
                title = "View mode",
                subtitle = if (state.viewMode == ViewMode.GRID) "Grid" else "List",
                onClick = { viewModel.onEvent(SettingEvents.ShowViewModeSheet) }
            )
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
                        importLauncher.launch(arrayOf("application/json", "*/*"))
                    }
                }
            )

            Spacer(Modifier.height(12.dp))

            SectionHeader("Community")
            SettingItem(
                icon = R.drawable.github_icon,
                title = "Star on GitHub",
                onClick = { openChromeTab("https://github.com/qeiq/Savr", context) }
            )
            Spacer(Modifier.height(4.dp))
            SettingItem(
                icon = R.drawable.bug_icon,
                title = "Report Issue",
                onClick = { openChromeTab("https://github.com/qeiq/Savr/issues", context) }
            )

            Spacer(Modifier.height(12.dp))

            SectionHeader("About")
            SettingItem(
                icon = R.drawable.about_icon,
                title = "App version",
                subtitle = versionName,
                onClick = { }
            )

            Spacer(Modifier.height(12.dp))
        }

        LoadingProgress(
            isLoading = state.exportState is ExportState.Loading || state.importState is ImportState.Loading
        )
    }

    if (state.showThemeSheet) {
        ThemeSheet(
            current = state.themeMode,
            onSelect = { viewModel.onEvent(SettingEvents.SelectTheme(it)) },
            onDismiss = { viewModel.onEvent(SettingEvents.HideThemeSheet) }
        )
    }

    if (state.showTapActionSheet) {
        TapActionSheet(
            current = state.tapAction,
            onSelect = { viewModel.onEvent(SettingEvents.SelectTapAction(it)) },
            onDismiss = { viewModel.onEvent(SettingEvents.HideTapActionSheet) }
        )
    }

    if (state.showViewModeSheet) {
        ViewModeSheet(
            current = state.viewMode,
            onSelect = { viewModel.onEvent(SettingEvents.ToggleViewMode(it)) },
            onDismiss = { viewModel.onEvent(SettingEvents.HideViewModeSheet) }
        )
    }

    if (state.exportState is ExportState.Error) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(SettingEvents.DismissExport) },
            title = { Text("Export failed") },
            text = { Text((state.exportState as ExportState.Error).message) },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(SettingEvents.DismissExport) }) {
                    Text("OK")
                }
            }
        )
    }

    if (state.importState is ImportState.Success) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(SettingEvents.DismissImportResult) },
            title = { Text("Import successful") },
            text = { Text("Your bookmarks and collections have been restored.") },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(SettingEvents.DismissImportResult) }) {
                    Text("OK")
                }
            }
        )
    }

    if (state.importState is ImportState.Error) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(SettingEvents.DismissImportResult) },
            title = { Text("Import failed") },
            text = { Text((state.importState as ImportState.Error).message) },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(SettingEvents.DismissImportResult) }) {
                    Text("OK")
                }
            }
        )
    }
}
