package com.inventsolutions.reflexrush.core.audio

import android.content.Context

/**
 * Lightweight persistent audio preferences shared by the settings screen and gameplay.
 * Values are stored locally and remain available between app launches.
 */
class AudioSettingsStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    val soundEffectsEnabled: Boolean
        get() = preferences.getBoolean(KEY_SOUND_EFFECTS, true)

    val musicEnabled: Boolean
        get() = preferences.getBoolean(KEY_MUSIC, false)

    fun setSoundEffectsEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_SOUND_EFFECTS, enabled).apply()
    }

    fun setMusicEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_MUSIC, enabled).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "reflex_rush_audio"
        const val KEY_SOUND_EFFECTS = "sound_effects_enabled"
        const val KEY_MUSIC = "music_enabled"
    }
}
