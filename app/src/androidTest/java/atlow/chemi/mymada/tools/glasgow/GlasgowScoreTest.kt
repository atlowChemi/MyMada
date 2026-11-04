package atlow.chemi.mymada.tools.glasgow

import org.junit.Assert.*
import org.junit.Test

class GlasgowScoreTest {
    @Test fun allScoresBandsAndInvalidInputs() {
        val counts = IntArray(16)
        for (eye in 0..3) for (verbal in 0..4) for (motor in 0..5) {
            val score = glasgowScore(intArrayOf(eye, verbal, motor))
            assertEquals(eye + verbal + motor + 3, score)
            counts[score]++
            assertEquals(when (score) {
                in 3..8 -> GlasgowBand.DEEP
                in 9..12 -> GlasgowBand.MODERATE
                else -> GlasgowBand.LIGHT
            }, glasgowBand(score))
        }
        assertArrayEquals(intArrayOf(1, 3, 6, 10, 14, 17, 18, 17, 14, 10, 6, 3, 1), counts.sliceArray(3..15))
        assertEquals(0xffc11516.toInt(), glasgowBand(8).color)
        assertEquals(0xffddbe42.toInt(), glasgowBand(9).color)
        assertEquals(0xffddbe42.toInt(), glasgowBand(12).color)
        assertEquals(0xff46b23a.toInt(), glasgowBand(13).color)
        for (invalid in listOf(intArrayOf(), intArrayOf(0, 0), intArrayOf(0, 0, 0, 0),
            intArrayOf(-1, 0, 0), intArrayOf(4, 0, 0), intArrayOf(0, -1, 0), intArrayOf(0, 5, 0),
            intArrayOf(0, 0, -1), intArrayOf(0, 0, 6))) {
            assertThrows(IllegalArgumentException::class.java) { glasgowScore(invalid) }
        }
        for (invalid in listOf(Int.MIN_VALUE, 2, 16, Int.MAX_VALUE)) {
            assertThrows(IllegalArgumentException::class.java) { glasgowBand(invalid) }
        }
    }
}
