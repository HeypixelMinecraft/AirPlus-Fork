/*
 * Air Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 */
package net.airplus.features.module.modules.music

import net.airplus.config.ListValue
import net.airplus.event.GameTickEvent
import net.airplus.event.handler
import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.airplus.features.module.modules.music.core.LocalMusicSource
import net.airplus.features.module.modules.music.core.MusicSource
import net.airplus.features.module.modules.music.core.ParsedLyrics
import net.airplus.features.module.modules.music.core.PlaybackEngine
import net.airplus.features.module.modules.music.core.Track
import net.airplus.features.module.modules.music.core.TrackSource
import net.airplus.utils.client.chat
import net.airplus.utils.client.ClientUtils
import net.airplus.utils.kotlin.SharedScopes
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream

/**
 * Music player orchestration layer (offline only, local files).
 *
 * This object no longer plays audio directly: it delegates stream opening and
 * lyric loading to a [MusicSource] (local files) and audio playback to a
 * [PlaybackEngine]. All public read-only properties used by the HUD keep
 * their original signatures so no UI code needs to change.
 */
object MusicPlayer : Module("MusicPlayer", Category.CLIENT) {

    private var volumeValue by int("音量", 50, 0..100)
    private val autoPlay by boolean("自动播放", false)
    private var loopMode by choices("循环模式", arrayOf("关闭", "单曲循环", "列表循环"), "列表循环")
    private val showInfo by boolean("显示信息", true)

    private var selectedMusicName = "无"

    private val engine = PlaybackEngine()

    /** Active playback queue (local tracks). */
    private val queue = mutableListOf<Track>()
    private var currentIndex = 0
    private var currentTrack: Track? = null

    /** Cached local scan, used for the dropdown and HUD list. */
    private var localTracks: List<Track> = emptyList()

    private var currentLyric: String = ""
    private var lyricLines = listOf<String>()
    private var currentLyricIndex = 0
    private var playStartTime: Long = 0
    private var lyricTimestamps = mutableListOf<Long>()
    private var musicDuration: Long = 0

    /** Paused playback position in ms, resumed via [resumeMusic]. */
    @Volatile
    var isPaused: Boolean = false
        private set

    private var pausedElapsed: Long = 0

    val currentMusicName: String
        get() = currentTrack?.displayName ?: "无"

    val currentLyricDisplay: String
        get() = currentLyric

    val previousLyricDisplay: String
        get() = if (currentLyricIndex > 0 && lyricLines.isNotEmpty()) lyricLines[currentLyricIndex - 1] else ""

    val nextLyricDisplay: String
        get() = if (currentLyricIndex < lyricLines.size - 1 && lyricLines.isNotEmpty()) lyricLines[currentLyricIndex + 1] else ""

    val isCurrentlyPlaying: Boolean
        get() = engine.isPlaying

    val musicListNames: List<String>
        get() = localTracks.map { it.displayName }

    val localTrackList: List<Track>
        get() = localTracks

    val playingTrack: Track?
        get() = currentTrack

    private lateinit var musicChoicesValue: ListValue

    private fun initMusicChoices() {
        // Note: onChanged() is declared on Value<T>, so chaining it directly on the
        // assignment would lose the static ListValue type. Assign first, then register.
        musicChoicesValue = choices("音乐", arrayOf("无"), "无")
        musicChoicesValue.onChanged {
            if (it != "无" && it != selectedMusicName) {
                selectedMusicName = it
                val index = musicListNames.indexOf(it)
                if (index >= 0) {
                    playLocalIndex(index)
                }
            }
        }
    }

    init {
        initMusicChoices()
    }

    val progress: Float
        get() {
            if (musicDuration <= 0) return 0F
            val elapsed = when {
                isPaused -> pausedElapsed
                engine.isPlaying -> System.currentTimeMillis() - playStartTime
                else -> return 0F
            }
            return (elapsed.toFloat() / musicDuration.toFloat()).coerceIn(0F, 1F)
        }

    val currentTimeString: String
        get() {
            if (isPaused) return formatSeconds(pausedElapsed / 1000)
            if (!engine.isPlaying) return "0:00"
            val elapsed = (System.currentTimeMillis() - playStartTime) / 1000
            return formatSeconds(elapsed)
        }

    val totalTimeString: String
        get() {
            if (musicDuration <= 0) return "0:00"
            val totalSeconds = musicDuration / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "$minutes:${seconds.toString().padStart(2, '0')}"
        }

    val timeDisplayString: String
        get() = "$currentTimeString / $totalTimeString"

