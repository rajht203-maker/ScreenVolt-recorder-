package com.example.data

data class RecordingSettings(
    val resolution: ResolutionOption = ResolutionOption.FHD_1080P,
    val frameRate: FpsOption = FpsOption.FPS_60,
    val bitrateMbps: Int = 12,
    val audioSource: AudioSourceOption = AudioSourceOption.MICROPHONE,
    val orientation: OrientationOption = OrientationOption.AUTO,
    val countdownSeconds: Int = 3,
    val showFloatingBall: Boolean = true,
    val showFacecam: Boolean = false,
    val shakeToStop: Boolean = true,
    val showTouchPoints: Boolean = true,
    val keepScreenOn: Boolean = true,
    val videoEncoder: String = "H.264 / AVC",
    val audioSampleRate: String = "48 kHz",
    val darkTheme: Boolean = true,
    val isProUnlocked: Boolean = true,
    val isQuadViewMode: Boolean = false,
    val targetDurationMinutes: Int = 30
) {
    fun getEffectiveBitrateMbps(): Int {
        return if (isQuadViewMode) {
            (bitrateMbps * 1.5f).toInt().coerceAtLeast(16)
        } else {
            bitrateMbps
        }
    }

    fun estimateSessionSizeBytes(durationMins: Int = targetDurationMinutes): Long {
        val effectiveBitrate = getEffectiveBitrateMbps()
        val bytesPerSec = (effectiveBitrate * 1_000_000L) / 8L
        return bytesPerSec * (durationMins * 60L)
    }
}

enum class ResolutionOption(val label: String, val width: Int, val height: Int, val description: String) {
    UHD_4K("4K Ultra HD", 3840, 2160, "2160p • Highest Quality"),
    QHD_2K("2K QHD", 2560, 1440, "1440p • Crystal Clear"),
    FHD_1080P("1080p Full HD", 1920, 1080, "1080p • Recommended"),
    HD_720P("720p HD", 1280, 720, "720p • Balanced"),
    SD_480P("480p SD", 854, 480, "480p • Saves Storage")
}

enum class FpsOption(val value: Int, val label: String, val description: String) {
    FPS_24(24, "24 FPS", "Cinematic"),
    FPS_30(30, "30 FPS", "Standard"),
    FPS_60(60, "60 FPS", "Ultra Smooth (Gaming)"),
    FPS_120(120, "120 FPS", "Pro Esports")
}

enum class AudioSourceOption(val label: String, val description: String) {
    MICROPHONE("Microphone", "Record voice and external sounds"),
    INTERNAL("Internal Audio", "Record game and app sound directly"),
    MIC_AND_INTERNAL("Mic + Internal", "Record both gameplay and commentary"),
    MUTE("Mute", "Silent screen capture without sound")
}

enum class OrientationOption(val label: String) {
    AUTO("Auto Detect"),
    PORTRAIT("Portrait"),
    LANDSCAPE("Landscape")
}
