package com.example.service

import android.content.Context
import android.media.AudioAttributes as AndroidAudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * Modern Media3 ExoPlayer Manager designed for vehicle infotainment systems.
 * Provides:
 * - Robust network resilience (automatic retries, reconnect on network recovery, preserved queue/position)
 * - Automotive audio focus handling (ducking on navigation voice prompts, pausing on calls, auto-resuming)
 * - High-capacity buffer management (DefaultLoadControl tuned for vehicle connectivity drops)
 * - Safe exception handling across all playback operations
 */
@OptIn(UnstableApi::class)
class CarPlayerManager(
    private val context: Context,
    private val onTrackEnded: () -> Unit = {},
    private val onPlayerErrorLogged: (String) -> Unit = {}
) {
    companion object {
        private const val TAG = "CarPlayerManager"
        private const val DUCK_VOLUME = 0.2f
        private const val NORMAL_VOLUME = 1.0f
        private const val MAX_RETRY_ATTEMPTS = 5
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val mainHandler = Handler(Looper.getMainLooper())

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private var audioFocusRequest: AudioFocusRequest? = null
    private var hasAudioFocus = false
    private var resumeOnFocusGain = false
    private var isDucked = false

    private var retryCount = 0
    private var pendingRetryRunnable: Runnable? = null
    private var lastFailedMediaItem: MediaItem? = null
    private var lastFailedPosition: Long = 0L
    private var isNetworkStalled = false

    val player: ExoPlayer

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _currentMediaItem = MutableStateFlow<MediaItem?>(null)
    val currentMediaItem: StateFlow<MediaItem?> = _currentMediaItem.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _playbackState = MutableStateFlow(Player.STATE_IDLE)
    val playbackState: StateFlow<Int> = _playbackState.asStateFlow()

    // Network callback to seamlessly recover when vehicle re-enters cellular or Wi-Fi coverage
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.i(TAG, "Network connection became available - checking for stalled streams")
            serviceScope.launch {
                if (isNetworkStalled || pendingRetryRunnable != null) {
                    retryPlaybackImmediately("Network recovered")
                }
            }
        }

        override fun onLost(network: Network) {
            Log.w(TAG, "Network connection lost during playback session")
        }
    }

    // Audio Focus Listener for navigation ducking and call pauses
    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        serviceScope.launch {
            handleAudioFocusChange(focusChange)
        }
    }

    init {
        // 1. Build high-capacity buffer control for cellular/tether drops
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs = */ 30_000,
                /* maxBufferMs = */ 120_000,
                /* bufferForPlaybackMs = */ 2_500,
                /* bufferForPlaybackAfterRebufferMs = */ 5_000
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .setBackBuffer(30_000, true)
            .build()

        // 2. HTTP Data Source with vehicle-optimized timeouts & redirects
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setConnectTimeoutMs(15_000)
            .setReadTimeoutMs(15_000)
            .setAllowCrossProtocolRedirects(true)

        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(DefaultDataSource.Factory(context, httpDataSourceFactory))
            .setLoadErrorHandlingPolicy(DefaultLoadErrorHandlingPolicy(MAX_RETRY_ATTEMPTS))

        // 3. Audio Attributes for Automotive Media Playback
        val mediaAudioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        // 4. Instantiate ExoPlayer
        player = ExoPlayer.Builder(context)
            .setAudioAttributes(mediaAudioAttributes, false) // We manage audio focus manually for precision ducking
            .setHandleAudioBecomingNoisy(true)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(mediaSourceFactory)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()
            .apply {
                playWhenReady = false
                repeatMode = Player.REPEAT_MODE_OFF
            }

        // 5. Attach Player Listener
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                _isPlaying.value = playing
                if (playing) {
                    retryCount = 0
                    isNetworkStalled = false
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                _playbackState.value = state
                _isBuffering.value = (state == Player.STATE_BUFFERING)

                try {
                    when (state) {
                        Player.STATE_READY -> {
                            _duration.value = player.duration.coerceAtLeast(0L)
                            _currentPosition.value = player.currentPosition.coerceAtLeast(0L)
                            retryCount = 0
                            isNetworkStalled = false
                        }
                        Player.STATE_ENDED -> {
                            onTrackEnded()
                        }
                        Player.STATE_IDLE -> {
                            // Idle state
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error handling playback state change: $state", e)
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                _currentMediaItem.value = mediaItem
                _duration.value = player.duration.coerceAtLeast(0L)
                _currentPosition.value = player.currentPosition.coerceAtLeast(0L)
            }

            override fun onPlayerError(error: PlaybackException) {
                handlePlayerError(error)
            }
        })

        // 6. Register Network Availability Callback
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager?.registerNetworkCallback(request, networkCallback)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to register network callback", e)
        }
    }

    // ==========================================
    // AUDIO FOCUS MANAGEMENT (Ducking & Pausing)
    // ==========================================

    private fun requestAudioFocus(): Boolean {
        val am = audioManager ?: return true
        if (hasAudioFocus) return true

        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AndroidAudioAttributes.Builder()
                .setUsage(AndroidAudioAttributes.USAGE_MEDIA)
                .setContentType(AndroidAudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(audioFocusChangeListener, mainHandler)
                .build()
            audioFocusRequest = request
            am.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            am.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }

        hasAudioFocus = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
        return hasAudioFocus
    }

    private fun abandonAudioFocus() {
        val am = audioManager ?: return
        if (!hasAudioFocus) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { am.abandonAudioFocusRequest(it) }
            } else {
                @Suppress("DEPRECATION")
                am.abandonAudioFocus(audioFocusChangeListener)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error abandoning audio focus", e)
        } finally {
            hasAudioFocus = false
            resumeOnFocusGain = false
            isDucked = false
        }
    }

    private fun handleAudioFocusChange(focusChange: Int) {
        Log.i(TAG, "Audio focus changed: $focusChange")
        try {
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    hasAudioFocus = true
                    if (isDucked) {
                        // Restore volume smoothly after navigation prompt finishes
                        player.volume = NORMAL_VOLUME
                        isDucked = false
                        Log.i(TAG, "Audio focus restored - unducked volume to $NORMAL_VOLUME")
                    }
                    if (resumeOnFocusGain) {
                        resumeOnFocusGain = false
                        player.play()
                        Log.i(TAG, "Audio focus restored - resumed playback")
                    }
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    // Navigation instructions (e.g. Waze, Google Maps) - Duck volume without pausing
                    isDucked = true
                    player.volume = DUCK_VOLUME
                    Log.i(TAG, "Audio focus ducked to $DUCK_VOLUME for navigation prompt")
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    // Short interruption (incoming phone call, voice assistant) - Pause and auto-resume
                    if (player.isPlaying) {
                        resumeOnFocusGain = true
                        player.pause()
                        Log.i(TAG, "Audio focus lost transiently - paused, will resume after call")
                    }
                }
                AudioManager.AUDIOFOCUS_LOSS -> {
                    // Permanent loss (another player app started) - Stop/pause without auto-resuming
                    hasAudioFocus = false
                    resumeOnFocusGain = false
                    if (player.isPlaying) {
                        player.pause()
                    }
                    Log.i(TAG, "Audio focus lost permanently - paused")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling audio focus change", e)
        }
    }

    // ==========================================
    // NETWORK RESILIENCE & ERROR RECOVERY
    // ==========================================

    private fun handlePlayerError(error: PlaybackException) {
        val currentItem = player.currentMediaItem
        val currentPos = player.currentPosition.coerceAtLeast(0L)
        val errorMessage = error.localizedMessage ?: error.errorCodeName
        Log.e(TAG, "Player Error [${error.errorCode}]: $errorMessage", error)
        onPlayerErrorLogged("Error [${error.errorCode}]: $errorMessage")

        // Save position and item for recovery
        if (currentItem != null) {
            lastFailedMediaItem = currentItem
            lastFailedPosition = currentPos
        }

        val uriScheme = currentItem?.localConfiguration?.uri?.scheme
        val uriPath = currentItem?.localConfiguration?.uri?.path
        val isLocalFile = uriScheme == "file" || (uriPath != null && uriPath.startsWith("/"))

        val isLocalFileMissing = isLocalFile ||
            error.errorCode == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND ||
            error.errorCode == PlaybackException.ERROR_CODE_IO_NO_PERMISSION ||
            error.cause is java.io.FileNotFoundException

        val isNetworkError = !isLocalFileMissing && when (error.errorCode) {
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT,
            PlaybackException.ERROR_CODE_IO_INVALID_HTTP_CONTENT_TYPE,
            PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS,
            PlaybackException.ERROR_CODE_IO_UNSPECIFIED -> true
            else -> error.cause is java.io.IOException
        }

        if (isNetworkError && retryCount < MAX_RETRY_ATTEMPTS) {
            isNetworkStalled = true
            retryCount++
            val backoffMs = (1000L * (1 shl retryCount)).coerceAtMost(10_000L) // 2s, 4s, 8s...
            Log.w(TAG, "Network stream interrupted. Scheduling auto-reconnect attempt $retryCount/$MAX_RETRY_ATTEMPTS in ${backoffMs}ms")

            pendingRetryRunnable?.let { mainHandler.removeCallbacks(it) }
            val runnable = Runnable {
                retryPlaybackImmediately("Exponential backoff retry #$retryCount")
            }
            pendingRetryRunnable = runnable
            mainHandler.postDelayed(runnable, backoffMs)
        } else {
            Log.e(TAG, "Local file error or max retries reached ($retryCount). Pausing safely.")
            isNetworkStalled = false
            try {
                player.pause()
            } catch (e: Exception) {
                Log.w(TAG, "Error pausing player after fatal error", e)
            }
        }
    }

    private fun retryPlaybackImmediately(reason: String) {
        pendingRetryRunnable?.let { mainHandler.removeCallbacks(it) }
        pendingRetryRunnable = null

        val itemToRetry = lastFailedMediaItem ?: player.currentMediaItem ?: return
        val posToResume = if (lastFailedPosition > 0L) lastFailedPosition else player.currentPosition

        Log.i(TAG, "Executing immediate reconnect ($reason) for ${itemToRetry.mediaId} at ${posToResume}ms")
        try {
            player.stop()
            player.clearMediaItems()
            player.setMediaItem(itemToRetry, posToResume)
            player.prepare()
            player.play()
            isNetworkStalled = false
        } catch (e: Exception) {
            Log.e(TAG, "Error executing immediate playback retry", e)
        }
    }

    // ==========================================
    // PUBLIC CONTROL API (WITH TRY-CATCH)
    // ==========================================

    fun play() {
        try {
            requestAudioFocus()
            val hasError = player.playerError != null
            val isIdleOrEnded = player.playbackState == Player.STATE_IDLE || player.playbackState == Player.STATE_ENDED
            val currentItem = player.currentMediaItem ?: lastFailedMediaItem

            if (hasError || isIdleOrEnded) {
                if (currentItem != null) {
                    val pos = if (lastFailedPosition > 0L) lastFailedPosition else player.currentPosition.coerceAtLeast(0L)
                    val uri = currentItem.localConfiguration?.uri
                    if (uri != null && (uri.scheme == "file" || uri.path?.startsWith("/") == true)) {
                        val filePath = uri.path ?: ""
                        val file = java.io.File(filePath)
                        if (!file.exists()) {
                            Log.w(TAG, "Cannot resume local file playback: file does not exist: $filePath")
                            return
                        }
                    }
                    Log.i(TAG, "Re-building ExoPlayer DataSource for ${currentItem.mediaId} at ${pos}ms")
                    player.stop()
                    player.clearMediaItems()
                    player.setMediaItem(currentItem, pos)
                    player.prepare()
                    lastFailedMediaItem = null
                    lastFailedPosition = 0L
                } else {
                    player.prepare()
                }
            }
            player.play()
        } catch (e: Exception) {
            Log.e(TAG, "Error invoking play()", e)
        }
    }

    fun pause() {
        try {
            player.pause()
        } catch (e: Exception) {
            Log.e(TAG, "Error invoking pause()", e)
        }
    }

    fun togglePlayPause() {
        try {
            if (player.isPlaying) {
                pause()
            } else {
                play()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error invoking togglePlayPause()", e)
        }
    }

    fun seekTo(positionMs: Long) {
        try {
            val dur = player.duration.coerceAtLeast(0L)
            val target = if (dur > 0L) positionMs.coerceIn(0L, dur) else positionMs.coerceAtLeast(0L)
            player.seekTo(target)
            _currentPosition.value = target
        } catch (e: Exception) {
            Log.e(TAG, "Error invoking seekTo($positionMs)", e)
        }
    }

    fun playMediaItem(
        mediaUri: Uri,
        mediaId: String,
        mimeType: String?,
        title: String,
        artist: String?,
        album: String?,
        startPositionMs: Long = 0L,
        artworkUri: Uri? = null
    ) {
        try {
            requestAudioFocus()
            retryCount = 0
            isNetworkStalled = false
            pendingRetryRunnable?.let { mainHandler.removeCallbacks(it) }

            val metadataBuilder = androidx.media3.common.MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist ?: "Unknown Artist")
                .setAlbumTitle(album ?: "Unknown Album")

            if (artworkUri != null) {
                metadataBuilder.setArtworkUri(artworkUri)
            }

            val mediaItem = MediaItem.Builder()
                .setUri(mediaUri)
                .setMediaId(mediaId)
                .setMimeType(mimeType)
                .setMediaMetadata(metadataBuilder.build())
                .build()

            player.stop()
            player.clearMediaItems()
            if (startPositionMs > 0L) {
                player.setMediaItem(mediaItem, startPositionMs)
            } else {
                player.setMediaItem(mediaItem)
            }
            player.prepare()
            player.play()

            _currentMediaItem.value = mediaItem
            _currentPosition.value = startPositionMs
        } catch (e: Exception) {
            Log.e(TAG, "Error playing media item: $mediaId", e)
        }
    }

    fun release() {
        try {
            pendingRetryRunnable?.let { mainHandler.removeCallbacks(it) }
            try {
                connectivityManager?.unregisterNetworkCallback(networkCallback)
            } catch (e: Exception) {
                Log.w(TAG, "Error unregistering network callback", e)
            }
            abandonAudioFocus()
            player.stop()
            player.release()
            serviceScope.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing CarPlayerManager", e)
        }
    }
}
