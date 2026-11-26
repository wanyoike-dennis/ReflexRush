package com.inventsolutions.reflexrush.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TargetLifecycleTest {

    private val lifecycle = TargetLifecycle()

    @Test
    fun reducesRemainingLifetimeForTargetsThatStayActive() {
        val target = target(id = 1L, lifetimeMs = 1_000L)

        val result = lifecycle.advance(listOf(target), 250L)

        assertTrue(result.expiredTargets.isEmpty())
        assertEquals(750L, result.remainingTargets.single().remainingLifetimeMs)
        assertEquals(1L, result.remainingTargets.single().id)
        // The source model is immutable and remains unchanged.
        assertEquals(1_000L, target.remainingLifetimeMs)
    }

    @Test
    fun expiresTargetsWhoseLifetimeIsEqualToOrBelowElapsedTime() {
        val targets = listOf(
            target(id = 1L, lifetimeMs = 100L),
            target(id = 2L, lifetimeMs = 200L),
            target(id = 3L, lifetimeMs = 201L)
        )

        val result = lifecycle.advance(targets, 200L)

        assertEquals(listOf(1L, 2L), result.expiredTargets.map { it.id })
        assertEquals(listOf(3L), result.remainingTargets.map { it.id })
        assertEquals(1L, result.remainingTargets.single().remainingLifetimeMs)
    }

    @Test
    fun zeroElapsedTimePreservesAllTargets() {
        val targets = listOf(
            target(id = 1L, lifetimeMs = 100L),
            target(id = 2L, lifetimeMs = 200L)
        )

        val result = lifecycle.advance(targets, 0L)

        assertTrue(result.expiredTargets.isEmpty())
        assertEquals(targets, result.remainingTargets)
    }

    @Test
    fun handlesEmptyTargetCollection() {
        val result = lifecycle.advance(emptyList(), 500L)

        assertTrue(result.expiredTargets.isEmpty())
        assertTrue(result.remainingTargets.isEmpty())
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNegativeElapsedTime() {
        lifecycle.advance(listOf(target(id = 1L, lifetimeMs = 100L)), -1L)
    }

    private fun target(id: Long, lifetimeMs: Long) = GameTarget(
        id = id,
        type = TargetType.GOOD,
        x = 0.5f,
        y = 0.5f,
        remainingLifetimeMs = lifetimeMs
    )
}
