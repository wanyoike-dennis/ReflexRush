package com.inventsolutions.reflexrush

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventsolutions.reflexrush.game.GameMode
import com.inventsolutions.reflexrush.game.ModeStatistics
import com.inventsolutions.reflexrush.game.PlayerStatistics
import com.inventsolutions.reflexrush.ui.theme.RushAmber
import com.inventsolutions.reflexrush.ui.theme.RushBackground
import com.inventsolutions.reflexrush.ui.theme.RushGreen
import com.inventsolutions.reflexrush.ui.theme.RushMint
import com.inventsolutions.reflexrush.ui.theme.RushRed
import com.inventsolutions.reflexrush.ui.theme.RushSurface
import com.inventsolutions.reflexrush.ui.theme.RushSurfaceVariant

@Composable
fun PlayerStatisticsScreen(
    statistics: PlayerStatistics,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RushBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("PLAYER", color = RushMint, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text("STATISTICS", color = MaterialTheme.colorScheme.onBackground, fontSize = 27.sp, fontWeight = FontWeight.Black)
            }
            OutlinedButton(onClick = onBack) { Text("BACK") }
        }
        Spacer(Modifier.height(20.dp))
        Text("LIFETIME OVERVIEW", modifier = Modifier.fillMaxWidth(), color = RushAmber, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(10.dp))
        StatsPanel {
            StatsRow("Games played", statistics.totalGamesPlayed.toString(), RushMint)
            StatsRow("Total play time", formatPlayTime(statistics.totalPlayTimeMs), RushMint)
            StatsRow("Target accuracy", "${statistics.accuracyPercent}%", RushGreen)
            StatsRow("Highest score", statistics.highestScore.toString(), RushAmber)
            StatsRow("Highest level", statistics.highestLevel.toString(), RushMint)
            StatsRow("Best combo", "${statistics.bestCombo}×", RushAmber)
        }
        Spacer(Modifier.height(18.dp))
        Text("GAMEPLAY TOTALS", modifier = Modifier.fillMaxWidth(), color = RushAmber, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(10.dp))
        StatsPanel {
            StatsRow("Good targets hit", statistics.totalGoodTargetsHit.toString(), RushGreen)
            StatsRow("Danger targets hit", statistics.totalDangerTargetsHit.toString(), RushRed)
            StatsRow("Targets missed", statistics.totalTargetsMissed.toString(), RushRed)
            StatsRow("Powerups collected", statistics.totalPowerupsCollected.toString(), RushAmber)
        }
        Spacer(Modifier.height(18.dp))
        Text("MODE RECORDS", modifier = Modifier.fillMaxWidth(), color = RushAmber, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(10.dp))
        GameMode.entries.forEach { mode ->
            ModeStatsCard(mode, statistics.forMode(mode))
            Spacer(Modifier.height(9.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Accuracy = good hits ÷ (good hits + danger hits + missed targets). Powerups are tracked separately.",
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.48f),
            fontSize = 10.sp,
            lineHeight = 15.sp
        )
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun StatsPanel(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(RushSurface, RoundedCornerShape(18.dp))
            .border(1.dp, RushSurfaceVariant, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) { content() }
}

@Composable
private fun StatsRow(label: String, value: String, tint: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f), fontSize = 12.sp)
        Text(value, color = tint, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun ModeStatsCard(mode: GameMode, stats: ModeStatistics) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(RushSurface, RoundedCornerShape(16.dp))
            .border(1.dp, RushSurfaceVariant, RoundedCornerShape(16.dp))
            .padding(15.dp)
    ) {
        Text(mode.name.replace('_', ' '), color = RushMint, fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Spacer(Modifier.height(7.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MiniStat("GAMES", stats.gamesPlayed.toString())
            MiniStat("BEST SCORE", stats.highestScore.toString())
            MiniStat("ACCURACY", "${stats.accuracyPercent}%")
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MiniStat("LEVEL", stats.highestLevel.toString())
            MiniStat("BEST COMBO", "${stats.bestCombo}×")
            MiniStat("POWERUPS", stats.powerupsCollected.toString())
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String) {
    Column {
        Text(label, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.42f), fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.7.sp)
        Text(value, color = RushAmber, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
    }
}

private fun formatPlayTime(durationMs: Long): String {
    val totalSeconds = (durationMs.coerceAtLeast(0L) / 1_000L)
    val hours = totalSeconds / 3_600L
    val minutes = (totalSeconds % 3_600L) / 60L
    return if (hours > 0L) "${hours}h ${minutes}m" else "${minutes}m ${totalSeconds % 60L}s"
}
