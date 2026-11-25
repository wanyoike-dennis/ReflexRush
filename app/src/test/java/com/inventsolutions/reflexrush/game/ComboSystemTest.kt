package com.inventsolutions.reflexrush.game

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ComboSystemTest {

    @Test
    fun multiplierUsesTheDefinedComboBoundaries() {
        val rules = ComboRules()

        assertEquals(1, rules.multiplierFor(0))
        assertEquals(1, rules.multiplierFor(1))
        assertEquals(1, rules.multiplierFor(2))
        assertEquals(2, rules.multiplierFor(3))
        assertEquals(2, rules.multiplierFor(4))
        assertEquals(3, rules.multiplierFor(5))
        assertEquals(3, rules.multiplierFor(9))
        assertEquals(5, rules.multiplierFor(10))
        assertEquals(5, rules.multiplierFor(25))
    }

    @Test
    fun consecutiveGoodHitsApplyMultipliersToScore() {
        val engine = engineWith(GameConfig(dangerTargetChance = 0f))

        engine.start()
        repeat(10) { hitNextGoodTarget(engine) }

        assertEquals(10, engine.state.comboCount)
        assertEquals(5, engine.state.comboMultiplier)
        assertEquals(10, engine.state.bestCombo)
        // 2 hits at 10 points, 2 at 20, 5 at 30, and the tenth at 50.
        assertEquals(260, engine.state.score)
    }

    @Test
    fun multiplierMilestonesEmitTypedEvents() {
        val engine = engineWith(GameConfig(dangerTargetChance = 0f))
        engine.start()
        val firstMilestoneEvents = buildList {
            repeat(3) { addAll(hitNextGoodTarget(engine)) }
        }
        assertTrue(
            firstMilestoneEvents.contains(
                GameplayEvent.ComboMilestone(comboCount = 3, multiplier = 2)
            )
        )

        val secondMilestoneEvents = buildList {
            repeat(2) { addAll(hitNextGoodTarget(engine)) }
        }
        assertTrue(
            secondMilestoneEvents.contains(
                GameplayEvent.ComboMilestone(comboCount = 5, multiplier = 3)
            )
        )
    }

    @Test
    fun tappingDangerTargetBreaksComboWithoutChangingItsPenaltyRule() {
        // Each target consumes three random values: type roll, x, then y.
        // The first two rolls must be above the 0.5 danger threshold;
        // the third deliberately falls below it.
        val random = ScriptedRandom(
            0.9f, 0.5f, 0.5f,
            0.9f, 0.5f, 0.5f,
            0.1f, 0.5f, 0.5f
        )
        val engine = GameEngine(
            GameConfig(
                targetLifetimeMs = 1_000L,
                spawnIntervalMs = 100L,
                dangerTargetChance = 0.5f,
                powerupSpawnChance = 0f
            ),
            random
        )
        engine.start()
        hitNextGoodTarget(engine)
        hitNextGoodTarget(engine)
        assertEquals(2, engine.state.comboCount)

        engine.advanceTime(engine.config.spawnIntervalMs)
        val dangerTarget = engine.state.targets.single()
        assertEquals(TargetType.DANGER, dangerTarget.type)
        engine.tapTarget(dangerTarget.id)

        assertEquals(0, engine.state.comboCount)
        assertEquals(1, engine.state.comboMultiplier)
        assertEquals(10, engine.state.score)
        assertTrue(
            engine.drainEvents().contains(
                GameplayEvent.ComboChanged(previousCount = 2, newCount = 0, multiplier = 1)
            )
        )
    }

    @Test
    fun missingGoodTargetBreaksComboAndCostsLife() {
        val engine = engineWith(
            GameConfig(
                targetLifetimeMs = 50L,
                spawnIntervalMs = 100L,
                dangerTargetChance = 0f
            )
        )
        engine.start()
        hitNextGoodTarget(engine)
        assertEquals(1, engine.state.comboCount)

        engine.advanceTime(engine.config.spawnIntervalMs)
        engine.drainEvents()
        val state = engine.advanceTime(engine.config.targetLifetimeMs)

        assertEquals(0, state.comboCount)
        assertEquals(1, state.comboMultiplier)
        assertEquals(2, state.lives)
        assertTrue(
            engine.drainEvents().contains(
                GameplayEvent.ComboChanged(previousCount = 1, newCount = 0, multiplier = 1)
            )
        )
    }

    @Test
    fun sessionSummaryRecordsBestCombo() {
        val engine = engineWith(
            GameConfig(
                startingLives = 1,
                targetLifetimeMs = 50L,
                spawnIntervalMs = 100L,
                dangerTargetChance = 0f
            )
        )
        engine.start()
        repeat(3) { hitNextGoodTarget(engine) }

        engine.advanceTime(engine.config.spawnIntervalMs)
        engine.advanceTime(engine.config.targetLifetimeMs)

        assertEquals(GameStatus.GAME_OVER, engine.state.status)
        assertEquals(3, engine.completedSessionSummary?.bestCombo)
    }

    @Test
    fun startingNewRoundResetsCurrentAndBestCombo() {
        val engine = engineWith(GameConfig(dangerTargetChance = 0f))
        engine.start()
        repeat(3) { hitNextGoodTarget(engine) }
        assertEquals(3, engine.state.bestCombo)

        engine.start()

        assertEquals(0, engine.state.comboCount)
        assertEquals(1, engine.state.comboMultiplier)
        assertEquals(0, engine.state.bestCombo)
    }

    private fun hitNextGoodTarget(engine: GameEngine): List<GameplayEvent> {
        engine.advanceTime(engine.config.spawnIntervalMs)
        val target = engine.state.targets.single()
        assertEquals(TargetType.GOOD, target.type)
        engine.tapTarget(target.id)
        return engine.drainEvents()
    }

    private fun engineWith(config: GameConfig) = GameEngine(config.copy(powerupSpawnChance = 0f), Random(123))

    private class ScriptedRandom(vararg values: Float) : Random() {
        private val sequence = values.iterator()

        override fun nextBits(bitCount: Int): Int = 0

        override fun nextFloat(): Float = sequence.next()
    }
}
