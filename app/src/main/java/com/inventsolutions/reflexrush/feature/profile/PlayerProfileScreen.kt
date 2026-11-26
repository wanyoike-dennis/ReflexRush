package com.inventsolutions.reflexrush.feature.profile

import com.inventsolutions.reflexrush.core.ui.ArcadeBackdrop
import com.inventsolutions.reflexrush.core.ui.ArcadePanel
import com.inventsolutions.reflexrush.core.ui.ArcadeScreenHeader
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
    val inset = ((width - 760) / 2).coerceAtLeast(20).dp
    val best = bestScores.values.maxOrNull() ?: 0
    val progress = if (progression.xpForNextLevel > 0) (progression.xpIntoLevel.toFloat() / progression.xpForNextLevel).coerceIn(0f, 1f) else 0f

    Box(Modifier.fillMaxSize()) {
        ArcadeBackdrop(Modifier.fillMaxSize(), RushGreen, intensity = .55f)
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState())
                .padding(horizontal = inset, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ArcadeScreenHeader("REFLEX RUSH", "PLAYER PROFILE", onBack)
            Spacer(Modifier.height(22.dp))
            Box(
                Modifier.size(104.dp).clip(CircleShape)
                    .background(Color(0xFF102B35))
                    .border(2.dp, RushGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) { Text("ϟ", fontSize = 54.sp, color = RushMint, fontWeight = FontWeight.Black) }
            Spacer(Modifier.height(12.dp))
            Text("REFLEX PLAYER", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
            Text("LEVEL ${progression.level}  •  SHARPSHOOTER", color = RushMint, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.3.sp)
            Spacer(Modifier.height(22.dp))
            ArcadePanel(Modifier.fillMaxWidth(), RushGreen, 23.dp) {
                Column(Modifier.fillMaxWidth().padding(19.dp)) {
                    Text("PLAYER OVERVIEW", color = RushMint, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.8.sp)
                    Spacer(Modifier.height(17.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        ProfileMetric("BEST SCORE", best.toString(), RushAmber)
                        ProfileMetric("GAMES PLAYED", statistics.totalGamesPlayed.toString(), RushMint)
                        ProfileMetric("ACCURACY", "${statistics.accuracyPercent}%", RushGreen)
                    }
                    Spacer(Modifier.height(19.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("LEVEL ${progression.level}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("${progression.experiencePoints} TOTAL XP", color = RushAmber, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.height(9.dp))
                    Box(Modifier.fillMaxWidth().height(9.dp).clip(CircleShape).background(Color.White.copy(alpha = .10f))) {
                        Box(Modifier.fillMaxWidth(progress).height(9.dp).clip(CircleShape).background(RushGreen))
                    }
                    Spacer(Modifier.height(7.dp))
                    Text("${progression.xpIntoLevel} / ${progression.xpForNextLevel} XP TO NEXT LEVEL", color = Color.White.copy(alpha = .55f), fontSize = 9.sp)
                }
            }
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("PLAYER HUB", color = RushMint, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.8.sp)
                Spacer(Modifier.weight(1f))
                Text("YOUR GAME DATA", color = Color.White.copy(alpha = .38f), fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Spacer(Modifier.height(11.dp))
            ProfileAction("▥", "STATISTICS", "Performance, accuracy and gameplay totals", RushMint, onStatistics)
            Spacer(Modifier.height(10.dp))
            ProfileAction("♛", "HIGH SCORES", "Personal records across every game mode", RushAmber, onHighScores)
            Spacer(Modifier.height(10.dp))
            ProfileAction("★", "ACHIEVEMENTS", "Daily missions, milestones and rewards", RushGreen, onAchievements)
            Spacer(Modifier.height(22.dp))
            Text("TRUST YOUR REFLEXES.", color = RushMint.copy(alpha = .42f), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        }
    }
}

@Composable
private fun ProfileMetric(label: String, value: String, tint: Color) {
    Column {
        Text(value, color = tint, fontSize = 20.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(3.dp))
        Text(label, color = Color.White.copy(alpha = .48f), fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = .4.sp)
    }
}

@Composable
private fun ProfileAction(icon: String, title: String, subtitle: String, tint: Color, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = 76.dp),
        shape = RoundedCornerShape(17.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, tint.copy(alpha = .48f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = tint),
        contentPadding = PaddingValues(horizontal = 17.dp, vertical = 13.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(tint.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
                Text(icon, fontSize = 22.sp, color = tint)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = tint, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Text(subtitle, color = Color.White.copy(alpha = .58f), fontSize = 10.sp)
            }
            Text("›", color = tint, fontSize = 25.sp)
        }
    }
}
