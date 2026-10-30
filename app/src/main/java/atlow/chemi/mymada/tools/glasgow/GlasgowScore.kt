package atlow.chemi.mymada.tools.glasgow

/** Zero-based positions in the shipped eye/verbal/motor arrays; each starts at one point. */
internal fun glasgowScore(choices: IntArray): Int {
    require(choices.size == 3)
    require(choices[0] in 0..3 && choices[1] in 0..4 && choices[2] in 0..5)
    return choices.sum() + 3
}

internal enum class GlasgowBand(val color: Int) {
    DEEP(0xffc11516.toInt()), MODERATE(0xffddbe42.toInt()), LIGHT(0xff46b23a.toInt())
}

internal fun glasgowBand(score: Int): GlasgowBand {
    require(score in 3..15)
    return when {
        score < 9 -> GlasgowBand.DEEP
        score > 12 -> GlasgowBand.LIGHT
        else -> GlasgowBand.MODERATE
    }
}
