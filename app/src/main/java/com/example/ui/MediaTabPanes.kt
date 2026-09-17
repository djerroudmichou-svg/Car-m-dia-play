package com.example.ui

import android.text.format.DateUtils
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.LayoutDirection
import androidx.media3.common.Player
import com.example.data.CustomPlaylistEntity
import com.example.data.MediaItemEntity
import com.example.localization.LocalAppStrings
import com.example.theme.LocalCarColors
import com.example.viewmodel.AlbumGroup
import com.example.viewmodel.ArtistGroup
import com.example.viewmodel.FolderGroup
import com.example.viewmodel.MusicCategory
import com.example.viewmodel.MusicVolumeGroup
import com.example.viewmodel.VideoCategory
import com.example.viewmodel.VideoPlaylistType
import com.example.viewmodel.VideoVolumeGroup
import java.io.File

/**
 * High-visibility docked mini player bar for small or narrow car screens.
 * Provides clear tactile controls and opens the fullscreen player on tap.
 */
@Composable
fun CompactAudioMiniBar(
    currentPlaying: MediaItemEntity,
    isPlaying: Boolean,
    playbackProgress: Long,
    playbackDuration: Long,
    onPlayPauseToggle: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onExpandFullscreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val colors = LocalCarColors.current

    var totalDragX by remember { mutableFloatStateOf(0f) }
    val animatedDragX by animateFloatAsState(
        targetValue = totalDragX,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "mini_bar_drag"
    )

    val swipeModifier = Modifier.pointerInput(Unit) {
        detectHorizontalDragGestures(
            onDragStart = { totalDragX = 0f },
            onHorizontalDrag = { _, dragAmount -> totalDragX += dragAmount },
            onDragEnd = {
                val threshold = 50.dp.toPx()
                if (kotlin.math.abs(totalDragX) > threshold) {
                    onExpandFullscreen()
                }
                totalDragX = 0f
            },
            onDragCancel = { totalDragX = 0f }
        )
    }

    Column(
        modifier = modifier
            .offset(x = animatedDragX.dp / 8) // Subtle visual feedback
            .background(colors.surface, RoundedCornerShape(12.dp))
            .border(1.dp, colors.accent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .then(swipeModifier)
            .clickable(onClick = onExpandFullscreen)
    ) {
        val progressFraction = if (playbackDuration > 0) {
            (playbackProgress.toFloat() / playbackDuration.toFloat()).coerceIn(0f, 1f)
        } else 0f

        LinearProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
            color = colors.accent,
            trackColor = colors.surfaceSecondary
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Album art
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                CoverArtImage(
                    coverArtPath = currentPlaying.coverArtPath,
                    title = currentPlaying.title,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Title & Artist
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentPlaying.title,
                    color = colors.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = currentPlaying.artist ?: strings.unknownArtist,
                    color = colors.textSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Big car touch controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = "Previous",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.accent)
                        .clickable(onClick = onPlayPauseToggle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = colors.onAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = onNext,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Next",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = onExpandFullscreen,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Fullscreen,
                        contentDescription = "Expand",
                        tint = colors.accent,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

/**
 * Left pane for Music Tab: Categories, search field, and track/folder/album/artist/drive lists.
 */
@Composable
fun MusicBrowserPane(
    currentCategory: MusicCategory,
    onCategorySelected: (MusicCategory) -> Unit,
    volumes: List<MusicVolumeGroup>,
    selectedVolumeId: String?,
    onVolumeSelected: (String) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedArtist: String?,
    onSelectArtist: (String?) -> Unit,
    selectedAlbum: String?,
    onSelectAlbum: (String?) -> Unit,
    selectedFolder: String?,
    onSelectFolder: (String?) -> Unit,
    selectedCustomPlaylist: CustomPlaylistEntity? = null,
    onSelectCustomPlaylist: (CustomPlaylistEntity?) -> Unit = {},
    customPlaylists: List<CustomPlaylistEntity> = emptyList(),
    customPlaylistCounts: Map<Long, Int> = emptyMap(),
    customPlaylistTracks: List<MediaItemEntity> = emptyList(),
    onCreatePlaylistClick: () -> Unit = {},
    onDeleteCustomPlaylist: (CustomPlaylistEntity) -> Unit = {},
    filteredTracks: List<MediaItemEntity>,
    currentPlaying: MediaItemEntity?,
    onPlayTrack: (MediaItemEntity, List<MediaItemEntity>) -> Unit,
    onTrackLongClick: ((MediaItemEntity) -> Unit)? = null,
    selectedIds: Set<String> = emptySet(),
    isSelectionMode: Boolean = false,
    onToggleSelection: (MediaItemEntity) -> Unit = {},
    onClearSelection: () -> Unit = {},
    onBulkDelete: () -> Unit = {},
    onBulkAddToPlaylist: () -> Unit = {},
    onBulkRemoveFromPlaylist: (Long) -> Unit = {},
    artistsList: List<ArtistGroup>,
    albumsList: List<AlbumGroup>,
    foldersList: List<FolderGroup>,
    isCompact: Boolean,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val colors = LocalCarColors.current

    Column(
        modifier = modifier
            .background(colors.surface, RoundedCornerShape(16.dp))
            .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
            .padding(if (isCompact) 8.dp else 12.dp)
    ) {
        if (isSelectionMode) {
            SelectionActionToolbar(
                selectedCount = selectedIds.size,
                onClearSelection = onClearSelection,
                onAddToPlaylist = onBulkAddToPlaylist,
                onDelete = onBulkDelete,
                onRemoveFromPlaylist = if (selectedCustomPlaylist != null) {
                    { onBulkRemoveFromPlaylist(selectedCustomPlaylist.id) }
                } else null,
                isCompact = isCompact
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        MusicCategorySelector(
            selectedCategory = currentCategory,
            onCategorySelected = {
                onCategorySelected(it)
                onSelectArtist(null)
                onSelectAlbum(null)
                onSelectFolder(null)
                onSelectCustomPlaylist(null)
            },
            volumes = volumes,
            selectedVolumeId = selectedVolumeId,
            onVolumeSelected = onVolumeSelected,
            isCompact = isCompact
        )

        Spacer(modifier = Modifier.height(if (isCompact) 4.dp else 8.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isCompact) 44.dp else 50.dp)
                .testTag("search_field"),
            placeholder = { Text(strings.searchPlaceholder, color = colors.textSecondary, fontSize = if (isCompact) 11.sp else 12.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.accent,
                unfocusedBorderColor = colors.cardBorder,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                focusedContainerColor = colors.surfaceSecondary,
                unfocusedContainerColor = colors.surfaceSecondary
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        var totalDragX by remember { mutableFloatStateOf(0f) }
        val musicCategories = remember {
            listOf(
                MusicCategory.ALL,
                MusicCategory.ARTISTS,
                MusicCategory.ALBUMS,
                MusicCategory.FOLDERS,
                MusicCategory.PLAYLISTS,
                MusicCategory.DRIVES
            )
        }

        val categorySwipeModifier = Modifier.pointerInput(
            currentCategory,
            selectedArtist,
            selectedAlbum,
            selectedFolder,
            selectedCustomPlaylist,
            selectedVolumeId,
            isRtl
        ) {
            detectHorizontalDragGestures(
                onDragStart = { totalDragX = 0f },
                onHorizontalDrag = { _, dragAmount ->
                    totalDragX += dragAmount
                },
                onDragEnd = {
                    val swipeThreshold = 50.dp.toPx()
                    val isNext = if (isRtl) totalDragX > swipeThreshold else totalDragX < -swipeThreshold
                    val isPrev = if (isRtl) totalDragX < -swipeThreshold else totalDragX > swipeThreshold

                    if (isNext) {
                        onSelectArtist(null)
                        onSelectAlbum(null)
                        onSelectFolder(null)
                        onSelectCustomPlaylist(null)
                        if (selectedVolumeId != null) onVolumeSelected("")
                        val currentIndex = musicCategories.indexOf(currentCategory)
                        if (currentIndex in 0 until musicCategories.size - 1) {
                            onCategorySelected(musicCategories[currentIndex + 1])
                        }
                    } else if (isPrev) {
                        if (selectedArtist != null) {
                            onSelectArtist(null)
                        } else if (selectedAlbum != null) {
                            onSelectAlbum(null)
                        } else if (selectedFolder != null) {
                            onSelectFolder(null)
                        } else if (selectedCustomPlaylist != null) {
                            onSelectCustomPlaylist(null)
                        } else if (selectedVolumeId != null) {
                            onVolumeSelected("")
                        } else {
                            val currentIndex = musicCategories.indexOf(currentCategory)
                            if (currentIndex > 0) {
                                onCategorySelected(musicCategories[currentIndex - 1])
                            }
                        }
                    }
                }
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .then(categorySwipeModifier)
        ) {
            when (currentCategory) {
            MusicCategory.ARTISTS -> {
                if (selectedArtist != null) {
                    SubListHeader(
                        title = selectedArtist,
                        subtitle = strings.backToArtists,
                        onBack = { onSelectArtist(null) }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TracksListView(
                        tracks = filteredTracks,
                        currentPlaying = currentPlaying,
                        isPlaying = currentPlaying != null,
                        selectedIds = selectedIds,
                        isSelectionMode = isSelectionMode,
                        onTrackClick = { onPlayTrack(it, filteredTracks) },
                        onTrackLongClick = onTrackLongClick,
                        onToggleSelection = onToggleSelection
                    )
                } else {
                    ArtistsListView(
                        artists = artistsList,
                        onArtistSelected = { onSelectArtist(it.name) }
                    )
                }
            }
            MusicCategory.ALBUMS -> {
                if (selectedAlbum != null) {
                    SubListHeader(
                        title = selectedAlbum,
                        subtitle = strings.backToAlbums,
                        onBack = { onSelectAlbum(null) }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TracksListView(
                        tracks = filteredTracks,
                        currentPlaying = currentPlaying,
                        isPlaying = currentPlaying != null,
                        selectedIds = selectedIds,
                        isSelectionMode = isSelectionMode,
                        onTrackClick = { onPlayTrack(it, filteredTracks) },
                        onTrackLongClick = onTrackLongClick,
                        onToggleSelection = onToggleSelection
                    )
                } else {
                    AlbumsListView(
                        albums = albumsList,
                        onAlbumSelected = { onSelectAlbum(it.title) }
                    )
                }
            }
            MusicCategory.FOLDERS -> {
                if (selectedFolder != null) {
                    SubListHeader(
                        title = selectedFolder,
                        subtitle = strings.backToFolders,
                        onBack = { onSelectFolder(null) }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TracksListView(
                        tracks = filteredTracks,
                        currentPlaying = currentPlaying,
                        isPlaying = currentPlaying != null,
                        selectedIds = selectedIds,
                        isSelectionMode = isSelectionMode,
                        onTrackClick = { onPlayTrack(it, filteredTracks) },
                        onTrackLongClick = onTrackLongClick,
                        onToggleSelection = onToggleSelection
                    )
                } else {
                    FoldersListView(
                        folders = foldersList,
                        onFolderSelected = { onSelectFolder(it.folderName) }
                    )
                }
            }
            MusicCategory.ALL -> {
                TracksListView(
                    tracks = filteredTracks,
                    currentPlaying = currentPlaying,
                    isPlaying = currentPlaying != null,
                    selectedIds = selectedIds,
                    isSelectionMode = isSelectionMode,
                    onTrackClick = { onPlayTrack(it, filteredTracks) },
                    onTrackLongClick = onTrackLongClick,
                    onToggleSelection = onToggleSelection
                )
            }
            MusicCategory.PLAYLISTS -> {
                if (selectedCustomPlaylist != null) {
                    SubListHeader(
                        title = selectedCustomPlaylist.name,
                        subtitle = strings.backToMusicPlaylists,
                        onBack = { onSelectCustomPlaylist(null) }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TracksListView(
                        tracks = customPlaylistTracks,
                        currentPlaying = currentPlaying,
                        isPlaying = currentPlaying != null,
                        selectedIds = selectedIds,
                        isSelectionMode = isSelectionMode,
                        onTrackClick = { onPlayTrack(it, customPlaylistTracks) },
                        onTrackLongClick = onTrackLongClick,
                        onToggleSelection = onToggleSelection
                    )
                } else {
                    CustomPlaylistsListView(
                        playlists = customPlaylists,
                        playlistCounts = customPlaylistCounts,
                        isVideo = false,
                        onPlaylistClick = { onSelectCustomPlaylist(it) },
                        onCreatePlaylistClick = onCreatePlaylistClick,
                        onDeletePlaylistClick = onDeleteCustomPlaylist,
                        isCompact = isCompact
                    )
                }
            }
            MusicCategory.DRIVES -> {
                if (selectedVolumeId != null) {
                    val currentDrive = volumes.find { it.volumeId == selectedVolumeId }
                    val driveTitle = currentDrive?.label ?: strings.storageDrives
                    SubListHeader(
                        title = driveTitle,
                        subtitle = strings.backToStorageDrives,
                        onBack = { onVolumeSelected(selectedVolumeId) }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TracksListView(
                        tracks = filteredTracks,
                        currentPlaying = currentPlaying,
                        isPlaying = currentPlaying != null,
                        selectedIds = selectedIds,
                        isSelectionMode = isSelectionMode,
                        onTrackClick = { onPlayTrack(it, filteredTracks) },
                        onTrackLongClick = onTrackLongClick,
                        onToggleSelection = onToggleSelection
                    )
                } else {
                    MusicDrivesListView(
                        drives = volumes,
                        onDriveSelected = { onVolumeSelected(it.volumeId) },
                        isCompact = isCompact
                    )
                }
            }
        }
        }
    }
}

/**
 * Right pane for Music Tab: Now Playing card with adaptive touch buttons.
 */
@Composable
fun MusicNowPlayingPane(
    currentPlaying: MediaItemEntity?,
    isPlaying: Boolean,
    playbackProgress: Long,
    playbackDuration: Long,
    repeatMode: Int,
    isShuffleEnabled: Boolean,
    isFastForwarding: Boolean,
    isRewinding: Boolean,
    onPlayPauseToggle: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onFastForwardStart: () -> Unit,
    onFastForwardEnd: () -> Unit,
    onRewindStart: () -> Unit,
    onRewindEnd: () -> Unit,
    onSeek: (Float) -> Unit,
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    onFullscreenExpand: () -> Unit,
    isCompact: Boolean,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val colors = LocalCarColors.current

    var totalDragX by remember { mutableFloatStateOf(0f) }
    val animatedDragX by animateFloatAsState(
        targetValue = totalDragX,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "pane_drag"
    )

    val swipeModifier = Modifier.pointerInput(Unit) {
        detectHorizontalDragGestures(
            onDragStart = { totalDragX = 0f },
            onHorizontalDrag = { _, dragAmount -> totalDragX += dragAmount },
            onDragEnd = {
                val threshold = 50.dp.toPx()
                if (kotlin.math.abs(totalDragX) > threshold) {
                    onFullscreenExpand()
                }
                totalDragX = 0f
            },
            onDragCancel = { totalDragX = 0f }
        )
    }

    Column(
        modifier = modifier
            .offset(x = animatedDragX.dp / 8) // Subtle visual feedback
            .background(colors.surface, RoundedCornerShape(16.dp))
            .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
            .then(swipeModifier)
            .padding(if (isCompact) 8.dp else 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.nowPlaying,
                color = colors.accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = onFullscreenExpand,
                modifier = Modifier
                    .size(if (isCompact) 32.dp else 38.dp)
                    .background(colors.surfaceSecondary, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Fullscreen,
                    contentDescription = strings.fullscreen,
                    tint = colors.textPrimary,
                    modifier = Modifier.size(if (isCompact) 18.dp else 22.dp)
                )
            }
        }

        // Cover Art
        Box(
            modifier = Modifier
                .sizeIn(
                    minWidth = if (isCompact) 48.dp else 70.dp,
                    maxWidth = if (isCompact) 85.dp else 115.dp,
                    minHeight = if (isCompact) 48.dp else 70.dp,
                    maxHeight = if (isCompact) 85.dp else 115.dp
                )
                .fillMaxHeight(if (isCompact) 0.30f else 0.36f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(if (isCompact) 12.dp else 16.dp))
                .border(2.dp, colors.accent.copy(alpha = 0.5f), RoundedCornerShape(if (isCompact) 12.dp else 16.dp))
                .clickable(onClick = onFullscreenExpand),
            contentAlignment = Alignment.Center
        ) {
            CoverArtImage(
                coverArtPath = currentPlaying?.coverArtPath,
                title = currentPlaying?.title ?: "Car Media",
                modifier = Modifier.fillMaxSize()
            )
        }

        // Track & Artist Info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = currentPlaying?.title ?: strings.selectMediaToPlay,
                color = colors.textPrimary,
                fontSize = if (isCompact) 13.sp else 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = currentPlaying?.artist ?: strings.unknownArtist,
                color = colors.textSecondary,
                fontSize = if (isCompact) 11.sp else 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }

        // Progress Slider
        Column(modifier = Modifier.fillMaxWidth()) {
            val progressFraction = if (playbackDuration > 0) {
                (playbackProgress.toFloat() / playbackDuration).coerceIn(0f, 1f)
            } else 0f

            Slider(
                value = progressFraction,
                onValueChange = onSeek,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = colors.accent,
                    activeTrackColor = colors.accent,
                    inactiveTrackColor = colors.cardBorder
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "\u200E${DateUtils.formatElapsedTime(playbackProgress / 1000)}\u200E",
                    color = colors.textSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = "\u200E${DateUtils.formatElapsedTime(playbackDuration / 1000)}\u200E",
                    color = colors.textSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isCompact) 48.dp else 58.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onShuffleToggle,
                modifier = Modifier.size(if (isCompact) 32.dp else 40.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (isShuffleEnabled) colors.accent else colors.textSecondary,
                    modifier = Modifier.size(if (isCompact) 18.dp else 22.dp)
                )
            }

            CarSeekButton(
                icon = Icons.Filled.SkipPrevious,
                contentDescription = strings.previous,
                modifier = Modifier.size(if (isCompact) 42.dp else 50.dp),
                iconSize = if (isCompact) 22.dp else 26.dp,
                isSeeking = isRewinding,
                onClick = onPrevious,
                onHoldStart = onRewindStart,
                onHoldEnd = onRewindEnd
            )

            IconButton(
                onClick = onPlayPauseToggle,
                modifier = Modifier
                    .size(if (isCompact) 48.dp else 58.dp)
                    .background(colors.accent, CircleShape)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) strings.pause else strings.play,
                    tint = colors.onAccent,
                    modifier = Modifier.size(if (isCompact) 26.dp else 32.dp)
                )
            }

            CarSeekButton(
                icon = Icons.Filled.SkipNext,
                contentDescription = strings.next,
                modifier = Modifier.size(if (isCompact) 42.dp else 50.dp),
                iconSize = if (isCompact) 22.dp else 26.dp,
                isSeeking = isFastForwarding,
                onClick = onNext,
                onHoldStart = onFastForwardStart,
                onHoldEnd = onFastForwardEnd
            )

            IconButton(
                onClick = onRepeatToggle,
                modifier = Modifier.size(if (isCompact) 32.dp else 40.dp)
            ) {
                Icon(
                    imageVector = if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                    contentDescription = "Repeat",
                    tint = if (repeatMode != Player.REPEAT_MODE_OFF) colors.accent else colors.textSecondary,
                    modifier = Modifier.size(if (isCompact) 18.dp else 22.dp)
                )
            }
        }
    }
}

/**
 * Browser pane for Video Tab: Categories, search, folders, playlists, drives, videos.
 */
@Composable
fun VideoBrowserPane(
    currentCategory: VideoCategory,
    onCategorySelected: (VideoCategory) -> Unit,
    drivesList: List<VideoVolumeGroup>,
    selectedVolume: String?,
    onVolumeSelected: (String?) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFolder: String?,
    onFolderSelected: (String?) -> Unit,
    selectedPlaylist: VideoPlaylistType?,
    onPlaylistSelected: (VideoPlaylistType?) -> Unit,
    selectedCustomPlaylist: CustomPlaylistEntity? = null,
    onSelectCustomPlaylist: (CustomPlaylistEntity?) -> Unit = {},
    customPlaylists: List<CustomPlaylistEntity> = emptyList(),
    customPlaylistCounts: Map<Long, Int> = emptyMap(),
    customPlaylistTracks: List<MediaItemEntity> = emptyList(),
    onCreatePlaylistClick: () -> Unit = {},
    onDeleteCustomPlaylist: (CustomPlaylistEntity) -> Unit = {},
    filteredVideos: List<MediaItemEntity>,
    currentPlaying: MediaItemEntity?,
    favoritePaths: Set<String>,
    selectedIds: Set<String> = emptySet(),
    isSelectionMode: Boolean = false,
    onToggleSelection: (MediaItemEntity) -> Unit = {},
    onClearSelection: () -> Unit = {},
    onBulkDelete: () -> Unit = {},
    onBulkAddToPlaylist: () -> Unit = {},
    onBulkRemoveFromPlaylist: (Long) -> Unit = {},
    onPlayVideo: (MediaItemEntity, List<MediaItemEntity>) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onVideoLongClick: ((MediaItemEntity) -> Unit)? = null,
    foldersList: List<FolderGroup>,
    totalVideosCount: Int,
    favoritesCount: Int,
    shortClipsCount: Int,
    moviesCount: Int,
    isCompact: Boolean,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val colors = LocalCarColors.current

    Column(
        modifier = modifier
            .background(colors.surface, RoundedCornerShape(16.dp))
            .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
            .padding(if (isCompact) 8.dp else 12.dp)
    ) {
        if (isSelectionMode) {
            SelectionActionToolbar(
                selectedCount = selectedIds.size,
                onClearSelection = onClearSelection,
                onAddToPlaylist = onBulkAddToPlaylist,
                onDelete = onBulkDelete,
                onRemoveFromPlaylist = if (selectedCustomPlaylist != null) {
                    { onBulkRemoveFromPlaylist(selectedCustomPlaylist.id) }
                } else null,
                isCompact = isCompact
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        VideoCategorySelector(
            selectedCategory = currentCategory,
            onCategorySelected = {
                onCategorySelected(it)
                onFolderSelected(null)
                onPlaylistSelected(null)
                onVolumeSelected(null)
                onSelectCustomPlaylist(null)
            },
            volumes = drivesList,
            selectedVolumeId = selectedVolume,
            onVolumeSelected = { volumeId ->
                onCategorySelected(VideoCategory.DRIVES)
                onFolderSelected(null)
                onPlaylistSelected(null)
                onVolumeSelected(volumeId)
                onSelectCustomPlaylist(null)
            },
            isCompact = isCompact
        )

        Spacer(modifier = Modifier.height(if (isCompact) 6.dp else 8.dp))

        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        var videoDragX by remember { mutableFloatStateOf(0f) }
        val videoCategories = remember {
            listOf(
                VideoCategory.ALL,
                VideoCategory.FOLDERS,
                VideoCategory.PLAYLISTS,
                VideoCategory.DRIVES
            )
        }

        val videoCategorySwipeModifier = Modifier.pointerInput(
            currentCategory,
            selectedFolder,
            selectedPlaylist,
            selectedCustomPlaylist,
            selectedVolume,
            isRtl
        ) {
            detectHorizontalDragGestures(
                onDragStart = { videoDragX = 0f },
                onHorizontalDrag = { _, dragAmount ->
                    videoDragX += dragAmount
                },
                onDragEnd = {
                    val swipeThreshold = 50.dp.toPx()
                    val isNext = if (isRtl) videoDragX > swipeThreshold else videoDragX < -swipeThreshold
                    val isPrev = if (isRtl) videoDragX < -swipeThreshold else videoDragX > swipeThreshold

                    if (isNext) {
                        onFolderSelected(null)
                        onPlaylistSelected(null)
                        onSelectCustomPlaylist(null)
                        onVolumeSelected(null)
                        val currentIndex = videoCategories.indexOf(currentCategory)
                        if (currentIndex in 0 until videoCategories.size - 1) {
                            onCategorySelected(videoCategories[currentIndex + 1])
                        }
                    } else if (isPrev) {
                        if (selectedCustomPlaylist != null) {
                            onSelectCustomPlaylist(null)
                        } else if (selectedFolder != null) {
                            onFolderSelected(null)
                        } else if (selectedPlaylist != null) {
                            onPlaylistSelected(null)
                        } else if (selectedVolume != null) {
                            onVolumeSelected(null)
                        } else {
                            val currentIndex = videoCategories.indexOf(currentCategory)
                            if (currentIndex > 0) {
                                onCategorySelected(videoCategories[currentIndex - 1])
                            }
                        }
                    }
                }
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .then(videoCategorySwipeModifier)
        ) {
            when {
            selectedCustomPlaylist != null -> {
                SubListHeader(
                    title = selectedCustomPlaylist.name,
                    subtitle = String.format(strings.videoCountLabel, customPlaylistTracks.size),
                    onBack = { onSelectCustomPlaylist(null) }
                )
                Spacer(modifier = Modifier.height(4.dp))
                VideoItemsListView(
                    videos = customPlaylistTracks,
                    currentPlaying = currentPlaying,
                    favoritePaths = favoritePaths,
                    selectedIds = selectedIds,
                    isSelectionMode = isSelectionMode,
                    onVideoClick = { onPlayVideo(it, customPlaylistTracks) },
                    onToggleFavorite = onToggleFavorite,
                    onVideoLongClick = onVideoLongClick,
                    onToggleSelection = onToggleSelection,
                    isCompact = isCompact
                )
            }
            selectedFolder != null -> {
                SubListHeader(
                    title = File(selectedFolder).name.ifEmpty { "Root" },
                    subtitle = String.format(strings.videoCountLabel, filteredVideos.size),
                    onBack = { onFolderSelected(null) }
                )
                Spacer(modifier = Modifier.height(4.dp))
                VideoItemsListView(
                    videos = filteredVideos,
                    currentPlaying = currentPlaying,
                    favoritePaths = favoritePaths,
                    selectedIds = selectedIds,
                    isSelectionMode = isSelectionMode,
                    onVideoClick = { onPlayVideo(it, filteredVideos) },
                    onToggleFavorite = onToggleFavorite,
                    onVideoLongClick = onVideoLongClick,
                    onToggleSelection = onToggleSelection,
                    isCompact = isCompact
                )
            }
            selectedPlaylist != null -> {
                val playlistTitle = when (selectedPlaylist) {
                    VideoPlaylistType.FAVORITES -> strings.playlistFavorites
                    VideoPlaylistType.SHORT_CLIPS -> strings.playlistShortClips
                    VideoPlaylistType.MOVIES -> strings.playlistMovies
                    VideoPlaylistType.RECENT -> strings.playlistRecent
                }
                SubListHeader(
                    title = playlistTitle,
                    subtitle = String.format(strings.videoCountLabel, filteredVideos.size),
                    onBack = { onPlaylistSelected(null) }
                )
                Spacer(modifier = Modifier.height(4.dp))
                VideoItemsListView(
                    videos = filteredVideos,
                    currentPlaying = currentPlaying,
                    favoritePaths = favoritePaths,
                    selectedIds = selectedIds,
                    isSelectionMode = isSelectionMode,
                    onVideoClick = { onPlayVideo(it, filteredVideos) },
                    onToggleFavorite = onToggleFavorite,
                    onVideoLongClick = onVideoLongClick,
                    onToggleSelection = onToggleSelection,
                    isCompact = isCompact
                )
            }
            selectedVolume != null -> {
                val driveLabel = drivesList.find { it.volumeId == selectedVolume }?.let {
                    if (it.isInternal) strings.internalStorage else it.label
                } ?: selectedVolume
                SubListHeader(
                    title = driveLabel,
                    subtitle = String.format(strings.videoCountLabel, filteredVideos.size),
                    onBack = { onVolumeSelected(null) }
                )
                Spacer(modifier = Modifier.height(4.dp))
                VideoItemsListView(
                    videos = filteredVideos,
                    currentPlaying = currentPlaying,
                    favoritePaths = favoritePaths,
                    selectedIds = selectedIds,
                    isSelectionMode = isSelectionMode,
                    onVideoClick = { onPlayVideo(it, filteredVideos) },
                    onToggleFavorite = onToggleFavorite,
                    onVideoLongClick = onVideoLongClick,
                    onToggleSelection = onToggleSelection,
                    isCompact = isCompact
                )
            }
            else -> {
                VideoSearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    isCompact = isCompact
                )

                Spacer(modifier = Modifier.height(8.dp))

                when (currentCategory) {
                    VideoCategory.ALL -> {
                        VideoItemsListView(
                            videos = filteredVideos,
                            currentPlaying = currentPlaying,
                            favoritePaths = favoritePaths,
                            selectedIds = selectedIds,
                            isSelectionMode = isSelectionMode,
                            onVideoClick = { onPlayVideo(it, filteredVideos) },
                            onToggleFavorite = onToggleFavorite,
                            onVideoLongClick = onVideoLongClick,
                            onToggleSelection = onToggleSelection,
                            isCompact = isCompact
                        )
                    }
                    VideoCategory.FOLDERS -> {
                        VideoFoldersListView(
                            folders = foldersList,
                            onFolderSelected = { onFolderSelected(it.folderPath) },
                            isCompact = isCompact
                        )
                    }
                    VideoCategory.PLAYLISTS -> {
                        VideoPlaylistsListView(
                            favoritesCount = favoritesCount,
                            shortClipsCount = shortClipsCount,
                            moviesCount = moviesCount,
                            recentCount = totalVideosCount,
                            customPlaylists = customPlaylists,
                            customPlaylistCounts = customPlaylistCounts,
                            onPlaylistSelected = onPlaylistSelected,
                            onCustomPlaylistSelected = onSelectCustomPlaylist,
                            onCreatePlaylistClick = onCreatePlaylistClick,
                            onDeleteCustomPlaylist = onDeleteCustomPlaylist,
                            isCompact = isCompact
                        )
                    }
                    VideoCategory.DRIVES -> {
                        VideoDrivesListView(
                            drives = drivesList,
                            onDriveSelected = { onVolumeSelected(it.volumeId) },
                            isCompact = isCompact
                        )
                    }
                }
            }
        }
    }
}
}
