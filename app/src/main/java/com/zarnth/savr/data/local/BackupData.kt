package com.zarnth.savr.data.local

import kotlinx.serialization.Serializable

@Serializable
data class BackupBookmark(
    val url: String,
    val title: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isCollectionOnly: Boolean = false,
    val isPinned: Boolean = false,
    val pinnedAt: Long? = null
)

@Serializable
data class BackupCollection(
    val name: String,
    val bookmarkUrls: List<String> = emptyList(),
    val pinnedBookmarkUrls: List<String> = emptyList(),
    val parentName: String? = null
)

@Serializable
data class BackupData(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val bookmarks: List<BackupBookmark> = emptyList(),
    val collections: List<BackupCollection> = emptyList()
)
