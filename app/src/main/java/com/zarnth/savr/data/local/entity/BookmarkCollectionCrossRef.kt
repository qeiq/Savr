package com.zarnth.savr.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "bookmark_collection_cross_ref",
    primaryKeys = ["bookmarkId", "collectionId"],
    foreignKeys = [
        ForeignKey(
            entity = BookmarkEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookmarkId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("bookmarkId"), Index("collectionId")]
)
data class BookmarkCollectionCrossRef(
    val bookmarkId: Long,
    val collectionId: Long
)
