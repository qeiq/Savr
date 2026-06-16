package com.zarnth.savr.data.local.mapper

import com.zarnth.savr.data.local.dao.CollectionWithCount
import com.zarnth.savr.data.local.entity.CollectionEntity
import com.zarnth.savr.data.local.entity.CollectionWithBookmarks
import com.zarnth.savr.domain.model.Collection

fun CollectionEntity.toDomain(): Collection {
    return Collection(
        id = id,
        name = name
    )
}

fun CollectionWithCount.toDomain(): Collection {
    return Collection(
        id = id,
        name = name,
        bookmarkCount = bookmarkCount,
        previewUrls = previewUrls?.split("|||")?.filter { it.isNotBlank() } ?: emptyList()
    )
}

fun Collection.toEntity(): CollectionEntity {
    return CollectionEntity(
        id = id,
        name = name
    )
}

fun CollectionWithBookmarks.toDomain(): Collection {
    return Collection(
        id = collection.id,
        name = collection.name,
        bookmarkCount = bookmarks.size
    )
}
