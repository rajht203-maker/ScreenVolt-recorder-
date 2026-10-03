package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AudioSourceOption
import com.example.data.RecordingSettings
import com.example.service.LiveAudioMetrics
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.RecordRed
import com.example.ui.theme.RecordRedDark
import com.example.ui.theme.RecordRedGlow
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class VisualizerDisplayMode(val label: String) {
    SPECTRUM("Spectrum"),
    OSCILLOSCOPE("Waveform"),
    VU_METER("VU Meter")
}

/**
 * High-performance, studio-grade real-time audio visualization HUD
 * and circular recording timer rendered with Jetpack Compose Graphics.
 */
@Composable
fun RealtimeAudioRecordingHUD(
    durationSeconds: Long,
    sizeBytes: Long,
    audioMetrics: LiveAudioMetrics,
    isPaused: Boolean,
    settings: RecordingSettings,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onSnapshot: () -> Unit,
    modifier: Modifier = Modifier
) {
    var displayMode by remember { mutableStateOf(VisualizerDisplayMode.SPECTRUM) }

    val isMuted = settings.audioSource == AudioSourceOption.MUTE

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("active_recording_hud"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.5.dp,
            brush = Brush.linearGradient(
                listOf(
                    if (isPaused) AccentPurple else RecordRed,
                    AccentCyan.copy(alpha = 0.6f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Top Status Bar: Live broadcast pill & Session stats
            HUDStatusHeader(
                isPaused = isPaused,
                sizeBytes = sizeBytes,
                settings = settings
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // COMPOSE GRAPHICS RECORDING TIMER HUD
            // ==========================================
            ComposeGraphicsRecordingTimer(
                durationSeconds = durationSeconds,
                audioNormalizedLevel = if (isPaused || isMuted) 0f else audioMetrics.normalizedLevel,
                isPaused = isPaused
            )

            Spacer(modifier = Modifier.height(18.dp))

            // ==========================================
            // REAL-TIME AUDIO VISUALIZATION HUD
            // ==========================================
            RealtimeAudioVisualizerSection(
                audioMetrics = audioMetrics,
                isMuted = isMuted,
                isPaused = isPaused,
                displayMode = displayMode,
                onSelectDisplayMode = { displayMode = it }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // ==========================================
            // STUDIO ACTION BUTTON CONTROLS
            // ==========================================
            HUDControlActions(
                isPaused = isPaused,
                onPause = onPause,
                onResume = onResume,
                onSnapshot = onSnapshot,
                onStop = onStop
            )
        }
    }
}

/**
 * Top header showing status badge and live size stats
 */
@Composable
private fun HUDStatusHeader(
    isPaused: Boolean,
    sizeBytes: Long,
    settings: RecordingSettings
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isPaused) 1f else 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    val sizeMb = String.format(Locale.getDefault(), "%.1f MB", sizeBytes / 1_000_000.0)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Live / Paused Badge
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isPaused) AccentPurple.copy(alpha = 0.15f) else RecordRed.copy(alpha = 0.15f),
            border = BorderStroke(
                1.dp,
                if (isPaused) AccentPurple.copy(alpha = 0.5f) else RecordRed.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isPaused) AccentPurple else RecordRed.copy(alpha = dotAlpha))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isPaused) "PAUSED" else "REC LIVE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = if (isPaused) AccentPurple else RecordRed
                )
            }
        }

        // Telemetry tags
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = sizeMb,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    ),
                    color = AccentCyan,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "${settings.resolution.label.split(" ").firstOrNull() ?: "1080p"} • ${settings.frameRate.label}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

/**
 * Pure Compose Graphics Recording Timer Dial & Digital Clock.
 * Draws radial ticks, dynamic sweep gradient ring, and pulsing halo.
 */
