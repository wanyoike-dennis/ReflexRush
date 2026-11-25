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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventsolutions.reflexrush.game.GameMode
import com.inventsolutions.reflexrush.ui.theme.RushAmber
import com.inventsolutions.reflexrush.ui.theme.RushBackground
import com.inventsolutions.reflexrush.ui.theme.RushGreen
import com.inventsolutions.reflexrush.ui.theme.RushMint
import com.inventsolutions.reflexrush.ui.theme.RushRed
import com.inventsolutions.reflexrush.ui.theme.RushSurface
import com.inventsolutions.reflexrush.ui.theme.RushSurfaceVariant

@Composable
fun GameModeSelectionScreen(
    onModeSelected: (GameMode) -> Unit,
    onBack: () -> Unit
) {
    val modes = listOf(
        ModeOption(
            mode = GameMode.CLASSIC,
            icon = "⚡",
            title = "CLASSIC",
            subtitle = "The original Reflex Rush",
            description = "Three lives. Build combos, collect powerups and beat your personal best.",
            accent = RushGreen,
            rule = "3 LIVES • ENDLESS ROUND"
        ),
        ModeOption(
            mode = GameMode.TIME_ATTACK,
            icon = "⏱",
            title = "TIME ATTACK",
            subtitle = "Score as much as you can",
            description = "You have 60 seconds. Every good hit counts before the clock reaches zero.",
            accent = RushAmber,
            rule = "60 SECONDS • POWERUPS"
        ),
        ModeOption(
            mode = GameMode.SURVIVAL,
            icon = "🔥",
            title = "SURVIVAL",
            subtitle = "Can you handle the pressure?",
            description = "Start with three lives. Targets arrive faster and danger becomes more frequent as you score.",
            accent = RushRed,
            rule = "3 LIVES • RISING INTENSITY"
        ),
        ModeOption(
            mode = GameMode.PRECISION,
            icon = "◎",
            title = "PRECISION",
            subtitle = "Accuracy over speed",
            description = "Face 30 targets. Missed good targets and danger taps reduce your score. Misses do not cost lives.",
            accent = RushMint,
            rule = "30 TARGETS • ACCURACY CHALLENGE"
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(RushBackground, Color(0xFF101B2C), RushBackground))
            )
            .safeDrawingPadding()
            .padding(horizontal = 22.dp, vertical = 18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("REFLEX RUSH", color = RushMint, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                    Text("CHOOSE YOUR CHALLENGE", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                }
                OutlinedButton(
                    onClick = onBack,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RushMint)
                ) {
                    Text("BACK", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(18.dp))
            Text(
                "PICK YOUR MODE",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 25.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.4.sp,
                textAlign = TextAlign.Center
            )
            Text(
                "Every mode tests your reflexes in a different way.",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(18.dp))

            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                modes.forEach { option ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(RushSurface)
                            .border(1.dp, RushSurfaceVariant, RoundedCornerShape(18.dp))
                            .clickable { onModeSelected(option.mode) }
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(option.accent.copy(alpha = 0.14f))
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(option.icon, color = option.accent, fontSize = 23.sp)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(option.title, color = option.accent, fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
                                Text(option.subtitle, color = MaterialTheme.colorScheme.onBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("›", color = option.accent, fontSize = 28.sp, fontWeight = FontWeight.Light)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            option.description,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.66f),
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(option.rule, color = option.accent.copy(alpha = 0.9f), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.8.sp)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "YOUR RECORDS ARE SAVED SEPARATELY FOR EACH MODE",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.7.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

private data class ModeOption(
    val mode: GameMode,
    val icon: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val accent: Color,
    val rule: String
)
