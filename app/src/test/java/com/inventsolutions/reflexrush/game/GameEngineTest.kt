package com.inventsolutions.reflexrush.game

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

    @Test
    fun startCreatesFreshRunningRound() {
        val engine = GameEngine(testConfig())

        val state = engine.start()

        assertEquals(GameStatus.RUNNING, state.status)
        assertEquals(0, state.score)
        assertEquals(3, state.lives)
        assertTrue(state.targets.isEmpty())
    }

    @Test
    fun startingAgainResetsScoreLivesAndTargets() {
        val engine = GameEngine(testConfig(dangerTargetChance = 0f), Random(1))
        engine.start()
        engine.advanceTime(100L)
        val target = engine.state.targets.single()
        engine.tapTarget(target.id)

        val restarted = engine.start()

        assertEquals(0, restarted.score)
        assertEquals(3, restarted.lives)
        assertTrue(restarted.targets.isEmpty())
        assertEquals(GameStatus.RUNNING, restarted.status)
    }

    @Test
    fun goodTargetAwardsPointsOnlyOnce() {
        val engine = GameEngine(testConfig(dangerTargetChance = 0f), Random(2))
        engine.start()
        val target = spawnOne(engine)

        val afterFirstTap = engine.tapTarget(target.id)
        val afterSecondTap = engine.tapTarget(target.id)

        assertEquals(10, afterFirstTap.score)
        assertEquals(10, afterSecondTap.score)
        assertTrue(afterSecondTap.targets.isEmpty())
    }

    @Test
    fun dangerTargetSubtractsPointsButNeverBelowZero() {
        val random = ScriptedRandom(
            0.9f, 0.5f, 0.5f,
            0.1f, 0.5f, 0.5f
        )
        val engine = GameEngine(
            testConfig(
                maxActiveTargets = 2,
                targetLifetimeMs = 1_000L,
                dangerTargetChance = 0.5f
            ),
            random
        )
        engine.start()

        engine.advanceTime(100L)
        val goodTarget = engine.state.targets.single()
        engine.tapTarget(goodTarget.id)
        assertEquals(10, engine.state.score)

        engine.advanceTime(100L)
        val dangerTarget = engine.state.targets.single()
        engine.tapTarget(dangerTarget.id)

        assertEquals(0, engine.state.score)
        assertEquals(3, engine.state.lives)
    }

    @Test
    fun goodTargetExpiryCostsOneLife() {
        val engine = GameEngine(testConfig(dangerTargetChance = 0f), Random(4))
        engine.start()
        spawnOne(engine)

        val state = engine.advanceTime(50L)

        assertEquals(2, state.lives)
        assertTrue(state.targets.isEmpty())
        assertEquals(GameStatus.RUNNING, state.status)
    }

    @Test
    fun dangerTargetExpiryDoesNotCostLife() {
        val engine = GameEngine(testConfig(dangerTargetChance = 1f), Random(5))
        engine.start()
        spawnOne(engine)

        val state = engine.advanceTime(50L)

        assertEquals(3, state.lives)
        assertTrue(state.targets.isEmpty())
    }

    @Test
    fun losingLastLifeEndsRoundAndClearsTargets() {
        val config = testConfig(startingLives = 1, dangerTargetChance = 0f)
        val engine = GameEngine(config, Random(6))
        engine.start()
        spawnOne(engine)

        val state = engine.advanceTime(50L)

        assertEquals(GameStatus.GAME_OVER, state.status)
        assertEquals(0, state.lives)
        assertTrue(state.targets.isEmpty())
        assertEquals(state, engine.advanceTime(500L))
    }

    @Test
    fun targetCountNeverExceedsConfiguredMaximum() {
        val engine = GameEngine(
            testConfig(maxActiveTargets = 2, targetLifetimeMs = 1_000L),
            Random(7)
        )
        engine.start()

        val state = engine.advanceTime(500L)

        assertEquals(2, state.targets.size)
    }

    @Test
    fun generatedTargetPositionsStayInsideSafePlayArea() {
        val engine = GameEngine(testConfig(dangerTargetChance = 0f), Random(8))
        engine.start()

        val target = spawnOne(engine)

        assertTrue(target.x in 0.08f..0.92f)
        assertTrue(target.y in 0.08f..0.92f)
    }

    @Test
    fun callsOutsideRunningRoundDoNotChangeState() {
        val engine = GameEngine(testConfig())
        val initial = engine.state

        assertEquals(initial, engine.advanceTime(1_000L))
        assertEquals(initial, engine.tapTarget(1L))
        assertFalse(engine.state.status == GameStatus.RUNNING)
    }


    @Test
    fun nonPositiveElapsedTimeDoesNotAdvanceRound() {
        val engine = GameEngine(testConfig())
        val initial = engine.start()

        assertEquals(initial, engine.advanceTime(0L))
        assertEquals(initial, engine.advanceTime(-10L))
    }

    @Test
    fun multipleMissedGoodTargetsCanEndRoundInOneUpdate() {
        val engine = GameEngine(
            testConfig(
                startingLives = 3,
                maxActiveTargets = 3,
                targetLifetimeMs = 50L,
                spawnIntervalMs = 100L,
                dangerTargetChance = 0f
            ),
            Random(18)
        )
        engine.start()
        engine.advanceTime(300L)

        assertEquals(3, engine.state.targets.size)

        val state = engine.advanceTime(50L)

        assertEquals(GameStatus.GAME_OVER, state.status)
        assertEquals(0, state.lives)
        assertTrue(state.targets.isEmpty())
    }

    @Test
    fun restartingAfterLevelUpResetsDifficultyAndRoundState() {
        val engine = GameEngine(
            testConfig(
                pointsPerGoodHit = 100,
                targetLifetimeMs = 2_000L,
                spawnIntervalMs = 100L,
                dangerTargetChance = 0f
            ),
            Random(19)
        )
        engine.start()
        engine.advanceTime(100L)
        engine.tapTarget(engine.state.targets.single().id)

        assertEquals(2, engine.state.difficultyLevel)

        val restarted = engine.start()

        assertEquals(1, restarted.difficultyLevel)
        assertEquals(0, restarted.score)
        assertEquals(3, restarted.lives)
        assertTrue(restarted.targets.isEmpty())
    }

    private class ScriptedRandom(vararg values: Float) : Random() {
        private val sequence = values.iterator()

        override fun nextBits(bitCount: Int): Int = 0

        override fun nextFloat(): Float = sequence.next()
    }

    private fun spawnOne(engine: GameEngine): GameTarget {
        engine.advanceTime(engine.config.spawnIntervalMs)
        return engine.state.targets.first()
    }

    private fun testConfig(
        startingLives: Int = 3,
        pointsPerGoodHit: Int = 10,
        maxActiveTargets: Int = 1,
        targetLifetimeMs: Long = 50L,
        spawnIntervalMs: Long = 100L,
        dangerTargetChance: Float = 0f
    ) = GameConfig(
        startingLives = startingLives,
        pointsPerGoodHit = pointsPerGoodHit,
        dangerHitPenalty = 10,
        targetLifetimeMs = targetLifetimeMs,
        spawnIntervalMs = spawnIntervalMs,
        maxActiveTargets = maxActiveTargets,
        dangerTargetChance = dangerTargetChance
    )
}