@Composable
fun ComposeGraphicsRecordingTimer(
    durationSeconds: Long,
    audioNormalizedLevel: Float,
    isPaused: Boolean,
    modifier: Modifier = Modifier
) {
    val hours = durationSeconds / 3600
    val minutes = (durationSeconds % 3600) / 60
    val seconds = durationSeconds % 60
    val formattedTime = if (hours > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "timerDial")
    val sweepRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepRotation"
    )

    val animatedAudioLevel by animateFloatAsState(
        targetValue = audioNormalizedLevel,
        animationSpec = tween(80, easing = FastOutSlowInEasing),
        label = "audioScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.Center
    ) {
        // Compose Graphics Canvas for Circular Dial
        Canvas(
            modifier = Modifier.size(175.dp)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.width / 2f - 12.dp.toPx()

            // 1. Background Track
            drawCircle(
                color = Color(0xFF1E2235),
                radius = radius,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )

            // 2. Dynamic Audio Reactive Outer Halo Ring
            if (!isPaused && animatedAudioLevel > 0.05f) {
                val haloRadius = radius + (animatedAudioLevel * 8.dp.toPx())
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AccentCyan.copy(alpha = 0.25f * animatedAudioLevel),
                            Color.Transparent
                        ),
                        center = center,
                        radius = haloRadius + 6.dp.toPx()
                    ),
                    radius = haloRadius,
                    center = center
                )
            }

            // 3. Rotating Sweep Gradient Arc
            if (!isPaused) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to RecordRed,
                        0.5f to AccentCyan,
                        0.85f to AccentPurple,
                        1.0f to RecordRed,
                        center = center
                    ),
                    startAngle = sweepRotation,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 4. 60 Radial Tick Marks (representing seconds of a minute)
            val currentSecondOfMinute = (seconds % 60).toInt()
            val tickCount = 60
            for (i in 0 until tickCount) {
                val angleDeg = (i * (360.0 / tickCount)) - 90.0
                val angleRad = angleDeg * (PI / 180.0)

                val isElapsed = i <= currentSecondOfMinute
                val isMajor = (i % 5 == 0)

                val innerDist = radius - if (isMajor) 9.dp.toPx() else 5.dp.toPx()
                val outerDist = radius - 1.5.dp.toPx()

                val startX = (center.x + innerDist * cos(angleRad)).toFloat()
                val startY = (center.y + innerDist * sin(angleRad)).toFloat()
                val endX = (center.x + outerDist * cos(angleRad)).toFloat()
                val endY = (center.y + outerDist * sin(angleRad)).toFloat()

                val tickColor = when {
                    isPaused -> AccentPurple.copy(alpha = if (isElapsed) 0.8f else 0.2f)
                    isElapsed -> if (isMajor) AccentCyan else RecordRed
                    else -> Color(0xFF333852)
                }

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (isMajor) 2.2.dp.toPx() else 1.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        // Inner Digital Timer Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "SESSION TIME",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = formattedTime,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    fontSize = 32.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("live_duration_text")
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isPaused) AccentPurple else AccentGreen)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isPaused) "HOLD" else "${durationSeconds % 60}s TICK",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = if (isPaused) AccentPurple else AccentGreen
                )
            }
        }
    }
}

/**
 * Real-time audio visualization section featuring 3 Compose Graphics renderers:
 * 1. Spectrum Frequency Bar Graph
 * 2. Live Oscilloscope Waveform
 * 3. Segmented LED VU Meter with live dB readout
 */
