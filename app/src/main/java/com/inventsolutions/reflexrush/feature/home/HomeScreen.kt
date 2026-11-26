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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventsolutions.reflexrush.ui.theme.RushAmber
import com.inventsolutions.reflexrush.ui.theme.RushGreen
import com.inventsolutions.reflexrush.ui.theme.RushMint

private val DashboardText = Color(0xFFF4F8FF)
private val DashboardBlue = Color(0xFF9BD8FF)
private val DashboardPanel = Color(0xB8092940)

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
    bestStreak: Int = 0,
    level: Int = 1,
    xpIntoLevel: Int = 0,
    xpForNextLevel: Int = 250
) {
    val width = LocalConfiguration.current.screenWidthDp
    val inset = ((width - 720) / 2).coerceAtLeast(20).dp
    val progress = if (xpForNextLevel > 0) (xpIntoLevel.toFloat() / xpForNextLevel).coerceIn(0f, 1f) else 0f
    val progressPercent = (progress * 100).toInt()

    Box(Modifier.fillMaxSize()) {
        ArcadeBackdrop(Modifier.fillMaxSize(), intensity = 1.05f)
        Column(
            Modifier.fillMaxSize().safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = inset, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Text("ϟ", color = RushGreen, fontSize = 54.sp, fontWeight = FontWeight.Black, lineHeight = 54.sp)
                    Spacer(Modifier.width(9.dp))
                    Column(verticalArrangement = Arrangement.spacedBy((-5).dp)) {
                        Text("REFLEX", color = DashboardText, fontSize = 25.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic, letterSpacing = (-1).sp)
                        Text("RUSH", color = RushGreen, fontSize = 27.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic, letterSpacing = (-.5).sp)
                    }
                }
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier.size(56.dp).border(1.5.dp, RushMint, CircleShape)
                        .background(Color(0xFF10334A), CircleShape)
                ) {
                    Text("D", color = DashboardText, fontSize = 25.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(30.dp))
            Column(Modifier.fillMaxWidth()) {
                Text("Welcome back", color = DashboardBlue, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                Text("Ready to beat", color = DashboardText, fontSize = 39.sp, lineHeight = 42.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1.1).sp)
                Text("your best?", color = RushGreen, fontSize = 43.sp, lineHeight = 45.sp, fontWeight = FontWeight.ExtraBold, fontStyle = FontStyle.Italic, letterSpacing = (-1.2).sp)
            }

            Spacer(Modifier.height(26.dp))
            ArcadePanel(Modifier.fillMaxWidth(), accent = RushMint, radius = 23.dp) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 19.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f)) {
                            Text("YOUR BEST SCORE", color = DashboardBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = .4.sp)
                            Spacer(Modifier.height(5.dp))
                            Text(if (bestScore > 0) "%,d".format(bestScore) else "—", color = DashboardText, fontSize = 48.sp, lineHeight = 52.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Row(
                            Modifier.clip(CircleShape)
                                .background(Brush.horizontalGradient(listOf(RushGreen, RushMint)))
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("♜", color = Color(0xFF04251C), fontSize = 17.sp)
                            Spacer(Modifier.width(6.dp))
                            Text("PERSONAL BEST", color = Color(0xFF04251C), fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Spacer(Modifier.height(3.dp))
                    Text("Keep improving. Every millisecond counts.", color = DashboardBlue, fontSize = 12.sp)
                    Spacer(Modifier.height(17.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF31617B)))
                    Spacer(Modifier.height(14.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        DashboardMetric("🎮", "Games played", totalGamesPlayed.toString(), DashboardBlue, Modifier.weight(1f))
                        Box(Modifier.width(1.dp).height(44.dp).background(Color(0xFF31617B)))
                        DashboardMetric("🔥", "Best streak", bestStreak.toString(), RushAmber, Modifier.weight(1f))
                        Box(Modifier.width(1.dp).height(44.dp).background(Color(0xFF31617B)))
                        DashboardMetric("★", "Level", level.toString(), Color(0xFFFFD92E), Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(22.dp))
            Button(
                onClick = onPlayClick,
                modifier = Modifier.fillMaxWidth().height(68.dp)
                    .shadow(17.dp, RoundedCornerShape(19.dp), ambientColor = Color(0x9927E6A1)),
                shape = RoundedCornerShape(19.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RushGreen, contentColor = Color(0xFF03251B))
            ) {
                Text("▶", fontSize = 23.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.width(13.dp))
                Text("PLAY NOW", fontSize = 22.sp, fontWeight = FontWeight.Black, letterSpacing = .7.sp)
            }

            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Your progress", color = DashboardText, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                TextButton(onClick = onProgressionClick, contentPadding = PaddingValues(horizontal = 3.dp, vertical = 0.dp)) {
                    Text("View all  →", color = RushMint, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(5.dp))
            ArcadePanel(Modifier.fillMaxWidth(), accent = RushMint, radius = 19.dp) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 17.dp, vertical = 15.dp)) {
                    Text("Level $level  ·  Sharpshooter", color = DashboardText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(9.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f).height(11.dp).clip(CircleShape).background(Color(0xFF12415B))) {
                            Box(Modifier.fillMaxWidth(progress).height(11.dp).clip(CircleShape).background(Brush.horizontalGradient(listOf(RushGreen, RushMint))))
                        }
                        Spacer(Modifier.width(12.dp))
                        Text("$progressPercent%", color = DashboardText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(7.dp))
                    Text("$xpIntoLevel / $xpForNextLevel XP to next level", color = DashboardBlue, fontSize = 11.sp)
                }
            }

            Spacer(Modifier.height(25.dp))
            Text("Explore", Modifier.fillMaxWidth(), color = DashboardText, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Start)
            Spacer(Modifier.height(11.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardExploreTile("▥", "Statistics", "Review performance", RushMint, onStatisticsClick, Modifier.weight(1f))
                DashboardExploreTile("🏆", "High scores", "Your records", Color(0xFFFFD92E), onHighScoresClick, Modifier.weight(1f))
            }
            Spacer(Modifier.height(11.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardExploreTile("ϟ", "Achievements", "Track your progress", Color(0xFFFFD92E), onProgressionClick, Modifier.weight(1f))
                DashboardExploreTile("⚙", "Settings", "Make it yours", RushMint, onSettingsClick, Modifier.weight(1f))
            }
            Spacer(Modifier.height(22.dp))
        }
    }
}

@Composable
private fun DashboardMetric(icon: String, label: String, value: String, tint: Color, modifier: Modifier = Modifier) {
    Row(modifier.padding(horizontal = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 20.sp)
        Spacer(Modifier.width(7.dp))
        Column {
            Text(label, color = DashboardBlue, fontSize = 9.sp, maxLines = 1)
            Text(value, color = DashboardText, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DashboardExploreTile(
    icon: String,
    title: String,
    subtitle: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(76.dp).border(1.dp, tint.copy(alpha = .8f), RoundedCornerShape(17.dp)),
        shape = RoundedCornerShape(17.dp),
        colors = ButtonDefaults.buttonColors(containerColor = DashboardPanel, contentColor = DashboardText),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, color = tint, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text(title, color = DashboardText, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(subtitle, color = DashboardBlue, fontSize = 9.sp, maxLines = 1)
            }
            Text("›", color = DashboardText, fontSize = 23.sp, fontWeight = FontWeight.Light)
        }
    }
}
