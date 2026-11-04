package atlow.chemi.mymada.tools.vitals

/** Maintainer-approved native app rules; not independent clinical validation. */
internal data class VitalRange(val low: Int, val high: Int) {
    fun contains(input: String): Boolean? = input.takeIf { it.isNotEmpty() }?.let {
        it.toIntOrNull()?.let { value -> value in low..high } ?: false
    }
}
internal data class VitalRanges(val pulse: VitalRange, val breath: VitalRange,
    val systolic: VitalRange, val diastolic: VitalRange)
internal val nativeVitals = listOf(
    VitalRanges(VitalRange(60,100), VitalRange(12,20), VitalRange(90,140), VitalRange(60,90)),
    VitalRanges(VitalRange(90,120), VitalRange(18,30), VitalRange(90,110), VitalRange(50,80)),
    VitalRanges(VitalRange(125,130), VitalRange(22,60), VitalRange(60,90), VitalRange(40,60)),
)
/** Validate each pair atomically, without inventing clinical upper bounds. */
internal fun remoteVitalRange(low: Any?, high: Any?, fallback: VitalRange): VitalRange {
    fun integer(value: Any?): Int? = (value as? Number)?.toDouble()?.let {
        if (it.isFinite() && it >= 0 && it <= Int.MAX_VALUE && it % 1.0 == 0.0) it.toInt() else null
    }
    val lower = integer(low) ?: return fallback
    val upper = integer(high) ?: return fallback
    return if (lower <= upper) VitalRange(lower, upper) else fallback
}
internal fun validVitalInput(value: String) = value.length <= 3 && value.all { it in '0'..'9' }
internal fun pressureStatus(sys: String, dia: String, ranges: VitalRanges): Pair<Boolean?, Boolean?> {
    if (sys.isEmpty()) return null to null // Native diastolic-only state is neutral.
    if (dia.isEmpty()) return ranges.systolic.contains(sys) to null
    val difference = sys.toIntOrNull()?.let { s -> dia.toIntOrNull()?.let { s - it } }
    val good = ranges.systolic.contains(sys) == true && ranges.diastolic.contains(dia) == true &&
        difference != null && difference in 30..60
    return good to good
}
