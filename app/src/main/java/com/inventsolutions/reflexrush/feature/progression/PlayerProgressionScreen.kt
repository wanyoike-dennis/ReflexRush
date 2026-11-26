package com.inventsolutions.reflexrush.feature.progression

import com.inventsolutions.reflexrush.core.ui.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventsolutions.reflexrush.game.DailyMission
import com.inventsolutions.reflexrush.game.PlayerAchievement
import com.inventsolutions.reflexrush.game.PlayerProgression
import com.inventsolutions.reflexrush.ui.theme.RushAmber
import com.inventsolutions.reflexrush.ui.theme.RushGreen
import com.inventsolutions.reflexrush.ui.theme.RushMint

@Composable
fun PlayerProgressionScreen(progression: PlayerProgression, onBack: () -> Unit) {
    val width = LocalConfiguration.current.screenWidthDp
    val inset = ((width - 900) / 2).coerceAtLeast(20).dp
    val xpFraction = if (progression.xpForNextLevel > 0) (progression.xpIntoLevel.toFloat() / progression.xpForNextLevel).coerceIn(0f, 1f) else 0f
    Box(Modifier.fillMaxSize()) {
        ArcadeBackdrop(Modifier.fillMaxSize(), RushMint, intensity = .4f)
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState())
                .padding(horizontal = inset, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ArcadeScreenHeader("PLAYER PROFILE", "ACHIEVEMENTS", onBack)
            Spacer(Modifier.height(18.dp))
            ArcadePanel(Modifier.fillMaxWidth(), RushGreen, 23.dp) {
                Column(Modifier.fillMaxWidth().padding(18.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("YOUR RANK", color = RushMint, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.8.sp)
                            Spacer(Modifier.height(3.dp))
                            Text("LEVEL ${progression.level}", color = Color.White, fontSize = 31.sp, fontWeight = FontWeight.Black)
                            Text("SHARPSHOOTER", color = RushGreen, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
                        }
                        Box(Modifier.size(66.dp).clip(CircleShape).background(RushGreen.copy(alpha = .13f)), contentAlignment = Alignment.Center) {
                            Text("★", color = RushAmber, fontSize = 37.sp)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${progression.experiencePoints} TOTAL XP", color = RushAmber, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Text("${(xpFraction * 100).toInt()}%", color = RushMint, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.fillMaxWidth().height(9.dp).clip(CircleShape).background(Color.White.copy(alpha = .10f))) {
                        Box(Modifier.fillMaxWidth(xpFraction).height(9.dp).clip(CircleShape).background(Brush.horizontalGradient(listOf(RushGreen, RushMint))))
                    }
                    Spacer(Modifier.height(7.dp))
                    Text("${progression.xpIntoLevel} / ${progression.xpForNextLevel} XP TO NEXT LEVEL", color = Color.White.copy(alpha = .52f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(22.dp))
            SectionHeading("DAILY MISSIONS", "Complete challenges to earn XP", RushAmber)
            DailyMission.entries.forEach { mission ->
                val progress = progression.missionProgress(mission)
                val complete = mission.id in progression.completedDailyMissionIds
                MissionCard(mission.title, mission.description, progress, mission.target, mission.xpReward, complete)
                Spacer(Modifier.height(9.dp))
            }
            Spacer(Modifier.height(14.dp))
            SectionHeading("ACHIEVEMENTS", "${progression.unlockedAchievements.size} of ${PlayerAchievement.entries.size} unlocked", RushMint)
            PlayerAchievement.entries.forEach { achievement ->
                RewardCard(
                    title = achievement.title,
                    description = achievement.description,
                    reward = "+${achievement.xpReward} XP",
                    unlocked = achievement.id in progression.unlockedAchievements
                )
                Spacer(Modifier.height(9.dp))
            }
            Spacer(Modifier.height(14.dp))
            SectionHeading("COSMETIC REWARDS", "Unlock target themes as you level up", RushAmber)
            listOf("Neon Lime" to 3, "Electric Violet" to 5, "Solar Gold" to 10).forEach { (name, level) ->
                RewardCard(name, "Target colour theme • Level $level", "LEVEL $level", name in progression.unlockedCosmetics)
                Spacer(Modifier.height(9.dp))
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun SectionHeading(title: String, subtitle: String, tint: Color) {
    Column(Modifier.fillMaxWidth()) {
        Text(title, color = tint, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.7.sp)
        Spacer(Modifier.height(3.dp))
        Text(subtitle, color = Color.White.copy(alpha = .48f), fontSize = 9.sp)
    }
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun MissionCard(title: String, description: String, progress: Int, target: Int, reward: Int, complete: Boolean) {
    val fraction = if (target > 0) (progress.toFloat() / target).coerceIn(0f, 1f) else 0f
    ArcadePanel(Modifier.fillMaxWidth(), if (complete) RushGreen else RushMint, 18.dp) {
        Column(Modifier.fillMaxWidth().padding(15.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(title, Modifier.weight(1f), color = if (complete) RushGreen else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(if (complete) "COMPLETED" else "+$reward XP", color = if (complete) RushGreen else RushAmber, fontSize = 8.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(5.dp))
            Text(description, color = Color.White.copy(alpha = .57f), fontSize = 9.sp, lineHeight = 13.sp)
            Spacer(Modifier.height(9.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("$progress / $target", color = RushMint, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(if (complete) "DONE" else "IN PROGRESS", color = if (complete) RushGreen else RushAmber, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))
            Box(Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(Color.White.copy(alpha = .09f))) {
                Box(Modifier.fillMaxWidth(fraction).height(6.dp).clip(CircleShape).background(Brush.horizontalGradient(listOf(RushGreen, RushMint))))
            }
        }
    }
}

@Composable
private fun RewardCard(title: String, description: String, reward: String, unlocked: Boolean) {
    ArcadePanel(Modifier.fillMaxWidth(), if (unlocked) RushGreen else RushMint, 17.dp) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(39.dp).clip(RoundedCornerShape(12.dp)).background(if (unlocked) RushGreen.copy(alpha = .14f) else Color.White.copy(alpha = .05f)), contentAlignment = Alignment.Center) {
                Text(if (unlocked) "✓" else "◇", color = if (unlocked) RushGreen else RushMint.copy(alpha = .58f), fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = if (unlocked) RushGreen else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(3.dp))
                Text(description, color = Color.White.copy(alpha = .52f), fontSize = 9.sp)
            }
            Spacer(Modifier.width(8.dp))
            Text(if (unlocked) "UNLOCKED" else reward, color = if (unlocked) RushGreen else RushAmber, fontSize = 8.sp, fontWeight = FontWeight.Black)
        }
    }
}
