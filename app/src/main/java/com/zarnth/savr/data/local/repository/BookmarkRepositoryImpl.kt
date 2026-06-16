package com.zarnth.savr.data.local.repository

import com.zarnth.savr.data.local.dao.BookmarkDao
import com.zarnth.savr.data.local.dao.CollectionDao
import com.zarnth.savr.data.local.entity.BookmarkCollectionCrossRef
import com.zarnth.savr.data.local.mapper.toDomain
import com.zarnth.savr.data.local.mapper.toEntity
import com.zarnth.savr.domain.model.Bookmark
import com.zarnth.savr.domain.model.Collection
import com.zarnth.savr.domain.repository.BookmarkRepository
import com.zarnth.savr.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class BookmarkRepositoryImpl(
    private val dao: BookmarkDao,
    private val collectionDao: CollectionDao
) : BookmarkRepository {

    override suspend fun insert(bookmark: Bookmark) {
        dao.insert(bookmark.toEntity())
    }

    override suspend fun deleteBookmarks(bookmarks: List<Bookmark>) {
        dao.delete(bookmarks.map { it.toEntity() })
    }

    override suspend fun hideBookmarks(ids: List<Long>) {
        dao.hideBookmarks(ids)
    }

    override suspend fun searchBookmarks(text: String): Flow<Resource<List<Bookmark>>> {
        return dao.searchBookmarks(text)
            .map { list -> Resource.Success(list.map { it.toDomain() }) as Resource<List<Bookmark>> }
            .onStart { emit(Resource.Loading()) }
            .catch { e -> emit(Resource.Error(e.message ?: "Unknown error")) }
    }

    override fun getBookmarks(): Flow<Resource<List<Bookmark>>> {
        return dao.getBookmarks()
            .map { list -> Resource.Success(list.map { it.toDomain() }) as Resource<List<Bookmark>> }
            .onStart { emit(Resource.Loading()) }
            .catch { e -> emit(Resource.Error(e.message ?: "Unknown error")) }
    }

    override suspend fun createCollection(name: String): Long {
        return collectionDao.insertCollection(
            com.zarnth.savr.data.local.entity.CollectionEntity(name = name)
        )
    }

    override suspend fun deleteCollection(collection: Collection) {
        collectionDao.deleteCollection(collection.toEntity())
    }

    override fun getAllCollections(): Flow<Resource<List<Collection>>> {
        return collectionDao.getAllCollections()
            .map { list -> Resource.Success(list.map { it.toDomain() }) as Resource<List<Collection>> }
            .onStart { emit(Resource.Loading()) }
            .catch { e -> emit(Resource.Error(e.message ?: "Unknown error")) }
    }

    override fun getBookmarksInCollection(collectionId: Long): Flow<Resource<List<Bookmark>>> {
        return collectionDao.getCollectionWithBookmarks(collectionId)
            .map { result ->
                Resource.Success(result?.bookmarks?.map { it.toDomain() } ?: emptyList()) as Resource<List<Bookmark>>
            }
            .onStart { emit(Resource.Loading()) }
            .catch { e -> emit(Resource.Error(e.message ?: "Unknown error")) }
    }

    override suspend fun addBookmarkToCollection(bookmarkId: Long, collectionId: Long) {
        collectionDao.addBookmarkToCollection(BookmarkCollectionCrossRef(bookmarkId, collectionId))
    }

    override suspend fun removeBookmarkFromCollection(bookmarkId: Long, collectionId: Long) {
        collectionDao.removeBookmarkFromCollection(BookmarkCollectionCrossRef(bookmarkId, collectionId))
    }

    override fun getCollectionsForBookmark(bookmarkId: Long): Flow<List<Collection>> {
        return collectionDao.getCollectionsForBookmark(bookmarkId).map { list ->
            list.map { it.toDomain() }
        }
    }
}
