package atlow.chemi.mymada.contractions

/** Epoch timestamps survive backgrounding; no animation measures a contraction. */
data class ContractionEntry(val number: Int, val start: Long, val end: Long?, val previous: Long?)

class ContractionSession(val reactNative: Boolean, entries: List<ContractionEntry> = emptyList()) {
    var entries = entries
        private set
    val active get() = entries.lastOrNull()?.takeIf { it.end == null }

    fun toggle(now: Long) {
        val running = active
        if (running != null) {
            entries = entries.dropLast(1) + running.copy(end = maxOf(now, running.start))
        } else {
            val last = entries.lastOrNull()
            val start = maxOf(now, last?.end ?: now)
            entries = entries + ContractionEntry((last?.number ?: 0) + 1, start, null,
                if (reactNative) last?.start else last?.end)
        }
    }

    fun clear() { entries = emptyList() }
}
