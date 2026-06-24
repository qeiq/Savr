package com.zarnth.savr.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.zarnth.savr.data.local.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Insert
    suspend fun insert(bookmarkEntity: BookmarkEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE url = :url AND isHidden = 0)")
    suspend fun existsByUrl(url: String): Boolean

    @Delete
    suspend fun delete(entities: List<BookmarkEntity>)

    @Query("SELECT * FROM bookmarks WHERE isHidden = 0")
    fun getBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE isHidden = 0 AND (title LIKE '%' || :searchQuery || '%' OR url LIKE '%' || :searchQuery || '%')")
    fun searchBookmarks(searchQuery: String): Flow<List<BookmarkEntity>>

    @Query("UPDATE bookmarks SET isHidden = 1 WHERE id IN (:ids)")
    suspend fun hideBookmarks(ids: List<Long>)

    @Query("SELECT * FROM bookmarks WHERE url = :url AND isHidden = 1 LIMIT 1")
    suspend fun findHiddenByUrl(url: String): BookmarkEntity?

    @Query("UPDATE bookmarks SET isHidden = 0, title = :title, description = :description, imageUrl = :imageUrl WHERE id = :id")
    suspend fun unhideBookmark(id: Long, title: String?, description: String?, imageUrl: String?)

    @Query("SELECT * FROM bookmarks")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>
}
