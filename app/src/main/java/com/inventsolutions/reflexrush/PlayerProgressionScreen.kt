package com.inventsolutions.reflexrush

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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventsolutions.reflexrush.game.DailyMission
import com.inventsolutions.reflexrush.game.PlayerAchievement
import com.inventsolutions.reflexrush.game.PlayerProgression
import com.inventsolutions.reflexrush.ui.theme.RushAmber
import com.inventsolutions.reflexrush.ui.theme.RushBackground
import com.inventsolutions.reflexrush.ui.theme.RushGreen
import com.inventsolutions.reflexrush.ui.theme.RushMint
import com.inventsolutions.reflexrush.ui.theme.RushRed
import com.inventsolutions.reflexrush.ui.theme.RushSurface
import com.inventsolutions.reflexrush.ui.theme.RushSurfaceVariant

@Composable
fun PlayerProgressionScreen(
    progression: PlayerProgression,
    onBack: () -> Unit
) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val horizontalInset = ((screenWidthDp - 900) / 2).coerceAtLeast(20).dp
    Column(
        modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF050914), Color(0xFF0B1727), RushBackground)))
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState()).padding(horizontal = horizontalInset, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("PLAYER PROGRESSION", color = RushMint, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Text("QUEST BOARD", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
            }
            OutlinedButton(onClick = onBack) { Text("BACK") }
        }
        Spacer(Modifier.height(18.dp))
        ProgressPanel {
            Text("LEVEL ${progression.level}", color = RushGreen, fontSize = 32.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Text("${progression.experiencePoints} XP  •  ${progression.xpIntoLevel}/${progression.xpForNextLevel} XP TO NEXT LEVEL",
                color = RushMint, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Complete missions to earn XP. Lifetime progress is saved on this device.",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f), fontSize = 11.sp)
        }
        Spacer(Modifier.height(18.dp))
        SectionTitle("⚡  DAILY MISSIONS", "New challenges every day")
        DailyMission.entries.forEach { mission ->
            val progress = progression.missionProgress(mission)
            val complete = mission.id in progression.completedDailyMissionIds
            ProgressItem(
                title = mission.title,
                description = mission.description,
                progress = progress,
                target = mission.target,
                reward = "+${mission.xpReward} XP",
                complete = complete
            )
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(12.dp))
        SectionTitle("🏆  ACHIEVEMENTS", "${progression.unlockedAchievements.size}/${PlayerAchievement.entries.size} unlocked")
        PlayerAchievement.entries.forEach { achievement ->
            val unlocked = achievement.id in progression.unlockedAchievements
            AchievementItem(
                title = achievement.title,
                description = achievement.description,
                reward = "+${achievement.xpReward} XP",
                unlocked = unlocked
            )
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(12.dp))
        SectionTitle("✦  COSMETIC REWARDS", "Unlock target themes as you level up")
        val cosmetics = listOf("Neon Lime" to 3, "Electric Violet" to 5, "Solar Gold" to 10)
        cosmetics.forEach { (name, level) ->
            AchievementItem(
                title = name,
                description = "Target colour theme • Level $level",
                reward = "LEVEL $level",
                unlocked = name in progression.unlockedCosmetics
            )
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun ProgressPanel(content: @Composable () -> Unit) {
    ArcadePanel(modifier = Modifier.fillMaxWidth(), accent = RushMint, radius = 20.dp) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) { content() }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, color = RushAmber, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
        Text(subtitle, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.48f), fontSize = 10.sp)
    }
    Spacer(Modifier.height(9.dp))
}

@Composable
private fun ProgressItem(
    title: String,
    description: String,
    progress: Int,
    target: Int,
    reward: String,
    complete: Boolean
) {
    ProgressPanel {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = if (complete) RushGreen else RushMint, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(if (complete) "COMPLETED" else reward, color = if (complete) RushGreen else RushAmber, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.height(5.dp))
        Text(description, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f), fontSize = 10.sp)
        Spacer(Modifier.height(7.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("$progress / $target", color = RushMint, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(if (complete) "DONE" else "IN PROGRESS", color = if (complete) RushGreen else RushAmber, fontSize = 9.sp)
        }
        Spacer(Modifier.height(7.dp))
        val progressFraction = if (target > 0) (progress.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 0f
        Box(
            Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.09f))
        ) {
            Box(
                Modifier.fillMaxWidth(progressFraction).height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Brush.horizontalGradient(listOf(RushGreen, RushMint)))
            )
        }
    }
}

@Composable
private fun AchievementItem(title: String, description: String, reward: String, unlocked: Boolean) {
    ProgressPanel {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = if (unlocked) RushGreen else RushMint, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(if (unlocked) "UNLOCKED" else reward, color = if (unlocked) RushGreen else RushAmber, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.height(5.dp))
        Text(description, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f), fontSize = 10.sp)
    }
}
