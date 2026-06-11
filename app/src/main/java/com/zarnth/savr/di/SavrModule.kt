package com.zarnth.savr.di

import androidx.room.Room
import com.zarnth.savr.data.local.BookmarkDatabase
import com.zarnth.savr.data.local.repository.BookmarkRepositoryImpl
import com.zarnth.savr.domain.repository.BookmarkRepository
import com.zarnth.savr.presentation.home.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val savrModule = module {

    // Database
    single {
        Room.databaseBuilder(
            get(),
            BookmarkDatabase::class.java,
            "bookmark_db"
        ).build()
    }

    single {
        get<BookmarkDatabase>().bookmarkDao()
    }

    // Repository
    single<BookmarkRepository> {
        BookmarkRepositoryImpl(get())
    }

    viewModel {
        HomeViewModel(get())
    }

}