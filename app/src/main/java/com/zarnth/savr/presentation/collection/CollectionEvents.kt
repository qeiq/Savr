package com.zarnth.savr.presentation.collection

import com.zarnth.savr.domain.model.Bookmark
import com.zarnth.savr.domain.model.Collection
import com.zarnth.savr.domain.model.SortOrder

sealed class CollectionEvents {
    data class InputNameChanged(val name: String) : CollectionEvents()
    object ShowCreateDialog : CollectionEvents()
    object HideCreateDialog : CollectionEvents()
    object CreateCollection : CollectionEvents()
    data class ShowRenameDialog(val collection: Collection) : CollectionEvents()
    object HideRenameDialog : CollectionEvents()
    object RenameCollection : CollectionEvents()
    data class SelectCollection(val collection: Collection) : CollectionEvents()
    data class RestoreCollectionDetail(val collectionId: Long) : CollectionEvents()
    data class ToggleSelection(val id: Long) : CollectionEvents()
    object SelectAll : CollectionEvents()
    object DeselectAll : CollectionEvents()
    object ClearSelection : CollectionEvents()
    object DeleteSelected : CollectionEvents()
    data class DeleteCollectionById(val collectionId: Long) : CollectionEvents()
    data class ShowDetailBodySheet(val bookmark: Bookmark) : CollectionEvents()
    object DismissDetailBodySheet : CollectionEvents()
    data class ShowEditBookmarkSheet(val bookmark: Bookmark) : CollectionEvents()
    object HideEditBookmarkSheet : CollectionEvents()
    data class EditTitleChanged(val text: String) : CollectionEvents()
    data class EditDescriptionChanged(val text: String) : CollectionEvents()
    object SaveEditedBookmark : CollectionEvents()
    data class ToggleDetailSelection(val id: Long) : CollectionEvents()
    object SelectAllDetail : CollectionEvents()
    object DeselectAllDetail : CollectionEvents()
    object ClearDetailSelection : CollectionEvents()
    data class TogglePinInCollection(val id: Long) : CollectionEvents()
    data class RemoveSelectedFromCollection(val collectionId: Long) : CollectionEvents()
    data class SetSortOrder(val sortOrder: SortOrder) : CollectionEvents()
    object ShowSortSheet : CollectionEvents()
    object HideSortSheet : CollectionEvents()
    object ShowAddBookmarkSheet : CollectionEvents()
    object HideAddBookmarkSheet : CollectionEvents()
    data class AddBookmarkUrlChanged(val url: String) : CollectionEvents()
    object AddBookmarkToCollection : CollectionEvents()
    object CollectionDuplicateToastShown : CollectionEvents()
    data class AddClipboardToCollection(
        val url: String,
        val title: String?,
        val description: String?,
        val imageUrl: String?
    ) : CollectionEvents()
    data class ShowCreateSubCollectionDialog(val parentCollection: Collection) : CollectionEvents()
    object CreateSubCollection : CollectionEvents()
    data class ShowDeleteCollectionDialog(val collection: Collection) : CollectionEvents()
    object HideDeleteCollectionDialog : CollectionEvents()
    object ConfirmDeleteCollection : CollectionEvents()
    object ResetToCollectionsList : CollectionEvents()
}
