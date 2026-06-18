package com.zarnth.savr.presentation.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zarnth.savr.domain.model.Bookmark
import com.zarnth.savr.domain.model.Collection
import com.zarnth.savr.domain.repository.BookmarkRepository
import com.zarnth.savr.utils.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CollectionViewModel(
    private val repository: BookmarkRepository
) : ViewModel() {
    private val _state = MutableStateFlow(CollectionState())
    val state = _state.asStateFlow()
    private var collectionJob: Job? = null

    init {
        loadCollections()
    }

    fun onEvent(event: CollectionEvents) {
        when (event) {
            is CollectionEvents.InputNameChanged -> {
                _state.update { it.copy(inputName = event.name) }
            }

            CollectionEvents.ShowCreateDialog -> {
                _state.update { it.copy(showCreateDialog = true, inputName = "") }
            }

            CollectionEvents.HideCreateDialog -> {
                _state.update { it.copy(showCreateDialog = false, inputName = "") }
            }

            CollectionEvents.CreateCollection -> {
                createCollection()
            }

            is CollectionEvents.SelectCollection -> {
                selectCollection(event.collection)
            }

            is CollectionEvents.ToggleSelection -> {
                val current = _state.value
                val newSelected = if (event.id in current.selectedIds) {
                    current.selectedIds - event.id
                } else {
                    current.selectedIds + event.id
                }
                _state.update {
                    it.copy(
                        selectedIds = newSelected,
                        isSelectionMode = newSelected.isNotEmpty()
                    )
                }
            }

            CollectionEvents.ClearSelection -> {
                _state.update { it.copy(selectedIds = emptySet(), isSelectionMode = false) }
            }

            CollectionEvents.SelectAll -> {
                val allIds = _state.value.collections.map { it.id }.toSet()
                _state.update {
                    it.copy(
                        selectedIds = allIds,
                        isSelectionMode = allIds.isNotEmpty()
                    )
                }
            }

            CollectionEvents.DeselectAll -> {
                _state.update { it.copy(selectedIds = emptySet(), isSelectionMode = false) }
            }

            CollectionEvents.DeleteSelected -> {
                deleteSelected()
            }

            is CollectionEvents.DeleteCollectionById -> {
                deleteCollectionById(event.collectionId)
            }

            is CollectionEvents.ShowDetailBodySheet -> {
                _state.update { it.copy(tempBookmark = event.bookmark, isDetailBodySheet = true) }
            }

            CollectionEvents.DismissDetailBodySheet -> {
                _state.update { it.copy(tempBookmark = null, isDetailBodySheet = false) }
            }

            is CollectionEvents.ToggleDetailSelection -> {
                val current = _state.value
                val newSelected = if (event.id in current.detailSelectedIds) {
                    current.detailSelectedIds - event.id
                } else {
                    current.detailSelectedIds + event.id
                }
                _state.update {
                    it.copy(
                        detailSelectedIds = newSelected,
                        isDetailSelectionMode = newSelected.isNotEmpty()
                    )
                }
            }

            CollectionEvents.ClearDetailSelection -> {
                _state.update { it.copy(detailSelectedIds = emptySet(), isDetailSelectionMode = false) }
            }

            CollectionEvents.SelectAllDetail -> {
                val allIds = _state.value.collectionBookmarks.map { it.id }.toSet()
                _state.update {
                    it.copy(
                        detailSelectedIds = allIds,
                        isDetailSelectionMode = allIds.isNotEmpty()
                    )
                }
            }

            CollectionEvents.DeselectAllDetail -> {
                _state.update { it.copy(detailSelectedIds = emptySet(), isDetailSelectionMode = false) }
            }

            is CollectionEvents.RemoveSelectedFromCollection -> {
                removeSelectedFromCollection(event.collectionId)
            }
        }
    }

    fun backToCollections() {
        collectionJob?.cancel()
        _state.update { it.copy(selectedCollection = null, collectionBookmarks = emptyList()) }
    }

    private fun selectCollection(collection: Collection) {
        collectionJob?.cancel()
        _state.update { it.copy(selectedCollection = collection, collectionBookmarks = emptyList(), isDetailLoading = true) }
        collectionJob = viewModelScope.launch {
            repository.getBookmarksInCollection(collection.id).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.update { it.copy(isDetailLoading = true) }
                    is Resource.Error -> _state.update { it.copy(isDetailLoading = false, error = resource.errorMessage ?: "Error") }
                    is Resource.Success -> _state.update { it.copy(isDetailLoading = false, collectionBookmarks = resource.data ?: emptyList()) }
                }
            }
        }
    }

    private fun createCollection() {
        val name = _state.value.inputName.trim()
        if (name.isEmpty()) return
        viewModelScope.launch {
            repository.createCollection(name)
            _state.update { it.copy(showCreateDialog = false, inputName = "") }
        }
    }

    private fun deleteSelected() {
        val selected = _state.value.collections.filter { it.id in _state.value.selectedIds }
        if (selected.isEmpty()) return
        viewModelScope.launch {
            selected.forEach { repository.deleteCollection(it) }
            _state.update { it.copy(selectedIds = emptySet(), isSelectionMode = false) }
        }
    }

    private fun deleteCollectionById(collectionId: Long) {
        viewModelScope.launch {
            val collection = _state.value.collections.find { it.id == collectionId } ?: return@launch
            repository.deleteCollection(collection)
            backToCollections()
        }
    }

    private fun removeSelectedFromCollection(collectionId: Long) {
        val ids = _state.value.detailSelectedIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            ids.forEach { repository.removeBookmarkFromCollection(it, collectionId) }
            _state.update { it.copy(detailSelectedIds = emptySet(), isDetailSelectionMode = false) }
        }
    }

    private fun loadCollections() {
        viewModelScope.launch {
            repository.getAllCollections().collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.update { it.copy(isLoading = true) }
                    is Resource.Error -> _state.update { it.copy(isLoading = false, error = resource.errorMessage ?: "Error") }
                    is Resource.Success -> _state.update { it.copy(isLoading = false, collections = resource.data ?: emptyList()) }
                }
            }
        }
    }
}
