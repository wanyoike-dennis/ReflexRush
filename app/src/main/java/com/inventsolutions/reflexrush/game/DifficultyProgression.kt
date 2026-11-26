package com.inventsolutions.reflexrush.game

data class DifficultyProfile(
    val level: Int,
    val spawnIntervalMs: Long,
    val targetLifetimeMs: Long,
    val dangerTargetChance: Float
)

/**
 * Five score-based difficulty levels. Each step tightens the timing and
 * increases danger-target frequency gradually, while preserving the base
 * GameConfig values for the first level.
 */
object DifficultyProgression {
    fun forScore(score: Int, config: GameConfig = GameConfig()): DifficultyProfile {
        require(score >= 0) { "Score cannot be negative." }

        val (level, spawnFactor, lifetimeFactor, dangerIncrease) = when {
            score < 100 -> ProgressionStep(1, 1.00, 1.00, 0.00f)
            score < 250 -> ProgressionStep(2, 0.92, 0.94, 0.03f)
            score < 450 -> ProgressionStep(3, 0.84, 0.88, 0.06f)
            score < 700 -> ProgressionStep(4, 0.76, 0.82, 0.09f)
            else -> ProgressionStep(5, 0.68, 0.76, 0.12f)
        }

        return DifficultyProfile(
            level = level,
            spawnIntervalMs = (config.spawnIntervalMs * spawnFactor).toLong().coerceAtLeast(1L),
            targetLifetimeMs = (config.targetLifetimeMs * lifetimeFactor).toLong().coerceAtLeast(1L),
            dangerTargetChance = (config.dangerTargetChance + dangerIncrease).coerceAtMost(0.45f)
        )
    }

    private data class ProgressionStep(
        val level: Int,
        val spawnFactor: Double,
        val lifetimeFactor: Double,
        val dangerIncrease: Float
    )
}

