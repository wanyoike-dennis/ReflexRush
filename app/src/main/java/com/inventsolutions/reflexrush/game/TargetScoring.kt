package com.inventsolutions.reflexrush.game

/**
 * Resolves the score change for a tapped scoring target.
 * Combo and timed-powerup multipliers affect good hits only; danger penalties
 * remain fixed and are capped by the current score through ScoreRules.
 */
class TargetScoring(private val rules: ScoreRules) {
    fun score(
        currentScore: Int,
        targetType: TargetType,
        comboMultiplier: Int = 1,
        doublePointsActive: Boolean = false
    ): ScoreChange = when (targetType) {
        TargetType.GOOD -> {
            val timedMultiplier = if (doublePointsActive) 2 else 1
            rules.awardGoodHit(currentScore, comboMultiplier * timedMultiplier)
        }
        TargetType.DANGER -> rules.applyDangerPenalty(currentScore)
        TargetType.POWERUP -> error("Powerup targets do not use normal target scoring.")
    }
}
