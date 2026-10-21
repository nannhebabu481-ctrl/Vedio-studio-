package com.example.data.repository

import android.content.Context
import com.example.data.db.VideoStudioDao
import com.example.data.model.ExtractedAudioEntity
import com.example.data.model.ProjectEntity
import com.example.engine.AudioExtractor
import com.example.engine.DemoMediaGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class StudioRepository(
    private val context: Context,
    private val dao: VideoStudioDao
) {
    val allProjects: Flow<List<ProjectEntity>> = dao.getAllProjects()
    val allExtractedAudio: Flow<List<ExtractedAudioEntity>> = dao.getAllExtractedAudio()

    fun getProject(id: Long): Flow<ProjectEntity?> = dao.getProjectById(id)

    suspend fun insertProject(project: ProjectEntity): Long = withContext(Dispatchers.IO) {
        dao.insertProject(project)
    }

    suspend fun updateProject(project: ProjectEntity) = withContext(Dispatchers.IO) {
        dao.updateProject(project.copy(lastModified = System.currentTimeMillis()))
    }

    suspend fun deleteProject(project: ProjectEntity) = withContext(Dispatchers.IO) {
        dao.deleteProject(project)
    }

    suspend fun extractAudio(
        projectId: Long?,
        videoUri: String,
        customTitle: String? = null
    ): Result<ExtractedAudioEntity> = withContext(Dispatchers.IO) {
        val result = AudioExtractor.extractAudioFromVideo(context, projectId, videoUri, customTitle)
        result.onSuccess { extracted ->
            dao.insertExtractedAudio(extracted)
        }
        result
    }

    suspend fun deleteExtractedAudio(audio: ExtractedAudioEntity) = withContext(Dispatchers.IO) {
        dao.deleteExtractedAudio(audio)
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        // Pre-generate demo files so immediate playback works flawlessly
        DemoMediaGenerator.getOrCreateSampleVideoAudio(context)
        DemoMediaGenerator.getOrCreateBgmFile(context, "bgm_cinematic")
        DemoMediaGenerator.getOrCreateBgmFile(context, "bgm_synthwave")
        DemoMediaGenerator.getOrCreateBgmFile(context, "bgm_lofi")

        val currentProjects = dao.getProjectByIdDirect(1L)
        if (currentProjects == null) {
            val starterProject = ProjectEntity(
                id = 1L,
                title = "Cinematic Travel Reel",
                videoUri = "sample://video_nature",
                videoDurationMs = 18000L,
                videoVolume = 1.0f,
                isVideoMuted = false,
                bgmTrackUri = "preset:bgm_cinematic",
                bgmTrackTitle = "Cinematic Horizon",
                bgmVolume = 0.65f,
                isBgmMuted = false,
                audioDucking = true,
                duckingAmount = 0.40f,
                eqPreset = "Studio Warmth",
                thumbnailResName = "img_studio_hero"
            )
            dao.insertProject(starterProject)

            val starterProject2 = ProjectEntity(
                id = 2L,
                title = "Cyber City Drone Shot",
                videoUri = "sample://video_cyber",
                videoDurationMs = 14000L,
                videoVolume = 0.8f,
                isVideoMuted = false,
                bgmTrackUri = "preset:bgm_synthwave",
                bgmTrackTitle = "Cyber Neon Beat",
                bgmVolume = 0.80f,
                isBgmMuted = false,
                audioDucking = false,
                duckingAmount = 0.35f,
                eqPreset = "Bass Boost",
                thumbnailResName = "img_studio_hero"
            )
            dao.insertProject(starterProject2)
        }
    }
}
