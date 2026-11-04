package atlow.chemi.mymada.tools.apgar

/** Resource option order is the score: appearance, pulse, grimace, activity, respiration. */
internal fun apgarScore(choices: IntArray): Int {
    require(choices.size == 5 && choices.all { it in 0..2 })
    return choices.sum()
}

/** Existing native/RN boundaries, including green at exactly 7. */
internal fun apgarColor(score: Int): Int {
    require(score in 0..10)
    return when (score) {
        in 0..3 -> 0xffbf1516.toInt()
        in 4..6 -> 0xffddbe42.toInt()
        else -> 0xff46b23a.toInt()
    }
}
