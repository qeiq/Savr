package com.zarnth.savr.presentation.setting

import com.zarnth.savr.ui.theme.ThemeMode

enum class TapAction { SHOW_PREVIEW, OPEN_BROWSER, COPY_LINK }

enum class ViewMode { GRID, LIST }

sealed class ExportState {
    object Idle : ExportState()
    object Loading : ExportState()
    data class Ready(val json: String) : ExportState()
    data class Error(val message: String) : ExportState()
}

sealed class ImportState {
    object Idle : ImportState()
    object Loading : ImportState()
    object Success : ImportState()
    data class Error(val message: String) : ImportState()
}

sealed class BrowserImportState {
    object Idle : BrowserImportState()
    object Loading : BrowserImportState()
    data class Success(val imported: Int, val skipped: Int, val collections: Int) : BrowserImportState()
    data class Error(val message: String) : BrowserImportState()
}

data class SettingState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val showThemeSheet: Boolean = false,
    val tapAction: TapAction = TapAction.SHOW_PREVIEW,
    val showTapActionSheet: Boolean = false,
    val dynamicColor: Boolean = true,
    val isDynamicColorSupported: Boolean = false,
    val viewMode: ViewMode = ViewMode.GRID,
    val showViewModeSheet: Boolean = false,
    val autoBackupEnabled: Boolean = false,
    val lastBackupTimeText: String = "",
    val showAutoBackupInfoDialog: Boolean = false,
    val exportState: ExportState = ExportState.Idle,
    val importState: ImportState = ImportState.Idle,
    val browserImportState: BrowserImportState = BrowserImportState.Idle
)
