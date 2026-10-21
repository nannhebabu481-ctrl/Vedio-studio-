package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.BgmTrack
import com.example.data.model.ExtractedAudioEntity
import com.example.data.model.ProjectEntity
import com.example.data.repository.StudioRepository
import com.example.engine.PlaybackState
import com.example.engine.StudioAudioMixer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class StudioScreen {
    EDITOR,
    PROJECTS,
    EXTRACTED_AUDIO,
    BGM_LIBRARY
}

enum class AudioEditorTab {
    VOLUME,
    BGM,
    EXTRACT,
    EQUALIZER
}

class VideoStudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudioRepository
    private val audioMixer: StudioAudioMixer

    val allProjects: StateFlow<List<ProjectEntity>>
    val allExtractedAudio: StateFlow<List<ExtractedAudioEntity>>

    private val _activeProject = MutableStateFlow<ProjectEntity?>(null)
    val activeProject: StateFlow<ProjectEntity?> = _activeProject.asStateFlow()

    private val _currentScreen = MutableStateFlow(StudioScreen.EDITOR)
    val currentScreen: StateFlow<StudioScreen> = _currentScreen.asStateFlow()

    private val _activeTab = MutableStateFlow(AudioEditorTab.VOLUME)
    val activeTab: StateFlow<AudioEditorTab> = _activeTab.asStateFlow()

    val playbackState: StateFlow<PlaybackState>

    private val _isExtracting = MutableStateFlow(false)
    val isExtracting: StateFlow<Boolean> = _isExtracting.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = StudioRepository(application, database.videoStudioDao())
        audioMixer = StudioAudioMixer(application)
        playbackState = audioMixer.playbackState

        allProjects = repository.allProjects
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allExtractedAudio = repository.allExtractedAudio
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            allProjects.collect { projects ->
                if (_activeProject.value == null && projects.isNotEmpty()) {
                    loadProject(projects.first())
                }
            }
        }
    }

    fun setScreen(screen: StudioScreen) {
        _currentScreen.value = screen
    }

    fun setActiveTab(tab: AudioEditorTab) {
        _activeTab.value = tab
    }

    fun loadProject(project: ProjectEntity) {
        _activeProject.value = project
        audioMixer.prepareProject(project)
    }

    fun selectProjectById(id: Long) {
        val found = allProjects.value.find { it.id == id }
        if (found != null) {
            loadProject(found)
            _currentScreen.value = StudioScreen.EDITOR
        }
    }

    // Video Volume Adjustment
    fun updateVideoVolume(newVolume: Float) {
        val current = _activeProject.value ?: return
        val updated = current.copy(videoVolume = newVolume.coerceIn(0f, 2f))
        _activeProject.value = updated
        audioMixer.applyVolumeSettings(updated)
        viewModelScope.launch {
            repository.updateProject(updated)
        }
    }

    fun toggleVideoMute() {
        val current = _activeProject.value ?: return
        val updated = current.copy(isVideoMuted = !current.isVideoMuted)
        _activeProject.value = updated
        audioMixer.applyVolumeSettings(updated)
        viewModelScope.launch {
            repository.updateProject(updated)
        }
    }

    // BGM Adjustment
    fun updateBgmVolume(newVolume: Float) {
        val current = _activeProject.value ?: return
        val updated = current.copy(bgmVolume = newVolume.coerceIn(0f, 1f))
        _activeProject.value = updated
        audioMixer.applyVolumeSettings(updated)
        viewModelScope.launch {
            repository.updateProject(updated)
        }
    }

    fun toggleBgmMute() {
        val current = _activeProject.value ?: return
        val updated = current.copy(isBgmMuted = !current.isBgmMuted)
        _activeProject.value = updated
        audioMixer.applyVolumeSettings(updated)
        viewModelScope.launch {
            repository.updateProject(updated)
        }
    }

    fun setBgmTrack(preset: BgmTrack) {
        val current = _activeProject.value ?: return
        val uri = "preset:${preset.id}"
        val updated = current.copy(
            bgmTrackUri = uri,
            bgmTrackTitle = preset.title,
            isBgmMuted = false
        )
        _activeProject.value = updated
        audioMixer.prepareBgmPlayer(updated)
        audioMixer.applyVolumeSettings(updated)
        viewModelScope.launch {
            repository.updateProject(updated)
            _userMessage.emit("Added BGM track: ${preset.title}")
        }
    }

    fun setCustomBgm(fileUri: String, title: String) {
        val current = _activeProject.value ?: return
        val updated = current.copy(
            bgmTrackUri = fileUri,
            bgmTrackTitle = title,
            isBgmMuted = false
        )
        _activeProject.value = updated
        audioMixer.prepareBgmPlayer(updated)
        audioMixer.applyVolumeSettings(updated)
        viewModelScope.launch {
            repository.updateProject(updated)
            _userMessage.emit("Added audio track: $title")
        }
    }

    fun removeBgmTrack() {
        val current = _activeProject.value ?: return
        val updated = current.copy(bgmTrackUri = null, bgmTrackTitle = null)
        _activeProject.value = updated
        audioMixer.prepareBgmPlayer(updated)
        audioMixer.applyVolumeSettings(updated)
        viewModelScope.launch {
            repository.updateProject(updated)
            _userMessage.emit("Removed BGM track")
        }
    }

    fun toggleAudioDucking() {
        val current = _activeProject.value ?: return
        val updated = current.copy(audioDucking = !current.audioDucking)
        _activeProject.value = updated
        audioMixer.applyVolumeSettings(updated)
        viewModelScope.launch {
            repository.updateProject(updated)
        }
    }

    fun updateDuckingAmount(factor: Float) {
        val current = _activeProject.value ?: return
        val updated = current.copy(duckingAmount = factor.coerceIn(0.1f, 0.9f))
        _activeProject.value = updated
        audioMixer.applyVolumeSettings(updated)
        viewModelScope.launch {
            repository.updateProject(updated)
        }
    }

    fun setEqPreset(preset: String) {
        val current = _activeProject.value ?: return
        val updated = current.copy(eqPreset = preset)
        _activeProject.value = updated
        viewModelScope.launch {
            repository.updateProject(updated)
            _userMessage.emit("Applied EQ Preset: $preset")
        }
    }

    fun setFadeSettings(fadeInMs: Long, fadeOutMs: Long) {
        val current = _activeProject.value ?: return
        val updated = current.copy(fadeInMs = fadeInMs, fadeOutMs = fadeOutMs)
        _activeProject.value = updated
        viewModelScope.launch {
            repository.updateProject(updated)
        }
    }

    // Audio Extraction
    fun extractAudio(customTitle: String? = null) {
        val current = _activeProject.value ?: return
        viewModelScope.launch {
            _isExtracting.value = true
            val title = customTitle?.takeIf { it.isNotBlank() } ?: "${current.title} (Audio)"
            val result = repository.extractAudio(current.id, current.videoUri, title)
            _isExtracting.value = false
            result.onSuccess { extracted ->
                _userMessage.emit("Audio extracted successfully: ${extracted.title} (${extracted.durationMs / 1000}s)")
            }.onFailure { err ->
                _userMessage.emit("Extraction error: ${err.message ?: "Unknown"}")
            }
        }
    }

    fun useExtractedAudioAsBgm(audio: ExtractedAudioEntity) {
        val current = _activeProject.value ?: return
        val updated = current.copy(
            bgmTrackUri = audio.fileUri,
            bgmTrackTitle = audio.title,
            isBgmMuted = false
        )
        _activeProject.value = updated
        audioMixer.prepareBgmPlayer(updated)
        audioMixer.applyVolumeSettings(updated)
        viewModelScope.launch {
            repository.updateProject(updated)
            _userMessage.emit("Set '${audio.title}' as BGM track")
            _currentScreen.value = StudioScreen.EDITOR
        }
    }

    fun deleteExtractedAudio(audio: ExtractedAudioEntity) {
        viewModelScope.launch {
            repository.deleteExtractedAudio(audio)
            _userMessage.emit("Deleted audio clip")
        }
    }

    // Create New Project (e.g. from imported video or template)
    fun createProject(title: String, videoUri: String, durationMs: Long = 15000L) {
        viewModelScope.launch {
            val newProject = ProjectEntity(
                title = title,
                videoUri = videoUri,
                videoDurationMs = durationMs,
                videoVolume = 1.0f,
                isVideoMuted = false,
                bgmTrackUri = null,
                bgmTrackTitle = null,
                bgmVolume = 0.6f,
                isBgmMuted = false,
                thumbnailResName = "img_studio_hero"
            )
            val newId = repository.insertProject(newProject)
            val created = newProject.copy(id = newId)
            loadProject(created)
            _currentScreen.value = StudioScreen.EDITOR
            _userMessage.emit("Created project: $title")
        }
    }

    // Playback controls
    fun togglePlayPause() = audioMixer.togglePlayPause()
    fun play() = audioMixer.play()
    fun pause() = audioMixer.pause()
    fun seekTo(positionMs: Long) = audioMixer.seekTo(positionMs)

    override fun onCleared() {
        super.onCleared()
        audioMixer.releasePlayers()
    }
}
