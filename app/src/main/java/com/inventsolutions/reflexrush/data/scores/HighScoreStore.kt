package com.inventsolutions.reflexrush.data.scores

import android.content.Context
import com.inventsolutions.reflexrush.game.BestScore
import com.inventsolutions.reflexrush.game.GameMode

/**
 * Stores the player's personal best on this device.
 *
 * SharedPreferences keeps this small piece of data local and available offline.
 */
class HighScoreStore(context: Context) {
    private val preferences = context.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    fun load(mode: GameMode = GameMode.CLASSIC): Int =
        preferences.getInt(keyFor(mode), 0).coerceAtLeast(0)

    fun saveIfHigher(roundScore: Int, mode: GameMode = GameMode.CLASSIC): Int {
        val key = keyFor(mode)
        val currentBest = load(mode)
        val updatedBest = BestScore.afterRound(currentBest, roundScore.coerceAtLeast(0))

        if (updatedBest > currentBest) {
            preferences.edit()
                .putInt(key, updatedBest)
                .apply()
        }

        return updatedBest
    }

    private fun keyFor(mode: GameMode): String =
        if (mode == GameMode.CLASSIC) KEY_BEST_SCORE else "${KEY_BEST_SCORE}_${mode.name.lowercase()}"

    private companion object {
        const val PREFERENCES_NAME = "reflex_rush_preferences"
        const val KEY_BEST_SCORE = "best_score"
    }
}
