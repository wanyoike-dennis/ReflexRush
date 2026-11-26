package com.inventsolutions.reflexrush

import android.content.Context
import com.inventsolutions.reflexrush.game.GameplayEvent
import com.inventsolutions.reflexrush.game.GameStatus

/** Keeps gameplay audio lifecycle and event routing out of the screen. */
class GameAudioCoordinator(context: Context) {
    private val manager = GameAudioManager(context.applicationContext)

    fun setGameActive(status: GameStatus, isResumed: Boolean) {
        manager.setGameActive(status == GameStatus.RUNNING && isResumed)
    }

    fun playEvents(events: List<GameplayEvent>) = manager.playEvents(events)

    fun playPersonalBest() = manager.play(GameAudioManager.Cue.PERSONAL_BEST)

    fun release() = manager.release()
}
