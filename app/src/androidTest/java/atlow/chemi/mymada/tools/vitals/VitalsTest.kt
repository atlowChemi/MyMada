package atlow.chemi.mymada.tools.vitals

import org.junit.Assert.*
import org.junit.Test

class VitalsTest {
    @Test fun boundariesAndMalformedData() {
        for (age in nativeVitals) for (range in listOf(age.pulse, age.breath, age.systolic, age.diastolic)) {
            assertEquals(false, range.contains((range.low - 1).toString()))
            assertEquals(true, range.contains(range.low.toString()))
            assertEquals(true, range.contains(range.high.toString()))
            assertEquals(false, range.contains((range.high + 1).toString()))
            assertNull(range.contains(""))
            assertEquals(false, range.contains("NaN"))
        }
        val adult = nativeVitals[0]
        assertEquals(true, adult.pulse.contains("60"))
        for ((sys, dia, good) in listOf(Triple("119","90",false), Triple("120","90",true),
            Triple("120","60",true), Triple("121","60",false), Triple("89","59",false))) {
            assertEquals(good to good, pressureStatus(sys, dia, adult))
        }
        assertEquals(null to null, pressureStatus("", "80", adult))
        assertEquals(true to null, pressureStatus("120", "", adult))
        assertEquals(false to false, pressureStatus("999", "80", adult))
        assertEquals(true, nativeVitals[2].pulse.contains("125"))
        assertEquals(VitalRange(125, 130), remoteVitalRange(null, null, nativeVitals[2].pulse))
        val fallback = adult.pulse
        for ((low, high) in listOf(null to 100, 60 to null, "60" to 100, 120 to 60,
            -1 to 100, Double.NaN to 100, 60.5 to 100, 60 to Double.POSITIVE_INFINITY,
            60 to Long.MAX_VALUE)) assertEquals(fallback, remoteVitalRange(low, high, fallback))
        assertEquals(VitalRange(61,101), remoteVitalRange(61L,101.0,fallback))
        for (input in listOf("", "0", "125", "999", "001")) assertTrue(validVitalInput(input))
        for (input in listOf("1000", "-1", "1.5", "1e2", "１２", " 60 ")) assertFalse(validVitalInput(input))
    }
}
