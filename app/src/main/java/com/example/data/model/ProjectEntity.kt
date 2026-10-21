package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val videoUri: String,
    val videoDurationMs: Long = 15000L,
    val videoVolume: Float = 1.0f, // 0.0f to 2.0f (200% volume boost)
    val isVideoMuted: Boolean = false,
    val bgmTrackUri: String? = null,
    val bgmTrackTitle: String? = null,
    val bgmVolume: Float = 0.6f, // 0.0f to 1.0f
    val isBgmMuted: Boolean = false,
    val audioDucking: Boolean = false,
    val duckingAmount: Float = 0.35f, // BGM volume reduction factor during dialogue
    val fadeInMs: Long = 0L,
    val fadeOutMs: Long = 0L,
    val eqPreset: String = "Studio Warmth",
    val lastModified: Long = System.currentTimeMillis(),
    val thumbnailResName: String? = null
)
