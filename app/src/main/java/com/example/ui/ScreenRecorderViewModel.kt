package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Environment
import android.os.StatFs
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AudioSourceOption
import com.example.data.FpsOption
import com.example.data.OrientationOption
import com.example.data.RecordingEntity
import com.example.data.RecordingRepository
import com.example.data.RecordingSettings
import com.example.data.ResolutionOption
import com.example.service.RecorderController
import com.example.service.RecorderState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

enum class SubScreen(val title: String, val index: Int) {
    RECORD("Record", 0),
    RECORDINGS("Recordings", 1),
    TOOLS("Tools", 2),
    SETTINGS("Settings", 3)
}

enum class MediaFilter {
    ALL, VIDEOS, SCREENSHOTS, FAVORITES
}

data class StorageInfo(
    val freeSpaceBytes: Long = 42_500_000_000L, // ~42.5 GB
    val totalSpaceBytes: Long = 128_000_000_000L, // 128 GB
    val estimatedMinutesLeft: Long = 480L
)

class ScreenRecorderViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = RecordingRepository(db.recordingDao(), application)
    val controller = RecorderController(application, repository)

    private val _currentSubScreen = MutableStateFlow(SubScreen.RECORD)
    val currentSubScreen: StateFlow<SubScreen> = _currentSubScreen.asStateFlow()

    private val _settings = MutableStateFlow(RecordingSettings())
    val settings: StateFlow<RecordingSettings> = _settings.asStateFlow()

    val recorderState: StateFlow<RecorderState> = controller.recorderState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(MediaFilter.ALL)
    val selectedFilter: StateFlow<MediaFilter> = _selectedFilter.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    private val _storageInfo = MutableStateFlow(StorageInfo())
    val storageInfo: StateFlow<StorageInfo> = _storageInfo.asStateFlow()

    // Dialog & Tool Overlays
    private val _activePlayingRecording = MutableStateFlow<RecordingEntity?>(null)
    val activePlayingRecording: StateFlow<RecordingEntity?> = _activePlayingRecording.asStateFlow()

    private val _activeTrimmingRecording = MutableStateFlow<RecordingEntity?>(null)
    val activeTrimmingRecording: StateFlow<RecordingEntity?> = _activeTrimmingRecording.asStateFlow()

    private val _activeCompressRecording = MutableStateFlow<RecordingEntity?>(null)
    val activeCompressRecording: StateFlow<RecordingEntity?> = _activeCompressRecording.asStateFlow()

    private val _activeGifRecording = MutableStateFlow<RecordingEntity?>(null)
    val activeGifRecording: StateFlow<RecordingEntity?> = _activeGifRecording.asStateFlow()

    private val _activeAudioExtractRecording = MutableStateFlow<RecordingEntity?>(null)
    val activeAudioExtractRecording: StateFlow<RecordingEntity?> = _activeAudioExtractRecording.asStateFlow()

    private val _isProDialogOpen = MutableStateFlow(false)
    val isProDialogOpen: StateFlow<Boolean> = _isProDialogOpen.asStateFlow()

    private val _isPrivacyDialogOpen = MutableStateFlow(false)
    val isPrivacyDialogOpen: StateFlow<Boolean> = _isPrivacyDialogOpen.asStateFlow()

    private val _isPermissionDialogOpen = MutableStateFlow(false)
    val isPermissionDialogOpen: StateFlow<Boolean> = _isPermissionDialogOpen.asStateFlow()

    private val _isWatermarkDialogOpen = MutableStateFlow(false)
    val isWatermarkDialogOpen: StateFlow<Boolean> = _isWatermarkDialogOpen.asStateFlow()

    private val _isLowStorageWarningOpen = MutableStateFlow(false)
    val isLowStorageWarningOpen: StateFlow<Boolean> = _isLowStorageWarningOpen.asStateFlow()

    private val _customWatermarkText = MutableStateFlow("Rec Studio")
    val customWatermarkText: StateFlow<String> = _customWatermarkText.asStateFlow()

    private val _isCustomWatermarkEnabled = MutableStateFlow(false)
    val isCustomWatermarkEnabled: StateFlow<Boolean> = _isCustomWatermarkEnabled.asStateFlow()

    private val _userFeedbackMessage = MutableSharedFlow<String>()
    val userFeedbackMessage: SharedFlow<String> = _userFeedbackMessage.asSharedFlow()

    // Filtered & Searched recordings list
    val displayedRecordings: StateFlow<List<RecordingEntity>> = combine(
        repository.allRecordings,
        _searchQuery,
        _selectedFilter,
        _selectedTag
    ) { list, query, filter, tag ->
        list.filter { item ->
            val matchesQuery = query.isEmpty() || item.title.contains(query, ignoreCase = true) || item.tags.contains(query, ignoreCase = true)
            val matchesFilter = when (filter) {
                MediaFilter.ALL -> true
                MediaFilter.VIDEOS -> !item.isScreenshot
                MediaFilter.SCREENSHOTS -> item.isScreenshot
                MediaFilter.FAVORITES -> item.isFavorite
            }
            val matchesTag = tag == null || item.tags.contains(tag, ignoreCase = true)
            matchesQuery && matchesFilter && matchesTag
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.initializeWithSampleDataIfEmpty()
            calculateStorageInfo()
        }
        viewModelScope.launch {
            controller.events.collect { message ->
                _userFeedbackMessage.emit(message)
            }
        }
    }

    fun selectSubScreen(screen: SubScreen) {
        _currentSubScreen.value = screen
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: MediaFilter) {
        _selectedFilter.value = filter
    }

    fun setSelectedTag(tag: String?) {
        _selectedTag.value = tag
    }

    // Recording operations
    fun setProjectionToken(resultCode: Int, data: android.content.Intent?) {
        controller.setProjectionToken(resultCode, data)
    }

    fun setQuadViewMode(enabled: Boolean) {
        _settings.value = _settings.value.copy(
            isQuadViewMode = enabled,
            showFacecam = if (enabled) true else _settings.value.showFacecam
        )
        controller.updateSettings(_settings.value)
        calculateStorageInfo()
    }

    fun setTargetDurationMinutes(minutes: Int) {
        _settings.value = _settings.value.copy(targetDurationMinutes = minutes)
        calculateStorageInfo()
    }

    fun checkStorageAndStartRecording(onProceedToRecord: () -> Unit) {
        val freeBytes = _storageInfo.value.freeSpaceBytes
        val estimatedSessionBytes = _settings.value.estimateSessionSizeBytes()
        val isQuad = _settings.value.isQuadViewMode

        // Trigger warning if available storage is less than session requirement,
        // less than 2.5 GB buffer, or session consumes > 70% of available disk space
        val isLowStorage = freeBytes < 2_500_000_000L ||
                estimatedSessionBytes > (freeBytes * 0.70f) ||
                (isQuad && freeBytes < 4_500_000_000L && _settings.value.targetDurationMinutes >= 30)

        if (isLowStorage) {
            _isLowStorageWarningOpen.value = true
        } else {
            onProceedToRecord()
        }
    }

    fun proceedRecordingDespiteWarning(onProceedToRecord: () -> Unit) {
        _isLowStorageWarningOpen.value = false
        onProceedToRecord()
    }

    fun dismissLowStorageWarning() {
        _isLowStorageWarningOpen.value = false
    }

    fun applySpaceSaverPreset() {
        _settings.value = _settings.value.copy(
            resolution = ResolutionOption.HD_720P,
            frameRate = FpsOption.FPS_30,
            bitrateMbps = 4
        )
        controller.updateSettings(_settings.value)
        _isLowStorageWarningOpen.value = false
        calculateStorageInfo()
        viewModelScope.launch {
            _userFeedbackMessage.emit("Space-Saver preset enabled (720p • 30fps • 4Mbps)")
        }
    }

    fun startRecording() {
        controller.startRecordingFlow(_settings.value)
    }

    fun pauseRecording() {
        controller.pauseRecording()
    }

    fun resumeRecording() {
        controller.resumeRecording()
    }

    fun stopRecording(customTitle: String? = null) {
        controller.stopRecording(customTitle)
    }

    fun takeScreenshot(customTitle: String? = null) {
        controller.captureScreenshot(customTitle)
    }

    fun dismissCompletedRecording() {
        controller.dismissCompleted()
    }

    // Settings Updates
    fun updateResolution(resolution: ResolutionOption) {
        _settings.value = _settings.value.copy(resolution = resolution)
        controller.updateSettings(_settings.value)
        calculateStorageInfo()
    }

    fun updateFps(fps: FpsOption) {
        _settings.value = _settings.value.copy(frameRate = fps)
        controller.updateSettings(_settings.value)
    }

    fun updateBitrate(mbps: Int) {
        _settings.value = _settings.value.copy(bitrateMbps = mbps)
        controller.updateSettings(_settings.value)
        calculateStorageInfo()
    }

    fun updateAudioSource(source: AudioSourceOption) {
        _settings.value = _settings.value.copy(audioSource = source)
        controller.updateSettings(_settings.value)
    }

    fun updateOrientation(orientation: OrientationOption) {
        _settings.value = _settings.value.copy(orientation = orientation)
    }

    fun updateCountdown(seconds: Int) {
        _settings.value = _settings.value.copy(countdownSeconds = seconds)
    }

    fun toggleFloatingBall(enabled: Boolean) {
        _settings.value = _settings.value.copy(showFloatingBall = enabled)
    }

    fun toggleFacecam(enabled: Boolean) {
        _settings.value = _settings.value.copy(showFacecam = enabled)
    }

    fun toggleShakeToStop(enabled: Boolean) {
        _settings.value = _settings.value.copy(shakeToStop = enabled)
        controller.updateSettings(_settings.value)
    }

    fun toggleTouchPoints(enabled: Boolean) {
        _settings.value = _settings.value.copy(showTouchPoints = enabled)
    }

    fun toggleDarkTheme(enabled: Boolean) {
        _settings.value = _settings.value.copy(darkTheme = enabled)
    }

    fun setWatermark(text: String, enabled: Boolean) {
        _customWatermarkText.value = text
        _isCustomWatermarkEnabled.value = enabled
        _isWatermarkDialogOpen.value = false
    }

    // Media library actions
    fun toggleFavorite(item: RecordingEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleFavorite(item.id)
        }
    }

    fun renameRecording(id: Long, newTitle: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.rename(id, newTitle)
            _userFeedbackMessage.emit("Renamed to: $newTitle")
        }
    }

    fun deleteRecording(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteById(id)
            _userFeedbackMessage.emit("Recording deleted")
            calculateStorageInfo()
        }
    }

    fun clearAllRecordings() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAll()
            _userFeedbackMessage.emit("Cleared all recordings")
            calculateStorageInfo()
        }
    }

    // Modal viewers & tools
    fun openPlayer(recording: RecordingEntity) {
        _activePlayingRecording.value = recording
    }

    fun closePlayer() {
        _activePlayingRecording.value = null
    }

    fun openTrimmer(recording: RecordingEntity) {
        _activeTrimmingRecording.value = recording
    }

    fun closeTrimmer() {
        _activeTrimmingRecording.value = null
    }

    fun openCompressor(recording: RecordingEntity) {
        _activeCompressRecording.value = recording
    }

    fun closeCompressor() {
        _activeCompressRecording.value = null
    }

    fun openGifConverter(recording: RecordingEntity) {
        _activeGifRecording.value = recording
    }

    fun closeGifConverter() {
        _activeGifRecording.value = null
    }

    fun openAudioExtractor(recording: RecordingEntity) {
        _activeAudioExtractRecording.value = recording
    }

    fun closeAudioExtractor() {
        _activeAudioExtractRecording.value = null
    }

    fun openProDialog(open: Boolean) {
        _isProDialogOpen.value = open
    }

    fun openPrivacyDialog(open: Boolean) {
        _isPrivacyDialogOpen.value = open
    }

    fun openPermissionDialog(open: Boolean) {
        _isPermissionDialogOpen.value = open
    }

    fun openWatermarkDialog(open: Boolean) {
        _isWatermarkDialogOpen.value = open
    }

    // Tool processing implementations
    fun saveTrimmedClip(original: RecordingEntity, startMs: Long, endMs: Long, title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val duration = (endMs - startMs).coerceAtLeast(1000L)
            val ratio = duration.toDouble() / original.durationMs.coerceAtLeast(1L)
            val newSize = (original.fileSizeBytes * ratio).toLong().coerceAtLeast(500_000L)
            val timestampStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

            val trimmed = RecordingEntity(
                title = title.ifEmpty { "${original.title} (Trimmed)" },
                filePath = "TRIM_${timestampStr}.mp4",
                durationMs = duration,
                fileSizeBytes = newSize,
                resolution = original.resolution,
                fps = original.fps,
                bitrateMbps = original.bitrateMbps,
                audioSource = original.audioSource,
                timestamp = System.currentTimeMillis(),
                isFavorite = false,
                isScreenshot = false,
                tags = "Trimmed, Clip",
                notes = "Trimmed from ${original.title}"
            )
            repository.saveRecording(trimmed)
            _activeTrimmingRecording.value = null
            _userFeedbackMessage.emit("Trimmed clip saved successfully!")
        }
    }

    fun saveCompressedVideo(original: RecordingEntity, targetReductionPercent: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val factor = (100 - targetReductionPercent) / 100.0
            val newSize = (original.fileSizeBytes * factor).toLong().coerceAtLeast(400_000L)
            val timestampStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

            val compressed = RecordingEntity(
                title = "${original.title} (Compressed -${targetReductionPercent}%)",
                filePath = "COMPRESS_${timestampStr}.mp4",
                durationMs = original.durationMs,
                fileSizeBytes = newSize,
                resolution = if (targetReductionPercent > 50) "720p" else original.resolution,
                fps = if (targetReductionPercent > 60) 30 else original.fps,
                bitrateMbps = (original.bitrateMbps * factor).toInt().coerceAtLeast(2),
                audioSource = original.audioSource,
                timestamp = System.currentTimeMillis(),
                isFavorite = false,
                isScreenshot = false,
                tags = "Compressed, Share Ready",
                notes = "Optimized for quick sharing"
            )
            repository.saveRecording(compressed)
            _activeCompressRecording.value = null
            _userFeedbackMessage.emit("Compressed video saved! Saved ${(original.fileSizeBytes - newSize) / 1_000_000} MB")
        }
    }

    fun saveGif(original: RecordingEntity, fps: Int, width: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val gifDurationMs = original.durationMs.coerceAtMost(10000L) // Max 10s GIF
            val gifSize = (gifDurationMs / 1000L) * fps * 80_000L
            val timestampStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

            val gifItem = RecordingEntity(
                title = "${original.title} (GIF)",
                filePath = "GIF_${timestampStr}.gif",
                durationMs = gifDurationMs,
                fileSizeBytes = gifSize,
                resolution = "${width}p",
                fps = fps,
                bitrateMbps = 0,
                audioSource = "None",
                timestamp = System.currentTimeMillis(),
                isFavorite = false,
                isScreenshot = false,
                tags = "GIF, Animation",
                notes = "Animated GIF created at ${fps} FPS"
            )
            repository.saveRecording(gifItem)
            _activeGifRecording.value = null
            _userFeedbackMessage.emit("Animated GIF created successfully!")
        }
    }

    fun saveExtractedAudio(original: RecordingEntity, format: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val audioBitrateKbps = 192L
            val audioSizeBytes = (original.durationMs / 1000L) * (audioBitrateKbps * 1000L / 8L)
            val timestampStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

            val audioItem = RecordingEntity(
                title = "${original.title} (Audio)",
                filePath = "AUDIO_${timestampStr}.${format.lowercase()}",
                durationMs = original.durationMs,
                fileSizeBytes = audioSizeBytes.coerceAtLeast(300_000L),
                resolution = "Audio Track",
                fps = 0,
                bitrateMbps = 0,
                audioSource = original.audioSource,
                timestamp = System.currentTimeMillis(),
                isFavorite = false,
                isScreenshot = false,
                tags = "Audio, $format",
                notes = "Extracted sound track ($format 192kbps)"
            )
            repository.saveRecording(audioItem)
            _activeAudioExtractRecording.value = null
            _userFeedbackMessage.emit("Extracted $format audio track saved!")
        }
    }

    private fun calculateStorageInfo() {
        try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
            val totalBytes = stat.blockCountLong * stat.blockSizeLong

            val effectiveBitrate = _settings.value.getEffectiveBitrateMbps()
            val bytesPerSec = (effectiveBitrate * 1_000_000L) / 8L
            val estMins = if (bytesPerSec > 0) (availableBytes / bytesPerSec) / 60L else 300L

            _storageInfo.value = StorageInfo(
                freeSpaceBytes = availableBytes,
                totalSpaceBytes = totalBytes,
                estimatedMinutesLeft = estMins
            )
        } catch (e: Exception) {
            _storageInfo.value = StorageInfo()
        }
    }
}
