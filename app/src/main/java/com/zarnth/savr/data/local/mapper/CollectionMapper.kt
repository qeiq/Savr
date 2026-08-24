package com.zarnth.savr.data.local.mapper

import com.zarnth.savr.data.local.dao.CollectionWithCount
import com.zarnth.savr.data.local.entity.CollectionEntity
import com.zarnth.savr.domain.model.Collection

fun CollectionEntity.toDomain(): Collection {
    return Collection(
        id = id,
        name = name,
        parentCollectionId = parentCollectionId,
        createdAt = createdAt
    )
}

fun CollectionWithCount.toDomain(): Collection {
    return Collection(
        id = id,
        name = name,
        bookmarkCount = bookmarkCount,
        parentCollectionId = parentCollectionId,
        createdAt = createdAt
    )
}

fun Collection.toEntity(): CollectionEntity {
    return CollectionEntity(
        id = id,
        name = name,
        parentCollectionId = parentCollectionId,
        createdAt = createdAt
    )
}
