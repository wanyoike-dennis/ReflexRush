package com.inventsolutions.reflexrush.game

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameModesTest {

    @Test
    fun timeAttackEndsWhenItsConfiguredClockRunsOut() {
        val config = GameSessionConfig(
            mode = GameMode.TIME_ATTACK,
            gameConfig = GameConfig(
                spawnIntervalMs = 100L,
                targetLifetimeMs = 1_000L,
                dangerTargetChance = 0f,
                powerupSpawnChance = 0f
            ),
            timeAttackDurationMs = 500L
        )
        val engine = GameEngine(config, Random(1))
        val started = engine.start()

        assertEquals(500L, started.timeRemainingMs)
        val finished = engine.advanceTime(500L)

        assertEquals(GameStatus.GAME_OVER, finished.status)
        assertEquals(0L, finished.timeRemainingMs)
        assertEquals(500L, engine.completedSessionSummary?.durationMs)
        assertEquals(GameMode.TIME_ATTACK, engine.completedSessionSummary?.mode)
    }

    @Test
    fun survivalUsesThreeLivesAndTighterTargetTimingThanClassic() {
        val gameConfig = GameConfig(
            spawnIntervalMs = 750L,
            targetLifetimeMs = 1_400L,
            dangerTargetChance = 0f,
            powerupSpawnChance = 0f
        )
        val survival = GameEngine(
            GameSessionConfig(mode = GameMode.SURVIVAL, gameConfig = gameConfig),
            Random(2)
        )
        val classic = GameEngine(
            GameSessionConfig(mode = GameMode.CLASSIC, gameConfig = gameConfig),
            Random(2)
        )

        assertEquals(3, survival.start().lives)
        assertEquals(3, classic.start().lives)

        val survivalTarget = survival.advanceTime(615L).targets.single()
        assertEquals(1_260L, survivalTarget.remainingLifetimeMs)
        assertTrue(survival.state.status == GameStatus.RUNNING)
    }

    @Test
    fun precisionCompletesAfterConfiguredNumberOfResolvedTargetsAndPenalisesMisses() {
        val config = GameSessionConfig(
            mode = GameMode.PRECISION,
            gameConfig = GameConfig(
                startingLives = 3,
                spawnIntervalMs = 100L,
                targetLifetimeMs = 1_000L,
                maxActiveTargets = 1,
                dangerTargetChance = 0f,
                powerupSpawnChance = 0f
            ),
            precisionTargetCount = 2,
            precisionMissPenalty = 5,
            enabledPowerups = emptySet()
        )
        val engine = GameEngine(config, Random(3))
        val started = engine.start()

        assertEquals(2, started.targetLimit)
        assertTrue(started.timeRemainingMs == null)

        engine.advanceTime(100L)
        val firstTarget = engine.state.targets.single()
        engine.tapTarget(firstTarget.id)
        assertEquals(10, engine.state.score)
        assertEquals(1, engine.state.targetsResolved)

        engine.advanceTime(100L)
        assertEquals(1, engine.state.targets.size)
        val secondTarget = engine.state.targets.single()
        val finished = engine.advanceTime(1_000L)

        assertEquals(GameStatus.GAME_OVER, finished.status)
        assertEquals(2, finished.targetsResolved)
        assertEquals(3, finished.lives)
        assertEquals(5, finished.score)
        assertEquals(1, engine.completedSessionSummary?.goodTargetsHit)
        assertEquals(1, engine.completedSessionSummary?.goodTargetsMissed)
        assertEquals(GameMode.PRECISION, engine.completedSessionSummary?.mode)
        assertNotNull(engine.completedSessionSummary)
        assertTrue(engine.drainEvents().contains(GameplayEvent.PrecisionMissPenalty(5, 5)))
        assertFalse(finished.targets.any { it.id == secondTarget.id })
    }

    @Test
    fun precisionModeDoesNotSpawnMoreThanItsFixedTargetCount() {
        val config = GameSessionConfig(
            mode = GameMode.PRECISION,
            gameConfig = GameConfig(
                spawnIntervalMs = 100L,
                targetLifetimeMs = 10_000L,
                maxActiveTargets = 3,
                dangerTargetChance = 0f,
                powerupSpawnChance = 0f
            ),
            precisionTargetCount = 3,
            enabledPowerups = emptySet()
        )
        val engine = GameEngine(config, Random(4))
        engine.start()

        engine.advanceTime(300L)

        assertEquals(3, engine.state.targets.size)
        assertEquals(3, engine.state.targetLimit)
    }

    @Test
    fun modeDefaultsKeepPowerupsAppropriateToTheChallenge() {
        assertEquals(
            setOf(PowerupType.GOLDEN_STAR, PowerupType.DOUBLE_POINTS, PowerupType.SLOW_TIME),
            GameSessionConfig.forMode(GameMode.TIME_ATTACK).enabledPowerups
        )
        assertEquals(
            setOf(PowerupType.GOLDEN_STAR),
            GameSessionConfig.forMode(GameMode.PRECISION).enabledPowerups
        )
        assertEquals(PowerupType.entries.toSet(), GameSessionConfig.forMode(GameMode.CLASSIC).enabledPowerups)
    }
}
