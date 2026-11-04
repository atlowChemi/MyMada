package atlow.chemi.mymada.calls

/** Input is the validated Settings snapshot produced by PreferenceMigration at startup. */
data class AlertDecision(
    val enabled: Boolean,
    val suffix: String,
    val sound: Boolean,
    val vibrate: Boolean,
    val customSound: Boolean,
    val ringtone: String,
    val popupVibrate: Boolean,
    val overlayVibrate: Boolean,
)

object CallAlerts {
    @JvmStatic fun suffix(type: Int): String = when (type) {
        2 -> "T2"
        3, 5 -> "T3"
        4 -> "T4"
        6 -> "T6"
        else -> "T1"
    }

    @JvmStatic fun decide(settings: Map<String, *>, type: Int, hour: Int, ringerMode: Int, test: Boolean): AlertDecision {
        fun bool(key: String, default: Boolean) = settings[key] as? Boolean ?: default
        fun integer(key: String, default: Int) = settings[key] as? Int ?: default
        val typeSuffix = suffix(type)
        val administrative = typeSuffix == "T4" || typeSuffix == "T6"
        val split = bool("cut", false)
        val audioSuffix = if (administrative) "T4" else if (split) typeSuffix else ""
        val enabled = test || (!administrative && !split) || bool("call$typeSuffix", true)
        val night = hour <= integer("nightEnd", 7) || hour >= integer("nightStart", 23)
        val sleep = integer("sleep", 0)
        val level = if (sleep in 0..8) { if (night) sleep % 3 else sleep / 3 } else 0
        val sound = bool("Nsou$audioSuffix", true) && level == 0 && (!bool("stat", false) || ringerMode == 2)
        val vibrate = bool("Nvib$audioSuffix", true) && level != 2
        val respectVibration = bool("statVib", false)
        return AlertDecision(enabled, typeSuffix, sound, vibrate, bool("Napp$audioSuffix", false),
            settings["NuRi$audioSuffix"] as? String ?: "",
            // Existing native paths disagree. Keep both explicit until #24's product decision.
            vibrate && (!respectVibration || ringerMode != 0),
            vibrate && (!respectVibration || ringerMode == 0))
    }
}
