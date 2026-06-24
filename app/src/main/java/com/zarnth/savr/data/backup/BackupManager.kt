package com.zarnth.savr.data.backup

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.zarnth.savr.data.local.BackupBookmark
import com.zarnth.savr.data.local.BackupCollection
import com.zarnth.savr.data.local.BackupData
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
                bookmarkDao.getBookmarks(),
                collectionDao.getAllCollections()
            ) { bookmarks, collections ->
                val backupBookmarks = bookmarks.map { BackupBookmark(url = it.url, title = it.title, description = it.description, imageUrl = it.imageUrl) }
                val backupCollections = collections.mapNotNull { collection ->
                    val urls = collectionDao.getBookmarkUrlsForCollection(collection.id)
                    if (collection.name.isNotBlank()) BackupCollection(name = collection.name, bookmarkUrls = urls) else null
                }
                BackupData(bookmarks = backupBookmarks, collections = backupCollections)
            }
                .debounce(500)
                .collect { data ->
                    val jsonString = json.encodeToString(data)
                    writeToBothLocations(jsonString)
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

    private fun writeToDownloads(jsonString: String) {
        val collectionUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        val selection = "${MediaStore.Downloads.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(FILE_NAME)
        context.contentResolver.delete(collectionUri, selection, selectionArgs)
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, FILE_NAME)
            put(MediaStore.Downloads.MIME_TYPE, "application/json")
            put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/Savr")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Downloads.IS_PENDING, 0)
            }
        }
        val uri = context.contentResolver.insert(collectionUri, contentValues)
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { output ->
                output.write(jsonString.toByteArray())
            }
        }
    }

    fun readInternalBackup(): String? {
        val file = getInternalFile()
        return if (file.exists()) file.readText() else null
    }

    suspend fun generateBackupJson(): String {
        val bookmarks = bookmarkDao.getBookmarks().first()
        val collections = collectionDao.getAllCollections().first()

        val backupBookmarks = bookmarks.map { BackupBookmark(url = it.url, title = it.title, description = it.description, imageUrl = it.imageUrl) }
        val backupCollections = collections.mapNotNull { collection ->
            val urls = collectionDao.getBookmarkUrlsForCollection(collection.id)
            if (collection.name.isNotBlank()) BackupCollection(name = collection.name, bookmarkUrls = urls) else null
        }

        val data = BackupData(bookmarks = backupBookmarks, collections = backupCollections)
        return json.encodeToString(data)
    }

    suspend fun importFromJson(jsonString: String) {
        val backupData = json.decodeFromString<BackupData>(jsonString)

        val existingUrls = bookmarkDao.getBookmarks().first().map { it.url }.toSet()

        for (b in backupData.bookmarks) {
            if (b.url !in existingUrls) {
                bookmarkDao.insert(BookmarkEntity(url = b.url, title = b.title, description = b.description, imageUrl = b.imageUrl))
            }
        }

        val bookmarkMap = bookmarkDao.getBookmarks().first().associateBy { it.url }

        val existingCollections = collectionDao.getAllCollectionsRaw().first().associateBy { it.name }
        val collectionNameToId = existingCollections.mapValues { it.value.id }.toMutableMap()

        for (c in backupData.collections) {
            val collectionId = collectionNameToId.getOrPut(c.name) {
                collectionDao.insertCollection(CollectionEntity(name = c.name))
            }
            c.bookmarkUrls.forEach { url ->
                bookmarkMap[url]?.let { bm ->
                    collectionDao.addBookmarkToCollection(BookmarkCollectionCrossRef(bm.id, collectionId))
                }
            }
        }
    }
}
