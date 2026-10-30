package atlow.chemi.mymada.tools.pulse

import org.junit.Assert.*
import org.junit.Test

class PulseStateTest {
    @Test fun nativeCalculationAndDeadlineBoundaries() {
        for (count in 0..99) assertEquals(count * 4, pulsePerMinute(count.toString()))
        assertEquals(4, pulsePerMinute("01"))
        listOf("", " ", "-1", "1.5", "100", "١", "12a").forEach { assertNull(pulsePerMinute(it)) }
        assertEquals(15, pulseSeconds(16_000, 1_000))
        assertEquals(14, pulseSeconds(16_000, 1_001))
        assertEquals(1, pulseSeconds(16_000, 15_000))
        assertEquals(0, pulseSeconds(16_000, 15_001))
        assertEquals(0, pulseSeconds(16_000, 16_000))
        assertEquals(0, pulseSeconds(16_000, 20_000))
    }
}
