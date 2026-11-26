package com.inventsolutions.reflexrush.core.feedback

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.ui.graphics.Color
import com.inventsolutions.reflexrush.game.GameplayEvent
import com.inventsolutions.reflexrush.game.PowerupType
import com.inventsolutions.reflexrush.ui.theme.RushAmber
import com.inventsolutions.reflexrush.ui.theme.RushGreen
import com.inventsolutions.reflexrush.ui.theme.RushRed

data class GameFeedback(val message: String, val color: Color)

/** Converts gameplay events into player-facing messages and vibration cues. */
class GameFeedbackCoordinator(context: Context) {
    private val appContext = context.applicationContext

    fun handle(events: List<GameplayEvent>): GameFeedback? {
        val livesLost = events.count { it is GameplayEvent.LifeLost }
        if (livesLost > 0) {
            vibrate(missedLife = true)
            return GameFeedback(
                if (livesLost == 1) "−1 LIFE" else "−$livesLost LIVES",
                RushRed
            )
        }

        val hit = events.firstOrNull {
            it is GameplayEvent.GoodTargetHit ||
                it is GameplayEvent.DangerTargetHit ||
                it is GameplayEvent.PowerupCollected
        } ?: return null

        vibrate(missedLife = false)
        return when (hit) {
            is GameplayEvent.GoodTargetHit -> GameFeedback("+${hit.pointsAwarded}", RushGreen)
            is GameplayEvent.DangerTargetHit -> GameFeedback("−${hit.pointsDeducted}", RushRed)
            is GameplayEvent.PowerupCollected -> GameFeedback(
                when (hit.powerup) {
                    PowerupType.GOLDEN_STAR -> "+50 BONUS"
                    PowerupType.DOUBLE_POINTS -> "DOUBLE POINTS!"
                    PowerupType.SLOW_TIME -> "SLOW TIME!"
                    PowerupType.SHIELD -> "SHIELD READY!"
                },
                RushAmber
            )
            else -> null
        }
    }

    @Suppress("DEPRECATION")
    private fun vibrate(missedLife: Boolean) {
        val vibrator = appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        if (!vibrator.hasVibrator()) return
        val timings = if (missedLife) longArrayOf(0L, 70L, 55L, 100L) else longArrayOf(0L, 45L)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(timings, -1))
        } else {
            vibrator.vibrate(timings, -1)
        }
    }
}
