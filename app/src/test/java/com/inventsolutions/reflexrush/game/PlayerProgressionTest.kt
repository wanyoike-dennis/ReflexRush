package com.inventsolutions.reflexrush.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerProgressionTest {
    private fun summary(
        mode: GameMode = GameMode.CLASSIC,
        score: Int = 100,
        level: Int = 2,
        hits: Int = 5,
        dangerHits: Int = 1,
        combo: Int = 1,
        powerups: Int = 0
    ) = GameSessionSummary(
        mode = mode,
        finalScore = score,
        levelReached = level,
        livesRemaining = 1,
        goodTargetsHit = hits,
        dangerTargetsHit = dangerHits,
        targetsMissed = 0,
        goodTargetsMissed = 0,
        durationMs = 20_000L,
        bestCombo = combo,
        powerupsCollected = powerups
    )

    @Test
    fun dailyMissionProgressAccumulatesAcrossRoundsAndAwardsXpOnlyOnce() {
        val first = PlayerProgression().include(summary(hits = 12), "2026-11-25")
        val second = first.include(summary(hits = 8), "2026-11-25")
        val repeated = second.include(summary(hits = 4), "2026-11-25")

        assertTrue(second.completedDailyMissionIds.contains(DailyMission.FIRST_STRIKE.id))
        assertEquals(150, second.experiencePoints)
        assertEquals(150, repeated.experiencePoints)
        assertEquals(24, repeated.dailyGoodTargetsHit)
    }

    @Test
    fun dailyMissionsRefreshOnNewDateButLifetimeProgressAndXpRemain() {
        val completed = PlayerProgression().include(summary(hits = 20), "2026-11-25")
        val nextDay = completed.include(summary(hits = 2), "2026-11-26")

        assertEquals("2026-11-26", nextDay.dailyDateKey)
        assertEquals(2, nextDay.dailyGoodTargetsHit)
        assertFalse(nextDay.completedDailyMissionIds.contains(DailyMission.FIRST_STRIKE.id))
        assertEquals(completed.experiencePoints, nextDay.experiencePoints)
        assertEquals(22, nextDay.totalGoodTargetsHit)
    }

    @Test
    fun modeSpecificMissionsRequireTheCorrectModeAndResult() {
        val wrongMode = PlayerProgression().include(summary(score = 400), "day")
        assertFalse(wrongMode.completedDailyMissionIds.contains(DailyMission.SPEED_DEMON.id))

        val timeAttack = wrongMode.include(
            summary(mode = GameMode.TIME_ATTACK, score = 250),
            "day"
        )
        assertTrue(timeAttack.completedDailyMissionIds.contains(DailyMission.SPEED_DEMON.id))
    }

    @Test
    fun achievementsUnlockOnceAndAwardLifetimeXp() {
        val first = PlayerProgression().include(summary(hits = 100, combo = 10), "day")
        val second = first.include(summary(hits = 1, combo = 1), "day")

        assertTrue(first.unlockedAchievements.contains(PlayerAchievement.FIRST_ROUND.id))
        assertTrue(first.unlockedAchievements.contains(PlayerAchievement.CENTURY.id))
        assertTrue(first.unlockedAchievements.contains(PlayerAchievement.COMBO_TEN.id))
        assertEquals(first.experiencePoints, second.experiencePoints)
    }

    @Test
    fun levelAndCosmeticUnlocksFollowLifetimeXp() {
        val progression = PlayerProgression(experiencePoints = 1_000)

        assertEquals(5, progression.level)
        assertEquals(0, progression.xpIntoLevel)
        assertTrue(progression.unlockedCosmetics.contains("Neon Lime"))
        assertTrue(progression.unlockedCosmetics.contains("Electric Violet"))
        assertFalse(progression.unlockedCosmetics.contains("Solar Gold"))
    }

    @Test
    fun dangerDodgerRequiresNoDangerHitsInTheCompletedRound() {
        val failed = PlayerProgression().include(summary(dangerHits = 1), "day")
        val passed = failed.include(summary(dangerHits = 0), "day")

        assertTrue(passed.completedDailyMissionIds.contains(DailyMission.DANGER_DODGER.id))
    }
}
