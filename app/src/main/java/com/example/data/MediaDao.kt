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

data class PlaylistItemCount(
    val playlistId: Long,
    val count: Int
)

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM custom_playlists WHERE isVideo = :isVideo ORDER BY createdAt DESC")
    fun getPlaylistsFlow(isVideo: Boolean): Flow<List<CustomPlaylistEntity>>

    @Query("SELECT * FROM custom_playlists WHERE id = :playlistId LIMIT 1")
    suspend fun getPlaylistById(playlistId: Long): CustomPlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: CustomPlaylistEntity): Long

    @Query("DELETE FROM custom_playlists WHERE id = :playlistId")
    suspend fun deletePlaylistById(playlistId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addPlaylistItem(item: PlaylistItemEntity)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId AND filePath = :filePath")
    suspend fun removePlaylistItem(playlistId: Long, filePath: String)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId")
    suspend fun deleteItemsForPlaylist(playlistId: Long)

    @Query("DELETE FROM playlist_items WHERE filePath = :filePath")
    suspend fun deleteItemsByFilePath(filePath: String)

    @Query("""
        SELECT m.* FROM media_items m
        INNER JOIN playlist_items p ON m.filePath = p.filePath
        WHERE p.playlistId = :playlistId
        ORDER BY p.addedAt DESC
    """)
    fun getPlaylistMediaItemsFlow(playlistId: Long): Flow<List<MediaItemEntity>>

    @Query("SELECT playlistId, COUNT(*) as count FROM playlist_items GROUP BY playlistId")
    fun getPlaylistItemCountsFlow(): Flow<List<PlaylistItemCount>>
}

@Dao
interface VolumeDao {
    @Query("SELECT * FROM volumes")
    fun getAllVolumesFlow(): Flow<List<VolumeEntity>>

    @Query("SELECT * FROM volumes")
    suspend fun getAllVolumes(): List<VolumeEntity>

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
