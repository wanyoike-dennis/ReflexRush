package com.inventsolutions.reflexrush

import android.content.Context
import com.inventsolutions.reflexrush.game.BestScore

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

    fun load(): Int = preferences.getInt(KEY_BEST_SCORE, 0).coerceAtLeast(0)

    fun saveIfHigher(roundScore: Int): Int {
        val currentBest = load()
        val updatedBest = BestScore.afterRound(currentBest, roundScore.coerceAtLeast(0))

        if (updatedBest > currentBest) {
            preferences.edit()
                .putInt(KEY_BEST_SCORE, updatedBest)
                .apply()
        }

        return updatedBest
    }

    private companion object {
        const val PREFERENCES_NAME = "reflex_rush_preferences"
        const val KEY_BEST_SCORE = "best_score"
    }
}
