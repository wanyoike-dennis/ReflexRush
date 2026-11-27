package com.inventsolutions.reflexrush.feature.game

import com.inventsolutions.reflexrush.core.ui.*

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventsolutions.reflexrush.game.GameMode
import com.inventsolutions.reflexrush.game.GameState
import com.inventsolutions.reflexrush.game.PowerupType
import com.inventsolutions.reflexrush.ui.theme.RushAmber
import com.inventsolutions.reflexrush.ui.theme.RushGreen
import com.inventsolutions.reflexrush.ui.theme.RushMint
import com.inventsolutions.reflexrush.ui.theme.RushRed
import com.inventsolutions.reflexrush.ui.theme.RushSurfaceVariant

@Composable
internal fun GameHeader(
    mode: GameMode,
    onPause: () -> Unit
) {
    val modeTitle = when (mode) {
        GameMode.CLASSIC -> "CLASSIC"
        GameMode.TIME_ATTACK -> "TIME ATTACK"
        GameMode.SURVIVAL -> "SURVIVAL"
        GameMode.PRECISION -> "PRECISION"
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "REFLEX RUSH  /  ARENA",
                color = RushMint,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.8.sp
            )
            Spacer(Modifier.height(3.dp))
            Text(
                modeTitle,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.1.sp
            )
        }
        Button(
            onClick = onPause,
            modifier = Modifier.height(54.dp),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RushGreen,
                contentColor = Color(0xFF06131C)
            )
        ) {
            Text("Ⅱ", fontSize = 22.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.width(8.dp))
            Text("PAUSE", fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
        }
    }
}

@Composable
internal fun GameStatsPanel(
    gameState: GameState,
    mode: GameMode,
    startingLives: Int
) {
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
                    "LIVES  ${gameState.lives}/$startingLives",
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
}

@Composable
internal fun GameComboPanel(gameState: GameState) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "COMBO STREAK",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.48f),
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
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.48f),
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
}

@Composable
internal fun GamePowerupPanel(gameState: GameState) {
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
}
