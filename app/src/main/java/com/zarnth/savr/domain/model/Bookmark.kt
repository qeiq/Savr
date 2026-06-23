package com.zarnth.savr.domain.model

data class Bookmark(
    val id: Long = 0,
    val url: String,
    val title: String?,
    val description: String?,
    val imageUrl: String?,
    val createdAt: Long = System.currentTimeMillis()
)