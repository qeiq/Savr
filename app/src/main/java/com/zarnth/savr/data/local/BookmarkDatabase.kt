package com.zarnth.savr.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.zarnth.savr.data.local.dao.BookmarkDao
import com.zarnth.savr.data.local.dao.CollectionDao
import com.zarnth.savr.data.local.entity.BookmarkCollectionCrossRef
import com.zarnth.savr.data.local.entity.BookmarkEntity
import com.zarnth.savr.data.local.entity.CollectionEntity

@Database(
    entities = [BookmarkEntity::class, CollectionEntity::class, BookmarkCollectionCrossRef::class],
    version = 3
)
abstract class BookmarkDatabase : RoomDatabase() {

    abstract fun bookmarkDao(): BookmarkDao
    abstract fun collectionDao(): CollectionDao
}