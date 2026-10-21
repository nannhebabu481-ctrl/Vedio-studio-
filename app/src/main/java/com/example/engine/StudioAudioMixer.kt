package com.example.engine

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.data.model.ProjectEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import kotlin.math.abs
import kotlin.math.sin

data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val totalDurationMs: Long = 15000L,
    val leftLevel: Float = 0f,
    val rightLevel: Float = 0f,
    val isVideoAudioAudible: Boolean = true,
    val isBgmMuted: Boolean = false,
    val effectiveBgmVolume: Float = 0.6f,
    val effectiveVideoVolume: Float = 1.0f,
    val isDuckingActive: Boolean = false
)

class StudioAudioMixer(private val context: Context) {
    private val TAG = "StudioAudioMixer"

    private var videoAudioPlayer: MediaPlayer? = null
    private var bgmPlayer: MediaPlayer? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var currentProject: ProjectEntity? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val progressRunnable = object : Runnable {
        override fun run() {
            if (_playbackState.value.isPlaying) {
                updateProgressAndMeter()
                mainHandler.postDelayed(this, 50)
            }
        }
    }

    fun prepareProject(project: ProjectEntity) {
        currentProject = project
        releasePlayers()

        // Prepare video audio player
        try {
            videoAudioPlayer = MediaPlayer().apply {
                val uri = Uri.parse(project.videoUri)
                if (project.videoUri.startsWith("content://") || project.videoUri.startsWith("android.resource://")) {
                    setDataSource(context, uri)
                } else {
                    val local = File(project.videoUri)
                    if (local.exists()) {
                        setDataSource(local.absolutePath)
                    } else {
                        // Use generated sample video audio
                        val demo = DemoMediaGenerator.getOrCreateSampleVideoAudio(context)
                        setDataSource(demo.absolutePath)
                    }
                }
                isLooping = true
                prepare()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Fallback video player init: ${e.message}")
            try {
                val demo = DemoMediaGenerator.getOrCreateSampleVideoAudio(context)
                videoAudioPlayer = MediaPlayer().apply {
                    setDataSource(demo.absolutePath)
                    isLooping = true
                    prepare()
                }
            } catch (err: Exception) {
                Log.e(TAG, "Failed fallback video player init", err)
            }
        }

        // Prepare BGM player
        prepareBgmPlayer(project)

        applyVolumeSettings(project)

        val duration = videoAudioPlayer?.duration?.toLong()?.coerceAtLeast(5000L) ?: project.videoDurationMs
        _playbackState.value = _playbackState.value.copy(
            totalDurationMs = duration,
            currentPositionMs = 0L,
            isPlaying = false
        )
    }

    fun prepareBgmPlayer(project: ProjectEntity) {
        bgmPlayer?.release()
        bgmPlayer = null

        val bgmUri = project.bgmTrackUri
        if (bgmUri.isNullOrBlank()) {
            return
        }

        try {
            bgmPlayer = MediaPlayer().apply {
                if (bgmUri.startsWith("content://")) {
                    setDataSource(context, Uri.parse(bgmUri))
                } else if (bgmUri.startsWith("preset:")) {
                    val trackId = bgmUri.removePrefix("preset:")
                    val file = DemoMediaGenerator.getOrCreateBgmFile(context, trackId)
                    setDataSource(file.absolutePath)
                } else {
                    val f = File(bgmUri)
                    if (f.exists()) {
                        setDataSource(f.absolutePath)
                    } else {
                        val file = DemoMediaGenerator.getOrCreateBgmFile(context, "bgm_cinematic")
                        setDataSource(file.absolutePath)
                    }
                }
                isLooping = true
                prepare()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing BGM player", e)
        }
    }

    fun applyVolumeSettings(project: ProjectEntity) {
        currentProject = project

        // Video Audio Volume
        val rawVideoVol = if (project.isVideoMuted) 0f else project.videoVolume.coerceIn(0f, 2f)
        val clampedVideoVol = (rawVideoVol / 2.0f * 1.0f).coerceIn(0f, 1f)
        try {
            videoAudioPlayer?.setVolume(clampedVideoVol, clampedVideoVol)
        } catch (e: Exception) {
            Log.w(TAG, "Error setting video volume: ${e.message}")
        }

        // Ducking calculation
        val isDucking = project.audioDucking && !project.isVideoMuted && rawVideoVol > 0.05f
        val duckFactor = if (isDucking) (1.0f - project.duckingAmount).coerceIn(0.1f, 1f) else 1.0f
        val rawBgmVol = if (project.isBgmMuted || project.bgmTrackUri == null) 0f else project.bgmVolume.coerceIn(0f, 1f)
        val effectiveBgm = rawBgmVol * duckFactor

        try {
            bgmPlayer?.setVolume(effectiveBgm, effectiveBgm)
        } catch (e: Exception) {
            Log.w(TAG, "Error setting BGM volume: ${e.message}")
        }

        _playbackState.value = _playbackState.value.copy(
            isVideoAudioAudible = !project.isVideoMuted && rawVideoVol > 0f,
            isBgmMuted = project.isBgmMuted || rawBgmVol == 0f,
            effectiveBgmVolume = effectiveBgm,
            effectiveVideoVolume = rawVideoVol,
            isDuckingActive = isDucking
        )
    }

    fun play() {
        try {
            videoAudioPlayer?.start()
            bgmPlayer?.start()
            _playbackState.value = _playbackState.value.copy(isPlaying = true)
            mainHandler.post(progressRunnable)
        } catch (e: Exception) {
            Log.e(TAG, "Error playing audio", e)
        }
    }

    fun pause() {
        try {
            videoAudioPlayer?.pause()
            bgmPlayer?.pause()
            mainHandler.removeCallbacks(progressRunnable)
            _playbackState.value = _playbackState.value.copy(
                isPlaying = false,
                leftLevel = 0f,
                rightLevel = 0f
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing audio", e)
        }
    }

    fun togglePlayPause() {
        if (_playbackState.value.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun seekTo(positionMs: Long) {
        val pos = positionMs.toInt()
        try {
            videoAudioPlayer?.seekTo(pos)
            bgmPlayer?.seekTo(pos)
            _playbackState.value = _playbackState.value.copy(currentPositionMs = positionMs)
        } catch (e: Exception) {
            Log.e(TAG, "Error seeking audio", e)
        }
    }

    private fun updateProgressAndMeter() {
        val currentMs = videoAudioPlayer?.currentPosition?.toLong() ?: _playbackState.value.currentPositionMs
        val durationMs = (videoAudioPlayer?.duration?.toLong() ?: _playbackState.value.totalDurationMs).coerceAtLeast(1000L)

        // Calculate dynamic VU meter response based on playing state & volume
        val state = _playbackState.value
        val t = currentMs / 1000.0
        val baseV = if (state.isVideoAudioAudible) state.effectiveVideoVolume.coerceIn(0f, 1f) else 0f
        val baseB = if (!state.isBgmMuted) state.effectiveBgmVolume else 0f
        val combinedEnergy = (baseV * 0.7f + baseB * 0.6f).coerceIn(0f, 1f)

        // Pulsing oscillation for realistic audio spectrum bounce
        val osc1 = (sin(t * 12.0) * 0.5 + 0.5).toFloat()
        val osc2 = (sin(t * 18.0 + 1.2) * 0.5 + 0.5).toFloat()
        val left = (combinedEnergy * (0.6f + 0.4f * osc1)).coerceIn(0.05f, 0.98f)
        val right = (combinedEnergy * (0.55f + 0.45f * osc2)).coerceIn(0.05f, 0.95f)

        _playbackState.value = state.copy(
            currentPositionMs = currentMs,
            totalDurationMs = durationMs,
            leftLevel = if (state.isPlaying) left else 0f,
            rightLevel = if (state.isPlaying) right else 0f
        )
    }

    fun releasePlayers() {
        mainHandler.removeCallbacks(progressRunnable)
        try {
            videoAudioPlayer?.stop()
            videoAudioPlayer?.release()
        } catch (e: Exception) {
            // ignore
        }
        videoAudioPlayer = null

        try {
            bgmPlayer?.stop()
            bgmPlayer?.release()
        } catch (e: Exception) {
            // ignore
        }
        bgmPlayer = null
    }
}
