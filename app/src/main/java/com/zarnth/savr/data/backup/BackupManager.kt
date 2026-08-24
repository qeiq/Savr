package com.zarnth.savr.data.backup

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.zarnth.savr.data.local.BackupBookmark
import com.zarnth.savr.data.local.BackupCollection
import com.zarnth.savr.data.local.BackupData
import com.zarnth.savr.link_fetcher.LinkMetadataParser
import com.zarnth.savr.data.local.dao.BookmarkDao
import com.zarnth.savr.data.local.dao.CollectionDao
import com.zarnth.savr.data.local.entity.BookmarkCollectionCrossRef
import com.zarnth.savr.data.local.entity.BookmarkEntity
import com.zarnth.savr.data.local.entity.CollectionEntity
import com.zarnth.savr.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class BackupManager(
    private val bookmarkDao: BookmarkDao,
    private val collectionDao: CollectionDao,
    private val settingsRepository: SettingsRepository,
    private val context: Context
) {
    companion object {
        private const val FILE_NAME = "savr_autobackup.json"
        private const val BACKUP_DIR = "backups"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var autoBackupJob: Job? = null
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    private val _lastBackupTimeMillis = MutableStateFlow(0L)
    val lastBackupTimeMillis: StateFlow<Long> = _lastBackupTimeMillis.asStateFlow()

    init {
        if (settingsRepository.getAutoBackupEnabled()) {
            refreshLastBackupTime()
            startAutoBackup()
        }
    }

    fun refreshLastBackupTime() {
        val file = getInternalFile()
        _lastBackupTimeMillis.value = if (file.exists()) file.lastModified() else 0L
    }

    private fun getInternalFile(): File {
        return File(File(context.filesDir, BACKUP_DIR), FILE_NAME)
    }

    fun startAutoBackup() {
        stopAutoBackup()
        autoBackupJob = scope.launch {
            combine(
                bookmarkDao.getAllBookmarksForBackup(),
                collectionDao.getAllCollectionsRaw()
            ) { bookmarks, collections ->
                val backupBookmarks = bookmarks.map { BackupBookmark(url = it.url, title = it.title, description = it.description, imageUrl = it.imageUrl, createdAt = it.createdAt, isCollectionOnly = it.isCollectionOnly, isPinned = it.isPinned, pinnedAt = it.pinnedAt) }
                val backupCollections = buildBackupCollections(collections)
                BackupData(bookmarks = backupBookmarks, collections = backupCollections)
            }
                .debounce(500)
                .collect { data ->
                    if (data.bookmarks.isNotEmpty() || data.collections.isNotEmpty()) {
                        val jsonString = json.encodeToString(data)
                        writeToBothLocations(jsonString)
                    }
                }
        }
    }

    fun stopAutoBackup() {
        autoBackupJob?.cancel()
        autoBackupJob = null
    }

    private fun writeToBothLocations(jsonString: String) {
        val backupDir = File(context.filesDir, BACKUP_DIR)
        backupDir.mkdirs()
        val internalFile = File(backupDir, FILE_NAME)
        internalFile.writeText(jsonString)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            writeToDownloads(jsonString)
        }

        _lastBackupTimeMillis.value = System.currentTimeMillis()
    }

    @SuppressLint("NewApi")
    private fun writeToDownloads(jsonString: String) {
        val resolver = context.contentResolver
        val bytes = jsonString.toByteArray()

        deleteAllBackupEntries(resolver)

        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, FILE_NAME)
            put(MediaStore.Downloads.MIME_TYPE, "application/json")
            put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/Savr")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            runCatching {
                resolver.openOutputStream(it)?.use { output ->
                    output.write(bytes)
                }
                val done = ContentValues().apply { put(MediaStore.Downloads.IS_PENDING, 0) }
                resolver.update(it, done, null, null)
            }
        }
    }

    @SuppressLint("NewApi")
    private fun deleteAllBackupEntries(resolver: android.content.ContentResolver) {
        val relativePath = "${Environment.DIRECTORY_DOWNLOADS}/Savr/"
        val projection = arrayOf(MediaStore.Downloads._ID)
        val selection =
            "${MediaStore.Downloads.RELATIVE_PATH} = ? AND ${MediaStore.Downloads.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf(relativePath, "savr_autobackup%.json")
        runCatching {
            resolver.query(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    runCatching {
                        resolver.delete(
                            MediaStore.Downloads.EXTERNAL_CONTENT_URI.buildUpon()
                                .appendPath(id.toString()).build(),
                            null, null
                        )
                    }
                }
            }
        }
    }

    fun readInternalBackup(): String? {
        val file = getInternalFile()
        return if (file.exists()) file.readText() else null
    }

    suspend fun generateBackupJson(): String {
        val bookmarks = bookmarkDao.getAllBookmarksForBackupOnce()
        val collections = collectionDao.getAllCollectionsRaw().first()

        val backupBookmarks = bookmarks.map { BackupBookmark(url = it.url, title = it.title, description = it.description, imageUrl = it.imageUrl, createdAt = it.createdAt, isCollectionOnly = it.isCollectionOnly, isPinned = it.isPinned, pinnedAt = it.pinnedAt) }
        val backupCollections = buildBackupCollections(collections)

        val data = BackupData(bookmarks = backupBookmarks, collections = backupCollections)
        return json.encodeToString(data)
    }

    private suspend fun buildBackupCollections(collections: List<CollectionEntity>): List<BackupCollection> {
        val byId = collections.associateBy { it.id }
        return orderParentsFirst(collections).mapNotNull { collection ->
            if (collection.name.isBlank()) return@mapNotNull null
            BackupCollection(
                name = collection.name,
                bookmarkUrls = collectionDao.getBookmarkUrlsForCollection(collection.id),
                pinnedBookmarkUrls = collectionDao.getPinnedBookmarkUrlsForCollection(collection.id),
                parentName = collection.parentCollectionId?.let { byId[it]?.name }
            )
        }
    }

    private fun orderParentsFirst(collections: List<CollectionEntity>): List<CollectionEntity> {
        val childrenOf = collections.groupBy { it.parentCollectionId }
        val ordered = mutableListOf<CollectionEntity>()
        val visited = mutableSetOf<Long>()
        fun walk(parentId: Long?) {
            for (child in childrenOf[parentId].orEmpty().sortedByDescending { it.createdAt }) {
                if (visited.add(child.id)) {
                    ordered.add(child)
                    walk(child.id)
                }
            }
        }
        walk(null)
        for (c in collections) {
            if (visited.add(c.id)) ordered.add(c)
        }
        return ordered
    }

    suspend fun generateExportHtml(): String {
        val bookmarks = bookmarkDao.getBookmarksOnce()
        val collections = collectionDao.getAllCollectionsRaw().first()
        val sb = StringBuilder()
        sb.appendLine("<!DOCTYPE NETSCAPE-Bookmark-file-1>")
        sb.appendLine("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">")
        sb.appendLine("<TITLE>Savr Bookmarks</TITLE>")
        sb.appendLine("<H1>Savr Bookmarks</H1>")
        sb.appendLine("<DL><p>")

        val byId = collections.associateBy { it.id }
        val childrenOf = collections.groupBy { it.parentCollectionId }
        val written = mutableSetOf<Long>()
        val assigned = mutableSetOf<Long>()

        suspend fun appendLinks(collectionId: Long, indent: String) {
            for (bm in bookmarks.filter { it.url in collectionDao.getBookmarkUrlsForCollection(collectionId) }) {
                assigned.add(bm.id)
                sb.appendLine("$indent<DT><A HREF=\"${escapeHtml(bm.url)}\"${if (bm.title != null) ">${escapeHtml(bm.title)}" else ">${escapeHtml(bm.url)}"}</A>")
            }
        }

        suspend fun appendFolder(c: CollectionEntity, indent: String) {
            if (!written.add(c.id)) return
            sb.appendLine("$indent<DT><H3>${escapeHtml(c.name)}")
            sb.appendLine("$indent<DL><p>")
            appendLinks(c.id, indent)
            for (child in childrenOf[c.id].orEmpty().sortedByDescending { it.createdAt }) {
                if (written.contains(child.id)) continue
                appendFolder(child, "$indent    ")
            }
            sb.appendLine("$indent</DL><p>")
        }

        suspend fun walkLevel(parentId: Long?, indent: String) {
            for (c in childrenOf[parentId].orEmpty().sortedByDescending { it.createdAt }) {
                appendFolder(c, indent)
            }
        }

        walkLevel(null, "")
        for (c in collections) {
            if (!written.contains(c.id)) appendFolder(c, "")
        }

        val unassigned = bookmarks.filter { it.id !in assigned }
        if (unassigned.isNotEmpty()) {
            sb.appendLine("<DT><H3>Uncategorized</H3>")
            sb.appendLine("<DL><p>")
            for (bm in unassigned) {
                sb.appendLine("<DT><A HREF=\"${escapeHtml(bm.url)}\"${if (bm.title != null) ">${escapeHtml(bm.title)}" else ">${escapeHtml(bm.url)}"}</A>")
            }
            sb.appendLine("</DL><p>")
        }

        sb.appendLine("</DL><p>")
        return sb.toString()
    }

    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("\"", "&quot;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
    }

    suspend fun importFromBrowserBookmarks(html: String): BrowserImportResult {
        val parser = BookmarkParser()
        val (parsedBookmarks, collectionNames) = parser.parse(html)

        val existingUrls = bookmarkDao.getBookmarksOnce().map { it.url }.toSet()
        var imported = 0
        var skipped = 0

        for (pb in parsedBookmarks) {
            if (pb.url in existingUrls) {
                skipped++
                continue
            }
            bookmarkDao.insertWithReturn(BookmarkEntity(url = pb.url, title = pb.title, description = null, imageUrl = null))
            imported++
        }

        val existingCollections = collectionDao.getAllCollectionsRaw().first().associateBy { it.name }
        val nameToId = existingCollections.mapValues { it.value.id }.toMutableMap()

        val bookmarkMap = bookmarkDao.getBookmarksOnce().associateBy { it.url }

        for (name in collectionNames) {
            val id = nameToId.getOrPut(name) {
                collectionDao.insertCollection(CollectionEntity(name = name))
            }
            parsedBookmarks.filter { it.collection == name }.forEach { pb ->
                bookmarkMap[pb.url]?.let { bm ->
                    collectionDao.addBookmarkToCollection(BookmarkCollectionCrossRef(bm.id, id))
                }
            }
        }

        return BrowserImportResult(imported = imported, skipped = skipped, collections = collectionNames.size)
    }

    suspend fun fetchMissingImages() {
        val parser = LinkMetadataParser()
        val missing = bookmarkDao.getBookmarksMissingMetadataOnce()
        withContext(Dispatchers.IO) {
            for (bm in missing) {
                try {
                    val meta = parser.parse(bm.url)
                    if (meta != null) {
                        bookmarkDao.updateMetadata(
                            id = bm.id,
                            title = meta.title?.takeIf { it.isNotBlank() } ?: bm.title,
                            description = meta.description?.takeIf { it.isNotBlank() } ?: bm.description,
                            imageUrl = meta.imageUrl?.takeIf { it.isNotBlank() } ?: bm.imageUrl
                        )
                    }
                } catch (_: Exception) { }
            }
        }
    }

    suspend fun importFromJson(jsonString: String) {
        val backupData = json.decodeFromString<BackupData>(jsonString)

        // url -> bookmark entity, kept up to date as rows are inserted so that
        // collection links below always resolve (even for URLs that are missing
        // from the backup's top-level bookmarks array).
        val bookmarkMap = bookmarkDao.getBookmarksOnce().associateBy { it.url }.toMutableMap()

        suspend fun ensureBookmark(url: String, source: BackupBookmark? = null): BookmarkEntity? {
            bookmarkMap[url]?.let { existing ->
                if (source?.isPinned == true) {
                    bookmarkDao.setPinned(existing.id, true, source.pinnedAt)
                }
                return existing
            }
            val hidden = bookmarkDao.findHiddenByUrl(url)
            val entity: BookmarkEntity? = if (hidden != null) {
                bookmarkDao.unhideBookmark(hidden.id, hidden.title, hidden.description, hidden.imageUrl, hidden.createdAt)
                bookmarkDao.getBookmarkByUrl(url)
            } else {
                val insertedId = bookmarkDao.insertWithReturn(BookmarkEntity(
                    url = url,
                    title = source?.title,
                    description = source?.description,
                    imageUrl = source?.imageUrl,
                    createdAt = source?.createdAt ?: System.currentTimeMillis(),
                    isCollectionOnly = source?.isCollectionOnly ?: true,
                    isPinned = source?.isPinned ?: false,
                    pinnedAt = source?.pinnedAt
                ))
                bookmarkDao.getBookmarkByUrl(url)
                    ?: BookmarkEntity(id = insertedId, url = url, title = source?.title, description = source?.description, imageUrl = source?.imageUrl, createdAt = source?.createdAt ?: System.currentTimeMillis(), isCollectionOnly = source?.isCollectionOnly ?: true, isPinned = source?.isPinned ?: false, pinnedAt = source?.pinnedAt)
            }
            if (entity != null) bookmarkMap[url] = entity
            return entity
        }

        for (b in backupData.bookmarks) {
            ensureBookmark(b.url, b)
        }

        val existingCollections = collectionDao.getAllCollectionsRaw().first().associateBy { it.name }
        val collectionNameToId = existingCollections.mapValues { it.value.id }.toMutableMap()

        suspend fun restoreCollection(c: BackupCollection) {
            val parentId = c.parentName?.let { collectionNameToId[it] }
            val collectionId = collectionNameToId.getOrPut(c.name) {
                collectionDao.insertCollection(CollectionEntity(name = c.name, parentCollectionId = parentId))
            }
            c.bookmarkUrls.forEach { url ->
                ensureBookmark(url)?.let { bm ->
                    collectionDao.addBookmarkToCollection(BookmarkCollectionCrossRef(bm.id, collectionId))
                }
            }
            if (c.pinnedBookmarkUrls.isNotEmpty()) {
                val base = System.currentTimeMillis()
                c.pinnedBookmarkUrls.forEachIndexed { index, url ->
                    ensureBookmark(url)?.let { bm ->
                        collectionDao.setPinnedInCollection(bm.id, collectionId, true, base + index)
                    }
                }
            }
        }

        val pending = backupData.collections.toMutableList()
        while (pending.isNotEmpty()) {
            val ready = pending.filter { it.parentName == null || collectionNameToId.containsKey(it.parentName) }
            if (ready.isEmpty()) break
            pending.removeAll(ready.toSet())
            for (c in ready) restoreCollection(c)
        }
        for (c in pending) restoreCollection(c)
    }
}
