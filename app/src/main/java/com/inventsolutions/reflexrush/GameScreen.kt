package com.inventsolutions.reflexrush

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.compose.runtime.withFrameNanos
import com.inventsolutions.reflexrush.game.GameEngine
import com.inventsolutions.reflexrush.game.GameplayEvent
import com.inventsolutions.reflexrush.game.GameStatus
import com.inventsolutions.reflexrush.game.GameTarget
import com.inventsolutions.reflexrush.game.PowerupType
import com.inventsolutions.reflexrush.game.TargetType
import com.inventsolutions.reflexrush.ui.theme.RushAmber
import com.inventsolutions.reflexrush.ui.theme.RushBackground
import com.inventsolutions.reflexrush.ui.theme.RushGreen
import com.inventsolutions.reflexrush.ui.theme.RushMint
import com.inventsolutions.reflexrush.ui.theme.RushRed
import com.inventsolutions.reflexrush.ui.theme.RushSurface
import com.inventsolutions.reflexrush.ui.theme.RushSurfaceVariant
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Uses the device vibrator directly so gameplay feedback is not dependent on
 * the Compose view's haptic-feedback setting. The permission is declared in
 * the app manifest; devices without a vibrator safely skip this effect.
 */
@Suppress("DEPRECATION")
private fun performGameVibration(context: Context, missedLife: Boolean = false) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
    if (!vibrator.hasVibrator()) return

    val timings = if (missedLife) longArrayOf(0L, 70L, 55L, 100L) else longArrayOf(0L, 45L)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createWaveform(timings, -1))
    } else {
        vibrator.vibrate(timings, -1)
    }
}