@Composable
fun RealtimeAudioVisualizerSection(
    audioMetrics: LiveAudioMetrics,
    isMuted: Boolean,
    isPaused: Boolean,
    displayMode: VisualizerDisplayMode,
    onSelectDisplayMode: (VisualizerDisplayMode) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("realtime_audio_visualizer_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Visualizer Header: Mic Icon, dB Level, Peak indicator & Mode Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (isMuted) Color.Gray.copy(alpha = 0.2f)
                                else if (audioMetrics.decibels > -6f) RecordRed.copy(alpha = 0.2f)
                                else AccentCyan.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                            contentDescription = "Microphone",
                            tint = if (isMuted) Color.Gray
                            else if (audioMetrics.decibels > -6f) RecordRed
                            else AccentCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isMuted) "MIC MUTED" else "MIC INPUT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (!isMuted && audioMetrics.isMicLive) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "LIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    ),
                                    color = AccentGreen
                                )
                            }
                        }

                        // Decibels Badge
                        val dbText = if (isMuted || isPaused) "-- dB"
                        else String.format(Locale.getDefault(), "%.1f dB", audioMetrics.decibels)

                        Text(
                            text = "Level: $dbText",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = when {
                                isMuted || isPaused -> MaterialTheme.colorScheme.onSurfaceVariant
                                audioMetrics.decibels > -6f -> RecordRed
                                audioMetrics.decibels > -18f -> AccentAmber
                                else -> AccentGreen
                            }
                        )
                    }
                }

                // Mode Switch Pills
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    VisualizerDisplayMode.values().forEach { mode ->
                        val isSelected = mode == displayMode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) AccentCyan else Color.Transparent)
                                .clickable { onSelectDisplayMode(mode) }
                                .padding(horizontal = 7.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Graphic Surface
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F111C))
                    .border(1.dp, Color(0xFF22263B), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (isMuted) {
                    Text(
                        text = "Microphone muted (Audio recording disabled in Settings)",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (isPaused) {
                    Text(
                        text = "Audio telemetry paused",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    when (displayMode) {
                        VisualizerDisplayMode.SPECTRUM -> {
                            ComposeSpectrumVisualizer(
                                frequencyBins = audioMetrics.frequencyBins,
                                peakLevel = audioMetrics.peakLevel
                            )
                        }
                        VisualizerDisplayMode.OSCILLOSCOPE -> {
                            ComposeOscilloscopeWaveform(
                                frequencyBins = audioMetrics.frequencyBins,
                                normalizedLevel = audioMetrics.normalizedLevel
                            )
                        }
                        VisualizerDisplayMode.VU_METER -> {
                            ComposeVUMeterGraphic(
                                decibels = audioMetrics.decibels,
                                peakLevel = audioMetrics.peakLevel
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 1. Spectrum Frequency Equalizer drawn with Compose Graphics Canvas
 */
@Composable
private fun ComposeSpectrumVisualizer(
    frequencyBins: List<Float>,
    peakLevel: Float,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        val count = frequencyBins.size.coerceAtLeast(1)
        val availableWidth = size.width
        val barSpacing = 3.dp.toPx()
        val totalSpacing = barSpacing * (count - 1)
        val barWidth = ((availableWidth - totalSpacing) / count).coerceAtLeast(2f)
        val maxHeight = size.height

        frequencyBins.forEachIndexed { index, rawAmp ->
            val amp = rawAmp.coerceIn(0.04f, 0.98f)
            val barHeight = (amp * (maxHeight - 8.dp.toPx())).coerceAtLeast(4.dp.toPx())
            val x = index * (barWidth + barSpacing)
            val y = maxHeight - barHeight

            // Gradient brush for bar: Cyan -> Purple -> Red for loud peaks
            val barBrush = Brush.verticalGradient(
                colors = listOf(
                    if (amp > 0.8f) RecordRed else AccentCyan,
                    AccentPurple,
                    Color(0xFF0055FF)
                ),
                startY = y,
                endY = maxHeight
            )

            // Draw Bar
            drawRoundRect(
                brush = barBrush,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )

            // Draw Peak Hold Dot
            val peakDotY = (y - 3.dp.toPx()).coerceAtLeast(0f)
            drawCircle(
                color = if (amp > 0.85f) RecordRed else AccentCyan.copy(alpha = 0.9f),
                radius = (barWidth / 2f).coerceIn(1.5.dp.toPx(), 3.dp.toPx()),
                center = Offset(x + barWidth / 2f, peakDotY)
            )
        }
    }
}

/**
 * 2. Mirrored Real-Time Oscilloscope Waveform drawn with Compose Graphics Canvas
 */
@Composable
private fun ComposeOscilloscopeWaveform(
    frequencyBins: List<Float>,
    normalizedLevel: Float,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f

        // Draw center reference baseline
        drawLine(
            color = Color(0xFF23273D),
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            strokeWidth = 1.dp.toPx()
        )

        val points = if (frequencyBins.isEmpty()) listOf(0.1f, 0.1f) else frequencyBins
        val stepX = width / (points.size - 1).coerceAtLeast(1)

        val upperPath = Path()
        val lowerPath = Path()

        upperPath.moveTo(0f, centerY)
        lowerPath.moveTo(0f, centerY)

        for (i in points.indices) {
            val x = i * stepX
            val amp = points[i].coerceIn(0.04f, 0.96f)
            val maxWaveHeight = (height / 2f) - 6.dp.toPx()
            val waveOffset = amp * maxWaveHeight

            val upperY = centerY - waveOffset
            val lowerY = centerY + waveOffset

            if (i == 0) {
                upperPath.moveTo(x, upperY)
                lowerPath.moveTo(x, lowerY)
            } else {
                val prevX = (i - 1) * stepX
                val prevAmp = points[i - 1].coerceIn(0.04f, 0.96f)
                val prevUpperY = centerY - (prevAmp * maxWaveHeight)
                val prevLowerY = centerY + (prevAmp * maxWaveHeight)

                val midX = (prevX + x) / 2f
                upperPath.quadraticBezierTo(prevX, prevUpperY, midX, (prevUpperY + upperY) / 2f)
                lowerPath.quadraticBezierTo(prevX, prevLowerY, midX, (prevLowerY + lowerY) / 2f)
            }
        }

        // Connect back for fill gradient
        val fillPath = Path().apply {
            addPath(upperPath)
            lineTo(width, centerY)
            lineTo(0f, centerY)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    AccentCyan.copy(alpha = 0.35f),
                    Color.Transparent
                ),
                startY = 0f,
                endY = centerY
            )
        )

        // Draw upper waveform stroke with glowing gradient
        drawPath(
            path = upperPath,
            brush = Brush.horizontalGradient(
                colors = listOf(AccentCyan, AccentGreen, AccentAmber, RecordRed)
            ),
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw mirrored lower waveform stroke with soft opacity
        drawPath(
            path = lowerPath,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    AccentCyan.copy(alpha = 0.5f),
                    AccentPurple.copy(alpha = 0.5f)
                )
            ),
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

/**
 * 3. Segmented LED VU Decibel Scale Meter drawn with Compose Graphics Canvas
 */
@Composable
private fun ComposeVUMeterGraphic(
    decibels: Float,
    peakLevel: Float,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        val width = size.width
        val height = size.height
        val segmentCount = 28
        val spacing = 3.dp.toPx()
        val segWidth = (width - (spacing * (segmentCount - 1))) / segmentCount
        val segHeight = height * 0.55f
        val topY = (height - segHeight) / 2f

        // Convert dB (-60 to 0) to 0..segmentCount
        val activeFraction = ((decibels + 60f) / 60f).coerceIn(0f, 1f)
        val activeSegments = (activeFraction * segmentCount).toInt()

        for (i in 0 until segmentCount) {
            val x = i * (segWidth + spacing)
            val isLit = i <= activeSegments

            // Segment color zone:
            // 0..16: Safe Green (-60 to -24 dB)
            // 17..22: Warm Amber (-24 to -6 dB)
            // 23..27: Peak Red (-6 to 0 dB)
            val baseColor = when {
                i >= 23 -> RecordRed
                i >= 17 -> AccentAmber
                else -> AccentGreen
            }

            val litColor = if (isLit) baseColor else baseColor.copy(alpha = 0.12f)

            drawRoundRect(
                color = litColor,
                topLeft = Offset(x, topY),
                size = Size(segWidth, segHeight),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }

        // Draw dB scale markers at bottom
        val scaleLabels = listOf(
            0.0f to "-60",
            0.33f to "-40",
            0.6f to "-20",
            0.8f to "-10",
            1.0f to "0 dB"
        )

        scaleLabels.forEach { (fraction, _) ->
            val tickX = (fraction * width).coerceIn(1.dp.toPx(), width - 1.dp.toPx())
            drawLine(
                color = Color(0xFF424765),
                start = Offset(tickX, topY + segHeight + 3.dp.toPx()),
                end = Offset(tickX, topY + segHeight + 7.dp.toPx()),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

/**
 * Studio Action Buttons: Snapshot, Pause/Resume, and Stop
 */
@Composable
private fun HUDControlActions(
    isPaused: Boolean,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onSnapshot: () -> Unit,
    onStop: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Quick Snapshot Button
        IconButton(
            onClick = onSnapshot,
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
                .testTag("hud_snapshot_button")
        ) {
            Icon(
                imageVector = Icons.Filled.CameraAlt,
                contentDescription = "Capture Snapshot",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }

        // Pause / Resume Main Button
        IconButton(
            onClick = { if (isPaused) onResume() else onPause() },
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .then(
                    if (isPaused) {
                        Modifier.background(AccentCyan)
                    } else {
                        Modifier.background(
                            Brush.linearGradient(listOf(Color(0xFF262B44), Color(0xFF1B1E32)))
                        )
                    }
                )
                .border(
                    2.dp,
                    if (isPaused) AccentCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    CircleShape
                )
                .testTag("hud_pause_resume_button")
        ) {
            Icon(
                imageVector = if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                contentDescription = if (isPaused) "Resume" else "Pause",
                tint = if (isPaused) Color.Black else Color.White,
                modifier = Modifier.size(34.dp)
            )
        }

        // Finish & Save Stop Button
        IconButton(
            onClick = onStop,
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(RecordRed, RecordRedDark)
                    )
                )
                .testTag("hud_stop_button")
        ) {
            Icon(
                imageVector = Icons.Filled.Stop,
                contentDescription = "Stop Recording",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
