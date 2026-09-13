package com.zarnth.savr.presentation.home

import com.zarnth.savr.domain.model.Bookmark
import com.zarnth.savr.domain.model.SortOrder

data class ClipboardSuggestion(
    val url: String,
    val title: String? = null,
    val description: String? = null,
    val imageUrl: String? = null
)

data class HomeState(
    val isLoading: Boolean = false,
    val error: String = "",
    val bookmarkData: List<Bookmark> = emptyList(),
    val inputUrl: String = "",
    val isDialog: Boolean = false,
    val isPhotoPreviewDialog: Boolean = false,
    val dialogPhotoUrl: String = "",
    val tempBookmark: Bookmark? = null,
    val isBodySheet: Boolean = false,
    val editingBookmark: Bookmark? = null,
    val isEditBookmarkSheet: Boolean = false,
    val editTitle: String = "",
    val editDescription: String = "",
    val selectedIds: Set<Long> = emptySet(),
    val isSelectionMode: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val showCollectionPicker: Boolean = false,
    val collections: List<com.zarnth.savr.domain.model.Collection> = emptyList(),
    val sortOrder: SortOrder = SortOrder.DATE_NEWEST,
    val showSortSheet: Boolean = false,
    val duplicateToastKey: Int = 0,
    val clipboardSuggestion: ClipboardSuggestion? = null,
    val isClipboardLoading: Boolean = false
)
