package com.example.ui

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.MediaItemEntity
import com.example.localization.LocalAppStrings
import com.example.theme.LocalCarColors
import com.example.viewmodel.AlbumGroup
import com.example.viewmodel.ArtistGroup
import com.example.viewmodel.FolderGroup
import com.example.viewmodel.MusicCategory
import com.example.viewmodel.MusicVolumeGroup
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File

// ==========================================
// CATEGORY CHIP (Car-Friendly Filters)
// ==========================================
@Composable
fun CategoryChip(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    isCompact: Boolean = false
) {
    val colors = LocalCarColors.current

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(if (isCompact) 14.dp else 20.dp))
            .background(if (isSelected) colors.accent else colors.surfaceSecondary)
            .border(
                width = 1.dp,
                color = if (isSelected) colors.accent else colors.cardBorder,
                shape = RoundedCornerShape(if (isCompact) 14.dp else 20.dp)
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = if (isCompact) 8.dp else 12.dp,
                vertical = if (isCompact) 4.dp else 7.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (isCompact) 4.dp else 5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) colors.onAccent else colors.textSecondary,
            modifier = Modifier.size(if (isCompact) 13.dp else 16.dp)
        )
        Text(
            text = title,
            color = if (isSelected) colors.onAccent else colors.textPrimary,
            fontSize = if (isCompact) 11.sp else 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

// ==========================================
// MUSIC CATEGORY SELECTOR (Horizontal Swiping Chips)
// ==========================================
@Composable
fun MusicCategorySelector(
    selectedCategory: MusicCategory,
    onCategorySelected: (MusicCategory) -> Unit,
    volumes: List<MusicVolumeGroup> = emptyList(),
    selectedVolumeId: String? = null,
    onVolumeSelected: (String) -> Unit = {},
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(if (isCompact) 6.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryChip(
            title = strings.allTracks,
            icon = Icons.Filled.MusicNote,
            isSelected = selectedCategory == MusicCategory.ALL && selectedVolumeId == null,
            onClick = {
                onCategorySelected(MusicCategory.ALL)
            },
            isCompact = isCompact
        )
        CategoryChip(
            title = strings.artists,
            icon = Icons.Filled.Person,
            isSelected = selectedCategory == MusicCategory.ARTISTS,
            onClick = { onCategorySelected(MusicCategory.ARTISTS) },
            isCompact = isCompact
        )
        CategoryChip(
            title = strings.albums,
            icon = Icons.Filled.Album,
            isSelected = selectedCategory == MusicCategory.ALBUMS,
            onClick = { onCategorySelected(MusicCategory.ALBUMS) },
            isCompact = isCompact
        )
        CategoryChip(
            title = strings.folders,
            icon = Icons.Filled.Folder,
            isSelected = selectedCategory == MusicCategory.FOLDERS,
            onClick = { onCategorySelected(MusicCategory.FOLDERS) },
            isCompact = isCompact
        )
        CategoryChip(
            title = strings.storageDrives,
            icon = Icons.Filled.Storage,
            isSelected = selectedCategory == MusicCategory.DRIVES && selectedVolumeId == null,
            onClick = { onCategorySelected(MusicCategory.DRIVES) },
            isCompact = isCompact
        )

        // Direct Storage / USB chips (Tablet Internal, USB 1, USB 2, SD Card)
        volumes.forEach { drive ->
            val icon = when {
                drive.isInternal -> Icons.Filled.Storage
                drive.isSdCard -> Icons.Filled.SdCard
                else -> Icons.Filled.Usb
            }
            CategoryChip(
                title = drive.label,
                icon = icon,
                isSelected = (selectedCategory == MusicCategory.DRIVES && selectedVolumeId == drive.volumeId),
                onClick = {
                    onVolumeSelected(drive.volumeId)
                },
                isCompact = isCompact
            )
        }
    }
}

// ==========================================
// SUB-LIST HEADER (Back Navigation)
// ==========================================
@Composable
fun SubListHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit
) {
    val colors = LocalCarColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(38.dp)
                .background(colors.surfaceSecondary, CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = subtitle,
                tint = colors.accent,
                modifier = Modifier.size(20.dp)
            )
        }
        Column {
            Text(
                text = title,
                color = colors.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                color = colors.textSecondary,
                fontSize = 11.sp
            )
        }
    }
}

