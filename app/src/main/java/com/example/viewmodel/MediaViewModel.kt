package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.MediaDatabase
import com.example.data.MediaItemEntity
import com.example.data.MediaRepository
import com.example.data.VolumeEntity
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.localization.LocalizationManager
import com.example.scanner.UsbEvent
import com.example.scanner.UsbMediaScanner
import com.example.service.PlaybackService
import com.example.theme.CarThemeManager
import com.example.theme.DynamicColorExtractor
import com.example.theme.ThemeMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

enum class MusicCategory {
    ALL,
    ARTISTS,
    ALBUMS,
    FOLDERS,
    DRIVES
}

enum class VideoCategory {
    ALL,
    FOLDERS,
    PLAYLISTS,
    DRIVES
}

enum class VideoPlaylistType {
    FAVORITES,
    SHORT_CLIPS,
    MOVIES,
    RECENT
}

data class VideoVolumeGroup(
    val volumeId: String,
    val label: String,
    val isInternal: Boolean,
    val videoCount: Int,
    val isSdCard: Boolean = false,
    val rootPath: String = ""
)

data class MusicVolumeGroup(
    val volumeId: String,
    val label: String,
    val isInternal: Boolean,
    val isSdCard: Boolean = false,
    val trackCount: Int,
    val rootPath: String = ""
)

data class ArtistGroup(
    val name: String,
    val trackCount: Int,
    val sampleTrack: MediaItemEntity
)

data class AlbumGroup(
    val title: String,
    val artist: String,
    val trackCount: Int,
    val sampleTrack: MediaItemEntity
)

data class FolderGroup(
    val folderPath: String,
    val folderName: String,
    val trackCount: Int
)

