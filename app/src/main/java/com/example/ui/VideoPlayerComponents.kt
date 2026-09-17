package com.example.ui

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.PlaybackParameters
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import com.example.data.MediaItemEntity
import com.example.localization.LocalAppStrings
import com.example.theme.LocalCarColors
import kotlinx.coroutines.delay

/**
 * Beautiful embedded video player card shown in the Video tab side pane.
 */
@Composable
fun EmbeddedVideoPlayerCard(
    player: ExoPlayer?,
    currentPlaying: MediaItemEntity?,
    isPlaying: Boolean,
    isVideoFullscreen: Boolean,
    playbackProgress: Long,
    playbackDuration: Long,
    onPlayPauseToggle: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onFullscreenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val colors = LocalCarColors.current
    val isCurrentVideo = currentPlaying?.isVideo == true

    var totalDragX by remember { mutableFloatStateOf(0f) }
    val animatedDragX by animateFloatAsState(
        targetValue = totalDragX,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "embedded_video_drag"
    )

    val swipeExpandModifier = Modifier.pointerInput(Unit) {
        detectHorizontalDragGestures(
            onDragStart = { totalDragX = 0f },
            onHorizontalDrag = { _, dragAmount -> totalDragX += dragAmount },
            onDragEnd = {
                val threshold = 50.dp.toPx()
                if (kotlin.math.abs(totalDragX) > threshold) {
                    onFullscreenClick()
                }
                totalDragX = 0f
            },
            onDragCancel = { totalDragX = 0f }
        )
    }

    Box(
        modifier = modifier
            .offset(x = animatedDragX.dp / 8)
            .clip(RoundedCornerShape(20.dp))
            .background(if (colors.isDark) Color(0xFF070A14) else Color(0xFF0F172A))
            .then(swipeExpandModifier),
        contentAlignment = Alignment.Center
    ) {
        if (player != null && isCurrentVideo && !isVideoFullscreen) {
            // Video Surface
            VideoPlayerView(
                player = player,
                modifier = Modifier.fillMaxSize(),
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            )

            // High-Visibility Floating Controls Overlay (hidden by default to avoid screen crowding)
            var showControls by remember { mutableStateOf(false) }

            LaunchedEffect(showControls) {
                if (showControls) {
                    delay(2500)
                    showControls = false
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showControls = !showControls }
            ) {
                AnimatedVisibility(
                    visible = showControls,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xCC050711),
                                        Color.Transparent,
                                        Color(0xEE050711)
                                    )
                                )
                            )
                            .padding(12.dp)
                    ) {
                        // Top Header (Title + Fullscreen Button)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentPlaying.title,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )

                            IconButton(
                                onClick = onFullscreenClick,
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color(0x991E2544), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Fullscreen,
                                    contentDescription = strings.fullscreen,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        // Bottom Control Deck (Scrubber + Playback Actions)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                        ) {
                            // Progress Scrub Bar (safe coerceIn to avoid crashes)
                            val safeProgress = if (playbackDuration > 0) {
                                (playbackProgress.toFloat() / playbackDuration).coerceIn(0f, 1f)
                            } else 0f

                            Slider(
                                value = safeProgress,
                                onValueChange = { frac ->
                                    onSeek((frac * playbackDuration).toLong())
                                },
                                modifier = Modifier.fillMaxWidth().height(24.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = colors.accent,
                                    activeTrackColor = colors.accent,
                                    inactiveTrackColor = Color(0x66FFFFFF)
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = DateUtils.formatElapsedTime(playbackProgress / 1000),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                // Fast car-sized buttons
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Rewind 10s
                                    IconButton(
                                        onClick = { onSeek((playbackProgress - 10000).coerceAtLeast(0L)) },
                                        modifier = Modifier.size(38.dp).background(Color(0x881C2340), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Replay10,
                                            contentDescription = strings.replay10,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    // Previous
                                    IconButton(
                                        onClick = onPrevious,
                                        modifier = Modifier.size(38.dp).background(Color(0x881C2340), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.SkipPrevious,
                                            contentDescription = strings.previous,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    // Play / Pause
                                    IconButton(
                                        onClick = onPlayPauseToggle,
                                        modifier = Modifier.size(48.dp).background(colors.accent, CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                            contentDescription = if (isPlaying) strings.pause else strings.play,
                                            tint = colors.onAccent,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }

                                    // Next
                                    IconButton(
                                        onClick = onNext,
                                        modifier = Modifier.size(38.dp).background(Color(0x881C2340), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.SkipNext,
                                            contentDescription = strings.next,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    // Forward 10s
                                    IconButton(
                                        onClick = { onSeek((playbackProgress + 10000).coerceAtMost(playbackDuration)) },
                                        modifier = Modifier.size(38.dp).background(Color(0x881C2340), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Forward10,
                                            contentDescription = strings.forward10,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = DateUtils.formatElapsedTime(playbackDuration / 1000),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Idle state placeholder
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color(0xFF161E38), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Movie,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = strings.selectVideoToPlay,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strings.tabVideo,
                    color = Color(0xFF8E9BB0),
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * True 100% Immersive Fullscreen Video Player for Car Screen.
 */
@Composable
fun FullscreenVideoPlayer(
    player: ExoPlayer?,
    currentPlaying: MediaItemEntity?,
    isPlaying: Boolean,
    playbackProgress: Long,
    playbackDuration: Long,
    onPlayPauseToggle: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onExitFullscreen: () -> Unit
) {
    val strings = LocalAppStrings.current
    val colors = LocalCarColors.current
    var showOverlay by remember { mutableStateOf(true) }

    var totalDragX by remember { mutableFloatStateOf(0f) }
    val animatedDragX by animateFloatAsState(
        targetValue = totalDragX,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "fullscreen_video_drag"
    )

    val swipeExitModifier = Modifier.pointerInput(Unit) {
        detectHorizontalDragGestures(
            onDragStart = { totalDragX = 0f },
            onHorizontalDrag = { _, dragAmount -> totalDragX += dragAmount },
            onDragEnd = {
                val threshold = 70.dp.toPx()
                if (kotlin.math.abs(totalDragX) > threshold) {
                    onExitFullscreen()
                }
                totalDragX = 0f
            },
            onDragCancel = { totalDragX = 0f }
        )
    }

    // Aspect ratio toggle state (Fit vs Zoom)
    var isZoomMode by remember { mutableStateOf(false) }

    // Playback speed state
    val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
    var currentSpeedIndex by remember { mutableIntStateOf(1) } // 1.0x default

    LaunchedEffect(showOverlay, isPlaying) {
        if (showOverlay && isPlaying) {
            delay(4000)
            showOverlay = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset(x = animatedDragX.dp / 10)
            .background(Color.Black)
            .then(swipeExitModifier)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { showOverlay = !showOverlay }
    ) {
        if (player != null) {
            VideoPlayerView(
                player = player,
                modifier = Modifier.fillMaxSize(),
                resizeMode = if (isZoomMode) AspectRatioFrameLayout.RESIZE_MODE_ZOOM else AspectRatioFrameLayout.RESIZE_MODE_FIT
            )
        }

        AnimatedVisibility(
            visible = showOverlay,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xDD000000),
                                Color(0x44000000),
                                Color(0xDD000000)
                            )
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // Top Bar (Exit button, Title, Speed selector, Aspect ratio)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onExitFullscreen,
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color(0x991C223E), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FullscreenExit,
                            contentDescription = strings.exitFullscreen,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = currentPlaying?.title ?: "",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Playback Speed Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x991C223E))
                                .clickable {
                                    currentSpeedIndex = (currentSpeedIndex + 1) % speeds.size
                                    val newSpeed = speeds[currentSpeedIndex]
                                    player?.playbackParameters = PlaybackParameters(newSpeed)
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "${speeds[currentSpeedIndex]}x",
                                color = colors.accent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Aspect Ratio Toggle Button (FIT / ZOOM)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isZoomMode) colors.accent else Color(0x991C223E))
                                .clickable { isZoomMode = !isZoomMode }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (isZoomMode) "ZOOM" else "FIT",
                                color = if (isZoomMode) colors.onAccent else Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Center Giant Media Controls (Comfortable for car use)
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rewind 10 seconds
                    IconButton(
                        onClick = { onSeek((playbackProgress - 10000).coerceAtLeast(0L)) },
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color(0x991C223E), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Replay10,
                            contentDescription = strings.replay10,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Previous Track
                    IconButton(
                        onClick = onPrevious,
                        modifier = Modifier
                            .size(58.dp)
                            .background(Color(0xAA1C223E), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipPrevious,
                            contentDescription = strings.previous,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Giant Play/Pause (76dp)
                    IconButton(
                        onClick = onPlayPauseToggle,
                        modifier = Modifier
                            .size(76.dp)
                            .background(colors.accent, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) strings.pause else strings.play,
                            tint = colors.onAccent,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    // Next Track
                    IconButton(
                        onClick = onNext,
                        modifier = Modifier
                            .size(58.dp)
                            .background(Color(0xAA1C223E), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipNext,
                            contentDescription = strings.next,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Forward 10 seconds
                    IconButton(
                        onClick = { onSeek((playbackProgress + 10000).coerceAtMost(playbackDuration)) },
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color(0x991C223E), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Forward10,
                            contentDescription = strings.forward10,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Bottom Scrub Deck
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {
                    val safeProgress = if (playbackDuration > 0) {
                        (playbackProgress.toFloat() / playbackDuration).coerceIn(0f, 1f)
                    } else 0f

                    Slider(
                        value = safeProgress,
                        onValueChange = { frac ->
                            onSeek((frac * playbackDuration).toLong())
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = colors.accent,
                            activeTrackColor = colors.accent,
                            inactiveTrackColor = Color(0x66FFFFFF)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = DateUtils.formatElapsedTime(playbackProgress / 1000),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = DateUtils.formatElapsedTime(playbackDuration / 1000),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
