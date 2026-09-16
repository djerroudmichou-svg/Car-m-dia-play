package com.example.ui

import android.text.format.DateUtils
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import com.example.data.MediaItemEntity
import com.example.localization.LocalAppStrings
import com.example.theme.LocalCarColors
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

/**
 * True Fullscreen Audio Player filling the automotive screen with huge cover art,
 * dynamic audio spectrum visualizer bars, and giant touch controls.
 */
@Composable
fun FullscreenAudioPlayer(
    item: MediaItemEntity,
    isPlaying: Boolean,
    playbackProgress: Long,
    playbackDuration: Long,
    repeatMode: Int,
    isShuffleEnabled: Boolean,
    isFastForwarding: Boolean = false,
    isRewinding: Boolean = false,
    onPlayPauseToggle: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onFastForwardStart: () -> Unit = {},
    onFastForwardEnd: () -> Unit = {},
    onRewindStart: () -> Unit = {},
    onRewindEnd: () -> Unit = {},
    onSeek: (Long) -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleShuffle: () -> Unit,
    onMinimize: () -> Unit
) {
    val strings = LocalAppStrings.current
    val colors = LocalCarColors.current

    // Toggle between Cover Art and Audio Equalizer Visualizer Bars
    var showVisualizerBars by rememberSaveable { mutableStateOf(false) }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            colors.background,
            colors.surfaceSecondary.copy(alpha = 0.8f),
            colors.background
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .padding(18.dp)
    ) {
        // Top Minimize & Mode Switch Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onMinimize,
                modifier = Modifier
                    .size(48.dp)
                    .background(colors.surfaceSecondary, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.FullscreenExit,
                    contentDescription = strings.minimize,
                    tint = colors.textPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Text(
                text = strings.audioPlayerHeader,
                color = colors.accent,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            // Switch button between Album Cover and Visualizer Bars
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surfaceSecondary)
                    .border(1.5.dp, colors.accent.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .clickable { showVisualizerBars = !showVisualizerBars }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (showVisualizerBars) Icons.Filled.GraphicEq else Icons.Filled.Album,
                    contentDescription = strings.toggleVisualizerTooltip,
                    tint = colors.accent,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = if (showVisualizerBars) strings.visualizerModeBars else strings.visualizerModeCover,
                    color = colors.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Center Content: Giant Cover Art / Equalizer Bars + Track Details
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 54.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Giant Visual Area: Click to toggle between Cover and Equalizer Bars
            Box(
                modifier = Modifier
                    .sizeIn(minWidth = 160.dp, maxWidth = 230.dp, minHeight = 160.dp, maxHeight = 230.dp)
                    .fillMaxHeight(0.85f)
                    .aspectRatio(1f)
                    .shadow(16.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .border(3.dp, colors.accent.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        showVisualizerBars = !showVisualizerBars
                    },
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = showVisualizerBars,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    },
                    label = "album_or_visualizer"
                ) { isBarsMode ->
                    if (isBarsMode) {
                        CarAudioSpectrumVisualizer(
                            isPlaying = isPlaying,
                            accentColor = colors.accent,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        CoverArtImage(
                            coverArtPath = item.coverArtPath,
                            title = item.title,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Information & Controls
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = item.title,
                    color = colors.textPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${item.artist ?: strings.unknownArtist} — ${item.album ?: strings.unknownAlbum}",
                    color = colors.textSecondary,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Large Scrub Slider
                val progressFraction = if (playbackDuration > 0) {
                    (playbackProgress.toFloat() / playbackDuration).coerceIn(0f, 1f)
                } else 0f

                Slider(
                    value = progressFraction,
                    onValueChange = { frac ->
                        onSeek((frac * playbackDuration).toLong())
                    },
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
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (isFastForwarding || isRewinding) {
                        Text(
                            text = if (isFastForwarding) ">> ${strings.fastForwarding}" else "<< ${strings.rewinding}",
                            color = colors.accent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "\u200E${DateUtils.formatElapsedTime(playbackDuration / 1000)}\u200E",
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Massive Touch Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onToggleShuffle,
                        modifier = Modifier.size(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (isShuffleEnabled) colors.accent else colors.textSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Previous / Rewind on Hold
                    CarSeekButton(
                        icon = Icons.Filled.SkipPrevious,
                        contentDescription = strings.previous,
                        modifier = Modifier.size(62.dp),
                        iconSize = 34.dp,
                        isSeeking = isRewinding,
                        onClick = onPrevious,
                        onHoldStart = onRewindStart,
                        onHoldEnd = onRewindEnd
                    )

                    // Main Giant Play / Pause Button
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

                    // Next / Fast-Forward on Hold
                    CarSeekButton(
                        icon = Icons.Filled.SkipNext,
                        contentDescription = strings.next,
                        modifier = Modifier.size(62.dp),
                        iconSize = 34.dp,
                        isSeeking = isFastForwarding,
                        onClick = onNext,
                        onHoldStart = onFastForwardStart,
                        onHoldEnd = onFastForwardEnd
                    )

                    IconButton(
                        onClick = onToggleRepeat,
                        modifier = Modifier.size(50.dp)
                    ) {
                        Icon(
                            imageVector = if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                            contentDescription = "Repeat",
                            tint = if (repeatMode != Player.REPEAT_MODE_OFF) colors.accent else colors.textSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * High-End Automotive Neon Audio Equalizer Bars Visualizer.
 * Renders 16 dynamic spectrum frequency bars with peak hold caps,
 * active harmonic pulsing, and smooth idle decay when paused.
 */
@Composable
fun CarAudioSpectrumVisualizer(
    isPlaying: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val barCount = 16
    val barFractions = remember { mutableStateListOf(*Array(barCount) { 0.12f }) }
    val peakFractions = remember { mutableStateListOf(*Array(barCount) { 0.14f }) }

    // Rhythmic live spectrum simulation with realistic frequency groupings (bass, mid, treble)
    LaunchedEffect(isPlaying) {
        var step = 0
        while (true) {
            if (isPlaying) {
                step++
                for (i in 0 until barCount) {
                    val frequencyWeight = when {
                        i < 4 -> 0.45f + 0.50f * sin(step * 0.25f + i * 0.4f).coerceAtLeast(0f) // Bass pulse
                        i < 11 -> 0.35f + 0.55f * sin(step * 0.40f + i * 0.6f).coerceAtLeast(0f) // Mids rhythm
                        else -> 0.25f + 0.65f * sin(step * 0.60f + i * 0.9f).coerceAtLeast(0f) // Treble shimmer
                    }
                    val jitter = Random.nextFloat() * 0.22f
                    val target = (frequencyWeight + jitter).coerceIn(0.12f, 0.98f)
                    barFractions[i] = target

                    // Update peaks
                    if (target > peakFractions[i]) {
                        peakFractions[i] = target
                    } else {
                        peakFractions[i] = (peakFractions[i] - 0.04f).coerceAtLeast(target)
                    }
                }
                delay(75)
            } else {
                // Calm smooth idle baseline
                for (i in 0 until barCount) {
                    barFractions[i] = (barFractions[i] * 0.85f).coerceAtLeast(0.08f)
                    peakFractions[i] = (peakFractions[i] * 0.85f).coerceAtLeast(0.10f)
                }
                delay(120)
            }
        }
    }

    Box(
        modifier = modifier
            .background(Color(0xFF0D1117))
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val totalWidth = size.width
            val totalHeight = size.height
            val spacing = 6.dp.toPx()
            val availableWidth = totalWidth - (spacing * (barCount - 1))
            val barWidth = availableWidth / barCount
            val cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)

            for (i in 0 until barCount) {
                val left = i * (barWidth + spacing)
                val currentFraction = barFractions[i]
                val barHeight = (totalHeight * currentFraction).coerceAtLeast(barWidth)
                val top = totalHeight - barHeight

                // Vibrant gradient: Electric Cyan/Green at base -> Accent -> Vivid Coral/Violet at peaks
                val barBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFF3366),
                        accentColor,
                        Color(0xFF00E5FF)
                    ),
                    startY = top,
                    endY = totalHeight
                )

                // Draw Bar
                drawRoundRect(
                    brush = barBrush,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = cornerRadius
                )

                // Peak Indicator Cap (classic car stereo spectrum visualizer style)
                val peakTop = (totalHeight - (totalHeight * peakFractions[i])).coerceIn(0f, totalHeight - 6.dp.toPx())
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.9f),
                    topLeft = Offset(left, peakTop),
                    size = Size(barWidth, 3.dp.toPx()),
                    cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
                )
            }
        }

        // Status / Mode indication at the bottom of the visualizer
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.GraphicEq,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = if (isPlaying) "LIVE EQUALIZER" else "PAUSED",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

