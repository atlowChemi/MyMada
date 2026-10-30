package atlow.chemi.mymada.tools.apgar

import org.junit.Assert.*
import org.junit.Test

class ApgarScoreTest {
    @Test fun all243CombinationsAndInvalidInputs() {
        val counts = IntArray(11)
        for (appearance in 0..2) for (pulse in 0..2) for (grimace in 0..2)
            for (activity in 0..2) for (respiration in 0..2) {
                val score = apgarScore(intArrayOf(appearance, pulse, grimace, activity, respiration))
                assertEquals(appearance + pulse + grimace + activity + respiration, score)
                counts[score]++
                assertEquals(when (score) {
                    0, 1, 2, 3 -> 0xffbf1516.toInt()
                    4, 5, 6 -> 0xffddbe42.toInt()
                    else -> 0xff46b23a.toInt()
                }, apgarColor(score))
            }
        assertArrayEquals(intArrayOf(1, 5, 15, 30, 45, 51, 45, 30, 15, 5, 1), counts)
        for (invalid in listOf(intArrayOf(), IntArray(4), IntArray(6), intArrayOf(-1, 0, 0, 0, 0), intArrayOf(0, 0, 0, 0, 3))) {
            assertThrows(IllegalArgumentException::class.java) { apgarScore(invalid) }
        }
        for (invalid in listOf(-1, 11)) assertThrows(IllegalArgumentException::class.java) { apgarColor(invalid) }
    }
}
