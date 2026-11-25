package com.inventsolutions.reflexrush.game

import org.junit.Assert.assertEquals
import org.junit.Test

class BestScoreTest {

    @Test
    fun higherRoundScoreBecomesNewBest() {
        assertEquals(120, BestScore.afterRound(80, 120))
    }

    @Test
    fun lowerRoundScoreDoesNotReplaceBest() {
        assertEquals(120, BestScore.afterRound(120, 70))
    }

    @Test
    fun equalRoundScoreKeepsExistingBest() {
        assertEquals(120, BestScore.afterRound(120, 120))
    }

    @Test
    fun firstPositiveScoreBecomesBest() {
        assertEquals(10, BestScore.afterRound(0, 10))
    }
}
