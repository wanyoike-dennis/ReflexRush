package com.inventsolutions.reflexrush.game

/**
 * Tracks round-level statistics independently from target and scoring rules.
 * It records facts supplied by GameEngine and creates the final immutable
 * session summary when the round ends.
 */
class GameSessionTracker(private val mode: GameMode) {
    var elapsedTimeMs: Long = 0L
        private set
    var goodTargetsHit: Int = 0
        private set
    var dangerTargetsHit: Int = 0
        private set
    var targetsMissed: Int = 0
        private set
    var goodTargetsMissed: Int = 0
        private set
    var bestCombo: Int = 0
        private set
    var powerupsCollected: Int = 0
        private set

    fun reset() {
        elapsedTimeMs = 0L
        goodTargetsHit = 0
        dangerTargetsHit = 0
        targetsMissed = 0
        goodTargetsMissed = 0
        bestCombo = 0
        powerupsCollected = 0
    }

    fun recordElapsedTime(elapsedMs: Long) {
        require(elapsedMs >= 0L) { "Elapsed time cannot be negative." }
        elapsedTimeMs += elapsedMs
    }

    fun recordGoodTargetHit(comboCount: Int) {
        goodTargetsHit++
        bestCombo = maxOf(bestCombo, comboCount)
    }

    fun recordDangerTargetHit() {
        dangerTargetsHit++
    }

    fun recordTargetMissed(type: TargetType) {
        targetsMissed++
        if (type == TargetType.GOOD) goodTargetsMissed++
    }

    fun recordPowerupCollected() {
        powerupsCollected++
    }

    fun createSummary(state: GameState): GameSessionSummary = GameSessionSummary(
        mode = mode,
        finalScore = state.score,
        levelReached = state.difficultyLevel,
        livesRemaining = state.lives,
        goodTargetsHit = goodTargetsHit,
        dangerTargetsHit = dangerTargetsHit,
        targetsMissed = targetsMissed,
        goodTargetsMissed = goodTargetsMissed,
        bestCombo = bestCombo,
        durationMs = elapsedTimeMs,
        powerupsCollected = powerupsCollected
    )
}
