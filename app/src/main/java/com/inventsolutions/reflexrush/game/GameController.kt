package com.inventsolutions.reflexrush.game

/** Immutable result of one command sent to the game engine. */
data class GameActionResult(
    val state: GameState,
    val events: List<GameplayEvent>
)

/** Coordinates engine commands while keeping game rules inside [GameEngine]. */
class GameController(private val engine: GameEngine) {
    val config: GameConfig
        get() = engine.config

    val completedSessionSummary: GameSessionSummary?
        get() = engine.completedSessionSummary

    var state: GameState = engine.state
        private set

    fun start(): GameActionResult {
        state = engine.start()
        return consumeResult()
    }

    fun advanceTime(elapsedMs: Long): GameActionResult {
        state = engine.advanceTime(elapsedMs)
        return consumeResult()
    }

    fun tapTarget(targetId: Long): GameActionResult {
        state = engine.tapTarget(targetId)
        return consumeResult()
    }

    private fun consumeResult() = GameActionResult(state, engine.drainEvents())
}
