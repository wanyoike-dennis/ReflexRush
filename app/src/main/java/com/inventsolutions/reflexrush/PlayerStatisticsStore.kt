package com.inventsolutions.reflexrush

import android.content.Context
import com.inventsolutions.reflexrush.game.GameMode
import com.inventsolutions.reflexrush.game.GameSessionSummary
import com.inventsolutions.reflexrush.game.ModeStatistics
import com.inventsolutions.reflexrush.game.PlayerStatistics

/** Offline lifetime statistics, stored independently for each game mode. */
class PlayerStatisticsStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun load(): PlayerStatistics = PlayerStatistics(
        modes = GameMode.entries.associateWith { mode ->
            val prefix = mode.name.lowercase()
            ModeStatistics(
                gamesPlayed = preferences.getInt(key(prefix, "games"), 0),
                totalPlayTimeMs = preferences.getLong(key(prefix, "time"), 0L),
                goodTargetsHit = preferences.getInt(key(prefix, "good"), 0),
                dangerTargetsHit = preferences.getInt(key(prefix, "danger"), 0),
                targetsMissed = preferences.getInt(key(prefix, "missed"), 0),
                powerupsCollected = preferences.getInt(key(prefix, "powerups"), 0),
                highestScore = preferences.getInt(key(prefix, "score"), 0),
                highestLevel = preferences.getInt(key(prefix, "level"), 0),
                bestCombo = preferences.getInt(key(prefix, "combo"), 0)
            )
        }
    )

    fun record(summary: GameSessionSummary): PlayerStatistics {
        val current = load()
        val updated = current.include(summary)
        val mode = updated.forMode(summary.mode)
        val prefix = summary.mode.name.lowercase()
        preferences.edit()
            .putInt(key(prefix, "games"), mode.gamesPlayed)
            .putLong(key(prefix, "time"), mode.totalPlayTimeMs)
            .putInt(key(prefix, "good"), mode.goodTargetsHit)
            .putInt(key(prefix, "danger"), mode.dangerTargetsHit)
            .putInt(key(prefix, "missed"), mode.targetsMissed)
            .putInt(key(prefix, "powerups"), mode.powerupsCollected)
            .putInt(key(prefix, "score"), mode.highestScore)
            .putInt(key(prefix, "level"), mode.highestLevel)
            .putInt(key(prefix, "combo"), mode.bestCombo)
            .apply()
        return updated
    }

    private fun key(mode: String, field: String) = "${mode}_${field}"

    private companion object {
        const val PREFERENCES_NAME = "reflex_rush_player_statistics"
    }
}
