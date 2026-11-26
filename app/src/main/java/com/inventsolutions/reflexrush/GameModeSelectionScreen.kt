package com.inventsolutions.reflexrush

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
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
fun GameModeSelectionScreen(
    onModeSelected: (GameMode) -> Unit,
    onBack: () -> Unit
) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val horizontalInset = ((screenWidthDp - 960) / 2).coerceAtLeast(20).dp
    val isTabletLayout = screenWidthDp >= 700
    val modes = listOf(
        ModeOption(GameMode.CLASSIC, "01", "⚡", "CLASSIC", "THE ORIGINAL TEST", "Three lives. Build combos, collect powerups and beat your personal best.", RushGreen, "3 LIVES  •  ENDLESS ROUND"),
        ModeOption(GameMode.TIME_ATTACK, "02", "⏱", "TIME ATTACK", "RACE THE CLOCK", "You have 60 seconds. Every good hit counts before time runs out.", RushAmber, "60 SECONDS  •  POWERUPS"),
        ModeOption(GameMode.SURVIVAL, "03", "🔥", "SURVIVAL", "NO ROOM FOR ERROR", "Start with three lives. Targets arrive faster and danger gets more frequent.", RushRed, "3 LIVES  •  RISING INTENSITY"),
        ModeOption(GameMode.PRECISION, "04", "◎", "PRECISION", "MAKE EVERY TAP COUNT", "Face 30 targets. Misses reduce your score, but do not cost lives.", RushMint, "30 TARGETS  •  ACCURACY CHALLENGE")
    )

    Box(Modifier.fillMaxSize()) {
        ArcadeBackdrop(Modifier.fillMaxSize(), RushMint)
        Column(
            modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = horizontalInset, vertical = if (isTabletLayout) 24.dp else 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("REFLEX RUSH", color = RushMint, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 2.2.sp)
                    Text("SELECT YOUR ARENA", color = Color.White.copy(alpha = 0.55f), fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
                }
                OutlinedButton(
                    onClick = onBack,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RushMint)
                ) { Text("EXIT", fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp) }
            }

            Spacer(Modifier.height(if (isTabletLayout) 30.dp else 22.dp))
            Text("CHOOSE YOUR", color = RushMint, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 3.sp)
            Text("CHALLENGE", color = Color.White, fontSize = 31.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp, textAlign = TextAlign.Center)
            Text("Four ways to test your reaction speed.", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
            Spacer(Modifier.height(20.dp))

            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                modes.forEach { option ->
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color(0xD90A1525))
                            .border(1.dp, option.accent.copy(alpha = 0.52f), RoundedCornerShape(22.dp))
                            .clickable { onModeSelected(option.mode) }
                            .padding(horizontal = 17.dp, vertical = 15.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(option.number, color = option.accent.copy(alpha = 0.55f), fontSize = 10.sp, fontWeight = FontWeight.Black)
                            Spacer(Modifier.width(12.dp))
                            Box(
                                modifier = Modifier.clip(RoundedCornerShape(13.dp))
                                    .background(option.accent.copy(alpha = 0.14f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) { Text(option.icon, color = option.accent, fontSize = 24.sp) }
                            Spacer(Modifier.width(13.dp))
                            Column(Modifier.weight(1f)) {
                                Text(option.title, color = option.accent, fontSize = 15.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
                                Text(option.subtitle, color = Color.White.copy(alpha = 0.75f), fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            }
                            Text("›", color = option.accent, fontSize = 30.sp, fontWeight = FontWeight.Light)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(option.description, color = Color.White.copy(alpha = 0.68f), fontSize = 10.sp, lineHeight = 15.sp)
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                .background(option.accent.copy(alpha = 0.09f)).padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("▶", color = option.accent, fontSize = 8.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(option.rule, color = option.accent, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 0.8.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("YOUR RECORDS ARE SAVED SEPARATELY FOR EACH MODE", color = Color.White.copy(alpha = 0.38f), fontSize = 7.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp, textAlign = TextAlign.Center)
        }
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
    val rule: String
)
