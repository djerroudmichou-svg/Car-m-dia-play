package com.example.scanner

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.MediaStore
import android.util.Log
import com.example.data.MediaItemEntity
import com.example.data.MediaRepository
import com.example.data.VolumeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class UsbMediaScanner(
    private val context: Context,
    private val repository: MediaRepository
) {
    companion object {
        private const val TAG = "UsbMediaScanner"
        private val AUDIO_EXTENSIONS = setOf(
            "mp3", "wav", "ogg", "flac", "m4a", "aac", "wma", "opus", "amr",
            "mid", "midi", "ape", "aiff", "alac", "dsf", "dff", "mka", "mp2", "mpa", "oga"
        )
        private val VIDEO_EXTENSIONS = setOf(
            "mp4", "mkv", "avi", "webm", "3gp", "mov", "ts", "m4v", "wmv",
            "flv", "f4v", "asf", "mpg", "mpeg", "vob", "ogv", "m2ts", "divx", "3g2", "m2v"
        )
    }

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanProgress = MutableStateFlow("")
    val scanProgress: StateFlow<String> = _scanProgress.asStateFlow()

    /**
     * Finds and synchronizes all currently mounted storage volumes (USB drives and Internal Storage).
     */
    suspend fun syncAllMountedVolumes() {
        if (_isScanning.value) return
        _isScanning.value = true
        _scanProgress.value = "جاري اكتشاف وحدات التخزين..."

        try {
            val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            val storageVolumes = storageManager.storageVolumes
            val currentlyMountedPaths = mutableSetOf<String>()

            // Unmount all volumes first in local status, then we will set active ones to true.
            repository.unmountAllVolumes()

            // 1. Scan Internal Storage if available
            try {
                val internalDir = Environment.getExternalStorageDirectory()
                if (internalDir != null && internalDir.exists() && internalDir.canRead()) {
                    currentlyMountedPaths.add(internalDir.absolutePath)
                    val internalVolume = VolumeEntity(
                        volumeId = "internal_storage",
                        rootPath = internalDir.absolutePath,
                        label = "ذاكرة الجهاز الداخلية",
                        lastScanned = System.currentTimeMillis(),
                        isMounted = true
                    )
                    repository.insertVolume(internalVolume)
                    _scanProgress.value = "فحص ذاكرة الجهاز الداخلية..."
                    scanInternalStorage(internalVolume, internalDir)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error checking internal storage", e)
            }

            // 2. Scan Mounted USB / OTG Volumes
            for (volume in storageVolumes) {
                val state = volume.state
                if (state == Environment.MEDIA_MOUNTED || state == Environment.MEDIA_MOUNTED_READ_ONLY) {
                    val rootDir = getVolumeDirectory(volume) ?: continue
                    if (!rootDir.exists() || !rootDir.canRead()) continue

                    // Skip if it's primary and already scanned as internal storage
                    if (rootDir.absolutePath in currentlyMountedPaths && volume.isPrimary) continue

                    currentlyMountedPaths.add(rootDir.absolutePath)
                    val uuid = volume.uuid ?: generateStableUuidFromPath(rootDir.absolutePath)
                    val label = volume.getDescription(context) ?: "USB Drive"

                    Log.d(TAG, "Mounted USB detected: $label at ${rootDir.absolutePath} with UUID $uuid")

                    val volumeEntity = VolumeEntity(
                        volumeId = uuid,
                        rootPath = rootDir.absolutePath,
                        label = label,
                        lastScanned = System.currentTimeMillis(),
                        isMounted = true
                    )
                    repository.insertVolume(volumeEntity)

                    _scanProgress.value = "جاري فحص $label..."
                    scanVolumeWithCacheValidation(volumeEntity, rootDir)
                }
            }

            Log.d(TAG, "Sync complete. Active volumes: ${currentlyMountedPaths.size}")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncAllMountedVolumes", e)
        } finally {
            _isScanning.value = false
            _scanProgress.value = ""
        }
    }

    /**
     * Scans internal device storage using Android MediaStore and directory fallback.
     * Prefers MediaStore for speed.
     */
    private suspend fun scanInternalStorage(volume: VolumeEntity, rootDir: File) {
        withContext(Dispatchers.IO) {
            try {
                val mediaStoreItems = mutableListOf<MediaItemEntity>()
                queryAudioMediaStore(volume.volumeId, mediaStoreItems)
                queryVideoMediaStore(volume.volumeId, mediaStoreItems)

                // If MediaStore is empty, we MUST scan manually
                if (mediaStoreItems.isEmpty()) {
                    Log.d(TAG, "MediaStore is empty for internal storage, falling back to disk scan")
                    val diskFiles = mutableListOf<File>()
                    listOf(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                    ).forEach { folder ->
                        if (folder != null && folder.exists()) {
                            findMediaFilesRecursively(folder, diskFiles)
                        }
                    }

                    if (diskFiles.isNotEmpty()) {
                        val retriever = MediaMetadataRetriever()
                        for (file in diskFiles) {
                            try {
                                mediaStoreItems.add(extractMetadata(file, volume.volumeId, retriever))
                            } catch (e: Exception) {
                                Log.w(TAG, "Error extracting metadata for internal file: ${file.path}", e)
                            }
                        }
                        try { retriever.release() } catch (e: Exception) {}
                    }
                }

                // Check cache match with current MediaStore state
                val cachedItems = repository.getMediaForVolume(volume.volumeId)
                val isIdentical = cachedItems.isNotEmpty() &&
                    cachedItems.size == mediaStoreItems.size &&
                    run {
                        val cachedMap = cachedItems.associateBy { it.filePath }
                        mediaStoreItems.all { item ->
                            val c = cachedMap[item.filePath]
                            c != null && c.size == item.size && c.lastModified == item.lastModified
                        }
                    }

                if (isIdentical) {
                    Log.d(TAG, "Internal storage cache matches MediaStore. Skipping update.")
                    return@withContext
                }

                // If difference detected, update DB
                Log.d(TAG, "Updating internal storage cache with ${mediaStoreItems.size} items.")
                repository.clearMediaForVolume(volume.volumeId)
                if (mediaStoreItems.isNotEmpty()) {
                    repository.insertMediaItems(mediaStoreItems)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error scanning internal storage", e)
            }
        }
    }

    /**
     * Scans a USB storage volume.
     * Optimized to perform incremental updates: only extracts metadata for NEW or CHANGED files.
     */
    private suspend fun scanVolumeWithCacheValidation(volume: VolumeEntity, rootDir: File) {
        withContext(Dispatchers.IO) {
            try {
                // 1. Fetch cached media items for this volume
                val cachedItems = repository.getMediaForVolume(volume.volumeId)
                val cachedMap = cachedItems.associateBy { it.filePath }

                // 2. Find files on disk (basic listing is fast)
                val filesOnDisk = mutableListOf<File>()
                findMediaFilesRecursively(rootDir, filesOnDisk)

                Log.d(TAG, "Syncing USB volume ${volume.label}. Disk: ${filesOnDisk.size}, Cache: ${cachedItems.size}")

                // 3. Detect changes
                val itemsToDelete = mutableListOf<MediaItemEntity>()
                val filesToScan = mutableListOf<File>()
                val itemsToKeep = mutableListOf<MediaItemEntity>()

                val diskPaths = filesOnDisk.map { it.absolutePath }.toSet()
                
                // Identify deleted items
                for (cached in cachedItems) {
                    if (!diskPaths.contains(cached.filePath)) {
                        itemsToDelete.add(cached)
                    }
                }

                // Identify new or changed files
                for (file in filesOnDisk) {
                    val cached = cachedMap[file.absolutePath]
                    if (cached == null || cached.lastModified != file.lastModified() || cached.size != file.length()) {
                        filesToScan.add(file)
                    } else {
                        itemsToKeep.add(cached)
                    }
                }

                if (itemsToDelete.isEmpty() && filesToScan.isEmpty()) {
                    Log.i(TAG, "USB volume ${volume.label} is already up to date.")
                    return@withContext
                }

                Log.i(TAG, "USB Incremental Sync: ${filesToScan.size} new/changed, ${itemsToDelete.size} deleted, ${itemsToKeep.size} unchanged.")

                // 4. Perform updates
                if (itemsToDelete.isNotEmpty()) {
                    repository.deleteMediaItems(itemsToDelete)
                }

                if (filesToScan.isNotEmpty()) {
                    val freshItems = mutableListOf<MediaItemEntity>()
                    val retriever = MediaMetadataRetriever()
                    
                    for ((index, file) in filesToScan.withIndex()) {
                        _scanProgress.value = "فحص الملفات الجديدة (${index + 1}/${filesToScan.size})"
                        try {
                            val entity = extractMetadata(file, volume.volumeId, retriever)
                            freshItems.add(entity)
                        } catch (e: Exception) {
                            Log.w(TAG, "Metadata extraction failed for ${file.path}", e)
                        }
                    }
                    
                    try { retriever.release() } catch (e: Exception) {}
                    
                    if (freshItems.isNotEmpty()) {
                        repository.insertMediaItems(freshItems)
                    }
                }

                Log.i(TAG, "Incremental sync complete for ${volume.label}")
            } catch (e: Exception) {
                Log.e(TAG, "Error in incremental sync for ${volume.volumeId}", e)
            }
        }
    }

    private fun queryAudioMediaStore(volumeId: String, items: MutableList<MediaItemEntity>) {
        try {
            val projection = arrayOf(
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATE_MODIFIED,
                MediaStore.Audio.Media.MIME_TYPE
            )
            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null
            )?.use { cursor ->
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val modCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)

                while (cursor.moveToNext()) {
                    val path = cursor.getString(dataCol) ?: continue
                    val file = File(path)
                    if (!file.exists()) continue

                    val title = cursor.getString(titleCol) ?: file.nameWithoutExtension
                    val artist = cursor.getString(artistCol)
                    val album = cursor.getString(albumCol)
                    val duration = cursor.getLong(durCol)
                    val size = cursor.getLong(sizeCol)
                    val lastMod = cursor.getLong(modCol) * 1000L
                    val mime = cursor.getString(mimeCol) ?: "audio/mpeg"

                    items.add(
                        MediaItemEntity(
                            filePath = path,
                            volumeId = volumeId,
                            title = title,
                            artist = artist,
                            album = album,
                            duration = duration,
                            size = size,
                            lastModified = lastMod,
                            isVideo = false,
                            mimeType = mime
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying audio media store", e)
        }
    }

    private fun queryVideoMediaStore(volumeId: String, items: MutableList<MediaItemEntity>) {
        try {
            val projection = arrayOf(
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_MODIFIED,
                MediaStore.Video.Media.MIME_TYPE
            )
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null
            )?.use { cursor ->
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
                val durCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val modCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)

                while (cursor.moveToNext()) {
                    val path = cursor.getString(dataCol) ?: continue
                    val file = File(path)
                    if (!file.exists()) continue

                    val title = cursor.getString(titleCol) ?: file.nameWithoutExtension
                    val duration = cursor.getLong(durCol)
                    val size = cursor.getLong(sizeCol)
                    val lastMod = cursor.getLong(modCol) * 1000L
                    val mime = cursor.getString(mimeCol) ?: "video/mp4"

                    items.add(
                        MediaItemEntity(
                            filePath = path,
                            volumeId = volumeId,
                            title = title,
                            artist = null,
                            album = null,
                            duration = duration,
                            size = size,
                            lastModified = lastMod,
                            isVideo = true,
                            mimeType = mime
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying video media store", e)
        }
    }

    /**
     * Recursively retrieves all music and video files from a directory.
     */
    private fun findMediaFilesRecursively(dir: File, result: MutableList<File>) {
        val files = dir.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) {
                // Ignore hidden directories to keep things efficient
                if (!file.name.startsWith(".")) {
                    findMediaFilesRecursively(file, result)
                }
            } else {
                val ext = file.extension.lowercase()
                if (AUDIO_EXTENSIONS.contains(ext) || VIDEO_EXTENSIONS.contains(ext)) {
                    result.add(file)
                }
            }
        }
    }

    /**
     * Uses MediaMetadataRetriever to parse and build a MediaItemEntity for Room.
     */
    private fun extractMetadata(file: File, volumeId: String, retriever: MediaMetadataRetriever): MediaItemEntity {
        val path = file.absolutePath
        val ext = file.extension.lowercase()
        val isVideo = VIDEO_EXTENSIONS.contains(ext)
        val mimeType = if (isVideo) "video/$ext" else "audio/$ext"

        var title = file.nameWithoutExtension
        var artist = "فنان غير معروف"
        var album = "ألبوم غير معروف"
        var duration = 0L
        var coverArtPath: String? = null

        try {
            retriever.setDataSource(path)
            title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: file.nameWithoutExtension
            artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "فنان غير معروف"
            album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "ألبوم غير معروف"
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            duration = durationStr?.toLongOrNull() ?: 0L

            if (!isVideo) {
                val picture = retriever.embeddedPicture
                if (picture != null && picture.isNotEmpty()) {
                    val coversDir = File(context.cacheDir, "album_covers")
                    if (!coversDir.exists()) coversDir.mkdirs()
                    val hash = Math.abs(path.hashCode()).toString()
                    val artFile = File(coversDir, "art_${hash}_${file.length()}.jpg")
                    if (!artFile.exists()) {
                        artFile.writeBytes(picture)
                    }
                    coverArtPath = artFile.absolutePath
                }
            }
        } catch (e: Exception) {
            // Silently fall back to file names if metadata retrieval fails (helpful during mock testing or permission lags)
            Log.w(TAG, "Could not extract metadata for $path, falling back to defaults", e)
        }

        return MediaItemEntity(
            filePath = path,
            volumeId = volumeId,
            title = title,
            artist = artist,
            album = album,
            duration = duration,
            size = file.length(),
            lastModified = file.lastModified(),
            isVideo = isVideo,
            mimeType = mimeType,
            coverArtPath = coverArtPath
        )
    }

    /**
     * Safely fetches the File directory of a StorageVolume using API level 30 or reflection fallback.
     */
    private fun getVolumeDirectory(volume: android.os.storage.StorageVolume): File? {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                return volume.directory
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch volume.directory directly, trying reflection", e)
        }
        return try {
            val getPathMethod = volume.javaClass.getMethod("getPath")
            val path = getPathMethod.invoke(volume) as String
            File(path)
        } catch (e: Exception) {
            Log.e(TAG, "Failed reflection fallback for StorageVolume path", e)
            null
        }
    }

    /**
     * Generates a stable UUID for a path when Volume UUID is not provided by Android.
     */
    private fun generateStableUuidFromPath(path: String): String {
        return UUID.nameUUIDFromBytes(path.toByteArray()).toString()
    }
}
