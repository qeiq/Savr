package com.zarnth.savr.domain.repository

import com.zarnth.savr.data.local.entity.BookmarkEntity
import com.zarnth.savr.domain.model.Bookmark
import com.zarnth.savr.utils.Resource
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {
    suspend fun insert(bookmark: Bookmark)
    fun getBookmarks(): Flow<Resource<List<Bookmark>>>
    suspend fun deleteBookmarks(bookmarks: List<Bookmark>)

    suspend fun searchBookmarks(text: String): Flow<Resource<List<Bookmark>>>
}