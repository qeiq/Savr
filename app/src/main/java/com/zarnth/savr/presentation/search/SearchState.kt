package com.zarnth.savr.presentation.search

import com.zarnth.savr.domain.model.Bookmark

data class SearchState(
    val searchQuery: String = "",
    val searchResults: List<Bookmark> = emptyList(),
    val collectionSearchQuery: String = "",
    val collectionSearchResults: List<Bookmark> = emptyList(),
    val isCollectionSearching: Boolean = false,
    val isLoading: Boolean = false,
    val error: String = ""
)
