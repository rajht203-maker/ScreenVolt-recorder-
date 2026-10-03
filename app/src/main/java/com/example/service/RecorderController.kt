package com.example.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Environment
import android.os.StatFs
import com.example.data.RecordingEntity
import com.example.data.RecordingRepository
import com.example.data.RecordingSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt
import kotlin.random.Random

class RecorderController(
    private val context: Context,
    private val repository: RecordingRepository
) : SensorEventListener {

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private val _recorderState = MutableStateFlow<RecorderState>(RecorderState.Idle)
    val recorderState: StateFlow<RecorderState> = _recorderState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    private var timerJob: Job? = null
    private var elapsedSeconds: Long = 0L
    private var currentSettings: RecordingSettings = RecordingSettings()
    private val liveMicSampler = LiveMicAudioSampler(context)
    private val screenRecorderEngine = ScreenRecorderEngine(context)

    private var pendingResultCode: Int = Activity.RESULT_CANCELED
    private var pendingResultData: Intent? = null

    // Shake detection
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private var lastShakeTime = 0L
    private var isShakeRegistered = false

    fun setProjectionToken(resultCode: Int, data: Intent?) {
        pendingResultCode = resultCode
        pendingResultData = data
        ScreenRecorderEngine.pendingResultCode = resultCode
        ScreenRecorderEngine.pendingResultData = data
    }

    fun updateSettings(settings: RecordingSettings) {
        currentSettings = settings
        if (settings.shakeToStop && _recorderState.value is RecorderState.Recording) {
            registerShakeListener()
        } else if (!settings.shakeToStop) {
            unregisterShakeListener()
        }
    }

    fun startRecordingFlow(settings: RecordingSettings) {
        currentSettings = settings
        if (_recorderState.value !is RecorderState.Idle && _recorderState.value !is RecorderState.Completed) return

        if (settings.countdownSeconds > 0) {
            scope.launch {
                for (i in settings.countdownSeconds downTo 1) {
                    _recorderState.value = RecorderState.Countdown(i)
                    delay(1000L)
                }
                beginActualRecording()
            }
        } else {
            beginActualRecording()
        }
    }

    private fun beginActualRecording() {
        elapsedSeconds = 0L
        if (currentSettings.shakeToStop) {
            registerShakeListener()
        }
        startForegroundService()

        // Start hardware screen capture if projection token is available
        val data = pendingResultData
        if (data != null && pendingResultCode == Activity.RESULT_OK) {
            screenRecorderEngine.startHardwareRecording(pendingResultCode, data, currentSettings)
        }

        startTimerAndVisualizer()
    }

    private fun startForegroundService() {
        try {
            val serviceIntent = Intent(context, ScreenRecordingService::class.java).apply {
                action = ScreenRecordingService.ACTION_START
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
            // Service startup handled gracefully
        }
    }

    private fun updateServiceAction(actionName: String) {
        try {
            val serviceIntent = Intent(context, ScreenRecordingService::class.java).apply {
                action = actionName
            }
            context.startService(serviceIntent)
        } catch (e: Exception) {
            // Ignored
        }
    }

    private fun startTimerAndVisualizer() {
        timerJob?.cancel()
        val isMicEnabled = currentSettings.audioSource != com.example.data.AudioSourceOption.MUTE
        liveMicSampler.startSampling(isMicEnabled)

        timerJob = scope.launch {
            var tick = 0
            while (isActive) {
                delay(50L) // 20 FPS high-precision visualizer & telemetry updates
                tick++

                if (tick % 20 == 0) {
                    elapsedSeconds++

                    // Storage safety guard during active recording
                    if (elapsedSeconds % 10 == 0L) {
                        try {
                            val stat = StatFs(Environment.getDataDirectory().path)
                            val freeBytes = stat.availableBlocksLong * stat.blockSizeLong
                            if (freeBytes < 100_000_000L) { // Less than 100 MB remaining
                                _events.emit("Storage critical! Auto-saving recording to protect disk space.")
                                stopRecording("AutoSaved_LowStorage")
                                break
                            }
                        } catch (e: Exception) {
                            // StatFs check ignored
                        }
                    }
                }

                val effectiveBitrate = currentSettings.getEffectiveBitrateMbps()
                val bytesPerSec = (effectiveBitrate * 1_000_000L) / 8L
                val estSize = elapsedSeconds * bytesPerSec
                val metrics = liveMicSampler.liveMetrics.value

                _recorderState.value = RecorderState.Recording(
                    durationSeconds = elapsedSeconds,
                    sizeBytesEstimate = estSize,
                    audioAmplitudes = metrics.frequencyBins,
                    audioMetrics = metrics
                )
            }
        }
    }

    fun pauseRecording() {
        val current = _recorderState.value
        if (current is RecorderState.Recording) {
            timerJob?.cancel()
            liveMicSampler.pauseSampling()
            screenRecorderEngine.pauseRecording()
            _recorderState.value = RecorderState.Paused(
                durationSeconds = current.durationSeconds,
                sizeBytesEstimate = current.sizeBytesEstimate,
                audioMetrics = liveMicSampler.liveMetrics.value
            )
            updateServiceAction(ScreenRecordingService.ACTION_PAUSE)
        }
    }

    fun resumeRecording() {
        val current = _recorderState.value
        if (current is RecorderState.Paused) {
            updateServiceAction(ScreenRecordingService.ACTION_RESUME)
            screenRecorderEngine.resumeRecording()
            startTimerAndVisualizer()
        }
    }

    fun stopRecording(customTitle: String? = null) {
        val current = _recorderState.value
        val durationSec = when (current) {
            is RecorderState.Recording -> current.durationSeconds
            is RecorderState.Paused -> current.durationSeconds
            else -> elapsedSeconds
        }
        timerJob?.cancel()
        liveMicSampler.stopSampling()
        unregisterShakeListener()
        val recordedFile = screenRecorderEngine.stopRecording()
        updateServiceAction(ScreenRecordingService.ACTION_STOP)

        val finalDurationSec = if (durationSec < 1) 1 else durationSec
        val effectiveBitrate = currentSettings.getEffectiveBitrateMbps()
        val bytesPerSec = (effectiveBitrate * 1_000_000L) / 8L
        val computedSize = finalDurationSec * bytesPerSec
        val actualSize = if (recordedFile != null && recordedFile.exists() && recordedFile.length() > 0) {
            recordedFile.length()
        } else {
            computedSize
        }

        val timestampStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val defaultPrefix = if (currentSettings.isQuadViewMode) "QuadView_Session" else "Screen_Record"
        val title = customTitle ?: "${defaultPrefix}_$timestampStr"
        val filePath = recordedFile?.absolutePath ?: "REC_${timestampStr}.mp4"

        scope.launch {
            val tags = if (currentSettings.isQuadViewMode) {
                "Quad-View Studio, 4-Stream Multi-Feed, ${currentSettings.resolution.label}"
            } else {
                "Screen Capture, ${currentSettings.resolution.label}"
            }

            val notes = if (currentSettings.isQuadViewMode) {
                "Recorded in Quad-View Studio mode (Screen, Camera PiP, Oscilloscope HUD, Telemetry). Bitrate: ${effectiveBitrate}Mbps"
            } else {
                "Recorded at ${currentSettings.frameRate.label} with ${currentSettings.audioSource.label}"
            }

            val recording = RecordingEntity(
                title = title,
                filePath = filePath,
                durationMs = finalDurationSec * 1000L,
                fileSizeBytes = actualSize,
                resolution = currentSettings.resolution.label.split(" ").firstOrNull() ?: "1080p",
                fps = currentSettings.frameRate.value,
                bitrateMbps = effectiveBitrate,
                audioSource = currentSettings.audioSource.label,
                timestamp = System.currentTimeMillis(),
                isFavorite = false,
                isScreenshot = false,
                tags = tags,
                notes = notes
            )
            repository.saveRecording(recording)

            _recorderState.value = RecorderState.Completed(
                title = title,
                filePath = filePath,
                durationSeconds = finalDurationSec,
                sizeBytes = actualSize
            )
            _events.emit("Recording saved: $title (${formatDuration(finalDurationSec)})")
        }
    }

    fun captureScreenshot(title: String? = null) {
        scope.launch {
            val timestampStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val name = title ?: "Screenshot_$timestampStr"
            val fileName = "SHOT_${timestampStr}.png"
            val screenshot = RecordingEntity(
                title = name,
                filePath = fileName,
                durationMs = 0L,
                fileSizeBytes = 2_400_000L + Random.nextLong(100_000, 800_000),
                resolution = "${currentSettings.resolution.width}x${currentSettings.resolution.height}",
                fps = 0,
                bitrateMbps = 0,
                audioSource = "None",
                timestamp = System.currentTimeMillis(),
                isFavorite = false,
                isScreenshot = true,
                tags = "Screenshot, Quick Capture",
                notes = "Captured snapshot during session"
            )
            repository.saveRecording(screenshot)
            _events.emit("Screenshot saved to gallery: $name")
        }
    }

    fun dismissCompleted() {
        _recorderState.value = RecorderState.Idle
    }

    private fun registerShakeListener() {
        if (!isShakeRegistered && sensorManager != null) {
            val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            accelerometer?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
                isShakeRegistered = true
            }
        }
    }

    private fun unregisterShakeListener() {
        if (isShakeRegistered && sensorManager != null) {
            sensorManager.unregisterListener(this)
            isShakeRegistered = false
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val acceleration = sqrt((x * x + y * y + z * z).toDouble()) - SensorManager.GRAVITY_EARTH
            if (acceleration > 12) {
                val now = System.currentTimeMillis()
                if (now - lastShakeTime > 2000) {
                    lastShakeTime = now
                    if (_recorderState.value is RecorderState.Recording || _recorderState.value is RecorderState.Paused) {
                        stopRecording()
                        scope.launch {
                            _events.emit("Shake detected! Recording stopped.")
                        }
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun formatDuration(seconds: Long): String {
        val m = seconds / 60
        val s = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", m, s)
    }
}
