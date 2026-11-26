package com.inventsolutions.reflexrush

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
import com.inventsolutions.reflexrush.game.GameController
import com.inventsolutions.reflexrush.game.GameEngine
import com.inventsolutions.reflexrush.game.GameSessionConfig
import com.inventsolutions.reflexrush.game.GameLoop
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
    val controller = remember(mode) {
        GameController(GameEngine(GameSessionConfig.forMode(mode)))
    }
    val context = LocalContext.current
    val audioCoordinator = remember(context) { GameAudioCoordinator(context.applicationContext) }
    val feedbackCoordinator = remember(context) { GameFeedbackCoordinator(context.applicationContext) }
    val gameLoop = remember(controller) { GameLoop() }
    var actionFeedback by remember { mutableStateOf<String?>(null) }
    var actionFeedbackColor by remember { mutableStateOf(RushGreen) }
    var feedbackToken by remember { mutableStateOf(0) }
    val feedbackAlpha by animateFloatAsState(
        targetValue = if (actionFeedback == null) 0f else 1f,
        animationSpec = spring(stiffness = 500f),
        label = "actionFeedbackAlpha"
    )
    val initialGameState = remember(controller) { controller.start().state }
    var gameState by remember { mutableStateOf(initialGameState) }
    var newBestAchieved by remember { mutableStateOf(false) }
    var isResumed by remember {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }

    DisposableEffect(audioCoordinator) {
        onDispose { audioCoordinator.release() }
    }

    LaunchedEffect(gameState.status, isResumed) {
        audioCoordinator.setGameActive(gameState.status, isResumed)
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
            audioCoordinator.playPersonalBest()
            newBestAchieved = true
        }
    }

    LaunchedEffect(gameState.status, isResumed) {
        if (gameState.status == GameStatus.RUNNING && isResumed) {
            gameLoop.reset()
            while (isActive && gameState.status == GameStatus.RUNNING) {
                withFrameNanos { frameNanos ->
                    val update = gameLoop.onFrame(frameNanos, gameState, controller)
                    if (update != null) {
                        audioCoordinator.playEvents(update.action.events)
                        feedbackCoordinator.handle(update.action.events)?.let { feedback ->
                            actionFeedback = feedback.message
                            actionFeedbackColor = feedback.color
                            feedbackToken++
                        }
                        if (update.shouldPublishState) {
                            gameState = update.action.state
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "REFLEX RUSH  /  ARENA",
                        color = RushMint,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        when (mode) {
                            GameMode.CLASSIC -> "CLASSIC • BUILD YOUR BEST SCORE"
                            GameMode.TIME_ATTACK -> "TIME ATTACK • 60 SECONDS"
                            GameMode.SURVIVAL -> "SURVIVAL • THE PACE KEEPS RISING"
                            GameMode.PRECISION -> "PRECISION • 30 TARGET CHALLENGE"
                        },
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.48f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }
                OutlinedButton(
                    onClick = onHome,
                    border = BorderStroke(1.dp, RushMint.copy(alpha = 0.55f)),
                    shape = RoundedCornerShape(11.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RushMint)
                ) {
                    Text("EXIT", fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }
            }

            Spacer(Modifier.height(if (compactLandscape) 6.dp else 12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xCC0A1728))
                    .border(1.dp, RushGreen.copy(alpha = 0.42f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GameStat(label = "SCORE", value = gameState.score.toString(), tint = RushAmber)
                when (mode) {
                    GameMode.TIME_ATTACK -> {
                        val remaining = gameState.timeRemainingMs ?: 0L
                        val seconds = (remaining + 999L) / 1_000L
                        GameStat(
                            label = "TIME LEFT",
                            value = "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}",
                            tint = if (remaining <= 10_000L) RushRed else RushAmber
                        )
                    }
                    GameMode.PRECISION -> GameStat(
                        label = "TARGETS",
                        value = "${gameState.targetsResolved}/${gameState.targetLimit ?: 30}",
                        tint = RushMint
                    )
                    GameMode.CLASSIC, GameMode.SURVIVAL -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "LIVES  ${gameState.lives}/${controller.config.startingLives}",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.4.sp
                        )
                        Spacer(Modifier.height(5.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            repeat(gameState.lives) {
                                Text("♥", color = RushRed, fontSize = 18.sp)
                            }
                        }
                    }
                }
                GameStat(label = "LEVEL", value = gameState.difficultyLevel.toString(), tint = RushGreen)
            }

            Spacer(Modifier.height(if (compactLandscape) 5.dp else 10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (gameState.comboCount >= 3) Color(0xFF30291B) else Color(0xCC0A1728))
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
                            val action = controller.tapTarget(target.id)
                            audioCoordinator.playEvents(action.events)
                            feedbackCoordinator.handle(action.events)?.let { feedback ->
                                actionFeedback = feedback.message
                                actionFeedbackColor = feedback.color
                                feedbackToken++
                            }
                            gameState = action.state
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
                    gameState = controller.start().state
                },
                onHome = onHome
            )
        }
    }
}
