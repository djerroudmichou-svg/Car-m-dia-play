package com.example.data

import kotlinx.coroutines.flow.Flow

class MediaRepository(private val database: MediaDatabase) {
    val mediaDao = database.mediaDao()
    val volumeDao = database.volumeDao()

    val mountedMusic: Flow<List<MediaItemEntity>> = mediaDao.getMountedMusicFlow()
    val mountedVideos: Flow<List<MediaItemEntity>> = mediaDao.getMountedVideosFlow()
    val allVolumes: Flow<List<VolumeEntity>> = volumeDao.getAllVolumesFlow()

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
    }

    suspend fun clearMediaForVolume(volumeId: String) {
        mediaDao.deleteMediaForVolume(volumeId)
    }

    suspend fun getMediaForVolume(volumeId: String): List<MediaItemEntity> {
        return mediaDao.getMediaForVolume(volumeId)
    }
}
