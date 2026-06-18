package com.zarnth.savr.presentation.setting

import com.zarnth.savr.ui.theme.ThemeMode

sealed class SettingEvents {
    data class SelectTheme(val themeMode: ThemeMode) : SettingEvents()
    object ShowThemeSheet : SettingEvents()
    object HideThemeSheet : SettingEvents()
}
