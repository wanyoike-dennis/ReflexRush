package com.inventsolutions.reflexrush.data.progression

import android.content.Context
import com.inventsolutions.reflexrush.game.GameMode
import com.inventsolutions.reflexrush.game.GameSessionSummary
import com.inventsolutions.reflexrush.game.PlayerProgression
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Offline persistence for daily missions, lifetime achievements, and XP. */
class PlayerProgressionStore(
    context: Context,
    private val todayKeyProvider: () -> String = {
        SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())
    }
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun load(): PlayerProgression {
        val today = todayKeyProvider()
        val savedDate = preferences.getString(KEY_DAILY_DATE, "").orEmpty()
        val sameDay = savedDate == today
        val progression = PlayerProgression(
            experiencePoints = preferences.getInt(KEY_XP, 0),
            totalGamesPlayed = preferences.getInt(KEY_GAMES, 0),
            totalGoodTargetsHit = preferences.getInt(KEY_GOOD_HITS, 0),
            totalPowerupsCollected = preferences.getInt(KEY_POWERUPS, 0),
            bestCombo = preferences.getInt(KEY_BEST_COMBO, 0),
            highestScore = preferences.getInt(KEY_HIGHEST_SCORE, 0),
            playedModes = preferences.getStringSet(KEY_PLAYED_MODES, mutableSetOf()).orEmpty()
                .mapNotNull { value -> GameMode.entries.firstOrNull { it.name == value } }.toSet(),
            unlockedAchievements = preferences.getStringSet(KEY_ACHIEVEMENTS, mutableSetOf()).orEmpty().toSet(),
            dailyDateKey = today,
            dailyGoodTargetsHit = if (sameDay) preferences.getInt(KEY_DAILY_GOOD, 0) else 0,
            dailyPowerupsCollected = if (sameDay) preferences.getInt(KEY_DAILY_POWERUPS, 0) else 0,
            dailyBestCombo = if (sameDay) preferences.getInt(KEY_DAILY_BEST_COMBO, 0) else 0,
            completedDailyMissionIds = if (sameDay) {
                preferences.getStringSet(KEY_DAILY_COMPLETED, mutableSetOf()).orEmpty().toSet()
            } else emptySet()
        )
        if (!sameDay) save(progression)
        return progression
    }

    fun record(summary: GameSessionSummary): PlayerProgression {
        val updated = load().include(summary, todayKeyProvider())
        save(updated)
        return updated
    }

    private fun save(value: PlayerProgression) {
        preferences.edit()
            .putInt(KEY_XP, value.experiencePoints)
            .putInt(KEY_GAMES, value.totalGamesPlayed)
            .putInt(KEY_GOOD_HITS, value.totalGoodTargetsHit)
            .putInt(KEY_POWERUPS, value.totalPowerupsCollected)
            .putInt(KEY_BEST_COMBO, value.bestCombo)
            .putInt(KEY_HIGHEST_SCORE, value.highestScore)
            .putStringSet(KEY_PLAYED_MODES, value.playedModes.map { it.name }.toSet())
            .putStringSet(KEY_ACHIEVEMENTS, value.unlockedAchievements)
            .putString(KEY_DAILY_DATE, value.dailyDateKey)
            .putInt(KEY_DAILY_GOOD, value.dailyGoodTargetsHit)
            .putInt(KEY_DAILY_POWERUPS, value.dailyPowerupsCollected)
            .putInt(KEY_DAILY_BEST_COMBO, value.dailyBestCombo)
            .putStringSet(KEY_DAILY_COMPLETED, value.completedDailyMissionIds)
            .apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "reflex_rush_player_progression"
        const val KEY_XP = "experience_points"
        const val KEY_GAMES = "total_games"
        const val KEY_GOOD_HITS = "total_good_hits"
        const val KEY_POWERUPS = "total_powerups"
        const val KEY_BEST_COMBO = "best_combo"
        const val KEY_HIGHEST_SCORE = "highest_score"
        const val KEY_PLAYED_MODES = "played_modes"
        const val KEY_ACHIEVEMENTS = "unlocked_achievements"
        const val KEY_DAILY_DATE = "daily_date"
        const val KEY_DAILY_GOOD = "daily_good_hits"
        const val KEY_DAILY_POWERUPS = "daily_powerups"
        const val KEY_DAILY_BEST_COMBO = "daily_best_combo"
        const val KEY_DAILY_COMPLETED = "daily_completed_missions"
    }
}
