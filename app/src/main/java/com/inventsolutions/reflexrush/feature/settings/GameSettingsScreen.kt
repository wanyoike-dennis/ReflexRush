package com.inventsolutions.reflexrush.feature.settings

import com.inventsolutions.reflexrush.core.ui.*

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventsolutions.reflexrush.ui.theme.RushAmber
import com.inventsolutions.reflexrush.ui.theme.RushGreen
import com.inventsolutions.reflexrush.ui.theme.RushMint

@Composable
fun GameSettingsScreen(
    soundEffectsEnabled: Boolean,
    musicEnabled: Boolean,
    backupStatus: String?,
    onSoundEffectsChange: (Boolean) -> Unit,
    onMusicChange: (Boolean) -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    onBack: () -> Unit
) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val horizontalInset = ((screenWidthDp - 760) / 2).coerceAtLeast(20).dp
    Box(Modifier.fillMaxSize()) {
        ArcadeBackdrop(Modifier.fillMaxSize(), RushAmber)
        Column(
            modifier = Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(horizontal = horizontalInset, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("REFLEX RUSH", color = RushMint, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    Text("GAME SETTINGS", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }
                OutlinedButton(onClick = onBack) { Text("BACK") }
            }
            Spacer(Modifier.height(24.dp))
            ArcadePanel(modifier = Modifier.fillMaxWidth(), accent = RushGreen, radius = 20.dp) {
                Column(Modifier.fillMaxWidth().padding(18.dp)) {
                    Text("AUDIO", color = RushAmber, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Choose the sounds you want during gameplay. Your choices are saved on this device.",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                        fontSize = 11.sp
                    )
                    Spacer(Modifier.height(18.dp))
                    AudioSettingRow(
                        title = "Sound effects",
                        subtitle = "Hits, combos, powerups and game events",
                        enabled = soundEffectsEnabled,
                        onChange = onSoundEffectsChange,
                        accent = RushGreen
                    )
                    Spacer(Modifier.height(12.dp))
                    AudioSettingRow(
                        title = "Background music",
                        subtitle = "Ambient music while a round is active",
                        enabled = musicEnabled,
                        onChange = onMusicChange,
                        accent = RushAmber
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            ArcadePanel(modifier = Modifier.fillMaxWidth(), accent = RushAmber, radius = 20.dp) {
                Column(Modifier.fillMaxWidth().padding(18.dp)) {
                    Text("BACKUP & RESTORE", color = RushAmber, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Save your scores, statistics, missions, achievements, XP and audio settings to a file, or restore them on this device.",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                        fontSize = 11.sp
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(onClick = onExportBackup, modifier = Modifier.weight(1f)) {
                            Text("EXPORT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(onClick = onImportBackup, modifier = Modifier.weight(1f)) {
                            Text("RESTORE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (!backupStatus.isNullOrBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            backupStatus,
                            color = RushMint,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "Android automatic backup is also available when enabled on your device.",
                color = RushMint.copy(alpha = 0.6f),
                fontSize = 10.sp
            )
            Spacer(Modifier.height(22.dp))
            Text("TRUST YOUR REFLEXES.", color = RushMint.copy(alpha = 0.42f), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
        }
    }
}

@Composable
private fun AudioSettingRow(
    title: String,
    subtitle: String,
    enabled: Boolean,
    onChange: (Boolean) -> Unit,
    accent: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(Color(0xCC0A1728), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = if (enabled) accent else RushMint, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f), fontSize = 10.sp)
            Text(if (enabled) "ON" else "OFF", color = if (enabled) accent else RushMint.copy(alpha = 0.45f), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
        }
        Switch(checked = enabled, onCheckedChange = onChange)
    }
}
