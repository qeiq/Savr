package com.zarnth.savr.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.zarnth.savr.data.local.dao.BookmarkDao
import com.zarnth.savr.data.local.dao.CollectionDao
import com.zarnth.savr.data.local.dao.CrashLogDao
import com.zarnth.savr.data.local.entity.BookmarkCollectionCrossRef
import com.zarnth.savr.data.local.entity.BookmarkEntity
import com.zarnth.savr.data.local.entity.CollectionEntity
import com.zarnth.savr.data.local.entity.CrashLogEntity

@Database(
    entities = [BookmarkEntity::class, CollectionEntity::class, BookmarkCollectionCrossRef::class, CrashLogEntity::class],
    version = 9
)
abstract class BookmarkDatabase : RoomDatabase() {

    abstract fun bookmarkDao(): BookmarkDao
    abstract fun collectionDao(): CollectionDao
    abstract fun crashLogDao(): CrashLogDao

    companion object {
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_bookmark_collection_cross_ref_bookmarkId` ON `bookmark_collection_cross_ref` (`bookmarkId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_bookmark_collection_cross_ref_collectionId` ON `bookmark_collection_cross_ref` (`collectionId`)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `crash_logs` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`timestamp` INTEGER NOT NULL, " +
                        "`versionName` TEXT NOT NULL, " +
                        "`versionCode` INTEGER NOT NULL, " +
                        "`androidVersion` TEXT NOT NULL, " +
                        "`sdkInt` INTEGER NOT NULL, " +
                        "`manufacturer` TEXT NOT NULL, " +
                        "`model` TEXT NOT NULL, " +
                        "`brand` TEXT NOT NULL, " +
                        "`exceptionClass` TEXT NOT NULL, " +
                        "`message` TEXT NOT NULL, " +
                        "`stackTrace` TEXT NOT NULL, " +
                        "`threadName` TEXT NOT NULL)"
                )
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `bookmarks` ADD COLUMN `isCollectionOnly` INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `bookmarks` ADD COLUMN `isPinned` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `bookmark_collection_cross_ref` ADD COLUMN `isPinned` INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `bookmarks` ADD COLUMN `pinnedAt` INTEGER")
                db.execSQL("ALTER TABLE `bookmark_collection_cross_ref` ADD COLUMN `pinnedAt` INTEGER")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `collections` ADD COLUMN `parentCollectionId` INTEGER REFERENCES `collections`(`id`) ON DELETE CASCADE")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_collections_parentCollectionId` ON `collections` (`parentCollectionId`)")
            }
        }

        val MIGRATION_9_8 = object : Migration(9, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `bookmark_tag_cross_ref`")
                db.execSQL("DROP TABLE IF EXISTS `tags`")
            }
        }
    }
}