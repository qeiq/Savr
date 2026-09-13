package com.zarnth.savr.presentation.home

import com.zarnth.savr.domain.model.Bookmark
import com.zarnth.savr.domain.model.SortOrder

sealed class HomeEvents {
    data class OnTextFieldValueChange(val text: String) : HomeEvents()
    object SaveBookmark : HomeEvents()
    object OnDialogDismissClick : HomeEvents()
    object FabClick : HomeEvents()
    data class PreviewImageClick(val url: String) : HomeEvents()
    object PreviewImageDismissClick : HomeEvents()

    data class BookmarkPreviewClick(val bookmark: Bookmark) : HomeEvents()
    object BookmarkPreviewDismissClick : HomeEvents()

    object ShowEditBookmarkSheet : HomeEvents()
    object HideEditBookmarkSheet : HomeEvents()
    data class EditTitleChanged(val text: String) : HomeEvents()
    data class EditDescriptionChanged(val text: String) : HomeEvents()
    object SaveEditedBookmark : HomeEvents()

    data class ToggleSelection(val id: Long) : HomeEvents()
    object SelectAll : HomeEvents()
    object DeselectAll : HomeEvents()
    object DeleteSelected : HomeEvents()
    object ConfirmDeleteSelected : HomeEvents()
    object ShowDeleteConfirmDialog : HomeEvents()
    object HideDeleteConfirmDialog : HomeEvents()
    object ClearSelection : HomeEvents()
    data class TogglePin(val id: Long) : HomeEvents()
    object ShowCollectionPicker : HomeEvents()
    object HideCollectionPicker : HomeEvents()
    data class AddToCollection(val collectionId: Long) : HomeEvents()
    data class SetSortOrder(val sortOrder: SortOrder) : HomeEvents()
    object ShowSortSheet : HomeEvents()
    object HideSortSheet : HomeEvents()
    data class ClipboardDetected(val text: String?) : HomeEvents()
    object DismissClipboardSheet : HomeEvents()
    object AddClipboardBookmark : HomeEvents()
    object DuplicateToastShown : HomeEvents()
}