package com.inventsolutions.reflexrush

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.inventsolutions.reflexrush.game.GameplayEvent
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.sin

/**
 * Small, offline audio layer for Reflex Rush.
 *
 * Short PCM WAV samples are synthesised into the app cache and played with
 * SoundPool. This avoids bundled media files and additional dependencies.
 * Audio settings are stored independently in SharedPreferences.
 */
class GameAudioManager(context: Context) {
    enum class Cue {
        GOOD_HIT,
        DANGER_HIT,
        COMBO,
        POWERUP,
        LIFE_LOST,
        SHIELD,
        GAME_OVER,
        PERSONAL_BEST
    }

    private val appContext = context.applicationContext
    private val settingsStore = AudioSettingsStore(appContext)

    var soundEffectsEnabled: Boolean
        private set
    var musicEnabled: Boolean
        private set

    private var gameActive = false
    private var released = false
    private var musicSampleId = 0
    private var musicStreamId = 0
    private val loadedSamples = mutableMapOf<Cue, Int>()
    private val readySamples = ConcurrentHashMap.newKeySet<Int>()

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(8)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    init {
        soundEffectsEnabled = settingsStore.soundEffectsEnabled
        musicEnabled = settingsStore.musicEnabled

        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                readySamples += sampleId
                if (sampleId == musicSampleId) startMusicIfAllowed()
            }
        }

        Cue.entries.forEach { cue ->
            val file = writeToneSample(cue)
            loadedSamples[cue] = soundPool.load(file.absolutePath, 1)
        }
        val musicFile = writeAmbientSample()
        musicSampleId = soundPool.load(musicFile.absolutePath, 1)
    }

    fun setSoundEffectsEnabled(enabled: Boolean) {
        soundEffectsEnabled = enabled
        settingsStore.setSoundEffectsEnabled(enabled)
    }

    fun setMusicEnabled(enabled: Boolean) {
        musicEnabled = enabled
        settingsStore.setMusicEnabled(enabled)
        if (enabled) startMusicIfAllowed() else stopMusic()
    }

    fun setGameActive(active: Boolean) {
        gameActive = active
        if (active) startMusicIfAllowed() else stopMusic()
    }

    fun play(cue: Cue) {
        if (released || !soundEffectsEnabled) return
        val sampleId = loadedSamples[cue] ?: return
        if (sampleId !in readySamples) return
        soundPool.play(sampleId, 0.9f, 0.9f, 2, 0, 1f)
    }

    fun playEvents(events: List<GameplayEvent>) {
        events.forEach { event ->
            when (event) {
                is GameplayEvent.GoodTargetHit -> play(Cue.GOOD_HIT)
                is GameplayEvent.DangerTargetHit -> play(Cue.DANGER_HIT)
                is GameplayEvent.ComboMilestone -> play(Cue.COMBO)
                is GameplayEvent.PowerupCollected -> play(Cue.POWERUP)
                is GameplayEvent.LifeLost, is GameplayEvent.PrecisionMissPenalty -> play(Cue.LIFE_LOST)
                GameplayEvent.ShieldConsumed -> play(Cue.SHIELD)
                is GameplayEvent.RoundCompleted -> play(Cue.GAME_OVER)
                else -> Unit
            }
        }
    }

    fun release() {
        if (released) return
        released = true
        stopMusic()
        soundPool.release()
    }

    private fun startMusicIfAllowed() {
        if (released || !gameActive || !musicEnabled || musicSampleId !in readySamples || musicStreamId != 0) {
            return
        }
        musicStreamId = soundPool.play(musicSampleId, 0.18f, 0.18f, 0, -1, 1f)
    }

    private fun stopMusic() {
        if (musicStreamId != 0) {
            soundPool.stop(musicStreamId)
            musicStreamId = 0
        }
    }

    private fun writeToneSample(cue: Cue): File {
        val notes = when (cue) {
            Cue.GOOD_HIT -> listOf(740.0 to 75, 990.0 to 85)
            Cue.DANGER_HIT -> listOf(190.0 to 150)
            Cue.COMBO -> listOf(660.0 to 65, 880.0 to 65, 1_100.0 to 110)
            Cue.POWERUP -> listOf(523.0 to 70, 659.0 to 70, 784.0 to 140)
            Cue.LIFE_LOST -> listOf(330.0 to 100, 220.0 to 180)
            Cue.SHIELD -> listOf(440.0 to 70, 880.0 to 150)
            Cue.GAME_OVER -> listOf(392.0 to 130, 330.0 to 130, 262.0 to 260)
            Cue.PERSONAL_BEST -> listOf(523.0 to 75, 659.0 to 75, 784.0 to 75, 1_046.0 to 220)
        }
        val pcm = ByteArrayOutputStream()
        notes.forEach { (frequency, durationMs) ->
            val count = SAMPLE_RATE * durationMs / 1_000
            repeat(count) { index ->
                val attack = (index / (SAMPLE_RATE * 0.008)).coerceIn(0.0, 1.0)
                val release = ((count - index) / (SAMPLE_RATE * 0.035)).coerceIn(0.0, 1.0)
                val envelope = minOf(attack, release)
                val sample = (sin(2.0 * PI * frequency * index / SAMPLE_RATE) * envelope * 0.48 * Short.MAX_VALUE)
                    .toInt().toShort()
                writeShortLe(pcm, sample)
            }
        }
        return writeWav("reflex_${cue.name.lowercase()}.wav", pcm.toByteArray())
    }

    private fun writeAmbientSample(): File {
        val durationSeconds = 3
        val count = SAMPLE_RATE * durationSeconds
        val pcm = ByteArrayOutputStream(count * 2)
        repeat(count) { index ->
            val time = index.toDouble() / SAMPLE_RATE
            val wave = sin(2.0 * PI * 110.0 * time) * 0.45 +
                sin(2.0 * PI * 165.0 * time) * 0.28 +
                sin(2.0 * PI * 220.0 * time) * 0.18
            val sample = (wave * 0.12 * Short.MAX_VALUE).toInt().toShort()
            writeShortLe(pcm, sample)
        }
        return writeWav("reflex_ambient.wav", pcm.toByteArray())
    }

    private fun writeWav(name: String, pcm: ByteArray): File {
        val header = ByteArrayOutputStream(44)
        header.write("RIFF".toByteArray(Charsets.US_ASCII))
        writeIntLe(header, 36 + pcm.size)
        header.write("WAVE".toByteArray(Charsets.US_ASCII))
        header.write("fmt ".toByteArray(Charsets.US_ASCII))
        writeIntLe(header, 16)
        writeShortLe(header, 1)
        writeShortLe(header, 1)
        writeIntLe(header, SAMPLE_RATE)
        writeIntLe(header, SAMPLE_RATE * 2)
        writeShortLe(header, 2)
        writeShortLe(header, 16)
        header.write("data".toByteArray(Charsets.US_ASCII))
        writeIntLe(header, pcm.size)

        val file = File(appContext.cacheDir, name)
        file.writeBytes(header.toByteArray() + pcm)
        return file
    }

    private fun writeShortLe(output: ByteArrayOutputStream, value: Int) {
        output.write(value and 0xFF)
        output.write((value ushr 8) and 0xFF)
    }

    private fun writeShortLe(output: ByteArrayOutputStream, value: Short) =
        writeShortLe(output, value.toInt())

    private fun writeIntLe(output: ByteArrayOutputStream, value: Int) {
        output.write(value and 0xFF)
        output.write((value ushr 8) and 0xFF)
        output.write((value ushr 16) and 0xFF)
        output.write((value ushr 24) and 0xFF)
    }

    private companion object {
        const val SAMPLE_RATE = 22_050
    }
}
