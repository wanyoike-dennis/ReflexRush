package com.inventsolutions.reflexrush.data.preferences

/**
 * Canonical names for the app's local SharedPreferences stores.
 *
 * Keep these identifiers stable: changing them would make existing player data
 * appear to be missing after an upgrade.
 */
object PreferenceStoreRegistry {
    const val HIGH_SCORES = "reflex_rush_preferences"
    const val STATISTICS = "reflex_rush_player_statistics"
    const val PROGRESSION = "reflex_rush_player_progression"
    const val AUDIO = "reflex_rush_audio"

    /** Stores included in manual export/import, in a stable order. */
    val backupStoreNames: List<String> = listOf(
        HIGH_SCORES,
        STATISTICS,
        PROGRESSION,
        AUDIO
    )
}
