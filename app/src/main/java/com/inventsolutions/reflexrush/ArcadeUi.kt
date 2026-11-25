package com.inventsolutions.reflexrush

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.inventsolutions.reflexrush.ui.theme.RushAmber
import com.inventsolutions.reflexrush.ui.theme.RushBackground
import com.inventsolutions.reflexrush.ui.theme.RushGreen
import com.inventsolutions.reflexrush.ui.theme.RushMint
import com.inventsolutions.reflexrush.ui.theme.RushRed
import com.inventsolutions.reflexrush.ui.theme.RushSurface

@Composable
fun ArcadeBackdrop(
    modifier: Modifier = Modifier,
    accent: Color = RushGreen
) {
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF050914), Color(0xFF0B1727), RushBackground)
                )
            )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val glowCenter = androidx.compose.ui.geometry.Offset(size.width * 0.82f, size.height * 0.16f)
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(accent.copy(alpha = 0.19f), Color.Transparent),
                    center = glowCenter,
                    radius = size.width * 0.72f
                ),
                radius = size.width * 0.72f,
                center = glowCenter
            )
            drawCircle(
                color = RushMint.copy(alpha = 0.055f),
                radius = size.minDimension * 0.39f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.12f, size.height * 0.72f),
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = accent.copy(alpha = 0.07f),
                radius = size.minDimension * 0.28f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.12f, size.height * 0.72f),
                style = Stroke(width = 1.dp.toPx())
            )
            val gridStep = 34.dp.toPx()
            var x = 0f
            while (x < size.width) {
                drawLine(Color.White.copy(alpha = 0.018f), androidx.compose.ui.geometry.Offset(x, size.height * 0.58f), androidx.compose.ui.geometry.Offset(x, size.height), 1.dp.toPx())
                x += gridStep
            }
            var y = size.height * 0.58f
            while (y < size.height) {
                drawLine(Color.White.copy(alpha = 0.018f), androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(size.width, y), 1.dp.toPx())
                y += gridStep
            }
        }
    }
}

@Composable
fun ArcadePanel(
    modifier: Modifier = Modifier,
    accent: Color = RushMint,
    radius: Dp = 22.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(radius)
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xEE17283D), Color(0xF20B1728))
                )
            )
            .border(1.dp, accent.copy(alpha = 0.34f), shape)
            .padding(1.dp),
        content = content
    )
}
