/*
 * Air Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 */
package net.airplus.features.module.modules.music.core

import java.io.File

/**
 * Where a [Track] comes from. Used by [MusicPlayer] to pick the right
 * [MusicSource] when opening a stream or loading lyrics.
 */
enum class TrackSource {
    LOCAL
}

/**
 * Unified track model shared by every music source.
 *
 * Keeping a single model means the orchestrator ([net.airplus.features.module.modules.music.MusicPlayer])
 * and UI never need to know where a song came from.
 */
data class Track(
    val title: String,
    val artist: String,
    val durationMs: Long,
    val source: TrackSource,
    val localFile: File? = null
) {
    /**
     * Human readable name used by HUD. Local files keep their bare file name.
     */
    val displayName: String
        get() = when {
            source == TrackSource.LOCAL && localFile != null -> localFile.nameWithoutExtension
            artist.isNotBlank() -> "$artist - $title"
            else -> title
        }

    companion object {
        fun fromLocalFile(file: File): Track =
            Track(
                title = file.nameWithoutExtension,
                artist = "",
                durationMs = 0L,
                source = TrackSource.LOCAL,
                localFile = file
            )
    }
}
