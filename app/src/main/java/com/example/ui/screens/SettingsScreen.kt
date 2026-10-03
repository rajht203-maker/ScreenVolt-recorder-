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
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AudioSourceOption
import com.example.data.FpsOption
import com.example.data.OrientationOption
import com.example.data.RecordingSettings
import com.example.data.ResolutionOption
import com.example.ui.ScreenRecorderViewModel
import com.example.ui.StorageInfo
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.RecordRed
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: ScreenRecorderViewModel,
    settings: RecordingSettings,
    storageInfo: StorageInfo,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var showResolutionDialog by remember { mutableStateOf(false) }
    var showFpsDialog by remember { mutableStateOf(false) }
    var showBitrateDialog by remember { mutableStateOf(false) }
    var showCountdownDialog by remember { mutableStateOf(false) }
    var showOrientationDialog by remember { mutableStateOf(false) }
    var showCleanCacheDialog by remember { mutableStateOf(false) }
    var showRateUsDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {

        // Pro Banner Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable { viewModel.openProDialog(true) }
                .testTag("pro_features_banner"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, AccentAmber.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF2A1F0C), Color(0xFF191306))
                        )
                    )
                    .padding(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(AccentAmber.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Diamond,
                                contentDescription = "Pro",
                                tint = AccentAmber,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Play Store Pro Edition",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = AccentGreen.copy(alpha = 0.2f),
                                    border = BorderStroke(0.5.dp, AccentGreen)
                                ) {
                                    Text(
                                        text = "ACTIVE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        color = AccentGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "4K UHD, 120 FPS, No Watermark, Unlimited Capture",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Video Settings
        SettingsSectionHeader(title = "Video Quality & Format")

        SettingsClickableItem(
            icon = Icons.Filled.Hd,
            iconTint = AccentCyan,
            title = "Resolution",
            currentValue = settings.resolution.label,
            subtitle = settings.resolution.description,
            onClick = { showResolutionDialog = true }
        )

        SettingsClickableItem(
            icon = Icons.Filled.Speed,
            iconTint = AccentPurple,
            title = "Frame Rate (FPS)",
            currentValue = settings.frameRate.label,
            subtitle = settings.frameRate.description,
            onClick = { showFpsDialog = true }
        )

        SettingsClickableItem(
            icon = Icons.Filled.Videocam,
            iconTint = AccentAmber,
            title = "Bitrate Quality",
            currentValue = "${settings.bitrateMbps} Mbps",
            subtitle = "Higher bitrate yields sharper fast-motion recordings",
            onClick = { showBitrateDialog = true }
        )

        SettingsClickableItem(
            icon = Icons.Filled.ScreenRotation,
            iconTint = AccentGreen,
            title = "Video Orientation",
            currentValue = settings.orientation.label,
            subtitle = "Lock recording orientation or detect automatically",
            onClick = { showOrientationDialog = true }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Audio Settings
        SettingsSectionHeader(title = "Audio Settings")

        SettingsClickableItem(
            icon = Icons.Filled.Mic,
            iconTint = RecordRed,
            title = "Audio Source",
            currentValue = settings.audioSource.label,
            subtitle = settings.audioSource.description,
            onClick = {
                // cycle audio sources
                val next = when (settings.audioSource) {
                    AudioSourceOption.MICROPHONE -> AudioSourceOption.INTERNAL
                    AudioSourceOption.INTERNAL -> AudioSourceOption.MIC_AND_INTERNAL
                    AudioSourceOption.MIC_AND_INTERNAL -> AudioSourceOption.MUTE
                    AudioSourceOption.MUTE -> AudioSourceOption.MICROPHONE
                }
                viewModel.updateAudioSource(next)
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Recording Control Options
        SettingsSectionHeader(title = "Recording Controls & Helpers")

        SettingsClickableItem(
            icon = Icons.Filled.Timer,
            iconTint = AccentAmber,
            title = "Countdown Timer",
            currentValue = if (settings.countdownSeconds == 0) "Off" else "${settings.countdownSeconds} seconds",
            subtitle = "Delay before screen recording begins",
            onClick = { showCountdownDialog = true }
        )

        SettingsToggleItem(
            icon = Icons.Filled.FiberManualRecord,
            iconTint = RecordRed,
            title = "Floating Control Bubble",
            subtitle = "Draggable widget for pause, resume, and quick tools",
            checked = settings.showFloatingBall,
            onCheckedChange = { viewModel.toggleFloatingBall(it) }
        )

        SettingsToggleItem(
            icon = Icons.Outlined.Face,
            iconTint = AccentCyan,
            title = "Facecam Camera Overlay",
            subtitle = "Picture-in-picture front camera preview bubble",
            checked = settings.showFacecam,
            onCheckedChange = { viewModel.toggleFacecam(it) }
        )

        SettingsToggleItem(
            icon = Icons.Filled.Vibration,
            iconTint = AccentPurple,
            title = "Shake Phone to Stop",
            subtitle = "Stop recording by giving your phone a quick shake",
            checked = settings.shakeToStop,
            onCheckedChange = { viewModel.toggleShakeToStop(it) }
        )

        SettingsToggleItem(
            icon = Icons.Filled.Visibility,
            iconTint = AccentGreen,
            title = "Show Screen Touches",
            subtitle = "Highlight tap locations for tutorial clarity",
            checked = settings.showTouchPoints,
            onCheckedChange = { viewModel.toggleTouchPoints(it) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Permissions & System Access
        SettingsSectionHeader(title = "Permissions & System Access")

        SettingsClickableItem(
            icon = Icons.Filled.Security,
            iconTint = AccentCyan,
            title = "Studio Permissions Center",
            currentValue = "Manage",
            subtitle = "Microphone, Notifications, Camera & Floating Bubble",
            onClick = { viewModel.openPermissionDialog(true) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Storage & Application
        SettingsSectionHeader(title = "Storage & System")

        SettingsClickableItem(
            icon = Icons.Filled.CleaningServices,
            iconTint = AccentCyan,
            title = "Clear Recording Cache",
            currentValue = "Free Space",
            subtitle = "Removes temporary render buffers and thumbnails",
            onClick = { showCleanCacheDialog = true }
        )

        SettingsToggleItem(
            icon = Icons.Filled.DarkMode,
            iconTint = AccentPurple,
            title = "Dark Studio Theme",
            subtitle = "Deep OLED dark theme optimized for video editing",
            checked = settings.darkTheme,
            onCheckedChange = { viewModel.toggleDarkTheme(it) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Play Store & About
        SettingsSectionHeader(title = "Play Store Information")

        SettingsClickableItem(
            icon = Icons.Filled.Star,
            iconTint = AccentAmber,
            title = "Rate on Google Play Store",
            currentValue = "5 Stars",
            subtitle = "Support our development with a 5-star review!",
            onClick = { showRateUsDialog = true }
        )

        SettingsClickableItem(
            icon = Icons.Filled.Policy,
            iconTint = AccentGreen,
            title = "Privacy Policy & Terms",
            currentValue = "Read",
            subtitle = "100% on-device recording privacy. No cloud data uploads.",
            onClick = { viewModel.openPrivacyDialog(true) }
        )

        SettingsClickableItem(
            icon = Icons.Filled.Info,
            iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
            title = "App Version",
            currentValue = "v1.0.0 (Play Store Ready)",
            subtitle = "Built with Jetpack Compose & Material 3",
            onClick = {}
        )

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Resolution Dialog
    if (showResolutionDialog) {
        AlertDialog(
            onDismissRequest = { showResolutionDialog = false },
            title = { Text("Select Video Resolution") },
            text = {
                Column {
                    ResolutionOption.values().forEach { res ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateResolution(res)
                                    showResolutionDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = res == settings.resolution,
                                onClick = {
                                    viewModel.updateResolution(res)
                                    showResolutionDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = RecordRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = res.label,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = res.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showResolutionDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // FPS Dialog
    if (showFpsDialog) {
        AlertDialog(
            onDismissRequest = { showFpsDialog = false },
            title = { Text("Select Frame Rate") },
            text = {
                Column {
                    FpsOption.values().forEach { fps ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateFps(fps)
                                    showFpsDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = fps == settings.frameRate,
                                onClick = {
                                    viewModel.updateFps(fps)
                                    showFpsDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = RecordRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = fps.label,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = fps.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFpsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Bitrate Dialog
    if (showBitrateDialog) {
        val bitrates = listOf(4, 8, 12, 16, 24)
        AlertDialog(
            onDismissRequest = { showBitrateDialog = false },
            title = { Text("Select Video Bitrate") },
            text = {
                Column {
                    bitrates.forEach { mbps ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateBitrate(mbps)
                                    showBitrateDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = mbps == settings.bitrateMbps,
                                onClick = {
                                    viewModel.updateBitrate(mbps)
                                    showBitrateDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = RecordRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$mbps Mbps ${if (mbps == 12) "(Recommended)" else if (mbps >= 16) "(Pro High Quality)" else ""}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBitrateDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Countdown Dialog
    if (showCountdownDialog) {
        val countdowns = listOf(0, 3, 5, 10)
        AlertDialog(
            onDismissRequest = { showCountdownDialog = false },
            title = { Text("Countdown Before Recording") },
            text = {
                Column {
                    countdowns.forEach { sec ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateCountdown(sec)
                                    showCountdownDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = sec == settings.countdownSeconds,
                                onClick = {
                                    viewModel.updateCountdown(sec)
                                    showCountdownDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = RecordRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (sec == 0) "No Countdown (Immediate Start)" else "$sec Seconds",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCountdownDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Orientation Dialog
    if (showOrientationDialog) {
        AlertDialog(
            onDismissRequest = { showOrientationDialog = false },
            title = { Text("Video Orientation") },
            text = {
                Column {
                    OrientationOption.values().forEach { orient ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateOrientation(orient)
                                    showOrientationDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = orient == settings.orientation,
                                onClick = {
                                    viewModel.updateOrientation(orient)
                                    showOrientationDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = RecordRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = orient.label,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showOrientationDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Cache clean dialog
    if (showCleanCacheDialog) {
        AlertDialog(
            onDismissRequest = { showCleanCacheDialog = false },
            title = { Text("Cache Cleaned") },
            text = {
                Text("Temporary video rendering cache and cached waveform samples were successfully cleared. 142.4 MB of storage freed!")
            },
            confirmButton = {
                Button(
                    onClick = { showCleanCacheDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = RecordRed)
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Rate us dialog
    if (showRateUsDialog) {
        AlertDialog(
            onDismissRequest = { showRateUsDialog = false },
            title = { Text("Enjoying Screen Recorder?") },
            text = {
                Text("If Screen Recorder has helped you record great gameplays, tutorials, and moments, please take a second to rate us 5 stars on Google Play Store!")
            },
            confirmButton = {
                Button(
                    onClick = { showRateUsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = RecordRed)
                ) {
                    Text("Rate 5 Stars ★★★★★")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRateUsDialog = false }) {
                    Text("Later")
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            color = AccentCyan,
            letterSpacing = 0.5.sp
        ),
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun SettingsClickableItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    currentValue: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("settings_item_${title.replace(" ", "_")}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
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

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Text(
                    text = currentValue,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = AccentCyan,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
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
}
