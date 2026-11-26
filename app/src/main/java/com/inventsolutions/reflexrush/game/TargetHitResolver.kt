package com.inventsolutions.reflexrush.game

/**
 * Resolves a normal GOOD or DANGER target hit. Scoring, combo transitions,
 * session statistics and level-change events stay together and independent
 * from frame timing, spawning and Compose.
 */
class TargetHitResolver(
    private val sessionTracker: GameSessionTracker,
    private val targetScoring: TargetScoring,
    private val comboTransitionResolver: ComboTransitionResolver,
    private val modeDifficultyResolver: ModeDifficultyResolver
) {
    fun resolve(state: GameState, target: GameTarget): TargetHitResolution {
        require(target.type != TargetType.POWERUP) {
            "Powerup targets must be handled by the powerup resolver."
        }

        val previousLevel = state.difficultyLevel
        val combo = if (target.type == TargetType.GOOD) {
            comboTransitionResolver.afterGoodHit(state.comboCount, state.comboMultiplier)
        } else {
            comboTransitionResolver.breakCombo(state.comboCount, state.comboMultiplier)
        }
        val score = targetScoring.score(
            currentScore = state.score,
            targetType = target.type,
            comboMultiplier = combo.multiplier,
            doublePointsActive = state.activeTimedPowerup == PowerupType.DOUBLE_POINTS
        )

        val events = mutableListOf<GameplayEvent>()
        when (target.type) {
            TargetType.GOOD -> {
                sessionTracker.recordGoodTargetHit(combo.comboCount)
                events += GameplayEvent.GoodTargetHit(target.id, score.pointsDelta, score.newScore)
            }
            TargetType.DANGER -> {
                sessionTracker.recordDangerTargetHit()
                events += GameplayEvent.DangerTargetHit(target.id, -score.pointsDelta, score.newScore)
            }
            TargetType.POWERUP -> error("Powerup targets are handled before normal scoring.")
        }

        val newLevel = modeDifficultyResolver.forScore(score.newScore).level
        val updatedState = state.copy(
            score = score.newScore,
            targets = state.targets.filterNot { it.id == target.id },
            targetsResolved = state.targetsResolved + 1,
            difficultyLevel = newLevel,
            comboCount = combo.comboCount,
            comboMultiplier = combo.multiplier,
            bestCombo = sessionTracker.bestCombo
        )
        events += combo.events
        if (newLevel != previousLevel) {
            events += GameplayEvent.LevelChanged(previousLevel, newLevel)
        }
        return TargetHitResolution(updatedState, events)
    }
}

data class TargetHitResolution(
    val state: GameState,
    val events: List<GameplayEvent>
)
