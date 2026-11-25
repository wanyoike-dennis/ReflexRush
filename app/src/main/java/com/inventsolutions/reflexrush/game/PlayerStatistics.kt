package com.inventsolutions.reflexrush.game

/**
 * Lifetime statistics for one game mode. Accuracy is based on resolved
 * ordinary targets: good hits + danger hits + all expired targets.
 * Powerup collections are tracked separately and do not inflate accuracy.
 */
data class ModeStatistics(
    val gamesPlayed: Int = 0,
    val totalPlayTimeMs: Long = 0L,
    val goodTargetsHit: Int = 0,
    val dangerTargetsHit: Int = 0,
    val targetsMissed: Int = 0,
    val powerupsCollected: Int = 0,
    val highestScore: Int = 0,
    val highestLevel: Int = 1,
    val bestCombo: Int = 0
) {
    val resolvedTargets: Int
        get() = goodTargetsHit + dangerTargetsHit + targetsMissed

    val accuracyPercent: Int
        get() = if (resolvedTargets == 0) 0
        else ((goodTargetsHit.toDouble() / resolvedTargets) * 100.0).toInt()

    fun include(summary: GameSessionSummary): ModeStatistics = copy(
        gamesPlayed = gamesPlayed + 1,
        totalPlayTimeMs = totalPlayTimeMs + summary.durationMs.coerceAtLeast(0L),
        goodTargetsHit = goodTargetsHit + summary.goodTargetsHit,
        dangerTargetsHit = dangerTargetsHit + summary.dangerTargetsHit,
        targetsMissed = targetsMissed + summary.targetsMissed,
        powerupsCollected = powerupsCollected + summary.powerupsCollected,
        highestScore = maxOf(highestScore, summary.finalScore),
        highestLevel = maxOf(highestLevel, summary.levelReached),
        bestCombo = maxOf(bestCombo, summary.bestCombo)
    )
}

/** Lifetime totals plus a separate record for every game mode. */
data class PlayerStatistics(
    val modes: Map<GameMode, ModeStatistics> = GameMode.entries.associateWith { ModeStatistics() }
) {
    val totalGamesPlayed: Int get() = modes.values.sumOf { it.gamesPlayed }
    val totalPlayTimeMs: Long get() = modes.values.sumOf { it.totalPlayTimeMs }
    val totalGoodTargetsHit: Int get() = modes.values.sumOf { it.goodTargetsHit }
    val totalDangerTargetsHit: Int get() = modes.values.sumOf { it.dangerTargetsHit }
    val totalTargetsMissed: Int get() = modes.values.sumOf { it.targetsMissed }
    val totalPowerupsCollected: Int get() = modes.values.sumOf { it.powerupsCollected }
    val highestScore: Int get() = modes.values.maxOfOrNull { it.highestScore } ?: 0
    val highestLevel: Int get() = modes.values.maxOfOrNull { it.highestLevel } ?: 1
    val bestCombo: Int get() = modes.values.maxOfOrNull { it.bestCombo } ?: 0
    val accuracyPercent: Int
        get() {
            val resolved = totalGoodTargetsHit + totalDangerTargetsHit + totalTargetsMissed
            return if (resolved == 0) 0 else ((totalGoodTargetsHit.toDouble() / resolved) * 100.0).toInt()
        }

    fun include(summary: GameSessionSummary): PlayerStatistics {
        val updated = modes.toMutableMap()
        updated[summary.mode] = (updated[summary.mode] ?: ModeStatistics()).include(summary)
        return copy(modes = updated)
    }

    fun forMode(mode: GameMode): ModeStatistics = modes[mode] ?: ModeStatistics()
}
