package com.zarnth.savr.di

import androidx.room.Room
import com.zarnth.savr.data.backup.BackupManager
import com.zarnth.savr.data.crash.CrashHandler
import com.zarnth.savr.data.local.BookmarkDatabase
import com.zarnth.savr.data.local.repository.BookmarkRepositoryImpl
import com.zarnth.savr.data.local.repository.CrashLogRepositoryImpl
import com.zarnth.savr.data.local.repository.SettingsRepositoryImpl
import com.zarnth.savr.domain.repository.BookmarkRepository
import com.zarnth.savr.domain.repository.CrashLogRepository
import com.zarnth.savr.domain.repository.SettingsRepository
import com.zarnth.savr.presentation.collection.CollectionViewModel
import com.zarnth.savr.presentation.crashlog.CrashLogViewModel
import com.zarnth.savr.presentation.home.HomeViewModel
import com.zarnth.savr.presentation.search.SearchViewModel
import com.zarnth.savr.presentation.setting.SettingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val savrModule = module {

    // Database
    single {
        Room.databaseBuilder(
                get(),
                BookmarkDatabase::class.java,
                "bookmark_db"
            )
            .addMigrations(
                BookmarkDatabase.MIGRATION_3_4,
                BookmarkDatabase.MIGRATION_4_5,
                BookmarkDatabase.MIGRATION_5_6,
                BookmarkDatabase.MIGRATION_6_7,
                BookmarkDatabase.MIGRATION_7_8,
                BookmarkDatabase.MIGRATION_8_9,
                BookmarkDatabase.MIGRATION_9_8
            )
            .build()
    }

    single {
        get<BookmarkDatabase>().bookmarkDao()
    }

    single {
        get<BookmarkDatabase>().collectionDao()
    }

    // Repository
    single<SettingsRepository> {
        SettingsRepositoryImpl(get())
    }

    single<BookmarkRepository> {
        BookmarkRepositoryImpl(get(), get())
    }

    single<CrashLogRepository> {
        CrashLogRepositoryImpl(get())
    }

    single {
        get<BookmarkDatabase>().crashLogDao()
    }

    single {
        CrashHandler(get(), get())
    }

    single {
        BackupManager(get(), get(), get(), get())
    }

    viewModel {
        HomeViewModel(get())
    }

    viewModel {
        SearchViewModel(get())
    }

    viewModel {
        CollectionViewModel(get())
    }

    viewModel {
        SettingViewModel(get(), get())
    }

    viewModel {
        CrashLogViewModel(get())
    }
}
