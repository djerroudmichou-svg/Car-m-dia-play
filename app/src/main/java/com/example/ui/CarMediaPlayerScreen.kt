package com.example.ui

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import com.example.data.MediaItemEntity
import com.example.localization.LocalAppStrings
import com.example.service.PlaybackService
import com.example.theme.LocalCarColors
import com.example.viewmodel.MediaViewModel
import com.example.viewmodel.MusicCategory
import com.example.viewmodel.VideoCategory
import com.example.viewmodel.VideoPlaylistType
import java.io.File

@Composable
fun CarMediaPlayerScreen(
    viewModel: MediaViewModel,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val colors = LocalCarColors.current

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val musicList by viewModel.musicList.collectAsStateWithLifecycle()
    val videoList by viewModel.videoList.collectAsStateWithLifecycle()
    val volumesList by viewModel.volumesList.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val scanProgress by viewModel.scanProgress.collectAsStateWithLifecycle()

    val currentPlaying by viewModel.currentPlayingItem.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val playbackProgress by viewModel.playbackProgress.collectAsStateWithLifecycle()
    val playbackDuration by viewModel.playbackDuration.collectAsStateWithLifecycle()
    val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()
    val isShuffleEnabled by viewModel.isShuffleEnabled.collectAsStateWithLifecycle()

    val isVideoFullscreen by viewModel.isVideoFullscreen.collectAsStateWithLifecycle()
    val isNowPlayingFullscreen by viewModel.isNowPlayingFullscreen.collectAsStateWithLifecycle()

    val activePlayer by PlaybackService.activePlayer.collectAsStateWithLifecycle()

    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val currentLux by viewModel.currentLux.collectAsStateWithLifecycle()
    val isDynamicColorEnabled by viewModel.isDynamicColorEnabled.collectAsStateWithLifecycle()
    val isCompactScreenMode by viewModel.isCompactScreenMode.collectAsStateWithLifecycle()
    val isKeepScreenOn by viewModel.isKeepScreenOn.collectAsStateWithLifecycle()
    val isFullscreenMode by viewModel.isFullscreenMode.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val usbNotification by viewModel.usbNotification.collectAsStateWithLifecycle()
    val isFastForwarding by viewModel.isFastForwarding.collectAsStateWithLifecycle()
    val isRewinding by viewModel.isRewinding.collectAsStateWithLifecycle()
    val isAutoLaunchOnUsb by viewModel.isAutoLaunchOnUsb.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // 1. TRUE FULLSCREEN VIDEO OVERLAY
        if (isVideoFullscreen && currentTab == 1 && currentPlaying?.isVideo == true) {
            FullscreenVideoPlayer(
                player = activePlayer,
                currentPlaying = currentPlaying,
                isPlaying = isPlaying,
                playbackProgress = playbackProgress,
                playbackDuration = playbackDuration,
                onPlayPauseToggle = { viewModel.togglePlayPause() },
                onNext = { viewModel.playNext() },
                onPrevious = { viewModel.playPrevious() },
                onSeek = { viewModel.seekTo(it) },
                onExitFullscreen = { viewModel.setVideoFullscreen(false) }
            )
        }
        // 2. TRUE FULLSCREEN AUDIO PLAYER OVERLAY
        else if (isNowPlayingFullscreen && currentPlaying != null && currentPlaying?.isVideo == false) {
            FullscreenAudioPlayer(
                item = currentPlaying!!,
                isPlaying = isPlaying,
                playbackProgress = playbackProgress,
                playbackDuration = playbackDuration,
                repeatMode = repeatMode,
                isShuffleEnabled = isShuffleEnabled,
                isFastForwarding = isFastForwarding,
                isRewinding = isRewinding,
                onPlayPauseToggle = { viewModel.togglePlayPause() },
                onNext = { viewModel.playNext() },
                onPrevious = { viewModel.playPrevious() },
                onFastForwardStart = { viewModel.startFastForward() },
                onFastForwardEnd = { viewModel.stopFastForward() },
                onRewindStart = { viewModel.startRewind() },
                onRewindEnd = { viewModel.stopRewind() },
                onSeek = { viewModel.seekTo(it) },
                onToggleRepeat = { viewModel.toggleRepeatMode() },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onMinimize = { viewModel.setNowPlayingFullscreen(false) }
            )
        }
        // 3. STANDARD CAR DASHBOARD INTERFACE
        else {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                val isNarrow = maxWidth < 600.dp
                val isCompactLayout = isCompactScreenMode || maxHeight < 440.dp || maxWidth < 750.dp

                if (isNarrow) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // COMPACT TOP HEADER BAR
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.navBackground)
                                .border(width = 1.dp, color = colors.cardBorder)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(colors.surfaceSecondary, CircleShape)
                                        .border(1.dp, colors.accent.copy(alpha = 0.4f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DirectionsCar,
                                        contentDescription = "Car",
                                        tint = colors.accent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = when (currentTab) {
                                        0 -> strings.tabMusic
                                        1 -> strings.tabVideo
                                        else -> strings.tabSettings
                                    },
                                    color = colors.textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Storage Scan / Status Action Button
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isScanning) Color(0xFFE5A93C).copy(alpha = 0.2f) else colors.surfaceSecondary)
                                    .clickable { viewModel.triggerScan() },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isScanning) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color(0xFFE5A93C),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.Sync,
                                        contentDescription = strings.storageScanButton,
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // MAIN CONTENT AREA
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 4.dp)
                        ) {
                            when (currentTab) {
                                0 -> MusicTabContent(
                                    viewModel = viewModel,
                                    currentPlaying = currentPlaying,
                                    isPlaying = isPlaying,
                                    playbackProgress = playbackProgress,
                                    playbackDuration = playbackDuration,
                                    repeatMode = repeatMode,
                                    isShuffleEnabled = isShuffleEnabled,
                                    isCompact = true
                                )
                                1 -> VideoTabContent(
                                    viewModel = viewModel,
                                    activePlayer = activePlayer,
                                    currentPlaying = currentPlaying,
                                    isPlaying = isPlaying,
                                    playbackProgress = playbackProgress,
                                    playbackDuration = playbackDuration,
                                    isVideoFullscreen = isVideoFullscreen,
                                    isCompact = true
                                )
                                2 -> UsbAndSettingsTabContent(
                                    volumesList = volumesList,
                                    isScanning = isScanning,
                                    scanProgress = scanProgress,
                                    currentLanguage = currentLanguage,
                                    onLanguageSelected = { viewModel.setLanguage(it) },
                                    onResetLanguage = { viewModel.resetLanguageToDefault() },
                                    onTriggerScan = { viewModel.triggerScan() },
                                    themeMode = themeMode,
                                    isLightSensorAvailable = viewModel.isLightSensorAvailable,
                                    currentLux = currentLux,
                                    isDynamicColorEnabled = isDynamicColorEnabled,
                                    onThemeModeSelected = { viewModel.setThemeMode(it) },
                                    onDynamicColorToggle = { viewModel.setDynamicColorEnabled(it) },
                                    isCompactScreenMode = true,
                                    onCompactScreenModeToggle = { viewModel.setCompactScreenMode(it) },
                                    isKeepScreenOn = isKeepScreenOn,
                                    onKeepScreenOnToggle = { viewModel.setKeepScreenOn(it) },
                                    isFullscreenMode = isFullscreenMode,
                                    onFullscreenModeToggle = { viewModel.setFullscreenMode(it) },
                                    isAutoLaunchOnUsb = isAutoLaunchOnUsb,
                                    onAutoLaunchOnUsbToggle = { viewModel.setAutoLaunchOnUsb(it) }
                                )
                            }
                        }

                        // BOTTOM CAR NAVIGATION BAR
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.navBackground)
                                .border(width = 1.dp, color = colors.cardBorder)
                                .padding(vertical = 4.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NavigationTabItem(
                                icon = Icons.Filled.MusicNote,
                                label = strings.tabMusic,
                                isSelected = currentTab == 0,
                                onClick = { viewModel.selectTab(0) },
                                tag = "music_tab",
                                isCompact = true,
                                isRowLayout = true
                            )

                            NavigationTabItem(
                                icon = Icons.Filled.Movie,
                                label = strings.tabVideo,
                                isSelected = currentTab == 1,
                                onClick = { viewModel.selectTab(1) },
                                tag = "video_tab",
                                isCompact = true,
                                isRowLayout = true
                            )

                            NavigationTabItem(
                                icon = Icons.Filled.Settings,
                                label = strings.tabSettings,
                                isSelected = currentTab == 2,
                                onClick = { viewModel.selectTab(2) },
                                tag = "usb_tab",
                                isCompact = true,
                                isRowLayout = true
                            )
                        }
                    }
                } else {
                    // WIDE / LANDSCAPE CAR DASHBOARD WITH SIDE RAIL
                    Row(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // CAR-ERGONOMIC SIDE NAVIGATION RAIL
                        Column(
                            modifier = Modifier
                                .width(if (isCompactLayout) 76.dp else 104.dp)
                                .fillMaxHeight()
                                .background(colors.navBackground)
                                .border(width = 1.dp, color = colors.cardBorder)
                                .padding(vertical = if (isCompactLayout) 8.dp else 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Header Brand Logo
                            Box(
                                modifier = Modifier
                                    .size(if (isCompactLayout) 40.dp else 52.dp)
                                    .background(colors.surfaceSecondary, CircleShape)
                                    .border(1.dp, colors.accent.copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DirectionsCar,
                                    contentDescription = "Car",
                                    tint = colors.accent,
                                    modifier = Modifier.size(if (isCompactLayout) 22.dp else 30.dp)
                                )
                            }

                            // Navigation Tab Buttons
                            Column(
                                verticalArrangement = Arrangement.spacedBy(if (isCompactLayout) 6.dp else 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                NavigationTabItem(
                                    icon = Icons.Filled.MusicNote,
                                    label = strings.tabMusic,
                                    isSelected = currentTab == 0,
                                    onClick = { viewModel.selectTab(0) },
                                    tag = "music_tab",
                                    isCompact = isCompactLayout
                                )

                                NavigationTabItem(
                                    icon = Icons.Filled.Movie,
                                    label = strings.tabVideo,
                                    isSelected = currentTab == 1,
                                    onClick = { viewModel.selectTab(1) },
                                    tag = "video_tab",
                                    isCompact = isCompactLayout
                                )

                                NavigationTabItem(
                                    icon = Icons.Filled.Settings,
                                    label = strings.tabSettings,
                                    isSelected = currentTab == 2,
                                    onClick = { viewModel.selectTab(2) },
                                    tag = "usb_tab",
                                    isCompact = isCompactLayout
                                )
                            }

                            // Storage Scan / Status Action Button
                            Box(
                                modifier = Modifier
                                    .size(if (isCompactLayout) 38.dp else 46.dp)
                                    .clip(CircleShape)
                                    .background(if (isScanning) Color(0xFFE5A93C).copy(alpha = 0.2f) else colors.surfaceSecondary)
                                    .clickable { viewModel.triggerScan() },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isScanning) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(if (isCompactLayout) 18.dp else 24.dp),
                                        color = Color(0xFFE5A93C),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.Sync,
                                        contentDescription = strings.storageScanButton,
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(if (isCompactLayout) 18.dp else 22.dp)
                                    )
                                }
                            }
                        }

                        // MAIN CONTENT AREA
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(
                                    start = if (isCompactLayout) 6.dp else 12.dp,
                                    top = if (isCompactLayout) 4.dp else 8.dp,
                                    end = if (isCompactLayout) 6.dp else 12.dp,
                                    bottom = if (isCompactLayout) 4.dp else 8.dp
                                )
                        ) {
                            when (currentTab) {
                                0 -> MusicTabContent(
                                    viewModel = viewModel,
                                    currentPlaying = currentPlaying,
                                    isPlaying = isPlaying,
                                    playbackProgress = playbackProgress,
                                    playbackDuration = playbackDuration,
                                    repeatMode = repeatMode,
                                    isShuffleEnabled = isShuffleEnabled,
                                    isCompact = isCompactLayout
                                )
                                1 -> VideoTabContent(
                                    viewModel = viewModel,
                                    activePlayer = activePlayer,
                                    currentPlaying = currentPlaying,
                                    isPlaying = isPlaying,
                                    playbackProgress = playbackProgress,
                                    playbackDuration = playbackDuration,
                                    isVideoFullscreen = isVideoFullscreen,
                                    isCompact = isCompactLayout
                                )
                                2 -> UsbAndSettingsTabContent(
                                    volumesList = volumesList,
                                    isScanning = isScanning,
                                    scanProgress = scanProgress,
                                    currentLanguage = currentLanguage,
                                    onLanguageSelected = { viewModel.setLanguage(it) },
                                    onResetLanguage = { viewModel.resetLanguageToDefault() },
                                    onTriggerScan = { viewModel.triggerScan() },
                                    themeMode = themeMode,
                                    isLightSensorAvailable = viewModel.isLightSensorAvailable,
                                    currentLux = currentLux,
                                    isDynamicColorEnabled = isDynamicColorEnabled,
                                    onThemeModeSelected = { viewModel.setThemeMode(it) },
                                    onDynamicColorToggle = { viewModel.setDynamicColorEnabled(it) },
                                    isCompactScreenMode = isCompactLayout,
                                    onCompactScreenModeToggle = { viewModel.setCompactScreenMode(it) },
                                    isKeepScreenOn = isKeepScreenOn,
                                    onKeepScreenOnToggle = { viewModel.setKeepScreenOn(it) },
                                    isFullscreenMode = isFullscreenMode,
                                    onFullscreenModeToggle = { viewModel.setFullscreenMode(it) },
                                    isAutoLaunchOnUsb = isAutoLaunchOnUsb,
                                    onAutoLaunchOnUsbToggle = { viewModel.setAutoLaunchOnUsb(it) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. FLOATING USB EVENT NOTIFICATION (Automatic popup when USB is inserted / removed)
        AnimatedVisibility(
            visible = usbNotification != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp)
                .zIndex(100f)
        ) {
            usbNotification?.let { notif ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(1.5.dp, colors.accent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .clickable { viewModel.dismissUsbNotification() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (notif.isConnected) Icons.Filled.Usb else Icons.Filled.UsbOff,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = notif.message,
                            color = colors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { viewModel.dismissUsbNotification() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Dismiss",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // 5. CONTINUOUS SEEKING HUD OVERLAY (Visible during hardware SWC or touch button hold)
        AnimatedVisibility(
            visible = isFastForwarding || isRewinding,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.Center)
                .zIndex(90f)
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.82f), RoundedCornerShape(16.dp))
                    .border(2.dp, colors.accent, RoundedCornerShape(16.dp))
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (isFastForwarding) Icons.Filled.FastForward else Icons.Filled.FastRewind,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = if (isFastForwarding) strings.fastForwarding else strings.rewinding,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==========================================
// NAVIGATION TAB ITEM
// ==========================================
@Composable
fun NavigationTabItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    tag: String,
    isCompact: Boolean = false,
    isRowLayout: Boolean = false
) {
    val colors = LocalCarColors.current

    if (isRowLayout) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) colors.surfaceSecondary else Color.Transparent)
                .border(
                    width = if (isSelected) 1.5.dp else 0.dp,
                    color = if (isSelected) colors.accent else Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp, horizontal = 12.dp)
                .testTag(tag)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) colors.accent else colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = if (isSelected) colors.textPrimary else colors.textSecondary,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(if (isCompact) 66.dp else 86.dp)
                .clip(RoundedCornerShape(if (isCompact) 10.dp else 14.dp))
                .background(if (isSelected) colors.surfaceSecondary else Color.Transparent)
                .border(
                    width = if (isSelected) 1.5.dp else 0.dp,
                    color = if (isSelected) colors.accent else Color.Transparent,
                    shape = RoundedCornerShape(if (isCompact) 10.dp else 14.dp)
                )
                .clickable(onClick = onClick)
                .padding(vertical = if (isCompact) 6.dp else 10.dp, horizontal = 4.dp)
                .testTag(tag)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) colors.accent else colors.textSecondary,
                modifier = Modifier.size(if (isCompact) 22.dp else 28.dp)
            )
            Spacer(modifier = Modifier.height(if (isCompact) 2.dp else 4.dp))
            Text(
                text = label,
                color = if (isSelected) colors.textPrimary else colors.textSecondary,
                fontSize = if (isCompact) 10.sp else 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ==========================================
// MUSIC TAB CONTENT (SPLIT DUAL PANE)
// ==========================================
@Composable
fun MusicTabContent(
    viewModel: MediaViewModel,
    currentPlaying: MediaItemEntity?,
    isPlaying: Boolean,
    playbackProgress: Long,
    playbackDuration: Long,
    repeatMode: Int,
    isShuffleEnabled: Boolean,
    isCompact: Boolean = false
) {
    val strings = LocalAppStrings.current
    val colors = LocalCarColors.current

    val currentCategory by viewModel.musicCategory.collectAsStateWithLifecycle()
    val filteredTracks by viewModel.filteredMusicList.collectAsStateWithLifecycle()
    val artistsList by viewModel.artistsList.collectAsStateWithLifecycle()
    val albumsList by viewModel.albumsList.collectAsStateWithLifecycle()
    val foldersList by viewModel.foldersList.collectAsStateWithLifecycle()

    val selectedArtist by viewModel.selectedArtist.collectAsStateWithLifecycle()
    val selectedAlbum by viewModel.selectedAlbum.collectAsStateWithLifecycle()
    val selectedFolder by viewModel.selectedFolder.collectAsStateWithLifecycle()
    val selectedMusicVolume by viewModel.selectedMusicVolume.collectAsStateWithLifecycle()
    val musicVolumesList by viewModel.musicVolumesList.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val isFastForwarding by viewModel.isFastForwarding.collectAsStateWithLifecycle()
    val isRewinding by viewModel.isRewinding.collectAsStateWithLifecycle()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isNarrow = maxWidth < 620.dp

        if (isNarrow) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Music Browser & Lists
                MusicBrowserPane(
                    currentCategory = currentCategory,
                    onCategorySelected = {
                        viewModel.setMusicCategory(it)
                        viewModel.selectMusicVolume(null)
                    },
                    volumes = musicVolumesList,
                    selectedVolumeId = selectedMusicVolume,
                    onVolumeSelected = { volumeId ->
                        viewModel.setMusicCategory(MusicCategory.DRIVES)
                        viewModel.selectMusicVolume(volumeId)
                    },
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    selectedArtist = selectedArtist,
                    onSelectArtist = { viewModel.selectArtist(it) },
                    selectedAlbum = selectedAlbum,
                    onSelectAlbum = { viewModel.selectAlbum(it) },
                    selectedFolder = selectedFolder,
                    onSelectFolder = { viewModel.selectFolder(it) },
                    filteredTracks = filteredTracks,
                    currentPlaying = currentPlaying,
                    onPlayTrack = { track, list -> viewModel.playMediaItem(track, list) },
                    artistsList = artistsList,
                    albumsList = albumsList,
                    foldersList = foldersList,
                    isCompact = true,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )

                // Compact Docked Mini-Player Bar when playing
                if (currentPlaying != null) {
                    CompactAudioMiniBar(
                        currentPlaying = currentPlaying,
                        isPlaying = isPlaying,
                        playbackProgress = playbackProgress,
                        playbackDuration = playbackDuration,
                        onPlayPauseToggle = { viewModel.togglePlayPause() },
                        onNext = { viewModel.playNext() },
                        onPrevious = { viewModel.playPrevious() },
                        onExpandFullscreen = { viewModel.setNowPlayingFullscreen(true) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 14.dp)
            ) {
                // LEFT PANE: MUSIC BROWSER & LISTS
                MusicBrowserPane(
                    currentCategory = currentCategory,
                    onCategorySelected = {
                        viewModel.setMusicCategory(it)
                        viewModel.selectMusicVolume(null)
                    },
                    volumes = musicVolumesList,
                    selectedVolumeId = selectedMusicVolume,
                    onVolumeSelected = { volumeId ->
                        viewModel.setMusicCategory(MusicCategory.DRIVES)
                        viewModel.selectMusicVolume(volumeId)
                    },
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    selectedArtist = selectedArtist,
                    onSelectArtist = { viewModel.selectArtist(it) },
                    selectedAlbum = selectedAlbum,
                    onSelectAlbum = { viewModel.selectAlbum(it) },
                    selectedFolder = selectedFolder,
                    onSelectFolder = { viewModel.selectFolder(it) },
                    filteredTracks = filteredTracks,
                    currentPlaying = currentPlaying,
                    onPlayTrack = { track, list -> viewModel.playMediaItem(track, list) },
                    artistsList = artistsList,
                    albumsList = albumsList,
                    foldersList = foldersList,
                    isCompact = isCompact,
                    modifier = Modifier
                        .weight(1.35f)
                        .fillMaxHeight()
                )

                // RIGHT PANE: NOW PLAYING CAR CARD
                MusicNowPlayingPane(
                    currentPlaying = currentPlaying,
                    isPlaying = isPlaying,
                    playbackProgress = playbackProgress,
                    playbackDuration = playbackDuration,
                    repeatMode = repeatMode,
                    isShuffleEnabled = isShuffleEnabled,
                    isFastForwarding = isFastForwarding,
                    isRewinding = isRewinding,
                    onPlayPauseToggle = { viewModel.togglePlayPause() },
                    onNext = { viewModel.playNext() },
                    onPrevious = { viewModel.playPrevious() },
                    onFastForwardStart = { viewModel.startFastForward() },
                    onFastForwardEnd = { viewModel.stopFastForward() },
                    onRewindStart = { viewModel.startRewind() },
                    onRewindEnd = { viewModel.stopRewind() },
                    onSeek = { frac -> onSeekSafe(viewModel, frac, playbackDuration) },
                    onShuffleToggle = { viewModel.toggleShuffle() },
                    onRepeatToggle = { viewModel.toggleRepeatMode() },
                    onFullscreenExpand = { viewModel.setNowPlayingFullscreen(true) },
                    isCompact = isCompact,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
    }
}

private fun onSeekSafe(viewModel: MediaViewModel, frac: Float, duration: Long) {
    if (duration > 0) {
        val target = (frac * duration).toLong().coerceIn(0L, duration)
        viewModel.seekTo(target)
    }
}

// ==========================================
// VIDEO TAB CONTENT (DUAL PANE WITH PLAYLISTS & FOLDERS)
// ==========================================
@Composable
fun VideoTabContent(
    viewModel: MediaViewModel,
    activePlayer: androidx.media3.exoplayer.ExoPlayer?,
    currentPlaying: MediaItemEntity?,
    isPlaying: Boolean,
    playbackProgress: Long,
    playbackDuration: Long,
    isVideoFullscreen: Boolean,
    isCompact: Boolean = false
) {
    val strings = LocalAppStrings.current
    val colors = LocalCarColors.current

    val currentCategory by viewModel.videoCategory.collectAsStateWithLifecycle()
    val filteredVideos by viewModel.filteredVideoList.collectAsStateWithLifecycle()
    val selectedFolder by viewModel.selectedVideoFolder.collectAsStateWithLifecycle()
    val selectedPlaylist by viewModel.selectedVideoPlaylist.collectAsStateWithLifecycle()
    val selectedVolume by viewModel.selectedVideoVolume.collectAsStateWithLifecycle()
    val foldersList by viewModel.videoFoldersList.collectAsStateWithLifecycle()
    val drivesList by viewModel.videoVolumesList.collectAsStateWithLifecycle()
    val searchQuery by viewModel.videoSearchQuery.collectAsStateWithLifecycle()
    val favoritePaths by viewModel.favoriteVideoPaths.collectAsStateWithLifecycle()
    val favoritesCount by viewModel.videoFavoritesCount.collectAsStateWithLifecycle()
    val shortClipsCount by viewModel.videoShortClipsCount.collectAsStateWithLifecycle()
    val moviesCount by viewModel.videoMoviesCount.collectAsStateWithLifecycle()
    val totalVideos by viewModel.videoList.collectAsStateWithLifecycle()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isNarrow = maxWidth < 620.dp

        if (isNarrow) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (currentPlaying?.isVideo == true) {
                    EmbeddedVideoPlayerCard(
                        player = activePlayer,
                        currentPlaying = currentPlaying,
                        isPlaying = isPlaying,
                        playbackProgress = playbackProgress,
                        playbackDuration = playbackDuration,
                        isVideoFullscreen = isVideoFullscreen,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isCompact) 175.dp else 210.dp),
                        onPlayPauseToggle = { viewModel.togglePlayPause() },
                        onNext = { viewModel.playNext() },
                        onPrevious = { viewModel.playPrevious() },
                        onSeek = { viewModel.seekTo(it) },
                        onFullscreenClick = { viewModel.setVideoFullscreen(true) }
                    )
                }

                VideoBrowserPane(
                    currentCategory = currentCategory,
                    onCategorySelected = { viewModel.setVideoCategory(it) },
                    drivesList = drivesList,
                    selectedVolume = selectedVolume,
                    onVolumeSelected = { viewModel.selectVideoVolume(it) },
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.setVideoSearchQuery(it) },
                    selectedFolder = selectedFolder,
                    onFolderSelected = { viewModel.selectVideoFolder(it) },
                    selectedPlaylist = selectedPlaylist,
                    onPlaylistSelected = { viewModel.selectVideoPlaylist(it) },
                    filteredVideos = filteredVideos,
                    currentPlaying = currentPlaying,
                    favoritePaths = favoritePaths,
                    onPlayVideo = { video, list -> viewModel.playMediaItem(video, list) },
                    onToggleFavorite = { viewModel.toggleVideoFavorite(it) },
                    foldersList = foldersList,
                    totalVideosCount = totalVideos.size,
                    favoritesCount = favoritesCount,
                    shortClipsCount = shortClipsCount,
                    moviesCount = moviesCount,
                    isCompact = true,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 14.dp)
            ) {
                VideoBrowserPane(
                    currentCategory = currentCategory,
                    onCategorySelected = { viewModel.setVideoCategory(it) },
                    drivesList = drivesList,
                    selectedVolume = selectedVolume,
                    onVolumeSelected = { viewModel.selectVideoVolume(it) },
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.setVideoSearchQuery(it) },
                    selectedFolder = selectedFolder,
                    onFolderSelected = { viewModel.selectVideoFolder(it) },
                    selectedPlaylist = selectedPlaylist,
                    onPlaylistSelected = { viewModel.selectVideoPlaylist(it) },
                    filteredVideos = filteredVideos,
                    currentPlaying = currentPlaying,
                    favoritePaths = favoritePaths,
                    onPlayVideo = { video, list -> viewModel.playMediaItem(video, list) },
                    onToggleFavorite = { viewModel.toggleVideoFavorite(it) },
                    foldersList = foldersList,
                    totalVideosCount = totalVideos.size,
                    favoritesCount = favoritesCount,
                    shortClipsCount = shortClipsCount,
                    moviesCount = moviesCount,
                    isCompact = isCompact,
                    modifier = Modifier
                        .weight(1.05f)
                        .fillMaxHeight()
                )

                EmbeddedVideoPlayerCard(
                    player = activePlayer,
                    currentPlaying = currentPlaying,
                    isPlaying = isPlaying,
                    playbackProgress = playbackProgress,
                    playbackDuration = playbackDuration,
                    isVideoFullscreen = isVideoFullscreen,
                    modifier = Modifier
                        .weight(1.35f)
                        .fillMaxHeight(),
                    onPlayPauseToggle = { viewModel.togglePlayPause() },
                    onNext = { viewModel.playNext() },
                    onPrevious = { viewModel.playPrevious() },
                    onSeek = { viewModel.seekTo(it) },
                    onFullscreenClick = { viewModel.setVideoFullscreen(true) }
                )
            }
        }
    }
}
