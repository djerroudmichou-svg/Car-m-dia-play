package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_items")
data class MediaItemEntity(
    @PrimaryKey val filePath: String,
    val volumeId: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val duration: Long,
    val size: Long,
    val lastModified: Long,
    val isVideo: Boolean,
    val mimeType: String,
    val coverArtPath: String? = null
)

@Entity(tableName = "volumes")
data class VolumeEntity(
    @PrimaryKey val volumeId: String,
    val rootPath: String,
    val label: String,
    val lastScanned: Long,
    val isMounted: Boolean
)

@Entity(tableName = "custom_playlists")
data class CustomPlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isVideo: Boolean,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "playlist_items",
    primaryKeys = ["playlistId", "filePath"]
)
data class PlaylistItemEntity(
    val playlistId: Long,
    val filePath: String,
    val addedAt: Long = System.currentTimeMillis()
)
