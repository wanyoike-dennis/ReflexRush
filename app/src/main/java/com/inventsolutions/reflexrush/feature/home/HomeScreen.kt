package com.inventsolutions.reflexrush.feature.home

import com.inventsolutions.reflexrush.core.ui.ArcadeBackdrop
import com.inventsolutions.reflexrush.core.ui.ArcadePanel
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventsolutions.reflexrush.ui.theme.RushAmber
import com.inventsolutions.reflexrush.ui.theme.RushGreen
import com.inventsolutions.reflexrush.ui.theme.RushMint

@Composable
fun ReflexRushHome(
    onPlayClick: () -> Unit = {},
    onStatisticsClick: () -> Unit = {},
    onProgressionClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onProfileClick: () -> Unit = onStatisticsClick,
    bestScore: Int = 0,
    totalGamesPlayed: Int = 0,
    accuracyPercent: Int = 0,
    level: Int = 1,
    xpIntoLevel: Int = 0,
    xpForNextLevel: Int = 250
) {
    val width = LocalConfiguration.current.screenWidthDp
    val inset = ((width - 720) / 2).coerceAtLeast(20).dp
    val progress = if (xpForNextLevel > 0) (xpIntoLevel.toFloat() / xpForNextLevel).coerceIn(0f, 1f) else 0f

    Box(Modifier.fillMaxSize()) {
        ArcadeBackdrop(Modifier.fillMaxSize(), intensity = 1.05f)
        Column(
            Modifier.fillMaxSize().safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = inset, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(42.dp).clip(RoundedCornerShape(13.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF7BFFD0), RushGreen))),
                        contentAlignment = Alignment.Center
                    ) { Text("R", color = Color(0xFF062116), fontSize = 25.sp, fontWeight = FontWeight.Black) }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("REFLEX RUSH", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black, letterSpacing = 1.8.sp)
                        Text("ARCADE REACTION CHALLENGE", color = RushMint, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    }
                }
                OutlinedButton(
                    onClick = onProfileClick,
                    shape = RoundedCornerShape(22.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RushGreen.copy(alpha = .7f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RushMint),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Box(
                        Modifier.size(30.dp).clip(CircleShape)
                            .background(Color(0xFF172438)).border(1.dp, RushGreen.copy(alpha = .7f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) { Text("●", color = RushMint, fontSize = 16.sp) }
                    Spacer(Modifier.width(8.dp))
                    Text("PLAYER PROFILE", color = RushMint, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = .7.sp)
                }
            }

            Spacer(Modifier.height(34.dp))
            Column(Modifier.fillMaxWidth()) {
                Text("WELCOME BACK", color = RushMint, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 2.5.sp)
                Text("Ready to beat your best?", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text("Every round is a chance to sharpen your reflexes.", color = Color.White.copy(alpha = .62f), fontSize = 11.sp)
            }

            Spacer(Modifier.height(26.dp))
            ArcadePanel(Modifier.fillMaxWidth(), accent = RushGreen, radius = 22.dp) {
                Column(Modifier.fillMaxWidth().padding(22.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("PERSONAL BEST", color = RushMint.copy(alpha = .75f), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
                            Text(if (bestScore > 0) bestScore.toString() else "—", color = RushAmber, fontSize = 35.sp, fontWeight = FontWeight.Black)
                            Text("CLASSIC MODE", color = Color.White.copy(alpha = .5f), fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                        Box(Modifier.size(72.dp).clip(CircleShape).background(RushGreen.copy(alpha = .13f)).border(1.dp, RushGreen.copy(alpha = .55f), CircleShape), contentAlignment = Alignment.Center) {
                            Text("⚡", color = RushMint, fontSize = 39.sp)
                        }
                    }
                    Spacer(Modifier.height(22.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        HomeMetric("GAMES PLAYED", totalGamesPlayed.toString(), RushMint)
                        HomeMetric("ACCURACY", if (totalGamesPlayed > 0) "$accuracyPercent%" else "—", RushGreen)
                        HomeMetric("PLAYER LEVEL", level.toString(), RushAmber)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onPlayClick,
                modifier = Modifier.fillMaxWidth().height(70.dp)
                    .shadow(17.dp, RoundedCornerShape(18.dp), ambientColor = Color(0x9927E6A1)),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RushGreen, contentColor = Color(0xFF04170F))
            ) {
                Text("PLAY NOW", fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 2.2.sp)
                Spacer(Modifier.width(12.dp))
                Text("▶", fontSize = 16.sp, fontWeight = FontWeight.Black)
            }

            Spacer(Modifier.height(18.dp))
            ArcadePanel(Modifier.fillMaxWidth(), accent = RushAmber, radius = 18.dp) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("YOUR PROGRESSION", color = RushMint, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.3.sp)
                            Text("Level $level · Sharpshooter", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("$xpIntoLevel / $xpForNextLevel XP", color = RushAmber, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Spacer(Modifier.height(15.dp))
                    Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(Color.White.copy(alpha = .1f))) {
                        Box(Modifier.fillMaxWidth(progress).height(8.dp).clip(CircleShape).background(Brush.horizontalGradient(listOf(RushGreen, RushMint))))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Earn XP by completing rounds, daily missions and achievements.", color = Color.White.copy(alpha = .55f), fontSize = 9.sp)
                }
            }

            Spacer(Modifier.height(30.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("EXPLORE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.8.sp)
                Text("YOUR GAME HUB", color = RushMint.copy(alpha = .6f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                ExploreTile("▥", "STATISTICS", "Your performance", RushMint, onStatisticsClick, Modifier.weight(1f))
                ExploreTile("★", "ACHIEVEMENTS", "Missions & rewards", RushAmber, onProgressionClick, Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ExploreTile("🏆", "HIGH SCORES", "Mode records", RushGreen, onStatisticsClick, Modifier.weight(1f))
                ExploreTile("⚙", "SETTINGS", "Audio & backup", RushMint, onSettingsClick, Modifier.weight(1f))
            }
            Spacer(Modifier.height(18.dp))
            Text("TRUST YOUR REFLEXES.", color = RushMint.copy(alpha = .42f), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun HomeMetric(label: String, value: String, tint: Color) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(value, color = tint, fontSize = 19.sp, fontWeight = FontWeight.Black)
        Text(label, color = Color.White.copy(alpha = .48f), fontSize = 7.sp, fontWeight = FontWeight.Bold, letterSpacing = .6.sp)
    }
}

@Composable
private fun ExploreTile(
    icon: String,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(96.dp),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = .48f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = accent)
    ) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 15.sp)
                Spacer(Modifier.width(7.dp))
                Text(title, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = .5.sp, maxLines = 1)
            }
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = Color.White.copy(alpha = .48f), fontSize = 8.sp, textAlign = TextAlign.Start)
        }
    }
}
