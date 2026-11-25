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

    fun awardGoodHit(currentScore: Int): ScoreChange {
        require(currentScore >= 0) { "Current score cannot be negative." }
        return ScoreChange(
            pointsDelta = pointsPerGoodHit,
            newScore = currentScore + pointsPerGoodHit
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
 * Immutable rules supplied when a round starts. Scoring defaults are derived
 * from GameConfig so existing Classic tuning remains the source of truth.
 */
data class GameSessionConfig(
    val mode: GameMode = GameMode.CLASSIC,
    val gameConfig: GameConfig = GameConfig(),
    val scoreRules: ScoreRules = ScoreRules(
        pointsPerGoodHit = gameConfig.pointsPerGoodHit,
        dangerHitPenalty = gameConfig.dangerHitPenalty
    )
)

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
    val durationMs: Long
)
