package com.inventsolutions.reflexrush.feature.game

import com.inventsolutions.reflexrush.core.ui.*

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.compose.runtime.withFrameNanos
import com.inventsolutions.reflexrush.game.GameMode
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

@Composable
fun GameScreen(
    onHome: () -> Unit,
    lifecycleOwner: LifecycleOwner,
    bestScore: Int,
    onNewBestScore: (Int) -> Unit,
    mode: GameMode = GameMode.CLASSIC,
    onSessionCompleted: (com.inventsolutions.reflexrush.game.GameSessionSummary) -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp
    val isTabletLayout = screenWidthDp >= 700
    val compactLandscape = screenWidthDp > screenHeightDp && screenHeightDp < 700
    val context = LocalContext.current
    val sessionRuntime = remember(context.applicationContext, mode) {
        GameSessionRuntime(context.applicationContext, mode)
    }
    val controller = sessionRuntime.controller
    var actionFeedback by remember { mutableStateOf<String?>(null) }
    var actionFeedbackColor by remember { mutableStateOf(RushGreen) }
    var feedbackToken by remember { mutableStateOf(0) }
    val feedbackAlpha by animateFloatAsState(
        targetValue = if (actionFeedback == null) 0f else 1f,
        animationSpec = spring(stiffness = 500f),
        label = "actionFeedbackAlpha"
    )
    val initialGameState = remember(sessionRuntime) { sessionRuntime.start().action.state }
    var gameState by remember { mutableStateOf(initialGameState) }
    var newBestAchieved by remember { mutableStateOf(false) }
    var isResumed by remember {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }

    DisposableEffect(sessionRuntime) {
        onDispose { sessionRuntime.release() }
    }

    LaunchedEffect(gameState.status, isResumed) {
        sessionRuntime.setGameActive(gameState.status, isResumed)
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

    LaunchedEffect(gameState.status) {
        if (gameState.status == GameStatus.GAME_OVER) {
            controller.completedSessionSummary?.let(onSessionCompleted)
        }
    }

    LaunchedEffect(gameState.status, bestScore) {
        val summary = controller.completedSessionSummary
        if (gameState.status == GameStatus.GAME_OVER &&
            summary != null &&
            summary.finalScore > bestScore
        ) {
            onNewBestScore(summary.finalScore)
            sessionRuntime.playPersonalBest()
            newBestAchieved = true
        }
    }

    LaunchedEffect(gameState.status, isResumed) {
        if (gameState.status == GameStatus.RUNNING && isResumed) {
            sessionRuntime.resetLoop()
            while (isActive && gameState.status == GameStatus.RUNNING) {
                withFrameNanos { frameNanos ->
                    val frame = sessionRuntime.onFrame(frameNanos, gameState)
                    if (frame != null) {
                        frame.feedback?.let { feedback ->
                            actionFeedback = feedback.message
                            actionFeedbackColor = feedback.color
                            feedbackToken++
                        }
                        if (frame.update.shouldPublishState) {
                            gameState = frame.update.action.state
                        }
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ArcadeBackdrop(Modifier.fillMaxSize(), RushGreen)
        Column(
            modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(
                horizontal = if (isTabletLayout) 28.dp else 16.dp,
                vertical = if (compactLandscape) 6.dp else 12.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GameHeader(mode = mode, onHome = onHome)

            Spacer(Modifier.height(if (compactLandscape) 6.dp else 12.dp))

            GameStatsPanel(
                gameState = gameState,
                mode = mode,
                startingLives = controller.config.startingLives
            )

            Spacer(Modifier.height(if (compactLandscape) 5.dp else 10.dp))

            GameComboPanel(gameState)

            Spacer(Modifier.height(8.dp))

            GamePowerupPanel(gameState)

            Text(
                "TAP GREEN  •  AVOID RED  •  COLLECT GOLD",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            Spacer(Modifier.height(if (compactLandscape) 5.dp else 12.dp))

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF071322), Color(0xFF0B1B2D), Color(0xFF07111F))
                        )
                    )
                    .border(1.5.dp, RushGreen.copy(alpha = 0.48f), RoundedCornerShape(26.dp))
            ) {
                GameArenaBackground()
                val targetSize = if (isTabletLayout) 78.dp else 66.dp
                gameState.targets.forEach { target ->
                    key(target.id) {
                    GameTargetButton(
                        target = target,
                        modifier = Modifier.offset(
                            x = (maxWidth * target.x - targetSize / 2).coerceIn(0.dp, maxWidth - targetSize),
                            y = (maxHeight * target.y - targetSize / 2).coerceIn(0.dp, maxHeight - targetSize)
                        ),
                        onClick = {
                            val result = sessionRuntime.tapTarget(target.id)
                            result.feedback?.let { feedback ->
                                actionFeedback = feedback.message
                                actionFeedbackColor = feedback.color
                                feedbackToken++
                            }
                            gameState = result.action.state
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
                bestCombo = controller.completedSessionSummary?.bestCombo ?: 0,
                hits = controller.completedSessionSummary?.goodTargetsHit ?: 0,
                misses = controller.completedSessionSummary?.targetsMissed ?: 0,
                level = controller.completedSessionSummary?.levelReached ?: gameState.difficultyLevel,
                mode = mode,
                onPlayAgain = {
                    newBestAchieved = false
                    gameState = sessionRuntime.start().action.state
                },
                onHome = onHome
            )
        }
    }
}
