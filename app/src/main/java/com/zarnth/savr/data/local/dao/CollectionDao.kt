package com.zarnth.savr.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zarnth.savr.data.local.entity.BookmarkCollectionCrossRef
import com.zarnth.savr.data.local.entity.BookmarkEntity
import com.zarnth.savr.data.local.entity.CollectionEntity
import kotlinx.coroutines.flow.Flow

data class CollectionWithCount(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val bookmarkCount: Int,
    val parentCollectionId: Long?
)

data class BookmarkWithCollectionPin(
    @Embedded val bookmark: BookmarkEntity,
    val pinnedInCollection: Boolean,
    val pinnedAtInCollection: Long?
)

@Dao
interface CollectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: CollectionEntity): Long

    @Query("UPDATE collections SET name = :name WHERE id = :id")
    suspend fun renameCollection(id: Long, name: String)

    @Delete
    suspend fun deleteCollection(collection: CollectionEntity)

    @Query("""
        SELECT c.id, c.name, c.createdAt, COUNT(bcc.collectionId) AS bookmarkCount, c.parentCollectionId
        FROM collections c
        LEFT JOIN bookmark_collection_cross_ref bcc ON c.id = bcc.collectionId
        WHERE c.parentCollectionId IS NULL
        GROUP BY c.id
        ORDER BY c.createdAt DESC
    """)
    fun getAllCollections(): Flow<List<CollectionWithCount>>

    @Query("""
        SELECT c.id, c.name, c.createdAt, COUNT(bcc.collectionId) AS bookmarkCount, c.parentCollectionId
        FROM collections c
        LEFT JOIN bookmark_collection_cross_ref bcc ON c.id = bcc.collectionId
        WHERE c.parentCollectionId = :parentId
        GROUP BY c.id
        ORDER BY c.createdAt DESC
    """)
    fun getSubCollections(parentId: Long): Flow<List<CollectionWithCount>>

    @Query("""
        SELECT b.imageUrl FROM bookmarks b
        INNER JOIN bookmark_collection_cross_ref bcc ON b.id = bcc.bookmarkId
        WHERE bcc.collectionId = :collectionId
          AND b.imageUrl IS NOT NULL AND b.imageUrl != ''
        ORDER BY b.createdAt DESC
        LIMIT 4
    """)
    suspend fun getPreviewUrlsForCollection(collectionId: Long): List<String>

    @Query("""
        SELECT b.*, bcc.isPinned AS pinnedInCollection, bcc.pinnedAt AS pinnedAtInCollection FROM bookmarks b
        INNER JOIN bookmark_collection_cross_ref bcc ON b.id = bcc.bookmarkId
        WHERE bcc.collectionId = :collectionId
        ORDER BY b.createdAt DESC
    """)
    fun getBookmarksInCollection(collectionId: Long): Flow<List<BookmarkWithCollectionPin>>

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

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM bookmark_collection_cross_ref bcc
            INNER JOIN bookmarks b ON b.id = bcc.bookmarkId
            WHERE bcc.collectionId = :collectionId AND b.url = :url
        )
    """)
    suspend fun isUrlInCollection(url: String, collectionId: Long): Boolean

    @Query("UPDATE bookmark_collection_cross_ref SET isPinned = :isPinned, pinnedAt = :pinnedAt WHERE bookmarkId = :bookmarkId AND collectionId = :collectionId")
    suspend fun setPinnedInCollection(bookmarkId: Long, collectionId: Long, isPinned: Boolean, pinnedAt: Long?)

    @Query("""
        SELECT b.url FROM bookmark_collection_cross_ref bcc
        INNER JOIN bookmarks b ON b.id = bcc.bookmarkId
        WHERE bcc.collectionId = :collectionId AND bcc.isPinned = 1
        ORDER BY bcc.pinnedAt ASC
    """)
    suspend fun getPinnedBookmarkUrlsForCollection(collectionId: Long): List<String>

    @Query("SELECT * FROM collections WHERE id = :id")
    suspend fun getCollectionById(id: Long): CollectionEntity?

    @Query("UPDATE collections SET parentCollectionId = :parentId WHERE id = :id")
    suspend fun setParentCollection(id: Long, parentId: Long?)

    @Query("DELETE FROM collections WHERE parentCollectionId = :parentId")
    suspend fun deleteSubCollections(parentId: Long)
}
