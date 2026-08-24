package com.zarnth.savr.domain.model

data class Collection(
    val id: Long = 0,
    val name: String,
    val bookmarkCount: Int = 0,
    val previewUrls: List<String> = emptyList(),
    val parentCollectionId: Long? = null,
    val subCollections: List<Collection> = emptyList(),
    val createdAt: Long = 0
)
