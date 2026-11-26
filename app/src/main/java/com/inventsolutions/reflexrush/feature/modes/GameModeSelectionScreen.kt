package com.inventsolutions.reflexrush.feature.modes

import com.inventsolutions.reflexrush.core.ui.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventsolutions.reflexrush.game.GameMode
import com.inventsolutions.reflexrush.ui.theme.RushAmber
import com.inventsolutions.reflexrush.ui.theme.RushGreen
import com.inventsolutions.reflexrush.ui.theme.RushMint
import com.inventsolutions.reflexrush.ui.theme.RushRed

@Composable
fun GameModeSelectionScreen(onModeSelected: (GameMode) -> Unit, onBack: () -> Unit) {
    val width = LocalConfiguration.current.screenWidthDp
    val inset = ((width - 820) / 2).coerceAtLeast(20).dp
    val isTablet = width >= 700
    val modes = listOf(
        ModeOption(GameMode.CLASSIC, "01", "⚡", "CLASSIC", "THE ORIGINAL TEST", "Three lives. Build combos, collect powerups and beat your personal best.", RushGreen, "3 LIVES", "ENDLESS ROUND"),
        ModeOption(GameMode.TIME_ATTACK, "02", "⏱", "TIME ATTACK", "RACE THE CLOCK", "You have 60 seconds. Every good hit counts before time runs out.", RushAmber, "60 SECONDS", "POWERUPS"),
        ModeOption(GameMode.SURVIVAL, "03", "🔥", "SURVIVAL", "NO ROOM FOR ERROR", "Targets arrive faster and danger becomes more frequent. Stay alive as long as you can.", RushRed, "3 LIVES", "RISING INTENSITY"),
        ModeOption(GameMode.PRECISION, "04", "◎", "PRECISION", "MAKE EVERY TAP COUNT", "Face 30 targets. Misses reduce your score, but do not cost lives.", RushMint, "30 TARGETS", "ACCURACY CHALLENGE")
    )
    Box(Modifier.fillMaxSize()) {
        ArcadeBackdrop(Modifier.fillMaxSize(), RushMint, intensity = .78f)
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = inset, vertical = if (isTablet) 24.dp else 17.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ArcadeScreenHeader("REFLEX RUSH", "CHOOSE YOUR MODE", onBack)
            Spacer(Modifier.height(19.dp))
            ArcadePanel(Modifier.fillMaxWidth(), RushMint, 21.dp) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 17.dp, vertical = 16.dp)) {
                    Text("ENTER THE ARENA", color = RushGreen, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    Spacer(Modifier.height(5.dp))
                    Text("Every mode tests a different reflex.", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("Choose a challenge and set a new personal record.", color = Color.White.copy(alpha = .55f), fontSize = 10.sp)
                }
            }
            Spacer(Modifier.height(17.dp))
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                modes.forEach { option ->
                    Column(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(21.dp))
                            .background(Color(0xE60A1525))
                            .border(1.dp, option.accent.copy(alpha = .55f), RoundedCornerShape(21.dp))
                            .clickable { onModeSelected(option.mode) }
                            .padding(horizontal = 16.dp, vertical = 15.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(option.number, color = option.accent.copy(alpha = .6f), fontSize = 9.sp, fontWeight = FontWeight.Black)
                            Spacer(Modifier.width(11.dp))
                            Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(option.accent.copy(alpha = .13f)), contentAlignment = Alignment.Center) {
                                Text(option.icon, color = option.accent, fontSize = 25.sp)
                            }
                            Spacer(Modifier.width(13.dp))
                            Column(Modifier.weight(1f)) {
                                Text(option.title, color = option.accent, fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 1.3.sp)
                                Text(option.subtitle, color = Color.White.copy(alpha = .62f), fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = .9.sp)
                            }
                            Text("›", color = option.accent, fontSize = 29.sp, fontWeight = FontWeight.Light)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(option.description, color = Color.White.copy(alpha = .68f), fontSize = 10.sp, lineHeight = 15.sp)
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            ModeRuleChip(option.ruleOne, option.accent, Modifier.weight(1f))
                            ModeRuleChip(option.ruleTwo, option.accent, Modifier.weight(1f))
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("YOUR RECORDS ARE SAVED SEPARATELY FOR EACH MODE", color = Color.White.copy(alpha = .4f), fontSize = 7.sp, fontWeight = FontWeight.Bold, letterSpacing = .8.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun ModeRuleChip(label: String, tint: Color, modifier: Modifier = Modifier) {
    Row(modifier.clip(RoundedCornerShape(8.dp)).background(tint.copy(alpha = .09f)).padding(horizontal = 9.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("•", color = tint, fontSize = 10.sp)
        Spacer(Modifier.width(5.dp))
        Text(label, color = tint, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = .5.sp, maxLines = 1)
    }
}

private data class ModeOption(
    val mode: GameMode,
    val number: String,
    val icon: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val accent: Color,
    val ruleOne: String,
    val ruleTwo: String
)
