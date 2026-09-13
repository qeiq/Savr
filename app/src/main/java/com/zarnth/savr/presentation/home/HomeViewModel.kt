package com.zarnth.savr.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zarnth.savr.domain.model.Bookmark
import com.zarnth.savr.domain.model.Collection
import com.zarnth.savr.domain.repository.BookmarkRepository
import com.zarnth.savr.domain.repository.SettingsRepository
import com.zarnth.savr.link_fetcher.LinkMetadataParser
import com.zarnth.savr.domain.model.SortOrder
import com.zarnth.savr.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class HomeViewModel(
    private val repository: BookmarkRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(
        HomeState(sortOrder = settingsRepository.getSortOrder())
    )
    val state = _state.asStateFlow()

    private val parser = LinkMetadataParser()
    private var rawBookmarks: List<Bookmark> = emptyList()
    private var isFetchingMetadata = false
    private var lastClipboardText: String? = null

    init {
        loadBookmarks()
        loadCollections()
        fetchMissingMetadataOnStart()
    }

    fun homeEvents(events: HomeEvents) {
        when (events) {
            is HomeEvents.OnTextFieldValueChange -> {
                _state.update { it.copy(inputUrl = events.text) }
            }

            HomeEvents.SaveBookmark -> {
                saveBookmark()
            }

            HomeEvents.OnDialogDismissClick -> {
                _state.update {
                    it.copy(
                        isDialog = !it.isDialog,
                    )
                }
            }

            HomeEvents.FabClick -> {
                _state.update { it.copy(isDialog = true) }
            }

            is HomeEvents.PreviewImageClick -> {
                _state.update {
                    it.copy(
                        isPhotoPreviewDialog = true,
                        dialogPhotoUrl = events.url
                    )
                }
            }

            HomeEvents.PreviewImageDismissClick -> {
                _state.update {
                    it.copy(isPhotoPreviewDialog = false)
                }
            }

            is HomeEvents.BookmarkPreviewClick -> {
                _state.update {
                    it.copy(
                        tempBookmark = events.bookmark,
                        isBodySheet = true
                    )
                }
            }

            HomeEvents.BookmarkPreviewDismissClick -> {
                _state.update {
                    it.copy(
                        isBodySheet = false,
                        tempBookmark = null
                    )
                }
            }

            HomeEvents.ShowEditBookmarkSheet -> {
                val bookmark = _state.value.tempBookmark
                if (bookmark != null) {
                    _state.update {
                        it.copy(
                            isBodySheet = false,
                            editingBookmark = bookmark,
                            editTitle = bookmark.title ?: "",
                            editDescription = bookmark.description ?: "",
                            isEditBookmarkSheet = true
                        )
                    }
                }
            }

            HomeEvents.HideEditBookmarkSheet -> {
                _state.update {
                    it.copy(
                        isEditBookmarkSheet = false,
                        editingBookmark = null,
                        editTitle = "",
                        editDescription = ""
                    )
                }
            }

            is HomeEvents.EditTitleChanged -> {
                _state.update { it.copy(editTitle = events.text) }
            }

            is HomeEvents.EditDescriptionChanged -> {
                _state.update { it.copy(editDescription = events.text) }
            }

            HomeEvents.SaveEditedBookmark -> {
                saveEditedBookmark()
            }

            is HomeEvents.ToggleSelection -> {
                val current = _state.value
                val newSelected = if (events.id in current.selectedIds) {
                    current.selectedIds - events.id
                } else {
                    current.selectedIds + events.id
                }
                _state.update {
                    it.copy(
                        selectedIds = newSelected,
                        isSelectionMode = newSelected.isNotEmpty()
                    )
                }
            }

            HomeEvents.DeleteSelected -> {
                deleteSelected()
            }

            HomeEvents.ConfirmDeleteSelected -> {
                _state.update { it.copy(showDeleteConfirm = false) }
                deleteSelected()
            }

            HomeEvents.ShowDeleteConfirmDialog -> {
                _state.update { it.copy(showDeleteConfirm = true) }
            }

            HomeEvents.HideDeleteConfirmDialog -> {
                _state.update { it.copy(showDeleteConfirm = false) }
            }

            HomeEvents.ClearSelection -> {
                _state.update {
                    it.copy(
                        selectedIds = emptySet(),
                        isSelectionMode = false
                    )
                }
            }

            is HomeEvents.TogglePin -> {
                togglePin(events.id)
            }

            HomeEvents.SelectAll -> {
                val allIds = _state.value.bookmarkData.map { it.id }.toSet()
                _state.update {
                    it.copy(
                        selectedIds = allIds,
                        isSelectionMode = allIds.isNotEmpty()
                    )
                }
            }

            HomeEvents.DeselectAll -> {
                _state.update {
                    it.copy(
                        selectedIds = emptySet(),
                        isSelectionMode = false
                    )
                }
            }

            HomeEvents.ShowCollectionPicker -> {
                _state.update { it.copy(showCollectionPicker = true) }
            }

            HomeEvents.HideCollectionPicker -> {
                _state.update { it.copy(showCollectionPicker = false) }
            }

            is HomeEvents.AddToCollection -> {
                addSelectedToCollection(events.collectionId)
            }

            is HomeEvents.SetSortOrder -> {
                settingsRepository.setSortOrder(events.sortOrder)
                _state.update {
                    it.copy(
                        sortOrder = events.sortOrder,
                        showSortSheet = false,
                        bookmarkData = sortBookmarks(rawBookmarks, events.sortOrder)
                    )
                }
            }

            HomeEvents.ShowSortSheet -> {
                _state.update { it.copy(showSortSheet = true) }
            }

            HomeEvents.HideSortSheet -> {
                _state.update { it.copy(showSortSheet = false) }
            }

            is HomeEvents.ClipboardDetected -> {
                onClipboardDetected(events.text)
            }

            HomeEvents.DismissClipboardSheet -> {
                _state.update { it.copy(clipboardSuggestion = null, isClipboardLoading = false) }
            }

            HomeEvents.AddClipboardBookmark -> {
                addClipboardBookmark()
            }

            HomeEvents.DuplicateToastShown -> {
                _state.update { it.copy(duplicateToastKey = 0) }
            }
        }
    }

    private fun deleteSelected() {
        val ids = _state.value.selectedIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            try {
                repository.hideBookmarks(ids)
                _state.update {
                    it.copy(
                        selectedIds = emptySet(),
                        isSelectionMode = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = e.message ?: "Delete failed")
                }
            }
        }
    }

    private fun togglePin(id: Long) {
        val bookmark = rawBookmarks.find { it.id == id } ?: return
        val newPinned = !bookmark.isPinned
        viewModelScope.launch {
            try {
                repository.setBookmarkPinned(
                    id,
                    newPinned,
                    if (newPinned) System.currentTimeMillis() else null
                )
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Pin failed") }
            }
        }
    }

    private fun saveEditedBookmark() {
        val bookmark = _state.value.editingBookmark ?: return
        val title = _state.value.editTitle.trim().ifBlank { null }
        val description = _state.value.editDescription.trim().ifBlank { null }
        viewModelScope.launch {
            try {
                repository.updateTitleAndDescription(bookmark.id, title, description)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Update failed") }
            }
        }
    }

    fun saveBookmark() {

        val rawUrl = _state.value.inputUrl.trim()

        if (rawUrl.isEmpty()) return
        val url = if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) {
            "https://$rawUrl"
        } else {
            rawUrl
        }
        viewModelScope.launch {
            try {
                if (repository.existsOnHomeByUrl(url)) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            duplicateToastKey = it.duplicateToastKey + 1
                        )
                    }
                    return@launch
                }
                _state.update { it.copy(isLoading = true) }
                val meta = parser.parse(url)
                val bookmarkUrl = meta?.url ?: url

                if (repository.existsOnHomeByUrl(bookmarkUrl)) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            duplicateToastKey = it.duplicateToastKey + 1
                        )
                    }
                    return@launch
                }

                val bookmark = Bookmark(
                    url = bookmarkUrl,
                    title = meta?.title,
                    description = meta?.description,
                    imageUrl = meta?.imageUrl
                )
                val inserted = repository.insertToHome(bookmark)
                _state.update {
                    it.copy(
                        isLoading = false,
                        inputUrl = if (inserted) "" else it.inputUrl,
                        duplicateToastKey = if (inserted) it.duplicateToastKey else it.duplicateToastKey + 1
                    )
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = e.message ?: "Unknown error",
                        isLoading = false
                    )
                }
            }
        }
    }


    private fun loadCollections() {
        viewModelScope.launch {
            repository.getAllCollections().collect { resource ->
                if (resource is Resource.Success) {
                    _state.update { it.copy(collections = resource.data ?: emptyList()) }
                }
            }
        }
    }

    private fun addSelectedToCollection(collectionId: Long) {
        val ids = _state.value.selectedIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repository.addBookmarksToCollection(ids, collectionId)
            _state.update {
                it.copy(
                    showCollectionPicker = false,
                    selectedIds = emptySet(),
                    isSelectionMode = false
                )
            }
        }
    }

    private fun onClipboardDetected(text: String?) {
        if (text.isNullOrBlank()) return
        if (text == lastClipboardText) return
        lastClipboardText = text
        val url = extractUrlFromText(text) ?: return
        viewModelScope.launch {
            if (repository.existsOnHomeByUrl(url)) return@launch
            _state.update {
                it.copy(
                    clipboardSuggestion = ClipboardSuggestion(url = url),
                    isClipboardLoading = true
                )
            }
            try {
                val meta = parser.parse(url)
                val current = _state.value.clipboardSuggestion ?: return@launch
                _state.update {
                    it.copy(
                        isClipboardLoading = false,
                        clipboardSuggestion = current.copy(
                            title = meta?.title ?: current.title,
                            description = meta?.description ?: current.description,
                            imageUrl = meta?.imageUrl ?: current.imageUrl
                        )
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isClipboardLoading = false) }
            }
        }
    }

    private fun addClipboardBookmark() {
        val suggestion = _state.value.clipboardSuggestion ?: return
        viewModelScope.launch {
            try {
                val inserted = repository.insertToHome(
                    Bookmark(
                        url = suggestion.url,
                        title = suggestion.title,
                        description = suggestion.description,
                        imageUrl = suggestion.imageUrl
                    )
                )
                _state.update {
                    it.copy(
                        clipboardSuggestion = null,
                        duplicateToastKey = if (inserted) it.duplicateToastKey else it.duplicateToastKey + 1
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Unknown error") }
            }
        }
    }

    private fun extractUrlFromText(text: String): String? {
        val schemeMatch = Regex("""(https?://[^\s<>"']+)""", RegexOption.IGNORE_CASE).find(text)
        val raw = schemeMatch?.value ?: text.trim()
        if (raw.isBlank()) return null
        val cleaned = raw.trim().trimEnd('.', ',', ';', '!', '?', ')', ']', '}')
        if (cleaned.startsWith("http://") || cleaned.startsWith("https://")) return cleaned
        if (cleaned.contains(".") && !cleaned.contains(" ")) return "https://$cleaned"
        return null
    }

    private fun loadBookmarks() {
        viewModelScope.launch {
            repository.getBookmarks().collect { data ->
                when (data) {
                    is Resource.Error<*> -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = data.errorMessage ?: "Unknown error"
                            )
                        }
                    }

                    is Resource.Loading<*> -> {
                        _state.update { it.copy(isLoading = true) }
                    }

                    is Resource.Success<*> -> {
                        val items = data.data ?: emptyList()
                        rawBookmarks = items
                        val sortOrder = _state.value.sortOrder
                        _state.update {
                            it.copy(
                                isLoading = false,
                                bookmarkData = sortBookmarks(items, sortOrder)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun fetchMissingMetadataOnStart() {
        viewModelScope.launch(Dispatchers.IO) {
            val missing = repository.getBookmarksMissingMetadata()
            if (missing.isEmpty() || isFetchingMetadata) return@launch
            isFetchingMetadata = true
            val semaphore = Semaphore(3)
            missing.map { bm ->
                async {
                    semaphore.withPermit {
                        try {
                            val meta = parser.parse(bm.url)
                            if (meta != null) {
                                repository.updateMetadata(
                                    id = bm.id,
                                    title = meta.title?.takeIf { it.isNotBlank() } ?: bm.title,
                                    description = meta.description?.takeIf { it.isNotBlank() } ?: bm.description,
                                    imageUrl = meta.imageUrl?.takeIf { it.isNotBlank() } ?: bm.imageUrl
                                )
                            }
                        } catch (_: Exception) { }
                    }
                }
            }.forEach { it.await() }
            isFetchingMetadata = false
        }
    }

    private fun sortBookmarks(bookmarks: List<Bookmark>, sortOrder: SortOrder): List<Bookmark> {
        val sorted = when (sortOrder) {
            SortOrder.DATE_NEWEST -> bookmarks.sortedByDescending { it.createdAt }
            SortOrder.DATE_OLDEST -> bookmarks.sortedBy { it.createdAt }
            SortOrder.TITLE_ASC -> bookmarks.sortedBy { it.title?.lowercase() }
            SortOrder.TITLE_DESC -> bookmarks.sortedByDescending { it.title?.lowercase() }
        }
        return sorted.sortedWith(
            compareByDescending<Bookmark> { it.isPinned }
                .thenBy { it.pinnedAt ?: Long.MAX_VALUE }
        )
    }
}