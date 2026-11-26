package com.inventsolutions.reflexrush.game

import org.junit.Assert.assertEquals
import org.junit.Test

class GameSessionTrackerTest {

    @Test
    fun recordsElapsedTimeAndRoundActions() {
        val tracker = GameSessionTracker(GameMode.CLASSIC)

        tracker.recordElapsedTime(120L)
        tracker.recordElapsedTime(80L)
        tracker.recordGoodTargetHit(comboCount = 3)
        tracker.recordGoodTargetHit(comboCount = 5)
        tracker.recordDangerTargetHit()
        tracker.recordTargetMissed(TargetType.GOOD)
        tracker.recordTargetMissed(TargetType.DANGER)
        tracker.recordPowerupCollected()

        assertEquals(200L, tracker.elapsedTimeMs)
        assertEquals(2, tracker.goodTargetsHit)
        assertEquals(1, tracker.dangerTargetsHit)
        assertEquals(2, tracker.targetsMissed)
        assertEquals(1, tracker.goodTargetsMissed)
        assertEquals(5, tracker.bestCombo)
        assertEquals(1, tracker.powerupsCollected)
    }

    @Test
    fun createsSummaryUsingTrackedMetricsAndFinalGameState() {
        val tracker = GameSessionTracker(GameMode.TIME_ATTACK)
        tracker.recordElapsedTime(5_000L)
        tracker.recordGoodTargetHit(comboCount = 4)
        tracker.recordDangerTargetHit()
        tracker.recordTargetMissed(TargetType.GOOD)
        tracker.recordPowerupCollected()

        val summary = tracker.createSummary(
            GameState(
                status = GameStatus.GAME_OVER,
                score = 125,
                lives = 2,
                difficultyLevel = 3
            )
        )

        assertEquals(
            GameSessionSummary(
                mode = GameMode.TIME_ATTACK,
                finalScore = 125,
                levelReached = 3,
                livesRemaining = 2,
                goodTargetsHit = 1,
                dangerTargetsHit = 1,
                targetsMissed = 1,
                goodTargetsMissed = 1,
                durationMs = 5_000L,
                bestCombo = 4,
                powerupsCollected = 1
            ),
            summary
        )
    }

    @Test
    fun resetClearsAllMetricsForANewRound() {
        val tracker = GameSessionTracker(GameMode.SURVIVAL)
        tracker.recordElapsedTime(1_000L)
        tracker.recordGoodTargetHit(comboCount = 7)
        tracker.recordDangerTargetHit()
        tracker.recordTargetMissed(TargetType.GOOD)
        tracker.recordPowerupCollected()

        tracker.reset()

        assertEquals(0L, tracker.elapsedTimeMs)
        assertEquals(0, tracker.goodTargetsHit)
        assertEquals(0, tracker.dangerTargetsHit)
        assertEquals(0, tracker.targetsMissed)
        assertEquals(0, tracker.goodTargetsMissed)
        assertEquals(0, tracker.bestCombo)
        assertEquals(0, tracker.powerupsCollected)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNegativeElapsedTime() {
        GameSessionTracker(GameMode.CLASSIC).recordElapsedTime(-1L)
    }
}
