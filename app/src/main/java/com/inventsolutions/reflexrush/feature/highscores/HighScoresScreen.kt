package com.inventsolutions.reflexrush.feature.highscores

import com.inventsolutions.reflexrush.core.ui.ArcadeBackdrop
import com.inventsolutions.reflexrush.core.ui.ArcadeScreenHeader
import com.inventsolutions.reflexrush.core.ui.ArcadePanel
import com.inventsolutions.reflexrush.game.GameMode
import com.inventsolutions.reflexrush.ui.theme.RushAmber
import com.inventsolutions.reflexrush.ui.theme.RushGreen
import com.inventsolutions.reflexrush.ui.theme.RushMint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HighScoresScreen(bestScores: Map<GameMode, Int>, onBack: () -> Unit) {
    val width = LocalConfiguration.current.screenWidthDp
    val inset = ((width - 760) / 2).coerceAtLeast(22).dp
    Box(Modifier.fillMaxSize()) {
        ArcadeBackdrop(Modifier.fillMaxSize(), RushAmber, intensity = .4f)
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState())
                .padding(horizontal = inset, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ArcadeScreenHeader(
                eyebrow = "REFLEX RUSH",
                title = "HIGH SCORES",
                onBack = onBack
            )
            Spacer(Modifier.height(28.dp))
            bestScores.entries.sortedByDescending { it.value }.forEachIndexed { index, entry ->
                ArcadePanel(Modifier.fillMaxWidth(), accent = if (index == 0) RushAmber else RushGreen, radius = 20.dp) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (index == 0) "♛" else "${index + 1}", color = if (index == 0) RushAmber else RushMint, fontSize = 25.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.width(18.dp))
                        Column(Modifier.weight(1f)) {
                            Text(entry.key.name.replace('_', ' '), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            Text(if (entry.value > 0) "PERSONAL RECORD" else "NO RECORD YET", color = RushMint.copy(alpha = .62f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(entry.value.toString(), color = if (index == 0) RushAmber else RushGreen, fontSize = 25.sp, fontWeight = FontWeight.Black)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text("Records are saved locally on this device.", color = Color.White.copy(alpha = .5f), fontSize = 10.sp)
        }
    }
}
