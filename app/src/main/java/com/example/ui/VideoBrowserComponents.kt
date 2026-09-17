package com.example.ui

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CustomPlaylistEntity
import com.example.data.MediaItemEntity
import com.example.localization.LocalAppStrings
import com.example.theme.LocalCarColors
import com.example.viewmodel.FolderGroup
import com.example.viewmodel.VideoCategory
import com.example.viewmodel.VideoPlaylistType
import com.example.viewmodel.VideoVolumeGroup
import java.io.File

/**
 * Top category switcher for Video tab: All Videos, Folders, Playlists, Drives.
 * Supports horizontal touch scrolling left & right for compact / small car displays.
 */
@Composable
fun VideoCategorySelector(
    selectedCategory: VideoCategory,
    onCategorySelected: (VideoCategory) -> Unit,
    volumes: List<VideoVolumeGroup> = emptyList(),
    selectedVolumeId: String? = null,
    onVolumeSelected: (String) -> Unit = {},
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val scrollState = rememberScrollState()

    LaunchedEffect(selectedCategory, selectedVolumeId) {
        if (scrollState.maxValue > 0) {
            val categories = listOf(
                VideoCategory.ALL,
                VideoCategory.FOLDERS,
                VideoCategory.PLAYLISTS,
                VideoCategory.DRIVES
            )
            val index = categories.indexOf(selectedCategory).coerceAtLeast(0)
            val fraction = index.toFloat() / (categories.size - 1).coerceAtLeast(1)
            val target = (scrollState.maxValue * fraction).toInt()
            scrollState.animateScrollTo(target)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(if (isCompact) 6.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryChip(
            title = strings.allVideos,
            icon = Icons.Filled.VideoLibrary,
            isSelected = selectedCategory == VideoCategory.ALL && selectedVolumeId == null,
            onClick = { onCategorySelected(VideoCategory.ALL) },
            isCompact = isCompact
        )

        CategoryChip(
            title = strings.videoFolders,
            icon = Icons.Outlined.Folder,
            isSelected = selectedCategory == VideoCategory.FOLDERS,
            onClick = { onCategorySelected(VideoCategory.FOLDERS) },
            isCompact = isCompact
        )

        CategoryChip(
            title = strings.videoPlaylists,
            icon = Icons.Filled.PlaylistPlay,
            isSelected = selectedCategory == VideoCategory.PLAYLISTS,
            onClick = { onCategorySelected(VideoCategory.PLAYLISTS) },
            isCompact = isCompact
        )

        CategoryChip(
            title = strings.storageDrives,
            icon = Icons.Filled.Storage,
            isSelected = selectedCategory == VideoCategory.DRIVES && selectedVolumeId == null,
            onClick = { onCategorySelected(VideoCategory.DRIVES) },
            isCompact = isCompact
        )

        // Direct Storage / USB chips for Videos (Tablet, USB 1, USB 2, SD Card)
        volumes.forEach { drive ->
            val icon = when {
                drive.isInternal -> Icons.Filled.Storage
                drive.isSdCard -> Icons.Filled.SdCard
                else -> Icons.Filled.Usb
            }
            CategoryChip(
                title = drive.label,
                icon = icon,
                isSelected = (selectedCategory == VideoCategory.DRIVES && selectedVolumeId == drive.volumeId),
                onClick = {
                    onVolumeSelected(drive.volumeId)
                },
                isCompact = isCompact
            )
        }
    }
}

/**
 * Car-friendly search bar for videos and video folders.
 */
@Composable
fun VideoSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val colors = LocalCarColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(if (isCompact) 36.dp else 42.dp)
            .clip(RoundedCornerShape(if (isCompact) 10.dp else 12.dp))
            .background(colors.surfaceSecondary)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(if (isCompact) 10.dp else 12.dp))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(if (isCompact) 16.dp else 18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    text = strings.searchVideosPlaceholder,
                    color = colors.textSecondary,
                    fontSize = if (isCompact) 11.sp else 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                cursorColor = colors.accent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true,
            modifier = Modifier.weight(1f).testTag("video_search_input")
        )
        if (query.isNotEmpty()) {
            IconButton(
                onClick = { onQueryChange("") },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Clear",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * List of folders containing video files.
 */
@Composable
fun VideoFoldersListView(
    folders: List<FolderGroup>,
    onFolderSelected: (FolderGroup) -> Unit,
    isCompact: Boolean = false
) {
    val strings = LocalAppStrings.current
    val colors = LocalCarColors.current

    if (folders.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(strings.noVideosFound, color = colors.textSecondary, fontSize = 13.sp)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(folders, key = { it.folderPath }) { folder ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                        .clickable { onFolderSelected(folder) }
                        .padding(horizontal = 12.dp, vertical = if (isCompact) 8.dp else 10.dp)
                        .testTag("video_folder_${folder.folderName}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isCompact) 38.dp else 44.dp)
                            .background(colors.surfaceSecondary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Folder,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(if (isCompact) 20.dp else 24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = folder.folderName,
                            color = colors.textPrimary,
                            fontSize = if (isCompact) 13.sp else 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = String.format(strings.videoCountLabel, folder.trackCount),
                            color = colors.textSecondary,
                            fontSize = if (isCompact) 10.sp else 11.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Playlists list view: Custom user video playlists + smart playlists (Favorites, Short Clips, Movies & Episodes, Recently Added).
 */
@Composable
fun VideoPlaylistsListView(
    favoritesCount: Int,
    shortClipsCount: Int,
    moviesCount: Int,
    recentCount: Int,
    customPlaylists: List<CustomPlaylistEntity> = emptyList(),
    customPlaylistCounts: Map<Long, Int> = emptyMap(),
    onPlaylistSelected: (VideoPlaylistType) -> Unit,
    onCustomPlaylistSelected: (CustomPlaylistEntity) -> Unit = {},
    onCreatePlaylistClick: () -> Unit = {},
    onDeleteCustomPlaylist: (CustomPlaylistEntity) -> Unit = {},
    isCompact: Boolean = false
) {
    val strings = LocalAppStrings.current
    val colors = LocalCarColors.current

    val smartPlaylists = listOf(
        PlaylistCardItem(
            type = VideoPlaylistType.FAVORITES,
            title = strings.playlistFavorites,
            count = favoritesCount,
            icon = Icons.Filled.Star,
            iconColor = Color(0xFFFFB300),
            bgAlphaColor = Color(0xFFFFB300)
        ),
        PlaylistCardItem(
            type = VideoPlaylistType.SHORT_CLIPS,
            title = strings.playlistShortClips,
            count = shortClipsCount,
            icon = Icons.Filled.Bolt,
            iconColor = Color(0xFF00E5FF),
            bgAlphaColor = Color(0xFF00E5FF)
        ),
        PlaylistCardItem(
            type = VideoPlaylistType.MOVIES,
            title = strings.playlistMovies,
            count = moviesCount,
            icon = Icons.Filled.Movie,
            iconColor = Color(0xFFFF5252),
            bgAlphaColor = Color(0xFFFF5252)
        ),
        PlaylistCardItem(
            type = VideoPlaylistType.RECENT,
            title = strings.playlistRecent,
            count = recentCount,
            icon = Icons.Filled.History,
            iconColor = Color(0xFF00E676),
            bgAlphaColor = Color(0xFF00E676)
        )
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Create New Video Playlist banner
        item(key = "create_video_playlist_banner") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.accent.copy(alpha = 0.12f))
                    .border(1.5.dp, colors.accent.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .clickable(onClick = onCreatePlaylistClick)
                    .padding(horizontal = 14.dp, vertical = if (isCompact) 10.dp else 12.dp)
                    .testTag("banner_create_video_playlist"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isCompact) 38.dp else 44.dp)
                        .background(colors.accent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = colors.onAccent,
                        modifier = Modifier.size(if (isCompact) 22.dp else 26.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.createPlaylist,
                        color = colors.accent,
                        fontSize = if (isCompact) 13.sp else 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "إنشاء قائمة فيديو مخصصة",
                        color = colors.textSecondary,
                        fontSize = if (isCompact) 10.sp else 11.sp
                    )
                }
            }
        }

        // Custom Playlists (if any)
        if (customPlaylists.isNotEmpty()) {
            item(key = "custom_video_playlists_header") {
                Text(
                    text = strings.customPlaylists,
                    color = colors.accent,
                    fontSize = if (isCompact) 12.sp else 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                )
            }

            items(customPlaylists, key = { "custom_${it.id}" }) { playlist ->
                val count = customPlaylistCounts[playlist.id] ?: 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(14.dp))
                        .clickable { onCustomPlaylistSelected(playlist) }
                        .padding(horizontal = 14.dp, vertical = if (isCompact) 10.dp else 12.dp)
                        .testTag("custom_video_playlist_${playlist.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isCompact) 42.dp else 46.dp)
                            .background(colors.accent.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VideoLibrary,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(if (isCompact) 22.dp else 24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = playlist.name,
                            color = colors.textPrimary,
                            fontSize = if (isCompact) 13.sp else 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = String.format(strings.videoCountLabel, count),
                            color = colors.textSecondary,
                            fontSize = if (isCompact) 10.sp else 11.sp
                        )
                    }

                    IconButton(
                        onClick = { onDeleteCustomPlaylist(playlist) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete Playlist",
                            tint = colors.textSecondary.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Smart Playlists Section Header
        item(key = "smart_playlists_header") {
            Text(
                text = "قوائم التشغيل الذكية",
                color = colors.accent,
                fontSize = if (isCompact) 12.sp else 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )
        }

        items(smartPlaylists, key = { it.type.name }) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(14.dp))
                    .clickable { onPlaylistSelected(item.type) }
                    .padding(horizontal = 14.dp, vertical = if (isCompact) 10.dp else 12.dp)
                    .testTag("video_playlist_${item.type.name}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isCompact) 42.dp else 48.dp)
                        .background(item.bgAlphaColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = item.iconColor,
                        modifier = Modifier.size(if (isCompact) 22.dp else 26.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        color = colors.textPrimary,
                        fontSize = if (isCompact) 13.sp else 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = String.format(strings.videoCountLabel, item.count),
                        color = colors.textSecondary,
                        fontSize = if (isCompact) 10.sp else 11.sp
                    )
                }

                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private data class PlaylistCardItem(
    val type: VideoPlaylistType,
    val title: String,
    val count: Int,
    val icon: ImageVector,
    val iconColor: Color,
    val bgAlphaColor: Color
)

/**
 * Storage drives / USB volumes view for videos.
 */
@Composable
fun VideoDrivesListView(
    drives: List<VideoVolumeGroup>,
    onDriveSelected: (VideoVolumeGroup) -> Unit,
    isCompact: Boolean = false
) {
    val strings = LocalAppStrings.current
    val colors = LocalCarColors.current

    if (drives.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Usb,
                    contentDescription = null,
                    tint = colors.textSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(if (isCompact) 36.dp else 48.dp)
                )
                Text(
                    text = strings.noUsbFound,
                    color = colors.textSecondary,
                    fontSize = if (isCompact) 12.sp else 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(if (isCompact) 6.dp else 8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(drives, key = { it.volumeId }) { drive ->
                val icon = when {
                    drive.isInternal -> Icons.Filled.Storage
                    drive.isSdCard -> Icons.Filled.SdCard
                    else -> Icons.Filled.Usb
                }

                val iconColor = when {
                    drive.isInternal -> Color(0xFF42A5F5)
                    drive.isSdCard -> Color(0xFFFFA726)
                    else -> colors.accent
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(if (isCompact) 10.dp else 12.dp))
                        .background(colors.surfaceSecondary)
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(if (isCompact) 10.dp else 12.dp))
                        .clickable { onDriveSelected(drive) }
                        .padding(
                            horizontal = if (isCompact) 10.dp else 14.dp,
                            vertical = if (isCompact) 8.dp else 12.dp
                        )
                        .testTag("video_drive_${drive.volumeId}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isCompact) 36.dp else 44.dp)
                            .clip(CircleShape)
                            .background(iconColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(if (isCompact) 20.dp else 24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = drive.label,
                            color = colors.textPrimary,
                            fontSize = if (isCompact) 13.sp else 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (drive.rootPath.isNotBlank()) {
                            Text(
                                text = drive.rootPath,
                                color = colors.textSecondary.copy(alpha = 0.7f),
                                fontSize = if (isCompact) 10.sp else 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Count badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.cardBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = String.format(strings.videoCountLabel, drive.videoCount),
                            color = colors.accent,
                            fontSize = if (isCompact) 10.sp else 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = colors.textSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(if (isCompact) 12.dp else 14.dp)
                    )
                }
            }
        }
    }
}

/**
 * Video list view item with favorite star button, car-sized touch targets,
 * duration display, and active playing indicator.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoItemsListView(
    videos: List<MediaItemEntity>,
    currentPlaying: MediaItemEntity?,
    favoritePaths: Set<String>,
    selectedIds: Set<String> = emptySet(),
    isSelectionMode: Boolean = false,
    onVideoClick: (MediaItemEntity) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onVideoLongClick: ((MediaItemEntity) -> Unit)? = null,
    onToggleSelection: ((MediaItemEntity) -> Unit)? = null,
    isCompact: Boolean = false
) {
    val strings = LocalAppStrings.current
    val colors = LocalCarColors.current

    if (videos.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(strings.noVideosFound, color = colors.textSecondary, fontSize = 13.sp)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(videos, key = { it.filePath }) { video ->
                val isSelected = currentPlaying?.filePath == video.filePath
                val isMultiSelected = selectedIds.contains(video.filePath)
                val isFav = favoritePaths.contains(video.filePath)
                val folderName = File(video.filePath).parentFile?.name ?: ""

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                isMultiSelected -> colors.accent.copy(alpha = 0.2f)
                                isSelected -> colors.surfaceSecondary
                                else -> colors.surface
                            }
                        )
                        .border(
                            width = when {
                                isMultiSelected -> 2.dp
                                isSelected -> 1.5.dp
                                else -> 1.dp
                            },
                            color = when {
                                isMultiSelected -> colors.accent
                                isSelected -> colors.accent.copy(alpha = 0.7f)
                                else -> colors.cardBorder
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        .combinedClickable(
                            onClick = { 
                                if (isSelectionMode && onToggleSelection != null) {
                                    onToggleSelection(video)
                                } else {
                                    onVideoClick(video)
                                }
                            },
                            onLongClick = {
                                if (onToggleSelection != null) {
                                    onToggleSelection(video)
                                } else {
                                    onVideoLongClick?.invoke(video)
                                }
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = if (isCompact) 6.dp else 8.dp)
                        .testTag("video_item_${video.filePath.hashCode()}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Selection Indicator
                    if (isSelectionMode) {
                        Icon(
                            imageVector = if (isMultiSelected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = "Selected",
                            tint = if (isMultiSelected) colors.accent else colors.textSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Video icon indicator
                    Box(
                        modifier = Modifier
                            .size(if (isCompact) 36.dp else 42.dp)
                            .background(
                                if (isSelected) colors.accent.copy(alpha = 0.2f) else colors.surfaceSecondary,
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) Icons.Filled.PlayArrow else Icons.Filled.Videocam,
                            contentDescription = null,
                            tint = if (isSelected) colors.accent else colors.textSecondary,
                            modifier = Modifier.size(if (isCompact) 20.dp else 24.dp)
                        )
                    }

                    // Video Info: Title + (Folder • Duration)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = video.title,
                            color = if (isSelected) colors.accent else colors.textPrimary,
                            fontSize = if (isCompact) 13.sp else 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = DateUtils.formatElapsedTime(video.duration / 1000),
                                color = colors.textSecondary,
                                fontSize = if (isCompact) 10.sp else 11.sp
                            )
                            if (folderName.isNotEmpty()) {
                                Text(
                                    text = "• $folderName",
                                    color = colors.textSecondary.copy(alpha = 0.8f),
                                    fontSize = if (isCompact) 10.sp else 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Favorite Toggle Star Button
                    IconButton(
                        onClick = { onToggleFavorite(video.filePath) },
                        modifier = Modifier
                            .size(if (isCompact) 32.dp else 36.dp)
                            .testTag("fav_video_${video.filePath.hashCode()}")
                    ) {
                        Icon(
                            imageVector = if (isFav) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = if (isFav) strings.removeFromFavorites else strings.addToFavorites,
                            tint = if (isFav) Color(0xFFFFB300) else colors.textSecondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(if (isCompact) 18.dp else 22.dp)
                        )
                    }

                    // Options / More Button for car convenience
                    if (onVideoLongClick != null) {
                        IconButton(
                            onClick = { onVideoLongClick(video) },
                            modifier = Modifier.size(if (isCompact) 32.dp else 36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Video options",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
