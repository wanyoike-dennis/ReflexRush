package com.inventsolutions.reflexrush.game

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TargetFactoryTest {

    @Test
    fun createsGoodTargetWithConfiguredIdentityLifetimeAndPositionBounds() {
        val config = GameConfig(
            minTargetPosition = 0.2f,
            maxTargetPosition = 0.8f,
            dangerTargetChance = 0f,
            powerupSpawnChance = 0f
        )
        val factory = TargetFactory(
            config,
            GameSessionConfig(gameConfig = config, enabledPowerups = emptySet()),
            ScriptedRandom(0.9f, 0.25f, 0.75f)
        )

        val result = factory.create(
            id = 42L,
            difficulty = difficulty(lifetimeMs = 900L),
            activeTimedPowerup = null
        )

        assertEquals(42L, result.target.id)
        assertEquals(TargetType.GOOD, result.target.type)
        assertEquals(0.35f, result.target.x, 0.0001f)
        assertEquals(0.65f, result.target.y, 0.0001f)
        assertEquals(900L, result.target.remainingLifetimeMs)
        assertNull(result.spawnedPowerup)
    }

    @Test
    fun dangerProbabilityIsAppliedBeforePowerupProbability() {
        val config = GameConfig(powerupSpawnChance = 0.5f)
        val factory = TargetFactory(
            config,
            GameSessionConfig(gameConfig = config),
            ScriptedRandom(0.1f, 0.4f, 0.6f)
        )

        val result = factory.create(1L, difficulty(dangerChance = 0.2f), null)

        assertEquals(TargetType.DANGER, result.target.type)
        assertNull(result.spawnedPowerup)
        assertNull(result.target.powerupType)
    }

    @Test
    fun selectsAnEnabledPowerupAndReturnsItsSpawnFact() {
        val config = GameConfig(dangerTargetChance = 0f, powerupSpawnChance = 1f)
        val factory = TargetFactory(
            config,
            GameSessionConfig(gameConfig = config),
            ScriptedRandom(0.2f, 0.75f, 0.5f, 0.25f)
        )

        val result = factory.create(7L, difficulty(), null)

        assertEquals(TargetType.POWERUP, result.target.type)
        assertEquals(PowerupType.SHIELD, result.target.powerupType)
        assertEquals(PowerupType.SHIELD, result.spawnedPowerup)
    }

    @Test
    fun activeTimedPowerupOnlyAllowsGoldenStarOrShieldToSpawn() {
        val config = GameConfig(dangerTargetChance = 0f, powerupSpawnChance = 1f)
        val factory = TargetFactory(
            config,
            GameSessionConfig(gameConfig = config),
            // With an active timed effect, the chance is halved and the
            // available list is [GOLDEN_STAR, SHIELD].
            ScriptedRandom(0.1f, 0.9f, 0.5f, 0.5f)
        )

        val result = factory.create(
            id = 3L,
            difficulty = difficulty(),
            activeTimedPowerup = PowerupType.DOUBLE_POINTS
        )

        assertEquals(PowerupType.SHIELD, result.target.powerupType)
        assertEquals(PowerupType.SHIELD, result.spawnedPowerup)
    }

    @Test
    fun disabledPowerupsCannotSpawn() {
        val config = GameConfig(dangerTargetChance = 0f, powerupSpawnChance = 1f)
        val factory = TargetFactory(
            config,
            GameSessionConfig(gameConfig = config, enabledPowerups = emptySet()),
            ScriptedRandom(0.2f, 0.5f, 0.5f)
        )

        val result = factory.create(1L, difficulty(), null)

        assertEquals(TargetType.GOOD, result.target.type)
        assertNull(result.target.powerupType)
        assertNull(result.spawnedPowerup)
    }

    @Test
    fun powerupSelectionRespectsTheSessionEnabledSet() {
        val config = GameConfig(dangerTargetChance = 0f, powerupSpawnChance = 1f)
        val factory = TargetFactory(
            config,
            GameSessionConfig(
                gameConfig = config,
                enabledPowerups = setOf(PowerupType.GOLDEN_STAR)
            ),
            ScriptedRandom(0.2f, 0.99f, 0.5f, 0.5f)
        )

        val result = factory.create(1L, difficulty(), null)

        assertEquals(PowerupType.GOLDEN_STAR, result.target.powerupType)
        assertEquals(PowerupType.GOLDEN_STAR, result.spawnedPowerup)
    }

    @Test
    fun generatedCoordinatesRemainInsideConfiguredRange() {
        val config = GameConfig(
            minTargetPosition = 0.15f,
            maxTargetPosition = 0.85f,
            dangerTargetChance = 0f,
            powerupSpawnChance = 0f
        )
        val factory = TargetFactory(
            config,
            GameSessionConfig(gameConfig = config, enabledPowerups = emptySet()),
            ScriptedRandom(0.9f, 0f, 0.999f)
        )

        val target = factory.create(1L, difficulty(), null).target

        assertTrue(target.x in 0.15f..0.85f)
        assertTrue(target.y in 0.15f..0.85f)
    }

    private fun difficulty(
        lifetimeMs: Long = 1_400L,
        dangerChance: Float = 0f
    ) = DifficultyProfile(
        level = 1,
        spawnIntervalMs = 750L,
        targetLifetimeMs = lifetimeMs,
        dangerTargetChance = dangerChance
    )

    private class ScriptedRandom(vararg values: Float) : Random() {
        private val sequence = values.iterator()

        override fun nextBits(bitCount: Int): Int = 0

        override fun nextFloat(): Float = sequence.next()
    }
}
