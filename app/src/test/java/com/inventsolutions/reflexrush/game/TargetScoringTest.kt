package com.inventsolutions.reflexrush.game

import org.junit.Assert.assertEquals
import org.junit.Test

class TargetScoringTest {

    private val scoring = TargetScoring(ScoreRules(pointsPerGoodHit = 10, dangerHitPenalty = 10))

    @Test
    fun goodTargetUsesBasePointsWhenNoMultipliersAreActive() {
        val result = scoring.score(
            currentScore = 20,
            targetType = TargetType.GOOD
        )

        assertEquals(10, result.pointsDelta)
        assertEquals(30, result.newScore)
    }

    @Test
    fun comboMultiplierIsAppliedToGoodTarget() {
        val result = scoring.score(
            currentScore = 20,
            targetType = TargetType.GOOD,
            comboMultiplier = 3
        )

        assertEquals(30, result.pointsDelta)
        assertEquals(50, result.newScore)
    }

    @Test
    fun doublePointsStacksWithComboMultiplier() {
        val result = scoring.score(
            currentScore = 20,
            targetType = TargetType.GOOD,
            comboMultiplier = 3,
            doublePointsActive = true
        )

        assertEquals(60, result.pointsDelta)
        assertEquals(80, result.newScore)
    }

    @Test
    fun dangerPenaltyIsNotMultipliedAndCannotMakeScoreNegative() {
        val result = scoring.score(
            currentScore = 4,
            targetType = TargetType.DANGER,
            comboMultiplier = 5,
            doublePointsActive = true
        )

        assertEquals(-4, result.pointsDelta)
        assertEquals(0, result.newScore)
    }

    @Test(expected = IllegalStateException::class)
    fun powerupTargetCannotUseNormalScoring() {
        scoring.score(
            currentScore = 0,
            targetType = TargetType.POWERUP
        )
    }
}
