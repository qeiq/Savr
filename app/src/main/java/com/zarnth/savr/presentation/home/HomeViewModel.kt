package com.zarnth.savr.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zarnth.savr.domain.model.Bookmark
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
        // fetching bookmarks from the db
        loadBookmarks()
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
        }
    }

    fun saveBookmark() {

        val url = _state.value.inputUrl.trim()

        if (url.isEmpty()) return
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