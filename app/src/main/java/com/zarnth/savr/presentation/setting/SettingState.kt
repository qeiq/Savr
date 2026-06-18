package com.zarnth.savr.presentation.setting

import com.zarnth.savr.ui.theme.ThemeMode

data class SettingState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val showThemeSheet: Boolean = false
)
