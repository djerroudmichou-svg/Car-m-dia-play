package com.example.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import androidx.annotation.OptIn
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.example.MainActivity
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Android Automotive PlaybackService:
 * - Hosts modern Media3 ExoPlayer via CarPlayerManager
 * - Integrates MediaSession for native steering wheel controls (SWC) and tablet OS media keys
 * - Uses CarForwardingPlayer so system notification and car controllers always know next/previous/play-pause are available
 * - Drives modern MediaStyle notifications with Album Art & Favorite action
 * - Protects against ANRs, memory leaks, and dirty service teardowns
 */
@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {

    companion object {
        private const val TAG = "PlaybackService"

        private val _activePlayer = MutableStateFlow<ExoPlayer?>(null)
        val activePlayer: StateFlow<ExoPlayer?> = _activePlayer.asStateFlow()

        private val _activePlayerManager = MutableStateFlow<CarPlayerManager?>(null)
        val activePlayerManager: StateFlow<CarPlayerManager?> = _activePlayerManager.asStateFlow()

        var onNextTrackRequested: (() -> Unit)? = null
        var onPrevTrackRequested: (() -> Unit)? = null
        var onPlayPauseRequested: (() -> Unit)? = null
        var onToggleFavoriteRequested: (() -> Unit)? = null

        private var instance: PlaybackService? = null

        fun updateNotificationMetadata(
            title: String,
            artist: String?,
            album: String?,
            coverArtPath: String?,
            isPlaying: Boolean,
            isFavorite: Boolean
        ) {
            instance?.notificationManager?.updateNotification(
                title = title,
                artist = artist,
                album = album,
                coverArtPath = coverArtPath,
                isPlaying = isPlaying,
                isFavorite = isFavorite
            )
        }
    }

    private var playerManager: CarPlayerManager? = null
    private var mediaSession: MediaSession? = null
    private var notificationManager: MediaNotificationManager? = null
    private var forwardingPlayer: CarForwardingPlayer? = null

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d(TAG, "PlaybackService onCreate: Initializing CarPlayerManager & MediaSession")

        // 1. Initialize CarPlayerManager with robust buffering & audio focus handling
        playerManager = CarPlayerManager(
            context = this,
            onTrackEnded = {
                mainHandler.post { onNextTrackRequested?.invoke() }
            },
            onPlayerErrorLogged = { err ->
                Log.w(TAG, "PlayerManager Logged: $err")
            }
        )

        val exoPlayer = playerManager!!.player

        // 2. Wrap ExoPlayer in CarForwardingPlayer so Android OS / SWC know Next & Prev are always capable
        forwardingPlayer = CarForwardingPlayer(
            player = exoPlayer,
            onNext = { mainHandler.post { onNextTrackRequested?.invoke() } },
            onPrev = { mainHandler.post { onPrevTrackRequested?.invoke() } },
            onPlayPause = { mainHandler.post { onPlayPauseRequested?.invoke() } }
        )

        // 3. Build Single-Top PendingIntent for Session Activity
        val activityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            activityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 4. Build MediaSession with ForwardingPlayer & Custom Callback
        mediaSession = MediaSession.Builder(this, forwardingPlayer!!)
            .setSessionActivity(pendingIntent)
            .setCallback(CustomMediaSessionCallback())
            .build()

        // 5. Initialize Modern MediaStyle Notification Manager & Promote to Foreground immediately
        notificationManager = MediaNotificationManager(this, mediaSession!!)
        notificationManager?.ensureForeground()

        // 6. Expose active player references to UI/ViewModel
        _activePlayer.value = exoPlayer
        _activePlayerManager.value = playerManager
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        notificationManager?.ensureForeground()
        val action = intent?.action
        Log.d(TAG, "PlaybackService onStartCommand action: $action")

        when (action) {
            MediaNotificationManager.ACTION_PREVIOUS -> {
                mainHandler.post { onPrevTrackRequested?.invoke() }
            }
            MediaNotificationManager.ACTION_PLAY_PAUSE -> {
                mainHandler.post { onPlayPauseRequested?.invoke() }
            }
            MediaNotificationManager.ACTION_PLAY -> {
                playerManager?.play()
            }
            MediaNotificationManager.ACTION_PAUSE -> {
                playerManager?.pause()
            }
            MediaNotificationManager.ACTION_NEXT -> {
                mainHandler.post { onNextTrackRequested?.invoke() }
            }
            MediaNotificationManager.ACTION_TOGGLE_FAVORITE -> {
                mainHandler.post { onToggleFavoriteRequested?.invoke() }
            }
            MediaNotificationManager.ACTION_STOP -> {
                playerManager?.pause()
                notificationManager?.removeNotification()
                stopSelf()
            }
            Intent.ACTION_MEDIA_BUTTON -> {
                val keyEvent = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
                if (keyEvent != null && keyEvent.action == KeyEvent.ACTION_DOWN) {
                    handleStandardMediaKey(keyEvent.keyCode)
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun handleStandardMediaKey(keyCode: Int): Boolean {
        Log.d(TAG, "handleStandardMediaKey: keyCode=$keyCode")
        when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            KeyEvent.KEYCODE_HEADSETHOOK -> {
                mainHandler.post { onPlayPauseRequested?.invoke() }
                return true
            }
            KeyEvent.KEYCODE_MEDIA_PLAY -> {
                playerManager?.play()
                return true
            }
            KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                playerManager?.pause()
                return true
            }
            KeyEvent.KEYCODE_MEDIA_NEXT -> {
                mainHandler.post { onNextTrackRequested?.invoke() }
                return true
            }
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                mainHandler.post { onPrevTrackRequested?.invoke() }
                return true
            }
            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                val pos = playerManager?.player?.currentPosition ?: 0L
                playerManager?.seekTo(pos + 10_000L)
                return true
            }
            KeyEvent.KEYCODE_MEDIA_REWIND -> {
                val pos = playerManager?.player?.currentPosition ?: 0L
                playerManager?.seekTo((pos - 10_000L).coerceAtLeast(0L))
                return true
            }
            KeyEvent.KEYCODE_MEDIA_STOP -> {
                playerManager?.pause()
                return true
            }
        }
        return false
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val isCurrentlyPlaying = playerManager?.player?.isPlaying == true
        if (!isCurrentlyPlaying) {
            Log.i(TAG, "Task removed and not playing, stopping PlaybackService")
            notificationManager?.removeNotification()
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        Log.d(TAG, "PlaybackService onDestroy called - cleaning up resources")
        instance = null
        _activePlayer.value = null
        _activePlayerManager.value = null

        notificationManager?.removeNotification()
        notificationManager = null

        mediaSession?.run {
            release()
            mediaSession = null
        }

        playerManager?.release()
        playerManager = null
        forwardingPlayer = null

        super.onDestroy()
    }

    /**
     * Custom MediaSession Callback handling standard vehicle steering wheel controls (SWC)
     * and media controller command requests.
     */
    private inner class CustomMediaSessionCallback : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                .add(SessionCommand(MediaNotificationManager.ACTION_TOGGLE_FAVORITE, android.os.Bundle.EMPTY))
                .build()

            val playerCommands = MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS.buildUpon()
                .add(Player.COMMAND_SEEK_TO_NEXT)
                .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                .add(Player.COMMAND_PLAY_PAUSE)
                .add(Player.COMMAND_STOP)
                .add(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)
                .add(Player.COMMAND_SEEK_BACK)
                .add(Player.COMMAND_SEEK_FORWARD)
                .build()

            return MediaSession.ConnectionResult.accept(sessionCommands, playerCommands)
        }

        override fun onPlayerCommandRequest(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            playerCommand: Int
        ): Int {
            when (playerCommand) {
                Player.COMMAND_SEEK_TO_NEXT,
                Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM -> {
                    mainHandler.post { onNextTrackRequested?.invoke() }
                    return SessionResult.RESULT_SUCCESS
                }
                Player.COMMAND_SEEK_TO_PREVIOUS,
                Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM -> {
                    mainHandler.post { onPrevTrackRequested?.invoke() }
                    return SessionResult.RESULT_SUCCESS
                }
                Player.COMMAND_PLAY_PAUSE -> {
                    mainHandler.post { onPlayPauseRequested?.invoke() }
                    return SessionResult.RESULT_SUCCESS
                }
                Player.COMMAND_STOP -> {
                    playerManager?.pause()
                    return SessionResult.RESULT_SUCCESS
                }
            }
            return super.onPlayerCommandRequest(session, controller, playerCommand)
        }

        override fun onMediaButtonEvent(
            session: MediaSession,
            controllerInfo: MediaSession.ControllerInfo,
            intent: Intent
        ): Boolean {
            val keyEvent = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
            if (keyEvent != null && keyEvent.action == KeyEvent.ACTION_DOWN) {
                val handled = handleStandardMediaKey(keyEvent.keyCode)
                if (handled) return true
            }
            return super.onMediaButtonEvent(session, controllerInfo, intent)
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: android.os.Bundle
        ): ListenableFuture<SessionResult> {
            if (customCommand.customAction == MediaNotificationManager.ACTION_TOGGLE_FAVORITE) {
                mainHandler.post { onToggleFavoriteRequested?.invoke() }
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
            return super.onCustomCommand(session, controller, customCommand, args)
        }
    }

    /**
     * CarForwardingPlayer ensures Android System UI, Bluetooth controllers,
     * Android Auto, and Steering Wheel Controls always recognize that Next and Previous
     * actions are supported, even if the internal ExoPlayer playlist only loads one item at a time.
     */
    private class CarForwardingPlayer(
        player: Player,
        private val onNext: () -> Unit,
        private val onPrev: () -> Unit,
        private val onPlayPause: () -> Unit
    ) : ForwardingPlayer(player) {

        override fun getAvailableCommands(): Player.Commands {
            return super.getAvailableCommands().buildUpon()
                .add(Player.COMMAND_SEEK_TO_NEXT)
                .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                .add(Player.COMMAND_PLAY_PAUSE)
                .add(Player.COMMAND_STOP)
                .add(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)
                .add(Player.COMMAND_SEEK_BACK)
                .add(Player.COMMAND_SEEK_FORWARD)
                .build()
        }

        override fun isCommandAvailable(command: Int): Boolean {
            return when (command) {
                Player.COMMAND_SEEK_TO_NEXT,
                Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
                Player.COMMAND_SEEK_TO_PREVIOUS,
                Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
                Player.COMMAND_PLAY_PAUSE,
                Player.COMMAND_STOP,
                Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM,
                Player.COMMAND_SEEK_BACK,
                Player.COMMAND_SEEK_FORWARD -> true
                else -> super.isCommandAvailable(command)
            }
        }

        override fun hasNextMediaItem(): Boolean = true
        override fun hasPreviousMediaItem(): Boolean = true

        override fun seekToNext() {
            onNext()
        }

        override fun seekToNextMediaItem() {
            onNext()
        }

        override fun seekToPrevious() {
            onPrev()
        }

        override fun seekToPreviousMediaItem() {
            onPrev()
        }
    }
}

