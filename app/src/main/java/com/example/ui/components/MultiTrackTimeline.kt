package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProjectEntity
import com.example.engine.PlaybackState
import com.example.ui.theme.StudioAccent
import com.example.ui.theme.StudioError
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioSecondary
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import kotlin.math.sin

@Composable
fun MultiTrackTimeline(
    project: ProjectEntity?,
    playbackState: PlaybackState,
    onSeek: (Long) -> Unit,
    onToggleVideoMute: () -> Unit,
    onToggleBgmMute: () -> Unit,
    onSelectBgmTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalMs = playbackState.totalDurationMs.coerceAtLeast(1000L)
    val curMs = playbackState.currentPositionMs.coerceIn(0L, totalMs)
    val progressFraction = (curMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)

    Surface(
        color = StudioSurface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262A3B)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("multi_track_timeline")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Timeline Timecode Header & Ruler
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STUDIO TIMELINE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "00:00",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF6B7280)
                    )
                    Text(
                        text = "00:05",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF6B7280)
                    )
                    Text(
                        text = "00:10",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF6B7280)
                    )
                    Text(
                        text = String.format("00:%02d", (totalMs / 1000).toInt()),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }

            // Interactive timeline container with playhead
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(totalMs) {
                        detectTapGestures { offset ->
                            val width = size.width
                            if (width > 0) {
                                val seekRatio = (offset.x / width).coerceIn(0f, 1f)
                                onSeek((seekRatio * totalMs).toLong())
                            }
                        }
                    }
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // TRACK 1: Video Track
                    TimelineTrackItem(
                        title = "Video Clip",
                        icon = Icons.Default.Movie,
                        iconTint = Color(0xFF818CF8),
                        badgeText = "${(totalMs / 1000)}s",
                        content = {
                            VideoFilmstrip(durationMs = totalMs)
                        }
                    )

                    // TRACK 2: Video Audio Track
                    val isVideoMuted = project?.isVideoMuted == true
                    val videoVolumePct = ((project?.videoVolume ?: 1.0f) * 100).toInt()
                    TimelineTrackItem(
                        title = "Video Audio",
                        icon = if (isVideoMuted) Icons.Default.VolumeMute else Icons.Default.GraphicEq,
                        iconTint = if (isVideoMuted) StudioError else StudioSecondary,
                        badgeText = if (isVideoMuted) "MUTED" else "$videoVolumePct%",
                        onBadgeClick = onToggleVideoMute,
                        content = {
                            AudioWaveformStrip(
                                amplitudeMultiplier = if (isVideoMuted) 0f else (project?.videoVolume ?: 1f).coerceIn(0f, 2f),
                                waveColor = if (isVideoMuted) Color(0xFF4B5563) else StudioSecondary,
                                isSpeechProfile = true
                            )
                        }
                    )

                    // TRACK 3: BGM Music Track
                    val hasBgm = !project?.bgmTrackUri.isNullOrBlank()
                    val isBgmMuted = project?.isBgmMuted == true
                    val bgmVolumePct = ((project?.bgmVolume ?: 0.6f) * 100).toInt()

                    if (hasBgm) {
                        TimelineTrackItem(
                            title = project?.bgmTrackTitle ?: "BGM",
                            icon = Icons.Default.MusicNote,
                            iconTint = StudioAccent,
                            badgeText = if (isBgmMuted) "MUTED" else "$bgmVolumePct%",
                            onBadgeClick = onToggleBgmMute,
                            content = {
                                AudioWaveformStrip(
                                    amplitudeMultiplier = if (isBgmMuted) 0f else (playbackState.effectiveBgmVolume / (project?.bgmVolume ?: 0.6f).coerceAtLeast(0.01f)),
                                    waveColor = if (isBgmMuted) Color(0xFF4B5563) else StudioAccent,
                                    isSpeechProfile = false
                                )
                            }
                        )
                    } else {
                        // Empty BGM slot invitation
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1B1E2B))
                                .border(1.dp, Color(0xFF33384C), RoundedCornerShape(8.dp))
                                .clickable { onSelectBgmTab() }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = StudioSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "+ Add Background Music Track",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = StudioSecondary
                                )
                            }
                        }
                    }
                }

                // Global Scrubber / Playhead Line
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = progressFraction.coerceIn(0.001f, 1f))
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .width(2.5.dp)
                            .height(150.dp)
                            .background(StudioSecondary)
                    )
                    // Playhead thumb
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 5.dp, y = (-2).dp)
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(StudioSecondary)
                            .border(1.5.dp, Color.White, CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineTrackItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    badgeText: String,
    onBadgeClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF141724))
            .border(0.5.dp, Color(0xFF23273A), RoundedCornerShape(8.dp))
            .padding(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Surface(
                color = iconTint.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.clickable(enabled = onBadgeClick != null) { onBadgeClick?.invoke() }
            ) {
                Text(
                    text = badgeText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = iconTint,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        content()
    }
}

@Composable
private fun VideoFilmstrip(durationMs: Long) {
    val frames = 10
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF1E2235)),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        for (i in 0 until frames) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF2C324E), Color(0xFF1A1E2F))
                        )
                    )
                    .border(0.5.dp, Color(0xFF3B4264), RoundedCornerShape(2.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${i * (durationMs / frames / 1000)}s",
                    fontSize = 8.sp,
                    color = Color(0xFF94A3B8),
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun AudioWaveformStrip(
    amplitudeMultiplier: Float,
    waveColor: Color,
    isSpeechProfile: Boolean
) {
    val barCount = 42
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF0F111A))
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val basePattern = if (isSpeechProfile) {
                // Speech: bursts with pauses
                val burst = sin(i * 0.45) * 0.5 + 0.5
                val cadence = if ((i / 6) % 3 == 2) 0.15 else 0.85
                (burst * cadence).toFloat()
            } else {
                // Music: rhythmic peaks
                val beat = if (i % 4 == 0) 0.9f else if (i % 2 == 0) 0.6f else 0.4f
                beat
            }

            val dynamicHeight = (basePattern * amplitudeMultiplier).coerceIn(0.08f, 1.0f)
            val barHeight = (dynamicHeight * 22).dp

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(barHeight)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(if (amplitudeMultiplier > 0f) waveColor else Color(0xFF374151))
            )
        }
    }
}
