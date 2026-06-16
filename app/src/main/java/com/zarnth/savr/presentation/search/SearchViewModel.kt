package com.zarnth.savr.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zarnth.savr.domain.repository.BookmarkRepository
import com.zarnth.savr.utils.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    private val repository: BookmarkRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SearchState())
    val state = _state.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }

        searchJob?.cancel()
        if (query.isBlank()) {
            _state.update { it.copy(searchResults = emptyList(), isLoading = false) }
            return
        }

        searchJob = viewModelScope.launch {
            repository.searchBookmarks(query).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.update { it.copy(isLoading = true) }
                    is Resource.Error -> _state.update { it.copy(isLoading = false, error = resource.errorMessage ?: "Error") }
                    is Resource.Success -> _state.update { it.copy(isLoading = false, searchResults = resource.data ?: emptyList()) }
                }
            }
        }
    }
}
