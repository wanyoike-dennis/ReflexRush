package com.inventsolutions.reflexrush.game

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameControllerTest {
    @Test
    fun startReturnsFreshStateAndRoundStartedEvent() {
        val controller = GameController(GameEngine())

        val result = controller.start()

        assertEquals(GameStatus.RUNNING, result.state.status)
        assertEquals(listOf(GameplayEvent.RoundStarted(GameMode.CLASSIC)), result.events)
        assertEquals(result.state, controller.state)
    }

    @Test
    fun targetTapReturnsUpdatedStateAndScoringEvents() {
        val config = GameConfig(dangerTargetChance = 0f, powerupSpawnChance = 0f)
        val controller = GameController(GameEngine(config, Random(31)))
        controller.start()
        controller.advanceTime(config.spawnIntervalMs)
        val target = controller.state.targets.single()

        val result = controller.tapTarget(target.id)

        assertEquals(10, result.state.score)
        assertTrue(result.state.targets.isEmpty())
        assertTrue(result.events.any { it is GameplayEvent.GoodTargetHit })
        assertEquals(result.state, controller.state)
    }
}
