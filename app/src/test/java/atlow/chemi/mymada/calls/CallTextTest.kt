package atlow.chemi.mymada.calls

import org.junit.Assert.*
import org.junit.Test

class CallTextTest {
    @Test fun extractsFieldsWithoutInventingTextWhenMarkersAreMissing() {
        val hebrew = CallText.parse(3, "הודעת צוות. 12:34 מונה:7 כתובת: בדיקה קוד הזנקה: א קוד רפואי: ב", "00:00")
        assertEquals("בדיקה", hebrew.address)
        assertEquals("א", hebrew.dispatchCode)
        assertEquals("ב", hebrew.medicalCode)
        assertEquals("12:34 ", hebrew.time)
        val english = CallText.parse(1, "אירוע חדש. 12:34 Incident number:7 Address: Fixture Dispatch Code: A Medical Code: B", "00:00")
        assertEquals("Fixture ", english.address)
        assertEquals("A ", english.dispatchCode)
        assertEquals("B", english.medicalCode)
        val malformed = CallText.parse(1, "אירוע חדש ללא שדות", "00:00")
        assertEquals("אירוע חדש ללא שדות", malformed.address)
        assertEquals("", malformed.medicalCode)
        assertEquals("00:00", malformed.time)
        val legacy = CallText.parse(5, "בדיקה בשעה 09:12 המשך//פרטים", "00:00")
        assertEquals("בדיקה בשעה 09:12 המשך", legacy.address)
        assertEquals("09:12", legacy.time)
        assertTrue(legacy.plain)
    }
}
