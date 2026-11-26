package com.inventsolutions.reflexrush

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventsolutions.reflexrush.game.GameMode
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

@Composable
internal fun GameArenaBackground() {
    Canvas(Modifier.fillMaxSize()) {
        val step = 38.dp.toPx()
        var gridX = 0f
        while (gridX < size.width) {
            drawLine(
                Color(0x1427E6A1),
                androidx.compose.ui.geometry.Offset(gridX, 0f),
                androidx.compose.ui.geometry.Offset(gridX, size.height),
                1.dp.toPx()
            )
            gridX += step
        }
        var gridY = 0f
        while (gridY < size.height) {
            drawLine(
                Color(0x1427E6A1),
                androidx.compose.ui.geometry.Offset(0f, gridY),
                androidx.compose.ui.geometry.Offset(size.width, gridY),
                1.dp.toPx()
            )
            gridY += step
        }
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0x2227E6A1), Color.Transparent),
                center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f),
                radius = size.minDimension * 0.7f
            ),
            radius = size.minDimension * 0.7f,
            center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
        )
    }
}

@Composable
internal fun GameStat(label: String, value: String, tint: Color) {
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
internal fun GameTargetButton(
    target: GameTarget,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDanger = target.type == TargetType.DANGER
    val isPowerup = target.type == TargetType.POWERUP
    val targetColor = when {
        isDanger -> RushRed
        isPowerup -> when (target.powerupType) {
            PowerupType.GOLDEN_STAR -> RushAmber
            PowerupType.DOUBLE_POINTS -> Color(0xFF67B7FF)
            PowerupType.SLOW_TIME -> Color(0xFF9B9DFF)
            PowerupType.SHIELD -> RushMint
            null -> RushAmber
        }
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
                target.powerupType == PowerupType.GOLDEN_STAR -> "★"
                target.powerupType == PowerupType.DOUBLE_POINTS -> "2×"
                target.powerupType == PowerupType.SLOW_TIME -> "⌛"
                target.powerupType == PowerupType.SHIELD -> "◇"
                else -> "＋"
            },
            color = if (isDanger) Color.White else Color(0xFF062116),
            fontSize = if (target.powerupType == PowerupType.DOUBLE_POINTS) 22.sp else 29.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
internal fun GameOverOverlay(
    score: Int,
    bestScore: Int,
    newBestAchieved: Boolean,
    bestCombo: Int,
    hits: Int,
    misses: Int,
    level: Int,
    mode: GameMode,
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
            border = BorderStroke(1.5.dp, RushGreen.copy(alpha = 0.72f)),
            shadowElevation = 28.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 26.dp, vertical = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "RUN COMPLETE",
                    color = RushGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.5.sp
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "RUN IT BACK",
                    color = Color.White,
                    fontSize = 30.sp,
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
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0A1A2B)).padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("HITS", color = RushMint.copy(alpha = 0.62f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text(hits.toString(), color = RushGreen, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("BEST COMBO", color = RushMint.copy(alpha = 0.62f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text("${bestCombo}×", color = RushAmber, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("LEVEL", color = RushMint.copy(alpha = 0.62f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text(level.toString(), color = RushMint, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("${mode.name.replace('_', ' ')}  •  ${misses} MISSES", color = RushMint.copy(alpha = 0.62f), fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                Spacer(Modifier.height(18.dp))
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