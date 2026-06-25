package com.zarnth.savr.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.zarnth.savr.data.local.entity.BookmarkCollectionCrossRef
import com.zarnth.savr.data.local.entity.CollectionEntity
import com.zarnth.savr.data.local.entity.CollectionWithBookmarks
import kotlinx.coroutines.flow.Flow

data class CollectionWithCount(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val bookmarkCount: Int,
    val previewUrls: String?
)

@Dao
interface CollectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: CollectionEntity): Long

    @Delete
    suspend fun deleteCollection(collection: CollectionEntity)

    @Query("""
        SELECT c.id, c.name, c.createdAt, COUNT(bcc.collectionId) AS bookmarkCount,
               (SELECT GROUP_CONCAT(b.imageUrl, '|||')
                FROM bookmark_collection_cross_ref bcc2
                INNER JOIN bookmarks b ON bcc2.bookmarkId = b.id
                WHERE bcc2.collectionId = c.id AND b.imageUrl IS NOT NULL AND b.imageUrl != ''
                LIMIT 4) AS previewUrls
        FROM collections c
        LEFT JOIN bookmark_collection_cross_ref bcc ON c.id = bcc.collectionId
        GROUP BY c.id
        ORDER BY c.createdAt DESC
    """)
    fun getAllCollections(): Flow<List<CollectionWithCount>>

    @Transaction
    @Query("SELECT * FROM collections WHERE id = :collectionId")
    fun getCollectionWithBookmarks(collectionId: Long): Flow<CollectionWithBookmarks?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addBookmarkToCollection(crossRef: BookmarkCollectionCrossRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addBookmarksToCollection(crossRefs: List<BookmarkCollectionCrossRef>)

    @Delete
    suspend fun removeBookmarkFromCollection(crossRef: BookmarkCollectionCrossRef)

    @Query("SELECT * FROM collections ORDER BY createdAt DESC")
    fun getAllCollectionsRaw(): Flow<List<CollectionEntity>>

    @Query("""
        SELECT b.url FROM bookmarks b
        INNER JOIN bookmark_collection_cross_ref bcc ON b.id = bcc.bookmarkId
        WHERE bcc.collectionId = :collectionId
    """)
    suspend fun getBookmarkUrlsForCollection(collectionId: Long): List<String>
}
