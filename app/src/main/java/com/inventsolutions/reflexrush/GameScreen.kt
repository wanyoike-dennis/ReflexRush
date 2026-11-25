package com.inventsolutions.reflexrush

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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
import com.inventsolutions.reflexrush.game.GameStatus
import com.inventsolutions.reflexrush.game.GameTarget
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
    onNewBestScore: (Int) -> Unit
) {
    val engine = remember { GameEngine() }
    val hapticFeedback = LocalHapticFeedback.current
    var actionFeedback by remember { mutableStateOf<String?>(null) }
    var actionFeedbackColor by remember { mutableStateOf(RushGreen) }
    var feedbackToken by remember { mutableStateOf(0) }
    val feedbackAlpha by animateFloatAsState(
        targetValue = if (actionFeedback == null) 0f else 1f,
        animationSpec = spring(stiffness = 500f),
        label = "actionFeedbackAlpha"
    )
    var gameState by remember { mutableStateOf(engine.start()) }
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

    LaunchedEffect(gameState.status, gameState.score, bestScore) {
        if (gameState.status == GameStatus.GAME_OVER && gameState.score > bestScore) {
            onNewBestScore(gameState.score)
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
                        val previousLives = gameState.lives
                        val nextState = engine.advanceTime(elapsedMs)
                        if (nextState.lives < previousLives) {
                            val livesLost = previousLives - nextState.lives
                            actionFeedback = if (livesLost == 1) "−1 LIFE" else "−$livesLost LIVES"
                            actionFeedbackColor = RushRed
                            feedbackToken++
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
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
                        "STAY SHARP. MOVE FAST.",
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
                GameStat(label = "TARGETS", value = gameState.targets.size.toString(), tint = RushGreen)
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "TAP GREEN  •  AVOID RED",
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
                            actionFeedback = if (target.type == TargetType.GOOD) "+${engine.config.pointsPerGoodHit}" else "−${engine.config.dangerHitPenalty}"
                            actionFeedbackColor = if (target.type == TargetType.GOOD) RushGreen else RushRed
                            feedbackToken++
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            gameState = engine.tapTarget(target.id)
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
    val targetColor = if (isDanger) RushRed else RushGreen
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
                onClickLabel = if (isDanger) "Danger target. Avoid tapping." else "Good target. Tap to score points.",
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            if (isDanger) "!" else "＋",
            color = if (isDanger) Color.White else Color(0xFF062116),
            fontSize = if (isDanger) 31.sp else 29.sp,
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
    LaunchedEffect(Unit) { overlayVisible = true }
    val overlayScale by animateFloatAsState(
        targetValue = if (overlayVisible) 1f else 0.92f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 360f),
        label = "gameOverScale"
    )
    val overlayAlpha by animateFloatAsState(
        targetValue = if (overlayVisible) 1f else 0f,
        animationSpec = spring(stiffness = 360f),
        label = "gameOverAlpha"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC050A12)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .graphicsLayer { scaleX = overlayScale; scaleY = overlayScale; alpha = overlayAlpha }
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            shape = RoundedCornerShape(28.dp),
            color = RushSurface,
            border = BorderStroke(1.dp, RushSurfaceVariant),
            shadowElevation = 20.dp
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
