package com.zarnth.savr.utils

sealed class Resource<T>(
    val data: T? = null,
    val errorMessage: String? = null
) {
    class Loading<T>() : Resource<T>()
    class Success<T>(private val success: T?) : Resource<T>(data = success)
    class Error<T>(private val error: String?) : Resource<T>(errorMessage = error)
}