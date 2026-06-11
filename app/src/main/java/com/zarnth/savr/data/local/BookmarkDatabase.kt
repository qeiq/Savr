package com.zarnth.savr.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.zarnth.savr.data.local.dao.BookmarkDao
import com.zarnth.savr.data.local.entity.BookmarkEntity

@Database(
    entities = [BookmarkEntity::class],
    version = 1
)
abstract class BookmarkDatabase : RoomDatabase() {

    abstract fun bookmarkDao(): BookmarkDao
}