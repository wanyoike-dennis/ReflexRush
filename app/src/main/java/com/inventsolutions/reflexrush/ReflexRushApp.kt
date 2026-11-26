package com.inventsolutions.reflexrush

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LifecycleOwner
import com.inventsolutions.reflexrush.game.GameMode
import com.inventsolutions.reflexrush.ui.theme.ReflexRushTheme

@Composable
fun ReflexRushApp(lifecycleOwner: LifecycleOwner) {
    ReflexRushTheme {
        val navigation = remember { AppNavigationState() }
        val appContext = LocalContext.current.applicationContext
        val highScoreStore = remember { HighScoreStore(appContext) }
        var bestScores by remember {
            mutableStateOf(GameMode.entries.associateWith { highScoreStore.load(it) })
        }
        val audioSettingsStore = remember { AudioSettingsStore(appContext) }
        val backupManager = remember { GameBackupManager(appContext) }
        val progressionStore = remember { PlayerProgressionStore(appContext) }
        val statisticsStore = remember { PlayerStatisticsStore(appContext) }

        var backupStatus by remember { mutableStateOf<String?>(null) }
        var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }
        var showRestoreConfirmation by remember { mutableStateOf(false) }
        var soundEffectsEnabled by remember { mutableStateOf(audioSettingsStore.soundEffectsEnabled) }
        var musicEnabled by remember { mutableStateOf(audioSettingsStore.musicEnabled) }
        var playerProgression by remember { mutableStateOf(progressionStore.load()) }
        var playerStatistics by remember { mutableStateOf(statisticsStore.load()) }

        val exportBackupLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("application/json")
        ) { uri ->
            if (uri != null) {
                backupStatus = runCatching {
                    backupManager.exportTo(uri)
                    "Backup exported successfully."
                }.getOrElse { error ->
                    "Backup failed: ${error.localizedMessage ?: "Unknown error"}"
                }
            }
        }
        val importBackupLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri != null) {
                pendingRestoreUri = uri
                showRestoreConfirmation = true
            }
        }

        if (showRestoreConfirmation && pendingRestoreUri != null) {
            AlertDialog(
                onDismissRequest = {
                    showRestoreConfirmation = false
                    pendingRestoreUri = null
                },
                title = { Text("Restore game backup?") },
                text = {
                    Text("Restoring replaces the current scores, statistics, missions, achievements, XP and audio settings on this device. Export a backup of your current progress first if you want to keep it.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val selectedBackup = pendingRestoreUri
                            showRestoreConfirmation = false
                            pendingRestoreUri = null
                            if (selectedBackup != null) {
                                runCatching { backupManager.importFrom(selectedBackup) }
                                    .onSuccess {
                                        bestScores = GameMode.entries.associateWith { highScoreStore.load(it) }
                                        playerStatistics = statisticsStore.load()
                                        playerProgression = progressionStore.load()
                                        soundEffectsEnabled = audioSettingsStore.soundEffectsEnabled
                                        musicEnabled = audioSettingsStore.musicEnabled
                                        backupStatus = "Backup restored successfully."
                                    }
                                    .onFailure { error ->
                                        backupStatus = "Restore failed: ${error.localizedMessage ?: "Unknown error"}"
                                    }
                            }
                        }
                    ) { Text("RESTORE") }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showRestoreConfirmation = false
                            pendingRestoreUri = null
                        }
                    ) { Text("CANCEL") }
                }
            )
        }

        BackHandler(enabled = navigation.destination != AppDestination.HOME) {
            navigation.goHome()
        }

        when (navigation.destination) {
            AppDestination.GAME -> GameScreen(
                onHome = navigation::goHome,
                lifecycleOwner = lifecycleOwner,
                bestScore = bestScores[navigation.selectedMode] ?: 0,
                onNewBestScore = { score ->
                    val saved = highScoreStore.saveIfHigher(score, navigation.selectedMode)
                    bestScores = bestScores + (navigation.selectedMode to saved)
                },
                mode = navigation.selectedMode,
                onSessionCompleted = { summary ->
                    playerStatistics = statisticsStore.record(summary)
                    playerProgression = progressionStore.record(summary)
                }
            )

            AppDestination.SETTINGS -> GameSettingsScreen(
                soundEffectsEnabled = soundEffectsEnabled,
                musicEnabled = musicEnabled,
                backupStatus = backupStatus,
                onExportBackup = { exportBackupLauncher.launch("reflex-rush-backup.json") },
                onImportBackup = { importBackupLauncher.launch(arrayOf("application/json", "text/json")) },
                onSoundEffectsChange = { enabled ->
                    audioSettingsStore.setSoundEffectsEnabled(enabled)
                    soundEffectsEnabled = enabled
                },
                onMusicChange = { enabled ->
                    audioSettingsStore.setMusicEnabled(enabled)
                    musicEnabled = enabled
                },
                onBack = navigation::goHome
            )

            AppDestination.PROGRESSION -> PlayerProgressionScreen(
                progression = playerProgression,
                onBack = navigation::goHome
            )

            AppDestination.STATISTICS -> PlayerStatisticsScreen(
                statistics = playerStatistics,
                onBack = navigation::goHome
            )

            AppDestination.MODE_SELECTION -> GameModeSelectionScreen(
                onModeSelected = navigation::startGame,
                onBack = navigation::goHome
            )

            AppDestination.HOME -> ReflexRushHome(
                onPlayClick = { navigation.navigateTo(AppDestination.MODE_SELECTION) },
                onStatisticsClick = {
                    playerStatistics = statisticsStore.load()
                    navigation.navigateTo(AppDestination.STATISTICS)
                },
                onProgressionClick = {
                    playerProgression = progressionStore.load()
                    navigation.navigateTo(AppDestination.PROGRESSION)
                },
                onSettingsClick = { navigation.navigateTo(AppDestination.SETTINGS) },
                bestScore = bestScores[GameMode.CLASSIC] ?: 0
            )
        }
    }
}