@Composable
fun GameScreen(
    onHome: () -> Unit,
    lifecycleOwner: LifecycleOwner,
    bestScore: Int,
    onNewBestScore: (Int) -> Unit
) {
    val engine = remember { GameEngine() }
    val context = LocalContext.current
    var actionFeedback by remember { mutableStateOf<String?>(null) }
    var actionFeedbackColor by remember { mutableStateOf(RushGreen) }
    var feedbackToken by remember { mutableStateOf(0) }
    val feedbackAlpha by animateFloatAsState(
        targetValue = if (actionFeedback == null) 0f else 1f,
        animationSpec = spring(stiffness = 500f),
        label = "actionFeedbackAlpha"
    )
    val initialGameState = remember(engine) { engine.start().also { engine.drainEvents() } }
    var gameState by remember { mutableStateOf(initialGameState) }
    var newBestAchieved by remember { mutableStateOf(false) }
    var isResumed by remember {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> isResumed = true
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> isResumed = false
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(feedbackToken) {
        if (actionFeedback != null) {
            delay(650L)
            actionFeedback = null
        }
    }

    LaunchedEffect(gameState.status, bestScore) {
        val summary = engine.completedSessionSummary
        if (gameState.status == GameStatus.GAME_OVER &&
            summary != null &&
            summary.finalScore > bestScore
        ) {
            onNewBestScore(summary.finalScore)
            newBestAchieved = true
        }
    }

    LaunchedEffect(gameState.status, isResumed) {
        if (gameState.status == GameStatus.RUNNING && isResumed) {
            var previousFrameNanos = 0L
            while (isActive && gameState.status == GameStatus.RUNNING) {
                withFrameNanos { frameNanos ->
                    if (previousFrameNanos != 0L) {
                        val elapsedMs = ((frameNanos - previousFrameNanos) / 1_000_000L)
                            .coerceAtLeast(1L)
                        val nextState = engine.advanceTime(elapsedMs)
                        val events = engine.drainEvents()
                        val livesLost = events.count { it is GameplayEvent.LifeLost }
                        if (livesLost > 0) {
                            actionFeedback = if (livesLost == 1) "−1 LIFE" else "−$livesLost LIVES"
                            actionFeedbackColor = RushRed
                            feedbackToken++
                            performGameVibration(context, missedLife = true)
                        }
                        gameState = nextState
                    }
                    previousFrameNanos = frameNanos
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(RushBackground, Color(0xFF101B2C), RushBackground)
                )
            )
            .safeDrawingPadding()
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "REFLEX RUSH",
                        color = RushMint,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        "SCORE UP. THE CHALLENGE RISES.",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.48f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }
                OutlinedButton(
                    onClick = onHome,
                    border = BorderStroke(1.dp, RushSurfaceVariant),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RushMint)
                ) {
                    Text("HOME", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(22.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(RushSurface)
                    .border(1.dp, RushSurfaceVariant, RoundedCornerShape(18.dp))
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GameStat(label = "SCORE", value = gameState.score.toString(), tint = RushAmber)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "LIVES",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.48f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp
                    )
                    Spacer(Modifier.height(5.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        repeat(engine.config.startingLives) { index ->
                            Text(
                                "♥",
                                color = if (index < gameState.lives) RushRed else RushSurfaceVariant,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
                GameStat(label = "LEVEL", value = gameState.difficultyLevel.toString(), tint = RushGreen)
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (gameState.comboCount >= 3) Color(0xFF30291B) else RushSurface.copy(alpha = 0.72f))
                    .border(
                        1.dp,
                        if (gameState.comboCount >= 3) RushAmber.copy(alpha = 0.72f) else RushSurfaceVariant,
                        RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "COMBO STREAK",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.52f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.3.sp
                    )
                    Text(
                        "${gameState.comboCount} HITS",
                        color = if (gameState.comboCount >= 3) RushAmber else RushMint,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "MULTIPLIER",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.52f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.3.sp
                    )
                    Text(
                        "${gameState.comboMultiplier}×",
                        color = if (gameState.comboMultiplier > 1) RushAmber else RushMint,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (gameState.activeTimedPowerup != null || gameState.shieldAvailable) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2B281D))
                        .border(1.dp, RushAmber.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        gameState.activeTimedPowerup?.let {
                            val seconds = (gameState.powerupRemainingMs + 999L) / 1_000L
                            when (it) {
                                PowerupType.DOUBLE_POINTS -> "2× POINTS  •  ${seconds}s"
                                PowerupType.SLOW_TIME -> "SLOW TIME  •  ${seconds}s"
                                else -> it.name
                            }
                        } ?: "POWERUP READY",
                        color = RushAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (gameState.shieldAvailable) {
                        Text("SHIELD  ●", color = RushMint, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            Text(
                "TAP GREEN  •  AVOID RED  •  COLLECT GOLD",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            Spacer(Modifier.height(12.dp))

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF142238), Color(0xFF0D1727))
                        )
                    )
                    .border(1.dp, RushSurfaceVariant, RoundedCornerShape(26.dp))
            ) {
                val targetSize = 66.dp
                gameState.targets.forEach { target ->
                    key(target.id) {
                    GameTargetButton(
                        target = target,
                        modifier = Modifier.offset(
                            x = (maxWidth * target.x - targetSize / 2).coerceIn(0.dp, maxWidth - targetSize),
                            y = (maxHeight * target.y - targetSize / 2).coerceIn(0.dp, maxHeight - targetSize)
                        ),
                        onClick = {
                            val nextState = engine.tapTarget(target.id)
                            val events = engine.drainEvents()
                            val hitEvent = events.firstOrNull {
                                it is GameplayEvent.GoodTargetHit ||
                                    it is GameplayEvent.DangerTargetHit ||
                                    it is GameplayEvent.PowerupCollected
                            }
                            when (hitEvent) {
                                is GameplayEvent.GoodTargetHit -> {
                                    actionFeedback = "+${hitEvent.pointsAwarded}"
                                    actionFeedbackColor = RushGreen
                                }
                                is GameplayEvent.DangerTargetHit -> {
                                    actionFeedback = "−${hitEvent.pointsDeducted}"
                                    actionFeedbackColor = RushRed
                                }
                                is GameplayEvent.PowerupCollected -> {
                                    actionFeedback = when (hitEvent.powerup) {
                                        PowerupType.GOLDEN_STAR -> "+50 BONUS"
                                        PowerupType.DOUBLE_POINTS -> "DOUBLE POINTS!"
                                        PowerupType.SLOW_TIME -> "SLOW TIME!"
                                        PowerupType.SHIELD -> "SHIELD READY!"
                                    }
                                    actionFeedbackColor = RushAmber
                                }
                                else -> Unit
                            }
                            if (hitEvent != null) {
                                feedbackToken++
                                performGameVibration(context)
                            }
                            gameState = nextState
                        }
                    )
                    }
                }

                if (actionFeedback != null) {
                    Text(
                        text = actionFeedback.orEmpty(),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 18.dp)
                            .graphicsLayer { alpha = feedbackAlpha; translationY = (1f - feedbackAlpha) * 10.dp.toPx() }
                            .clip(RoundedCornerShape(14.dp))
                            .background(RushSurface.copy(alpha = 0.94f))
                            .border(1.dp, actionFeedbackColor.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 18.dp, vertical = 9.dp),
                        color = actionFeedbackColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                }

                if (gameState.targets.isEmpty() && gameState.status == GameStatus.RUNNING) {
                    Text(
                        "GET READY…",
                        modifier = Modifier.align(Alignment.Center),
                        color = RushMint.copy(alpha = 0.35f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Text(
                "GOOD TARGET  +10     •     DANGER TARGET  −10",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.42f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Miss a green target and you lose one life.",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.38f),
                fontSize = 10.sp
            )
        }

        if (gameState.status == GameStatus.GAME_OVER) {
            GameOverOverlay(
                score = gameState.score,
                bestScore = bestScore,
                newBestAchieved = newBestAchieved,
                onPlayAgain = {
                    newBestAchieved = false
                    gameState = engine.start()
                    engine.drainEvents()
                },
                onHome = onHome
            )
        }
    }
}

@Composable
private fun GameStat(label: String, value: String, tint: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.48f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.4.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(value, color = tint, fontSize = 23.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun GameTargetButton(
    target: GameTarget,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDanger = target.type == TargetType.DANGER
    val isPowerup = target.type == TargetType.POWERUP
    val targetColor = when {
        isDanger -> RushRed
        isPowerup -> RushAmber
        else -> RushGreen
    }
    var hasAppeared by remember(target.id) { mutableStateOf(false) }
    LaunchedEffect(target.id) { hasAppeared = true }
    val targetScale by animateFloatAsState(
        targetValue = if (hasAppeared) 1f else 0.62f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 520f),
        label = "targetScale"
    )
    val targetAlpha by animateFloatAsState(
        targetValue = if (hasAppeared) 1f else 0f,
        animationSpec = spring(stiffness = 520f),
        label = "targetAlpha"
    )
    Box(
        modifier = modifier
            .graphicsLayer { scaleX = targetScale; scaleY = targetScale; alpha = targetAlpha }
            .size(66.dp)
            .shadow(14.dp, CircleShape, ambientColor = targetColor.copy(alpha = 0.55f))
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(targetColor.copy(alpha = 0.95f), targetColor.copy(alpha = 0.58f))
                )
            )
            .border(3.dp, Color.White.copy(alpha = 0.72f), CircleShape)
            .clickable(
                onClickLabel = when {
                    isDanger -> "Danger target. Avoid tapping."
                    isPowerup -> "Powerup target. Tap to collect."
                    else -> "Good target. Tap to score points."
                },
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            when {
                isDanger -> "!"
                isPowerup -> "★"
                else -> "＋"
            },
            color = if (isDanger) Color.White else Color(0xFF062116),
            fontSize = 29.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun GameOverOverlay(
    score: Int,
    bestScore: Int,
    newBestAchieved: Boolean,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit
) {
    var overlayVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(60L)
        overlayVisible = true
    }
    val overlayScale by animateFloatAsState(
        targetValue = if (overlayVisible) 1f else 0.68f,
        animationSpec = tween(durationMillis = 520),
        label = "gameOverScale"
    )
    val overlayAlpha by animateFloatAsState(
        targetValue = if (overlayVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 420),
        label = "gameOverAlpha"
    )
    val scrimAlpha by animateFloatAsState(
        targetValue = if (overlayVisible) 0.82f else 0f,
        animationSpec = tween(durationMillis = 360),
        label = "gameOverScrim"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050A12).copy(alpha = scrimAlpha)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = overlayScale
                    scaleY = overlayScale
                    alpha = overlayAlpha
                    translationY = (1f - overlayAlpha) * 72.dp.toPx()
                }
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            shape = RoundedCornerShape(28.dp),
            color = RushSurface,
            border = BorderStroke(1.5.dp, RushRed.copy(alpha = 0.72f)),
            shadowElevation = 28.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 26.dp, vertical = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "ROUND OVER",
                    color = RushRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.5.sp
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "Nice try!",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "YOUR SCORE",
                    color = RushMint.copy(alpha = 0.65f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.8.sp
                )
                Text(
                    score.toString(),
                    color = RushAmber,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Black
                )
                if (newBestAchieved) {
                    Text(
                        "NEW PERSONAL BEST!",
                        color = RushGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.4.sp
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "BEST SCORE  $bestScore",
                    color = RushMint.copy(alpha = 0.75f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onPlayAgain,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RushGreen,
                        contentColor = RushBackground
                    )
                ) {
                    Text("PLAY AGAIN", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.4.sp)
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onHome,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    border = BorderStroke(1.dp, RushSurfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("BACK TO HOME", color = RushMint, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
