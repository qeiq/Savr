package com.zarnth.savr.presentation.home

import com.zarnth.savr.domain.model.Bookmark
import com.zarnth.savr.domain.model.SortOrder

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
    val selectedIds: Set<Long> = emptySet(),
    val isSelectionMode: Boolean = false,
    val showCollectionPicker: Boolean = false,
    val collections: List<com.zarnth.savr.domain.model.Collection> = emptyList(),
    val sortOrder: SortOrder = SortOrder.DATE_NEWEST,
    val showSortSheet: Boolean = false
)
