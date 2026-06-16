package com.zarnth.savr.presentation.home

import com.zarnth.savr.domain.model.Bookmark

sealed class HomeEvents {
    data class OnTextFieldValueChange(val text: String) : HomeEvents()
    object SaveBookmark : HomeEvents()
    object OnDialogDismissClick : HomeEvents()
    object FabClick : HomeEvents()
    data class PreviewImageClick(val url: String) : HomeEvents()
    object PreviewImageDismissClick : HomeEvents()

    data class BookmarkPreviewClick(val bookmark: Bookmark) : HomeEvents()
    object BookmarkPreviewDismissClick : HomeEvents()

    data class ToggleSelection(val id: Long) : HomeEvents()
    object DeleteSelected : HomeEvents()
    object ClearSelection : HomeEvents()
    object ShowCollectionPicker : HomeEvents()
    object HideCollectionPicker : HomeEvents()
    data class AddToCollection(val collectionId: Long) : HomeEvents()
}