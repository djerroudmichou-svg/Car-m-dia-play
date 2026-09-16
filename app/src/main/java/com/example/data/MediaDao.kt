package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM media_items WHERE isVideo = 0 AND volumeId IN (SELECT volumeId FROM volumes WHERE isMounted = 1) ORDER BY title ASC")
    fun getMountedMusicFlow(): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE isVideo = 1 AND volumeId IN (SELECT volumeId FROM volumes WHERE isMounted = 1) ORDER BY title ASC")
    fun getMountedVideosFlow(): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE volumeId = :volumeId")
    suspend fun getMediaForVolume(volumeId: String): List<MediaItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaItems(items: List<MediaItemEntity>)

    @Delete
    suspend fun deleteMediaItems(items: List<MediaItemEntity>)

    @Query("DELETE FROM media_items WHERE filePath = :filePath")
    suspend fun deleteByPath(filePath: String)

    @Query("DELETE FROM media_items WHERE volumeId = :volumeId")
    suspend fun deleteMediaForVolume(volumeId: String)
}

@Dao
interface VolumeDao {
    @Query("SELECT * FROM volumes")
    fun getAllVolumesFlow(): Flow<List<VolumeEntity>>

    @Query("SELECT * FROM volumes WHERE volumeId = :volumeId LIMIT 1")
    suspend fun getVolumeById(volumeId: String): VolumeEntity?

    @Query("SELECT * FROM volumes WHERE rootPath = :rootPath LIMIT 1")
    suspend fun getVolumeByRootPath(rootPath: String): VolumeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVolume(volume: VolumeEntity)

    @Query("UPDATE volumes SET isMounted = :isMounted WHERE volumeId = :volumeId")
    suspend fun updateMountStatus(volumeId: String, isMounted: Boolean)

    @Query("UPDATE volumes SET isMounted = 0")
    suspend fun unmountAllVolumes()
}
