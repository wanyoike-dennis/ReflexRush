package com.inventsolutions.reflexrush.game

import kotlin.random.Random

/**
 * Creates targets using the active session rules and injected randomness.
 *
 * This class deliberately has no engine state or event side effects. The
 * caller owns target IDs, spawn counts, and publishing the returned powerup
 * spawn event.
 */
class TargetFactory(
    private val config: GameConfig,
    private val sessionConfig: GameSessionConfig,
    private val random: Random = Random.Default
) {
    fun create(
        id: Long,
        difficulty: DifficultyProfile,
        activeTimedPowerup: PowerupType?
    ): TargetSpawnResult {
        val roll = random.nextFloat()
        val canSpawnTimedPowerup = activeTimedPowerup == null
        val availablePowerups = PowerupType.entries.filter { powerup ->
            powerup in sessionConfig.enabledPowerups &&
                (canSpawnTimedPowerup ||
                    powerup == PowerupType.GOLDEN_STAR ||
                    powerup == PowerupType.SHIELD)
        }
        val powerupChance = when {
            availablePowerups.isEmpty() -> 0f
            canSpawnTimedPowerup -> config.powerupSpawnChance
            else -> config.powerupSpawnChance * 0.5f
        }
        val type = when {
            roll < difficulty.dangerTargetChance -> TargetType.DANGER
            roll < difficulty.dangerTargetChance + powerupChance -> TargetType.POWERUP
            else -> TargetType.GOOD
        }
        val powerupType = if (type == TargetType.POWERUP) {
            availablePowerups[(random.nextFloat() * availablePowerups.size).toInt()
                .coerceIn(0, availablePowerups.lastIndex)]
        } else null
        val positionRange = config.minTargetPosition..config.maxTargetPosition

        val target = GameTarget(
            id = id,
            type = type,
            x = random.nextFloat() * (positionRange.endInclusive - positionRange.start) +
                positionRange.start,
            y = random.nextFloat() * (positionRange.endInclusive - positionRange.start) +
                positionRange.start,
            remainingLifetimeMs = difficulty.targetLifetimeMs,
            powerupType = powerupType
        )
        return TargetSpawnResult(target = target, spawnedPowerup = powerupType)
    }
}

/** Target data and the optional event fact produced by one spawn operation. */
data class TargetSpawnResult(
    val target: GameTarget,
    val spawnedPowerup: PowerupType?
)
