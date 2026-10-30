package atlow.chemi.mymada.tools.pulse

/** Native two-digit count over 15 seconds, expressed as beats per minute. */
internal fun pulsePerMinute(input: String): Int? =
    input.takeIf { it.length in 1..2 && it.all { c -> c in '0'..'9' } }?.toInt()?.times(4)

internal fun pulseSeconds(deadline: Long, now: Long): Int =
    ((deadline - now).coerceIn(0, 15_000) / 1000).toInt()
