package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recordings")
data class RecordingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val filePath: String,
    val thumbnailUri: String? = null,
    val durationMs: Long = 0L,
    val fileSizeBytes: Long = 0L,
    val resolution: String = "1080p",
    val fps: Int = 60,
    val bitrateMbps: Int = 12,
    val audioSource: String = "Microphone",
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isScreenshot: Boolean = false,
    val tags: String = "General",
    val notes: String = ""
)
