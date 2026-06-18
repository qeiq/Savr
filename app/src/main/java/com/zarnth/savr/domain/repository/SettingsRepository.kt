package com.zarnth.savr.domain.repository

import com.zarnth.savr.ui.theme.ThemeMode

interface SettingsRepository {
    fun getThemeMode(): ThemeMode
    fun setThemeMode(mode: ThemeMode)
}
