package com.inventsolutions.reflexrush.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModeDifficultyResolverTest {

    @Test
    fun classicAndOtherNonSurvivalModesKeepSharedDifficultyProfile() {
        val config = GameConfig()
        val expected = DifficultyProgression.forScore(250, config)

        listOf(GameMode.CLASSIC, GameMode.TIME_ATTACK, GameMode.PRECISION).forEach { mode ->
            assertEquals(expected, ModeDifficultyResolver(mode, config).forScore(250))
        }
    }

    @Test
    fun survivalAppliesItsExistingTimingAndDangerAdjustments() {
        val config = GameConfig()
        val base = DifficultyProgression.forScore(0, config)
        val actual = ModeDifficultyResolver(GameMode.SURVIVAL, config).forScore(0)

        assertEquals((base.spawnIntervalMs * 0.82f).toLong(), actual.spawnIntervalMs)
        assertEquals((base.targetLifetimeMs * 0.9f).toLong(), actual.targetLifetimeMs)
        assertEquals(base.dangerTargetChance + 0.04f, actual.dangerTargetChance, 0.0001f)
        assertEquals(base.level, actual.level)
    }

    @Test
    fun survivalDangerChanceRemainsCappedAtFiftyPercent() {
        val config = GameConfig(dangerTargetChance = 0.45f)
        val actual = ModeDifficultyResolver(GameMode.SURVIVAL, config).forScore(10_000)

        assertTrue(actual.dangerTargetChance <= 0.5f)
        assertEquals(0.5f, actual.dangerTargetChance, 0.0001f)
    }

    @Test
    fun survivalRetainsTheSharedScoreBasedLevel() {
        val resolver = ModeDifficultyResolver(GameMode.SURVIVAL, GameConfig())

        assertEquals(1, resolver.forScore(0).level)
        assertEquals(2, resolver.forScore(100).level)
        assertEquals(5, resolver.forScore(700).level)
    }
}
