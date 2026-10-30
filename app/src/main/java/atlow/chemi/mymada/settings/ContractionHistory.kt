package atlow.chemi.mymada.settings

import android.content.SharedPreferences
import atlow.chemi.mymada.classes.Contraction
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.util.Date

/** Both installed writers use contractLong for eligibility, but only RN means page exit. */
class ContractionHistory(private val preferences: SharedPreferences) {
    private val gson = Gson()
    var writable = true
        private set
    var reactNative = false
        private set

    data class Saved(val completed: ArrayList<Contraction>, val active: Contraction?) {
        val number: Int get() = active?.number?.minus(1) ?: completed.lastOrNull()?.number ?: 0
    }

    /** Back up before validation/expiration; never overwrite the first captured original. */
    fun read(): Saved? {
        val raw = preferences.getString("contractList", "").orEmpty()
        if (raw.isEmpty()) return null
        val editor = preferences.edit()
        for (key in listOf("contractList", "contractLong", "contarctNum")) {
            if (preferences.contains("migration.original.$key")) continue
            when (val value = preferences.all[key]) {
                is String -> editor.putString("migration.original.$key", value)
                is Long -> editor.putLong("migration.original.$key", value)
                is Int -> editor.putInt("migration.original.$key", value)
            }
        }
        writable = editor.commit()
        if (!writable) return null
        return try {
            val array = JsonParser.parseString(raw).asJsonArray
            if (array.size() == 0) return Saved(arrayListOf(), null)
            reactNative = array[0].asJsonObject.get("start").let { it.isJsonPrimitive && it.asJsonPrimitive.isNumber }
            val completed = arrayListOf<Contraction>()
            var active: Contraction? = null
            var previousNumber = 0
            var previousEnd = 0L
            for ((index, element) in array.withIndex()) {
                val item = element.asJsonObject
                val number = item.get("number").asBigDecimal.intValueExact()
                require(number > previousNumber && number < Int.MAX_VALUE)
                val start = date(item, "start") ?: error("Missing start")
                val end = date(item, "end")
                require(start.time >= previousEnd && (end == null || end >= start))
                val oldEnd = if (reactNative) completed.lastOrNull()?.start else date(item, "oldEnd")
                val contraction = Contraction(number, start, end, oldEnd)
                if (end == null) {
                    require(reactNative && index == array.size() - 1)
                    active = contraction
                } else completed.add(contraction)
                previousNumber = number
                previousEnd = end?.time ?: start.time
            }
            Saved(completed, active)
        } catch (_: RuntimeException) {
            // Invalid/ambiguous records stay verbatim; a new explicit session may replace the live key.
            null
        }
    }

    private fun date(item: JsonObject, key: String): Date? {
        val value = item.get(key) ?: return null
        if (value.isJsonNull) return null
        require(value.isJsonPrimitive)
        val date = if (reactNative) {
            require(value.asJsonPrimitive.isNumber)
            Date(value.asBigDecimal.longValueExact())
        } else {
            require(value.asJsonPrimitive.isString)
            gson.fromJson(value, Date::class.java)
        }
        require(date.time >= 0)
        return date
    }

    fun eligible(now: Long): Boolean = now - preferences.getLong("contractLong", 0) <= 20 * 60 * 1000

    fun clear() {
        if (writable) preferences.edit().putString("contractList", "").putInt("contarctNum", 0)
            .putLong("contractLong", 0).apply()
    }

    fun save(completed: List<Contraction>, activeStart: Date?, number: Int, now: Long) {
        if (!writable || completed.isEmpty() && (!reactNative || activeStart == null)) return
        val json = if (reactNative) {
            val array = JsonArray()
            fun append(number: Int, start: Date, end: Date?) {
                array.add(JsonObject().apply {
                    addProperty("number", number)
                    addProperty("start", start.time)
                    end?.let { addProperty("end", it.time) }
                })
            }
            completed.forEach { append(it.number, it.start, it.end) }
            activeStart?.let { append(number + 1, it, null) }
            array.toString()
        } else gson.toJson(completed)
        preferences.edit().putString("contractList", json).putInt("contarctNum", number)
            .putLong("contractLong", if (reactNative) now else completed.last().end.time).apply()
    }
}
