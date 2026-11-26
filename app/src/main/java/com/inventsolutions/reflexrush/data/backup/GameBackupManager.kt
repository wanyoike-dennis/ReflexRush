package com.inventsolutions.reflexrush.data.backup

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.inventsolutions.reflexrush.data.preferences.PreferenceStoreRegistry
import org.json.JSONObject
import java.io.IOException

/**
 * Reads and writes portable backups through Android's document picker.
 *
 * The version-1 JSON structure and preference identifiers are kept stable for
 * compatibility with backups already exported by earlier app versions.
 */
class GameBackupManager(context: Context) {
    private val appContext = context.applicationContext
    private val preferenceFiles = PreferenceStoreRegistry.backupStoreNames

    fun exportTo(uri: Uri) {
        val allPreferences = JSONObject()
        preferenceFiles.forEach { name ->
            val entries = PreferenceBackupCodec.encodeEntries(
                appContext.getSharedPreferences(name, Context.MODE_PRIVATE).all
            )
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
        require(jsonText.length <= MAX_BACKUP_CHARACTERS) {
            "The selected backup file is too large."
        }

        val root = try {
            JSONObject(jsonText)
        } catch (exception: Exception) {
            throw IllegalArgumentException("This is not a valid Reflex Rush backup file.", exception)
        }

        require(root.opt("formatVersion") == FORMAT_VERSION) {
            "This backup uses an unsupported format version."
        }
        require(root.optString("applicationId") == APPLICATION_ID) {
            "This backup was not created by Reflex Rush."
        }

        val sourcePreferences = root.optJSONObject("preferences")
            ?: throw IllegalArgumentException("The backup does not contain player data.")

        // Decode every store before touching the current on-device data.
        val decoded = preferenceFiles.associateWith { name ->
            val source = sourcePreferences.optJSONObject(name)
                ?: throw IllegalArgumentException("The backup is missing required data: $name.")
            PreferenceBackupCodec.decodeEntries(source)
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
            // SharedPreferences has no cross-file transaction; restore snapshots if a
            // later store fails after an earlier store has already been replaced.
            val rollbackFailures = previous.mapNotNull { (name, values) ->
                runCatching { replacePreferences(name, values) }.exceptionOrNull()
            }
            val message = if (rollbackFailures.isEmpty()) {
                "Could not restore the backup. Previous data was rolled back."
            } else {
                "Could not restore the backup, and rollback was incomplete."
            }
            throw IOException(message, exception)
        }
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
    }
}
