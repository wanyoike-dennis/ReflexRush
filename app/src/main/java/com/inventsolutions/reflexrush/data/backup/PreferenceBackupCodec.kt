package com.inventsolutions.reflexrush.data.backup

import org.json.JSONArray
import org.json.JSONObject

/**
 * Converts supported SharedPreferences values to and from the version-1 backup
 * representation. File access and preference writes stay in GameBackupManager.
 */
internal object PreferenceBackupCodec {
    fun encodeEntries(values: Map<String, *>): JSONObject {
        val entries = JSONObject()
        values.toSortedMap().forEach { (key, value) ->
            require(key.isNotBlank()) { "A stored preference key cannot be blank." }
            entries.put(key, encodeValue(requireNotNull(value) {
                "A stored preference value cannot be null."
            }))
        }
        return entries
    }

    fun decodeEntries(source: JSONObject): Map<String, Any> {
        require(source.length() <= MAX_ENTRIES_PER_FILE) {
            "The backup contains too many entries."
        }
        val decoded = linkedMapOf<String, Any>()
        val keys = source.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            require(key.isNotBlank()) { "The backup contains an invalid preference key." }
            val encoded = source.optJSONObject(key)
                ?: throw IllegalArgumentException("The backup contains an invalid preference value.")
            val value = encoded.opt("value")
            val decodedValue: Any = when (encoded.optString("type")) {
                "int" -> value.asExactInteger()
                "long" -> value.asExactLong()
                "boolean" -> value as? Boolean
                    ?: throw IllegalArgumentException("Invalid boolean value in backup.")
                "float" -> value.asFiniteFloat()
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

    private fun encodeValue(value: Any): JSONObject = when (value) {
        is Int -> JSONObject().put("type", "int").put("value", value)
        is Long -> JSONObject().put("type", "long").put("value", value)
        is Boolean -> JSONObject().put("type", "boolean").put("value", value)
        is Float -> {
            require(value.isFinite()) { "A non-finite decimal cannot be included in the backup." }
            JSONObject().put("type", "float").put("value", value.toDouble())
        }
        is String -> JSONObject().put("type", "string").put("value", value)
        is Set<*> -> {
            require(value.all { it is String }) { "Unsupported preference set value." }
            val array = JSONArray()
            value.filterIsInstance<String>().sorted().forEach { item -> array.put(item) }
            JSONObject().put("type", "string_set").put("value", array)
        }
        else -> throw IllegalArgumentException("A stored value cannot be included in the backup.")
    }

    private fun Any?.asExactInteger(): Int {
        val number = this as? Number
            ?: throw IllegalArgumentException("Invalid integer value in backup.")
        val asLong = number.asExactLong()
        require(asLong in Int.MIN_VALUE..Int.MAX_VALUE) {
            "Integer value is outside the supported range in backup."
        }
        return asLong.toInt()
    }

    private fun Any?.asExactLong(): Long {
        val number = this as? Number
            ?: throw IllegalArgumentException("Invalid long value in backup.")
        val value = number.toLong()
        require(number.toDouble().isFinite() && number.toDouble() == value.toDouble()) {
            "Invalid whole-number value in backup."
        }
        return value
    }

    private fun Any?.asFiniteFloat(): Float {
        val number = this as? Number
            ?: throw IllegalArgumentException("Invalid decimal value in backup.")
        val value = number.toFloat()
        require(value.isFinite()) { "Invalid decimal value in backup." }
        return value
    }

    private const val MAX_ENTRIES_PER_FILE = 2_000
}