// ==========================================
// TRACKS LIST VIEW
// ==========================================
@Composable
fun TracksListView(
    tracks: List<MediaItemEntity>,
    currentPlaying: MediaItemEntity?,
    isPlaying: Boolean,
    onTrackClick: (MediaItemEntity) -> Unit
) {
    val strings = LocalAppStrings.current
    val colors = LocalCarColors.current

    if (tracks.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.surface, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(strings.noTracksFound, color = colors.textSecondary, fontSize = 13.sp)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(tracks, key = { it.filePath }) { track ->
                val isSelected = currentPlaying?.filePath == track.filePath
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) colors.surfaceSecondary.copy(alpha = 0.9f) else colors.surface)
                        .border(
                            width = if (isSelected) 1.5.dp else 0.dp,
                            color = if (isSelected) colors.accent else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onTrackClick(track) }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Album Cover Thumbnail
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        CoverArtImage(
                            coverArtPath = track.coverArtPath,
                            title = track.title,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (isSelected && isPlaying) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0x80000000)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.GraphicEq,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    // Track Info
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            color = if (isSelected) colors.accent else colors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${track.artist ?: strings.unknownArtist} • ${track.album ?: strings.unknownAlbum}",
                            color = colors.textSecondary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Duration
                    Text(
                        text = DateUtils.formatElapsedTime(track.duration / 1000),
                        color = colors.textSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// ARTISTS LIST VIEW
// ==========================================
@Composable
fun ArtistsListView(
    artists: List<ArtistGroup>,
    onArtistSelected: (ArtistGroup) -> Unit
) {
    val strings = LocalAppStrings.current
    val colors = LocalCarColors.current

    if (artists.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(strings.noTracksFound, color = colors.textSecondary, fontSize = 13.sp)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(artists, key = { it.name }) { artist ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .clickable { onArtistSelected(artist) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(colors.surfaceSecondary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = artist.name,
                            color = colors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${artist.trackCount} ${strings.tracksCount}",
                            color = colors.textSecondary,
                            fontSize = 11.sp
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

// ==========================================
// ALBUMS LIST VIEW
// ==========================================
@Composable
fun AlbumsListView(
    albums: List<AlbumGroup>,
    onAlbumSelected: (AlbumGroup) -> Unit
) {
    val strings = LocalAppStrings.current
    val colors = LocalCarColors.current

    if (albums.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(strings.noTracksFound, color = colors.textSecondary, fontSize = 13.sp)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(albums, key = { "${it.title}_${it.artist}" }) { album ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .clickable { onAlbumSelected(album) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        CoverArtImage(
                            coverArtPath = album.sampleTrack.coverArtPath,
                            title = album.title,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = album.title,
                            color = colors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${album.artist ?: strings.unknownArtist} • ${album.trackCount} ${strings.tracksCount}",
                            color = colors.textSecondary,
                            fontSize = 11.sp
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

// ==========================================
// FOLDERS LIST VIEW
// ==========================================
@Composable
fun FoldersListView(
    folders: List<FolderGroup>,
    onFolderSelected: (FolderGroup) -> Unit
) {
    val strings = LocalAppStrings.current
    val colors = LocalCarColors.current

    if (folders.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(strings.noTracksFound, color = colors.textSecondary, fontSize = 13.sp)
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
                        .clickable { onFolderSelected(folder) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(colors.surfaceSecondary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Folder,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = folder.folderName,
                            color = colors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${folder.trackCount} ${strings.tracksCount}",
                            color = colors.textSecondary,
                            fontSize = 11.sp
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

// ==========================================
// COVER ART IMAGE LOADER (COIL + CAR GRADIENT FALLBACK)
// ==========================================
@Composable
fun CoverArtImage(
    coverArtPath: String?,
    title: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalCarColors.current

    if (!coverArtPath.isNullOrEmpty() && File(coverArtPath).exists()) {
        AsyncImage(
            model = File(coverArtPath),
            contentDescription = title,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        val gradient = Brush.linearGradient(
            colors = listOf(
                colors.surfaceSecondary,
                colors.surface
            )
        )
        Box(
            modifier = modifier.background(gradient),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Album,
                contentDescription = null,
                tint = colors.accent.copy(alpha = 0.8f),
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

// ==========================================
// HOLDABLE SEEK BUTTON (TAP TO SKIP, HOLD TO FAST-FORWARD / REWIND)
// ==========================================
@Composable
fun CarSeekButton(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 26.dp,
    tint: Color = LocalCarColors.current.textPrimary,
    containerColor: Color = LocalCarColors.current.surfaceSecondary,
    isSeeking: Boolean = false,
    onClick: () -> Unit,
    onHoldStart: () -> Unit,
    onHoldEnd: () -> Unit
) {
    val colors = LocalCarColors.current
    var isHolding by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(if (isSeeking || isHolding) colors.accent.copy(alpha = 0.35f) else containerColor)
            .border(
                width = if (isSeeking || isHolding) 1.5.dp else 0.dp,
                color = if (isSeeking || isHolding) colors.accent else Color.Transparent,
                shape = CircleShape
            )
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    var held = false
                    val timeoutResult = withTimeoutOrNull(350L) {
                        waitForUpOrCancellation()
                    }
                    if (timeoutResult != null) {
                        // Short click (Next or Previous Track)
                        onClick()
                    } else {
                        // Held past 350ms -> Continuous Fast-Forward / Rewind
                        held = true
                        isHolding = true
                        onHoldStart()
                        waitForUpOrCancellation()
                        isHolding = false
                        onHoldEnd()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isSeeking || isHolding) colors.accent else tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

// ==========================================
// MUSIC DRIVES LIST VIEW (Internal Tablet, USB 1, USB 2, SD Card)
// ==========================================
@Composable
fun MusicDrivesListView(
    drives: List<MusicVolumeGroup>,
    onDriveSelected: (MusicVolumeGroup) -> Unit,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colors = LocalCarColors.current
    val strings = LocalAppStrings.current

    if (drives.isEmpty()) {
        Box(
            modifier = modifier
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
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(if (isCompact) 6.dp else 8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(drives, key = { it.volumeId }) { drive ->
                MusicDriveItem(
                    drive = drive,
                    onClick = { onDriveSelected(drive) },
                    isCompact = isCompact
                )
            }
        }
    }
}

@Composable
fun MusicDriveItem(
    drive: MusicVolumeGroup,
    onClick: () -> Unit,
    isCompact: Boolean = false
) {
    val colors = LocalCarColors.current
    val strings = LocalAppStrings.current

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
            .clickable(onClick = onClick)
            .padding(
                horizontal = if (isCompact) 10.dp else 14.dp,
                vertical = if (isCompact) 8.dp else 12.dp
            )
            .testTag("music_drive_${drive.volumeId}"),
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
                text = "${drive.trackCount} ${strings.tracksCount}",
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

