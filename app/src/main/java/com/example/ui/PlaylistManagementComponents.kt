package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.CustomPlaylistEntity
import com.example.data.MediaItemEntity
import com.example.localization.LocalAppStrings
import com.example.theme.LocalCarColors
import java.io.File

/**
 * Car-friendly Bottom/Center Action Dialog triggered by Long-Pressing on any audio track or video.
 */
@Composable
fun MediaItemActionDialog(
    item: MediaItemEntity,
    isInCustomPlaylist: Boolean = false,
    onDismiss: () -> Unit,
    onAddToPlaylistClick: () -> Unit,
    onRemoveFromPlaylistClick: (() -> Unit)? = null,
    onDeleteClick: () -> Unit,
    onSelectModeClick: (() -> Unit)? = null,
    isCompact: Boolean = false
) {
    val colors = LocalCarColors.current
    val strings = LocalAppStrings.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable(onClick = onDismiss)
                .padding(horizontal = if (isCompact) 16.dp else 32.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
                    .border(1.5.dp, colors.cardBorder, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(if (isCompact) 14.dp else 20.dp),
                    verticalArrangement = Arrangement.spacedBy(if (isCompact) 10.dp else 14.dp)
                ) {
                    // Header: Media Item info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (isCompact) 44.dp else 52.dp)
                                .background(colors.surfaceSecondary, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (item.isVideo) Icons.Filled.Videocam else Icons.Filled.MusicNote,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(if (isCompact) 24.dp else 28.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.trackActionTitle,
                                color = colors.accent,
                                fontSize = if (isCompact) 10.sp else 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = item.title,
                                color = colors.textPrimary,
                                fontSize = if (isCompact) 14.sp else 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val sub = if (item.isVideo) {
                                File(item.filePath).parentFile?.name ?: ""
                            } else {
                                item.artist ?: strings.unknownArtist
                            }
                            if (sub.isNotBlank()) {
                                Text(
                                    text = sub,
                                    color = colors.textSecondary,
                                    fontSize = if (isCompact) 11.sp else 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = colors.textSecondary
                            )
                        }
                    }

                    HorizontalDivider(color = colors.cardBorder)

                    // Selection Mode Action
                    if (onSelectModeClick != null) {
                        Button(
                            onClick = {
                                onDismiss()
                                onSelectModeClick()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isCompact) 44.dp else 50.dp)
                                .testTag("action_enter_selection"),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.surfaceSecondary),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Checklist,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = strings.multiSelect,
                                    color = colors.textPrimary,
                                    fontSize = if (isCompact) 13.sp else 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Action 1: Add to Playlist
                    Button(
                        onClick = {
                            onDismiss()
                            onAddToPlaylistClick()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isCompact) 44.dp else 50.dp)
                            .testTag("action_add_to_playlist"),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.surfaceSecondary),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlaylistAdd,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = strings.addToPlaylist,
                                color = colors.textPrimary,
                                fontSize = if (isCompact) 13.sp else 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Action 2 (Optional): Remove from current custom playlist
                    if (isInCustomPlaylist && onRemoveFromPlaylistClick != null) {
                        Button(
                            onClick = {
                                onDismiss()
                                onRemoveFromPlaylistClick()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isCompact) 44.dp else 50.dp)
                                .testTag("action_remove_from_playlist"),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.surfaceSecondary),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlaylistRemove,
                                    contentDescription = null,
                                    tint = Color(0xFFFFA726),
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = strings.removeFromPlaylist,
                                    color = colors.textPrimary,
                                    fontSize = if (isCompact) 13.sp else 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Action 3: Delete File permanently
                    Button(
                        onClick = {
                            onDismiss()
                            onDeleteClick()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isCompact) 44.dp else 50.dp)
                            .testTag("action_delete_media"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F).copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = strings.deleteMediaItem,
                                color = Color(0xFFFF5252),
                                fontSize = if (isCompact) 13.sp else 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog to create a new custom playlist with automotive-sized touch elements.
 */
@Composable
fun CreatePlaylistDialog(
    isVideo: Boolean,
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
    isCompact: Boolean = false
) {
    val colors = LocalCarColors.current
    val strings = LocalAppStrings.current
    var playlistName by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable(onClick = onDismiss)
                .padding(horizontal = if (isCompact) 16.dp else 32.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .widthIn(max = 440.dp)
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
                    .border(1.5.dp, colors.cardBorder, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(if (isCompact) 16.dp else 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(colors.accent.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isVideo) Icons.Filled.VideoLibrary else Icons.Filled.QueueMusic,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Text(
                            text = strings.createPlaylist,
                            color = colors.textPrimary,
                            fontSize = if (isCompact) 15.sp else 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedTextField(
                        value = playlistName,
                        onValueChange = { playlistName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_playlist_name"),
                        placeholder = {
                            Text(strings.playlistNamePlaceholder, color = colors.textSecondary, fontSize = 13.sp)
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(if (isCompact) 44.dp else 48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(colors.cardBorder))
                        ) {
                            Text(
                                text = strings.cancel,
                                color = colors.textSecondary,
                                fontSize = if (isCompact) 13.sp else 14.sp
                            )
                        }

                        Button(
                            onClick = {
                                if (playlistName.isNotBlank()) {
                                    onCreate(playlistName.trim())
                                    onDismiss()
                                }
                            },
                            enabled = playlistName.isNotBlank(),
                            modifier = Modifier
                                .weight(1f)
                                .height(if (isCompact) 44.dp else 48.dp)
                                .testTag("btn_confirm_create_playlist"),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = strings.newPlaylistTitle,
                                color = colors.onAccent,
                                fontSize = if (isCompact) 13.sp else 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog to select an existing playlist to add the selected media item to,
 * or create a new playlist on the fly.
 */
@Composable
fun AddToPlaylistDialog(
    item: MediaItemEntity,
    playlists: List<CustomPlaylistEntity>,
    playlistCounts: Map<Long, Int>,
    onDismiss: () -> Unit,
    onPlaylistSelected: (CustomPlaylistEntity) -> Unit,
    onCreateNewPlaylistClick: () -> Unit,
    isCompact: Boolean = false
) {
    val colors = LocalCarColors.current
    val strings = LocalAppStrings.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable(onClick = onDismiss)
                .padding(horizontal = if (isCompact) 16.dp else 32.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
                    .border(1.5.dp, colors.cardBorder, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(if (isCompact) 14.dp else 18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Title Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = strings.selectPlaylistToAdd,
                            color = colors.textPrimary,
                            fontSize = if (isCompact) 15.sp else 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = colors.textSecondary
                            )
                        }
                    }

                    // Create New Playlist Shortcut Button
                    Button(
                        onClick = {
                            onDismiss()
                            onCreateNewPlaylistClick()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isCompact) 44.dp else 48.dp)
                            .testTag("btn_add_to_new_playlist"),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                tint = colors.onAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = strings.createPlaylist,
                                color = colors.onAccent,
                                fontSize = if (isCompact) 12.sp else 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    HorizontalDivider(color = colors.cardBorder)

                    // Existing playlists list
                    if (playlists.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = strings.noCustomPlaylists,
                                color = colors.textSecondary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(playlists, key = { it.id }) { pl ->
                                val count = playlistCounts[pl.id] ?: 0
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.surfaceSecondary)
                                        .clickable {
                                            onPlaylistSelected(pl)
                                            onDismiss()
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(colors.accent.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (pl.isVideo) Icons.Filled.VideoLibrary else Icons.Filled.QueueMusic,
                                            contentDescription = null,
                                            tint = colors.accent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = pl.name,
                                            color = colors.textPrimary,
                                            fontSize = if (isCompact) 13.sp else 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "$count ${if (pl.isVideo) strings.allVideos else strings.tracksCount}",
                                            color = colors.textSecondary,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Filled.AddCircleOutline,
                                        contentDescription = "Add",
                                        tint = colors.accent,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Confirmation dialog for permanently deleting a media item or custom playlist.
 */
@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isCompact: Boolean = false
) {
    val colors = LocalCarColors.current
    val strings = LocalAppStrings.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable(onClick = onDismiss)
                .padding(horizontal = if (isCompact) 16.dp else 32.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
                    .border(1.5.dp, Color(0xFFD32F2F).copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(if (isCompact) 16.dp else 22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFD32F2F).copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Text(
                            text = title,
                            color = colors.textPrimary,
                            fontSize = if (isCompact) 15.sp else 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = message,
                        color = colors.textSecondary,
                        fontSize = if (isCompact) 12.sp else 13.sp,
                        lineHeight = 18.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(if (isCompact) 42.dp else 46.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(colors.cardBorder))
                        ) {
                            Text(
                                text = strings.cancel,
                                color = colors.textSecondary,
                                fontSize = if (isCompact) 12.sp else 13.sp
                            )
                        }

                        Button(
                            onClick = {
                                onConfirm()
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(if (isCompact) 42.dp else 46.dp)
                                .testTag("btn_confirm_delete"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = strings.delete,
                                color = Color.White,
                                fontSize = if (isCompact) 12.sp else 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * High-End Selection Toolbar for bulk actions (Add to Playlist, Delete, Remove from Playlist).
 */
@Composable
fun SelectionActionToolbar(
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onDelete: () -> Unit,
    onRemoveFromPlaylist: (() -> Unit)? = null, // Only for custom playlist view
    isCompact: Boolean = false
) {
    val colors = LocalCarColors.current
    val strings = LocalAppStrings.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.accent)
            .padding(horizontal = 12.dp, vertical = if (isCompact) 8.dp else 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(onClick = onClearSelection) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Clear Selection",
                tint = colors.onAccent
            )
        }

        Text(
            text = "$selectedCount ${strings.tracksCount}",
            color = colors.onAccent,
            fontSize = if (isCompact) 14.sp else 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        if (onRemoveFromPlaylist != null) {
            IconButton(onClick = onRemoveFromPlaylist) {
                Icon(
                    imageVector = Icons.Filled.PlaylistRemove,
                    contentDescription = "Remove from Playlist",
                    tint = colors.onAccent
                )
            }
        }

        IconButton(onClick = onAddToPlaylist) {
            Icon(
                imageVector = Icons.Filled.PlaylistAdd,
                contentDescription = "Add to Playlist",
                tint = colors.onAccent
            )
        }

        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete",
                tint = colors.onAccent
            )
        }
    }
}

/**
 * Dialog to select a custom playlist for bulk adding items.
 */
@Composable
fun PlaylistSelectionDialog(
    playlists: List<CustomPlaylistEntity>,
    onPlaylistSelected: (CustomPlaylistEntity) -> Unit,
    onCreateNewPlaylist: () -> Unit,
    onDismiss: () -> Unit,
    isCompact: Boolean = false
) {
    val colors = LocalCarColors.current
    val strings = LocalAppStrings.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .widthIn(max = 400.dp)
                    .fillMaxWidth(0.9f)
                    .clickable(enabled = false) {}
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = strings.addToPlaylist,
                        color = colors.textPrimary,
                        fontSize = if (isCompact) 16.sp else 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Create New Playlist Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.accent.copy(alpha = 0.15f))
                            .clickable {
                                onCreateNewPlaylist()
                                // We don't dismiss here yet, because the Create dialog will show over this
                                // Or we dismiss this and show the other. Usually, dismiss this is cleaner.
                                onDismiss()
                            }
                            .border(1.dp, colors.accent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = strings.newPlaylistTitle,
                            color = colors.accent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    HorizontalDivider(color = colors.cardBorder)

                    if (playlists.isEmpty()) {
                        Text(
                            text = strings.noPlaylistsFound,
                            color = colors.textSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(playlists) { playlist ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.surfaceSecondary)
                                        .clickable { 
                                            onPlaylistSelected(playlist)
                                            onDismiss()
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PlaylistPlay,
                                        contentDescription = null,
                                        tint = colors.accent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = playlist.name,
                                        color = colors.textPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(text = strings.close, color = colors.accent)
                    }
                }
            }
        }
    }
}

/**
 * Display list of custom playlists for music or video with creation button and item count.
 */
@Composable
fun CustomPlaylistsListView(
    playlists: List<CustomPlaylistEntity>,
    playlistCounts: Map<Long, Int>,
    isVideo: Boolean,
    onPlaylistClick: (CustomPlaylistEntity) -> Unit,
    onCreatePlaylistClick: () -> Unit,
    onDeletePlaylistClick: (CustomPlaylistEntity) -> Unit,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colors = LocalCarColors.current
    val strings = LocalAppStrings.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Top Action: Create New Playlist Card
        item(key = "create_playlist_banner") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.accent.copy(alpha = 0.12f))
                    .border(1.5.dp, colors.accent.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .clickable(onClick = onCreatePlaylistClick)
                    .padding(horizontal = 14.dp, vertical = if (isCompact) 10.dp else 12.dp)
                    .testTag("banner_create_playlist"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isCompact) 38.dp else 44.dp)
                        .background(colors.accent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = colors.onAccent,
                        modifier = Modifier.size(if (isCompact) 22.dp else 26.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.createPlaylist,
                        color = colors.accent,
                        fontSize = if (isCompact) 13.sp else 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isVideo) strings.createPlaylistSubtitleVideo else strings.createPlaylistSubtitleMusic,
                        color = colors.textSecondary,
                        fontSize = if (isCompact) 10.sp else 11.sp
                    )
                }
            }
        }

        if (playlists.isEmpty()) {
            item(key = "empty_playlists") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.noCustomPlaylists,
                        color = colors.textSecondary,
                        fontSize = if (isCompact) 12.sp else 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(playlists, key = { it.id }) { playlist ->
                val count = playlistCounts[playlist.id] ?: 0

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(14.dp))
                        .clickable { onPlaylistClick(playlist) }
                        .padding(horizontal = 14.dp, vertical = if (isCompact) 10.dp else 12.dp)
                        .testTag("custom_playlist_${playlist.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isCompact) 42.dp else 46.dp)
                            .background(colors.surfaceSecondary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isVideo) Icons.Filled.VideoLibrary else Icons.Filled.QueueMusic,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(if (isCompact) 22.dp else 24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = playlist.name,
                            color = colors.textPrimary,
                            fontSize = if (isCompact) 13.sp else 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$count ${if (isVideo) strings.allVideos else strings.tracksCount}",
                            color = colors.textSecondary,
                            fontSize = if (isCompact) 10.sp else 11.sp
                        )
                    }

                    // Delete Playlist button
                    IconButton(
                        onClick = { onDeletePlaylistClick(playlist) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete Playlist",
                            tint = colors.textSecondary.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
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
