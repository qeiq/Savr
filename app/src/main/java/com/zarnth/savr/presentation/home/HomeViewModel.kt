package com.zarnth.savr.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zarnth.savr.domain.model.Bookmark
import com.zarnth.savr.domain.model.Collection
import com.zarnth.savr.domain.repository.BookmarkRepository
import com.zarnth.savr.link_fetcher.LinkMetadataParser
import com.zarnth.savr.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: BookmarkRepository) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    val parser = LinkMetadataParser()

    init {
        loadBookmarks()
        loadCollections()
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

            HomeEvents.ClearSelection -> {
                _state.update {
                    it.copy(
                        selectedIds = emptySet(),
                        isSelectionMode = false
                    )
                }
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
        }
    }

    fun searchBookmarks(searchQuery: String) {
        viewModelScope.launch {
            repository.searchBookmarks(searchQuery).collect { search ->
                when (search) {
                    is Resource.Error<*> -> {
                        Log.d("Search VM", "Error ${search.errorMessage}")
                    }

                    is Resource.Loading<*> -> {
                        Log.d("Search VM", "Loading...")
                    }

                    is Resource.Success<*> -> {
                        Log.d("Search VM", "Result ${search.data}")

                    }
                }
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
                _state.update { it.copy(isLoading = true) }
                val meta = parser.parse(url)

                val bookmark = Bookmark(
                    url = meta?.url ?: url,
                    title = meta?.title,
                    description = meta?.description,
                    imageUrl = meta?.imageUrl
                )
                repository.insert(bookmark)
                _state.update { it.copy(isLoading = false, inputUrl = "") }

            } catch (e: Exception) {
                _state.update {
                    it.copy(error = e.message ?: "Unknown error")
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
        val ids = _state.value.selectedIds
        viewModelScope.launch {
            ids.forEach { id ->
                repository.addBookmarkToCollection(id, collectionId)
            }
            _state.update {
                it.copy(
                    showCollectionPicker = false,
                    selectedIds = emptySet(),
                    isSelectionMode = false
                )
            }
        }
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
                        _state.update {
                            it.copy(
                                isLoading = false,
                                bookmarkData = data.data ?: emptyList()
                            )
                        }
                        Log.d("BRO", "Madafaka why isnt it Parsed? : ${data.data}")
                    }
                }
            }
        }
    }


}