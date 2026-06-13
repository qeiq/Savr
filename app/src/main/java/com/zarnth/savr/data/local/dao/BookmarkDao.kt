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

    @Delete
    suspend fun delete(entities: List<BookmarkEntity>)

    @Query("SELECT * FROM bookmarks")
    fun getBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE title LIKE '%' || :searchQuery || '%'")
    fun searchBookmarks(searchQuery: String): Flow<List<BookmarkEntity>>
}