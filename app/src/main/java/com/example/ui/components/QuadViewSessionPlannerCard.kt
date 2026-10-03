package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RecordingSettings
import com.example.ui.StorageInfo
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.RecordRed
import java.util.Locale

@Composable
fun QuadViewSessionPlannerCard(
    settings: RecordingSettings,
    storageInfo: StorageInfo,
    onToggleQuadView: (Boolean) -> Unit,
    onDurationSelected: (Int) -> Unit,
    onApplySpaceSaver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isQuad = settings.isQuadViewMode
    val durationMins = settings.targetDurationMinutes
    val estimatedBytes = settings.estimateSessionSizeBytes()
    val estimatedGb = estimatedBytes / 1_000_000_000.0
    val freeGb = storageInfo.freeSpaceBytes / 1_000_000_000.0

    // Low storage threshold detection
    val isStorageCritical = storageInfo.freeSpaceBytes < estimatedBytes ||
            storageInfo.freeSpaceBytes < 2_500_000_000L ||
            estimatedBytes > (storageInfo.freeSpaceBytes * 0.70f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("quad_view_session_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)),
        border = BorderStroke(
            if (isQuad) 1.5.dp else 1.dp,
            if (isQuad) Brush.horizontalGradient(listOf(AccentCyan, AccentPurple))
            else SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Mode Switcher Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Standard mode tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (!isQuad) MaterialTheme.colorScheme.surfaceVariant
                            else Color.Transparent
                        )
                        .clickable { onToggleQuadView(false) }
                        .testTag("standard_mode_tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Standard Capture",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (!isQuad) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (!isQuad) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Quad-view mode tab
                Box(
                    modifier = Modifier
                        .weight(1.15f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isQuad) Brush.horizontalGradient(listOf(AccentCyan.copy(alpha = 0.25f), AccentPurple.copy(alpha = 0.25f)))
                            else SolidColor(Color.Transparent)
                        )
                        .border(
                            if (isQuad) 1.dp else 0.dp,
                            if (isQuad) AccentCyan.copy(alpha = 0.5f) else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onToggleQuadView(true) }
                        .testTag("quad_view_mode_tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Layers,
                            contentDescription = null,
                            tint = if (isQuad) AccentCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Quad-View Studio",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isQuad) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isQuad) AccentCyan else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isQuad,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    // 4 Stream badges
                    Text(
                        text = "4-STREAM SYNCHRONIZED CAPTURE FEEDS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = AccentCyan
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        QuadFeedPill(icon = Icons.Filled.Tv, title = "Screen", color = AccentCyan, modifier = Modifier.weight(1f))
                        QuadFeedPill(icon = Icons.Filled.CameraAlt, title = "Facecam", color = AccentPurple, modifier = Modifier.weight(1f))
                        QuadFeedPill(icon = Icons.Filled.GraphicEq, title = "Spectrum", color = AccentGreen, modifier = Modifier.weight(1f))
                        QuadFeedPill(icon = Icons.Filled.Speed, title = "Telemetry", color = AccentAmber, modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Duration selector chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Planned Session Duration",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${settings.getEffectiveBitrateMbps()} Mbps High-Bitrate",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val presets = listOf(15, 30, 60, 120)
                        presets.forEach { mins ->
                            val isSelected = durationMins == mins
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onDurationSelected(mins) }
                                    .testTag("duration_chip_${mins}m"),
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) AccentCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) AccentCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            ) {
                                Text(
                                    text = "${mins}m",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) AccentCyan else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Storage Estimation Gauge & Alert Banner
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isStorageCritical) RecordRed.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isStorageCritical) AccentAmber.copy(alpha = 0.7f)
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isStorageCritical) Icons.Filled.WarningAmber else Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        tint = if (isStorageCritical) AccentAmber else AccentGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isStorageCritical) "Storage Warning Armed" else "Session Space Check",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isStorageCritical) AccentAmber else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Text(
                                    text = String.format(Locale.getDefault(), "Est. %.2f GB", estimatedGb),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isStorageCritical) RecordRed else AccentCyan
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (isStorageCritical) {
                                    "Session requires ~${String.format(Locale.getDefault(), "%.2f", estimatedGb)} GB for ${durationMins}m, but device has only ${String.format(Locale.getDefault(), "%.1f", freeGb)} GB free. Storage check will guard against corruption."
                                } else {
                                    "Safe to record: Device has ${String.format(Locale.getDefault(), "%.1f", freeGb)} GB available for this ${durationMins}m Quad-View session."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (isStorageCritical) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onApplySpaceSaver() }
                                        .testTag("inline_space_saver_button"),
                                    shape = RoundedCornerShape(8.dp),
                                    color = AccentAmber.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, AccentAmber.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "Auto-Optimize: Enable Space-Saver (720p • 4Mbps)",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = AccentAmber
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
}

@Composable
private fun QuadFeedPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = color
            )
        }
    }
}
