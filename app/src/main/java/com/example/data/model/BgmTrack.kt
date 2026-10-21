package com.example.data.model

data class BgmTrack(
    val id: String,
    val title: String,
    val artist: String,
    val category: String,
    val durationMs: Long,
    val bpm: Int,
    val colorHex: Long = 0xFF6C5CE7,
    val localUri: String? = null,
    val isCustom: Boolean = false
) {
    companion object {
        val PRESETS = listOf(
            BgmTrack(
                id = "bgm_cinematic",
                title = "Cinematic Horizon",
                artist = "Studio Orchestral",
                category = "Cinematic",
                durationMs = 24000L,
                bpm = 95,
                colorHex = 0xFF6C5CE7
            ),
            BgmTrack(
                id = "bgm_synthwave",
                title = "Cyber Neon Beat",
                artist = "Future Wave",
                category = "Electronic",
                durationMs = 20000L,
                bpm = 124,
                colorHex = 0xFF00CEC9
            ),
            BgmTrack(
                id = "bgm_lofi",
                title = "Lo-Fi Sunset Glow",
                artist = "Chill Hop Lab",
                category = "Lo-Fi",
                durationMs = 28000L,
                bpm = 82,
                colorHex = 0xFFFDCB6E
            ),
            BgmTrack(
                id = "bgm_acoustic",
                title = "Upbeat Acoustic Spark",
                artist = "Sunny Strings",
                category = "Acoustic",
                durationMs = 18000L,
                bpm = 110,
                colorHex = 0xFF00B894
            ),
            BgmTrack(
                id = "bgm_bass",
                title = "Deep Bass Groove",
                artist = "Club Pulse",
                category = "Urban",
                durationMs = 22000L,
                bpm = 128,
                colorHex = 0xFFFF7675
            ),
            BgmTrack(
                id = "bgm_zen",
                title = "Zen Piano Meditation",
                artist = "Aura Studio",
                category = "Ambient",
                durationMs = 30000L,
                bpm = 70,
                colorHex = 0xFFA29BFE
            )
        )
    }
}
