package com.inventsolutions.reflexrush.feature.home

import com.inventsolutions.reflexrush.core.ui.ArcadeBackdrop
import com.inventsolutions.reflexrush.core.ui.ArcadePanel
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
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
    onHighScoresClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    bestScore: Int = 0,
    totalGamesPlayed: Int = 0,
    accuracyPercent: Int = 0,
    level: Int = 1,
    xpIntoLevel: Int = 0,
    xpForNextLevel: Int = 250
) {
    val width = LocalConfiguration.current.screenWidthDp
    val inset = ((width - 720) / 2).coerceAtLeast(22).dp
    val progress = if (xpForNextLevel > 0) (xpIntoLevel.toFloat() / xpForNextLevel).coerceIn(0f, 1f) else 0f

    Box(Modifier.fillMaxSize()) {
        ArcadeBackdrop(Modifier.fillMaxSize(), intensity = 1.05f)
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState())
                .padding(horizontal = inset, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Brand header and the single avatar entry point to Player Profile.
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(46.dp).clip(RoundedCornerShape(14.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF8CFFDA), RushGreen))),
                        contentAlignment = Alignment.Center
                    ) { Text("R", color = Color(0xFF062116), fontSize = 28.sp, fontWeight = FontWeight.Black) }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("REFLEX RUSH", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Text("ARCADE REACTION CHALLENGE", color = RushMint, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.1.sp)
                    }
                }
                IconButton(onClick = onProfileClick, modifier = Modifier.size(54.dp)) {
                    Box(
                        Modifier.size(46.dp).clip(CircleShape).background(Color(0xFF14283A))
                            .border(1.5.dp, RushGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("●", color = RushMint, fontSize = 22.sp)
                    }
                }
            }

            Spacer(Modifier.height(34.dp))
            Column(Modifier.fillMaxWidth()) {
                Text("WELCOME BACK", color = RushMint, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 2.7.sp)
                Spacer(Modifier.height(5.dp))
                Text("Ready to beat your best?", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(5.dp))
                Text("Every round is a chance to sharpen your reflexes.", color = Color.White.copy(alpha = .65f), fontSize = 11.sp)
            }

            Spacer(Modifier.height(24.dp))
            // Large personal-best panel with a luminous circular score motif.
            ArcadePanel(Modifier.fillMaxWidth(), accent = RushGreen, radius = 24.dp) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 22.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("PERSONAL BEST", color = RushMint.copy(alpha = .8f), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.8.sp)
                            Spacer(Modifier.height(5.dp))
                            Text(if (bestScore > 0) bestScore.toString() else "—", color = RushAmber, fontSize = 42.sp, lineHeight = 46.sp, fontWeight = FontWeight.Black)
                            Text("CLASSIC MODE", color = Color.White.copy(alpha = .52f), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp)
                        }
                        Box(
                            Modifier.size(100.dp).clip(CircleShape)
                                .background(Brush.radialGradient(listOf(RushGreen.copy(alpha = .28f), RushGreen.copy(alpha = .05f), Color.Transparent)))
                                .border(1.dp, RushMint.copy(alpha = .5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(Modifier.size(76.dp).border(1.dp, RushGreen.copy(alpha = .55f), CircleShape), contentAlignment = Alignment.Center) {
                                Text("⚡", color = RushMint, fontSize = 42.sp)
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        HomeMetric("GAMES PLAYED", totalGamesPlayed.toString(), RushMint)
                        HomeMetric("ACCURACY", if (totalGamesPlayed > 0) "$accuracyPercent%" else "—", RushGreen)
                        HomeMetric("PLAYER LEVEL", level.toString(), RushAmber)
                    }
                }
            }

            Spacer(Modifier.height(22.dp))
            Button(
                onClick = onPlayClick,
                modifier = Modifier.fillMaxWidth().height(68.dp)
                    .shadow(18.dp, RoundedCornerShape(19.dp), ambientColor = Color(0x9927E6A1)),
                shape = RoundedCornerShape(19.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RushGreen, contentColor = Color(0xFF04170F))
            ) {
                Text("PLAY NOW", fontSize = 19.sp, fontWeight = FontWeight.Black, letterSpacing = 2.5.sp)
                Spacer(Modifier.width(12.dp))
                Text("▶", fontSize = 17.sp, fontWeight = FontWeight.Black)
            }

            Spacer(Modifier.height(22.dp))
            ArcadePanel(Modifier.fillMaxWidth(), accent = RushAmber, radius = 20.dp) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 19.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("YOUR PROGRESSION", color = RushMint, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.6.sp)
                            Spacer(Modifier.height(4.dp))
                            Text("Level $level · Sharpshooter", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("$xpIntoLevel / $xpForNextLevel XP", color = RushAmber, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Spacer(Modifier.height(14.dp))
                    Box(Modifier.fillMaxWidth().height(9.dp).clip(CircleShape).background(Color.White.copy(alpha = .1f))) {
                        Box(Modifier.fillMaxWidth(progress).height(9.dp).clip(CircleShape).background(Brush.horizontalGradient(listOf(RushGreen, RushMint))))
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("Earn XP by completing rounds, daily missions and achievements.", color = Color.White.copy(alpha = .58f), fontSize = 10.sp)
                }
            }

            Spacer(Modifier.height(30.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("EXPLORE", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Text("YOUR GAME HUB", color = RushMint.copy(alpha = .65f), fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                ExploreTile("▥", "STATISTICS", "Your performance", RushMint, onStatisticsClick, Modifier.weight(1f))
                ExploreTile("★", "ACHIEVEMENTS", "Missions & rewards", RushAmber, onProgressionClick, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                ExploreTile("🏆", "HIGH SCORES", "Mode records", RushGreen, onHighScoresClick, Modifier.weight(1f))
                ExploreTile("⚙", "SETTINGS", "Audio & backup", RushMint, onSettingsClick, Modifier.weight(1f))
            }
            Spacer(Modifier.height(24.dp))
            Text("TRUST YOUR REFLEXES.", color = RushMint.copy(alpha = .45f), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.6.sp)
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun HomeMetric(label: String, value: String, tint: Color) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(value, color = tint, fontSize = 20.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(3.dp))
        Text(label, color = Color.White.copy(alpha = .52f), fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = .7.sp)
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
        modifier = modifier.height(92.dp),
        shape = RoundedCornerShape(17.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = .5f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = accent),
        contentPadding = PaddingValues(horizontal = 13.dp, vertical = 12.dp)
    ) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 17.sp)
                Spacer(Modifier.width(8.dp))
                Text(title, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = .6.sp, maxLines = 1)
            }
            Spacer(Modifier.height(6.dp))
            Text(subtitle, color = Color.White.copy(alpha = .55f), fontSize = 9.sp, textAlign = TextAlign.Start)
        }
    }
}
