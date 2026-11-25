package com.inventsolutions.reflexrush.game

/**
 * Modes share one engine. Only CLASSIC is playable in this foundation stage;
 * the other values reserve stable identities for later mode implementations.
 */
enum class GameMode {
    CLASSIC,
    TIME_ATTACK,
    SURVIVAL,
    PRECISION
}

/**
 * Central scoring policy for a session. Danger penalties are capped at the
 * current score so a round score can never become negative.
 */
data class ScoreRules(
    val pointsPerGoodHit: Int = 10,
    val dangerHitPenalty: Int = 10
) {
    init {
        require(pointsPerGoodHit >= 0) { "Good-hit points cannot be negative." }
        require(dangerHitPenalty >= 0) { "Danger penalty cannot be negative." }
    }

    fun awardGoodHit(currentScore: Int, multiplier: Int = 1): ScoreChange {
        require(currentScore >= 0) { "Current score cannot be negative." }
        require(multiplier >= 1) { "Score multiplier must be at least one." }
        val awardedPoints = pointsPerGoodHit * multiplier
        return ScoreChange(
            pointsDelta = awardedPoints,
            newScore = currentScore + awardedPoints
        )
    }

    fun applyDangerPenalty(currentScore: Int): ScoreChange {
        require(currentScore >= 0) { "Current score cannot be negative." }
        val appliedPenalty = minOf(currentScore, dangerHitPenalty)
        return ScoreChange(
            pointsDelta = -appliedPenalty,
            newScore = currentScore - appliedPenalty
        )
    }
}

data class ScoreChange(
    /** Positive for points awarded, negative for points deducted. */
    val pointsDelta: Int,
    val newScore: Int
)

/**
 * Combo thresholds are based on consecutive successful good-target hits.
 * A failed action resets the current streak, but never the session best.
 */
data class ComboRules(
    val doubleMultiplierAt: Int = 3,
    val tripleMultiplierAt: Int = 5,
    val fiveTimesMultiplierAt: Int = 10
) {
    init {
        require(doubleMultiplierAt >= 1)
        require(tripleMultiplierAt > doubleMultiplierAt)
        require(fiveTimesMultiplierAt > tripleMultiplierAt)
    }

    fun multiplierFor(comboCount: Int): Int {
        require(comboCount >= 0) { "Combo count cannot be negative." }
        return when {
            comboCount >= fiveTimesMultiplierAt -> 5
            comboCount >= tripleMultiplierAt -> 3
            comboCount >= doubleMultiplierAt -> 2
            else -> 1
        }
    }
}

/**
 * Immutable rules supplied when a round starts. Scoring defaults are derived
 * from GameConfig so existing Classic tuning remains the source of truth.
 */
data class GameSessionConfig(
    val mode: GameMode = GameMode.CLASSIC,
    val gameConfig: GameConfig = GameConfig(),
    val scoreRules: ScoreRules = ScoreRules(
        pointsPerGoodHit = gameConfig.pointsPerGoodHit,
        dangerHitPenalty = gameConfig.dangerHitPenalty
    ),
    val comboRules: ComboRules = ComboRules(),
    val enabledPowerups: Set<PowerupType> = PowerupType.entries.toSet(),
    val timeAttackDurationMs: Long = 60_000L,
    val precisionTargetCount: Int = 30,
    val precisionMissPenalty: Int = 5
) {
    init {
        require(timeAttackDurationMs > 0L) { "Time Attack duration must be greater than zero." }
        require(precisionTargetCount > 0) { "Precision target count must be greater than zero." }
        require(precisionMissPenalty >= 0) { "Precision miss penalty cannot be negative." }
    }

    companion object {
        fun forMode(mode: GameMode): GameSessionConfig = when (mode) {
            GameMode.CLASSIC -> GameSessionConfig()
            GameMode.TIME_ATTACK -> GameSessionConfig(
                mode = mode,
                enabledPowerups = setOf(
                    PowerupType.GOLDEN_STAR,
                    PowerupType.DOUBLE_POINTS,
                    PowerupType.SLOW_TIME
                )
            )
            GameMode.SURVIVAL -> GameSessionConfig(mode = mode)
            GameMode.PRECISION -> GameSessionConfig(
                mode = mode,
                enabledPowerups = setOf(PowerupType.GOLDEN_STAR)
            )
        }
    }
}

/**
 * A completed round snapshot. It contains only engine-owned facts and is safe
 * to persist or consume from statistics and mission systems later.
 */
data class GameSessionSummary(
    val mode: GameMode,
    val finalScore: Int,
    val levelReached: Int,
    val livesRemaining: Int,
    val goodTargetsHit: Int,
    val dangerTargetsHit: Int,
    val targetsMissed: Int,
    val goodTargetsMissed: Int,
    val durationMs: Long,
    val bestCombo: Int = 0
)
