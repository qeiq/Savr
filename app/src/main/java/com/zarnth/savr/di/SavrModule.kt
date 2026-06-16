package com.zarnth.savr.di

import androidx.room.Room
import com.zarnth.savr.data.local.BookmarkDatabase
import com.zarnth.savr.data.local.repository.BookmarkRepositoryImpl
import com.zarnth.savr.domain.repository.BookmarkRepository
import com.zarnth.savr.presentation.collection.CollectionViewModel
import com.zarnth.savr.presentation.home.HomeViewModel
import com.zarnth.savr.presentation.search.SearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val savrModule = module {

    // Database
    single {
        Room.databaseBuilder(
                get(),
                BookmarkDatabase::class.java,
                "bookmark_db"
            ).fallbackToDestructiveMigration(false)
            .build()
    }

    single {
        get<BookmarkDatabase>().bookmarkDao()
    }

    single {
        get<BookmarkDatabase>().collectionDao()
    }

    // Repository
    single<BookmarkRepository> {
        BookmarkRepositoryImpl(get(), get())
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

}
