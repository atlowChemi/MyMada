package atlow.chemi.mymada.integrations

import org.junit.Assert.*
import org.junit.Test

class FormContractTest {
    @Test fun nativePayloadKeepsWhitespaceOptionalThresholdAndPairedBilling() {
        val values = mutableListOf(" 1234 ", "1", "x", "56", "78", "90", " First ", "Last", "2000, 5000")
        assertEquals("אמב'  1234 \nחולה נפגע 56\nהתחייבות 78\nסכום 90\nקוד 2000, 5000\nשם:  First  Last", FormContract.body(values))
        values[4] = ""
        assertNotEquals(0, FormContract.errors(values)[4])
        values[5] = ""
        assertFalse(FormContract.body(values).contains("התחייבות"))
        values[1] = "12"; values[2] = "רחוב א"
        assertTrue(FormContract.body(values).contains("\nויזה: 12\nכתובת: רחוב א"))
        values[0] = " "
        assertNotEquals(0, FormContract.errors(values)[0])
        values[8] = "2000&5000"
        assertNotEquals(0, FormContract.errors(values)[8])
        assertEquals(161, MedicalCatalog.names.size)
        assertEquals(149, MedicalCatalog.ids.count { it != "sep" })
        assertEquals(149, MedicalCatalog.ids.filter { it != "sep" }.distinct().size)
    }
}
