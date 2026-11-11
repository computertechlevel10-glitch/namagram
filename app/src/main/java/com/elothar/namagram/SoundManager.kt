package com.elothar.namagram

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log
import androidx.core.content.edit

class SoundManager(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("NamagramPrefs", Context.MODE_PRIVATE)

    private val soundPool: SoundPool
    private var soundClick = 0
    private var soundTick = 0
    private var soundVictory = 0
    private var isLoaded = false
    private val tag = "SoundManager"

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                Log.d(tag, "Successfully loaded sound sample ID: $sampleId")
            } else {
                Log.e(tag, "Failed to load sound sample ID: $sampleId, status: $status")
            }
        }

        try {
            soundClick = soundPool.load(appContext, R.raw.sound_click, 1)
            soundTick = soundPool.load(appContext, R.raw.sound_tick, 1)
            soundVictory = soundPool.load(appContext, R.raw.sound_victory, 1)
            isLoaded = true
        } catch (e: Exception) {
            Log.e(tag, "Exception loading raw sounds", e)
        }
    }

    private fun isSoundEnabled(): Boolean {
        return prefs.getBoolean("sound_enabled", true)
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit { putBoolean("sound_enabled", enabled) }
    }

    private fun playSound(soundId: Int, volume: Float = 1.0f) {
        if (!isSoundEnabled() || !isLoaded || soundId == 0) return
        soundPool.play(soundId, volume, volume, 1, 0, 1.0f)
    }

    // Uses MediaPlayer for guaranteed playback of MP3/WAV files without SoundPool limitations
    private fun playMediaResource(resourceId: Int) {
        if (!isSoundEnabled()) return
        try {
            MediaPlayer.create(appContext, resourceId)?.apply {
                setVolume(1.0f, 1.0f)
                setOnCompletionListener { mp ->
                    mp.release()
                }
                start()
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to play media resource", e)
        }
    }

    fun playClickSound() {
        playSound(soundClick, 1.0f)
    }

    fun playTickSound() {
        playSound(soundTick, 0.8f)
    }

    fun playTimeoutSound() {
        playMediaResource(R.raw.sound_timeout)
    }

    fun playVictoryChime() {
        playSound(soundVictory, 1.0f)
    }

    fun playTilesDrawSound() {
        playMediaResource(R.raw.sound_tiles)
    }

    fun stopCountdownSounds() {}

    fun release() {
        try {
            soundPool.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}