data class UsbNotification(
    val message: String,
    val isConnected: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class MediaViewModel(
    application: Application,
    private val repository: MediaRepository,
    private val scanner: UsbMediaScanner
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "MediaViewModel"
        private const val PREFS_NAME = "car_playback_resume_prefs"
        private const val KEY_LAST_PATH = "last_played_path"
        private const val KEY_LAST_POS = "last_played_pos"
        private const val KEY_LAST_IS_VIDEO = "last_played_is_video"
    }

    private val playbackPrefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Localization Manager
    val localizationManager = LocalizationManager(application)
    val currentLanguage: StateFlow<AppLanguage> = localizationManager.currentLanguage
    val appStrings: StateFlow<AppStrings> = currentLanguage
        .map { localizationManager.getStrings(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, localizationManager.getStrings())

    fun setLanguage(language: AppLanguage) {
        localizationManager.setLanguage(language)
    }

    fun resetLanguageToDefault() {
        localizationManager.resetToSystemDefault()
    }

    // Car Theme & Ambient Sensor Manager
    val themeManager = CarThemeManager(application)
    val themeMode: StateFlow<ThemeMode> = themeManager.themeMode
    val isDarkTheme: StateFlow<Boolean> = themeManager.isDarkTheme
    val isLightSensorAvailable: Boolean = themeManager.isLightSensorAvailable
    val currentLux: StateFlow<Float> = themeManager.currentLux
    val isDynamicColorEnabled: StateFlow<Boolean> = themeManager.isDynamicColorEnabled
    val isCompactScreenMode: StateFlow<Boolean> = themeManager.isCompactScreenMode
    val isKeepScreenOn: StateFlow<Boolean> = themeManager.isKeepScreenOn
    val isFullscreenMode: StateFlow<Boolean> = themeManager.isFullscreenMode
    val isAutoLaunchOnUsb: StateFlow<Boolean> = themeManager.isAutoLaunchOnUsb

    fun setThemeMode(mode: ThemeMode) {
        themeManager.setThemeMode(mode)
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        themeManager.setDynamicColorEnabled(enabled)
    }

    fun setCompactScreenMode(enabled: Boolean) {
        themeManager.setCompactScreenMode(enabled)
    }

    fun setKeepScreenOn(enabled: Boolean) {
        themeManager.setKeepScreenOn(enabled)
    }

    fun setFullscreenMode(enabled: Boolean) {
        themeManager.setFullscreenMode(enabled)
    }

    fun setAutoLaunchOnUsb(enabled: Boolean) {
        themeManager.setAutoLaunchOnUsb(enabled)
    }

    private val _dynamicAccentColor = MutableStateFlow<Color?>(null)
    val dynamicAccentColor: StateFlow<Color?> = _dynamicAccentColor.asStateFlow()

    // UI state
    val musicList: StateFlow<List<MediaItemEntity>> = repository.mountedMusic
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val videoList: StateFlow<List<MediaItemEntity>> = repository.mountedVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val volumesList: StateFlow<List<VolumeEntity>> = repository.allVolumes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isScanning: StateFlow<Boolean> = scanner.isScanning
    val scanProgress: StateFlow<String> = scanner.scanProgress

    // Navigation and tab states
    private val _currentTab = MutableStateFlow(0) // 0: Music, 1: Video, 2: Volumes
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    // Music category & playlist filter state
    private val _musicCategory = MutableStateFlow(MusicCategory.ALL)
    val musicCategory: StateFlow<MusicCategory> = _musicCategory.asStateFlow()

    private val _selectedArtist = MutableStateFlow<String?>(null)
    val selectedArtist: StateFlow<String?> = _selectedArtist.asStateFlow()

    private val _selectedAlbum = MutableStateFlow<String?>(null)
    val selectedAlbum: StateFlow<String?> = _selectedAlbum.asStateFlow()

    private val _selectedFolder = MutableStateFlow<String?>(null)
    val selectedFolder: StateFlow<String?> = _selectedFolder.asStateFlow()

    private val _selectedMusicVolume = MutableStateFlow<String?>(null)
    val selectedMusicVolume: StateFlow<String?> = _selectedMusicVolume.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Video category, playlists, folders & search state
    private val _videoCategory = MutableStateFlow(VideoCategory.ALL)
    val videoCategory: StateFlow<VideoCategory> = _videoCategory.asStateFlow()

    private val _selectedVideoFolder = MutableStateFlow<String?>(null)
    val selectedVideoFolder: StateFlow<String?> = _selectedVideoFolder.asStateFlow()

    private val _selectedVideoPlaylist = MutableStateFlow<VideoPlaylistType?>(null)
    val selectedVideoPlaylist: StateFlow<VideoPlaylistType?> = _selectedVideoPlaylist.asStateFlow()

    private val _selectedVideoVolume = MutableStateFlow<String?>(null)
    val selectedVideoVolume: StateFlow<String?> = _selectedVideoVolume.asStateFlow()

    private val _videoSearchQuery = MutableStateFlow("")
    val videoSearchQuery: StateFlow<String> = _videoSearchQuery.asStateFlow()

    private val _favoriteVideoPaths = MutableStateFlow<Set<String>>(loadFavoriteVideos())
    val favoriteVideoPaths: StateFlow<Set<String>> = _favoriteVideoPaths.asStateFlow()

    private fun loadFavoriteVideos(): Set<String> {
        return playbackPrefs.getStringSet("favorite_videos_set", emptySet()) ?: emptySet()
    }

    // Fullscreen view states
    private val _isVideoFullscreen = MutableStateFlow(false)
    val isVideoFullscreen: StateFlow<Boolean> = _isVideoFullscreen.asStateFlow()

    private val _isNowPlayingFullscreen = MutableStateFlow(false)
    val isNowPlayingFullscreen: StateFlow<Boolean> = _isNowPlayingFullscreen.asStateFlow()

    // Playback loop and shuffle
    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    // Active playlist and item states
    private val _currentPlayingItem = MutableStateFlow<MediaItemEntity?>(null)
    val currentPlayingItem: StateFlow<MediaItemEntity?> = _currentPlayingItem.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0L)
    val playbackProgress: StateFlow<Long> = _playbackProgress.asStateFlow()

    private val _playbackDuration = MutableStateFlow(0L)
    val playbackDuration: StateFlow<Long> = _playbackDuration.asStateFlow()

    // Fast-Forward & Rewind states
    private val _isFastForwarding = MutableStateFlow(false)
    val isFastForwarding: StateFlow<Boolean> = _isFastForwarding.asStateFlow()

    private val _isRewinding = MutableStateFlow(false)
    val isRewinding: StateFlow<Boolean> = _isRewinding.asStateFlow()

    private var fastForwardJob: Job? = null
    private var rewindJob: Job? = null

    // USB in-app notifications
    private val _usbNotification = MutableStateFlow<UsbNotification?>(null)
    val usbNotification: StateFlow<UsbNotification?> = _usbNotification.asStateFlow()
    private var usbDismissJob: Job? = null

    // Resume indicator state
    private val _isResumedSession = MutableStateFlow(false)
    val isResumedSession: StateFlow<Boolean> = _isResumedSession.asStateFlow()
    private var hasAttemptedRestore = false

    private var activePlayer: ExoPlayer? = null
    private var currentPlaylist = emptyList<MediaItemEntity>()

    // Artist groups
    val artistsList: StateFlow<List<ArtistGroup>> = musicList.map { list ->
        list.groupBy { it.artist ?: "Unknown Artist" }
            .map { (artist, tracks) ->
                ArtistGroup(
                    name = artist,
                    trackCount = tracks.size,
                    sampleTrack = tracks.first()
                )
            }
            .sortedBy { it.name.lowercase() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Album groups
    val albumsList: StateFlow<List<AlbumGroup>> = musicList.map { list ->
        list.groupBy { (it.album ?: "Unknown Album") to (it.artist ?: "Unknown Artist") }
            .map { (key, tracks) ->
                AlbumGroup(
                    title = key.first,
                    artist = key.second,
                    trackCount = tracks.size,
                    sampleTrack = tracks.first()
                )
            }
            .sortedBy { it.title.lowercase() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Folder groups
    val foldersList: StateFlow<List<FolderGroup>> = musicList.map { list ->
        list.groupBy { File(it.filePath).parent ?: "Root" }
            .map { (folderPath, tracks) ->
                FolderGroup(
                    folderPath = folderPath,
                    folderName = File(folderPath).name,
                    trackCount = tracks.size
                )
            }
            .sortedBy { it.folderName.lowercase() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private data class FilterCriteria(
        val category: MusicCategory,
        val artist: String?,
        val album: String?,
        val folder: String?,
        val volumeId: String?
    )

    // Filtered track list based on category, selection, and search query
    val filteredMusicList: StateFlow<List<MediaItemEntity>> = combine(
        musicList,
        combine(_musicCategory, _selectedArtist, _selectedAlbum, _selectedFolder, _selectedMusicVolume) { cat, art, alb, fold, vol ->
            FilterCriteria(cat, art, alb, fold, vol)
        },
        _searchQuery
    ) { tracks, criteria, query ->
        var result = tracks

        // Filter by category selection
        if (criteria.category == MusicCategory.ARTISTS && criteria.artist != null) {
            result = result.filter { (it.artist ?: "Unknown Artist") == criteria.artist }
        } else if (criteria.category == MusicCategory.ALBUMS && criteria.album != null) {
            result = result.filter { (it.album ?: "Unknown Album") == criteria.album }
        } else if (criteria.category == MusicCategory.FOLDERS && criteria.folder != null) {
            result = result.filter { (File(it.filePath).parent ?: "Root") == criteria.folder }
        } else if (criteria.category == MusicCategory.DRIVES && criteria.volumeId != null) {
            result = result.filter { it.volumeId == criteria.volumeId }
        }

        // Filter by search query
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            result = result.filter {
                it.title.lowercase().contains(q) ||
                (it.artist?.lowercase()?.contains(q) == true) ||
                (it.album?.lowercase()?.contains(q) == true)
            }
        }

        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Music drives / storage volumes grouping (Tablet internal, USB 1, USB 2, SD Card, etc.)
    val musicVolumesList: StateFlow<List<MusicVolumeGroup>> = combine(musicList, volumesList) { tracks, volumes ->
        val volMap = volumes.associateBy { it.volumeId }
        val tracksByVol = tracks.groupBy { it.volumeId }
        val allVolIds = (volumes.filter { it.isMounted }.map { it.volumeId } + tracksByVol.keys).distinct()

        var usbIndex = 0
        allVolIds.map { volId ->
            val vol = volMap[volId]
            val isInternal = volId == "internal_storage" || (vol?.rootPath?.contains("emulated") == true)
            val isSd = !isInternal && (vol?.label?.lowercase()?.contains("sd") == true || vol?.rootPath?.lowercase()?.contains("sdcard") == true)
            val count = tracksByVol[volId]?.size ?: 0
            val rootPath = vol?.rootPath ?: ""

            val displayLabel = when {
                isInternal -> appStrings.value.tabletInternalStorage
                isSd -> if (vol?.label?.isNotBlank() == true && vol.label != "USB Drive") vol.label else appStrings.value.sdCardStorage
                else -> {
                    usbIndex++
                    val base = vol?.label?.takeIf { it.isNotBlank() && it != "USB Drive" && it != "USB storage" }
                    base ?: "${appStrings.value.usbDriveLabel} $usbIndex"
                }
            }

            MusicVolumeGroup(
                volumeId = volId,
                label = displayLabel,
                isInternal = isInternal,
                isSdCard = isSd,
                trackCount = count,
                rootPath = rootPath
            )
        }.sortedWith(compareByDescending<MusicVolumeGroup> { it.isInternal }.thenByDescending { it.trackCount })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Video folder groups
    val videoFoldersList: StateFlow<List<FolderGroup>> = videoList.map { list ->
        list.groupBy { File(it.filePath).parent ?: "Root" }
            .map { (folderPath, videos) ->
                FolderGroup(
                    folderPath = folderPath,
                    folderName = File(folderPath).name.ifEmpty { "Root" },
                    trackCount = videos.size
                )
            }
            .sortedBy { it.folderName.lowercase() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Video drives/volumes grouping (Tablet internal, USB 1, USB 2, SD Card, etc.)
    val videoVolumesList: StateFlow<List<VideoVolumeGroup>> = combine(videoList, volumesList) { videos, volumes ->
        val volMap = volumes.associateBy { it.volumeId }
        val videosByVol = videos.groupBy { it.volumeId }
        val allVolIds = (volumes.filter { it.isMounted }.map { it.volumeId } + videosByVol.keys).distinct()

        var usbIndex = 0
        allVolIds.map { volId ->
            val vol = volMap[volId]
            val isInternal = volId == "internal_storage" || (vol?.rootPath?.contains("emulated") == true)
            val isSd = !isInternal && (vol?.label?.lowercase()?.contains("sd") == true || vol?.rootPath?.lowercase()?.contains("sdcard") == true)
            val count = videosByVol[volId]?.size ?: 0
            val rootPath = vol?.rootPath ?: ""

            val displayLabel = when {
                isInternal -> appStrings.value.tabletInternalStorage
                isSd -> if (vol?.label?.isNotBlank() == true && vol.label != "USB Drive") vol.label else appStrings.value.sdCardStorage
                else -> {
                    usbIndex++
                    val base = vol?.label?.takeIf { it.isNotBlank() && it != "USB Drive" && it != "USB storage" }
                    base ?: "${appStrings.value.usbDriveLabel} $usbIndex"
                }
            }

            VideoVolumeGroup(
                volumeId = volId,
                label = displayLabel,
                isInternal = isInternal,
                isSdCard = isSd,
                videoCount = count,
                rootPath = rootPath
            )
        }.sortedWith(compareByDescending<VideoVolumeGroup> { it.isInternal }.thenByDescending { it.videoCount })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Smart playlist pre-computed counts
    val videoFavoritesCount: StateFlow<Int> = combine(videoList, _favoriteVideoPaths) { videos, favs ->
        videos.count { favs.contains(it.filePath) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val videoShortClipsCount: StateFlow<Int> = videoList.map { videos ->
        videos.count { it.duration in 1..300_000L } // < 5 minutes
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val videoMoviesCount: StateFlow<Int> = videoList.map { videos ->
        videos.count { it.duration >= 600_000L } // >= 10 minutes
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private data class VideoFilterState(
        val category: VideoCategory,
        val folder: String?,
        val playlist: VideoPlaylistType?,
        val volumeId: String?,
        val favorites: Set<String>,
        val query: String
    )

    private val videoFilterCriteriaFlow = combine(
        combine(_videoCategory, _selectedVideoFolder, _selectedVideoPlaylist) { cat, fold, play ->
            Triple(cat, fold, play)
        },
        combine(_selectedVideoVolume, _favoriteVideoPaths, _videoSearchQuery) { vol, favs, q ->
            Triple(vol, favs, q)
        }
    ) { (cat, fold, play), (vol, favs, q) ->
        VideoFilterState(cat, fold, play, vol, favs, q)
    }

    // Filtered video list based on category, folder, playlist, volume, and search query
    val filteredVideoList: StateFlow<List<MediaItemEntity>> = combine(
        videoList,
        videoFilterCriteriaFlow
    ) { videos, filter ->
        var result = videos

        when (filter.category) {
            VideoCategory.FOLDERS -> {
                if (filter.folder != null) {
                    result = result.filter { (File(it.filePath).parent ?: "Root") == filter.folder }
                }
            }
            VideoCategory.PLAYLISTS -> {
                when (filter.playlist) {
                    VideoPlaylistType.FAVORITES -> {
                        result = result.filter { filter.favorites.contains(it.filePath) }
                    }
                    VideoPlaylistType.SHORT_CLIPS -> {
                        result = result.filter { it.duration in 1..300_000L }
                    }
                    VideoPlaylistType.MOVIES -> {
                        result = result.filter { it.duration >= 600_000L }
                    }
                    VideoPlaylistType.RECENT -> {
                        result = result.sortedByDescending { it.lastModified }
                    }
                    null -> {
                        // All
                    }
                }
            }
            VideoCategory.DRIVES -> {
                if (filter.volumeId != null) {
                    result = result.filter { it.volumeId == filter.volumeId }
                }
            }
            VideoCategory.ALL -> {
                // Keep all
            }
        }

        // Apply instant search filter
        if (filter.query.isNotBlank()) {
            val q = filter.query.trim().lowercase()
            result = result.filter {
                it.title.lowercase().contains(q) ||
                File(it.filePath).name.lowercase().contains(q) ||
                (File(it.filePath).parent ?: "").lowercase().contains(q)
            }
        }

        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(playing: Boolean) {
            _isPlaying.value = playing
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val mediaId = mediaItem?.mediaId ?: return
            val currentItem = currentPlaylist.find { it.filePath == mediaId }
                ?: musicList.value.find { it.filePath == mediaId }
                ?: videoList.value.find { it.filePath == mediaId }
            _currentPlayingItem.value = currentItem
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            try {
                if (playbackState == Player.STATE_READY) {
                    _playbackDuration.value = activePlayer?.duration?.coerceAtLeast(0L) ?: 0L
                } else if (playbackState == Player.STATE_ENDED) {
                    playNext()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in onPlaybackStateChanged listener", e)
            }
        }
    }

    init {
        // Track ExoPlayer reference when PlaybackService instantiates it
        viewModelScope.launch {
            PlaybackService.activePlayer.collect { player ->
                try {
                    activePlayer?.removeListener(playerListener)
                    activePlayer = player
                    player?.let { p ->
                        p.addListener(playerListener)
                        _isPlaying.value = p.isPlaying
                        _playbackDuration.value = p.duration.coerceAtLeast(0L)
                        
                        // Recover playing metadata if already active
                        p.currentMediaItem?.mediaId?.let { id ->
                            val item = musicList.value.find { it.filePath == id } ?: videoList.value.find { it.filePath == id }
                            _currentPlayingItem.value = item
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error initializing player collector", e)
                }
            }
        }

        // Smooth polling loop for playback progress & state auto-save
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                try {
                    activePlayer?.let { p ->
                        if (p.isPlaying) {
                            _playbackProgress.value = p.currentPosition
                            _playbackDuration.value = p.duration.coerceAtLeast(0L)
                            savePlaybackState()
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error polling playback progress", e)
                }
            }
        }

        // Restore playback state across app/car restarts once media is loaded
        viewModelScope.launch {
            combine(musicList, videoList) { music, videos -> music + videos }
                .collect { allMedia ->
                    if (allMedia.isNotEmpty() && !hasAttemptedRestore && _currentPlayingItem.value == null) {
                        restorePlaybackStateIfAvailable(allMedia)
                    }
                }
        }

        // Dynamic Accent Color Extractor from Album Cover Art
        viewModelScope.launch {
            combine(_currentPlayingItem, isDynamicColorEnabled) { item, dynamicEnabled ->
                if (dynamicEnabled && item != null && !item.coverArtPath.isNullOrEmpty()) {
                    DynamicColorExtractor.extractVibrantColor(item.coverArtPath)
                } else {
                    null
                }
            }.collect { extractedColor ->
                _dynamicAccentColor.value = extractedColor
            }
        }
    }

    /**
     * Saves the current playback session (file path, exact millisecond position, isVideo)
     * so that if the user leaves the app or restarts the car, it can resume seamlessly.
     */
    fun savePlaybackState() {
        val item = _currentPlayingItem.value ?: return
        val pos = activePlayer?.currentPosition ?: _playbackProgress.value
        if (pos > 0L) {
            playbackPrefs.edit()
                .putString(KEY_LAST_PATH, item.filePath)
                .putLong(KEY_LAST_POS, pos)
                .putBoolean(KEY_LAST_IS_VIDEO, item.isVideo)
                .apply()
        }
    }

    /**
     * Restores the exact media track and position where the user left off.
     */
    private fun restorePlaybackStateIfAvailable(items: List<MediaItemEntity>) {
        if (hasAttemptedRestore || _currentPlayingItem.value != null) return
        val lastPath = playbackPrefs.getString(KEY_LAST_PATH, null) ?: return
        val lastPos = playbackPrefs.getLong(KEY_LAST_POS, 0L)

        val target = items.find { it.filePath == lastPath }
        if (target != null) {
            hasAttemptedRestore = true
            _currentPlayingItem.value = target
            _playbackProgress.value = lastPos
            _playbackDuration.value = target.duration
            _isResumedSession.value = true

            // Set playlist to the item's category
            currentPlaylist = if (target.isVideo) videoList.value else musicList.value

            activePlayer?.let { player ->
                try {
                    val mediaUri = if (target.filePath.startsWith("/")) {
                        Uri.fromFile(File(target.filePath))
                    } else {
                        Uri.parse(target.filePath)
                    }
                    val metadata = MediaMetadata.Builder()
                        .setTitle(target.title)
                        .setArtist(target.artist)
                        .setAlbumTitle(target.album)
                        .build()
                    val mediaItem = MediaItem.Builder()
                        .setUri(mediaUri)
                        .setMediaId(target.filePath)
                        .setMimeType(target.mimeType)
                        .setMediaMetadata(metadata)
                        .build()

                    player.setMediaItem(mediaItem, lastPos)
                    player.prepare()
                    player.playWhenReady = false
                    Log.i(TAG, "Auto-resumed playback session for ${target.title} at ${lastPos}ms")
                } catch (e: Exception) {
                    Log.e(TAG, "Error restoring saved player session", e)
                }
            }
        }
    }

    // --- Fast Forward & Rewind (Seeking while button held) ---

    fun startFastForward() {
        if (fastForwardJob?.isActive == true) return
        stopRewind()
        _isFastForwarding.value = true
        Log.i(TAG, "Starting continuous fast forward >>")
        fastForwardJob = viewModelScope.launch {
            while (isActive) {
                val p = activePlayer
                val cur = p?.currentPosition ?: _playbackProgress.value
                val dur = _playbackDuration.value
                val step = 3000L
                val nextPos = if (dur > 0) (cur + step).coerceAtMost(dur) else (cur + step)
                seekTo(nextPos)
                delay(200L)
            }
        }
    }

    fun stopFastForward() {
        fastForwardJob?.cancel()
        fastForwardJob = null
        _isFastForwarding.value = false
        savePlaybackState()
        Log.i(TAG, "Stopped continuous fast forward.")
    }

    fun startRewind() {
        if (rewindJob?.isActive == true) return
        stopFastForward()
        _isRewinding.value = true
        Log.i(TAG, "Starting continuous rewind <<")
        rewindJob = viewModelScope.launch {
            while (isActive) {
                val p = activePlayer
                val cur = p?.currentPosition ?: _playbackProgress.value
                val step = 3000L
                val nextPos = (cur - step).coerceAtLeast(0L)
                seekTo(nextPos)
                delay(200L)
            }
        }
    }

    fun stopRewind() {
        rewindJob?.cancel()
        rewindJob = null
        _isRewinding.value = false
        savePlaybackState()
        Log.i(TAG, "Stopped continuous rewind.")
    }

    // --- In-App USB Notifications ---

    fun handleUsbEvent(event: UsbEvent) {
        val fmt = if (event.isConnected) appStrings.value.usbConnectedToast else appStrings.value.usbDisconnectedToast
        val msg = String.format(fmt, event.deviceName)
        _usbNotification.value = UsbNotification(msg, event.isConnected)

        usbDismissJob?.cancel()
        usbDismissJob = viewModelScope.launch {
            delay(4000L)
            _usbNotification.value = null
        }
    }

    fun dismissUsbNotification() {
        usbDismissJob?.cancel()
        _usbNotification.value = null
    }

    fun selectTab(index: Int) {
        _currentTab.value = index
    }

    fun setMusicCategory(category: MusicCategory) {
        _musicCategory.value = category
        // Reset sub-selections when category changes
        _selectedArtist.value = null
        _selectedAlbum.value = null
        _selectedFolder.value = null
        _selectedMusicVolume.value = null
    }

    fun selectMusicVolume(volumeId: String?) {
        _selectedMusicVolume.value = volumeId
    }

    fun selectArtist(artist: String?) {
        _selectedArtist.value = artist
    }

    fun selectAlbum(album: String?) {
        _selectedAlbum.value = album
    }

    fun selectFolder(folder: String?) {
        _selectedFolder.value = folder
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Video Category & Playlist Navigation
    fun setVideoCategory(category: VideoCategory) {
        _videoCategory.value = category
        _selectedVideoFolder.value = null
        _selectedVideoPlaylist.value = null
        _selectedVideoVolume.value = null
    }

    fun selectVideoFolder(folder: String?) {
        _selectedVideoFolder.value = folder
    }

    fun selectVideoPlaylist(playlist: VideoPlaylistType?) {
        _selectedVideoPlaylist.value = playlist
    }

    fun selectVideoVolume(volumeId: String?) {
        _selectedVideoVolume.value = volumeId
    }

    fun setVideoSearchQuery(query: String) {
        _videoSearchQuery.value = query
    }

    fun toggleVideoFavorite(filePath: String) {
        val current = _favoriteVideoPaths.value.toMutableSet()
        if (current.contains(filePath)) {
            current.remove(filePath)
        } else {
            current.add(filePath)
        }
        _favoriteVideoPaths.value = current
        playbackPrefs.edit().putStringSet("favorite_videos_set", current).apply()
    }

    fun isVideoFavorite(filePath: String): Boolean {
        return _favoriteVideoPaths.value.contains(filePath)
    }

    fun setVideoFullscreen(isFullscreen: Boolean) {
        _isVideoFullscreen.value = isFullscreen
    }

    fun setNowPlayingFullscreen(isFullscreen: Boolean) {
        _isNowPlayingFullscreen.value = isFullscreen
    }

    fun toggleRepeatMode() {
        val nextMode = when (_repeatMode.value) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        _repeatMode.value = nextMode
        activePlayer?.repeatMode = nextMode
    }

    fun toggleShuffle() {
        val nextShuffle = !_isShuffleEnabled.value
        _isShuffleEnabled.value = nextShuffle
        activePlayer?.shuffleModeEnabled = nextShuffle
    }

    fun triggerScan() {
        viewModelScope.launch {
            scanner.syncAllMountedVolumes()
        }
    }

    fun playMediaItem(item: MediaItemEntity, playlist: List<MediaItemEntity>? = null) {
        val player = activePlayer ?: return
        currentPlaylist = playlist ?: if (item.isVideo) filteredVideoList.value.ifEmpty { videoList.value } else filteredMusicList.value.ifEmpty { musicList.value }
        _currentPlayingItem.value = item

        try {
            // Set up Media3 metadata
            val metadata = MediaMetadata.Builder()
                .setTitle(item.title)
                .setArtist(item.artist ?: "فنان غير معروف")
                .setAlbumTitle(item.album ?: "ألبوم غير معروف")
                .build()

            val mediaUri = if (item.filePath.startsWith("/")) {
                Uri.fromFile(File(item.filePath))
            } else {
                Uri.parse(item.filePath)
            }

            val mediaItem = MediaItem.Builder()
                .setUri(mediaUri)
                .setMediaId(item.filePath)
                .setMimeType(item.mimeType)
                .setMediaMetadata(metadata)
                .build()

            player.stop()
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
        } catch (e: Exception) {
            Log.e(TAG, "Error playing media item: ${item.filePath}", e)
        }
    }

    fun togglePlayPause() {
        val player = activePlayer ?: return
        try {
            if (player.isPlaying) {
                player.pause()
            } else {
                if (player.playbackState == Player.STATE_IDLE || player.playbackState == Player.STATE_ENDED) {
                    player.prepare()
                }
                player.play()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in togglePlayPause", e)
        }
    }

    fun playNext() {
        val current = _currentPlayingItem.value
        val isVid = current?.isVideo == true
        val fullList = if (isVid) videoList.value else musicList.value

        // Resolve active playlist with fallback to filtered and then full list
        val candidatePlaylist = if (currentPlaylist.isNotEmpty()) {
            currentPlaylist
        } else {
            val filtered = if (isVid) filteredVideoList.value else filteredMusicList.value
            filtered.ifEmpty { fullList }
        }

        // If candidate playlist has <= 1 item but the full library has more, expand to the full library
        val effectivePlaylist = if (candidatePlaylist.size <= 1 && fullList.size > 1) {
            fullList
        } else if (current != null && candidatePlaylist.none { it.filePath == current.filePath } && fullList.any { it.filePath == current.filePath }) {
            fullList
        } else {
            candidatePlaylist.ifEmpty { fullList }
        }

        if (effectivePlaylist.isEmpty()) return

        if (current == null) {
            playMediaItem(effectivePlaylist.first(), effectivePlaylist)
            return
        }

        if (effectivePlaylist.size == 1) {
            // Truly only 1 track in entire storage
            seekTo(0L)
            activePlayer?.play()
            return
        }

        val currentIndex = effectivePlaylist.indexOfFirst { it.filePath == current.filePath }
        val nextIndex = if (_isShuffleEnabled.value) {
            var r = (0 until effectivePlaylist.size).random()
            if (r == currentIndex && effectivePlaylist.size > 1) {
                r = (currentIndex + 1) % effectivePlaylist.size
            }
            r
        } else {
            if (currentIndex == -1) 0 else (currentIndex + 1) % effectivePlaylist.size
        }

        Log.i(TAG, "playNext: transition from index $currentIndex to $nextIndex (Title: ${effectivePlaylist[nextIndex].title})")
        playMediaItem(effectivePlaylist[nextIndex], effectivePlaylist)
    }

    fun playPrevious() {
        val current = _currentPlayingItem.value
        val isVid = current?.isVideo == true
        val fullList = if (isVid) videoList.value else musicList.value

        val candidatePlaylist = if (currentPlaylist.isNotEmpty()) {
            currentPlaylist
        } else {
            val filtered = if (isVid) filteredVideoList.value else filteredMusicList.value
            filtered.ifEmpty { fullList }
        }

        val effectivePlaylist = if (candidatePlaylist.size <= 1 && fullList.size > 1) {
            fullList
        } else if (current != null && candidatePlaylist.none { it.filePath == current.filePath } && fullList.any { it.filePath == current.filePath }) {
            fullList
        } else {
            candidatePlaylist.ifEmpty { fullList }
        }

        if (effectivePlaylist.isEmpty()) return

        if (current == null) {
            playMediaItem(effectivePlaylist.first(), effectivePlaylist)
            return
        }

        if (effectivePlaylist.size == 1) {
            seekTo(0L)
            activePlayer?.play()
            return
        }

        val currentIndex = effectivePlaylist.indexOfFirst { it.filePath == current.filePath }
        val prevIndex = if (_isShuffleEnabled.value) {
            var r = (0 until effectivePlaylist.size).random()
            if (r == currentIndex && effectivePlaylist.size > 1) {
                r = if (currentIndex - 1 < 0) effectivePlaylist.size - 1 else currentIndex - 1
            }
            r
        } else {
            if (currentIndex <= 0) {
                effectivePlaylist.size - 1
            } else {
                currentIndex - 1
            }
        }

        Log.i(TAG, "playPrevious: transition from index $currentIndex to $prevIndex (Title: ${effectivePlaylist[prevIndex].title})")
        playMediaItem(effectivePlaylist[prevIndex], effectivePlaylist)
    }

    fun seekTo(positionMs: Long) {
        val maxDuration = _playbackDuration.value.coerceAtLeast(0L)
        val safePos = if (maxDuration > 0) positionMs.coerceIn(0L, maxDuration) else positionMs.coerceAtLeast(0L)
        activePlayer?.seekTo(safePos)
        _playbackProgress.value = safePos
        savePlaybackState()
    }

    override fun onCleared() {
        savePlaybackState()
        stopFastForward()
        stopRewind()
        themeManager.cleanup()
        activePlayer?.removeListener(playerListener)
        super.onCleared()
    }

    class Factory(
        private val application: Application,
        private val repository: MediaRepository,
        private val scanner: UsbMediaScanner
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MediaViewModel::class.java)) {
                return MediaViewModel(application, repository, scanner) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
