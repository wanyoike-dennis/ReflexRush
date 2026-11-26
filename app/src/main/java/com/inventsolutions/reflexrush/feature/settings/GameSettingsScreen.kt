package com.inventsolutions.reflexrush.feature.settings

import com.inventsolutions.reflexrush.core.ui.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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
    val width = LocalConfiguration.current.screenWidthDp
    val inset = ((width - 760) / 2).coerceAtLeast(20).dp
    Box(Modifier.fillMaxSize()) {
        ArcadeBackdrop(Modifier.fillMaxSize(), RushAmber, intensity = .42f)
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState())
                .padding(horizontal = inset, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ArcadeScreenHeader("REFLEX RUSH", "GAME SETTINGS", onBack)
            Spacer(Modifier.height(22.dp))
            SettingsSectionHeader("SOUND & AUDIO", "Tune the arena to your preference", RushGreen)
            ArcadePanel(Modifier.fillMaxWidth(), RushGreen, 21.dp) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    AudioSettingRow("Sound effects", "Hits, combos, powerups and game events", soundEffectsEnabled, onSoundEffectsChange, RushGreen)
                    Spacer(Modifier.height(10.dp))
                    AudioSettingRow("Background music", "Ambient music while a round is active", musicEnabled, onMusicChange, RushAmber)
                }
            }
            Spacer(Modifier.height(22.dp))
            SettingsSectionHeader("BACKUP & RESTORE", "Keep your progress safe when changing devices", RushAmber)
            ArcadePanel(Modifier.fillMaxWidth(), RushAmber, 21.dp) {
                Column(Modifier.fillMaxWidth().padding(17.dp)) {
                    Text("YOUR GAME DATA", color = RushMint, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.3.sp)
                    Spacer(Modifier.height(7.dp))
                    Text(
                        "Export scores, statistics, missions, achievements, XP and audio settings to a backup file. Restore a previous backup on this device.",
                        color = Color.White.copy(alpha = .65f), fontSize = 10.sp, lineHeight = 15.sp
                    )
                    Spacer(Modifier.height(15.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = onExportBackup, modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(13.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RushGreen, contentColor = Color(0xFF04251B))
                        ) { Text("EXPORT BACKUP", fontSize = 9.sp, fontWeight = FontWeight.Black) }
                        OutlinedButton(
                            onClick = onImportBackup, modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(13.dp)
                        ) { Text("RESTORE", fontSize = 10.sp, fontWeight = FontWeight.Black, color = RushMint) }
                    }
                    if (!backupStatus.isNullOrBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("●", color = RushGreen, fontSize = 10.sp)
                            Spacer(Modifier.width(7.dp))
                            Text(backupStatus, color = RushMint, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(13.dp))
            Text("Restoring replaces the current local game progress. Export a backup first if you want to keep it.", Modifier.fillMaxWidth(), color = Color.White.copy(alpha = .42f), fontSize = 9.sp, lineHeight = 14.sp)
            Spacer(Modifier.height(19.dp))
            Text("TRUST YOUR REFLEXES.", color = RushMint.copy(alpha = .42f), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
            Spacer(Modifier.height(15.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String, subtitle: String, tint: Color) {
    Column(Modifier.fillMaxWidth()) {
        Text(title, color = tint, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.7.sp)
        Spacer(Modifier.height(3.dp))
        Text(subtitle, color = Color.White.copy(alpha = .48f), fontSize = 10.sp)
    }
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun AudioSettingRow(title: String, subtitle: String, enabled: Boolean, onChange: (Boolean) -> Unit, accent: Color) {
    Row(
        Modifier.fillMaxWidth().background(Color(0xCC0A1728), RoundedCornerShape(16.dp)).padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = if (enabled) accent else RushMint, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = Color.White.copy(alpha = .53f), fontSize = 9.sp, lineHeight = 13.sp)
            Spacer(Modifier.height(4.dp))
            Text(if (enabled) "ON" else "OFF", color = if (enabled) accent else RushMint.copy(alpha = .42f), fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }
        Switch(checked = enabled, onCheckedChange = onChange)
    }
}
