package com.zarnth.savr.presentation.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zarnth.savr.domain.model.Bookmark
import com.zarnth.savr.domain.model.Collection
import com.zarnth.savr.domain.repository.BookmarkRepository
import com.zarnth.savr.domain.model.SortOrder
import com.zarnth.savr.link_fetcher.LinkMetadataParser
import com.zarnth.savr.utils.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CollectionViewModel(
    private val repository: BookmarkRepository
) : ViewModel() {
    private val _state = MutableStateFlow(CollectionState())
    val state = _state.asStateFlow()
    private var collectionJob: Job? = null
    private var subCollectionsJob: Job? = null
    private val detailStack = mutableListOf<Collection>()
    private var rawCollectionBookmarks: List<Bookmark> = emptyList()
    private val parser = LinkMetadataParser()

    private fun updateActiveDetailData(
        collectionId: Long,
        collection: Collection?,
        items: List<CollectionDetailItem>?,
        isLoading: Boolean
    ) {
        _state.update { current ->
            val existing = current.detailDataById[collectionId] ?: CollectionDetailData()
            val updated = CollectionDetailData(
                collection = collection ?: existing.collection,
                detailItems = items ?: existing.detailItems,
                isLoading = isLoading
            )
            current.copy(detailDataById = current.detailDataById + (collectionId to updated))
        }
    }

    init {
        loadCollections()
    }

    fun onEvent(event: CollectionEvents) {
        when (event) {
            is CollectionEvents.InputNameChanged -> {
                _state.update { it.copy(inputName = event.name) }
            }

            CollectionEvents.ShowCreateDialog -> {
                _state.update { it.copy(showCreateDialog = true, inputName = "") }
            }

            CollectionEvents.HideCreateDialog -> {
                _state.update {
                    it.copy(
                        showCreateDialog = false,
                        inputName = "",
                        showCreateSubCollectionDialog = false,
                        creatingSubCollectionFor = null
                    )
                }
            }

            CollectionEvents.CreateCollection -> {
                createCollection()
            }

            is CollectionEvents.ShowRenameDialog -> {
                _state.update {
                    it.copy(
                        renamingCollection = event.collection,
                        isRenameDialogVisible = true,
                        inputName = event.collection.name
                    )
                }
            }

            CollectionEvents.HideRenameDialog -> {
                _state.update {
                    it.copy(isRenameDialogVisible = false, renamingCollection = null, inputName = "")
                }
            }

            CollectionEvents.RenameCollection -> {
                renameCollection()
            }

            is CollectionEvents.SelectCollection -> {
                openCollection(event.collection, push = true)
            }

            is CollectionEvents.RestoreCollectionDetail -> {
                restoreCollectionDetail(event.collectionId)
            }

            is CollectionEvents.ToggleSelection -> {
                val current = _state.value
                val newSelected = if (event.id in current.selectedIds) {
                    current.selectedIds - event.id
                } else {
                    current.selectedIds + event.id
                }
                _state.update {
                    it.copy(
                        selectedIds = newSelected,
                        isSelectionMode = newSelected.isNotEmpty()
                    )
                }
            }

            CollectionEvents.ClearSelection -> {
                _state.update { it.copy(selectedIds = emptySet(), isSelectionMode = false) }
            }

            CollectionEvents.SelectAll -> {
                val allIds = _state.value.collections.map { it.id }.toSet()
                _state.update {
                    it.copy(
                        selectedIds = allIds,
                        isSelectionMode = allIds.isNotEmpty()
                    )
                }
            }

            CollectionEvents.DeselectAll -> {
                _state.update { it.copy(selectedIds = emptySet(), isSelectionMode = false) }
            }

            CollectionEvents.DeleteSelected -> {
                deleteSelected()
            }

            is CollectionEvents.DeleteCollectionById -> {
                deleteCollectionById(event.collectionId)
            }

            is CollectionEvents.ShowDetailBodySheet -> {
                _state.update { it.copy(tempBookmark = event.bookmark, isDetailBodySheet = true) }
            }

            CollectionEvents.DismissDetailBodySheet -> {
                _state.update { it.copy(tempBookmark = null, isDetailBodySheet = false) }
            }

            is CollectionEvents.ShowEditBookmarkSheet -> {
                _state.update {
                    it.copy(
                        isDetailBodySheet = false,
                        editingBookmark = event.bookmark,
                        editTitle = event.bookmark.title ?: "",
                        editDescription = event.bookmark.description ?: "",
                        isEditBookmarkSheet = true
                    )
                }
            }

            CollectionEvents.HideEditBookmarkSheet -> {
                _state.update {
                    it.copy(
                        isEditBookmarkSheet = false,
                        editingBookmark = null,
                        editTitle = "",
                        editDescription = ""
                    )
                }
            }

            is CollectionEvents.EditTitleChanged -> {
                _state.update { it.copy(editTitle = event.text) }
            }

            is CollectionEvents.EditDescriptionChanged -> {
                _state.update { it.copy(editDescription = event.text) }
            }

            CollectionEvents.SaveEditedBookmark -> {
                saveEditedBookmark()
            }

            is CollectionEvents.TogglePinInCollection -> {
                togglePinInCollection(event.id)
            }

            is CollectionEvents.ToggleDetailSelection -> {
                val current = _state.value
                val newSelected = if (event.id in current.detailSelectedIds) {
                    current.detailSelectedIds - event.id
                } else {
                    current.detailSelectedIds + event.id
                }
                _state.update {
                    it.copy(
                        detailSelectedIds = newSelected,
                        isDetailSelectionMode = newSelected.isNotEmpty()
                    )
                }
            }

            CollectionEvents.ClearDetailSelection -> {
                _state.update { it.copy(detailSelectedIds = emptySet(), isDetailSelectionMode = false) }
            }

            CollectionEvents.SelectAllDetail -> {
                val allIds = _state.value.collectionBookmarks.map { it.id }.toSet()
                _state.update {
                    it.copy(
                        detailSelectedIds = allIds,
                        isDetailSelectionMode = allIds.isNotEmpty()
                    )
                }
            }

            CollectionEvents.DeselectAllDetail -> {
                _state.update { it.copy(detailSelectedIds = emptySet(), isDetailSelectionMode = false) }
            }

            is CollectionEvents.RemoveSelectedFromCollection -> {
                removeSelectedFromCollection(event.collectionId)
            }

            is CollectionEvents.SetSortOrder -> {
                val sorted = sortBookmarks(rawCollectionBookmarks, event.sortOrder)
                _state.update { current ->
                    val activeId = current.selectedCollection?.id
                    val existingData = activeId?.let { current.detailDataById[it] }
                    val rebuilt = existingData?.copy(
                        detailItems = buildDetailItems(sorted, current.subCollections, event.sortOrder)
                    )
                    current.copy(
                        sortOrder = event.sortOrder,
                        showSortSheet = false,
                        collectionBookmarks = sorted,
                        detailDataById = if (activeId != null && rebuilt != null) {
                            current.detailDataById + (activeId to rebuilt)
                        } else {
                            current.detailDataById
                        }
                    )
                }
            }

            CollectionEvents.ShowSortSheet -> {
                _state.update { it.copy(showSortSheet = true) }
            }

            CollectionEvents.HideSortSheet -> {
                _state.update { it.copy(showSortSheet = false) }
            }

            CollectionEvents.ShowAddBookmarkSheet -> {
                _state.update { it.copy(showAddBookmarkSheet = true, inputUrl = "") }
            }

            CollectionEvents.HideAddBookmarkSheet -> {
                _state.update { it.copy(showAddBookmarkSheet = false, inputUrl = "") }
            }

            is CollectionEvents.AddBookmarkUrlChanged -> {
                _state.update { it.copy(inputUrl = event.url) }
            }

            CollectionEvents.AddBookmarkToCollection -> {
                addBookmarkToCollection()
            }

            CollectionEvents.CollectionDuplicateToastShown -> {
                _state.update { it.copy(duplicateToastKey = 0) }
            }

            is CollectionEvents.AddClipboardToCollection -> {
                addClipboardToCollection(event.url, event.title, event.description, event.imageUrl)
            }

            is CollectionEvents.ShowCreateSubCollectionDialog -> {
                _state.update {
                    it.copy(
                        showCreateSubCollectionDialog = true,
                        creatingSubCollectionFor = event.parentCollection,
                        inputName = ""
                    )
                }
            }

            CollectionEvents.CreateSubCollection -> {
                createSubCollection()
            }

            is CollectionEvents.ShowDeleteCollectionDialog -> {
                _state.update { it.copy(deleteTarget = event.collection) }
            }

            CollectionEvents.HideDeleteCollectionDialog -> {
                _state.update { it.copy(deleteTarget = null) }
            }

            CollectionEvents.ConfirmDeleteCollection -> {
                confirmDeleteCollection()
            }

            CollectionEvents.ResetToCollectionsList -> {
                backToCollections()
            }
        }
    }

    fun backToCollections() {
        collectionJob?.cancel()
        subCollectionsJob?.cancel()
        detailStack.clear()
        _state.update {
            it.copy(
                selectedCollection = null,
                collectionBookmarks = emptyList(),
                detailItems = emptyList(),
                subCollections = emptyList(),
                parentCollection = null,
                showCreateSubCollectionDialog = false,
                creatingSubCollectionFor = null,
                deleteTarget = null,
                detailSelectedIds = emptySet(),
                isDetailSelectionMode = false
            )
        }
    }

    private fun invalidateDetailCache(ids: Iterable<Long>) {
        val idSet = ids.toSet()
        if (idSet.isEmpty()) return
        _state.update { it.copy(detailDataById = it.detailDataById - idSet) }
    }

    private fun restoreCollectionDetail(collectionId: Long) {
        val selected = _state.value.selectedCollection
        if (selected?.id == collectionId) {
            if (detailStack.isEmpty()) detailStack.add(selected)
            return
        }
        val stackIndex = detailStack.indexOfFirst { it.id == collectionId }
        if (stackIndex >= 0) {
            while (detailStack.size > stackIndex + 1) detailStack.removeAt(detailStack.lastIndex)
            openCollection(detailStack.last(), push = false)
            return
        }
        val found = _state.value.collections.find { it.id == collectionId }
            ?: _state.value.subCollections.find { it.id == collectionId }
        if (found != null) {
            detailStack.clear()
            detailStack.add(found)
            openCollection(found, push = false)
        } else {
            viewModelScope.launch {
                val fetched = repository.getCollectionById(collectionId) ?: return@launch
                if (_state.value.selectedCollection?.id != collectionId) {
                    detailStack.clear()
                    detailStack.add(fetched)
                    openCollection(fetched, push = false)
                }
            }
        }
    }

    private fun openCollection(collection: Collection, push: Boolean) {
        collectionJob?.cancel()
        subCollectionsJob?.cancel()
        if (push) {
            val existing = detailStack.indexOfFirst { it.id == collection.id }
            if (existing >= 0) {
                while (detailStack.size > existing + 1) detailStack.removeAt(detailStack.lastIndex)
            } else {
                detailStack.add(collection)
            }
        }
        _state.update {
            val hasData = it.detailDataById[collection.id]?.let { d -> !d.isLoading } ?: false
            it.copy(
                selectedCollection = collection,
                parentCollection = null,
                isDetailLoading = !hasData,
                showCreateSubCollectionDialog = false,
                creatingSubCollectionFor = null,
                deleteTarget = null,
                detailDataById = if (it.detailDataById.containsKey(collection.id)) {
                    it.detailDataById
                } else {
                    it.detailDataById + (collection.id to CollectionDetailData(collection = collection, isLoading = true))
                }
            )
        }
        rawCollectionBookmarks = emptyList()
        collectionJob = viewModelScope.launch {
            repository.getBookmarksInCollection(collection.id).collect { resource ->
                when (resource) {
                    is Resource.Loading -> Unit
                    is Resource.Error -> _state.update {
                        it.copy(
                            isDetailLoading = false,
                            error = resource.errorMessage ?: "Error",
                            detailDataById = it.detailDataById + (collection.id to
                                (it.detailDataById[collection.id] ?: CollectionDetailData()).copy(isLoading = false))
                        )
                    }
                    is Resource.Success -> {
                        val items = resource.data ?: emptyList()
                        rawCollectionBookmarks = items
                        val sortOrder = _state.value.sortOrder
                        val sorted = sortBookmarks(items, sortOrder)
                        _state.update { s ->
                            s.copy(
                                isDetailLoading = false,
                                collectionBookmarks = sorted
                            )
                        }
                        updateActiveDetailData(
                            collectionId = collection.id,
                            collection = collection,
                            items = buildDetailItems(sorted, _state.value.subCollections, sortOrder),
                            isLoading = false
                        )
                    }
                }
            }
        }
        loadSubCollections(collection.id)
    }

    private fun loadSubCollections(parentId: Long) {
        subCollectionsJob?.cancel()
        subCollectionsJob = viewModelScope.launch {
            repository.getSubCollections(parentId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.update { it.copy(isSubCollectionsLoading = true) }
                    is Resource.Error -> _state.update { it.copy(isSubCollectionsLoading = false, error = resource.errorMessage ?: "Error") }
                    is Resource.Success -> {
                        val subs = resource.data ?: emptyList()
                        val sortOrder = _state.value.sortOrder
                        _state.update { it.copy(isSubCollectionsLoading = false, subCollections = subs) }
                        updateActiveDetailData(
                            collectionId = parentId,
                            collection = null,
                            items = buildDetailItems(_state.value.collectionBookmarks, subs, sortOrder),
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    private fun createSubCollection() {
        val parent = _state.value.creatingSubCollectionFor ?: return
        val name = _state.value.inputName.trim()
        if (name.isEmpty()) return
        viewModelScope.launch {
            try {
                repository.createCollection(name, parent.id)
                _state.update {
                    it.copy(
                        showCreateSubCollectionDialog = false,
                        creatingSubCollectionFor = null,
                        inputName = ""
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Create failed") }
            }
        }
    }

    private fun saveEditedBookmark() {
        val bookmark = _state.value.editingBookmark ?: return
        val title = _state.value.editTitle.trim().ifBlank { null }
        val description = _state.value.editDescription.trim().ifBlank { null }
        viewModelScope.launch {
            try {
                repository.updateTitleAndDescription(bookmark.id, title, description)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Update failed") }
            }
        }
    }

    private fun togglePinInCollection(bookmarkId: Long) {
        val collectionId = _state.value.selectedCollection?.id ?: return
        val bookmark = _state.value.collectionBookmarks.find { it.id == bookmarkId } ?: return
        val newPinned = !bookmark.isPinned
        viewModelScope.launch {
            try {
                repository.setBookmarkPinnedInCollection(
                    bookmarkId,
                    collectionId,
                    newPinned,
                    if (newPinned) System.currentTimeMillis() else null
                )
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Pin failed") }
            }
        }
    }

    private fun createCollection() {
        val name = _state.value.inputName.trim()
        if (name.isEmpty()) return
        viewModelScope.launch {
            repository.createCollection(name)
            _state.update { it.copy(showCreateDialog = false, inputName = "") }
        }
    }

    private fun renameCollection() {
        val collection = _state.value.renamingCollection ?: return
        val name = _state.value.inputName.trim()
        if (name.isEmpty() || name == collection.name) {
            _state.update { it.copy(isRenameDialogVisible = false, renamingCollection = null, inputName = "") }
            return
        }
        viewModelScope.launch {
            repository.renameCollection(collection.id, name)
            _state.update { it.copy(isRenameDialogVisible = false, renamingCollection = null, inputName = "") }
        }
    }

    private fun confirmDeleteCollection() {
        val target = _state.value.deleteTarget ?: return
        viewModelScope.launch {
            try {
                repository.deleteCollection(target)
                detailStack.removeAll { it.id == target.id }
                invalidateDetailCache(listOf(target.id))
                _state.update { it.copy(deleteTarget = null) }
                if (target.id == _state.value.selectedCollection?.id) {
                    val parent = detailStack.lastOrNull()
                    if (parent == null) backToCollections() else openCollection(parent, push = false)
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Delete failed", deleteTarget = null) }
            }
        }
    }

    private fun deleteSelected() {
        val selected = _state.value.collections.filter { it.id in _state.value.selectedIds }
        if (selected.isEmpty()) return
        viewModelScope.launch {
            selected.forEach { repository.deleteCollection(it) }
            invalidateDetailCache(selected.map { it.id })
            _state.update { it.copy(selectedIds = emptySet(), isSelectionMode = false) }
        }
    }

    private fun deleteCollectionById(collectionId: Long) {
        viewModelScope.launch {
            val collection = _state.value.collections.find { it.id == collectionId }
                ?: _state.value.subCollections.find { it.id == collectionId }
                ?: repository.getCollectionById(collectionId)
                ?: return@launch
            repository.deleteCollection(collection)
            invalidateDetailCache(listOf(collectionId))
            backToCollections()
        }
    }

    private fun removeSelectedFromCollection(collectionId: Long) {
        val ids = _state.value.detailSelectedIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            ids.forEach { repository.removeBookmarkFromCollection(it, collectionId) }
            _state.update { it.copy(detailSelectedIds = emptySet(), isDetailSelectionMode = false) }
        }
    }

    private fun addBookmarkToCollection() {
        val collection = _state.value.selectedCollection ?: return
        val rawUrl = _state.value.inputUrl.trim()
        if (rawUrl.isEmpty()) return
        val url = if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) {
            "https://$rawUrl"
        } else {
            rawUrl
        }
        viewModelScope.launch {
            try {
                if (repository.isUrlInCollection(url, collection.id)) {
                    _state.update {
                        it.copy(
                            showAddBookmarkSheet = false,
                            inputUrl = "",
                            duplicateToastKey = it.duplicateToastKey + 1
                        )
                    }
                    return@launch
                }
                _state.update { it.copy(isAddBookmarkLoading = true) }
                val meta = parser.parse(url)
                val bookmarkUrl = meta?.url ?: url
                if (repository.isUrlInCollection(bookmarkUrl, collection.id)) {
                    _state.update {
                        it.copy(
                            isAddBookmarkLoading = false,
                            showAddBookmarkSheet = false,
                            inputUrl = "",
                            duplicateToastKey = it.duplicateToastKey + 1
                        )
                    }
                    return@launch
                }
                val bookmark = Bookmark(
                    url = bookmarkUrl,
                    title = meta?.title,
                    description = meta?.description,
                    imageUrl = meta?.imageUrl,
                    isCollectionOnly = true
                )
                repository.insert(bookmark)
                val bookmarkId = repository.getBookmarkIdByUrl(bookmarkUrl)
                if (bookmarkId != null) {
                    repository.addBookmarkToCollection(bookmarkId, collection.id)
                }
                _state.update {
                    it.copy(
                        isAddBookmarkLoading = false,
                        showAddBookmarkSheet = false,
                        inputUrl = ""
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = e.message ?: "Unknown error",
                        isAddBookmarkLoading = false
                    )
                }
            }
        }
    }

    private fun addClipboardToCollection(url: String, title: String?, description: String?, imageUrl: String?) {
        val collection = _state.value.selectedCollection ?: return
        if (url.isBlank()) return
        viewModelScope.launch {
            try {
                if (repository.isUrlInCollection(url, collection.id)) {
                    _state.update { it.copy(duplicateToastKey = it.duplicateToastKey + 1) }
                    return@launch
                }
                _state.update { it.copy(isAddBookmarkLoading = true) }
                repository.insert(
                    Bookmark(
                        url = url,
                        title = title,
                        description = description,
                        imageUrl = imageUrl,
                        isCollectionOnly = true
                    )
                )
                val bookmarkId = repository.getBookmarkIdByUrl(url)
                if (bookmarkId != null) {
                    repository.addBookmarkToCollection(bookmarkId, collection.id)
                }
                _state.update { it.copy(isAddBookmarkLoading = false) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = e.message ?: "Unknown error",
                        isAddBookmarkLoading = false
                    )
                }
            }
        }
    }

    private fun loadCollections() {
        viewModelScope.launch {
            repository.getAllCollections().collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.update { it.copy(isLoading = true) }
                    is Resource.Error -> _state.update { it.copy(isLoading = false, error = resource.errorMessage ?: "Error") }
                    is Resource.Success -> _state.update { it.copy(isLoading = false, collections = resource.data ?: emptyList()) }
                }
            }
        }
    }

    private fun buildDetailItems(
        bookmarks: List<Bookmark>,
        subCollections: List<Collection>,
        sortOrder: SortOrder
    ): List<CollectionDetailItem> {
        val comparator = when (sortOrder) {
            SortOrder.DATE_NEWEST -> compareByDescending<CollectionDetailItem> { it.createdAt }
            SortOrder.DATE_OLDEST -> compareBy<CollectionDetailItem> { it.createdAt }
            SortOrder.TITLE_ASC -> compareBy<CollectionDetailItem> { it.title.lowercase() }
            SortOrder.TITLE_DESC -> compareByDescending<CollectionDetailItem> { it.title.lowercase() }
        }

        val pinnedBookmarks = bookmarks.filter { it.isPinned }
            .sortedBy { it.pinnedAt ?: Long.MAX_VALUE }
            .map { CollectionDetailItem.BookmarkRow(it) }

        val subCollectionRows = subCollections
            .map { CollectionDetailItem.SubCollectionRow(it) }
            .sortedWith(comparator)

        val regularBookmarks = bookmarks.filterNot { it.isPinned }
            .map { CollectionDetailItem.BookmarkRow(it) }
            .sortedWith(comparator)

        return pinnedBookmarks + subCollectionRows + regularBookmarks
    }

    private fun sortBookmarks(bookmarks: List<Bookmark>, sortOrder: SortOrder): List<Bookmark> {
        val sorted = when (sortOrder) {
            SortOrder.DATE_NEWEST -> bookmarks.sortedByDescending { it.createdAt }
            SortOrder.DATE_OLDEST -> bookmarks.sortedBy { it.createdAt }
            SortOrder.TITLE_ASC -> bookmarks.sortedBy { it.title?.lowercase() }
            SortOrder.TITLE_DESC -> bookmarks.sortedByDescending { it.title?.lowercase() }
        }
        return sorted.sortedWith(
            compareByDescending<Bookmark> { it.isPinned }
                .thenBy { it.pinnedAt ?: Long.MAX_VALUE }
        )
    }
}
