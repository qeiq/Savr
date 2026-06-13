package com.zarnth.savr.data.local.repository

import com.zarnth.savr.data.local.dao.BookmarkDao
import com.zarnth.savr.data.local.mapper.toDomain
import com.zarnth.savr.data.local.mapper.toEntity
import com.zarnth.savr.domain.model.Bookmark
import com.zarnth.savr.domain.repository.BookmarkRepository
import com.zarnth.savr.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class BookmarkRepositoryImpl(
    private val dao: BookmarkDao
) : BookmarkRepository {

    override suspend fun insert(bookmark: Bookmark) {
        dao.insert(bookmark.toEntity())
    }

    override suspend fun deleteBookmarks(bookmarks: List<Bookmark>) {
        dao.delete(bookmarks.map { it.toEntity() })
    }

    override suspend fun searchBookmarks(text: String): Flow<Resource<List<Bookmark>>> {
        return dao.searchBookmarks(text)
            .map { list ->
                Resource.Success(list.map { it.toDomain() }) as Resource<List<Bookmark>>
            }

            .onStart {
                emit(Resource.Loading())
            }

            .catch { e ->
                emit(
                    Resource.Error(
                        e.message ?: "Unknown error"
                    )
                )
            }
    }

    override fun getBookmarks(): Flow<Resource<List<Bookmark>>> {

        return dao.getBookmarks()

            .map { list ->
                Resource.Success(
                    list.map { it.toDomain() }
                ) as Resource<List<Bookmark>>
            }

            .onStart {
                emit(Resource.Loading())
            }

            .catch { e ->
                emit(
                    Resource.Error(
                        e.message ?: "Unknown error"
                    )
                )
            }
    }
}