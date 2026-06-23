package com.zarnth.savr.presentation.collection

import com.zarnth.savr.domain.model.Bookmark
import com.zarnth.savr.domain.model.Collection
import com.zarnth.savr.presentation.setting.SortOrder

data class CollectionState(
    val collections: List<Collection> = emptyList(),
    val isLoading: Boolean = false,
    val error: String = "",
    val showCreateDialog: Boolean = false,
    val inputName: String = "",
    val selectedCollection: Collection? = null,
    val collectionBookmarks: List<Bookmark> = emptyList(),
    val isDetailLoading: Boolean = false,
    val selectedIds: Set<Long> = emptySet(),
    val isSelectionMode: Boolean = false,
    val detailSelectedIds: Set<Long> = emptySet(),
    val isDetailSelectionMode: Boolean = false,
    val tempBookmark: Bookmark? = null,
    val isDetailBodySheet: Boolean = false,
    val sortOrder: SortOrder = SortOrder.DATE_NEWEST,
    val showSortSheet: Boolean = false
)
