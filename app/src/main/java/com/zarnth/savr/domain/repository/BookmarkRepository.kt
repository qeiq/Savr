package com.zarnth.savr.domain.repository

import com.zarnth.savr.domain.model.Bookmark
import com.zarnth.savr.domain.model.Collection
import com.zarnth.savr.utils.Resource
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {
    suspend fun insert(bookmark: Bookmark): Boolean
    suspend fun insertToHome(bookmark: Bookmark): Boolean
    suspend fun existsByUrl(url: String): Boolean
    suspend fun existsOnHomeByUrl(url: String): Boolean
    suspend fun getBookmarkIdByUrl(url: String): Long?
    fun getBookmarks(): Flow<Resource<List<Bookmark>>>
    suspend fun getBookmarksWithoutImage(): List<Bookmark>
    suspend fun getBookmarksMissingMetadata(): List<Bookmark>
    suspend fun hideBookmarks(ids: List<Long>)
    suspend fun searchBookmarks(text: String): Flow<Resource<List<Bookmark>>>

    suspend fun createCollection(name: String): Long
    suspend fun createCollection(name: String, parentCollectionId: Long?): Long
    suspend fun renameCollection(id: Long, name: String)
    suspend fun deleteCollection(collection: Collection)
    fun getAllCollections(): Flow<Resource<List<Collection>>>
    fun getSubCollections(parentCollectionId: Long): Flow<Resource<List<Collection>>>
    suspend fun getCollectionById(id: Long): Collection?
    fun getBookmarksInCollection(collectionId: Long): Flow<Resource<List<Bookmark>>>
    suspend fun addBookmarkToCollection(bookmarkId: Long, collectionId: Long)
    suspend fun addBookmarksToCollection(bookmarkIds: List<Long>, collectionId: Long)
    suspend fun removeBookmarkFromCollection(bookmarkId: Long, collectionId: Long)
    suspend fun isUrlInCollection(url: String, collectionId: Long): Boolean
    suspend fun updateImageUrl(id: Long, imageUrl: String?)
    suspend fun updateMetadata(id: Long, title: String?, description: String?, imageUrl: String?)
    suspend fun updateTitleAndDescription(id: Long, title: String?, description: String?)
    suspend fun setBookmarkPinned(id: Long, isPinned: Boolean, pinnedAt: Long?)
    suspend fun setBookmarkPinnedInCollection(bookmarkId: Long, collectionId: Long, isPinned: Boolean, pinnedAt: Long?)
    suspend fun setParentCollection(collectionId: Long, parentCollectionId: Long?)
}
