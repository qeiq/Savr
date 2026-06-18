package com.zarnth.savr.presentation.setting

import androidx.lifecycle.ViewModel
import com.zarnth.savr.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingState(themeMode = settingsRepository.getThemeMode()))
    val state = _state.asStateFlow()

    fun onEvent(event: SettingEvents) {
        when (event) {
            is SettingEvents.SelectTheme -> {
                settingsRepository.setThemeMode(event.themeMode)
                _state.update {
                    it.copy(
                        themeMode = event.themeMode,
                        showThemeSheet = false
                    )
                }
            }

            SettingEvents.ShowThemeSheet -> {
                _state.update { it.copy(showThemeSheet = true) }
            }

            SettingEvents.HideThemeSheet -> {
                _state.update { it.copy(showThemeSheet = false) }
            }
        }
    }
}
