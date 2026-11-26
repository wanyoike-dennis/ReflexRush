package com.inventsolutions.reflexrush.game

import org.junit.Assert.assertEquals
import org.junit.Test

class ComboTransitionResolverTest {

    private val resolver = ComboTransitionResolver(ComboRules())

    @Test
    fun goodHitsAdvanceComboAndEmitMilestoneEventsAtThresholds() {
        val first = resolver.afterGoodHit(previousCount = 2, previousMultiplier = 1)
        assertEquals(3, first.comboCount)
        assertEquals(2, first.multiplier)
        assertEquals(
            listOf(
                GameplayEvent.ComboChanged(2, 3, 2),
                GameplayEvent.ComboMilestone(3, 2)
            ),
            first.events
        )

        val second = resolver.afterGoodHit(previousCount = 4, previousMultiplier = 2)
        assertEquals(5, second.comboCount)
        assertEquals(3, second.multiplier)
        assertEquals(
            listOf(
                GameplayEvent.ComboChanged(4, 5, 3),
                GameplayEvent.ComboMilestone(5, 3)
            ),
            second.events
        )
    }

    @Test
    fun goodHitAboveThresholdChangesCountWithoutRepeatingMilestone() {
        val result = resolver.afterGoodHit(previousCount = 5, previousMultiplier = 3)

        assertEquals(6, result.comboCount)
        assertEquals(3, result.multiplier)
        assertEquals(
            listOf(GameplayEvent.ComboChanged(5, 6, 3)),
            result.events
        )
    }

    @Test
    fun breakingActiveComboResetsCountAndMultiplier() {
        val result = resolver.breakCombo(previousCount = 7, previousMultiplier = 3)

        assertEquals(0, result.comboCount)
        assertEquals(1, result.multiplier)
        assertEquals(
            listOf(GameplayEvent.ComboChanged(7, 0, 1)),
            result.events
        )
    }

    @Test
    fun breakingInactiveComboDoesNotEmitAnEvent() {
        val result = resolver.breakCombo(previousCount = 0, previousMultiplier = 1)

        assertEquals(0, result.comboCount)
        assertEquals(1, result.multiplier)
        assertEquals(emptyList<GameplayEvent>(), result.events)
    }
}
