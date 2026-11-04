package atlow.chemi.mymada.settings

import android.content.SharedPreferences

internal object DayNightSettings {
    fun setMode(preferences: SharedPreferences, night: Boolean, mode: Int) {
        require(mode in 0..2)
        val dayMode = if (night) preferences.getInt("sleepDay", 0) else mode
        val nightMode = if (night) mode else preferences.getInt("sleepNight", 0)
        require(dayMode in 0..2 && nightMode in 0..2)
        // Existing receivers decode day/night as a 3×3 table; update its code atomically.
        preferences.edit().putInt(if (night) "sleepNight" else "sleepDay", mode)
            .putInt("sleep", dayMode * 3 + nightMode).apply()
    }
}
