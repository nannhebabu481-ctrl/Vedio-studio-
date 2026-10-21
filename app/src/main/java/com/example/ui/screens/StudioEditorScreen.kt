package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ExtractedAudioEntity
import com.example.data.model.ProjectEntity
import com.example.engine.PlaybackState
import com.example.ui.components.AudioControlSheet
import com.example.ui.components.ExportDialog
import com.example.ui.components.MultiTrackTimeline
import com.example.ui.components.StudioTopBar
import com.example.ui.components.VideoCanvasView
import com.example.ui.theme.StudioBackground
import com.example.ui.viewmodel.AudioEditorTab
import com.example.ui.viewmodel.StudioScreen
import com.example.ui.viewmodel.VideoStudioViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun StudioEditorScreen(
    viewModel: VideoStudioViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val activeProject by viewModel.activeProject.collectAsStateWithLifecycle()
    val allProjects by viewModel.allProjects.collectAsStateWithLifecycle()
    val allExtractedAudio by viewModel.allExtractedAudio.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val isExtracting by viewModel.isExtracting.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showExportDialog by remember { mutableStateOf(false) }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val title = it.lastPathSegment ?: "Imported_Audio"
            viewModel.setCustomBgm(it.toString(), title)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    if (showExportDialog) {
        ExportDialog(
            project = activeProject,
            onDismiss = { showExportDialog = false },
            onExtractAudioOnly = {
                viewModel.extractAudio()
            }
        )
    }

    when (currentScreen) {
        StudioScreen.EDITOR -> {
            Scaffold(
                containerColor = StudioBackground,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    StudioTopBar(
                        currentScreen = currentScreen,
                        projectTitle = activeProject?.title,
                        onNavigate = { viewModel.setScreen(it) },
                        onQuickExport = { showExportDialog = true }
                    )
                },
                modifier = modifier.fillMaxSize().testTag("screen_studio_editor")
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(StudioBackground)
                ) {
                    // Video Viewport Canvas
                    VideoCanvasView(
                        project = activeProject,
                        playbackState = playbackState,
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onRewind = { viewModel.seekTo(0L) }
                    )

                    // Multi-track Timeline
                    MultiTrackTimeline(
                        project = activeProject,
                        playbackState = playbackState,
                        onSeek = { viewModel.seekTo(it) },
                        onToggleVideoMute = { viewModel.toggleVideoMute() },
                        onToggleBgmMute = { viewModel.toggleBgmMute() },
                        onSelectBgmTab = {
                            viewModel.setActiveTab(AudioEditorTab.BGM)
                        }
                    )

                    // Audio Editing Bottom Sheet / Control Tabs
                    AudioControlSheet(
                        project = activeProject,
                        activeTab = activeTab,
                        isExtracting = isExtracting,
                        extractedAudiosForProject = allExtractedAudio.filter { it.projectId == activeProject?.id },
                        onTabSelected = { viewModel.setActiveTab(it) },
                        onVideoVolumeChange = { viewModel.updateVideoVolume(it) },
                        onToggleVideoMute = { viewModel.toggleVideoMute() },
                        onBgmVolumeChange = { viewModel.updateBgmVolume(it) },
                        onToggleBgmMute = { viewModel.toggleBgmMute() },
                        onOpenBgmLibrary = { viewModel.setScreen(StudioScreen.BGM_LIBRARY) },
                        onPickCustomAudio = { audioPickerLauncher.launch("audio/*") },
                        onRemoveBgm = { viewModel.removeBgmTrack() },
                        onToggleDucking = { viewModel.toggleAudioDucking() },
                        onDuckingAmountChange = { viewModel.updateDuckingAmount(it) },
                        onEqPresetChange = { viewModel.setEqPreset(it) },
                        onExtractAudio = { customTitle -> viewModel.extractAudio(customTitle) },
                        onUseExtractedAsBgm = { viewModel.useExtractedAudioAsBgm(it) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        StudioScreen.EXTRACTED_AUDIO -> {
            ExtractedAudioScreen(
                extractedList = allExtractedAudio,
                onBack = { viewModel.setScreen(StudioScreen.EDITOR) },
                onUseAsBgm = { viewModel.useExtractedAudioAsBgm(it) },
                onDelete = { viewModel.deleteExtractedAudio(it) },
                onGoToEditor = {
                    viewModel.setActiveTab(AudioEditorTab.EXTRACT)
                    viewModel.setScreen(StudioScreen.EDITOR)
                }
            )
        }

        StudioScreen.BGM_LIBRARY -> {
            BgmLibraryScreen(
                currentBgmUri = activeProject?.bgmTrackUri,
                onSelectPreset = { preset ->
                    viewModel.setBgmTrack(preset)
                    viewModel.setScreen(StudioScreen.EDITOR)
                },
                onSelectCustomAudio = { uri, title ->
                    viewModel.setCustomBgm(uri, title)
                    viewModel.setScreen(StudioScreen.EDITOR)
                },
                onBack = { viewModel.setScreen(StudioScreen.EDITOR) }
            )
        }

        StudioScreen.PROJECTS -> {
            ProjectsScreen(
                projects = allProjects,
                activeProjectId = activeProject?.id,
                onSelectProject = { proj ->
                    viewModel.loadProject(proj)
                    viewModel.setScreen(StudioScreen.EDITOR)
                },
                onCreateProject = { title, uri, duration ->
                    viewModel.createProject(title, uri, duration)
                },
                onBack = { viewModel.setScreen(StudioScreen.EDITOR) }
            )
        }
    }
}
