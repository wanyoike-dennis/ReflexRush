package com.inventsolutions.reflexrush.feature.highscores

import com.inventsolutions.reflexrush.core.ui.ArcadeBackdrop
import com.inventsolutions.reflexrush.core.ui.ArcadePanel
import com.inventsolutions.reflexrush.core.ui.ArcadeScreenHeader
import com.inventsolutions.reflexrush.game.GameMode
import com.inventsolutions.reflexrush.ui.theme.RushAmber
import com.inventsolutions.reflexrush.ui.theme.RushGreen
import com.inventsolutions.reflexrush.ui.theme.RushMint
import androidx.compose.foundation.background
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
fun HighScoresScreen(bestScores: Map<GameMode, Int>, onBack: () -> Unit) {
    val width = LocalConfiguration.current.screenWidthDp
    val inset = ((width - 760) / 2).coerceAtLeast(20).dp
    val ranked = bestScores.entries.sortedByDescending { it.value }
    val overallBest = ranked.firstOrNull()?.value ?: 0
    Box(Modifier.fillMaxSize()) {
        ArcadeBackdrop(Modifier.fillMaxSize(), RushAmber, intensity = .48f)
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState())
                .padding(horizontal = inset, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ArcadeScreenHeader("PLAYER PROFILE", "HIGH SCORES", onBack)
            Spacer(Modifier.height(22.dp))
            ArcadePanel(Modifier.fillMaxWidth(), RushAmber, 24.dp) {
                Column(Modifier.fillMaxWidth().padding(vertical = 22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ALL-TIME PERSONAL BEST", color = RushMint, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.7.sp)
                    Spacer(Modifier.height(7.dp))
                    Text(if (overallBest > 0) "%,d".format(overallBest) else "—", color = RushAmber, fontSize = 48.sp, fontWeight = FontWeight.Black)
                    Text("YOUR TOP SCORE ACROSS ALL MODES", color = Color.White.copy(alpha = .48f), fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = .8.sp)
                }
            }
            Spacer(Modifier.height(23.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("MODE LEADERBOARD", color = RushMint, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.6.sp)
                Spacer(Modifier.weight(1f))
                Text("PERSONAL RECORDS", color = Color.White.copy(alpha = .38f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            ranked.forEachIndexed { index, entry ->
                val tint = if (index == 0 && entry.value > 0) RushAmber else RushGreen
                ArcadePanel(Modifier.fillMaxWidth(), tint, 19.dp) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 17.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(42.dp).clip(CircleShape).background(tint.copy(alpha = .13f)), contentAlignment = Alignment.Center) {
                            Text(if (index == 0 && entry.value > 0) "♛" else "${index + 1}", color = tint, fontSize = 19.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(entry.key.name.replace('_', ' '), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = .8.sp)
                            Text(if (entry.value > 0) "PERSONAL RECORD" else "NO RECORD YET", color = RushMint.copy(alpha = .55f), fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = .7.sp)
                        }
                        Text(if (entry.value > 0) "%,d".format(entry.value) else "—", color = tint, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text("Records are saved locally on this device.", color = Color.White.copy(alpha = .48f), fontSize = 10.sp)
            Spacer(Modifier.height(20.dp))
        }
    }
}
