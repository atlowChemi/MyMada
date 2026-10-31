package atlow.chemi.mymada.contractions

import android.content.SharedPreferences
import atlow.chemi.mymada.settings.ContractionHistory
import com.google.gson.Gson

/** Feature-owned state; #45 is only a legacy reader/backupper, never the new writer. */
class ContractionStore(private val preferences: SharedPreferences) {
    data class Saved(val entries: List<ContractionEntry>, val reactNative: Boolean,
        val away: ContractionAway? = null)

    private val gson = Gson()

    fun read(): Saved {
        preferences.getString(KEY, null)?.let { raw ->
            // Fail closed rather than replacing an unreadable session with an empty one.
            return gson.fromJson(raw, Saved::class.java).also { saved ->
                var previousEnd = 0L
                var previousNumber = 0
                saved.entries.forEachIndexed { index, row ->
                    require(row.number > previousNumber && row.number < Int.MAX_VALUE)
                    require(row.start >= previousEnd && (row.end == null || row.end >= row.start))
                    require(row.end != null || index == saved.entries.lastIndex)
                    require(row.previous == null || row.previous >= 0)
                    previousNumber = row.number
                    previousEnd = row.end ?: row.start
                }
            }
        }
        val legacy = ContractionHistory(preferences)
        val imported = legacy.read()
        check(legacy.writable) { "Could not back up legacy contractions" }
        val saved = Saved(imported?.let { value ->
            (value.completed + listOfNotNull(value.active)).map {
                ContractionEntry(it.number, it.start.time, it.end?.time, it.oldEnd?.time)
            }
        }.orEmpty(), legacy.reactNative)
        // Legacy native completion time cannot establish time away. Preserve on first import.
        write(saved)
        return saved
    }

    fun write(saved: Saved) {
        check(preferences.edit().putString(KEY, gson.toJson(saved)).commit()) {
            "Could not persist contractions"
        }
    }

    fun retire(reason: String): Saved {
        val raw = preferences.getString(KEY, null)
        val editor = preferences.edit()
        if (raw != null) {
            var index = preferences.getLong("$KEY.archive.count", 0)
            do { index++ } while (preferences.contains("$KEY.archive.$index"))
            editor.putString("$KEY.archive.$index", raw)
                .putString("$KEY.archive.$index.reason", reason).putLong("$KEY.archive.count", index)
        }
        val empty = Saved(emptyList(), false)
        check(editor.putString(KEY, gson.toJson(empty)).commit()) { "Could not archive contractions" }
        return empty
    }

    companion object { const val KEY = "contractions.session.v1" }
}
