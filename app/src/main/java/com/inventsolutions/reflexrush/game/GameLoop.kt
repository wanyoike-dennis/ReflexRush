package com.inventsolutions.reflexrush.game

data class GameLoopUpdate(
    val action: GameActionResult,
    val shouldPublishState: Boolean
)

/**
 * Pure frame-time coordinator. Compose supplies frame timestamps, while this
 * class controls simulation cadence and less-frequent UI state publication.
 */
class GameLoop(
    private val tickIntervalMs: Long = 33L,
    private val uiUpdateIntervalMs: Long = 100L
) {
    private var previousFrameNanos = 0L
    private var accumulatedFrameMs = 0L
    private var uiUpdateElapsedMs = 0L

    init {
        require(tickIntervalMs > 0L)
        require(uiUpdateIntervalMs > 0L)
    }

    fun reset() {
        previousFrameNanos = 0L
        accumulatedFrameMs = 0L
        uiUpdateElapsedMs = 0L
    }

    fun onFrame(
        frameNanos: Long,
        previousUiState: GameState,
        controller: GameController
    ): GameLoopUpdate? {
        if (previousFrameNanos != 0L) {
            accumulatedFrameMs +=
                ((frameNanos - previousFrameNanos) / NANOS_PER_MILLI).coerceAtLeast(1L)
        }
        previousFrameNanos = frameNanos

        if (accumulatedFrameMs < tickIntervalMs) return null

        val elapsedMs = accumulatedFrameMs
        accumulatedFrameMs = 0L
        val action = controller.advanceTime(elapsedMs)
        val nextState = action.state
        uiUpdateElapsedMs += elapsedMs

        val immediateUiChange =
            nextState.status != previousUiState.status ||
                targetIdsChanged(previousUiState.targets, nextState.targets) ||
                nextState.score != previousUiState.score ||
                nextState.lives != previousUiState.lives ||
                nextState.comboCount != previousUiState.comboCount ||
                nextState.comboMultiplier != previousUiState.comboMultiplier ||
                nextState.difficultyLevel != previousUiState.difficultyLevel ||
                nextState.activeTimedPowerup != previousUiState.activeTimedPowerup ||
                nextState.shieldAvailable != previousUiState.shieldAvailable ||
                nextState.timeRemainingMs?.div(1_000L) !=
                previousUiState.timeRemainingMs?.div(1_000L)

        val publish = immediateUiChange || uiUpdateElapsedMs >= uiUpdateIntervalMs
        if (publish) uiUpdateElapsedMs = 0L
        return GameLoopUpdate(action, publish)
    }

    private fun targetIdsChanged(previous: List<GameTarget>, current: List<GameTarget>): Boolean {
        if (previous.size != current.size) return true
        for (index in previous.indices) {
            if (previous[index].id != current[index].id) return true
        }
        return false
    }

    private companion object {
        const val NANOS_PER_MILLI = 1_000_000L
    }
}
