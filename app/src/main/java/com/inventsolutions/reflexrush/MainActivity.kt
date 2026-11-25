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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberSaveable
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
import com.inventsolutions.reflexrush.ui.theme.ReflexRushTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReflexRushTheme {
                var showGame by rememberSaveable { mutableStateOf(false) }
                BackHandler(enabled = showGame) {
                    showGame = false
                }
                if (showGame) {
                    GameScreen(
                        onHome = { showGame = false },
                        lifecycleOwner = this
                    )
                } else {
                    ReflexRushHome(onPlayClick = { showGame = true })
                }
            }
        }
    }
}

@Composable
fun ReflexRushHome(onPlayClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        Color(0xFF101B2C),
                        Color(0xFF080E19)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x3327E6A1), Color.Transparent),
                    center = Offset(size.width * 0.82f, size.height * 0.22f),
                    radius = size.width * 0.58f
                ),
                radius = size.width * 0.58f,
                center = Offset(size.width * 0.82f, size.height * 0.22f)
            )
            drawCircle(
                color = Color(0x1827E6A1),
                radius = size.width * 0.34f,
                center = Offset(size.width * 0.5f, size.height * 0.39f),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 26.dp, vertical = 28.dp),
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
                        Text(
                            "REFLEX",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.2.sp
                        )
                        Text(
                            "RUSH",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp
                        )
                    }
                }
                Text(
                    "ARCADE  •  01",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.48f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            }

            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier.size(220.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color(0x1427E6A1),
                        style = Stroke(width = 1.dp.toPx()),
                        radius = size.minDimension * 0.48f
                    )
                    drawCircle(
                        color = Color(0x2227E6A1),
                        style = Stroke(width = 1.dp.toPx()),
                        radius = size.minDimension * 0.36f
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color(0x5527E6A1), Color(0x0527E6A1))
                        ),
                        radius = size.minDimension * 0.29f
                    )
                }
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .shadow(26.dp, CircleShape, ambientColor = Color(0x6627E6A1))
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF43F5B2), Color(0xFF0DBB82))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "⚡",
                        fontSize = 58.sp,
                        textAlign = TextAlign.Center
                    )
                }
                TargetDot(
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 22.dp, end = 18.dp),
                    color = Color(0xFFFF5D69)
                )
                TargetDot(
                    modifier = Modifier.align(Alignment.BottomStart).padding(start = 20.dp, bottom = 25.dp),
                    color = Color(0xFF43F5B2)
                )
                TargetDot(
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 2.dp).size(12.dp),
                    color = Color(0xFFFFC45C)
                )
            }

            Spacer(Modifier.height(30.dp))

            Text(
                "TRUST YOUR REFLEXES.",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.58f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.6.sp
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Ready, set,\nREFLEX!",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 42.sp,
                lineHeight = 46.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Hit the good targets. Avoid the danger.\nHow sharp are your reactions?",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f),
                fontSize = 14.sp,
                lineHeight = 21.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(30.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF172438))
                    .border(1.dp, Color(0xFF26384D), RoundedCornerShape(18.dp))
                    .padding(vertical = 16.dp, horizontal = 18.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatPreview(value = "TAP", label = "THE TARGETS", tint = MaterialTheme.colorScheme.primary)
                Box(
                    Modifier
                        .width(1.dp)
                        .height(34.dp)
                        .background(Color(0xFF304056))
                )
                StatPreview(value = "DODGE", label = "THE RED ONES", tint = Color(0xFFFF6975))
                Box(
                    Modifier
                        .width(1.dp)
                        .height(34.dp)
                        .background(Color(0xFF304056))
                )
                StatPreview(value = "SCORE", label = "BIG", tint = Color(0xFFFFC45C))
            }

            Spacer(Modifier.height(22.dp))

            Button(
                onClick = onPlayClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .shadow(14.dp, RoundedCornerShape(18.dp), ambientColor = Color(0x5527E6A1)),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color(0xFF062116)
                )
            ) {
                Text(
                    "PLAY NOW",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.width(10.dp))
                Text("→", fontSize = 21.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "BEST SCORE",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.48f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "—",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.weight(1f))
            Text(
                "A QUICK GAME. A SHARPER MIND.",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.32f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.8.sp
            )
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
