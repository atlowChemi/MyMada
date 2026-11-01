package atlow.chemi.mymada.integrations

import org.junit.Assert.*
import org.junit.Test

class FormContractTest {
    @Test fun approvedPayloadTrimsExceptAmbulanceAndKeepsPairedBilling() {
        val values = mutableListOf(" 1234 ", " 1 ", " x ", " 56 ", " 78 ", " 90 ", " First ", " Last ", "2000, 5000")
        val originalLocale = java.util.Locale.getDefault()
        try {
            for (language in listOf("en", "he")) {
                java.util.Locale.setDefault(java.util.Locale.forLanguageTag(language))
                assertEquals("אמב:  1234 \nויזה: 1\nכתובת: x\nחולה נפגע: 56\nהתחייבות: 78\nסכום: 90\nקוד: 2000, 5000\nשם: First Last", FormContract.body(values))
            }
        } finally { java.util.Locale.setDefault(originalLocale) }
        values[4] = ""
        assertNotEquals(0, FormContract.errors(values)[4])
        values[5] = ""
        assertFalse(FormContract.body(values).contains("התחייבות"))
        values[1] = " \t"; values[2] = "\n "; values[4] = " "; values[5] = "\t"
        assertEquals("אמב:  1234 \nחולה נפגע: 56\nקוד: 2000, 5000\nשם: First Last", FormContract.body(values))
        values[5] = "90"
        assertNotEquals(0, FormContract.errors(values)[4])
        values[4] = "78"; values[5] = " "
        assertNotEquals(0, FormContract.errors(values)[5])
        values[4] = ""; values[5] = ""
        for (index in listOf(3, 6, 7)) {
            val original = values[index]
            values[index] = " \t"
            assertNotEquals(0, FormContract.errors(values)[index])
            assertThrows(IllegalArgumentException::class.java) { FormContract.body(values) }
            values[index] = original
        }
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
