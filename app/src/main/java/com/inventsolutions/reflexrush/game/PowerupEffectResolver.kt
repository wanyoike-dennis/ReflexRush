package com.inventsolutions.reflexrush.game

/**
 * Applies a collected powerup to an immutable game-state snapshot.
 * Session metrics and event buffering remain owned by GameEngine.
 */
class PowerupEffectResolver(
    private val config: GameConfig,
    private val difficultyForScore: (Int) -> DifficultyProfile
) {
    fun apply(state: GameState, powerup: PowerupType): PowerupEffectResult {
        val updatedState: GameState
        val events = mutableListOf<GameplayEvent>()

        events += GameplayEvent.PowerupCollected(powerup)
        when (powerup) {
            PowerupType.GOLDEN_STAR -> {
                val newScore = state.score + 50
                val previousLevel = state.difficultyLevel
                val newLevel = difficultyForScore(newScore).level
                updatedState = state.copy(score = newScore, difficultyLevel = newLevel)
                events += GameplayEvent.PowerupActivated(powerup)
                if (newLevel != previousLevel) {
                    events += GameplayEvent.LevelChanged(previousLevel, newLevel)
                }
            }
            PowerupType.SHIELD -> {
                updatedState = state.copy(shieldAvailable = true)
                events += GameplayEvent.PowerupActivated(powerup)
            }
            PowerupType.DOUBLE_POINTS, PowerupType.SLOW_TIME -> {
                updatedState = state.copy(
                    activeTimedPowerup = powerup,
                    powerupRemainingMs = config.powerupDurationMs
                )
                events += GameplayEvent.PowerupActivated(powerup)
            }
        }
        return PowerupEffectResult(updatedState, events)
    }
}

data class PowerupEffectResult(
    val state: GameState,
    val events: List<GameplayEvent>
)
