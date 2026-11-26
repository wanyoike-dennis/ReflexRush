package com.inventsolutions.reflexrush.feature.statistics

import com.inventsolutions.reflexrush.core.ui.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventsolutions.reflexrush.game.GameMode
import com.inventsolutions.reflexrush.game.ModeStatistics
import com.inventsolutions.reflexrush.game.PlayerStatistics
import com.inventsolutions.reflexrush.ui.theme.RushAmber
import com.inventsolutions.reflexrush.ui.theme.RushGreen
import com.inventsolutions.reflexrush.ui.theme.RushMint
import com.inventsolutions.reflexrush.ui.theme.RushRed

@Composable
fun PlayerStatisticsScreen(statistics: PlayerStatistics, onBack: () -> Unit) {
    val width = LocalConfiguration.current.screenWidthDp
    val inset = ((width - 900) / 2).coerceAtLeast(20).dp
    Box(Modifier.fillMaxSize()) {
        ArcadeBackdrop(Modifier.fillMaxSize(), RushMint, intensity = .38f)
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState())
                .padding(horizontal = inset, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ArcadeScreenHeader("PLAYER PROFILE", "YOUR STATISTICS", onBack)
            Spacer(Modifier.height(18.dp))
            Text("LIFETIME OVERVIEW", Modifier.fillMaxWidth(), color = RushMint, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.7.sp)
            Spacer(Modifier.height(10.dp))
            ArcadePanel(Modifier.fillMaxWidth(), RushMint, 21.dp) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        StatHighlight("GAMES", statistics.totalGamesPlayed.toString(), RushMint, Modifier.weight(1f))
                        StatHighlight("ACCURACY", "${statistics.accuracyPercent}%", RushGreen, Modifier.weight(1f))
                        StatHighlight("BEST SCORE", statistics.highestScore.toString(), RushAmber, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(15.dp))
                    StatsRow("Total play time", formatPlayTime(statistics.totalPlayTimeMs), RushMint)
                    StatsRow("Highest level", statistics.highestLevel.toString(), RushMint)
                    StatsRow("Best combo", "${statistics.bestCombo}×", RushAmber)
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("GAMEPLAY TOTALS", Modifier.fillMaxWidth(), color = RushAmber, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.7.sp)
            Spacer(Modifier.height(10.dp))
            ArcadePanel(Modifier.fillMaxWidth(), RushAmber, 21.dp) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
                    StatsRow("Good targets hit", statistics.totalGoodTargetsHit.toString(), RushGreen)
                    StatsRow("Danger targets hit", statistics.totalDangerTargetsHit.toString(), RushRed)
                    StatsRow("Targets missed", statistics.totalTargetsMissed.toString(), RushRed)
                    StatsRow("Powerups collected", statistics.totalPowerupsCollected.toString(), RushAmber)
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("RECORDS BY GAME MODE", Modifier.fillMaxWidth(), color = RushMint, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.7.sp)
            Spacer(Modifier.height(10.dp))
            GameMode.entries.forEach { mode ->
                ModeStatsCard(mode, statistics.forMode(mode))
                Spacer(Modifier.height(10.dp))
            }
            Text(
                "Accuracy = good hits ÷ (good hits + danger hits + missed targets). Powerups are tracked separately.",
                Modifier.fillMaxWidth(), color = Color.White.copy(alpha = .43f), fontSize = 9.sp, lineHeight = 14.sp
            )
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun StatHighlight(label: String, value: String, tint: Color, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(14.dp)).padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = tint, fontSize = 20.sp, fontWeight = FontWeight.Black)
        Text(label, color = Color.White.copy(alpha = .48f), fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = .3.sp)
    }
}

@Composable
private fun StatsRow(label: String, value: String, tint: Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color.White.copy(alpha = .68f), fontSize = 11.sp)
        Text(value, color = tint, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun ModeStatsCard(mode: GameMode, stats: ModeStatistics) {
    ArcadePanel(Modifier.fillMaxWidth(), RushGreen, 18.dp) {
        Column(Modifier.fillMaxWidth().padding(15.dp)) {
            Text(mode.name.replace('_', ' '), color = RushMint, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MiniStat("GAMES", stats.gamesPlayed.toString())
                MiniStat("BEST SCORE", stats.highestScore.toString())
                MiniStat("ACCURACY", "${stats.accuracyPercent}%")
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MiniStat("LEVEL", stats.highestLevel.toString())
                MiniStat("BEST COMBO", "${stats.bestCombo}×")
                MiniStat("POWERUPS", stats.powerupsCollected.toString())
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String) {
    Column {
        Text(label, color = Color.White.copy(alpha = .42f), fontSize = 7.sp, fontWeight = FontWeight.Bold, letterSpacing = .6.sp)
        Text(value, color = RushAmber, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
    }
}

private fun formatPlayTime(durationMs: Long): String {
    val totalSeconds = durationMs.coerceAtLeast(0L) / 1_000L
    val hours = totalSeconds / 3_600L
    val minutes = (totalSeconds % 3_600L) / 60L
    return if (hours > 0L) "${hours}h ${minutes}m" else "${minutes}m ${totalSeconds % 60L}s"
}
