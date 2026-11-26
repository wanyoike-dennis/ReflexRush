package com.inventsolutions.reflexrush.feature.profile

import com.inventsolutions.reflexrush.core.ui.ArcadeBackdrop
import com.inventsolutions.reflexrush.core.ui.ArcadePanel
import com.inventsolutions.reflexrush.game.GameMode
import com.inventsolutions.reflexrush.game.PlayerProgression
import com.inventsolutions.reflexrush.game.PlayerStatistics
import com.inventsolutions.reflexrush.ui.theme.RushAmber
import com.inventsolutions.reflexrush.ui.theme.RushGreen
import com.inventsolutions.reflexrush.ui.theme.RushMint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlayerProfileScreen(
    statistics: PlayerStatistics,
    progression: PlayerProgression,
    bestScores: Map<GameMode, Int>,
    onStatistics: () -> Unit,
    onHighScores: () -> Unit,
    onAchievements: () -> Unit,
    onBack: () -> Unit
) {
    val width = LocalConfiguration.current.screenWidthDp
    val inset = ((width - 760) / 2).coerceAtLeast(22).dp
    val best = bestScores.values.maxOrNull() ?: 0
    Box(Modifier.fillMaxSize()) {
        ArcadeBackdrop(Modifier.fillMaxSize(), intensity = .48f)
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState())
                .padding(horizontal = inset, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ArcadeScreenHeader(
                eyebrow = "REFLEX RUSH",
                title = "PLAYER PROFILE",
                onBack = onBack
            )
            Spacer(Modifier.height(28.dp))
            Box(
                Modifier.size(104.dp).clip(CircleShape)
                    .background(Color(0xFF102B35))
                    .border(2.dp, RushGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) { Text("⚡", fontSize = 48.sp, color = RushMint) }
            Spacer(Modifier.height(12.dp))
            Text("REFLEX PLAYER", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
            Text("LEVEL ${progression.level}  •  SHARPSHOOTER", color = RushMint, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.3.sp)
            Spacer(Modifier.height(24.dp))
            ArcadePanel(Modifier.fillMaxWidth(), accent = RushGreen, radius = 22.dp) {
                Column(Modifier.fillMaxWidth().padding(22.dp)) {
                    Text("PLAYER AT A GLANCE", color = RushMint, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                    Spacer(Modifier.height(18.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        ProfileMetric("BEST SCORE", best.toString(), RushAmber)
                        ProfileMetric("GAMES PLAYED", statistics.totalGamesPlayed.toString(), RushMint)
                        ProfileMetric("ACCURACY", "${statistics.accuracyPercent}%", RushGreen)
                    }
                    Spacer(Modifier.height(20.dp))
                    Text("${progression.experiencePoints} TOTAL XP", color = RushAmber, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(8.dp))
                    val fraction = if (progression.xpForNextLevel > 0) (progression.xpIntoLevel.toFloat() / progression.xpForNextLevel).coerceIn(0f, 1f) else 0f
                    Box(Modifier.fillMaxWidth().height(9.dp).clip(CircleShape).background(Color.White.copy(alpha = .1f))) {
                        Box(Modifier.fillMaxWidth(fraction).height(9.dp).clip(CircleShape).background(RushGreen))
                    }
                    Spacer(Modifier.height(7.dp))
                    Text("${progression.xpIntoLevel} / ${progression.xpForNextLevel} XP TO NEXT LEVEL", color = Color.White.copy(alpha = .55f), fontSize = 9.sp)
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("YOUR PLAYER HUB", Modifier.fillMaxWidth(), color = RushMint, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.8.sp)
            Spacer(Modifier.height(12.dp))
            ProfileAction("▥", "STATISTICS", "Review your performance and gameplay totals", RushMint, onStatistics)
            Spacer(Modifier.height(10.dp))
            ProfileAction("🏆", "HIGH SCORES", "Your best records across every game mode", RushAmber, onHighScores)
            Spacer(Modifier.height(10.dp))
            ProfileAction("★", "ACHIEVEMENTS", "Daily missions, milestones and rewards", RushGreen, onAchievements)
            Spacer(Modifier.height(24.dp))
            Text("TRUST YOUR REFLEXES.", color = RushMint.copy(alpha = .42f), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        }
    }
}

@Composable
private fun ProfileMetric(label: String, value: String, tint: Color) {
    Column {
        Text(value, color = tint, fontSize = 20.sp, fontWeight = FontWeight.Black)
        Text(label, color = Color.White.copy(alpha = .48f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ProfileAction(icon: String, title: String, subtitle: String, tint: Color, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = 76.dp),
        shape = RoundedCornerShape(17.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, tint.copy(alpha = .5f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = tint),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 24.sp)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = tint, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Text(subtitle, color = Color.White.copy(alpha = .55f), fontSize = 10.sp)
            }
            Text("›", color = tint, fontSize = 24.sp)
        }
    }
}
