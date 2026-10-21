package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "extracted_audio")
data class ExtractedAudioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long? = null,
    val title: String,
    val filePath: String,
    val fileUri: String,
    val durationMs: Long,
    val fileSizeBytes: Long,
    val sampleRate: Int = 44100,
    val mimeType: String = "audio/mp4",
    val bitRateKbps: Int = 192,
    val createdAt: Long = System.currentTimeMillis()
)
