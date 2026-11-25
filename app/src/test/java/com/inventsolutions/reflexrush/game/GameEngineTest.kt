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
        val engine = GameEngine(testConfig(dangerTargetChance = 1f), Random(3))
        engine.start()
        val firstDanger = spawnOne(engine)
        engine.tapTarget(firstDanger.id)
        assertEquals(0, engine.state.score)

        engine.advanceTime(100L)
        val secondDanger = engine.state.targets.single()
        engine.tapTarget(secondDanger.id)

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

    private fun spawnOne(engine: GameEngine): GameTarget {
        engine.advanceTime(engine.config.spawnIntervalMs)
        return engine.state.targets.first()
    }

    private fun testConfig(
        startingLives: Int = 3,
        maxActiveTargets: Int = 1,
        targetLifetimeMs: Long = 50L,
        spawnIntervalMs: Long = 100L,
        dangerTargetChance: Float = 0f
    ) = GameConfig(
        startingLives = startingLives,
        pointsPerGoodHit = 10,
        dangerHitPenalty = 10,
        targetLifetimeMs = targetLifetimeMs,
        spawnIntervalMs = spawnIntervalMs,
        maxActiveTargets = maxActiveTargets,
        dangerTargetChance = dangerTargetChance
    )
}
