package com.zarnth.savr.presentation.setting

import android.os.Build
import android.os.Build.VERSION_CODES
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zarnth.savr.data.local.BackupBookmark
import com.zarnth.savr.data.local.BackupCollection
import com.zarnth.savr.data.local.BackupData
import com.zarnth.savr.domain.model.Bookmark
import com.zarnth.savr.domain.repository.BookmarkRepository
import com.zarnth.savr.domain.repository.SettingsRepository
import com.zarnth.savr.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SettingViewModel(
    private val settingsRepository: SettingsRepository,
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    private val _state = MutableStateFlow(
        SettingState(
            themeMode = settingsRepository.getThemeMode(),
            tapAction = settingsRepository.getTapAction(),
            dynamicColor = settingsRepository.getDynamicColor(),
            isDynamicColorSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
            viewMode = settingsRepository.getViewMode()
        )
    )
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
        }
    }

    private fun exportData() {
        viewModelScope.launch {
            _state.update { it.copy(exportState = ExportState.Loading) }
            try {
                val bookmarksRes = bookmarkRepository.getBookmarks().first { it !is Resource.Loading }
                val collectionsRes = bookmarkRepository.getAllCollections().first { it !is Resource.Loading }

                if (bookmarksRes is Resource.Error) {
                    _state.update { it.copy(exportState = ExportState.Error(bookmarksRes.errorMessage ?: "Failed to load bookmarks")) }
                    return@launch
                }
                if (collectionsRes is Resource.Error) {
                    _state.update { it.copy(exportState = ExportState.Error(collectionsRes.errorMessage ?: "Failed to load collections")) }
                    return@launch
                }

                val bookmarkList = bookmarksRes.data ?: emptyList()
                val collectionList = collectionsRes.data ?: emptyList()

                val backupBookmarks = bookmarkList.map { BackupBookmark(url = it.url, title = it.title, description = it.description, imageUrl = it.imageUrl) }

                val backupCollections = collectionList.mapNotNull { collection ->
                    val bookmarksInRes = bookmarkRepository.getBookmarksInCollection(collection.id).first { it !is Resource.Loading }
                    val urls = bookmarksInRes.data?.map { it.url } ?: emptyList()
                    if (collection.name.isNotBlank()) BackupCollection(name = collection.name, bookmarkUrls = urls) else null
                }

                val data = BackupData(bookmarks = backupBookmarks, collections = backupCollections)
                val jsonString = json.encodeToString(data)
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
                val backupData = json.decodeFromString<BackupData>(jsonString)

                val existingRes = bookmarkRepository.getBookmarks().first { it !is Resource.Loading }
                val existingUrls = existingRes.data?.map { it.url }?.toSet() ?: emptySet()

                for (b in backupData.bookmarks) {
                    if (b.url !in existingUrls) {
                        val bookmark = Bookmark(url = b.url, title = b.title, description = b.description, imageUrl = b.imageUrl)
                        bookmarkRepository.insert(bookmark)
                    }
                }

                val allRes = bookmarkRepository.getBookmarks().first { it !is Resource.Loading }
                val bookmarkMap = allRes.data?.associateBy { it.url } ?: emptyMap()

                val existingCollectionsRes = bookmarkRepository.getAllCollections().first { it !is Resource.Loading }
                val existingCollectionNameToId = existingCollectionsRes.data?.associateBy { it.name }?.mapValues { it.value.id } ?: emptyMap()
                val collectionNameToId = existingCollectionNameToId.toMutableMap()

                for (c in backupData.collections) {
                    val collectionId = collectionNameToId.getOrPut(c.name) {
                        bookmarkRepository.createCollection(c.name)
                    }
                    c.bookmarkUrls.forEach { url ->
                        bookmarkMap[url]?.let { bm ->
                            bookmarkRepository.addBookmarkToCollection(bm.id, collectionId)
                        }
                    }
                }

                _state.update { it.copy(importState = ImportState.Success) }
            } catch (e: Exception) {
                _state.update { it.copy(importState = ImportState.Error(e.message ?: "Import failed")) }
            }
        }
    }
}
