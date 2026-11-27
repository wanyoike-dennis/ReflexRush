package com.inventsolutions.reflexrush.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventsolutions.reflexrush.ui.theme.RushBackground
import com.inventsolutions.reflexrush.ui.theme.RushSurfaceVariant
import com.inventsolutions.reflexrush.ui.theme.RushSurface
import com.inventsolutions.reflexrush.ui.theme.RushRed
import com.inventsolutions.reflexrush.ui.theme.RushAmber
import com.inventsolutions.reflexrush.ui.theme.RushGreen
import com.inventsolutions.reflexrush.ui.theme.RushMint

/**
 * Shared Reflex Rush grid-and-ring background.
 * Intensity controls decorative glow only; it does not affect gameplay rendering.
 */
fun Modifier.arcadeBackdrop(
    accent: Color = RushGreen,
    intensity: Float = 1f
): Modifier = background(
    Brush.verticalGradient(
        listOf(Color(0xFF050914), Color(0xFF0B1727), RushBackground)
    )
).drawBehind {
    val strength = intensity.coerceIn(0f, 2f)
    val glowCenter = Offset(size.width * 0.82f, size.height * 0.18f)
    val glowRadius = size.width * 0.76f
    drawCircle(
        brush = Brush.radialGradient(
            listOf(accent.copy(alpha = 0.17f * strength), Color.Transparent),
            center = glowCenter,
            radius = glowRadius
        ),
        radius = glowRadius,
        center = glowCenter
    )

    val ringCenter = Offset(size.width * 0.12f, size.height * 0.72f)
    listOf(0.43f, 0.32f, 0.21f).forEachIndexed { index, fraction ->
        drawCircle(
            color = (if (index == 1) accent else RushMint).copy(
                alpha = (0.045f + index * 0.012f) * strength
            ),
            radius = size.minDimension * fraction,
            center = ringCenter,
            style = Stroke(width = (1f + index * 0.35f).dp.toPx())
        )
    }

    val gridStep = 34.dp.toPx()
    var x = 0f
    while (x < size.width) {
        drawLine(
            Color.White.copy(alpha = 0.018f * strength),
            Offset(x, 0f),
            Offset(x, size.height),
            1.dp.toPx()
        )
        x += gridStep
    }
    var y = 0f
    while (y < size.height) {
        drawLine(
            Color.White.copy(alpha = 0.018f * strength),
            Offset(0f, y),
            Offset(size.width, y),
            1.dp.toPx()
        )
        y += gridStep
    }
}

@Composable
fun ArcadeBackdrop(
    modifier: Modifier = Modifier,
    accent: Color = RushGreen,
    intensity: Float = 1f
) {
    Box(modifier = modifier.arcadeBackdrop(accent, intensity))
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


/**
 * Shared navigation header for secondary screens.
 * The back action is an icon-only control, never a decorative card.
 */
@Composable
fun ArcadeScreenHeader(
    eyebrow: String,
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(48.dp).semantics {
                contentDescription = "Go back"
            }
        ) {
            Text(
                text = "‹",
                color = RushMint,
                fontSize = 38.sp,
                fontWeight = FontWeight.Normal
            )
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = eyebrow,
                color = RushMint,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Text(
                text = title,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = .4.sp
            )
        }
    }
}
