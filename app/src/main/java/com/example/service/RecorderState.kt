package com.example.service

data class LiveAudioMetrics(
    val decibels: Float = -60f,
    val normalizedLevel: Float = 0f,
    val peakLevel: Float = 0f,
    val frequencyBins: List<Float> = List(28) { 0.05f },
    val isMicLive: Boolean = false
)

sealed class RecorderState {
    object Idle : RecorderState()
    data class Countdown(val secondsLeft: Int) : RecorderState()
    data class Recording(
        val durationSeconds: Long,
        val sizeBytesEstimate: Long,
        val audioAmplitudes: List<Float> = emptyList(),
        val audioMetrics: LiveAudioMetrics = LiveAudioMetrics()
    ) : RecorderState()
    data class Paused(
        val durationSeconds: Long,
        val sizeBytesEstimate: Long,
        val audioMetrics: LiveAudioMetrics = LiveAudioMetrics()
    ) : RecorderState()
    data class Completed(
        val title: String,
        val filePath: String,
        val durationSeconds: Long,
        val sizeBytes: Long
    ) : RecorderState()
}
