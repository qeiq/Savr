package com.zarnth.savr.presentation.home

import com.zarnth.savr.domain.model.Bookmark

data class HomeState(
    val isLoading: Boolean = false,
    val error: String = "",
    val bookmarkData: List<Bookmark> = emptyList(),
    val inputUrl: String = "",
    val isDialog: Boolean = false,
    val isPhotoPreviewDialog: Boolean = false,
    val dialogPhotoUrl: String = "",
    val tempBookmark: Bookmark? = null,
    val isBodySheet: Boolean = false
)
