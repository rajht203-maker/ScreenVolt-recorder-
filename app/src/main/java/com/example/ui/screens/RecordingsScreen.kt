package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Gif
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.Compress
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RecordingEntity
import com.example.ui.MediaFilter
import com.example.ui.ScreenRecorderViewModel
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.RecordRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecordingsScreen(
    viewModel: ScreenRecorderViewModel,
    recordings: List<RecordingEntity>,
    selectedFilter: MediaFilter,
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    var renamingRecording by remember { mutableStateOf<RecordingEntity?>(null) }
    var renameInputText by remember { mutableStateOf("") }
    var deletingRecording by remember { mutableStateOf<RecordingEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = {
                Text(
                    text = "Search recordings, tags...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = AccentCyan
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_recordings_input"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = AccentCyan,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips (All, Videos, Screenshots, Favorites)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MediaFilter.values().forEach { filter ->
                val selected = filter == selectedFilter
                val label = when (filter) {
                    MediaFilter.ALL -> "All Media"
                    MediaFilter.VIDEOS -> "Videos"
                    MediaFilter.SCREENSHOTS -> "Screenshots"
                    MediaFilter.FAVORITES -> "Favorites"
                }
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.setFilter(filter) },
                    label = { Text(text = label) },
                    leadingIcon = {
                        Icon(
                            imageVector = when (filter) {
                                MediaFilter.ALL -> Icons.Filled.Folder
                                MediaFilter.VIDEOS -> Icons.Filled.Videocam
                                MediaFilter.SCREENSHOTS -> Icons.Filled.PhotoCamera
                                MediaFilter.FAVORITES -> Icons.Filled.Favorite
                            },
                            contentDescription = label,
                            modifier = Modifier.size(16.dp),
                            tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RecordRed,
                        selectedLabelColor = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        selectedBorderColor = RecordRed
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Media List or Empty State
        if (recordings.isEmpty()) {
            EmptyRecordingsView(
                isSearch = searchQuery.isNotEmpty(),
                onClearSearch = { viewModel.setSearchQuery("") }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("recordings_lazy_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recordings, key = { it.id }) { item ->
                    RecordingCardItem(
                        recording = item,
                        onPlay = { viewModel.openPlayer(item) },
                        onToggleFavorite = { viewModel.toggleFavorite(item) },
                        onTrim = { viewModel.openTrimmer(item) },
                        onCompress = { viewModel.openCompressor(item) },
                        onGif = { viewModel.openGifConverter(item) },
                        onAudioExtract = { viewModel.openAudioExtractor(item) },
                        onRename = {
                            renamingRecording = item
                            renameInputText = item.title
                        },
                        onDelete = { deletingRecording = item }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // Rename Dialog
    if (renamingRecording != null) {
        AlertDialog(
            onDismissRequest = { renamingRecording = null },
            title = { Text(text = "Rename Recording") },
            text = {
                OutlinedTextField(
                    value = renameInputText,
                    onValueChange = { renameInputText = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        renamingRecording?.let {
                            if (renameInputText.isNotBlank()) {
                                viewModel.renameRecording(it.id, renameInputText.trim())
                            }
                        }
                        renamingRecording = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RecordRed)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { renamingRecording = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (deletingRecording != null) {
        AlertDialog(
            onDismissRequest = { deletingRecording = null },
            title = { Text(text = "Delete Recording?") },
            text = {
                Text(
                    text = "Are you sure you want to permanently delete \"${deletingRecording?.title}\"? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        deletingRecording?.let { viewModel.deleteRecording(it.id) }
                        deletingRecording = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RecordRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingRecording = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun RecordingCardItem(
    recording: RecordingEntity,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onTrim: () -> Unit,
    onCompress: () -> Unit,
    onGif: () -> Unit,
    onAudioExtract: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val dateStr = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(recording.timestamp))
    val sizeMb = String.format(Locale.getDefault(), "%.1f MB", recording.fileSizeBytes / 1_000_000.0)
    val durationFormatted = if (recording.isScreenshot) "Image" else {
        val s = (recording.durationMs / 1000)
        val m = s / 60
        val remS = s % 60
        String.format(Locale.getDefault(), "%02d:%02d", m, remS)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recording_item_${recording.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail / Icon Box
                Box(
                    modifier = Modifier
                        .size(width = 80.dp, height = 64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = if (recording.isScreenshot) {
                                    listOf(AccentCyan.copy(alpha = 0.4f), AccentPurple.copy(alpha = 0.6f))
                                } else {
                                    listOf(RecordRed.copy(alpha = 0.4f), Color(0xFF1E1020))
                                }
                            )
                        )
                        .clickable { onPlay() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (recording.isScreenshot) Icons.Filled.Photo else Icons.Filled.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )

                    // Duration overlay badge
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.75f)
                    ) {
                        Text(
                            text = durationFormatted,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Info Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onPlay() }
                ) {
                    Text(
                        text = recording.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = if (recording.isScreenshot) "Snapshot" else "${recording.resolution} • ${recording.fps}fps",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                color = AccentCyan,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = sizeMb,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Actions Column (Favorite & More)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (recording.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (recording.isFavorite) RecordRed else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "More",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Play / View") },
                                onClick = {
                                    menuExpanded = false
                                    onPlay()
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = AccentGreen)
                                }
                            )

                            if (!recording.isScreenshot) {
                                DropdownMenuItem(
                                    text = { Text("Trim Video") },
                                    onClick = {
                                        menuExpanded = false
                                        onTrim()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Filled.ContentCut, contentDescription = null, tint = AccentCyan)
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Compress Video") },
                                    onClick = {
                                        menuExpanded = false
                                        onCompress()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Compress, contentDescription = null, tint = AccentPurple)
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Convert to GIF") },
                                    onClick = {
                                        menuExpanded = false
                                        onGif()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Filled.Gif, contentDescription = null, tint = AccentAmber)
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Extract Audio (MP3)") },
                                    onClick = {
                                        menuExpanded = false
                                        onAudioExtract()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Filled.Audiotrack, contentDescription = null, tint = AccentGreen)
                                    }
                                )
                            }

                            DropdownMenuItem(
                                text = { Text("Rename") },
                                onClick = {
                                    menuExpanded = false
                                    onRename()
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Delete", color = RecordRed) },
                                onClick = {
                                    menuExpanded = false
                                    onDelete()
                                },
                                leadingIcon = {
                                    Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = RecordRed)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyRecordingsView(
    isSearch: Boolean,
    onClearSearch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSearch) Icons.Filled.Search else Icons.Filled.Videocam,
                contentDescription = "Empty",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isSearch) "No recordings match your search" else "No recordings yet",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isSearch) "Try searching for a different keyword or clear the search query." else "Tap the REC button in the Record tab to start your first capture.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        if (isSearch) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onClearSearch,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text("Clear Search", color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
