package com.zarnth.savr.presentation.setting

import android.os.Build
import android.os.Build.VERSION_CODES
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zarnth.savr.data.backup.BackupManager
import com.zarnth.savr.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingViewModel(
    private val settingsRepository: SettingsRepository,
    private val backupManager: BackupManager
) : ViewModel() {

    private val _state = MutableStateFlow(
        SettingState(
            themeMode = settingsRepository.getThemeMode(),
            tapAction = settingsRepository.getTapAction(),
            dynamicColor = settingsRepository.getDynamicColor(),
            isDynamicColorSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
            viewMode = settingsRepository.getViewMode(),
            autoBackupEnabled = settingsRepository.getAutoBackupEnabled()
        )
    )
    val state = _state.asStateFlow()

    init {
        if (settingsRepository.getAutoBackupEnabled()) {
            backupManager.refreshLastBackupTime()
        }
        viewModelScope.launch {
            backupManager.lastBackupTimeMillis.collect { millis ->
                if (_state.value.autoBackupEnabled) {
                    _state.update { it.copy(lastBackupTimeText = formatLastBackupTime(millis)) }
                }
            }
        }
    }

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

            is SettingEvents.SelectTapAction -> {
                settingsRepository.setTapAction(event.action)
                _state.update {
                    it.copy(
                        tapAction = event.action,
                        showTapActionSheet = false
                    )
                }
            }

            SettingEvents.ShowTapActionSheet -> {
                _state.update { it.copy(showTapActionSheet = true) }
            }

            SettingEvents.HideTapActionSheet -> {
                _state.update { it.copy(showTapActionSheet = false) }
            }

            SettingEvents.ExportData -> {
                exportData()
            }

            is SettingEvents.ToggleDynamicColor -> {
                settingsRepository.setDynamicColor(event.enabled)
                _state.update { it.copy(dynamicColor = event.enabled) }
            }

            is SettingEvents.ToggleViewMode -> {
                settingsRepository.setViewMode(event.viewMode)
                _state.update {
                    it.copy(
                        viewMode = event.viewMode,
                        showViewModeSheet = false
                    )
                }
            }

            SettingEvents.ShowViewModeSheet -> {
                _state.update { it.copy(showViewModeSheet = true) }
            }

            SettingEvents.HideViewModeSheet -> {
                _state.update { it.copy(showViewModeSheet = false) }
            }

            SettingEvents.DismissExport -> {
                _state.update { it.copy(exportState = ExportState.Idle) }
            }

            is SettingEvents.ImportData -> {
                importData(event.json)
            }

            SettingEvents.DismissImportResult -> {
                _state.update { it.copy(importState = ImportState.Idle) }
            }

            is SettingEvents.ToggleAutoBackup -> {
                if (event.enabled) {
                    _state.update { it.copy(showAutoBackupInfoDialog = true) }
                } else {
                    settingsRepository.setAutoBackupEnabled(false)
                    backupManager.stopAutoBackup()
                    _state.update { it.copy(autoBackupEnabled = false, lastBackupTimeText = "") }
                }
            }

            SettingEvents.ConfirmAutoBackupEnable -> {
                settingsRepository.setAutoBackupEnabled(true)
                backupManager.startAutoBackup()
                backupManager.refreshLastBackupTime()
                _state.update { it.copy(autoBackupEnabled = true, showAutoBackupInfoDialog = false) }
            }

            SettingEvents.DismissAutoBackupInfoDialog -> {
                _state.update { it.copy(showAutoBackupInfoDialog = false) }
            }

            is SettingEvents.ImportBrowserBookmarks -> {
                importBrowserBookmarks(event.html)
            }

            SettingEvents.DismissBrowserImportResult -> {
                _state.update { it.copy(browserImportState = BrowserImportState.Idle) }
            }
        }
    }

    private fun exportData() {
        viewModelScope.launch {
            _state.update { it.copy(exportState = ExportState.Loading) }
            try {
                val jsonString = backupManager.generateBackupJson()
                _state.update { it.copy(exportState = ExportState.Ready(jsonString)) }
            } catch (e: Exception) {
                _state.update { it.copy(exportState = ExportState.Error(e.message ?: "Export failed")) }
            }
        }
    }

    private fun importData(jsonString: String) {
        viewModelScope.launch {
            _state.update { it.copy(importState = ImportState.Loading) }
            try {
                backupManager.importFromJson(jsonString)
                _state.update { it.copy(importState = ImportState.Success) }
            } catch (e: Exception) {
                _state.update { it.copy(importState = ImportState.Error(e.message ?: "Import failed")) }
            }
        }
    }

    private fun importBrowserBookmarks(html: String) {
        viewModelScope.launch {
            _state.update { it.copy(browserImportState = BrowserImportState.Loading) }
            try {
                val result = backupManager.importFromBrowserBookmarks(html)
                _state.update { it.copy(browserImportState = BrowserImportState.Success(result.imported, result.skipped, result.collections)) }
            } catch (e: Exception) {
                _state.update { it.copy(browserImportState = BrowserImportState.Error(e.message ?: "Import failed")) }
            }
        }
    }

    private fun formatLastBackupTime(millis: Long): String {
        if (millis <= 0L) return ""
        val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        return "Last backup: ${sdf.format(Date(millis))}"
    }
}
