package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaStyleNotificationHelper
import com.example.MainActivity
import com.example.R
import com.example.localization.LocalizationManager
import java.io.File

/**
 * Modern MediaNotificationManager using Android MediaStyle notifications:
 * - High-resolution Album Art rendering with safe bitmap memory decoding
 * - Interactive standard controls: Previous, Play/Pause, Next
 * - Spotify-like custom "Favorite / Like" toggle action directly in the notification bar
 * - Clean foreground service lifecycle handling (swipe-to-dismiss when paused, startForeground when playing)
 */
@OptIn(UnstableApi::class)
class MediaNotificationManager(
    private val service: Service,
    private val mediaSession: MediaSession
) {
    companion object {
        private const val TAG = "MediaNotificationMgr"
        const val NOTIFICATION_ID = 2001
        const val CHANNEL_ID = "car_media_playback_channel_v2"

        const val ACTION_PLAY = "com.example.service.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.service.ACTION_PAUSE"
        const val ACTION_PLAY_PAUSE = "com.example.service.ACTION_PLAY_PAUSE"
        const val ACTION_PREVIOUS = "com.example.service.ACTION_PREVIOUS"
        const val ACTION_NEXT = "com.example.service.ACTION_NEXT"
        const val ACTION_TOGGLE_FAVORITE = "com.example.service.ACTION_TOGGLE_FAVORITE"
        const val ACTION_STOP = "com.example.service.ACTION_STOP"
    }

    private val localizationManager = LocalizationManager(service)

    private val notificationManager =
        service.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    private var isForegroundService = false

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val strings = localizationManager.strings
            val name = strings.appName
            val descriptionText = strings.appName
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Builds and displays an updated MediaStyle notification.
     */
    fun updateNotification(
        title: String,
        artist: String?,
        album: String?,
        coverArtPath: String?,
        isPlaying: Boolean,
        isFavorite: Boolean
    ) {
        try {
            val strings = localizationManager.strings

            // Update MediaSession custom layout for Android 13+ System UI & Auto Media Controller
            try {
                val favIcon = if (isFavorite) R.drawable.ic_notification_fav_filled else R.drawable.ic_notification_fav_border
                val favTitle = if (isFavorite) strings.removeFromFavorites else strings.addToFavorites
                val favButton = androidx.media3.session.CommandButton.Builder()
                    .setDisplayName(favTitle)
                    .setIconResId(favIcon)
                    .setSessionCommand(androidx.media3.session.SessionCommand(ACTION_TOGGLE_FAVORITE, android.os.Bundle.EMPTY))
                    .build()
                mediaSession.setCustomLayout(listOf(favButton))
            } catch (e: Exception) {
                Log.w(TAG, "Failed setting mediaSession custom layout", e)
            }

            val notification = buildNotification(
                title = title,
                artist = artist ?: strings.unknownArtist,
                album = album,
                coverArtPath = coverArtPath,
                isPlaying = isPlaying,
                isFavorite = isFavorite
            )

            if (!isForegroundService) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        service.startForeground(
                            NOTIFICATION_ID,
                            notification,
                            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                        )
                    } else {
                        service.startForeground(NOTIFICATION_ID, notification)
                    }
                    isForegroundService = true
                } catch (e: Exception) {
                    Log.w(TAG, "Failed startForeground. Posting standard notification instead.", e)
                    notificationManager?.notify(NOTIFICATION_ID, notification)
                }
            } else {
                notificationManager?.notify(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating media notification", e)
        }
    }

    /**
     * Promotes the service to Foreground immediately upon creation or onStartCommand
     * to satisfy Android 8.0+ Context.startForegroundService() contract within 5 seconds.
     */
    fun ensureForeground() {
        if (!isForegroundService) {
            try {
                val strings = localizationManager.strings
                val notification = buildNotification(
                    title = service.getString(R.string.app_name),
                    artist = strings.selectMediaToPlay,
                    album = null,
                    coverArtPath = null,
                    isPlaying = false,
                    isFavorite = false
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    service.startForeground(
                        NOTIFICATION_ID,
                        notification,
                        android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                    )
                } else {
                    service.startForeground(NOTIFICATION_ID, notification)
                }
                isForegroundService = true
                Log.d(TAG, "ensureForeground: PlaybackService successfully promoted to foreground")
            } catch (e: Exception) {
                Log.e(TAG, "Error ensuring foreground service status", e)
            }
        }
    }

    private fun buildNotification(
        title: String,
        artist: String,
        album: String?,
        coverArtPath: String?,
        isPlaying: Boolean,
        isFavorite: Boolean
    ): Notification {
        val safeImmutableFlag = if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0

        // 1. Content Intent (Launches MainActivity single-top)
        val activityIntent = Intent(service, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            service,
            0,
            activityIntent,
            safeImmutableFlag or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 2. Control Actions Pending Intents
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= 31) {
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val prevPendingIntent = PendingIntent.getService(
            service,
            1,
            Intent(service, PlaybackService::class.java).setAction(ACTION_PREVIOUS),
            pendingIntentFlags
        )

        val playPausePendingIntent = PendingIntent.getService(
            service,
            2,
            Intent(service, PlaybackService::class.java).setAction(ACTION_PLAY_PAUSE),
            pendingIntentFlags
        )

        val nextPendingIntent = PendingIntent.getService(
            service,
            3,
            Intent(service, PlaybackService::class.java).setAction(ACTION_NEXT),
            pendingIntentFlags
        )

        val favoritePendingIntent = PendingIntent.getService(
            service,
            4,
            Intent(service, PlaybackService::class.java).setAction(ACTION_TOGGLE_FAVORITE),
            pendingIntentFlags
        )

        val stopPendingIntent = PendingIntent.getService(
            service,
            5,
            Intent(service, PlaybackService::class.java).setAction(ACTION_STOP),
            pendingIntentFlags
        )

        // 3. Decode Large Album Art safely
        val albumArtBitmap = loadAlbumArtBitmapSafely(coverArtPath)

        // 4. MediaStyle layout
        val mediaStyle = MediaStyleNotificationHelper.MediaStyle(mediaSession)
            .setShowActionsInCompactView(0, 1, 2) // Prev, Play/Pause, Next in compact car view

        val strings = localizationManager.strings
        val playPauseIcon = if (isPlaying) R.drawable.ic_notification_pause else R.drawable.ic_notification_play
        val playPauseTitle = if (isPlaying) strings.pause else strings.play

        val favIcon = if (isFavorite) R.drawable.ic_notification_fav_filled else R.drawable.ic_notification_fav_border
        val favTitle = if (isFavorite) strings.removeFromFavorites else strings.addToFavorites

        return NotificationCompat.Builder(service, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_car_media)
            .setContentTitle(title)
            .setContentText(artist)
            .setSubText(album)
            .setLargeIcon(albumArtBitmap)
            .setContentIntent(contentPendingIntent)
            .setDeleteIntent(stopPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .setStyle(mediaStyle)
            // Actions:
            // 0: Previous
            .addAction(R.drawable.ic_notification_prev, strings.previous, prevPendingIntent)
            // 1: Play / Pause
            .addAction(playPauseIcon, playPauseTitle, playPausePendingIntent)
            // 2: Next
            .addAction(R.drawable.ic_notification_next, strings.next, nextPendingIntent)
            // 3: Custom Favorite / Like toggle
            .addAction(favIcon, favTitle, favoritePendingIntent)
            .build()
    }

    private fun loadAlbumArtBitmapSafely(path: String?): Bitmap? {
        if (path.isNullOrBlank()) return null
        return try {
            val file = File(path)
            if (!file.exists() || !file.canRead()) return null

            // Downsample image to avoid memory exhaustion (512x512 target)
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, boundsOptions)

            var sampleSize = 1
            val targetSize = 512
            if (boundsOptions.outHeight > targetSize || boundsOptions.outWidth > targetSize) {
                val halfHeight = boundsOptions.outHeight / 2
                val halfWidth = boundsOptions.outWidth / 2
                while ((halfHeight / sampleSize) >= targetSize && (halfWidth / sampleSize) >= targetSize) {
                    sampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to decode album art from path: $path", e)
            null
        }
    }

    fun removeNotification() {
        try {
            if (isForegroundService) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    service.stopForeground(Service.STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    service.stopForeground(true)
                }
                isForegroundService = false
            }
            notificationManager?.cancel(NOTIFICATION_ID)
        } catch (e: Exception) {
            Log.e(TAG, "Error removing media notification", e)
        }
    }
}