    private fun formatSeconds(totalSeconds: Long): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "$minutes:${seconds.toString().padStart(2, '0')}"
    }

    override fun onEnable() {
        super.onEnable()
        scanMusicFiles()

        if (localTracks.isEmpty()) {
            chat("§c[音乐播放器] 未找到本地音乐文件！请将音乐文件放入: ${LocalMusicSource.musicDir.absolutePath}")
        } else if (showInfo) {
            chat("§a[音乐播放器] 已加载 ${localTracks.size} 首本地音乐，可在HUD编辑界面中加入歌词显示")
            chat("§7[音乐播放器] 音乐目录: ${LocalMusicSource.musicDir.absolutePath}")
        }

        // Default to the local queue so list-loop / next / prev work as before.
        queue.clear()
        queue.addAll(localTracks)
        currentIndex = 0

        if (selectedMusicName != "无" && musicListNames.contains(selectedMusicName)) {
            val index = musicListNames.indexOf(selectedMusicName)
            if (index >= 0) {
                playLocalIndex(index)
                return
            }
        }

        if (autoPlay && localTracks.isNotEmpty()) {
            playLocalIndex(0)
        }
    }

    override fun onDisable() {
        super.onDisable()
        stopMusic()
        if (showInfo) {
            chat("§c[音乐播放器] 已停止播放")
        }
    }

    val onTick = handler<GameTickEvent> {
        if (!engine.isPlaying && !isPaused && autoPlay && loopMode == "列表循环" && queue.isNotEmpty()) {
            playNext()
        }

        if (engine.isPlaying && lyricTimestamps.isNotEmpty()) {
            val elapsed = System.currentTimeMillis() - playStartTime
            var newIndex = 0
            for (i in lyricTimestamps.indices) {
                if (elapsed >= lyricTimestamps[i]) {
                    newIndex = i
                } else {
                    break
                }
            }
            if (newIndex != currentLyricIndex && newIndex < lyricLines.size) {
                currentLyricIndex = newIndex
                currentLyric = lyricLines[currentLyricIndex]
            }
        }

        updateVolume()
    }

    fun scanMusicFiles() {
        localTracks = LocalMusicSource.scan()
        updateMusicChoices()
    }

    private fun updateMusicChoices() {
        val names = mutableListOf("无")
        names.addAll(musicListNames)

        musicChoicesValue.updateValues(names.toTypedArray())
        if (selectedMusicName !in names) {
            selectedMusicName = "无"
        }
    }

    private fun sourceFor(track: Track): MusicSource = when (track.source) {
        TrackSource.LOCAL -> LocalMusicSource
    }

    /**
     * Play [track] immediately. Adds it to the queue if not already present.
     * The actual stream opening / lyric loading runs on a background IO
     * coroutine, so this is safe to call from any thread.
     */
    fun playTrack(track: Track) {
        val idx = queue.indexOf(track)
        if (idx >= 0) {
            currentIndex = idx
        } else {
            queue.add(track)
            currentIndex = queue.size - 1
        }
        dispatchPlayback(track)
    }

    /**
     * Start playback of [track] on the shared IO pool.
     *
     * Crucially this must run off both the render thread (file I/O would
     * freeze the game) and off the previous [PlaybackEngine] thread: the first
     * thing [startPlayback] does is [PlaybackEngine.stop], which interrupts the
     * previous playback thread.
     */
    private fun dispatchPlayback(track: Track, seekMs: Long = 0L) {
        SharedScopes.IO.launch { startPlayback(track, seekMs) }
    }

    /**
     * Append [track] to the queue. Returns its 1-based position.
     */
    fun enqueue(track: Track): Int {
        queue.add(track)
        return queue.size
    }

    val queueTracks: List<Track>
        get() = queue.toList()

    private fun startPlayback(track: Track, seekMs: Long = 0L) {
        engine.stop()
        isPaused = false
        pausedElapsed = 0

        val source = sourceFor(track)
        currentTrack = track
        selectedMusicName = track.displayName

        val lyrics = try {
            source.loadLyrics(track)
        } catch (e: Exception) {
            ParsedLyrics.EMPTY
        }
        applyLyrics(lyrics)

        musicDuration = when {
            track.durationMs > 0 -> track.durationMs
            lyrics.durationHintMs > 0 -> lyrics.durationHintMs
            track.localFile != null -> LocalMusicSource.getDuration(track.localFile)
            else -> 0L
        }

        val stream = try {
            source.openStream(track)
        } catch (e: Exception) {
            chat("§c[音乐播放器] 播放失败: ${e.message}")
            currentLyric = ""
            return
        }

        // Resume support: skip approximately [seekMs] of audio data by byte
        // ratio. Exact for CBR streams, a close estimate for VBR ones.
        playStartTime = System.currentTimeMillis() - seekMs.coerceAtLeast(0L)
        if (seekMs > 0L && musicDuration > 0L && track.localFile != null) {
            skipStream(stream, track.localFile!!, seekMs)
        }

        if (showInfo) {
            chat("§a[音乐播放器] 正在播放: §f${track.displayName}")
        }

        engine.play(
            stream = stream,
            onComplete = { onMusicComplete() },
            onError = { msg -> chat("§c[音乐播放器] 播放失败: $msg") }
        )

        engine.setVolume(volumeValue / 100F)
    }

    private fun skipStream(stream: InputStream, file: File, seekMs: Long) {
        try {
            val target = (file.length().toDouble() * (seekMs.toDouble() / musicDuration)).toLong()
            var skipped = 0L
            while (skipped < target) {
                val step = stream.skip(target - skipped)
                if (step <= 0L) break
                skipped += step
            }
        } catch (e: Exception) {
            playStartTime = System.currentTimeMillis()
            ClientUtils.LOGGER.warn("[MusicPlayer] 恢复播放进度失败，从头开始: ${e.message}")
        }
    }

    private fun applyLyrics(lyrics: ParsedLyrics) {
        lyricLines = lyrics.lines
        lyricTimestamps = lyrics.timestamps.toMutableList()
        currentLyricIndex = 0
        currentLyric = if (lyricLines.isNotEmpty()) lyricLines[0] else ""
    }

    private fun onMusicComplete() {
        when (loopMode) {
            "单曲循环" -> currentTrack?.let { dispatchPlayback(it) }
            "列表循环" -> playNext()
            "关闭" -> { /* stop, nothing else */ }
        }
    }

    fun stopMusic() {
        engine.stop()
        currentLyric = ""
        lyricLines = emptyList()
        lyricTimestamps.clear()
        playStartTime = 0
        isPaused = false
        pausedElapsed = 0
    }

    /** Pause playback, remembering the position so [resumeMusic] can seek back. */
    fun pauseMusic() {
        if (!engine.isPlaying || isPaused) return
        pausedElapsed = (System.currentTimeMillis() - playStartTime).coerceIn(0L, musicDuration)
        engine.stop()
        isPaused = true
    }

    /** Resume paused playback, restoring the remembered position. */
    fun resumeMusic() {
        if (!isPaused) return
        val track = currentTrack ?: run {
            isPaused = false
            return
        }
        val seek = pausedElapsed
        isPaused = false
        pausedElapsed = 0
        dispatchPlayback(track, seek)
    }

    /**
     * Toggle play / pause for the GUI main button. When nothing has been
     * selected yet, starts the first local track.
     */
    fun togglePlayPause() {
        when {
            isPaused -> resumeMusic()
            engine.isPlaying -> pauseMusic()
            localTracks.isNotEmpty() -> {
                val index = musicListNames.indexOf(selectedMusicName).takeIf { it >= 0 } ?: 0
                playLocalIndex(index)
            }
        }
    }

    /**
     * Called by the GUI when the user picks a song: updates the module's
     * music choice (persisted, triggers [playLocalIndex] through onChanged).
     */
    fun selectMusicFromGui(name: String) {
        if (name == "无") return
        musicChoicesValue.set(name)
    }

    fun currentLoopMode(): String = loopMode

    fun changeLoopMode(mode: String) {
        if (mode in listOf("关闭", "单曲循环", "列表循环")) {
            loopMode = mode
        }
    }

    /** Opens the local music folder in the OS file explorer. */
    fun openMusicFolder() {
        try {
            java.awt.Desktop.getDesktop().open(LocalMusicSource.musicDir)
        } catch (e: Exception) {
            chat("§c[音乐播放器] 无法打开音乐文件夹: ${e.message}")
        }
    }

    fun playNext() {
        if (queue.isEmpty()) return
        currentIndex = (currentIndex + 1) % queue.size
        dispatchPlayback(queue[currentIndex])
    }

    fun playPrevious() {
        if (queue.isEmpty()) return
        currentIndex = (currentIndex - 1 + queue.size) % queue.size
        dispatchPlayback(queue[currentIndex])
    }

    /**
     * Play a local track by its index in the (dropdown) local list. Resets the
     * active queue to the local list so list-loop works across local files.
     */
    fun playLocalIndex(index: Int) {
        if (index !in localTracks.indices) return
        queue.clear()
        queue.addAll(localTracks)
        currentIndex = index
        dispatchPlayback(queue[index])
    }

    /**
     * (Re)scan and return the local track list.
     */
    fun refreshLocalTracks(): List<Track> {
        scanMusicFiles()
        return localTracks
    }

    private fun updateVolume() {
        engine.setVolume(volumeValue / 100F)
    }

    fun setVolume(vol: Int) {
        volumeValue = vol.coerceIn(0, 100)
        updateVolume()
    }

    fun getVolume(): Int = volumeValue
}
