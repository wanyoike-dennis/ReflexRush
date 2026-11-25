package com.inventsolutions.reflexrush.game

import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerStatisticsTest {
    @Test
    fun completedSessionsAccumulateLifetimeTotalsAndAccuracyFromTargetOutcomes() {
        val first = GameSessionSummary(
            mode = GameMode.CLASSIC,
            finalScore = 120,
            levelReached = 3,
            livesRemaining = 1,
            goodTargetsHit = 8,
            dangerTargetsHit = 2,
            targetsMissed = 2,
            goodTargetsMissed = 2,
            durationMs = 45_000L,
            bestCombo = 5,
            powerupsCollected = 3
        )
        val second = first.copy(
            finalScore = 90,
            levelReached = 2,
            goodTargetsHit = 4,
            dangerTargetsHit = 1,
            targetsMissed = 1,
            durationMs = 30_000L,
            bestCombo = 3,
            powerupsCollected = 1
        )

        val stats = PlayerStatistics().include(first).include(second)
        val classic = stats.forMode(GameMode.CLASSIC)

        assertEquals(2, stats.totalGamesPlayed)
        assertEquals(75_000L, stats.totalPlayTimeMs)
        assertEquals(12, stats.totalGoodTargetsHit)
        assertEquals(3, stats.totalDangerTargetsHit)
        assertEquals(3, stats.totalTargetsMissed)
        assertEquals(4, stats.totalPowerupsCollected)
        assertEquals(66, stats.accuracyPercent)
        assertEquals(120, classic.highestScore)
        assertEquals(3, classic.highestLevel)
        assertEquals(5, classic.bestCombo)
    }

    @Test
    fun recordsRemainSeparatedByGameMode() {
        val summary = GameSessionSummary(
            mode = GameMode.TIME_ATTACK,
            finalScore = 250,
            levelReached = 4,
            livesRemaining = 3,
            goodTargetsHit = 10,
            dangerTargetsHit = 1,
            targetsMissed = 2,
            goodTargetsMissed = 1,
            durationMs = 60_000L,
            bestCombo = 7,
            powerupsCollected = 2
        )

        val stats = PlayerStatistics().include(summary)

        assertEquals(250, stats.forMode(GameMode.TIME_ATTACK).highestScore)
        assertEquals(0, stats.forMode(GameMode.CLASSIC).gamesPlayed)
        assertEquals(1, stats.totalGamesPlayed)
    }

    @Test
    fun emptyStatisticsHaveZeroAccuracyAndNoGames() {
        val stats = PlayerStatistics()

        assertEquals(0, stats.totalGamesPlayed)
        assertEquals(0, stats.accuracyPercent)
        assertEquals(0, stats.highestScore)
    }
}
