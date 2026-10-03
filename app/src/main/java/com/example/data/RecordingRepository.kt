package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import java.io.File

class RecordingRepository(private val dao: RecordingDao, private val context: Context) {

    val allRecordings: Flow<List<RecordingEntity>> = dao.getAllRecordings()
    val videos: Flow<List<RecordingEntity>> = dao.getVideos()
    val screenshots: Flow<List<RecordingEntity>> = dao.getScreenshots()
    val favorites: Flow<List<RecordingEntity>> = dao.getFavorites()

    suspend fun initializeWithSampleDataIfEmpty() {
        val count = dao.getCount()
        if (count == 0) {
            val now = System.currentTimeMillis()
            val sampleItems = listOf(
                RecordingEntity(
                    title = "Battle Royale Epic Victory Gameplay",
                    filePath = "sample_gameplay.mp4",
                    durationMs = 184000L, // 3m 4s
                    fileSizeBytes = 54_600_000L, // ~52 MB
                    resolution = "1080p",
                    fps = 60,
                    bitrateMbps = 12,
                    audioSource = "Mic + Internal",
                    timestamp = now - 3600000L * 2,
                    isFavorite = true,
                    isScreenshot = false,
                    tags = "Gaming, 60fps",
                    notes = "Clutch 1v3 endgame finish on ranked match!"
                ),
                RecordingEntity(
                    title = "App Tutorial - Jetpack Compose Navigation",
                    filePath = "sample_tutorial.mp4",
                    durationMs = 345000L, // 5m 45s
                    fileSizeBytes = 78_200_000L, // ~74.5 MB
                    resolution = "1080p",
                    fps = 30,
                    bitrateMbps = 8,
                    audioSource = "Microphone",
                    timestamp = now - 3600000L * 24,
                    isFavorite = true,
                    isScreenshot = false,
                    tags = "Tutorial, Tech",
                    notes = "Explaining state management and responsive layouts."
                ),
                RecordingEntity(
                    title = "Bug Reproduction - Checkout Flow Crash",
                    filePath = "sample_bug_report.mp4",
                    durationMs = 42000L, // 42s
                    fileSizeBytes = 12_400_000L, // ~11.8 MB
                    resolution = "720p",
                    fps = 30,
                    bitrateMbps = 6,
                    audioSource = "Mute",
                    timestamp = now - 3600000L * 48,
                    isFavorite = false,
                    isScreenshot = false,
                    tags = "Bug Report, QA",
                    notes = "Null pointer on address confirm step."
                ),
                RecordingEntity(
                    title = "High Score Leaderboard Screenshot",
                    filePath = "sample_screenshot_1.png",
                    durationMs = 0L,
                    fileSizeBytes = 2_850_000L, // ~2.7 MB
                    resolution = "1080x2400",
                    fps = 0,
                    bitrateMbps = 0,
                    audioSource = "None",
                    timestamp = now - 3600000L * 12,
                    isFavorite = true,
                    isScreenshot = true,
                    tags = "Screenshot, Gaming",
                    notes = "New personal best: 142,500 pts"
                ),
                RecordingEntity(
                    title = "Wireframe Concept Design Capture",
                    filePath = "sample_screenshot_2.png",
                    durationMs = 0L,
                    fileSizeBytes = 1_920_000L,
                    resolution = "1080x2400",
                    fps = 0,
                    bitrateMbps = 0,
                    audioSource = "None",
                    timestamp = now - 3600000L * 60,
                    isFavorite = false,
                    isScreenshot = true,
                    tags = "Design, UI",
                    notes = "Dark mode dashboard mockup"
                )
            )
            dao.insertAll(sampleItems)
        }
    }

    suspend fun saveRecording(recording: RecordingEntity): Long {
        return dao.insertRecording(recording)
    }

    suspend fun updateRecording(recording: RecordingEntity) {
        dao.updateRecording(recording)
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    suspend fun toggleFavorite(id: Long) {
        dao.toggleFavorite(id)
    }

    suspend fun rename(id: Long, newTitle: String) {
        dao.rename(id, newTitle)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}
