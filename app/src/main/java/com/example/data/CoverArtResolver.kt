package com.example.data

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * High-performance, memory-cached Album Art and Cover Image Resolver.
 * Handles:
 * 1. Pre-extracted cached cover files in cache/album_covers
 * 2. Companion folder cover art images (cover.jpg, folder.jpg, album.jpg, front.jpg, artwork.jpg)
 * 3. Embedded ID3 APIC metadata extraction via MediaMetadataRetriever
 * 4. Android MediaStore Audio Album Art queries
 */
object CoverArtResolver {
    private const val TAG = "CoverArtResolver"
    private val memoryCache = ConcurrentHashMap<String, String>()

    private val COMMON_COVER_NAMES = listOf(
        "cover.jpg", "cover.png", "cover.jpeg", "cover.webp",
        "folder.jpg", "folder.png", "folder.jpeg", "folder.webp",
        "album.jpg", "album.png", "album.jpeg", "album.webp",
        "front.jpg", "front.png", "front.jpeg", "front.webp",
        "artwork.jpg", "artwork.png", "artwork.jpeg", "artwork.webp",
        ".folder.jpg", ".cover.jpg"
    )

    suspend fun resolveCoverArt(context: Context, coverArtPath: String?, filePath: String?): String? = withContext(Dispatchers.IO) {
        if (!coverArtPath.isNullOrEmpty()) {
            val file = File(coverArtPath)
            if (file.exists() && file.length() > 0L) {
                return@withContext coverArtPath
            }
        }

        if (filePath.isNullOrEmpty()) return@withContext null

        // 1. Check in-memory resolution cache
        memoryCache[filePath]?.let { cached ->
            if (File(cached).exists()) return@withContext cached
        }

        val songFile = File(filePath)

        // 2. Check disk cache
        try {
            val coversDir = File(context.cacheDir, "album_covers")
            if (!coversDir.exists()) coversDir.mkdirs()
            val hash = Math.abs(filePath.hashCode()).toString()
            val cachedArtFile = File(coversDir, "art_${hash}.jpg")
            if (cachedArtFile.exists() && cachedArtFile.length() > 0L) {
                memoryCache[filePath] = cachedArtFile.absolutePath
                return@withContext cachedArtFile.absolutePath
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error accessing disk art cache", e)
        }

        // 3. Check companion cover image files in the song's parent directory
        val parentDir = songFile.parentFile
        if (parentDir != null && parentDir.exists() && parentDir.canRead()) {
            for (name in COMMON_COVER_NAMES) {
                val candidate = File(parentDir, name)
                if (candidate.exists() && candidate.isFile && candidate.length() > 0L) {
                    val path = candidate.absolutePath
                    memoryCache[filePath] = path
                    return@withContext path
                }
            }

            // Also check any standalone image file in the directory
            val imageFiles = parentDir.listFiles { f ->
                f.isFile && f.length() > 0L && (
                    f.extension.equals("jpg", ignoreCase = true) ||
                    f.extension.equals("jpeg", ignoreCase = true) ||
                    f.extension.equals("png", ignoreCase = true) ||
                    f.extension.equals("webp", ignoreCase = true)
                )
            }
            if (!imageFiles.isNullOrEmpty()) {
                val firstArt = imageFiles[0].absolutePath
                memoryCache[filePath] = firstArt
                return@withContext firstArt
            }
        }

        // 4. Extract embedded picture using MediaMetadataRetriever
        if (songFile.exists() && songFile.canRead()) {
            var retriever: MediaMetadataRetriever? = null
            try {
                retriever = MediaMetadataRetriever()
                retriever.setDataSource(filePath)
                val picture = retriever.embeddedPicture

                if (picture != null && picture.isNotEmpty()) {
                    val coversDir = File(context.cacheDir, "album_covers")
                    if (!coversDir.exists()) coversDir.mkdirs()
                    val hash = Math.abs(filePath.hashCode()).toString()
                    val artFile = File(coversDir, "art_${hash}.jpg")
                    artFile.writeBytes(picture)
                    val artPath = artFile.absolutePath
                    memoryCache[filePath] = artPath
                    return@withContext artPath
                }
            } catch (t: Throwable) {
                Log.w(TAG, "Failed to extract embedded art for $filePath: ${t.message}")
            } finally {
                try {
                    retriever?.release()
                } catch (ignored: Throwable) {
                    // Ignore release errors
                }
            }
        }

        null
    }

    /**
     * Synchronous fast-check for folder companion image without launching coroutines.
     */
    fun findCompanionCoverArt(file: File): String? {
        val parentDir = file.parentFile ?: return null
        if (!parentDir.exists() || !parentDir.canRead()) return null

        for (name in COMMON_COVER_NAMES) {
            val candidate = File(parentDir, name)
            if (candidate.exists() && candidate.isFile && candidate.length() > 0L) {
                return candidate.absolutePath
            }
        }

        val imageFiles = parentDir.listFiles { f ->
            f.isFile && f.length() > 0L && (
                f.extension.equals("jpg", ignoreCase = true) ||
                f.extension.equals("jpeg", ignoreCase = true) ||
                f.extension.equals("png", ignoreCase = true) ||
                f.extension.equals("webp", ignoreCase = true)
            )
        }
        if (!imageFiles.isNullOrEmpty()) {
            return imageFiles[0].absolutePath
        }

        return null
    }
}
