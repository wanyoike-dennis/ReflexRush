package com.inventsolutions.reflexrush.data.backup

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/**
 * Exports and restores the app's offline player data through the Android document picker.
 *
 * The backup format is versioned and deliberately limited to the app's four known
 * SharedPreferences files. Import validates the complete document before replacing data.
 */
class GameBackupManager(context: Context) {
    private val appContext = context.applicationContext
    private val preferenceFiles = listOf(
        "reflex_rush_preferences",
        "reflex_rush_player_statistics",
        "reflex_rush_player_progression",
        "reflex_rush_audio"
    )

    fun exportTo(uri: Uri) {
        val allPreferences = JSONObject()
        preferenceFiles.forEach { name ->
            val entries = JSONObject()
            appContext.getSharedPreferences(name, Context.MODE_PRIVATE)
                .all
                .toSortedMap()
                .forEach { (key, value) ->
                    entries.put(
                        key,
                        encodeValue(requireNotNull(value) { "A stored preference value cannot be null." })
                    )
                }
            allPreferences.put(name, entries)
        }

        val backup = JSONObject()
            .put("formatVersion", FORMAT_VERSION)
            .put("applicationId", APPLICATION_ID)
            .put("exportedAtEpochMs", System.currentTimeMillis())
            .put("preferences", allPreferences)

        val output = appContext.contentResolver.openOutputStream(uri)
            ?: throw IOException("Unable to open the selected backup destination.")
        output.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write(backup.toString(2))
        }
    }

    fun importFrom(uri: Uri) {
        val input = appContext.contentResolver.openInputStream(uri)
            ?: throw IOException("Unable to open the selected backup file.")
        val jsonText = input.bufferedReader(Charsets.UTF_8).use { it.readText() }
        if (jsonText.length > MAX_BACKUP_CHARACTERS) {
            throw IllegalArgumentException("The selected backup file is too large.")
        }

        val root = try {
            JSONObject(jsonText)
        } catch (exception: Exception) {
            throw IllegalArgumentException("This is not a valid Reflex Rush backup file.", exception)
        }

        require(root.optInt("formatVersion", -1) == FORMAT_VERSION) {
            "This backup uses an unsupported format version."
        }
        require(root.optString("applicationId") == APPLICATION_ID) {
            "This backup was not created by Reflex Rush."
        }

        val sourcePreferences = root.optJSONObject("preferences")
            ?: throw IllegalArgumentException("The backup does not contain player data.")

        // Decode every preference file before changing anything on the device.
        val decoded = preferenceFiles.associateWith { name ->
            val source = sourcePreferences.optJSONObject(name)
                ?: throw IllegalArgumentException("The backup is missing required data: $name.")
            decodeEntries(source)
        }

        val previous = preferenceFiles.associateWith { name ->
            appContext.getSharedPreferences(name, Context.MODE_PRIVATE).all
                .mapNotNull { (key, value) ->
                    value?.let { stored ->
                        key to if (stored is Set<*>) stored.toSet() else stored
                    }
                }
                .toMap()
        }

        try {
            decoded.forEach { (name, values) -> replacePreferences(name, values) }
        } catch (exception: Exception) {
            // Best-effort rollback if a device reports a preferences write failure.
            previous.forEach { (name, values) ->
                runCatching { replacePreferences(name, values) }
            }
            throw IOException("Could not restore the backup. Existing data was preserved where possible.", exception)
        }
    }

    private fun encodeValue(value: Any): JSONObject = when (value) {
        is Int -> JSONObject().put("type", "int").put("value", value)
        is Long -> JSONObject().put("type", "long").put("value", value)
        is Boolean -> JSONObject().put("type", "boolean").put("value", value)
        is Float -> JSONObject().put("type", "float").put("value", value.toDouble())
        is String -> JSONObject().put("type", "string").put("value", value)
        is Set<*> -> {
            require(value.all { it is String }) { "Unsupported preference set value." }
            val array = JSONArray()
            value.filterIsInstance<String>().sorted().forEach { item -> array.put(item) }
            JSONObject().put("type", "string_set").put("value", array)
        }
        else -> throw IllegalArgumentException("A stored value cannot be included in the backup.")
    }

    private fun decodeEntries(source: JSONObject): Map<String, Any> {
        require(source.length() <= MAX_ENTRIES_PER_FILE) { "The backup contains too many entries." }
        val decoded = linkedMapOf<String, Any>()
        val keys = source.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            require(key.isNotBlank()) { "The backup contains an invalid preference key." }
            val encoded = source.optJSONObject(key)
                ?: throw IllegalArgumentException("The backup contains an invalid preference value.")
            val value = encoded.opt("value")
            val decodedValue: Any = when (encoded.optString("type")) {
                "int" -> (value as? Number)?.toInt()
                    ?: throw IllegalArgumentException("Invalid integer value in backup.")
                "long" -> (value as? Number)?.toLong()
                    ?: throw IllegalArgumentException("Invalid long value in backup.")
                "boolean" -> value as? Boolean
                    ?: throw IllegalArgumentException("Invalid boolean value in backup.")
                "float" -> (value as? Number)?.toFloat()
                    ?: throw IllegalArgumentException("Invalid decimal value in backup.")
                "string" -> value as? String
                    ?: throw IllegalArgumentException("Invalid text value in backup.")
                "string_set" -> {
                    val array = value as? JSONArray
                        ?: throw IllegalArgumentException("Invalid text collection in backup.")
                    buildSet {
                        for (index in 0 until array.length()) {
                            add(array.getString(index))
                        }
                    }
                }
                else -> throw IllegalArgumentException("The backup contains an unknown value type.")
            }
            decoded[key] = decodedValue
        }
        return decoded
    }

    private fun replacePreferences(name: String, values: Map<String, Any>) {
        val preferences: SharedPreferences =
            appContext.getSharedPreferences(name, Context.MODE_PRIVATE)
        val editor = preferences.edit().clear()
        values.forEach { (key, value) ->
            when (value) {
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is Boolean -> editor.putBoolean(key, value)
                is Float -> editor.putFloat(key, value)
                is String -> editor.putString(key, value)
                is Set<*> -> {
                    require(value.all { it is String })
                    editor.putStringSet(key, value.filterIsInstance<String>().toSet())
                }
                else -> throw IllegalArgumentException("Unsupported restored preference value.")
            }
        }
        if (!editor.commit()) throw IOException("Unable to save restored player data.")
    }

    private companion object {
        const val APPLICATION_ID = "com.inventsolutions.reflexrush"
        const val FORMAT_VERSION = 1
        const val MAX_BACKUP_CHARACTERS = 2_000_000
        const val MAX_ENTRIES_PER_FILE = 2_000
    }
}
