package com.inventsolutions.reflexrush.game

/**
 * Immutable facts emitted by the game engine. Presentation, audio, statistics,
 * and mission systems can react to these events without re-deriving game rules.
 */
sealed interface GameplayEvent {
    data class RoundStarted(val mode: GameMode) : GameplayEvent

    data class GoodTargetHit(
        val targetId: Long,
        val pointsAwarded: Int,
        val totalScore: Int
    ) : GameplayEvent

    data class DangerTargetHit(
        val targetId: Long,
        val pointsDeducted: Int,
        val totalScore: Int
    ) : GameplayEvent

    data class TargetMissed(
        val targetId: Long,
        val targetType: TargetType
    ) : GameplayEvent

    data class LifeLost(val livesRemaining: Int) : GameplayEvent

    data class ComboChanged(
        val previousCount: Int,
        val newCount: Int,
        val multiplier: Int
    ) : GameplayEvent

    data class ComboMilestone(
        val comboCount: Int,
        val multiplier: Int
    ) : GameplayEvent

    data class PowerupSpawned(val powerup: PowerupType) : GameplayEvent
    data class PowerupCollected(val powerup: PowerupType) : GameplayEvent
    data class PowerupActivated(val powerup: PowerupType) : GameplayEvent
    data class PowerupExpired(val powerup: PowerupType) : GameplayEvent
    data object ShieldConsumed : GameplayEvent

    data class LevelChanged(
        val previousLevel: Int,
        val newLevel: Int
    ) : GameplayEvent

    data class RoundCompleted(val summary: GameSessionSummary) : GameplayEvent
}
