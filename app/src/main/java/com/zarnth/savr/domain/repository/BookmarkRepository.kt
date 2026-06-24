package com.zarnth.savr.domain.repository

import com.zarnth.savr.domain.model.Bookmark
import com.zarnth.savr.domain.model.Collection
import com.zarnth.savr.utils.Resource
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {
    suspend fun insert(bookmark: Bookmark): Boolean
    fun getBookmarks(): Flow<Resource<List<Bookmark>>>
    suspend fun deleteBookmarks(bookmarks: List<Bookmark>)
    suspend fun hideBookmarks(ids: List<Long>)
    suspend fun searchBookmarks(text: String): Flow<Resource<List<Bookmark>>>

    suspend fun createCollection(name: String): Long
    suspend fun deleteCollection(collection: Collection)
    fun getAllCollections(): Flow<Resource<List<Collection>>>
    fun getBookmarksInCollection(collectionId: Long): Flow<Resource<List<Bookmark>>>
    suspend fun addBookmarkToCollection(bookmarkId: Long, collectionId: Long)
    suspend fun removeBookmarkFromCollection(bookmarkId: Long, collectionId: Long)
    fun getCollectionsForBookmark(bookmarkId: Long): Flow<List<Collection>>
}
