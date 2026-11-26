package com.inventsolutions.reflexrush.feature.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import com.inventsolutions.reflexrush.game.GameStatus
import com.inventsolutions.reflexrush.game.GameTarget
import com.inventsolutions.reflexrush.ui.theme.RushGreen
import com.inventsolutions.reflexrush.ui.theme.RushMint
import com.inventsolutions.reflexrush.ui.theme.RushSurface

/** Renders the live playfield and forwards target taps to the active session. */
@Composable
internal fun GameArenaSurface(
    modifier: Modifier,
    targets: List<GameTarget>,
    status: GameStatus,
    isTabletLayout: Boolean,
    feedbackMessage: String?,
    feedbackColor: Color,
    feedbackAlpha: Float,
    onTargetTap: (Long) -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF071322), Color(0xFF0B1B2D), Color(0xFF07111F))
                )
            )
            .border(1.5.dp, RushGreen.copy(alpha = 0.48f), RoundedCornerShape(26.dp))
    ) {
        GameArenaBackground()
        val targetSize = if (isTabletLayout) 78.dp else 66.dp
        targets.forEach { target ->
            key(target.id) {
                GameTargetButton(
                    target = target,
                    modifier = Modifier.offset(
                        x = (maxWidth * target.x - targetSize / 2).coerceIn(0.dp, maxWidth - targetSize),
                        y = (maxHeight * target.y - targetSize / 2).coerceIn(0.dp, maxHeight - targetSize)
                    ),
                    onClick = { onTargetTap(target.id) }
                )
            }
        }

        if (feedbackMessage != null) {
            Text(
                text = feedbackMessage,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 18.dp)
                    .graphicsLayer {
                        alpha = feedbackAlpha
                        translationY = (1f - feedbackAlpha) * 10.dp.toPx()
                    }
                    .clip(RoundedCornerShape(14.dp))
                    .background(RushSurface.copy(alpha = 0.94f))
                    .border(1.dp, feedbackColor.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 18.dp, vertical = 9.dp),
                color = feedbackColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp
            )
        }

        if (targets.isEmpty() && status == GameStatus.RUNNING) {
            Text(
                "GET READY…",
                modifier = Modifier.align(Alignment.Center),
                color = RushMint.copy(alpha = 0.35f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
    }
}
