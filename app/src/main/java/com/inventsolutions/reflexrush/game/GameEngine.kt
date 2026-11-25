package com.inventsolutions.reflexrush.game

import kotlin.random.Random

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
    val maxTargetPosition: Float = 0.92f
) {
    init {
        require(startingLives > 0) { "Starting lives must be greater than zero." }
        require(pointsPerGoodHit >= 0) { "Good-hit points cannot be negative." }
        require(dangerHitPenalty >= 0) { "Danger penalty cannot be negative." }
        require(targetLifetimeMs > 0L) { "Target lifetime must be greater than zero." }
        require(spawnIntervalMs > 0L) { "Spawn interval must be greater than zero." }
        require(maxActiveTargets > 0) { "At least one target must be allowed." }
        require(dangerTargetChance in 0f..1f) { "Danger chance must be between zero and one." }
        require(minTargetPosition >= 0f && maxTargetPosition <= 1f) {
            "Target positions must stay within the play area."
        }
        require(minTargetPosition < maxTargetPosition) {
            "Minimum target position must be less than maximum target position."
        }
    }
}

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

enum class GameStatus {
    READY,
    RUNNING,
    GAME_OVER
}

enum class TargetType {
    GOOD,
    DANGER
}

data class GameTarget(
    val id: Long,
    val type: TargetType,
    val x: Float,
    val y: Float,
    val remainingLifetimeMs: Long
)

data class GameState(
    val status: GameStatus = GameStatus.READY,
    val score: Int = 0,
    val lives: Int = 3,
    val targets: List<GameTarget> = emptyList(),
    val difficultyLevel: Int = 1
)

/**
 * Pure Kotlin game rules. The UI is responsible for calling [advanceTime]
 * regularly while a round is running and forwarding target taps to [tapTarget].
 *
 * The injected Random instance makes target generation deterministic in tests.
 */
class GameEngine(
    val config: GameConfig = GameConfig(),
    private val random: Random = Random.Default
) {
    var state: GameState = GameState(lives = config.startingLives)
        private set

    private var nextTargetId = 1L
    private var timeSinceLastSpawnMs = 0L

    fun start(): GameState {
        nextTargetId = 1L
        timeSinceLastSpawnMs = 0L
        state = GameState(
            status = GameStatus.RUNNING,
            score = 0,
            lives = config.startingLives,
            targets = emptyList(),
            difficultyLevel = 1
        )
        return state
    }

    /**
     * Advances target lifetimes and the spawn clock by [elapsedMs].
     * Non-positive time changes and calls outside an active round are ignored.
     */
    fun advanceTime(elapsedMs: Long): GameState {
        if (state.status != GameStatus.RUNNING || elapsedMs <= 0L) {
            return state
        }

        val expiredTargets = state.targets.filter {
            it.remainingLifetimeMs <= elapsedMs
        }
        val remainingTargets = state.targets
            .filter { it.remainingLifetimeMs > elapsedMs }
            .map { it.copy(remainingLifetimeMs = it.remainingLifetimeMs - elapsedMs) }

        val missedGoodTargets = expiredTargets.count { it.type == TargetType.GOOD }
        val updatedLives = (state.lives - missedGoodTargets).coerceAtLeast(0)

        if (updatedLives == 0) {
            timeSinceLastSpawnMs = 0L
            state = state.copy(
                status = GameStatus.GAME_OVER,
                lives = 0,
                targets = emptyList()
            )
            return state
        }

        var updatedTargets = remainingTargets
        timeSinceLastSpawnMs += elapsedMs

        while (true) {
            val difficulty = DifficultyProgression.forScore(state.score, config)
            if (timeSinceLastSpawnMs < difficulty.spawnIntervalMs) break

            timeSinceLastSpawnMs -= difficulty.spawnIntervalMs
            if (updatedTargets.size < config.maxActiveTargets) {
                updatedTargets = updatedTargets + createTarget(difficulty)
            }
        }

        state = state.copy(
            lives = updatedLives,
            targets = updatedTargets
        )
        return state
    }

    /**
     * Removes a target before applying its effect, so the same target cannot
     * award points or apply a penalty more than once.
     */
    fun tapTarget(targetId: Long): GameState {
        if (state.status != GameStatus.RUNNING) {
            return state
        }

        val target = state.targets.firstOrNull { it.id == targetId } ?: return state
        val updatedScore = when (target.type) {
            TargetType.GOOD -> state.score + config.pointsPerGoodHit
            TargetType.DANGER -> (state.score - config.dangerHitPenalty).coerceAtLeast(0)
        }

        state = state.copy(
            score = updatedScore,
            targets = state.targets.filterNot { it.id == targetId },
            difficultyLevel = DifficultyProgression.forScore(updatedScore, config).level
        )
        return state
    }

    private fun createTarget(difficulty: DifficultyProfile): GameTarget {
        val type = if (random.nextFloat() < difficulty.dangerTargetChance) {
            TargetType.DANGER
        } else {
            TargetType.GOOD
        }
        val positionRange = config.minTargetPosition..config.maxTargetPosition

        return GameTarget(
            id = nextTargetId++,
            type = type,
            x = random.nextFloat() * (positionRange.endInclusive - positionRange.start) +
                positionRange.start,
            y = random.nextFloat() * (positionRange.endInclusive - positionRange.start) +
                positionRange.start,
            remainingLifetimeMs = difficulty.targetLifetimeMs
        )
    }
}
