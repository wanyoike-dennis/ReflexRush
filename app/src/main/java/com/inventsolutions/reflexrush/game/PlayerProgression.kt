package com.inventsolutions.reflexrush.game

/** Stable identifiers for the daily challenges shown to the player. */
enum class DailyMission(val id: String, val title: String, val description: String, val target: Int, val xpReward: Int) {
    FIRST_STRIKE("first_strike", "First Strike", "Hit 20 good targets today", 20, 50),
    COMBO_MASTER("combo_master", "Combo Master", "Reach a 5-hit combo in a round", 5, 50),
    DANGER_DODGER("danger_dodger", "Danger Dodger", "Finish a round without tapping a danger target", 1, 50),
    SPEED_DEMON("speed_demon", "Speed Demon", "Score 250 in Time Attack", 250, 50),
    SURVIVOR("survivor", "Survivor", "Reach Level 4 in Classic", 4, 50),
    POWER_COLLECTOR("power_collector", "Power Collector", "Collect 5 powerups today", 5, 50)
}

/** Permanent milestones. Each achievement can award XP only once. */
enum class PlayerAchievement(val id: String, val title: String, val description: String, val xpReward: Int) {
    FIRST_ROUND("first_round", "Getting Started", "Complete your first round", 100),
    CENTURY("century", "Century Club", "Hit 100 good targets", 100),
    COMBO_TEN("combo_ten", "Unstoppable", "Reach a 10-hit combo", 100),
    MODE_EXPLORER("mode_explorer", "Mode Explorer", "Play every game mode", 150),
    POWERUP_EXPERT("powerup_expert", "Powerup Expert", "Collect 25 powerups", 100),
    HIGH_SCORER("high_scorer", "High Scorer", "Score 1,000 points in a round", 150)
}

/** Lifetime progress plus the current local day's mission counters. */
data class PlayerProgression(
    val experiencePoints: Int = 0,
    val totalGamesPlayed: Int = 0,
    val totalGoodTargetsHit: Int = 0,
    val totalPowerupsCollected: Int = 0,
    val bestCombo: Int = 0,
    val highestScore: Int = 0,
    val playedModes: Set<GameMode> = emptySet(),
    val unlockedAchievements: Set<String> = emptySet(),
    val dailyDateKey: String = "",
    val dailyGoodTargetsHit: Int = 0,
    val dailyPowerupsCollected: Int = 0,
    val dailyBestCombo: Int = 0,
    val dailyDangerFreeRounds: Int = 0,
    val dailyBestTimeAttackScore: Int = 0,
    val dailyBestClassicLevel: Int = 0,
    val completedDailyMissionIds: Set<String> = emptySet()
) {
    val level: Int get() = experiencePoints / XP_PER_LEVEL + 1
    val xpIntoLevel: Int get() = experiencePoints % XP_PER_LEVEL
    val xpForNextLevel: Int get() = XP_PER_LEVEL

    val unlockedCosmetics: Set<String>
        get() = buildSet {
            if (level >= 3) add("Neon Lime")
            if (level >= 5) add("Electric Violet")
            if (level >= 10) add("Solar Gold")
        }

    fun include(summary: GameSessionSummary, todayKey: String): PlayerProgression {
        require(todayKey.isNotBlank()) { "A local date key is required." }
        val dayBase = if (dailyDateKey == todayKey) this else copy(
            dailyDateKey = todayKey,
            dailyGoodTargetsHit = 0,
            dailyPowerupsCollected = 0,
            dailyBestCombo = 0,
            dailyDangerFreeRounds = 0,
            dailyBestTimeAttackScore = 0,
            dailyBestClassicLevel = 0,
            completedDailyMissionIds = emptySet()
        )
        val updated = dayBase.copy(
            totalGamesPlayed = dayBase.totalGamesPlayed + 1,
            totalGoodTargetsHit = dayBase.totalGoodTargetsHit + summary.goodTargetsHit,
            totalPowerupsCollected = dayBase.totalPowerupsCollected + summary.powerupsCollected,
            bestCombo = maxOf(dayBase.bestCombo, summary.bestCombo),
            highestScore = maxOf(dayBase.highestScore, summary.finalScore),
            playedModes = dayBase.playedModes + summary.mode,
            dailyGoodTargetsHit = dayBase.dailyGoodTargetsHit + summary.goodTargetsHit,
            dailyPowerupsCollected = dayBase.dailyPowerupsCollected + summary.powerupsCollected,
            dailyBestCombo = maxOf(dayBase.dailyBestCombo, summary.bestCombo),
            dailyDangerFreeRounds = dayBase.dailyDangerFreeRounds + if (summary.dangerTargetsHit == 0) 1 else 0,
            dailyBestTimeAttackScore = maxOf(dayBase.dailyBestTimeAttackScore, if (summary.mode == GameMode.TIME_ATTACK) summary.finalScore else 0),
            dailyBestClassicLevel = maxOf(dayBase.dailyBestClassicLevel, if (summary.mode == GameMode.CLASSIC) summary.levelReached else 0)
        )

        val newlyCompletedMissions = DailyMission.entries.filter { mission ->
            mission.id !in updated.completedDailyMissionIds && updated.missionProgress(mission, summary) >= mission.target
        }.map { it.id }.toSet()
        val completed = updated.completedDailyMissionIds + newlyCompletedMissions

        val newlyUnlocked = PlayerAchievement.entries.filter { achievement ->
            achievement.id !in updated.unlockedAchievements && updated.meets(achievement)
        }.map { it.id }.toSet()

        val earnedXp = DailyMission.entries.filter { it.id in newlyCompletedMissions }.sumOf { it.xpReward } +
            PlayerAchievement.entries.filter { it.id in newlyUnlocked }.sumOf { it.xpReward }

        return updated.copy(
            experiencePoints = updated.experiencePoints + earnedXp,
            completedDailyMissionIds = completed,
            unlockedAchievements = updated.unlockedAchievements + newlyUnlocked
        )
    }

    fun missionProgress(mission: DailyMission): Int {
        if (mission.id in completedDailyMissionIds) return mission.target
        return when (mission) {
        DailyMission.FIRST_STRIKE -> dailyGoodTargetsHit
        DailyMission.COMBO_MASTER -> dailyBestCombo
        DailyMission.DANGER_DODGER -> dailyDangerFreeRounds
        DailyMission.SPEED_DEMON -> dailyBestTimeAttackScore
        DailyMission.SURVIVOR -> dailyBestClassicLevel
        DailyMission.POWER_COLLECTOR -> dailyPowerupsCollected
        }.coerceAtMost(mission.target)
    }

    private fun meets(achievement: PlayerAchievement): Boolean = when (achievement) {
        PlayerAchievement.FIRST_ROUND -> totalGamesPlayed >= 1
        PlayerAchievement.CENTURY -> totalGoodTargetsHit >= 100
        PlayerAchievement.COMBO_TEN -> bestCombo >= 10
        PlayerAchievement.MODE_EXPLORER -> playedModes.containsAll(GameMode.entries.toSet())
        PlayerAchievement.POWERUP_EXPERT -> totalPowerupsCollected >= 25
        PlayerAchievement.HIGH_SCORER -> highestScore >= 1_000
    }

    companion object {
        const val XP_PER_LEVEL = 250
    }
}
