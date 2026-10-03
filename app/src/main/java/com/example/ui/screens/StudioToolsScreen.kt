package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BrandingWatermark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Gif
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.Compress
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RecordingEntity
import com.example.ui.ScreenRecorderViewModel
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.RecordRed

@Composable
fun StudioToolsScreen(
    viewModel: ScreenRecorderViewModel,
    recordings: List<RecordingEntity>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val videos = recordings.filter { !it.isScreenshot }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // Banner Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("studio_tools_banner"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.4f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(AccentCyan.copy(alpha = 0.15f), AccentPurple.copy(alpha = 0.15f))
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AccentCyan.copy(alpha = 0.2f),
                            border = BorderStroke(0.5.dp, AccentCyan)
                        ) {
                            Text(
                                text = "PLAY STORE PRO TOOLS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    letterSpacing = 1.sp
                                ),
                                color = AccentCyan,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Video & Audio Studio Suite",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Trim, compress, extract audio, and create animated GIFs instantly from your screen captures.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(AccentCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoFixHigh,
                            contentDescription = "Studio Tools",
                            tint = AccentCyan,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Video Editing Tools",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Tool 1: Video Trimmer
        ToolCardItem(
            icon = Icons.Filled.ContentCut,
            iconTint = RecordRed,
            title = "Video Trimmer & Cutter",
            description = "Cut out unwanted start/end parts and export highlights",
            badge = "Essential",
            badgeColor = RecordRed,
            onClick = {
                val candidate = videos.firstOrNull()
                if (candidate != null) {
                    viewModel.openTrimmer(candidate)
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Tool 2: Video Compressor
        ToolCardItem(
            icon = Icons.Outlined.Compress,
            iconTint = AccentCyan,
            title = "Video Compressor",
            description = "Reduce video file size by up to 70% for WhatsApp, Discord & Email",
            badge = "Save Storage",
            badgeColor = AccentGreen,
            onClick = {
                val candidate = videos.firstOrNull()
                if (candidate != null) {
                    viewModel.openCompressor(candidate)
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Tool 3: GIF Converter
        ToolCardItem(
            icon = Icons.Filled.Gif,
            iconTint = AccentAmber,
            title = "Video to Animated GIF",
            description = "Convert your favorite recording moments into high quality GIFs",
            badge = "Shareable",
            badgeColor = AccentAmber,
            onClick = {
                val candidate = videos.firstOrNull()
                if (candidate != null) {
                    viewModel.openGifConverter(candidate)
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Tool 4: Audio Extractor
        ToolCardItem(
            icon = Icons.Filled.Audiotrack,
            iconTint = AccentPurple,
            title = "Audio Extractor (MP3 / AAC)",
            description = "Rip microphone voice commentary or game sound into standalone audio tracks",
            badge = "Audio Studio",
            badgeColor = AccentPurple,
            onClick = {
                val candidate = videos.firstOrNull()
                if (candidate != null) {
                    viewModel.openAudioExtractor(candidate)
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Branding & Annotations",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Tool 5: Custom Watermark
        ToolCardItem(
            icon = Icons.Filled.BrandingWatermark,
            iconTint = AccentCyan,
            title = "Custom Brand Watermark",
            description = "Add text or channel logo branding to your exported recordings",
            badge = if (viewModel.isCustomWatermarkEnabled.value) "Active" else "Custom",
            badgeColor = if (viewModel.isCustomWatermarkEnabled.value) AccentGreen else AccentCyan,
            onClick = {
                viewModel.openWatermarkDialog(true)
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Tool 6: Quick Snapshot
        ToolCardItem(
            icon = Icons.Filled.CameraAlt,
            iconTint = AccentGreen,
            title = "Screen Snapshot Tool",
            description = "Capture an instant high-resolution frame capture to gallery",
            badge = "Instant",
            badgeColor = AccentGreen,
            onClick = {
                viewModel.takeScreenshot("Studio_Snap_${System.currentTimeMillis()}")
            }
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ToolCardItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String,
    badge: String,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("tool_card_${title.replace(" ", "_")}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = badgeColor.copy(alpha = 0.15f),
                        border = BorderStroke(0.5.dp, badgeColor)
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
