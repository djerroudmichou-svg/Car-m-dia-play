package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MediaRepository(private val database: MediaDatabase) {
    val mediaDao = database.mediaDao()
    val volumeDao = database.volumeDao()
    val playlistDao = database.playlistDao()

    val mountedMusic: Flow<List<MediaItemEntity>> = mediaDao.getMountedMusicFlow()
    val mountedVideos: Flow<List<MediaItemEntity>> = mediaDao.getMountedVideosFlow()
    val allVolumes: Flow<List<VolumeEntity>> = volumeDao.getAllVolumesFlow()
    suspend fun getAllVolumes(): List<VolumeEntity> = volumeDao.getAllVolumes()

    suspend fun getVolumeById(volumeId: String): VolumeEntity? = volumeDao.getVolumeById(volumeId)

    suspend fun getVolumeByRootPath(rootPath: String): VolumeEntity? = volumeDao.getVolumeByRootPath(rootPath)

    suspend fun insertVolume(volume: VolumeEntity) = volumeDao.insertVolume(volume)

    suspend fun updateMountStatus(volumeId: String, isMounted: Boolean) {
        volumeDao.updateMountStatus(volumeId, isMounted)
    }

    suspend fun unmountAllVolumes() {
        volumeDao.unmountAllVolumes()
    }

    suspend fun insertMediaItems(items: List<MediaItemEntity>) {
        mediaDao.insertMediaItems(items)
    }

    suspend fun deleteMediaItems(items: List<MediaItemEntity>) {
        mediaDao.deleteMediaItems(items)
    }

    suspend fun deleteByPath(filePath: String) {
        mediaDao.deleteByPath(filePath)
        playlistDao.deleteItemsByFilePath(filePath)
    }

    suspend fun deleteMediaItemPermanently(filePath: String): Boolean {
        mediaDao.deleteByPath(filePath)
        playlistDao.deleteItemsByFilePath(filePath)
        return try {
            val file = java.io.File(filePath)
            if (file.exists() && file.canWrite()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun clearMediaForVolume(volumeId: String) {
        mediaDao.deleteMediaForVolume(volumeId)
    }

    suspend fun getMediaForVolume(volumeId: String): List<MediaItemEntity> {
        return mediaDao.getMediaForVolume(volumeId)
    }

    // Custom Playlists Operations
    fun getCustomPlaylistsFlow(isVideo: Boolean): Flow<List<CustomPlaylistEntity>> {
        return playlistDao.getPlaylistsFlow(isVideo)
    }

    suspend fun createCustomPlaylist(name: String, isVideo: Boolean): Long {
        return playlistDao.insertPlaylist(
            CustomPlaylistEntity(name = name.trim(), isVideo = isVideo)
        )
    }

    suspend fun deleteCustomPlaylist(playlistId: Long) {
        playlistDao.deleteItemsForPlaylist(playlistId)
        playlistDao.deletePlaylistById(playlistId)
    }

    suspend fun addItemToPlaylist(playlistId: Long, filePath: String) {
        playlistDao.addPlaylistItem(
            PlaylistItemEntity(playlistId = playlistId, filePath = filePath)
        )
    }

    suspend fun removeItemFromPlaylist(playlistId: Long, filePath: String) {
        playlistDao.removePlaylistItem(playlistId, filePath)
    }

    fun getPlaylistMediaItemsFlow(playlistId: Long): Flow<List<MediaItemEntity>> {
        return playlistDao.getPlaylistMediaItemsFlow(playlistId)
    }

    fun getPlaylistItemCountsFlow(): Flow<Map<Long, Int>> {
        return playlistDao.getPlaylistItemCountsFlow().map { list ->
            list.associate { it.playlistId to it.count }
        }
    }
}
