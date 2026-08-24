package com.zarnth.savr.presentation.collection

import com.zarnth.savr.domain.model.Bookmark
import com.zarnth.savr.domain.model.Collection
import com.zarnth.savr.domain.model.SortOrder

data class CollectionDetailData(
    val collection: Collection? = null,
    val detailItems: List<CollectionDetailItem> = emptyList(),
    val isLoading: Boolean = true
)

data class CollectionState(
    val collections: List<Collection> = emptyList(),
    val isLoading: Boolean = false,
    val error: String = "",
    val showCreateDialog: Boolean = false,
    val isRenameDialogVisible: Boolean = false,
    val renamingCollection: Collection? = null,
    val inputName: String = "",
    val selectedCollection: Collection? = null,
    val collectionBookmarks: List<Bookmark> = emptyList(),
    val detailItems: List<CollectionDetailItem> = emptyList(),
    val isDetailLoading: Boolean = false,
    val selectedIds: Set<Long> = emptySet(),
    val isSelectionMode: Boolean = false,
    val detailSelectedIds: Set<Long> = emptySet(),
    val isDetailSelectionMode: Boolean = false,
    val tempBookmark: Bookmark? = null,
    val isDetailBodySheet: Boolean = false,
    val editingBookmark: Bookmark? = null,
    val isEditBookmarkSheet: Boolean = false,
    val editTitle: String = "",
    val editDescription: String = "",
    val sortOrder: SortOrder = SortOrder.DATE_NEWEST,
    val showSortSheet: Boolean = false,
    val showAddBookmarkSheet: Boolean = false,
    val inputUrl: String = "",
    val isAddBookmarkLoading: Boolean = false,
    val duplicateToastKey: Int = 0,
    val showCreateSubCollectionDialog: Boolean = false,
    val creatingSubCollectionFor: Collection? = null,
    val parentCollection: Collection? = null,
    val subCollections: List<Collection> = emptyList(),
    val isSubCollectionsLoading: Boolean = false,
    val deleteTarget: Collection? = null,
    val detailDataById: Map<Long, CollectionDetailData> = emptyMap()
)
