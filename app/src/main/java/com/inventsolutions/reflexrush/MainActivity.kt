package com.inventsolutions.reflexrush

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventsolutions.reflexrush.game.GameMode
import com.inventsolutions.reflexrush.ui.theme.ReflexRushTheme
import com.inventsolutions.reflexrush.ui.theme.RushAmber
import com.inventsolutions.reflexrush.ui.theme.RushGreen
import com.inventsolutions.reflexrush.ui.theme.RushMint
import com.inventsolutions.reflexrush.ui.theme.RushRed

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReflexRushTheme {
                val highScoreStore = remember { HighScoreStore(applicationContext) }
                var bestScores by remember {
                    mutableStateOf(GameMode.entries.associateWith { highScoreStore.load(it) })
                }
                var showModeSelection by remember { mutableStateOf(false) }
                var showGame by remember { mutableStateOf(false) }
                var showStatistics by remember { mutableStateOf(false) }
                var showProgression by remember { mutableStateOf(false) }
                var showSettings by remember { mutableStateOf(false) }
                val audioSettingsStore = remember { AudioSettingsStore(applicationContext) }
                var soundEffectsEnabled by remember { mutableStateOf(audioSettingsStore.soundEffectsEnabled) }
                var musicEnabled by remember { mutableStateOf(audioSettingsStore.musicEnabled) }
                val progressionStore = remember { PlayerProgressionStore(applicationContext) }
                var playerProgression by remember { mutableStateOf(progressionStore.load()) }
                val statisticsStore = remember { PlayerStatisticsStore(applicationContext) }
                var playerStatistics by remember { mutableStateOf(statisticsStore.load()) }
                var selectedMode by remember { mutableStateOf(GameMode.CLASSIC) }
                BackHandler(enabled = showGame || showModeSelection || showStatistics || showProgression || showSettings) {
                    when {
                        showGame -> showGame = false
                        showModeSelection -> showModeSelection = false
                        showStatistics -> showStatistics = false
                        showProgression -> showProgression = false
                        showSettings -> showSettings = false
                    }
                }
                when {
                    showGame -> GameScreen(
                        onHome = { showGame = false },
                        lifecycleOwner = this,
                        bestScore = bestScores[selectedMode] ?: 0,
                        onNewBestScore = { score ->
                            val saved = highScoreStore.saveIfHigher(score, selectedMode)
                            bestScores = bestScores + (selectedMode to saved)
                        },
                        mode = selectedMode,
                        onSessionCompleted = { summary ->
                            playerStatistics = statisticsStore.record(summary)
                            playerProgression = progressionStore.record(summary)
                        }
                    )
                    showSettings -> GameSettingsScreen(
                        soundEffectsEnabled = soundEffectsEnabled,
                        musicEnabled = musicEnabled,
                        onSoundEffectsChange = { enabled ->
                            audioSettingsStore.setSoundEffectsEnabled(enabled)
                            soundEffectsEnabled = enabled
                        },
                        onMusicChange = { enabled ->
                            audioSettingsStore.setMusicEnabled(enabled)
                            musicEnabled = enabled
                        },
                        onBack = { showSettings = false }
                    )
                    showProgression -> PlayerProgressionScreen(
                        progression = playerProgression,
                        onBack = { showProgression = false }
                    )
                    showStatistics -> PlayerStatisticsScreen(
                        statistics = playerStatistics,
                        onBack = { showStatistics = false }
                    )
                    showModeSelection -> GameModeSelectionScreen(
                        onModeSelected = { mode ->
                            selectedMode = mode
                            showModeSelection = false
                            showGame = true
                        },
                        onBack = { showModeSelection = false }
                    )
                    else -> ReflexRushHome(
                        onPlayClick = { showModeSelection = true },
                        onStatisticsClick = {
                            playerStatistics = statisticsStore.load()
                            showStatistics = true
                        },
                        onProgressionClick = {
                            playerProgression = progressionStore.load()
                            showProgression = true
                        },
                        onSettingsClick = { showSettings = true },
                        bestScore = bestScores[GameMode.CLASSIC] ?: 0
                    )
                }
            }
        }
    }
}

