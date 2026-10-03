package com.example

import com.example.data.FpsOption
import com.example.data.RecordingSettings
import com.example.data.ResolutionOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testStandardRecordingEstimation() {
        val settings = RecordingSettings(
            resolution = ResolutionOption.FHD_1080P,
            frameRate = FpsOption.FPS_60,
            bitrateMbps = 12,
            isQuadViewMode = false,
            targetDurationMinutes = 60
        )
        // 12 Mbps = 1.5 MB/s = 5.4 GB for 60 min
        val estimatedBytes = settings.estimateSessionSizeBytes()
        val estimatedMb = estimatedBytes / (1024 * 1024)
        assertTrue(estimatedMb > 5000)
    }

    @Test
    fun testQuadViewSessionEstimationScalesBitrate() {
        val settings = RecordingSettings(
            resolution = ResolutionOption.FHD_1080P,
            frameRate = FpsOption.FPS_60,
            bitrateMbps = 12,
            isQuadViewMode = true,
            targetDurationMinutes = 30
        )
        // Quad-View scales bitrate by 1.5x (12 * 1.5 = 18 Mbps)
        assertEquals(18, settings.getEffectiveBitrateMbps())
        val estimatedBytes = settings.estimateSessionSizeBytes()
        // 18 Mbps = 2.25 MB/s * 1800s = ~4.05 GB
        assertTrue(estimatedBytes > 3_500_000_000L)
    }

    @Test
    fun testSessionDurationScaling() {
        val baseSettings = RecordingSettings(isQuadViewMode = true, targetDurationMinutes = 15)
        val longerSettings = RecordingSettings(isQuadViewMode = true, targetDurationMinutes = 60)
        assertTrue(longerSettings.estimateSessionSizeBytes() > baseSettings.estimateSessionSizeBytes() * 3)
    }
}
