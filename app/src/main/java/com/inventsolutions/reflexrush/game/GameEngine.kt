package com.inventsolutions.reflexrush.game

import kotlin.random.Random

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
    private val targetFactory = TargetFactory(config, sessionConfig, random)
    private val targetLifecycle = TargetLifecycle()
    private val sessionTracker = GameSessionTracker(sessionConfig.mode)
    private val targetScoring = TargetScoring(sessionConfig.scoreRules)
    private val comboTransitionResolver = ComboTransitionResolver(sessionConfig.comboRules)
    private val modeDifficultyResolver = ModeDifficultyResolver(sessionConfig.mode, config)
    private val powerupEffectResolver = PowerupEffectResolver(config, modeDifficultyResolver::forScore)
    private var nextTargetId = 1L
    private var timeSinceLastSpawnMs = 0L
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
        sessionTracker.reset()
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
        sessionTracker.recordElapsedTime(roundElapsedMs)
        val nextTimeRemaining = state.timeRemainingMs?.let { (it - roundElapsedMs).coerceAtLeast(0L) }

        val slowTimeActive = state.activeTimedPowerup == PowerupType.SLOW_TIME
        val gameplayElapsedMs = if (slowTimeActive) {
            (roundElapsedMs * config.slowTimeFactor).toLong().coerceAtLeast(1L)
        } else roundElapsedMs

        val lifecycleResult = targetLifecycle.advance(state.targets, gameplayElapsedMs)
        val expiredTargets = lifecycleResult.expiredTargets
        val remainingTargets = lifecycleResult.remainingTargets

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

        val missResolution = MissedTargetResolver.resolve(
            initialState = state,
            expiredTargets = expiredTargets,
            mode = sessionConfig.mode,
            precisionMissPenalty = sessionConfig.precisionMissPenalty,
            sessionTracker = sessionTracker,
            comboTransitionResolver = comboTransitionResolver
        )
        state = missResolution.state
        targetsResolved += expiredTargets.size
        pendingEvents += missResolution.events
        val updatedLives = state.lives

        if (missResolution.gameOver) {
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
            val difficulty = modeDifficultyResolver.forScore(state.score)
            if (timeSinceLastSpawnMs < difficulty.spawnIntervalMs) break

            timeSinceLastSpawnMs -= difficulty.spawnIntervalMs
            val precisionLimitReached = sessionConfig.mode == GameMode.PRECISION &&
                targetsCreated >= sessionConfig.precisionTargetCount
            if (!precisionLimitReached && updatedTargets.size < config.maxActiveTargets) {
                targetsCreated++
                val spawn = targetFactory.create(
                    id = nextTargetId++,
                    difficulty = difficulty,
                    activeTimedPowerup = state.activeTimedPowerup
                )
                updatedTargets = updatedTargets + spawn.target
                spawn.spawnedPowerup?.let { powerup ->
                    pendingEvents += GameplayEvent.PowerupSpawned(powerup)
                }
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

        if (target.type == TargetType.POWERUP) {
            targetsResolved++
            collectPowerup(target)
            state = state.copy(targetsResolved = targetsResolved)
            finishPrecisionIfComplete()
            return state
        }

        val resolution = TargetHitResolver(
            sessionTracker = sessionTracker,
            targetScoring = targetScoring,
            comboTransitionResolver = comboTransitionResolver,
            modeDifficultyResolver = modeDifficultyResolver
        ).resolve(state, target)
        state = resolution.state
        targetsResolved++
        pendingEvents += resolution.events
        finishPrecisionIfComplete()
        return state
    }

    private fun collectPowerup(target: GameTarget) {
        val powerup = target.powerupType ?: return
        state = state.copy(targets = state.targets.filterNot { it.id == target.id })
        sessionTracker.recordPowerupCollected()
        val result = powerupEffectResolver.apply(state, powerup)
        state = result.state
        pendingEvents += result.events
    }

    private fun completeSession() {
        if (completedSessionSummary != null) return

        val summary = sessionTracker.createSummary(state)
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

    private fun breakCombo() {
        val transition = comboTransitionResolver.breakCombo(
            previousCount = state.comboCount,
            previousMultiplier = state.comboMultiplier
        )
        if (transition.events.isEmpty()) return

        state = state.copy(
            comboCount = transition.comboCount,
            comboMultiplier = transition.multiplier
        )
        pendingEvents += transition.events
    }

}
