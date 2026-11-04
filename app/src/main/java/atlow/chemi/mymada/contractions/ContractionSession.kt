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
                last?.start)
        }
    }

    fun clear() { entries = emptyList() }
}

/** A null anchor means no departure was observed, not that the session is old. */
data class ContractionAway(val wall: Long, val elapsed: Long, val boot: Int) {
    fun expired(now: ContractionAway): Boolean {
        val duration = if (boot >= 0 && boot == now.boot && now.elapsed >= elapsed)
            now.elapsed - elapsed else (now.wall - wall).coerceAtLeast(0)
        return duration > 20 * 60 * 1000L
    }
}
