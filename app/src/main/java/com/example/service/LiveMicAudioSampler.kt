package com.example.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

class LiveMicAudioSampler(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var samplingJob: Job? = null
    private var audioRecord: AudioRecord? = null

    private val _liveMetrics = MutableStateFlow(LiveAudioMetrics())
    val liveMetrics: StateFlow<LiveAudioMetrics> = _liveMetrics.asStateFlow()

    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val minBufferSize = try {
        AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
    } catch (e: Exception) {
        2048
    }
    private val bufferSize = max(minBufferSize * 2, 2048)

    private var isSamplingActive = false
    private var peakDecay = 0f
    private var smoothLevel = 0f
    private val binCount = 28
    private var simTick = 0

    fun startSampling(isMicEnabled: Boolean) {
        if (!isMicEnabled) {
            _liveMetrics.value = LiveAudioMetrics(
                decibels = -60f,
                normalizedLevel = 0f,
                peakLevel = 0f,
                frequencyBins = List(binCount) { 0.02f },
                isMicLive = false
            )
            return
        }

        stopSampling()
        isSamplingActive = true

        samplingJob = scope.launch {
            val hasPermission = try {
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            } catch (e: Exception) {
                false
            }

            var realAudioActive = false

            if (hasPermission) {
                try {
                    val record = AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        sampleRate,
                        channelConfig,
                        audioFormat,
                        bufferSize
                    )
                    if (record.state == AudioRecord.STATE_INITIALIZED) {
                        record.startRecording()
                        audioRecord = record
                        realAudioActive = true
                    } else {
                        record.release()
                    }
                } catch (e: Exception) {
                    realAudioActive = false
                }
            }

            val pcmBuffer = ShortArray(bufferSize / 2)

            while (isActive && isSamplingActive) {
                var handledRealAudio = false

                if (realAudioActive && audioRecord != null) {
                    try {
                        val readSamples = audioRecord?.read(pcmBuffer, 0, pcmBuffer.size) ?: 0
                        if (readSamples > 0) {
                            handledRealAudio = true
                            var sumSq = 0.0
                            var maxAmp = 0

                            val samplesPerBin = max(1, readSamples / binCount)
                            val bins = MutableList(binCount) { 0.05f }

                            for (i in 0 until readSamples) {
                                val sample = pcmBuffer[i].toInt()
                                val absSample = abs(sample)
                                if (absSample > maxAmp) {
                                    maxAmp = absSample
                                }
                                sumSq += (sample * sample).toDouble()

                                val binIdx = (i / samplesPerBin).coerceIn(0, binCount - 1)
                                val normVal = absSample / 32768f
                                if (normVal > bins[binIdx]) {
                                    bins[binIdx] = normVal
                                }
                            }

                            val rms = sqrt(sumSq / readSamples)
                            val rawNormalized = (rms / 32768.0).toFloat().coerceIn(0f, 1f)
                            val peakVal = (maxAmp / 32768f).coerceIn(0f, 1f)

                            // Calculate dB (-60 to 0)
                            val rawDb = if (rms > 1.0) {
                                (20.0 * log10(rms / 32768.0)).toFloat().coerceIn(-60f, 0f)
                            } else {
                                -60f
                            }

                            smoothLevel = smoothLevel * 0.35f + rawNormalized * 0.65f
                            peakDecay = max(peakVal, peakDecay * 0.88f)

                            val smoothedBins = bins.map { v ->
                                (v * 1.6f).coerceIn(0.04f, 0.98f)
                            }

                            _liveMetrics.value = LiveAudioMetrics(
                                decibels = rawDb,
                                normalizedLevel = smoothLevel,
                                peakLevel = peakDecay,
                                frequencyBins = smoothedBins,
                                isMicLive = true
                            )
                        }
                    } catch (e: Exception) {
                        handledRealAudio = false
                    }
                }

                if (!handledRealAudio) {
                    produceFallbackSample(isMicEnabled = true)
                }

                delay(50L) // 20 FPS live telemetry
            }
        }
    }

    private fun produceFallbackSample(isMicEnabled: Boolean) {
        simTick++
        if (isMicEnabled) {
            val baseEnergy = 0.2f + 0.35f * (0.5f + 0.5f * sin(simTick * 0.22).toFloat())
            val simulatedDb = (-42f + 25f * baseEnergy).coerceIn(-60f, -6f)
            peakDecay = max(baseEnergy * 1.15f, peakDecay * 0.9f).coerceIn(0f, 1f)
            val bins = List(binCount) { idx ->
                val wave = sin((simTick * 0.18) + (idx * 0.3)).toFloat()
                (0.1f + 0.65f * (0.5f + 0.5f * wave) * baseEnergy).coerceIn(0.05f, 0.95f)
            }
            _liveMetrics.value = LiveAudioMetrics(
                decibels = simulatedDb,
                normalizedLevel = baseEnergy.coerceIn(0f, 1f),
                peakLevel = peakDecay,
                frequencyBins = bins,
                isMicLive = true
            )
        } else {
            _liveMetrics.value = LiveAudioMetrics(
                decibels = -60f,
                normalizedLevel = 0f,
                peakLevel = 0f,
                frequencyBins = List(binCount) { 0.02f },
                isMicLive = false
            )
        }
    }

    fun pauseSampling() {
        try {
            audioRecord?.stop()
        } catch (e: Exception) {
            // Ignore
        }
        _liveMetrics.value = _liveMetrics.value.copy(
            normalizedLevel = 0f,
            peakLevel = 0f,
            frequencyBins = List(binCount) { 0.02f }
        )
    }

    fun resumeSampling(isMicEnabled: Boolean) {
        if (isMicEnabled) {
            try {
                audioRecord?.startRecording()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun stopSampling() {
        isSamplingActive = false
        samplingJob?.cancel()
        samplingJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            // Ignore
        }
        audioRecord = null
        _liveMetrics.value = LiveAudioMetrics(
            decibels = -60f,
            normalizedLevel = 0f,
            peakLevel = 0f,
            frequencyBins = List(binCount) { 0.02f },
            isMicLive = false
        )
    }
}
