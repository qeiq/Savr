package com.zarnth.savr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.zarnth.savr.data.local.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Insert
    suspend fun insert(bookmarkEntity: BookmarkEntity)

    @Insert
    suspend fun insertWithReturn(bookmarkEntity: BookmarkEntity): Long

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE url = :url AND isHidden = 0)")
    suspend fun existsByUrl(url: String): Boolean

    @Query("SELECT * FROM bookmarks WHERE url = :url AND isHidden = 0 LIMIT 1")
    suspend fun getBookmarkByUrl(url: String): BookmarkEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE url = :url AND isHidden = 0 AND isCollectionOnly = 0)")
    suspend fun existsOnHomeByUrl(url: String): Boolean

    @Query("SELECT * FROM bookmarks WHERE url = :url AND isHidden = 0 AND isCollectionOnly = 1 LIMIT 1")
    suspend fun findCollectionOnlyByUrl(url: String): BookmarkEntity?

    @Query("UPDATE bookmarks SET isHidden = 0, isCollectionOnly = 0, title = :title, description = :description, imageUrl = :imageUrl, createdAt = :createdAt WHERE id = :id")
    suspend fun convertCollectionOnlyToHome(id: Long, title: String?, description: String?, imageUrl: String?, createdAt: Long)

    @Query("SELECT * FROM bookmarks WHERE isHidden = 0 AND isCollectionOnly = 0")
    fun getBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE isHidden = 0")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE isHidden = 0 AND isCollectionOnly = 0 AND (title LIKE '%' || :searchQuery || '%' OR url LIKE '%' || :searchQuery || '%')")
    fun searchBookmarks(searchQuery: String): Flow<List<BookmarkEntity>>

    @Query("UPDATE bookmarks SET isHidden = 1 WHERE id IN (:ids)")
    suspend fun hideBookmarks(ids: List<Long>)

    @Query("SELECT * FROM bookmarks WHERE url = :url AND isHidden = 1 LIMIT 1")
    suspend fun findHiddenByUrl(url: String): BookmarkEntity?

    @Query("UPDATE bookmarks SET isHidden = 0, title = :title, description = :description, imageUrl = :imageUrl, createdAt = :createdAt WHERE id = :id")
    suspend fun unhideBookmark(id: Long, title: String?, description: String?, imageUrl: String?, createdAt: Long)

    @Query("UPDATE bookmarks SET imageUrl = :imageUrl WHERE id = :id")
    suspend fun updateImageUrl(id: Long, imageUrl: String?)

    @Query("UPDATE bookmarks SET title = :title, description = :description, imageUrl = :imageUrl WHERE id = :id")
    suspend fun updateMetadata(id: Long, title: String?, description: String?, imageUrl: String?)

    @Query("UPDATE bookmarks SET title = :title, description = :description WHERE id = :id")
    suspend fun updateTitleAndDescription(id: Long, title: String?, description: String?)

    @Query("UPDATE bookmarks SET isPinned = :isPinned, pinnedAt = :pinnedAt WHERE id = :id")
    suspend fun setPinned(id: Long, isPinned: Boolean, pinnedAt: Long?)

    @Query("SELECT * FROM bookmarks WHERE isHidden = 0")
    suspend fun getBookmarksOnce(): List<BookmarkEntity>

    @Query("SELECT * FROM bookmarks WHERE isHidden = 0 OR id IN (SELECT bookmarkId FROM bookmark_collection_cross_ref)")
    fun getAllBookmarksForBackup(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE isHidden = 0 OR id IN (SELECT bookmarkId FROM bookmark_collection_cross_ref)")
    suspend fun getAllBookmarksForBackupOnce(): List<BookmarkEntity>

    @Query("SELECT * FROM bookmarks WHERE isHidden = 0 AND (title IS NULL OR title = '' OR description IS NULL OR description = '')")
    suspend fun getBookmarksMissingMetadataOnce(): List<BookmarkEntity>

    @Query("SELECT * FROM bookmarks WHERE isHidden = 0 AND (imageUrl IS NULL OR imageUrl = '')")
    suspend fun getBookmarksWithoutImageOnce(): List<BookmarkEntity>

    @Transaction
    suspend fun insertOrUnhide(bookmark: BookmarkEntity): Boolean {
        if (existsByUrl(bookmark.url)) return false
        val hidden = findHiddenByUrl(bookmark.url)
        if (hidden != null) {
            unhideBookmark(hidden.id, bookmark.title, bookmark.description, bookmark.imageUrl, bookmark.createdAt)
        } else {
            insert(bookmark)
        }
        return true
    }
}
