package com.zarnth.savr.domain.repository

import com.zarnth.savr.presentation.setting.TapAction
import com.zarnth.savr.presentation.setting.ViewMode
import com.zarnth.savr.ui.theme.ThemeMode

interface SettingsRepository {
    fun getThemeMode(): ThemeMode
    fun setThemeMode(mode: ThemeMode)
    fun getTapAction(): TapAction
    fun setTapAction(action: TapAction)
    fun getDynamicColor(): Boolean
    fun setDynamicColor(enabled: Boolean)
    fun getViewMode(): ViewMode
    fun setViewMode(mode: ViewMode)
}
