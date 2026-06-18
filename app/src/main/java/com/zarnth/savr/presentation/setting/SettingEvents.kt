package com.zarnth.savr.presentation.setting

import com.zarnth.savr.ui.theme.ThemeMode

sealed class SettingEvents {
    data class SelectTheme(val themeMode: ThemeMode) : SettingEvents()
    object ShowThemeSheet : SettingEvents()
    object HideThemeSheet : SettingEvents()
    data class SelectTapAction(val action: TapAction) : SettingEvents()
    object ShowTapActionSheet : SettingEvents()
    object HideTapActionSheet : SettingEvents()
    data class ToggleDynamicColor(val enabled: Boolean) : SettingEvents()
    data class ToggleViewMode(val viewMode: ViewMode) : SettingEvents()
    object ShowViewModeSheet : SettingEvents()
    object HideViewModeSheet : SettingEvents()
    object ExportData : SettingEvents()
    object DismissExport : SettingEvents()
    data class ImportData(val json: String) : SettingEvents()
    object DismissImportResult : SettingEvents()
}
