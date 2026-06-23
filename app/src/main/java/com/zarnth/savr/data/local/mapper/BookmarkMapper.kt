package com.zarnth.savr.data.local.mapper

import com.zarnth.savr.data.local.entity.BookmarkEntity
import com.zarnth.savr.domain.model.Bookmark


fun BookmarkEntity.toDomain(): Bookmark {
    return Bookmark(
        id = id,
        url = url,
        title = title,
        description = description,
        imageUrl = imageUrl,
        createdAt = createdAt
    )
}

fun Bookmark.toEntity(): BookmarkEntity {
    return BookmarkEntity(
        id = id,
        url = url,
        title = title,
        description = description,
        imageUrl = imageUrl,
        createdAt = createdAt
    )
}