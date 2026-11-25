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

/**
 * Pure Kotlin game rules. The UI advances the engine while a round is active
 * and forwards target taps. Events are buffered until [drainEvents] is called.
 *
 * The injected Random instance makes target generation deterministic in tests.
 */
class GameEngine(
    val sessionConfig: GameSessionConfig = GameSessionConfig(),
    private val random: Random = Random.Default
) {
    constructor(
        config: GameConfig,
        random: Random = Random.Default
    ) : this(
        sessionConfig = GameSessionConfig(gameConfig = config),
        random = random
    )

    val config: GameConfig
        get() = sessionConfig.gameConfig

    var state: GameState = GameState(lives = config.startingLives)
        private set

    var completedSessionSummary: GameSessionSummary? = null
        private set

    private val pendingEvents = mutableListOf<GameplayEvent>()
    private var nextTargetId = 1L
    private var timeSinceLastSpawnMs = 0L
    private var elapsedSessionMs = 0L
    private var goodTargetsHit = 0
    private var dangerTargetsHit = 0
    private var targetsMissed = 0
    private var goodTargetsMissed = 0
    private var bestCombo = 0
    private var targetsCreated = 0
    private var targetsResolved = 0

    init {
        // All supported modes share this engine with mode-specific rules.
    }

    /**
     * Returns and clears events generated since the previous drain.
     * Consumers should drain after each engine action they want to observe.
     */
    fun drainEvents(): List<GameplayEvent> {
        val events = pendingEvents.toList()
        pendingEvents.clear()
        return events
    }

    fun start(): GameState {
        nextTargetId = 1L
        timeSinceLastSpawnMs = 0L
        elapsedSessionMs = 0L
        goodTargetsHit = 0
        dangerTargetsHit = 0
        targetsMissed = 0
        goodTargetsMissed = 0
        bestCombo = 0
        targetsCreated = 0
        targetsResolved = 0
        completedSessionSummary = null
        pendingEvents.clear()
        state = GameState(
            status = GameStatus.RUNNING,
            score = 0,
            lives = config.startingLives,
            targets = emptyList(),
            difficultyLevel = 1,
            comboCount = 0,
            comboMultiplier = 1,
            bestCombo = 0,
            activeTimedPowerup = null,
            powerupRemainingMs = 0L,
            shieldAvailable = false,
            timeRemainingMs = if (sessionConfig.mode == GameMode.TIME_ATTACK) {
                sessionConfig.timeAttackDurationMs
            } else null,
            targetsResolved = 0,
            targetLimit = if (sessionConfig.mode == GameMode.PRECISION) {
                sessionConfig.precisionTargetCount
            } else null
        )
        pendingEvents += GameplayEvent.RoundStarted(sessionConfig.mode)
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

        val roundElapsedMs = if (sessionConfig.mode == GameMode.TIME_ATTACK) {
            minOf(elapsedMs, state.timeRemainingMs ?: sessionConfig.timeAttackDurationMs)
        } else elapsedMs
        elapsedSessionMs += roundElapsedMs
        val nextTimeRemaining = state.timeRemainingMs?.let { (it - roundElapsedMs).coerceAtLeast(0L) }

        val slowTimeActive = state.activeTimedPowerup == PowerupType.SLOW_TIME
        val gameplayElapsedMs = if (slowTimeActive) {
            (roundElapsedMs * config.slowTimeFactor).toLong().coerceAtLeast(1L)
        } else roundElapsedMs

        val expiredTargets = state.targets.filter {
            it.remainingLifetimeMs <= gameplayElapsedMs
        }
        val remainingTargets = state.targets
            .filter { it.remainingLifetimeMs > gameplayElapsedMs }
            .map { it.copy(remainingLifetimeMs = it.remainingLifetimeMs - gameplayElapsedMs) }

        if (state.activeTimedPowerup != null) {
            val remainingEffectMs = state.powerupRemainingMs - roundElapsedMs
            if (remainingEffectMs <= 0L) {
                val expiredPowerup = requireNotNull(state.activeTimedPowerup)
                state = state.copy(activeTimedPowerup = null, powerupRemainingMs = 0L)
                pendingEvents += GameplayEvent.PowerupExpired(expiredPowerup)
            } else {
                state = state.copy(powerupRemainingMs = remainingEffectMs)
            }
        }

        var updatedLives = state.lives
        expiredTargets.forEach { target ->
            targetsMissed++
            targetsResolved++
            pendingEvents += GameplayEvent.TargetMissed(target.id, target.type)
            if (target.type == TargetType.GOOD) {
                goodTargetsMissed++
                breakCombo()
                if (sessionConfig.mode == GameMode.PRECISION) {
                    val penalty = minOf(state.score, sessionConfig.precisionMissPenalty)
                    if (penalty > 0) {
                        state = state.copy(score = state.score - penalty)
                        pendingEvents += GameplayEvent.PrecisionMissPenalty(penalty, state.score)
                    }
                } else if (state.shieldAvailable) {
                    state = state.copy(shieldAvailable = false)
                    pendingEvents += GameplayEvent.ShieldConsumed
                } else {
                    updatedLives = (updatedLives - 1).coerceAtLeast(0)
                    pendingEvents += GameplayEvent.LifeLost(updatedLives)
                }
            }
        }

        if (updatedLives == 0) {
            timeSinceLastSpawnMs = 0L
            state = state.copy(
                status = GameStatus.GAME_OVER,
                lives = 0,
                targets = emptyList(),
                timeRemainingMs = nextTimeRemaining,
                targetsResolved = targetsResolved
            )
            completeSession()
            return state
        }

        var updatedTargets = remainingTargets
        timeSinceLastSpawnMs += gameplayElapsedMs

        while (true) {
            val difficulty = difficultyForCurrentMode()
            if (timeSinceLastSpawnMs < difficulty.spawnIntervalMs) break

            timeSinceLastSpawnMs -= difficulty.spawnIntervalMs
            val precisionLimitReached = sessionConfig.mode == GameMode.PRECISION &&
                targetsCreated >= sessionConfig.precisionTargetCount
            if (!precisionLimitReached && updatedTargets.size < config.maxActiveTargets) {
                updatedTargets = updatedTargets + createTarget(difficulty)
            }
            if (precisionLimitReached) {
                timeSinceLastSpawnMs = 0L
                break
            }
        }

        state = state.copy(
            lives = updatedLives,
            targets = updatedTargets,
            timeRemainingMs = nextTimeRemaining,
            targetsResolved = targetsResolved
        )

        if (sessionConfig.mode == GameMode.TIME_ATTACK && nextTimeRemaining == 0L) {
            finishRound()
        } else if (
            sessionConfig.mode == GameMode.PRECISION &&
            targetsCreated >= sessionConfig.precisionTargetCount &&
            targetsResolved >= sessionConfig.precisionTargetCount &&
            updatedTargets.isEmpty()
        ) {
            finishRound()
        }
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
        val previousLevel = state.difficultyLevel
        val previousComboCount = state.comboCount
        val previousMultiplier = state.comboMultiplier
        if (target.type == TargetType.POWERUP) {
            targetsResolved++
            collectPowerup(target)
            state = state.copy(targetsResolved = targetsResolved)
            finishPrecisionIfComplete()
            return state
        }

        val nextComboCount = if (target.type == TargetType.GOOD) previousComboCount + 1 else 0
        val nextMultiplier = sessionConfig.comboRules.multiplierFor(nextComboCount)
        val timedMultiplier = if (state.activeTimedPowerup == PowerupType.DOUBLE_POINTS) 2 else 1
        val scoreChange = when (target.type) {
            TargetType.GOOD -> sessionConfig.scoreRules.awardGoodHit(state.score, nextMultiplier * timedMultiplier)
            TargetType.DANGER -> sessionConfig.scoreRules.applyDangerPenalty(state.score)
            TargetType.POWERUP -> error("Powerup targets are handled before normal scoring.")
        }

        when (target.type) {
            TargetType.GOOD -> {
                goodTargetsHit++
                bestCombo = maxOf(bestCombo, nextComboCount)
                pendingEvents += GameplayEvent.GoodTargetHit(
                    targetId = target.id,
                    pointsAwarded = scoreChange.pointsDelta,
                    totalScore = scoreChange.newScore
                )
            }
            TargetType.DANGER -> {
                dangerTargetsHit++
                pendingEvents += GameplayEvent.DangerTargetHit(
                    targetId = target.id,
                    pointsDeducted = -scoreChange.pointsDelta,
                    totalScore = scoreChange.newScore
                )
            }
            TargetType.POWERUP -> error("Powerup targets are handled before normal scoring.")
        }

        targetsResolved++
        val newLevel = difficultyForCurrentMode(scoreChange.newScore).level
        state = state.copy(
            score = scoreChange.newScore,
            targets = state.targets.filterNot { it.id == targetId },
            targetsResolved = targetsResolved,
            difficultyLevel = newLevel,
            comboCount = nextComboCount,
            comboMultiplier = nextMultiplier,
            bestCombo = bestCombo
        )

        if (nextComboCount != previousComboCount) {
            pendingEvents += GameplayEvent.ComboChanged(
                previousCount = previousComboCount,
                newCount = nextComboCount,
                multiplier = nextMultiplier
            )
        }
        if (nextMultiplier > previousMultiplier) {
            pendingEvents += GameplayEvent.ComboMilestone(nextComboCount, nextMultiplier)
        }
        if (newLevel != previousLevel) {
            pendingEvents += GameplayEvent.LevelChanged(previousLevel, newLevel)
        }
        finishPrecisionIfComplete()
        return state
    }

    private fun collectPowerup(target: GameTarget) {
        val powerup = target.powerupType ?: return
        state = state.copy(targets = state.targets.filterNot { it.id == target.id })
        pendingEvents += GameplayEvent.PowerupCollected(powerup)
        when (powerup) {
            PowerupType.GOLDEN_STAR -> {
                val newScore = state.score + 50
                val previousLevel = state.difficultyLevel
                val newLevel = difficultyForCurrentMode(newScore).level
                state = state.copy(
                    score = newScore,
                    difficultyLevel = newLevel
                )
                pendingEvents += GameplayEvent.PowerupActivated(powerup)
                if (newLevel != previousLevel) {
                    pendingEvents += GameplayEvent.LevelChanged(previousLevel, newLevel)
                }
            }
            PowerupType.SHIELD -> {
                state = state.copy(shieldAvailable = true)
                pendingEvents += GameplayEvent.PowerupActivated(powerup)
            }
            PowerupType.DOUBLE_POINTS, PowerupType.SLOW_TIME -> {
                state = state.copy(
                    activeTimedPowerup = powerup,
                    powerupRemainingMs = config.powerupDurationMs
                )
                pendingEvents += GameplayEvent.PowerupActivated(powerup)
            }
        }
    }

    private fun completeSession() {
        if (completedSessionSummary != null) return

        val summary = GameSessionSummary(
            mode = sessionConfig.mode,
            finalScore = state.score,
            levelReached = state.difficultyLevel,
            livesRemaining = state.lives,
            goodTargetsHit = goodTargetsHit,
            dangerTargetsHit = dangerTargetsHit,
            targetsMissed = targetsMissed,
            goodTargetsMissed = goodTargetsMissed,
            bestCombo = bestCombo,
            durationMs = elapsedSessionMs
        )
        completedSessionSummary = summary
        pendingEvents += GameplayEvent.RoundCompleted(summary)
    }

    private fun finishPrecisionIfComplete() {
        if (
            sessionConfig.mode == GameMode.PRECISION &&
            targetsCreated >= sessionConfig.precisionTargetCount &&
            targetsResolved >= sessionConfig.precisionTargetCount &&
            state.targets.isEmpty()
        ) {
            finishRound()
        }
    }

    private fun finishRound() {
        if (state.status == GameStatus.GAME_OVER) return
        state = state.copy(status = GameStatus.GAME_OVER, targets = emptyList())
        completeSession()
    }

    private fun difficultyForCurrentMode(score: Int = state.score): DifficultyProfile {
        val base = DifficultyProgression.forScore(score, config)
        return if (sessionConfig.mode == GameMode.SURVIVAL) {
            base.copy(
                spawnIntervalMs = (base.spawnIntervalMs * 0.82f).toLong().coerceAtLeast(1L),
                targetLifetimeMs = (base.targetLifetimeMs * 0.9f).toLong().coerceAtLeast(1L),
                dangerTargetChance = (base.dangerTargetChance + 0.04f).coerceAtMost(0.5f)
            )
        } else base
    }

    private fun breakCombo() {
        if (state.comboCount == 0) return

        val previousCount = state.comboCount
        state = state.copy(comboCount = 0, comboMultiplier = 1)
        pendingEvents += GameplayEvent.ComboChanged(
            previousCount = previousCount,
            newCount = 0,
            multiplier = 1
        )
    }

    private fun createTarget(difficulty: DifficultyProfile): GameTarget {
        val roll = random.nextFloat()
        targetsCreated++
        val canSpawnTimedPowerup = state.activeTimedPowerup == null
        val availablePowerups = PowerupType.entries.filter { powerup ->
            powerup in sessionConfig.enabledPowerups &&
                (canSpawnTimedPowerup || powerup == PowerupType.GOLDEN_STAR || powerup == PowerupType.SHIELD)
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

        if (powerupType != null) {
            pendingEvents += GameplayEvent.PowerupSpawned(powerupType)
        }

        return GameTarget(
            id = nextTargetId++,
            type = type,
            x = random.nextFloat() * (positionRange.endInclusive - positionRange.start) +
                positionRange.start,
            y = random.nextFloat() * (positionRange.endInclusive - positionRange.start) +
                positionRange.start,
            remainingLifetimeMs = difficulty.targetLifetimeMs,
            powerupType = powerupType
        )
    }
}
