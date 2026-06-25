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

    @Query("SELECT * FROM bookmarks WHERE isHidden = 0")
    fun getBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE isHidden = 0 AND (title LIKE '%' || :searchQuery || '%' OR url LIKE '%' || :searchQuery || '%')")
    fun searchBookmarks(searchQuery: String): Flow<List<BookmarkEntity>>

    @Query("UPDATE bookmarks SET isHidden = 1 WHERE id IN (:ids)")
    suspend fun hideBookmarks(ids: List<Long>)

    @Query("SELECT * FROM bookmarks WHERE url = :url AND isHidden = 1 LIMIT 1")
    suspend fun findHiddenByUrl(url: String): BookmarkEntity?

    @Query("UPDATE bookmarks SET isHidden = 0, title = :title, description = :description, imageUrl = :imageUrl, createdAt = :createdAt WHERE id = :id")
    suspend fun unhideBookmark(id: Long, title: String?, description: String?, imageUrl: String?, createdAt: Long)

    @Query("UPDATE bookmarks SET imageUrl = :imageUrl WHERE id = :id")
    suspend fun updateImageUrl(id: Long, imageUrl: String?)

    @Query("SELECT * FROM bookmarks WHERE isHidden = 0")
    suspend fun getBookmarksOnce(): List<BookmarkEntity>

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
