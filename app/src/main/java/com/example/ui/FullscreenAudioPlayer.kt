package com.example.ui

import android.text.format.DateUtils
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.example.data.MediaItemEntity
import com.example.localization.LocalAppStrings
import com.example.theme.LocalCarColors
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
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
    showVisualizerBars: Boolean,
    onToggleVisualizerBars: (Boolean) -> Unit,
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
    onMinimize: () -> Unit,
    showClock: Boolean = true,
    showDate: Boolean = true,
    showTemp: Boolean = true,
    showSpeed: Boolean = true,
    carSpeed: Int = 0,
    ambientTemp: Int = 24
) {
    val strings = LocalAppStrings.current
    val colors = LocalCarColors.current

    val currentTime = remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime.longValue = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormatter = remember { SimpleDateFormat("EEE, dd MMM", Locale.getDefault()) }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            colors.background.copy(alpha = 0.95f),
            colors.surfaceSecondary.copy(alpha = 0.7f),
            colors.background.copy(alpha = 0.95f)
        )
    )

    var totalDragX by remember { mutableFloatStateOf(0f) }
    val animatedDragX by animateFloatAsState(
        targetValue = totalDragX,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "fullscreen_audio_drag"
    )

    val swipeMinimizeModifier = Modifier.pointerInput(Unit) {
        detectHorizontalDragGestures(
            onDragStart = { totalDragX = 0f },
            onHorizontalDrag = { _, dragAmount -> totalDragX += dragAmount },
            onDragEnd = {
                val threshold = 70.dp.toPx()
                if (kotlin.math.abs(totalDragX) > threshold) {
                    onMinimize()
                }
                totalDragX = 0f
            },
            onDragCancel = { totalDragX = 0f }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset(x = animatedDragX.dp / 10)
            .background(colors.background)
            .then(swipeMinimizeModifier)
    ) {
        // Dynamic Blurred Background Art
        if (item.coverArtPath != null) {
            AsyncImage(
                model = item.coverArtPath,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(28.dp)
                    .alpha(0.4f),
                contentScale = ContentScale.Crop
            )
        }

        // Overlay Gradient for Depth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
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

            // Center: Dashboard Widgets
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                if (showClock) {
                    Text(
                        text = timeFormatter.format(Date(currentTime.longValue)),
                        color = colors.accent,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                if (showDate) {
                    Text(
                        text = dateFormatter.format(Date(currentTime.longValue)),
                        color = colors.textSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (showTemp) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Thermostat,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "$ambientTemp${strings.tempUnit}",
                            color = colors.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (showSpeed) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Speed,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "$carSpeed",
                            color = colors.accent,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = strings.speedUnit,
                            color = colors.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (!showClock && !showDate && !showTemp && !showSpeed) {
                    Text(
                        text = strings.audioPlayerHeader,
                        color = colors.accent,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Switch button between Album Cover and Visualizer Bars
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surfaceSecondary)
                    .border(1.5.dp, colors.accent.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .clickable { onToggleVisualizerBars(!showVisualizerBars) }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp, bottom = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Giant Visual Area: Click to toggle between Cover and Equalizer Bars
            Box(
                modifier = Modifier
                    .weight(2.5f) // Increased from 1.8f to make it much larger
                    .fillMaxWidth(0.95f) // Slightly wider
                    .shadow(32.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .border(2.dp, colors.accent.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onToggleVisualizerBars(!showVisualizerBars)
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
                            backgroundColor = if (colors.isDark) Color(0xFF07090C) else colors.surface.copy(alpha = 0.8f),
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        CoverArtImage(
                            coverArtPath = item.coverArtPath,
                            title = item.title,
                            modifier = Modifier.fillMaxSize().aspectRatio(1f)
                        )
                    }
                }
            }

            // Information & Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = item.title,
                        color = colors.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.artist ?: strings.unknownArtist} — ${item.album ?: strings.unknownAlbum}",
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Large Scrub Slider
                val progressFraction = if (playbackDuration > 0) {
                    (playbackProgress.toFloat() / playbackDuration).coerceIn(0f, 1f)
                } else 0f

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "\u200E${DateUtils.formatElapsedTime(playbackProgress / 1000)}\u200E",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Slider(
                        value = progressFraction,
                        onValueChange = { frac ->
                            onSeek((frac * playbackDuration).toLong())
                        },
                        modifier = Modifier.weight(1f).height(32.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = colors.accent,
                            activeTrackColor = colors.accent,
                            inactiveTrackColor = colors.cardBorder
                        )
                    )

                    Text(
                        text = "\u200E${DateUtils.formatElapsedTime(playbackDuration / 1000)}\u200E",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Massive Touch Controls
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onToggleShuffle,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (isShuffleEnabled) colors.accent else colors.textSecondary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Previous / Rewind on Hold
                    CarSeekButton(
                        icon = Icons.Filled.SkipPrevious,
                        contentDescription = strings.previous,
                        modifier = Modifier.size(64.dp),
                        iconSize = 32.dp,
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
                        modifier = Modifier.size(64.dp),
                        iconSize = 32.dp,
                        isSeeking = isFastForwarding,
                        onClick = onNext,
                        onHoldStart = onFastForwardStart,
                        onHoldEnd = onFastForwardEnd
                    )

                    IconButton(
                        onClick = onToggleRepeat,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                            contentDescription = "Repeat",
                            tint = if (repeatMode != Player.REPEAT_MODE_OFF) colors.accent else colors.textSecondary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}
}

/**
 * High-End Automotive Neon Audio Equalizer Bars Visualizer.
 * Renders 40 dynamic spectrum frequency bars with peak hold caps,
 * active harmonic pulsing, and smooth idle decay when paused.
 */
@Composable
fun CarAudioSpectrumVisualizer(
    isPlaying: Boolean,
    accentColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    val barCount = 40
    val barFractions = remember { mutableStateListOf(*Array(barCount) { 0.12f }) }
    val peakFractions = remember { mutableStateListOf(*Array(barCount) { 0.14f }) }

    // Rhythmic live spectrum simulation with realistic frequency groupings (bass, mid, treble)
    LaunchedEffect(isPlaying) {
        var step = 0
        while (true) {
            if (isPlaying) {
                step++
                for (i in 0 until barCount) {
                    // Multi-component chaotic target generation for realism
                    // Combines different sine frequencies to break predictable patterns
                    val baseOsc = sin(step * (0.15f + (i % 3) * 0.05f) + i * 0.4f)
                    val fastOsc = sin(step * 0.45f - i * 0.2f) * 0.4f
                    val rhythmicPulse = if (i < 10 && step % 12 < 4) 0.3f else 0f // Extra bass kick simulation
                    
                    val combinedWeight = when {
                        i < 10 -> 0.4f + (baseOsc * 0.4f) + (fastOsc * 0.2f) + rhythmicPulse // Bass
                        i < 30 -> 0.3f + (baseOsc * 0.45f) + (fastOsc * 0.35f) // Mids
                        else -> 0.2f + (baseOsc * 0.5f) + (fastOsc * 0.5f) // Treble
                    }.coerceAtLeast(0f)

                    val pureRandom = Random.nextFloat() * 0.45f // Increased randomness
                    val target = (combinedWeight + pureRandom).coerceIn(0.08f, 0.98f)
                    
                    // Faster smoothing for more jittery/real feel
                    barFractions[i] = (barFractions[i] * 0.55f + target * 0.45f)

                    // Update peaks with variable decay rates for realism
                    if (barFractions[i] > peakFractions[i]) {
                        peakFractions[i] = barFractions[i]
                    } else {
                        val decay = 0.035f + (i % 5) * 0.01f // Faster decay
                        peakFractions[i] = (peakFractions[i] - decay).coerceAtLeast(barFractions[i])
                    }
                }
                delay(45)
            } else {
                // Calm smooth idle baseline
                for (i in 0 until barCount) {
                    barFractions[i] = (barFractions[i] * 0.92f).coerceAtLeast(0.06f)
                    peakFractions[i] = (peakFractions[i] * 0.92f).coerceAtLeast(0.08f)
                }
                delay(100)
            }
        }
    }

    Box(
        modifier = modifier
            .background(backgroundColor)
            .padding(10.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val totalWidth = size.width
            val totalHeight = size.height
            val spacing = 3.dp.toPx()
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
                        Color(0xFFFF2E63),
                        accentColor,
                        Color(0xFF00FFF5)
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

                // Peak Indicator Cap
                val peakTop = (totalHeight - (totalHeight * peakFractions[i])).coerceIn(0f, totalHeight - 4.dp.toPx())
                drawRoundRect(
                    color = if (backgroundColor.luminance() > 0.5f) Color.Black.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.85f),
                    topLeft = Offset(left, peakTop),
                    size = Size(barWidth, 2.5.dp.toPx()),
                    cornerRadius = CornerRadius(1.2.dp.toPx(), 1.2.dp.toPx())
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
                tint = if (backgroundColor.luminance() > 0.5f) Color.Black.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = if (isPlaying) "LIVE EQUALIZER" else "PAUSED",
                color = if (backgroundColor.luminance() > 0.5f) Color.Black.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.6f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

