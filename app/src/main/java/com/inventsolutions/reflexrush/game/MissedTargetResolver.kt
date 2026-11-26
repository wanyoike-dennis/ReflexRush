package com.inventsolutions.reflexrush.game

/**
 * Applies mode-specific consequences for targets that expire during a frame.
 * It owns miss penalties, shield/life handling and combo breaks; the engine
 * remains responsible for updating session counters and publishing events.
 */
object MissedTargetResolver {
    fun resolve(
        initialState: GameState,
        expiredTargets: List<GameTarget>,
        mode: GameMode,
        precisionMissPenalty: Int,
        sessionTracker: GameSessionTracker,
        comboTransitionResolver: ComboTransitionResolver
    ): MissResolution {
        var state = initialState
        val events = mutableListOf<GameplayEvent>()
        var lives = state.lives

        expiredTargets.forEach { target ->
            sessionTracker.recordTargetMissed(target.type)
            events += GameplayEvent.TargetMissed(target.id, target.type)
            if (target.type == TargetType.GOOD) {
                val combo = comboTransitionResolver.breakCombo(
                    previousCount = state.comboCount,
                    previousMultiplier = state.comboMultiplier
                )
                if (combo.events.isNotEmpty()) {
                    state = state.copy(
                        comboCount = combo.comboCount,
                        comboMultiplier = combo.multiplier
                    )
                    events += combo.events
                }

                if (mode == GameMode.PRECISION) {
                    val penalty = minOf(state.score, precisionMissPenalty)
                    if (penalty > 0) {
                        state = state.copy(score = state.score - penalty)
                        events += GameplayEvent.PrecisionMissPenalty(penalty, state.score)
                    }
                } else if (state.shieldAvailable) {
                    state = state.copy(shieldAvailable = false)
                    events += GameplayEvent.ShieldConsumed
                } else {
                    lives = (lives - 1).coerceAtLeast(0)
                    events += GameplayEvent.LifeLost(lives)
                }
            }
        }

        state = state.copy(lives = lives)
        return MissResolution(state = state, events = events, gameOver = lives == 0)
    }
}

data class MissResolution(
    val state: GameState,
    val events: List<GameplayEvent>,
    val gameOver: Boolean
)
