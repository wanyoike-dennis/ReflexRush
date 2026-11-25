package com.inventsolutions.reflexrush.game

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameSessionArchitectureTest {

    @Test
    fun defaultSessionUsesClassicModeAndPreservesConfiguredScoring() {
        val config = GameConfig(pointsPerGoodHit = 25, dangerHitPenalty = 7)
        val session = GameSessionConfig(gameConfig = config)

        assertEquals(GameMode.CLASSIC, session.mode)
        assertEquals(25, session.scoreRules.pointsPerGoodHit)
        assertEquals(7, session.scoreRules.dangerHitPenalty)
    }

    @Test
    fun startEmitsRoundStartedEventAndDrainClearsEvents() {
        val engine = GameEngine()

        engine.start()

        assertEquals(
            listOf(GameplayEvent.RoundStarted(GameMode.CLASSIC)),
            engine.drainEvents()
        )
        assertTrue(engine.drainEvents().isEmpty())
    }

    @Test
    fun goodTargetHitEmitsScoringEvent() {
        val engine = engineWith(GameConfig(dangerTargetChance = 0f))
        engine.start()
        engine.drainEvents()
        engine.advanceTime(engine.config.spawnIntervalMs)
        val target = engine.state.targets.single()
        engine.drainEvents()

        engine.tapTarget(target.id)

        assertEquals(
            listOf(
                GameplayEvent.GoodTargetHit(target.id, 10, 10),
                GameplayEvent.ComboChanged(previousCount = 0, newCount = 1, multiplier = 1)
            ),
            engine.drainEvents()
        )
    }

    @Test
    fun dangerTargetEventReportsOnlyThePenaltyActuallyApplied() {
        val engine = engineWith(GameConfig(dangerTargetChance = 1f, powerupSpawnChance = 0f))
        engine.start()
        engine.drainEvents()
        engine.advanceTime(engine.config.spawnIntervalMs)
        val target = engine.state.targets.single()
        engine.drainEvents()

        engine.tapTarget(target.id)

        assertEquals(
            listOf(GameplayEvent.DangerTargetHit(target.id, 0, 0)),
            engine.drainEvents()
        )
    }

    @Test
    fun levelChangeIsEmittedWhenClassicScoreCrossesThreshold() {
        val config = GameConfig(
            pointsPerGoodHit = 100,
            targetLifetimeMs = 2_000L,
            dangerTargetChance = 0f
        )
        val engine = engineWith(config)
        engine.start()
        engine.drainEvents()
        engine.advanceTime(engine.config.spawnIntervalMs)
        val target = engine.state.targets.single()
        engine.drainEvents()

        engine.tapTarget(target.id)

        assertEquals(
            listOf(
                GameplayEvent.GoodTargetHit(target.id, 100, 100),
                GameplayEvent.ComboChanged(previousCount = 0, newCount = 1, multiplier = 1),
                GameplayEvent.LevelChanged(1, 2)
            ),
            engine.drainEvents()
        )
    }

    @Test
    fun completedClassicRoundExposesSummaryAndCompletionEvent() {
        val config = GameConfig(
            startingLives = 1,
            targetLifetimeMs = 50L,
            spawnIntervalMs = 100L,
            dangerTargetChance = 0f
        )
        val engine = engineWith(config)
        engine.start()
        engine.drainEvents()

        engine.advanceTime(100L)
        val target = engine.state.targets.single()
        engine.drainEvents()
        engine.advanceTime(50L)

        val summary = engine.completedSessionSummary
        assertNotNull(summary)
        assertEquals(
            GameSessionSummary(
                mode = GameMode.CLASSIC,
                finalScore = 0,
                levelReached = 1,
                livesRemaining = 0,
                goodTargetsHit = 0,
                dangerTargetsHit = 0,
                targetsMissed = 1,
                goodTargetsMissed = 1,
                durationMs = 150L
            ),
            summary
        )
        assertEquals(
            listOf(
                GameplayEvent.TargetMissed(target.id, TargetType.GOOD),
                GameplayEvent.LifeLost(0),
                GameplayEvent.RoundCompleted(summary!!)
            ),
            engine.drainEvents()
        )
    }

    @Test
    fun restartingClearsCompletedSummaryAndStartsFreshSession() {
        val engine = engineWith(
            GameConfig(startingLives = 1, targetLifetimeMs = 50L, dangerTargetChance = 0f)
        )
        engine.start()
        engine.drainEvents()
        engine.advanceTime(engine.config.spawnIntervalMs)
        engine.advanceTime(engine.config.targetLifetimeMs)
        assertNotNull(engine.completedSessionSummary)
        engine.drainEvents()

        engine.start()

        assertNull(engine.completedSessionSummary)
        assertEquals(0, engine.state.score)
        assertEquals(1, engine.state.lives)
        assertEquals(listOf(GameplayEvent.RoundStarted(GameMode.CLASSIC)), engine.drainEvents())
    }

    @Test
    fun nonClassicModesCannotAccidentallyRunClassicRules() {
        val session = GameSessionConfig(mode = GameMode.TIME_ATTACK)

        val error = runCatching { GameEngine(session) }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
        assertFalse(error?.message.isNullOrBlank())
    }

    private fun engineWith(config: GameConfig) = GameEngine(
        sessionConfig = GameSessionConfig(gameConfig = config.copy(powerupSpawnChance = 0f)),
        random = Random(42)
    )
}
