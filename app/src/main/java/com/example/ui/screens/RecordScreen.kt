package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.data.AudioSourceOption
import com.example.data.FpsOption
import com.example.data.RecordingSettings
import com.example.data.ResolutionOption
import com.example.service.LiveAudioMetrics
import com.example.service.RecorderState
import com.example.ui.ScreenRecorderViewModel
import com.example.ui.StorageInfo
import com.example.ui.components.RealtimeAudioRecordingHUD
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.RecordRed
import com.example.ui.theme.RecordRedDark
import com.example.ui.theme.RecordRedGlow
import com.example.ui.theme.StudioCardBorder
import com.example.ui.components.QuadViewSessionPlannerCard
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen(
    viewModel: ScreenRecorderViewModel,
    settings: RecordingSettings,
    recorderState: RecorderState,
    storageInfo: StorageInfo,
    onRequestStartRecording: () -> Unit = { viewModel.startRecording() },
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Top Studio Status & Storage Header
            StudioStatusCard(
                storageInfo = storageInfo,
                recorderState = recorderState,
                settings = settings
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quad-View Studio Mode & Long-Duration Storage Planner
            QuadViewSessionPlannerCard(
                settings = settings,
                storageInfo = storageInfo,
                onToggleQuadView = { viewModel.setQuadViewMode(it) },
                onDurationSelected = { viewModel.setTargetDurationMinutes(it) },
                onApplySpaceSaver = { viewModel.applySpaceSaverPreset() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Main Recording Center Stage (Huge Animated Record Button / Active Recording HUD)
            when (recorderState) {
                is RecorderState.Idle -> {
                    IdleRecordStage(
                        settings = settings,
                        onStartRecording = onRequestStartRecording,
                        onQuickScreenshot = { viewModel.takeScreenshot() }
                    )
                }
                is RecorderState.Countdown -> {
                    CountdownStage(secondsLeft = recorderState.secondsLeft)
                }
                is RecorderState.Recording -> {
                    ActiveRecordingHUD(
                        durationSeconds = recorderState.durationSeconds,
                        sizeBytes = recorderState.sizeBytesEstimate,
                        amplitudes = recorderState.audioAmplitudes,
                        audioMetrics = recorderState.audioMetrics,
                        isPaused = false,
                        settings = settings,
                        onPause = { viewModel.pauseRecording() },
                        onResume = { viewModel.resumeRecording() },
                        onStop = { viewModel.stopRecording() },
                        onSnapshot = { viewModel.takeScreenshot() }
                    )
                }
                is RecorderState.Paused -> {
                    ActiveRecordingHUD(
                        durationSeconds = recorderState.durationSeconds,
                        sizeBytes = recorderState.sizeBytesEstimate,
                        amplitudes = emptyList(),
                        audioMetrics = recorderState.audioMetrics,
                        isPaused = true,
                        settings = settings,
                        onPause = { viewModel.pauseRecording() },
                        onResume = { viewModel.resumeRecording() },
                        onStop = { viewModel.stopRecording() },
                        onSnapshot = { viewModel.takeScreenshot() }
                    )
                }
                is RecorderState.Completed -> {
                    // Handled in parent dialog or idle
                    IdleRecordStage(
                        settings = settings,
                        onStartRecording = onRequestStartRecording,
                        onQuickScreenshot = { viewModel.takeScreenshot() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Studio Presets (Resolution, FPS, Audio Source)
            ResolutionAndFpsSelector(
                currentResolution = settings.resolution,
                currentFps = settings.frameRate,
                onResolutionSelected = { viewModel.updateResolution(it) },
                onFpsSelected = { viewModel.updateFps(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Audio Mode Selector
            AudioSourceSelector(
                currentAudioSource = settings.audioSource,
                onAudioSourceSelected = { viewModel.updateAudioSource(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Studio Helper Toggles (Floating Ball, Facecam, Shake to Stop, Touch Indicator)
            StudioToolsToggles(
                settings = settings,
                onToggleFloating = { viewModel.toggleFloatingBall(it) },
                onToggleFacecam = { viewModel.toggleFacecam(it) },
                onToggleShake = { viewModel.toggleShakeToStop(it) },
                onToggleTouchPoints = { viewModel.toggleTouchPoints(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Studio Permissions Quick Access Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { viewModel.openPermissionDialog(true) },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(AccentCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Security,
                                contentDescription = "Permissions",
                                tint = AccentCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "Recording & Audio Permissions",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap to check Mic, Notifications & Floating Overlay",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AccentCyan.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Check",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = AccentCyan,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StudioStatusCard(
    storageInfo: StorageInfo,
    recorderState: RecorderState,
    settings: RecordingSettings
) {
    val freeGb = String.format(Locale.getDefault(), "%.1f", storageInfo.freeSpaceBytes / (1024.0 * 1024.0 * 1024.0))
    val estHours = storageInfo.estimatedMinutesLeft / 60
    val estMins = storageInfo.estimatedMinutesLeft % 60

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("studio_status_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            when (recorderState) {
                                is RecorderState.Recording -> RecordRed.copy(alpha = 0.2f)
                                is RecorderState.Paused -> AccentPurple.copy(alpha = 0.2f)
                                else -> AccentCyan.copy(alpha = 0.15f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (recorderState) {
                            is RecorderState.Recording -> Icons.Filled.FiberManualRecord
                            is RecorderState.Paused -> Icons.Filled.Pause
                            else -> Icons.Outlined.Storage
                        },
                        contentDescription = "Studio Status",
                        tint = when (recorderState) {
                            is RecorderState.Recording -> RecordRed
                            is RecorderState.Paused -> AccentPurple
                            else -> AccentCyan
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = when (recorderState) {
                            is RecorderState.Recording -> "RECORDER ACTIVE"
                            is RecorderState.Paused -> "RECORDING PAUSED"
                            is RecorderState.Countdown -> "PREPARING CAPTURE"
                            else -> "READY TO RECORD"
                        },
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = when (recorderState) {
                            is RecorderState.Recording -> RecordRed
                            is RecorderState.Paused -> AccentPurple
                            else -> AccentCyan
                        }
                    )
                    Text(
                        text = "${settings.resolution.label} • ${settings.frameRate.label}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Storage pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "$freeGb GB Free",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = AccentGreen
                    )
                    Text(
                        text = "~${estHours}h ${estMins}m rec",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun IdleRecordStage(
    settings: RecordingSettings,
    onStartRecording: () -> Unit,
    onQuickScreenshot: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("idle_stage_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.screenvolt_logo),
                contentDescription = "ScreenVolt Logo",
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.5.dp, AccentCyan.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "ScreenVolt Studio",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "RECORD • CAPTURE • SHARE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = AccentCyan,
                modifier = Modifier.padding(top = 4.dp, bottom = if (settings.isQuadViewMode) 12.dp else 22.dp)
            )

            if (settings.isQuadViewMode) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AccentCyan.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.4f)),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AccentCyan)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "QUAD-VIEW MULTI-STREAM ACTIVE (${settings.targetDurationMinutes}m Target)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = AccentCyan
                        )
                    }
                }
            }

            // Animated Glowing Start Button
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(pulseScale),
                contentAlignment = Alignment.Center
            ) {
                // Outer glow halo
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(RecordRedGlow)
                )

                // Inner ring
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(RecordRed, RecordRedDark)
                            )
                        )
                        .clickable { onStartRecording() }
                        .testTag("start_recording_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FiberManualRecord,
                            contentDescription = "Start Recording",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "REC",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action row (Quick Screenshot & Start Button)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onQuickScreenshot,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("quick_screenshot_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoCamera,
                        contentDescription = "Screenshot",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Screenshot",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Button(
                    onClick = onStartRecording,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("start_rec_secondary_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RecordRed)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Videocam,
                        contentDescription = "Start",
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Start Record",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun CountdownStage(secondsLeft: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .testTag("countdown_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(2.dp, RecordRed)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Starting Capture In...",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$secondsLeft",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Black
                ),
                color = RecordRed
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Get your screen ready",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ActiveRecordingHUD(
    durationSeconds: Long,
    sizeBytes: Long,
    amplitudes: List<Float> = emptyList(),
    audioMetrics: LiveAudioMetrics = LiveAudioMetrics(),
    isPaused: Boolean,
    settings: RecordingSettings,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onSnapshot: () -> Unit
) {
    RealtimeAudioRecordingHUD(
        durationSeconds = durationSeconds,
        sizeBytes = sizeBytes,
        audioMetrics = audioMetrics,
        isPaused = isPaused,
        settings = settings,
        onPause = onPause,
        onResume = onResume,
        onStop = onStop,
        onSnapshot = onSnapshot
    )
}

@Composable
fun AudioVisualizerBars(
    amplitudes: List<Float>,
    isMuted: Boolean,
    isPaused: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Filled.MicOff else Icons.Filled.GraphicEq,
                contentDescription = "Audio Level",
                tint = if (isMuted) MaterialTheme.colorScheme.onSurfaceVariant else AccentCyan,
                modifier = Modifier.size(20.dp)
            )

            if (isMuted) {
                Text(
                    text = "Microphone muted (Silent Capture)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (isPaused) {
                Text(
                    text = "Audio stream paused",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(
                    modifier = Modifier.weight(1f).padding(start = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val bars = if (amplitudes.isEmpty()) List(20) { 0.2f } else amplitudes
                    bars.take(20).forEach { amp ->
                        val barHeight = (amp * 36.dp.value).coerceIn(4f, 36f).dp
                        Box(
                            modifier = Modifier
                                .width(3.5.dp)
                                .height(barHeight)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(AccentCyan, AccentPurple)
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ResolutionAndFpsSelector(
    currentResolution: ResolutionOption,
    currentFps: FpsOption,
    onResolutionSelected: (ResolutionOption) -> Unit,
    onFpsSelected: (FpsOption) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Hd,
                        contentDescription = "Resolution",
                        tint = AccentCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Video Resolution & FPS",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "${currentResolution.width}x${currentResolution.height}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Resolution horizontal chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ResolutionOption.values().forEach { res ->
                    val selected = res == currentResolution
                    FilterChip(
                        selected = selected,
                        onClick = { onResolutionSelected(res) },
                        label = {
                            Text(
                                text = res.label.split(" ").firstOrNull() ?: res.label,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RecordRed,
                            selectedLabelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            selectedBorderColor = RecordRed
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // FPS selection row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FpsOption.values().forEach { fps ->
                    val selected = fps == currentFps
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onFpsSelected(fps) },
                        color = if (selected) AccentCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (selected) AccentCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = fps.label,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (selected) AccentCyan else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AudioSourceSelector(
    currentAudioSource: AudioSourceOption,
    onAudioSourceSelected: (AudioSourceOption) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = "Audio Source",
                    tint = AccentPurple,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Audio Capture Source",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AudioSourceOption.values().forEach { source ->
                    val selected = source == currentAudioSource
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onAudioSourceSelected(source) },
                        color = if (selected) AccentPurple.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (selected) AccentPurple else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = when (source) {
                                    AudioSourceOption.MICROPHONE -> Icons.Filled.Mic
                                    AudioSourceOption.INTERNAL -> Icons.Filled.VolumeUp
                                    AudioSourceOption.MIC_AND_INTERNAL -> Icons.Filled.GraphicEq
                                    AudioSourceOption.MUTE -> Icons.Filled.MicOff
                                },
                                contentDescription = source.label,
                                tint = if (selected) AccentPurple else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = source.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 10.sp
                                ),
                                color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudioToolsToggles(
    settings: RecordingSettings,
    onToggleFloating: (Boolean) -> Unit,
    onToggleFacecam: (Boolean) -> Unit,
    onToggleShake: (Boolean) -> Unit,
    onToggleTouchPoints: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Recording Assistant & Helpers",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Floating Ball Toggle
            ToggleRowItem(
                icon = Icons.Filled.FiberManualRecord,
                iconTint = RecordRed,
                title = "Floating Control Ball",
                subtitle = "Quick overlay widget for pause & stop",
                checked = settings.showFloatingBall,
                onCheckedChange = onToggleFloating
            )

            // Facecam PIP Toggle
            ToggleRowItem(
                icon = Icons.Outlined.Face,
                iconTint = AccentCyan,
                title = "Facecam Camera Overlay",
                subtitle = "Show front camera preview bubble",
                checked = settings.showFacecam,
                onCheckedChange = onToggleFacecam
            )

            // Shake to stop
            ToggleRowItem(
                icon = Icons.Outlined.Vibration,
                iconTint = AccentPurple,
                title = "Shake Device to Stop",
                subtitle = "Shake phone immediately to finish recording",
                checked = settings.shakeToStop,
                onCheckedChange = onToggleShake
            )

            // Show Touch Points
            ToggleRowItem(
                icon = Icons.Filled.Visibility,
                iconTint = AccentGreen,
                title = "Show Screen Touches",
                subtitle = "Visual tap indicator for tutorials",
                checked = settings.showTouchPoints,
                onCheckedChange = onToggleTouchPoints
            )
        }
    }
}

@Composable
fun ToggleRowItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = RecordRed
            )
        )
    }
}
