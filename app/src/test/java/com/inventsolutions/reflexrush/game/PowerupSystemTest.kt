package com.inventsolutions.reflexrush.game

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PowerupSystemTest {

    @Test
    fun goldenStarAwardsFiftyPointsWithoutChangingCombo() {
        val engine = engine(
            GameConfig(spawnIntervalMs = 100L, dangerTargetChance = 0f, powerupSpawnChance = 1f),
            0.2f, 0.0f, 0.5f, 0.5f
        )
        engine.start()
        engine.advanceTime(100L)
        val star = engine.state.targets.single()

        assertEquals(TargetType.POWERUP, star.type)
        assertEquals(PowerupType.GOLDEN_STAR, star.powerupType)
        engine.tapTarget(star.id)

        assertEquals(50, engine.state.score)
        assertEquals(0, engine.state.comboCount)
        val events = engine.drainEvents()
        assertTrue(events.contains(GameplayEvent.PowerupSpawned(PowerupType.GOLDEN_STAR)))
        assertTrue(events.contains(GameplayEvent.PowerupCollected(PowerupType.GOLDEN_STAR)))
        assertTrue(events.contains(GameplayEvent.PowerupActivated(PowerupType.GOLDEN_STAR)))
    }

    @Test
    fun doublePointsExpiresAfterFiveSeconds() {
        val engine = engine(
            GameConfig(spawnIntervalMs = 10_000L, dangerTargetChance = 0f, powerupSpawnChance = 1f),
            0.2f, 0.3f, 0.5f, 0.5f
        )
        engine.start()
        engine.advanceTime(10_000L)
        val doublePoints = engine.state.targets.single()
        assertEquals(PowerupType.DOUBLE_POINTS, doublePoints.powerupType)
        engine.tapTarget(doublePoints.id)

        assertEquals(PowerupType.DOUBLE_POINTS, engine.state.activeTimedPowerup)
        assertEquals(5_000L, engine.state.powerupRemainingMs)
        engine.advanceTime(5_000L)

        assertNull(engine.state.activeTimedPowerup)
        assertTrue(engine.drainEvents().contains(GameplayEvent.PowerupExpired(PowerupType.DOUBLE_POINTS)))
    }

    @Test
    fun doublePointsDoublesTheComboAdjustedGoodHitScore() {
        val engine = engine(
            GameConfig(
                spawnIntervalMs = 100L,
                dangerTargetChance = 0f,
                powerupSpawnChance = 0.5f
            ),
            0.2f, 0.3f, 0.5f, 0.5f, 0.9f, 0.5f, 0.5f
        )
        engine.start()
        engine.advanceTime(100L)
        engine.tapTarget(engine.state.targets.single().id)
        engine.advanceTime(100L)
        val goodTarget = engine.state.targets.single()

        engine.tapTarget(goodTarget.id)

        assertEquals(20, engine.state.score)
        assertEquals(1, engine.state.comboCount)
    }

    @Test
    fun sessionCanDisableIndividualPowerups() {
        val engine = GameEngine(
            sessionConfig = GameSessionConfig(
                gameConfig = GameConfig(
                    spawnIntervalMs = 100L,
                    dangerTargetChance = 0f,
                    powerupSpawnChance = 1f
                ),
                enabledPowerups = emptySet()
            ),
            random = ScriptedRandom(0.2f, 0.5f, 0.5f)
        )
        engine.start()
        engine.advanceTime(100L)

        assertEquals(TargetType.GOOD, engine.state.targets.single().type)
    }

    @Test
    fun slowTimeReducesTargetLifetimeAndSpawnClockProgress() {
        val engine = engine(
            GameConfig(
                spawnIntervalMs = 100L,
                targetLifetimeMs = 1_000L,
                maxActiveTargets = 1,
                dangerTargetChance = 0f,
                powerupSpawnChance = 0.5f
            ),
            0.2f, 0.7f, 0.5f, 0.5f, 0.9f, 0.5f, 0.5f
        )
        engine.start()
        engine.advanceTime(100L)
        val slowTime = engine.state.targets.single()
        assertEquals(PowerupType.SLOW_TIME, slowTime.powerupType)
        engine.tapTarget(slowTime.id)

        engine.advanceTime(100L)
        assertTrue(engine.state.targets.isEmpty())
        engine.advanceTime(100L)
        assertEquals(1, engine.state.targets.size)
        assertEquals(1_000L, engine.state.targets.single().remainingLifetimeMs)
        engine.advanceTime(100L)
        assertEquals(950L, engine.state.targets.single().remainingLifetimeMs)
    }

    @Test
    fun shieldPreventsOneMissedGoodTargetFromRemovingALife() {
        val engine = engine(
            GameConfig(
                spawnIntervalMs = 100L,
                // Keep the miss update shorter than the spawn interval so
                // the deterministic test does not create unrelated targets.
                targetLifetimeMs = 50L,
                dangerTargetChance = 0f,
                powerupSpawnChance = 0.5f
            ),
            0.2f, 0.95f, 0.5f, 0.5f, 0.9f, 0.5f, 0.5f
        )
        engine.start()
        engine.advanceTime(100L)
        val shield = engine.state.targets.single()
        assertEquals(PowerupType.SHIELD, shield.powerupType)
        engine.tapTarget(shield.id)
        assertTrue(engine.state.shieldAvailable)

        engine.advanceTime(100L)
        val goodTarget = engine.state.targets.single()
        assertEquals(TargetType.GOOD, goodTarget.type)
        engine.advanceTime(50L)

        assertEquals(3, engine.state.lives)
        assertFalse(engine.state.shieldAvailable)
        assertTrue(engine.drainEvents().contains(GameplayEvent.ShieldConsumed))
    }

    @Test
    fun startingNewRoundClearsAllPowerupEffects() {
        val engine = engine(
            GameConfig(spawnIntervalMs = 100L, dangerTargetChance = 0f, powerupSpawnChance = 1f),
            0.2f, 0.3f, 0.5f, 0.5f
        )
        engine.start()
        engine.advanceTime(100L)
        engine.tapTarget(engine.state.targets.single().id)
        assertEquals(PowerupType.DOUBLE_POINTS, engine.state.activeTimedPowerup)

        val restarted = engine.start()

        assertNull(restarted.activeTimedPowerup)
        assertEquals(0L, restarted.powerupRemainingMs)
        assertFalse(restarted.shieldAvailable)
    }

    private fun engine(config: GameConfig, vararg randomValues: Float): GameEngine =
        GameEngine(config, ScriptedRandom(*randomValues))

    private class ScriptedRandom(vararg values: Float) : Random() {
        private val sequence = values.iterator()
        override fun nextBits(bitCount: Int): Int = 0
        override fun nextFloat(): Float = sequence.next()
    }
}
