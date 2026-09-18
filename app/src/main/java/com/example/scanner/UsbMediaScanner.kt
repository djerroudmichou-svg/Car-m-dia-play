package com.example.scanner

import android.content.Context
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.MediaStore
import android.util.Log
import com.example.data.CoverArtResolver
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
     * CACHE-FIRST: Preserves existing database items and avoids resetting or flashing to 0.
     */
    suspend fun syncAllMountedVolumes() {
        if (_isScanning.value) return
        _isScanning.value = true
        _scanProgress.value = ""

        try {
            val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as? StorageManager
            val currentlyMountedPaths = mutableSetOf<String>()
            val activeVolumeIds = mutableSetOf<String>()

            // 1. Scan Internal Storage if available (always keep mounted)
            try {
                val internalDir = Environment.getExternalStorageDirectory()
                if (internalDir != null && internalDir.exists() && internalDir.canRead()) {
                    currentlyMountedPaths.add(internalDir.absolutePath)
                    val internalVolumeId = "internal_storage"
                    activeVolumeIds.add(internalVolumeId)

                    val internalVolume = VolumeEntity(
                        volumeId = internalVolumeId,
                        rootPath = internalDir.absolutePath,
                        label = "Internal Storage",
                        lastScanned = System.currentTimeMillis(),
                        isMounted = true
                    )
                    repository.insertVolume(internalVolume)
                    scanInternalStorage(internalVolume, internalDir)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error checking internal storage", e)
            }

            // 2. Discover Mounted USB / OTG Volumes via StorageManager
            if (storageManager != null) {
                try {
                    val storageVolumes = storageManager.storageVolumes
                    for (volume in storageVolumes) {
                        try {
                            val state = volume.state
                            if (state == Environment.MEDIA_MOUNTED || state == Environment.MEDIA_MOUNTED_READ_ONLY) {
                                val rootDir = getVolumeDirectory(volume) ?: continue
                                if (!rootDir.exists() || !rootDir.canRead()) continue

                                // Skip if it's primary and already scanned as internal storage
                                if (rootDir.absolutePath in currentlyMountedPaths && volume.isPrimary) continue

                                currentlyMountedPaths.add(rootDir.absolutePath)
                                val uuid = volume.uuid ?: generateStableUuidFromPath(rootDir.absolutePath)
                                val volumeId = "usb_${uuid.replace("-", "_")}"
                                activeVolumeIds.add(volumeId)

                                val label = volume.getDescription(context)?.ifBlank { null } ?: "USB (${rootDir.name})"

                                Log.d(TAG, "Mounted USB detected: $label at ${rootDir.absolutePath} with ID $volumeId")

                                val volumeEntity = VolumeEntity(
                                    volumeId = volumeId,
                                    rootPath = rootDir.absolutePath,
                                    label = label,
                                    lastScanned = System.currentTimeMillis(),
                                    isMounted = true
                                )
                                repository.insertVolume(volumeEntity)

                                scanVolumeWithCacheValidation(volumeEntity, rootDir)
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Error inspecting storage volume", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "StorageManager storageVolumes retrieval failed", e)
                }
            }

            // 3. Fallback discovery for Android Head Units (Allwinner, Rockchip, MTK, Android 7/8/9 OTG paths)
            discoverAlternativeUsbPaths(currentlyMountedPaths, activeVolumeIds)

            // 4. Update mount status for all known volumes without wiping database
            try {
                val existingVolumes = repository.getAllVolumes()
                for (v in existingVolumes) {
                    val shouldBeMounted = activeVolumeIds.contains(v.volumeId)
                    if (v.isMounted != shouldBeMounted) {
                        repository.updateMountStatus(v.volumeId, shouldBeMounted)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error updating volume mount states", e)
            }

            Log.d(TAG, "Sync complete. Active volumes: ${currentlyMountedPaths.size}")
        } catch (e: Exception) {
            Log.e(TAG, "Error in syncAllMountedVolumes", e)
        } finally {
            _isScanning.value = false
            _scanProgress.value = ""
        }
    }

    /**
     * Checks known Android Head Unit mount paths for USB storage (/storage, /mnt/media_rw, /mnt/usb).
     */
    private suspend fun discoverAlternativeUsbPaths(
        currentlyMountedPaths: MutableSet<String>,
        activeVolumeIds: MutableSet<String>
    ) {
        val candidateRoots = mutableListOf<File>()

        // Check Context secondary storage directories
        try {
            val extDirs = androidx.core.content.ContextCompat.getExternalFilesDirs(context, null)
            for (dir in extDirs) {
                if (dir != null) {
                    var parent: File? = dir
                    while (parent != null && parent.parentFile != null && parent.parentFile?.absolutePath != "/storage" && parent.parentFile?.absolutePath != "/mnt") {
                        parent = parent.parentFile
                    }
                    if (parent != null && parent.exists() && parent.canRead()) {
                        candidateRoots.add(parent)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error inspecting getExternalFilesDirs", e)
        }

        // Common Car Head Unit mount locations
        val searchFolders = listOf("/storage", "/mnt/media_rw", "/mnt/usb", "/mnt/udisk", "/mnt/usb_storage")
        for (folderPath in searchFolders) {
            try {
                val folder = File(folderPath)
                if (folder.exists() && folder.isDirectory && folder.canRead()) {
                    val subDirs = folder.listFiles() ?: continue
                    for (sub in subDirs) {
                        if (sub.isDirectory && sub.canRead() && !sub.name.startsWith(".") && sub.name != "emulated" && sub.name != "self") {
                            candidateRoots.add(sub)
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore security restrictions on certain paths
            }
        }

        for (rootDir in candidateRoots.distinctBy { it.absolutePath }) {
            try {
                if (rootDir.absolutePath in currentlyMountedPaths) continue
                if (!rootDir.exists() || !rootDir.canRead()) continue

                currentlyMountedPaths.add(rootDir.absolutePath)
                val uuid = generateStableUuidFromPath(rootDir.absolutePath)
                val volumeId = "usb_${uuid.replace("-", "_")}"
                activeVolumeIds.add(volumeId)

                val label = "USB (${rootDir.name})"
                Log.d(TAG, "Discovered Car Head Unit USB path: $label at ${rootDir.absolutePath}")

                val volumeEntity = VolumeEntity(
                    volumeId = volumeId,
                    rootPath = rootDir.absolutePath,
                    label = label,
                    lastScanned = System.currentTimeMillis(),
                    isMounted = true
                )
                repository.insertVolume(volumeEntity)
                scanVolumeWithCacheValidation(volumeEntity, rootDir)
            } catch (e: Exception) {
                Log.w(TAG, "Error scanning candidate USB path: ${rootDir.absolutePath}", e)
            }
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
                        for (file in diskFiles) {
                            try {
                                mediaStoreItems.add(extractMetadata(file, volume.volumeId))
                            } catch (e: Exception) {
                                Log.w(TAG, "Error extracting metadata for internal file: ${file.path}", e)
                            }
                        }
                    }
                }

                // Check cache match with current MediaStore state
                val cachedItems = repository.getMediaForVolume(volume.volumeId)
                val cachedMap = cachedItems.associateBy { it.filePath }
                val isIdentical = cachedItems.isNotEmpty() &&
                    cachedItems.size == mediaStoreItems.size &&
                    mediaStoreItems.all { item ->
                        val c = cachedMap[item.filePath]
                        c != null && c.size == item.size && c.lastModified == item.lastModified
                    }

                if (isIdentical) {
                    Log.d(TAG, "Internal storage cache matches MediaStore. Skipping update.")
                    return@withContext
                }

                // Perform smart incremental update: do NOT clear whole volume
                val mediaStorePaths = mediaStoreItems.map { it.filePath }.toSet()
                val itemsToDelete = cachedItems.filter { !mediaStorePaths.contains(it.filePath) }
                val itemsToInsert = mediaStoreItems.filter { item ->
                    val cached = cachedMap[item.filePath]
                    cached == null || cached.lastModified != item.lastModified || cached.size != item.size
                }

                Log.d(TAG, "Updating internal storage cache: +${itemsToInsert.size} new/modified, -${itemsToDelete.size} deleted, ${cachedItems.size - itemsToDelete.size} preserved.")
                if (itemsToDelete.isNotEmpty()) {
                    repository.deleteMediaItems(itemsToDelete)
                }
                if (itemsToInsert.isNotEmpty()) {
                    repository.insertMediaItems(itemsToInsert)
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

                    for (file in filesToScan) {
                        try {
                            val entity = extractMetadata(file, volume.volumeId)
                            freshItems.add(entity)
                        } catch (e: Exception) {
                            Log.w(TAG, "Metadata extraction failed for ${file.path}", e)
                        }
                    }

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

                    val coverArtPath = CoverArtResolver.findCompanionCoverArt(file)

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
                            mimeType = mime,
                            coverArtPath = coverArtPath
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
    private fun extractMetadata(file: File, volumeId: String): MediaItemEntity {
        val path = file.absolutePath
        val ext = file.extension.lowercase()
        val isVideo = VIDEO_EXTENSIONS.contains(ext)
        val mimeType = if (isVideo) "video/$ext" else "audio/$ext"

        var title = file.nameWithoutExtension
        var artist = ""
        var album = ""
        var duration = 0L
        var coverArtPath: String? = null

        var retriever: MediaMetadataRetriever? = null
        try {
            retriever = MediaMetadataRetriever()
            retriever.setDataSource(path)
            title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: file.nameWithoutExtension
            artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: ""
            album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: ""
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            duration = durationStr?.toLongOrNull() ?: 0L

            if (!isVideo) {
                val picture = retriever.embeddedPicture
                if (picture != null && picture.isNotEmpty()) {
                    val coversDir = File(context.cacheDir, "album_covers")
                    if (!coversDir.exists()) coversDir.mkdirs()
                    val hash = Math.abs(path.hashCode()).toString()
                    val artFile = File(coversDir, "art_${hash}_${file.length()}.jpg")
                    if (!artFile.exists() || artFile.length() == 0L) {
                        artFile.writeBytes(picture)
                    }
                    coverArtPath = artFile.absolutePath
                } else {
                    coverArtPath = CoverArtResolver.findCompanionCoverArt(file)
                }
            }
        } catch (t: Throwable) {
            // Silently fall back to file names if metadata retrieval fails or memory is tight
            Log.w(TAG, "Could not extract metadata for $path, falling back to defaults: ${t.message}")
        } finally {
            try {
                retriever?.release()
            } catch (ignored: Throwable) {
                // Ignore release failure
            }
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
