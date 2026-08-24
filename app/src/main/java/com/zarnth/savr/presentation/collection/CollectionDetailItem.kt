package com.zarnth.savr.presentation.collection

import com.zarnth.savr.domain.model.Bookmark
import com.zarnth.savr.domain.model.Collection

sealed class CollectionDetailItem {
    data class BookmarkRow(val bookmark: Bookmark) : CollectionDetailItem()
    data class SubCollectionRow(val collection: Collection) : CollectionDetailItem()

    val createdAt: Long
        get() = when (this) {
            is BookmarkRow -> bookmark.createdAt
            is SubCollectionRow -> collection.createdAt
        }

    val title: String
        get() = when (this) {
            is BookmarkRow -> bookmark.title ?: ""
            is SubCollectionRow -> collection.name
        }
}
