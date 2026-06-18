package com.zarnth.savr.data.local.repository

import android.content.Context
import com.zarnth.savr.domain.repository.SettingsRepository
import com.zarnth.savr.presentation.setting.TapAction
import com.zarnth.savr.ui.theme.ThemeMode
import androidx.core.content.edit

class SettingsRepositoryImpl(private val context: Context) : SettingsRepository {

    companion object {
        private const val PREFS_NAME = "savr_settings"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_TAP_ACTION = "tap_action"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getThemeMode(): ThemeMode {
        val ordinal = prefs.getInt(KEY_THEME_MODE, ThemeMode.SYSTEM.ordinal)
        return ThemeMode.entries.getOrElse(ordinal) { ThemeMode.SYSTEM }
    }

    override fun setThemeMode(mode: ThemeMode) {
        prefs.edit { putInt(KEY_THEME_MODE, mode.ordinal) }
    }

    override fun getTapAction(): TapAction {
        val ordinal = prefs.getInt(KEY_TAP_ACTION, TapAction.SHOW_PREVIEW.ordinal)
        return TapAction.entries.getOrElse(ordinal) { TapAction.SHOW_PREVIEW }
    }

    override fun setTapAction(action: TapAction) {
        prefs.edit { putInt(KEY_TAP_ACTION, action.ordinal) }
    }

    override fun getDynamicColor(): Boolean {
        return prefs.getBoolean(KEY_DYNAMIC_COLOR, true)
    }

    override fun setDynamicColor(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_DYNAMIC_COLOR, enabled) }
    }
}
