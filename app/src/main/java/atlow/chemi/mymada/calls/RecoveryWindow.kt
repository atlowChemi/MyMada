package atlow.chemi.mymada.calls

/** Reconnect snapshots only. Live callbacks are never deduplicated or age-filtered. */
class RecoveryWindow(var excludedThrough: Long, val handled: MutableMap<String, Long> = mutableMapOf()) {
    fun eligible(revision: String, postedAt: Long, sourceAt: Long, now: Long) =
        postedAt > excludedThrough && revision !in handled && sourceAt <= postedAt &&
            fresh(postedAt, now) && fresh(sourceAt, now)

    fun remember(revision: String, postedAt: Long, now: Long) {
        handled[revision] = postedAt
        excludedThrough = maxOf(excludedThrough, now - MAX_AGE_MS - 1)
        handled.entries.removeAll { it.value <= excludedThrough }
        if (handled.size > MAX_REVISIONS) {
            // ponytail: bounded ledger; overflow advances the recovery floor, never live suppression.
            excludedThrough = maxOf(excludedThrough, handled.values.sorted()[handled.size - MAX_REVISIONS - 1])
            handled.entries.removeAll { it.value <= excludedThrough }
        }
    }

    companion object {
        const val MAX_AGE_MS = 45_000L
        const val MAX_REVISIONS = 1024
        fun fresh(timestamp: Long, now: Long) = timestamp > 0 && timestamp <= now && now - timestamp <= MAX_AGE_MS
        fun recoveryNow(wall: Long, anchorWall: Long, anchorElapsed: Long, elapsed: Long): Long {
            if (anchorWall <= 0 || anchorElapsed < 0 || elapsed < anchorElapsed) return Long.MAX_VALUE
            val since = elapsed - anchorElapsed
            if (anchorWall > Long.MAX_VALUE - since) return Long.MAX_VALUE
            return maxOf(wall, anchorWall + since)
        }
    }
}
