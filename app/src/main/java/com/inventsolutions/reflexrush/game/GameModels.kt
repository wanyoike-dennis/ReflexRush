package com.inventsolutions.reflexrush.game

/**
 * Tunable rules for a Reflex Rush round.
 *
 * Positions are normalised to the play area: 0f is the left/top edge and 1f
 * is the right/bottom edge.
 */
data class GameConfig(
    val startingLives: Int = 3,
    val pointsPerGoodHit: Int = 10,
    val dangerHitPenalty: Int = 10,
    val targetLifetimeMs: Long = 1_400L,
    val spawnIntervalMs: Long = 750L,
    val maxActiveTargets: Int = 3,
    val dangerTargetChance: Float = 0.22f,
    val minTargetPosition: Float = 0.08f,
    val maxTargetPosition: Float = 0.92f,
    val powerupSpawnChance: Float = 0.06f,
    val powerupDurationMs: Long = 5_000L,
    val slowTimeFactor: Float = 0.5f
) {
    init {
        require(startingLives > 0) { "Starting lives must be greater than zero." }
        require(pointsPerGoodHit >= 0) { "Good-hit points cannot be negative." }
        require(dangerHitPenalty >= 0) { "Danger penalty cannot be negative." }
        require(targetLifetimeMs > 0L) { "Target lifetime must be greater than zero." }
        require(spawnIntervalMs > 0L) { "Spawn interval must be greater than zero." }
        require(maxActiveTargets > 0) { "At least one target must be allowed." }
        require(dangerTargetChance in 0f..1f) { "Danger chance must be between zero and one." }
        require(powerupSpawnChance in 0f..1f) { "Powerup chance must be between zero and one." }
        require(dangerTargetChance + powerupSpawnChance <= 1f) {
            "Danger and powerup chances cannot add up to more than one."
        }
        require(powerupDurationMs > 0L) { "Powerup duration must be greater than zero." }
        require(slowTimeFactor in 0.1f..1f) { "Slow-time factor must be between 0.1 and 1." }
        require(minTargetPosition >= 0f && maxTargetPosition <= 1f) {
            "Target positions must stay within the play area."
        }
        require(minTargetPosition < maxTargetPosition) {
            "Minimum target position must be less than maximum target position."
        }
    }
}

enum class GameStatus {
    READY,
    RUNNING,
    GAME_OVER
}

enum class TargetType {
    GOOD,
    DANGER,
    POWERUP
}

data class GameTarget(
    val id: Long,
    val type: TargetType,
    val x: Float,
    val y: Float,
    val remainingLifetimeMs: Long,
    val powerupType: PowerupType? = null
)

data class GameState(
    val status: GameStatus = GameStatus.READY,
    val score: Int = 0,
    val lives: Int = 3,
    val targets: List<GameTarget> = emptyList(),
    val difficultyLevel: Int = 1,
    val comboCount: Int = 0,
    val comboMultiplier: Int = 1,
    val bestCombo: Int = 0,
    val activeTimedPowerup: PowerupType? = null,
    val powerupRemainingMs: Long = 0L,
    val shieldAvailable: Boolean = false,
    val timeRemainingMs: Long? = null,
    val targetsResolved: Int = 0,
    val targetLimit: Int? = null
)

