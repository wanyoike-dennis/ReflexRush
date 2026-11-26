package com.inventsolutions.reflexrush.game

/**
 * Applies each mode's difficulty tuning on top of the shared score progression.
 * Classic, Time Attack, and Precision retain the shared profile unchanged.
 */
class ModeDifficultyResolver(
    private val mode: GameMode,
    private val config: GameConfig
) {
    fun forScore(score: Int): DifficultyProfile {
        val base = DifficultyProgression.forScore(score, config)
        return if (mode == GameMode.SURVIVAL) {
            base.copy(
                spawnIntervalMs = (base.spawnIntervalMs * SURVIVAL_SPAWN_FACTOR)
                    .toLong().coerceAtLeast(1L),
                targetLifetimeMs = (base.targetLifetimeMs * SURVIVAL_LIFETIME_FACTOR)
                    .toLong().coerceAtLeast(1L),
                dangerTargetChance = (base.dangerTargetChance + SURVIVAL_DANGER_INCREASE)
                    .coerceAtMost(SURVIVAL_MAX_DANGER_CHANCE)
            )
        } else {
            base
        }
    }

    private companion object {
        const val SURVIVAL_SPAWN_FACTOR = 0.82f
        const val SURVIVAL_LIFETIME_FACTOR = 0.9f
        const val SURVIVAL_DANGER_INCREASE = 0.04f
        const val SURVIVAL_MAX_DANGER_CHANCE = 0.5f
    }
}
