package com.example

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.RecorderState
import com.example.ui.MediaFilter
import com.example.ui.ScreenRecorderViewModel
import com.example.ui.SubScreen
import com.example.ui.components.AudioExtractorDialog
import com.example.ui.components.CompletedRecordingDialog
import com.example.ui.components.CompressorDialog
import com.example.ui.components.FacecamOverlay
import com.example.ui.components.GifConverterDialog
import com.example.ui.components.LowStorageWarningDialog
import com.example.ui.components.PermissionManagerDialog
import com.example.ui.components.PrivacyDialog
import com.example.ui.components.ProFeaturesDialog
import com.example.ui.components.VideoPlayerDialog
import com.example.ui.components.VideoTrimmerDialog
import com.example.ui.components.WatermarkDialog
import com.example.ui.screens.RecordScreen
import com.example.ui.screens.RecordingsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudioToolsScreen
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RecordRed

class MainActivity : ComponentActivity() {

    private val viewModel: ScreenRecorderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = settings.darkTheme) {
                ScreenRecorderApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenRecorderApp(viewModel: ScreenRecorderViewModel) {
    val currentScreen by viewModel.currentSubScreen.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val recorderState by viewModel.recorderState.collectAsStateWithLifecycle()
    val storageInfo by viewModel.storageInfo.collectAsStateWithLifecycle()
    val recordings by viewModel.displayedRecordings.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    // Dialog & overlay states
    val activePlayingRecording by viewModel.activePlayingRecording.collectAsStateWithLifecycle()
    val activeTrimmingRecording by viewModel.activeTrimmingRecording.collectAsStateWithLifecycle()
    val activeCompressRecording by viewModel.activeCompressRecording.collectAsStateWithLifecycle()
    val activeGifRecording by viewModel.activeGifRecording.collectAsStateWithLifecycle()
    val activeAudioExtractRecording by viewModel.activeAudioExtractRecording.collectAsStateWithLifecycle()
    val isProDialogOpen by viewModel.isProDialogOpen.collectAsStateWithLifecycle()
    val isPrivacyDialogOpen by viewModel.isPrivacyDialogOpen.collectAsStateWithLifecycle()
    val isPermissionDialogOpen by viewModel.isPermissionDialogOpen.collectAsStateWithLifecycle()
    val isWatermarkDialogOpen by viewModel.isWatermarkDialogOpen.collectAsStateWithLifecycle()
    val isLowStorageWarningOpen by viewModel.isLowStorageWarningOpen.collectAsStateWithLifecycle()
    val customWatermarkText by viewModel.customWatermarkText.collectAsStateWithLifecycle()
    val isCustomWatermarkEnabled by viewModel.isCustomWatermarkEnabled.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val mediaProjectionManager = remember {
        context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
    }

    val screenCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            viewModel.setProjectionToken(result.resultCode, result.data)
        }
        viewModel.startRecording()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        val intent = mediaProjectionManager?.createScreenCaptureIntent()
        if (intent != null) {
            try {
                screenCaptureLauncher.launch(intent)
            } catch (e: Exception) {
                viewModel.startRecording()
            }
        } else {
            viewModel.startRecording()
        }
    }

    val proceedWithCapture: () -> Unit = {
        val neededPermissions = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.RECORD_AUDIO)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (neededPermissions.isNotEmpty()) {
            permissionLauncher.launch(neededPermissions.toTypedArray())
        } else {
            val intent = mediaProjectionManager?.createScreenCaptureIntent()
            if (intent != null) {
                try {
                    screenCaptureLauncher.launch(intent)
                } catch (e: Exception) {
                    viewModel.startRecording()
                }
            } else {
                viewModel.startRecording()
            }
        }
    }

    val requestRecordingWithStorageCheck = {
        viewModel.checkStorageAndStartRecording {
            proceedWithCapture()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.userFeedbackMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.screenvolt_logo),
                            contentDescription = "ScreenVolt Logo",
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(7.dp))
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "ScreenVolt",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AccentAmber.copy(alpha = 0.2f),
                            border = BorderStroke(0.5.dp, AccentAmber)
                        ) {
                            Text(
                                text = "PRO",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                ),
                                color = AccentAmber,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.openProDialog(true) },
                        modifier = Modifier.testTag("top_pro_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Diamond,
                            contentDescription = "Pro Features",
                            tint = AccentAmber
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_bottom_navigation"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                SubScreen.values().forEach { screen ->
                    val selected = currentScreen == screen
                    NavigationBarItem(
                        selected = selected,
                        onClick = { viewModel.selectSubScreen(screen) },
                        icon = {
                            Icon(
                                imageVector = when (screen) {
                                    SubScreen.RECORD -> if (selected) Icons.Filled.Videocam else Icons.Outlined.Videocam
                                    SubScreen.RECORDINGS -> if (selected) Icons.Filled.Folder else Icons.Outlined.Folder
                                    SubScreen.TOOLS -> if (selected) Icons.Filled.AutoFixHigh else Icons.Outlined.AutoFixHigh
                                    SubScreen.SETTINGS -> if (selected) Icons.Filled.Settings else Icons.Outlined.Settings
                                },
                                contentDescription = screen.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RecordRed,
                            selectedTextColor = RecordRed,
                            indicatorColor = RecordRed.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_${screen.name.lowercase()}")
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 4 Sub-Screens
            when (currentScreen) {
                SubScreen.RECORD -> {
                    RecordScreen(
                        viewModel = viewModel,
                        settings = settings,
                        recorderState = recorderState,
                        storageInfo = storageInfo,
                        onRequestStartRecording = requestRecordingWithStorageCheck
                    )
                }
                SubScreen.RECORDINGS -> {
                    RecordingsScreen(
                        viewModel = viewModel,
                        recordings = recordings,
                        selectedFilter = selectedFilter,
                        searchQuery = searchQuery
                    )
                }
                SubScreen.TOOLS -> {
                    StudioToolsScreen(
                        viewModel = viewModel,
                        recordings = recordings
                    )
                }
                SubScreen.SETTINGS -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        settings = settings,
                        storageInfo = storageInfo
                    )
                }
            }

            // Optional Facecam PIP overlay
            if (settings.showFacecam) {
                FacecamOverlay()
            }
        }
    }

    // Modal Dialogs
    activePlayingRecording?.let { rec ->
        VideoPlayerDialog(
            recording = rec,
            onDismiss = { viewModel.closePlayer() },
            onTrim = { viewModel.openTrimmer(it) }
        )
    }

    activeTrimmingRecording?.let { rec ->
        VideoTrimmerDialog(
            recording = rec,
            onDismiss = { viewModel.closeTrimmer() },
            onSaveTrimmed = { startMs, endMs, title ->
                viewModel.saveTrimmedClip(rec, startMs, endMs, title)
            }
        )
    }

    activeCompressRecording?.let { rec ->
        CompressorDialog(
            recording = rec,
            onDismiss = { viewModel.closeCompressor() },
            onCompress = { reduction ->
                viewModel.saveCompressedVideo(rec, reduction)
            }
        )
    }

    activeGifRecording?.let { rec ->
        GifConverterDialog(
            recording = rec,
            onDismiss = { viewModel.closeGifConverter() },
            onSaveGif = { fps, quality ->
                viewModel.saveGif(rec, fps, quality)
            }
        )
    }

    activeAudioExtractRecording?.let { rec ->
        AudioExtractorDialog(
            recording = rec,
            onDismiss = { viewModel.closeAudioExtractor() },
            onExtract = { format ->
                viewModel.saveExtractedAudio(rec, format)
            }
        )
    }

    if (isWatermarkDialogOpen) {
        WatermarkDialog(
            initialText = customWatermarkText,
            initialEnabled = isCustomWatermarkEnabled,
            onDismiss = { viewModel.openWatermarkDialog(false) },
            onSave = { text, enabled ->
                viewModel.setWatermark(text, enabled)
            }
        )
    }

    if (isProDialogOpen) {
        ProFeaturesDialog(onDismiss = { viewModel.openProDialog(false) })
    }

    if (isPrivacyDialogOpen) {
        PrivacyDialog(onDismiss = { viewModel.openPrivacyDialog(false) })
    }

    if (isPermissionDialogOpen) {
        PermissionManagerDialog(
            isOpen = true,
            onDismiss = { viewModel.openPermissionDialog(false) }
        )
    }

    if (isLowStorageWarningOpen) {
        LowStorageWarningDialog(
            storageInfo = storageInfo,
            settings = settings,
            onDismiss = { viewModel.dismissLowStorageWarning() },
            onProceedAnyway = {
                viewModel.proceedRecordingDespiteWarning {
                    proceedWithCapture()
                }
            },
            onApplySpaceSaver = {
                viewModel.applySpaceSaverPreset()
            },
            onManageRecordings = {
                viewModel.dismissLowStorageWarning()
                viewModel.selectSubScreen(SubScreen.RECORDINGS)
            }
        )
    }

    if (recorderState is RecorderState.Completed) {
        val completed = recorderState as RecorderState.Completed
        CompletedRecordingDialog(
            completed = completed,
            onDismiss = { viewModel.dismissCompletedRecording() },
            onViewInRecordings = {
                viewModel.dismissCompletedRecording()
                viewModel.selectSubScreen(SubScreen.RECORDINGS)
            }
        )
    }
}
