package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExtractedAudioEntity
import com.example.data.model.ProjectEntity
import com.example.ui.theme.StudioAccent
import com.example.ui.theme.StudioError
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioSecondary
import com.example.ui.theme.StudioSuccess
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioWarning
import com.example.ui.viewmodel.AudioEditorTab
import kotlin.math.roundToInt

@Composable
fun AudioControlSheet(
    project: ProjectEntity?,
    activeTab: AudioEditorTab,
    isExtracting: Boolean,
    extractedAudiosForProject: List<ExtractedAudioEntity>,
    onTabSelected: (AudioEditorTab) -> Unit,
    onVideoVolumeChange: (Float) -> Unit,
    onToggleVideoMute: () -> Unit,
    onBgmVolumeChange: (Float) -> Unit,
    onToggleBgmMute: () -> Unit,
    onOpenBgmLibrary: () -> Unit,
    onPickCustomAudio: () -> Unit,
    onRemoveBgm: () -> Unit,
    onToggleDucking: () -> Unit,
    onDuckingAmountChange: (Float) -> Unit,
    onEqPresetChange: (String) -> Unit,
    onExtractAudio: (String) -> Unit,
    onUseExtractedAsBgm: (ExtractedAudioEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = StudioSurface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262A3B)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("audio_control_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // Tab Header Row
            TabRow(
                selectedTabIndex = activeTab.ordinal,
                containerColor = StudioSurfaceVariant,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[activeTab.ordinal]),
                        color = when (activeTab) {
                            AudioEditorTab.VOLUME -> StudioSecondary
                            AudioEditorTab.BGM -> StudioAccent
                            AudioEditorTab.EXTRACT -> StudioPrimary
                            AudioEditorTab.EQUALIZER -> StudioSuccess
                        }
                    )
                },
                divider = {}
            ) {
                Tab(
                    selected = activeTab == AudioEditorTab.VOLUME,
                    onClick = { onTabSelected(AudioEditorTab.VOLUME) },
                    text = { Text("Video Volume", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    selectedContentColor = StudioSecondary,
                    unselectedContentColor = Color(0xFF94A3B8),
                    modifier = Modifier.testTag("tab_video_volume")
                )

                Tab(
                    selected = activeTab == AudioEditorTab.BGM,
                    onClick = { onTabSelected(AudioEditorTab.BGM) },
                    text = { Text("BGM Music", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    selectedContentColor = StudioAccent,
                    unselectedContentColor = Color(0xFF94A3B8),
                    modifier = Modifier.testTag("tab_bgm_music")
                )

                Tab(
                    selected = activeTab == AudioEditorTab.EXTRACT,
                    onClick = { onTabSelected(AudioEditorTab.EXTRACT) },
                    text = { Text("Extract Audio", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    selectedContentColor = StudioPrimary,
                    unselectedContentColor = Color(0xFF94A3B8),
                    modifier = Modifier.testTag("tab_extract_audio")
                )

                Tab(
                    selected = activeTab == AudioEditorTab.EQUALIZER,
                    onClick = { onTabSelected(AudioEditorTab.EQUALIZER) },
                    text = { Text("EQ & FX", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    selectedContentColor = StudioSuccess,
                    unselectedContentColor = Color(0xFF94A3B8),
                    modifier = Modifier.testTag("tab_eq_fx")
                )
            }

            // Tab Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                when (activeTab) {
                    AudioEditorTab.VOLUME -> {
                        VideoVolumeTabContent(
                            project = project,
                            onVolumeChange = onVideoVolumeChange,
                            onToggleMute = onToggleVideoMute
                        )
                    }
                    AudioEditorTab.BGM -> {
                        BgmTrackTabContent(
                            project = project,
                            onVolumeChange = onBgmVolumeChange,
                            onToggleMute = onToggleBgmMute,
                            onOpenLibrary = onOpenBgmLibrary,
                            onPickAudio = onPickCustomAudio,
                            onRemoveBgm = onRemoveBgm,
                            onToggleDucking = onToggleDucking,
                            onDuckingAmountChange = onDuckingAmountChange
                        )
                    }
                    AudioEditorTab.EXTRACT -> {
                        ExtractAudioTabContent(
                            project = project,
                            isExtracting = isExtracting,
                            extractedAudios = extractedAudiosForProject,
                            onExtract = onExtractAudio,
                            onUseAsBgm = onUseExtractedAsBgm
                        )
                    }
                    AudioEditorTab.EQUALIZER -> {
                        EqualizerTabContent(
                            project = project,
                            onEqChange = onEqPresetChange
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoVolumeTabContent(
    project: ProjectEntity?,
    onVolumeChange: (Float) -> Unit,
    onToggleMute: () -> Unit
) {
    val currentVolume = project?.videoVolume ?: 1.0f
    val isMuted = project?.isVideoMuted == true
    val percentage = (currentVolume * 100).roundToInt()

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Volume Display Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1F2E)),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C3249)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isMuted) StudioError.copy(alpha = 0.2f) else StudioSecondary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = if (isMuted) StudioError else StudioSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Original Video Audio",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isMuted) "Audio Output Muted" else if (currentVolume > 1.0f) "Volume Boost Active (+${percentage - 100}%)" else "Normal Gain",
                                fontSize = 11.sp,
                                color = if (isMuted) StudioError else if (currentVolume > 1.0f) StudioAccent else Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Percentage Gauge
                    Surface(
                        color = if (isMuted) StudioError.copy(alpha = 0.15f) else StudioSecondary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isMuted) StudioError else StudioSecondary)
                    ) {
                        Text(
                            text = if (isMuted) "0%" else "$percentage%",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (isMuted) StudioError else StudioSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Slider
                Slider(
                    value = if (isMuted) 0f else currentVolume,
                    onValueChange = { newVol ->
                        if (isMuted) onToggleMute()
                        onVolumeChange(newVol)
                    },
                    valueRange = 0f..2.0f,
                    steps = 19,
                    colors = SliderDefaults.colors(
                        thumbColor = StudioSecondary,
                        activeTrackColor = StudioSecondary,
                        inactiveTrackColor = Color(0xFF2C3249)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("slider_video_volume")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0% (Silent)", fontSize = 10.sp, color = Color(0xFF64748B))
                    Text("100% (Default)", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                    Text("200% (Boost)", fontSize = 10.sp, color = StudioAccent, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Quick Preset Chips & Mute Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onToggleMute,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isMuted) StudioError else Color(0xFF24283B),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .testTag("btn_mute_video_audio")
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isMuted) "Unmute" else "Mute", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            listOf(0.5f to "50%", 1.0f to "100%", 1.5f to "150%", 2.0f to "200%").forEach { (v, label) ->
                FilterChip(
                    selected = !isMuted && (currentVolume - v).let { kotlin.math.abs(it) < 0.05f },
                    onClick = {
                        if (isMuted) onToggleMute()
                        onVolumeChange(v)
                    },
                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StudioSecondary,
                        selectedLabelColor = Color(0xFF0F172A),
                        containerColor = Color(0xFF1B1E2B)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}

@Composable
private fun BgmTrackTabContent(
    project: ProjectEntity?,
    onVolumeChange: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onOpenLibrary: () -> Unit,
    onPickAudio: () -> Unit,
    onRemoveBgm: () -> Unit,
    onToggleDucking: () -> Unit,
    onDuckingAmountChange: (Float) -> Unit
) {
    val hasBgm = !project?.bgmTrackUri.isNullOrBlank()
    val bgmTitle = project?.bgmTrackTitle ?: "No Music Added"
    val bgmVolume = project?.bgmVolume ?: 0.6f
    val isBgmMuted = project?.isBgmMuted == true
    val percentage = (bgmVolume * 100).roundToInt()
    val duckingActive = project?.audioDucking == true
    val duckingAmount = project?.duckingAmount ?: 0.35f

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Active BGM Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D2B)),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (hasBgm) StudioAccent.copy(alpha = 0.5f) else Color(0xFF2C3249)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(StudioAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = StudioAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = bgmTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (hasBgm) "Background Soundtrack Layer" else "Select a track to enhance your video",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    if (hasBgm) {
                        IconButton(
                            onClick = onRemoveBgm,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove BGM",
                                tint = StudioError,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (hasBgm) {
                    Spacer(modifier = Modifier.height(14.dp))

                    // BGM Volume Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Music Track Volume",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = if (isBgmMuted) "MUTED (0%)" else "$percentage%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (isBgmMuted) StudioError else StudioAccent
                        )
                    }

                    Slider(
                        value = if (isBgmMuted) 0f else bgmVolume,
                        onValueChange = {
                            if (isBgmMuted) onToggleMute()
                            onVolumeChange(it)
                        },
                        valueRange = 0f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = StudioAccent,
                            activeTrackColor = StudioAccent,
                            inactiveTrackColor = Color(0xFF2C3249)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("slider_bgm_volume")
                    )

                    // Quick percentage chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(0.2f to "20% (Low)", 0.5f to "50% (Balanced)", 0.8f to "80% (Rich)", 1.0f to "100%").forEach { (v, label) ->
                            FilterChip(
                                selected = !isBgmMuted && (bgmVolume - v).let { kotlin.math.abs(it) < 0.05f },
                                onClick = {
                                    if (isBgmMuted) onToggleMute()
                                    onVolumeChange(v)
                                },
                                label = { Text(label, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StudioAccent,
                                    selectedLabelColor = Color(0xFF0F172A),
                                    containerColor = Color(0xFF131520)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons: Open Music Library / Pick Custom File
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onOpenLibrary,
                colors = ButtonDefaults.buttonColors(
                    containerColor = StudioAccent,
                    contentColor = Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("btn_browse_bgm")
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Browse BGM Library", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onPickAudio,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioSecondary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioSecondary),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("btn_import_custom_audio")
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Import Audio File", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Auto Audio Ducking Section
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141724)),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF242A3D)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = StudioSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Auto Audio Ducking",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "Lowers background music automatically while video dialogue/speech is audible",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Switch(
                        checked = duckingActive,
                        onCheckedChange = { onToggleDucking() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = StudioSecondary,
                            checkedTrackColor = StudioSecondary.copy(alpha = 0.4f),
                            uncheckedThumbColor = Color(0xFF64748B),
                            uncheckedTrackColor = Color(0xFF1E2235)
                        ),
                        modifier = Modifier.testTag("switch_ducking")
                    )
                }

                if (duckingActive) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Music Reduction: ${(duckingAmount * 100).toInt()}% attenuation",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = StudioSecondary
                    )
                    Slider(
                        value = duckingAmount,
                        onValueChange = onDuckingAmountChange,
                        valueRange = 0.1f..0.8f,
                        colors = SliderDefaults.colors(
                            thumbColor = StudioSecondary,
                            activeTrackColor = StudioSecondary,
                            inactiveTrackColor = Color(0xFF2C3249)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun ExtractAudioTabContent(
    project: ProjectEntity?,
    isExtracting: Boolean,
    extractedAudios: List<ExtractedAudioEntity>,
    onExtract: (String) -> Unit,
    onUseAsBgm: (ExtractedAudioEntity) -> Unit
) {
    var customTitle by remember { mutableStateOf("${project?.title ?: "Clip"}_Audio") }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Hero Extraction Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181B2B)),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioPrimary.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(StudioPrimary.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Audiotrack,
                            contentDescription = null,
                            tint = StudioPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Extract Audio from Video",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Separates pristine audio track into a standalone M4A/AAC file",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Track Title Input
                OutlinedTextField(
                    value = customTitle,
                    onValueChange = { customTitle = it },
                    label = { Text("Extracted Audio File Name", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = StudioPrimary,
                        unfocusedBorderColor = Color(0xFF333B54)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_extract_audio_title")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Extract Button
                Button(
                    onClick = { onExtract(customTitle) },
                    enabled = !isExtracting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StudioPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("btn_extract_audio_action")
                ) {
                    if (isExtracting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Extracting Media Track...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Extract Audio Now", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tech Specs Footnote
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text("Format: AAC / M4A", fontSize = 10.sp, color = Color(0xFF64748B), fontFamily = FontFamily.Monospace)
                    Text("•", fontSize = 10.sp, color = Color(0xFF475569))
                    Text("Quality: 192 kbps", fontSize = 10.sp, color = Color(0xFF64748B), fontFamily = FontFamily.Monospace)
                    Text("•", fontSize = 10.sp, color = Color(0xFF475569))
                    Text("Sample Rate: 44.1 kHz", fontSize = 10.sp, color = Color(0xFF64748B), fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Extracted Clips from This Project
        if (extractedAudios.isNotEmpty()) {
            Text(
                text = "EXTRACTED AUDIO FROM THIS VIDEO",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )

            extractedAudios.forEach { audio ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141724)),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222738)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StudioPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Audiotrack,
                                    contentDescription = null,
                                    tint = StudioPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = audio.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${audio.durationMs / 1000}s • ${(audio.fileSizeBytes / 1024)} KB • ${audio.mimeType}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8),
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Button(
                            onClick = { onUseAsBgm(audio) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StudioSecondary.copy(alpha = 0.2f),
                                contentColor = StudioSecondary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Use as BGM", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EqualizerTabContent(
    project: ProjectEntity?,
    onEqChange: (String) -> Unit
) {
    val activePreset = project?.eqPreset ?: "Studio Warmth"
    val presets = listOf(
        "Studio Warmth" to "Adds analog harmonics and subtle low-mid presence",
        "Bass Boost" to "Deepens low frequencies (60Hz - 150Hz)",
        "Vocal Clarity" to "Enhances dialogue intelligibility (1kHz - 4kHz)",
        "Flat / Neutral" to "Unprocessed, authentic raw video audio capture"
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "AUDIO EQUALIZATION PRESETS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )

        presets.forEach { (preset, description) ->
            val isSelected = activePreset == preset
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFF1E243A) else Color(0xFF141724)
                ),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) StudioSuccess else Color(0xFF23273A)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEqChange(preset) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = preset,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) StudioSuccess else Color.White
                        )
                        Text(
                            text = description,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = StudioSuccess,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
