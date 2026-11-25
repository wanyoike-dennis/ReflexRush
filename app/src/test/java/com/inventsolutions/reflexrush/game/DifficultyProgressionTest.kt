package com.inventsolutions.reflexrush.game

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DifficultyProgressionTest {

    @Test
    fun scoreThresholdsSelectTheExpectedDifficultyLevel() {
        val config = GameConfig()

        assertEquals(1, DifficultyProgression.forScore(0, config).level)
        assertEquals(1, DifficultyProgression.forScore(90, config).level)
        assertEquals(2, DifficultyProgression.forScore(100, config).level)
        assertEquals(3, DifficultyProgression.forScore(250, config).level)
        assertEquals(4, DifficultyProgression.forScore(450, config).level)
        assertEquals(5, DifficultyProgression.forScore(700, config).level)
    }

    @Test
    fun higherLevelsSpawnFasterAndUseShorterTargetLifetimes() {
        val config = GameConfig()
        val first = DifficultyProgression.forScore(0, config)
        val second = DifficultyProgression.forScore(100, config)
        val fifth = DifficultyProgression.forScore(700, config)

        assertTrue(second.spawnIntervalMs < first.spawnIntervalMs)
        assertTrue(fifth.spawnIntervalMs < second.spawnIntervalMs)
        assertTrue(second.targetLifetimeMs < first.targetLifetimeMs)
        assertTrue(fifth.targetLifetimeMs < second.targetLifetimeMs)
    }

    @Test
    fun dangerTargetChanceIncreasesGraduallyAndStaysCapped() {
        val config = GameConfig(dangerTargetChance = 0.22f)

        val first = DifficultyProgression.forScore(0, config)
        val third = DifficultyProgression.forScore(250, config)
        val fifth = DifficultyProgression.forScore(700, config)

        assertTrue(first.dangerTargetChance < third.dangerTargetChance)
        assertTrue(third.dangerTargetChance < fifth.dangerTargetChance)
        assertEquals(0.34f, fifth.dangerTargetChance, 0.0001f)
        assertTrue(DifficultyProgression.forScore(10_000, GameConfig(dangerTargetChance = 0.40f))
            .dangerTargetChance <= 0.45f)
    }


    @Test
    fun engineUsesFasterSpawnIntervalAndShorterLifetimeAfterLevelUp() {
        val config = GameConfig(
            pointsPerGoodHit = 100,
            targetLifetimeMs = 1_400L,
            spawnIntervalMs = 1_000L,
            dangerTargetChance = 0f,
            powerupSpawnChance = 0f,
            maxActiveTargets = 1
        )
        val engine = GameEngine(config, Random(15))
        engine.start()
        engine.advanceTime(config.spawnIntervalMs)
        engine.tapTarget(engine.state.targets.single().id)

        val beforeFasterSpawn = engine.advanceTime(919L)
        assertTrue(beforeFasterSpawn.targets.isEmpty())

        val afterFasterSpawn = engine.advanceTime(1L)
        assertEquals(1, afterFasterSpawn.targets.size)
        assertEquals(1_316L, afterFasterSpawn.targets.single().remainingLifetimeMs)
    }

    @Test
    fun engineUpdatesDisplayedLevelWhenScoreCrossesThreshold() {
        val config = GameConfig(
            pointsPerGoodHit = 100,
            targetLifetimeMs = 2_000L,
            spawnIntervalMs = 100L,
            dangerTargetChance = 0f,
            powerupSpawnChance = 0f
        )
        val engine = GameEngine(config, Random(12))
        engine.start()
        engine.advanceTime(config.spawnIntervalMs)

        val target = engine.state.targets.single()
        val updated = engine.tapTarget(target.id)

        assertEquals(100, updated.score)
        assertEquals(2, updated.difficultyLevel)
    }
}
