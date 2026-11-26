package com.inventsolutions.reflexrush

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.inventsolutions.reflexrush.game.GameMode

enum class AppDestination {
    HOME,
    PLAYER_PROFILE,
    HIGH_SCORES,
    MODE_SELECTION,
    GAME,
    STATISTICS,
    PROGRESSION,
    SETTINGS
}

/**
 * Holds top-level app navigation state.
 *
 * Game rules and screen-specific state remain owned by their existing layers.
 */
@Stable
class AppNavigationState {
    var destination by mutableStateOf(AppDestination.HOME)
        private set

    var selectedMode by mutableStateOf(GameMode.CLASSIC)
        private set

    fun navigateTo(destination: AppDestination) {
        this.destination = destination
    }

    fun startGame(mode: GameMode) {
        selectedMode = mode
        destination = AppDestination.GAME
    }

    fun goHome() {
        destination = AppDestination.HOME
    }
}
