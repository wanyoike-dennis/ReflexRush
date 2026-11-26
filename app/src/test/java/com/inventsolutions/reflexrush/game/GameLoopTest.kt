package com.inventsolutions.reflexrush.game

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameLoopTest {
    @Test
    fun firstFrameOnlyEstablishesClockBaseline() {
        val controller = controller()
        val initial = controller.start().state
        val loop = GameLoop()

        assertNull(loop.onFrame(1_000_000_000L, initial, controller))
    }

    @Test
    fun simulationAdvancesAtConfiguredTickIntervalAndPublishesSpawnImmediately() {
        val controller = controller(spawnIntervalMs = 100L)
        val initial = controller.start().state
        val loop = GameLoop(tickIntervalMs = 33L, uiUpdateIntervalMs = 100L)

        assertNull(loop.onFrame(1_000_000_000L, initial, controller))
        val first = loop.onFrame(1_040_000_000L, initial, controller)
        val second = loop.onFrame(1_080_000_000L, initial, controller)
        val third = loop.onFrame(1_120_000_000L, initial, controller)

        assertNotNull(first)
        assertNotNull(second)
        assertNotNull(third)
        assertFalse(first!!.shouldPublishState)
        assertFalse(second!!.shouldPublishState)
        assertTrue(third!!.shouldPublishState)
        assertEquals(1, third.action.state.targets.size)
    }

    @Test
    fun resetDiscardsElapsedTimeFromPreviousRunOrPause() {
        val controller = controller(spawnIntervalMs = 100L)
        val initial = controller.start().state
        val loop = GameLoop()
        loop.onFrame(1_000_000_000L, initial, controller)
        loop.onFrame(1_040_000_000L, initial, controller)

        loop.reset()
        assertNull(loop.onFrame(5_000_000_000L, controller.state, controller))
        val next = loop.onFrame(5_040_000_000L, controller.state, controller)

        assertNotNull(next)
        assertFalse(next!!.shouldPublishState)
        assertTrue(controller.state.targets.isEmpty())
    }

    private fun controller(spawnIntervalMs: Long = 750L) = GameController(
        GameEngine(
            GameConfig(
                spawnIntervalMs = spawnIntervalMs,
                targetLifetimeMs = 500L,
                dangerTargetChance = 0f,
                powerupSpawnChance = 0f
            ),
            Random(7)
        )
    )
}
