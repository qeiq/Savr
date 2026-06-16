package com.zarnth.savr.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class CollectionWithBookmarks(
    @Embedded val collection: CollectionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = BookmarkCollectionCrossRef::class,
            parentColumn = "collectionId",
            entityColumn = "bookmarkId"
        )
    )
    val bookmarks: List<BookmarkEntity>
)
