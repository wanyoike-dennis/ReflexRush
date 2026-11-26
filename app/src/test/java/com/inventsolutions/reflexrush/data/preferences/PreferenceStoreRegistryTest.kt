package com.inventsolutions.reflexrush.data.preferences

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PreferenceStoreRegistryTest {
    @Test
    fun backupIncludesEveryExistingStoreExactlyOnce() {
        assertEquals(
            listOf(
                "reflex_rush_preferences",
                "reflex_rush_player_statistics",
                "reflex_rush_player_progression",
                "reflex_rush_audio"
            ),
            PreferenceStoreRegistry.backupStoreNames
        )
        assertEquals(
            PreferenceStoreRegistry.backupStoreNames.size,
            PreferenceStoreRegistry.backupStoreNames.toSet().size
        )
    }

    @Test
    fun storeIdentifiersRemainStableAndNonBlank() {
        assertTrue(PreferenceStoreRegistry.backupStoreNames.all { it.isNotBlank() })
    }
}
