package com.zarnth.savr.data.local.repository

import com.zarnth.savr.data.local.dao.BookmarkDao
import com.zarnth.savr.data.local.dao.CollectionDao
import com.zarnth.savr.data.local.entity.BookmarkCollectionCrossRef
import com.zarnth.savr.data.local.entity.CollectionEntity
import com.zarnth.savr.data.local.mapper.toDomain
import com.zarnth.savr.data.local.mapper.toEntity
import com.zarnth.savr.domain.model.Bookmark
import com.zarnth.savr.domain.model.Collection
import com.zarnth.savr.domain.repository.BookmarkRepository
import com.zarnth.savr.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class BookmarkRepositoryImpl(
    private val dao: BookmarkDao,
    private val collectionDao: CollectionDao
) : BookmarkRepository {

    override suspend fun insert(bookmark: Bookmark): Boolean {
        return dao.insertOrUnhide(bookmark.toEntity())
    }

    override suspend fun insertToHome(bookmark: Bookmark): Boolean {
        val entity = bookmark.toEntity()
        if (dao.existsOnHomeByUrl(bookmark.url)) return false
        val hidden = dao.findHiddenByUrl(bookmark.url)
        if (hidden != null) {
            dao.unhideBookmark(hidden.id, entity.title, entity.description, entity.imageUrl, entity.createdAt)
            return true
        }
        val collectionOnly = dao.findCollectionOnlyByUrl(bookmark.url)
        if (collectionOnly != null) {
            dao.convertCollectionOnlyToHome(collectionOnly.id, entity.title, entity.description, entity.imageUrl, entity.createdAt)
            return true
        }
        dao.insert(entity)
        return true
    }

    override suspend fun existsByUrl(url: String): Boolean {
        return dao.existsByUrl(url)
    }

    override suspend fun existsOnHomeByUrl(url: String): Boolean {
        return dao.existsOnHomeByUrl(url)
    }

    override suspend fun getBookmarkIdByUrl(url: String): Long? {
        return dao.getBookmarkByUrl(url)?.id
    }

    override suspend fun getBookmarksWithoutImage(): List<Bookmark> {
        return dao.getBookmarksWithoutImageOnce().map { it.toDomain() }
    }

    override suspend fun getBookmarksMissingMetadata(): List<Bookmark> {
        return dao.getBookmarksMissingMetadataOnce().map { it.toDomain() }
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
            CollectionEntity(name = name)
        )
    }

    override suspend fun createCollection(name: String, parentCollectionId: Long?): Long {
        return collectionDao.insertCollection(
            CollectionEntity(name = name, parentCollectionId = parentCollectionId)
        )
    }

    override suspend fun deleteCollection(collection: Collection) {
        collectionDao.deleteCollection(collection.toEntity())
    }

    override suspend fun renameCollection(id: Long, name: String) {
        collectionDao.renameCollection(id, name)
    }

    override fun getAllCollections(): Flow<Resource<List<Collection>>> {
        return collectionDao.getAllCollections()
            .flatMapLatest { collections ->
                flow {
                    val result = collections.map { c ->
                        Collection(
                            id = c.id,
                            name = c.name,
                            bookmarkCount = c.bookmarkCount,
                            previewUrls = collectionDao.getPreviewUrlsForCollection(c.id),
                            parentCollectionId = c.parentCollectionId,
                            createdAt = c.createdAt
                        )
                    }
                    emit(Resource.Success(result) as Resource<List<Collection>>)
                }
            }
            .onStart { emit(Resource.Loading()) }
            .catch { e -> emit(Resource.Error(e.message ?: "Unknown error")) }
    }

    override fun getSubCollections(parentCollectionId: Long): Flow<Resource<List<Collection>>> {
        return collectionDao.getSubCollections(parentCollectionId)
            .flatMapLatest { collections ->
                flow {
                    val result = collections.map { c ->
                        Collection(
                            id = c.id,
                            name = c.name,
                            bookmarkCount = c.bookmarkCount,
                            previewUrls = collectionDao.getPreviewUrlsForCollection(c.id),
                            parentCollectionId = c.parentCollectionId,
                            createdAt = c.createdAt
                        )
                    }
                    emit(Resource.Success(result) as Resource<List<Collection>>)
                }
            }
            .onStart { emit(Resource.Loading()) }
            .catch { e -> emit(Resource.Error(e.message ?: "Unknown error")) }
    }

    override suspend fun getCollectionById(id: Long): Collection? {
        return collectionDao.getCollectionById(id)?.toDomain()
    }

    override fun getBookmarksInCollection(collectionId: Long): Flow<Resource<List<Bookmark>>> {
        return collectionDao.getBookmarksInCollection(collectionId)
            .map { list ->
                Resource.Success(
                    list.map { it.bookmark.toDomain().copy(isPinned = it.pinnedInCollection, pinnedAt = it.pinnedAtInCollection) }
                ) as Resource<List<Bookmark>>
            }
            .onStart { emit(Resource.Loading()) }
            .catch { e -> emit(Resource.Error(e.message ?: "Unknown error")) }
    }

    override suspend fun addBookmarkToCollection(bookmarkId: Long, collectionId: Long) {
        collectionDao.addBookmarkToCollection(BookmarkCollectionCrossRef(bookmarkId, collectionId))
    }

    override suspend fun addBookmarksToCollection(bookmarkIds: List<Long>, collectionId: Long) {
        collectionDao.addBookmarksToCollection(bookmarkIds.map { BookmarkCollectionCrossRef(it, collectionId) })
    }

    override suspend fun removeBookmarkFromCollection(bookmarkId: Long, collectionId: Long) {
        collectionDao.removeBookmarkFromCollection(BookmarkCollectionCrossRef(bookmarkId, collectionId))
    }

    override suspend fun isUrlInCollection(url: String, collectionId: Long): Boolean {
        return collectionDao.isUrlInCollection(url, collectionId)
    }

    override suspend fun updateImageUrl(id: Long, imageUrl: String?) {
        dao.updateImageUrl(id, imageUrl)
    }

    override suspend fun updateMetadata(id: Long, title: String?, description: String?, imageUrl: String?) {
        dao.updateMetadata(id, title, description, imageUrl)
    }

    override suspend fun updateTitleAndDescription(id: Long, title: String?, description: String?) {
        dao.updateTitleAndDescription(id, title, description)
    }

    override suspend fun setBookmarkPinned(id: Long, isPinned: Boolean, pinnedAt: Long?) {
        dao.setPinned(id, isPinned, pinnedAt)
    }

    override suspend fun setBookmarkPinnedInCollection(bookmarkId: Long, collectionId: Long, isPinned: Boolean, pinnedAt: Long?) {
        collectionDao.setPinnedInCollection(bookmarkId, collectionId, isPinned, pinnedAt)
    }

    override suspend fun setParentCollection(collectionId: Long, parentCollectionId: Long?) {
        collectionDao.setParentCollection(collectionId, parentCollectionId)
    }
}
