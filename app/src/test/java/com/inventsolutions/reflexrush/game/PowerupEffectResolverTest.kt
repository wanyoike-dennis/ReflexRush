package com.inventsolutions.reflexrush.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PowerupEffectResolverTest {

    private val config = GameConfig(powerupDurationMs = 5_000L)
    private val resolver = PowerupEffectResolver(config) { score ->
        DifficultyProgression.forScore(score, config)
    }

    @Test
    fun goldenStarAddsFiftyPointsAndUpdatesDifficulty() {
        val initial = GameState(score = 90, difficultyLevel = 1)

        val result = resolver.apply(initial, PowerupType.GOLDEN_STAR)

        assertEquals(140, result.state.score)
        assertEquals(2, result.state.difficultyLevel)
        assertEquals(
            listOf(
                GameplayEvent.PowerupCollected(PowerupType.GOLDEN_STAR),
                GameplayEvent.PowerupActivated(PowerupType.GOLDEN_STAR),
                GameplayEvent.LevelChanged(1, 2)
            ),
            result.events
        )
    }

    @Test
    fun shieldPowerupEnablesShieldWithoutChangingScoreOrTimedEffect() {
        val initial = GameState(
            score = 35,
            shieldAvailable = false,
            activeTimedPowerup = PowerupType.SLOW_TIME,
            powerupRemainingMs = 2_000L
        )

        val result = resolver.apply(initial, PowerupType.SHIELD)

        assertTrue(result.state.shieldAvailable)
        assertEquals(35, result.state.score)
        assertEquals(PowerupType.SLOW_TIME, result.state.activeTimedPowerup)
        assertEquals(2_000L, result.state.powerupRemainingMs)
        assertEquals(
            listOf(
                GameplayEvent.PowerupCollected(PowerupType.SHIELD),
                GameplayEvent.PowerupActivated(PowerupType.SHIELD)
            ),
            result.events
        )
    }

    @Test
    fun timedPowerupSetsConfiguredDurationAndReplacesPreviousTimedEffect() {
        val initial = GameState(
            activeTimedPowerup = PowerupType.SLOW_TIME,
            powerupRemainingMs = 1_000L
        )

        val result = resolver.apply(initial, PowerupType.DOUBLE_POINTS)

        assertEquals(PowerupType.DOUBLE_POINTS, result.state.activeTimedPowerup)
        assertEquals(5_000L, result.state.powerupRemainingMs)
        assertEquals(
            listOf(
                GameplayEvent.PowerupCollected(PowerupType.DOUBLE_POINTS),
                GameplayEvent.PowerupActivated(PowerupType.DOUBLE_POINTS)
            ),
            result.events
        )
    }

    @Test
    fun goldenStarDoesNotChangeDifficultyWhenThresholdIsNotCrossed() {
        val initial = GameState(score = 0, difficultyLevel = 1)

        val result = resolver.apply(initial, PowerupType.GOLDEN_STAR)

        assertEquals(50, result.state.score)
        assertEquals(1, result.state.difficultyLevel)
        assertFalse(result.events.any { it is GameplayEvent.LevelChanged })
    }

    @Test
    fun resolverDoesNotMutateOriginalState() {
        val initial = GameState(
            score = 10,
            shieldAvailable = false,
            activeTimedPowerup = null,
            powerupRemainingMs = 0L
        )

        val result = resolver.apply(initial, PowerupType.SHIELD)

        assertFalse(initial.shieldAvailable)
        assertNull(initial.activeTimedPowerup)
        assertTrue(result.state.shieldAvailable)
    }
}