@Composable
fun ReflexRushHome(
    onPlayClick: () -> Unit = {},
    onStatisticsClick: () -> Unit = {},
    onProgressionClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    bestScore: Int = 0
) {
    Box(Modifier.fillMaxSize()) {
        ArcadeBackdrop(Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .then(Modifier.padding(horizontal = 22.dp, vertical = 18.dp))
                .verticalScroll(androidx.compose.foundation.rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BrandMark()
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("REFLEX RUSH", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Text("ARCADE REACTION CHALLENGE", color = RushMint, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.4.sp)
                    }
                }
                Text("V 1.0", color = RushMint.copy(alpha = 0.65f), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            }

            Spacer(Modifier.height(22.dp))

            Box(
                modifier = Modifier.fillMaxWidth().height(218.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    listOf(0.46f, 0.34f, 0.22f).forEachIndexed { index, fraction ->
                        drawCircle(
                            color = RushGreen.copy(alpha = 0.12f + index * 0.04f),
                            radius = size.minDimension * fraction,
                            center = center,
                            style = Stroke(width = (1.2f + index * 0.5f).dp.toPx())
                        )
                    }
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color(0x6632F5B0), Color(0x1132F5B0), Color.Transparent),
                            center = center,
                            radius = size.minDimension * 0.34f
                        ),
                        radius = size.minDimension * 0.34f,
                        center = center
                    )
                }
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .shadow(28.dp, CircleShape, ambientColor = Color(0xAA27E6A1))
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF7BFFD0), Color(0xFF08A875)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚡", color = Color(0xFF062116), fontSize = 58.sp, fontWeight = FontWeight.Black)
                }
                TargetDot(Modifier.align(Alignment.TopEnd).padding(top = 34.dp, end = 54.dp), RushRed)
                TargetDot(Modifier.align(Alignment.BottomStart).padding(start = 48.dp, bottom = 30.dp), RushGreen)
                TargetDot(Modifier.align(Alignment.CenterEnd).padding(end = 36.dp).size(12.dp), RushAmber)
            }

            Text("TRUST YOUR", color = RushMint, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 4.sp)
            Text(
                "REFLEXES.",
                color = Color.White,
                fontSize = 39.sp,
                lineHeight = 43.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Hit green. Dodge red. Chase the perfect combo.",
                color = Color.White.copy(alpha = 0.66f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(22.dp))

            ArcadePanel(
                modifier = Modifier.fillMaxWidth(),
                accent = RushGreen,
                radius = 20.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("CLASSIC RECORD", color = RushMint.copy(alpha = 0.72f), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp)
                        Text(if (bestScore > 0) bestScore.toString() else "—", color = RushAmber, fontSize = 30.sp, fontWeight = FontWeight.Black)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("YOUR MISSION", color = RushMint.copy(alpha = 0.72f), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp)
                        Text("BEAT YOUR BEST", color = RushGreen, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = onPlayClick,
                modifier = Modifier.fillMaxWidth().height(64.dp)
                    .shadow(18.dp, RoundedCornerShape(20.dp), ambientColor = Color(0x9927E6A1)),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RushGreen, contentColor = Color(0xFF04170F))
            ) {
                Text("PLAY", fontSize = 19.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                Spacer(Modifier.width(12.dp))
                Text("▶", fontSize = 17.sp, fontWeight = FontWeight.Black)
            }

            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                androidx.compose.material3.OutlinedButton(
                    onClick = onProgressionClick,
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RushAmber.copy(alpha = 0.65f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RushAmber)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("★", fontSize = 16.sp)
                        Text("MISSIONS", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    }
                }
                androidx.compose.material3.OutlinedButton(
                    onClick = onStatisticsClick,
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RushMint.copy(alpha = 0.55f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RushMint)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("▥", fontSize = 16.sp)
                        Text("STATS", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            androidx.compose.material3.OutlinedButton(
                onClick = onSettingsClick,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(15.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, RushAmber.copy(alpha = 0.55f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = RushAmber)
            ) {
                Text("⚙  GAME SETTINGS", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp)
            }

            Spacer(Modifier.height(18.dp))
            Text("4 GAME MODES  •  COMBOS  •  POWERUPS  •  DAILY MISSIONS", color = Color.White.copy(alpha = 0.4f), fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun BrandMark() {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Text("R", color = Color(0xFF062116), fontSize = 22.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun TargetDot(modifier: Modifier = Modifier, color: Color) {
    Box(
        modifier = modifier
            .size(16.dp)
            .shadow(8.dp, CircleShape)
            .background(color, CircleShape)
            .border(2.dp, Color.White.copy(alpha = 0.45f), CircleShape)
    )
}

@Composable
private fun StatPreview(value: String, label: String, tint: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = tint, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.48f),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF080E19)
@Composable
private fun ReflexRushHomePreview() {
    ReflexRushTheme {
        Surface {
            ReflexRushHome()
        }
    }
}
