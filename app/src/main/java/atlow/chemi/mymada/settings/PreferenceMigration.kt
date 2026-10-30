package atlow.chemi.mymada.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.core.app.LocaleManagerCompat

/** In-place bridge for native typed preferences and RN's string-only writer. */
object PreferenceMigration {
    private val booleans = setOf("Mapp", "realM", "privacy", "oldU", "pickedMoked", "WasLangChanged",
        "cut", "not", "win", "stat", "statVib", "ride", "loop", "killS", "volumeApp") +
        (1..6).map { "callT$it" } +
        listOf("Nvib", "Nsou", "Napp").flatMap { prefix -> listOf(prefix) + (1..4).map { "${prefix}T$it" } }
    private val integers = mapOf("merhav" to 0..11, "nightStart" to 0..23, "nightEnd" to 0..23,
        "sleepDay" to 0..2, "sleepNight" to 0..2, "sleep" to 0..8,
        "volumeAppNum" to 0..Int.MAX_VALUE, "contarctNum" to 0..Int.MAX_VALUE)
    private val strings = setOf("lng", "first_time_migration", "firebaseGenratedKey", "fcm", "UserName",
        "contractList", "timeK", "NuRi") + (1..4).map { "NuRiT$it" }

    @JvmStatic fun migrate(context: Context) {
        migrateFile(context.getSharedPreferences("Settings", 0), "Settings")
        migrateFile(context.getSharedPreferences("permissionStatus", 0), "permissionStatus")
        migrateFile(context.getSharedPreferences("rate_app", 0), "rate_app")
        // notifications has no typed reader/writer in the restored native code; retain it verbatim.
        val settings = context.getSharedPreferences("Settings", 0)
        if (!settings.contains("nativeLocaleImported")) {
            val language = LocaleManagerCompat.getApplicationLocales(context)[0]?.language
            val editor = settings.edit()
            if (language in setOf("he", "iw", "en")) {
                if (!settings.contains("migration.original.lng")) {
                    settings.getString("lng", null)?.let { editor.putString("migration.original.lng", it) }
                }
                editor.putString("lng", if (language == "en") "en" else "iw")
            }
            editor.putBoolean("nativeLocaleImported", true).commit()
        }
    }

    internal fun migrateFile(preferences: SharedPreferences, file: String): Boolean {
        val values = preferences.all
        val editor = preferences.edit()
        var changed = false
        for ((key, original) in values) {
            if (original == null || key.startsWith("migration.original.")) continue
            val converted: Any? = when {
                file == "Settings" && key in booleans || file == "rate_app" && key == "dontshowagain" ||
                    file == "permissionStatus" && key.startsWith("android.permission.") ->
                    if (original is Boolean) original else (original as? String)?.toBooleanStrictOrNull()
                file == "Settings" && key in integers -> integer(original)?.takeIf { it in integers.getValue(key) }
                file == "Settings" && key == "contractLong" ||
                    file == "rate_app" && key in setOf("launch_count", "date_first_launch") ->
                    long(original)?.takeIf { it >= 0 }
                file == "Settings" && key == "Tools" -> tools(original)
                file == "Settings" && key == "timeK" ->
                    integer(original)?.takeIf { it in 0..Int.MAX_VALUE / 1000 }?.toString()
                file == "Settings" && key in strings -> original as? String
                else -> continue
            }
            if (converted == original) continue
            // The original and conversion/default fallback commit atomically in the SAME file.
            // Malformed values use the existing reader's default without destroying recovery data.
            val backup = "migration.original.$key"
            if (!values.containsKey(backup)) put(editor, backup, original)
            if (converted == null) editor.remove(key) else put(editor, key, converted)
            changed = true
        }
        return !changed || editor.commit()
    }

    private fun integer(value: Any): Int? = when (value) {
        is Int -> value
        is Long -> value.takeIf { it in Int.MIN_VALUE..Int.MAX_VALUE }?.toInt()
        is String -> value.toIntOrNull()
        else -> null
    }

    private fun long(value: Any): Long? = when (value) {
        is Long -> value
        is Int -> value.toLong()
        is String -> value.toLongOrNull()
        else -> null
    }

    private fun tools(value: Any): Set<String>? {
        if (value is Set<*>) return value.takeIf { it.all { item -> item is String } }?.filterIsInstance<String>()?.toSet()
        if (value !is String || !value.startsWith('[') || !value.endsWith(']')) return null
        val body = value.substring(1, value.length - 1).trim()
        if (body.isEmpty()) return emptySet()
        val items = body.split(',').map(String::trim)
        return items.takeIf { it.all { item -> item.matches(Regex("[A-Za-z][A-Za-z0-9_]*")) } }?.toSet()
    }

    private fun put(editor: SharedPreferences.Editor, key: String, value: Any) {
        when (value) {
            is Boolean -> editor.putBoolean(key, value)
            is Int -> editor.putInt(key, value)
            is Long -> editor.putLong(key, value)
            is Float -> editor.putFloat(key, value)
            is String -> editor.putString(key, value)
            is Set<*> -> editor.putStringSet(key, value.filterIsInstance<String>().toSet())
        }
    }
}
