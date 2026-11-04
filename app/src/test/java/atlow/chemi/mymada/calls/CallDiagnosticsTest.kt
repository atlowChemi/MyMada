package atlow.chemi.mymada.calls

import org.junit.Assert.*
import org.junit.Test

class CallDiagnosticsTest {
    @Test fun metadataNeverRendersValuesAndHashesAreStableOnlyWithinTheProcess() {
        val privateText = "PRIVATE_LOCATION_NAME_PHONE_TOKEN"
        assertEquals("present=true,type=String,len=${privateText.length}", CallDiagnostics.shape(true, privateText))
        assertEquals("present=false,type=null,len=-1", CallDiagnostics.shape(false, null))
        assertEquals("present=true,type=null,len=-1", CallDiagnostics.shape(true, null))
        val unsafeToRender = object : CharSequence {
            override val length = 42
            override fun get(index: Int): Char = error("must not read content")
            override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = error("must not read content")
            override fun toString(): String = error("must not render content")
        }
        assertEquals("present=true,type=CharSequence,len=42", CallDiagnostics.shape(true, unsafeToRender))
        val hashed = CallDiagnostics.hash(privateText)
        assertTrue(hashed.matches(Regex("[0-9a-f]{16}")))
        assertFalse(hashed.contains(privateText))
        assertEquals(hashed, CallDiagnostics.hash(privateText))
        assertNotEquals(hashed, CallDiagnostics.hash("another-key"))
        assertEquals("absent", CallDiagnostics.hash(null))
        assertNotEquals("absent", CallDiagnostics.hash(""))
        assertEquals("android.text", CallDiagnostics.extraName("android.text"))
        assertEquals("custom#${CallDiagnostics.hash("android.PatientAlice123")}", CallDiagnostics.extraName("android.PatientAlice123"))
        assertFalse(CallDiagnostics.extraName("android.PatientAlice123").contains("PatientAlice123"))
        assertFalse(CallDiagnostics.extraName(privateText).contains(privateText))
        assertFalse(CallDiagnostics.relevant("unrelated.chat", "atlow.chemi.mymada.debug"))
        assertTrue(CallDiagnostics.relevant("com.groboot.mdaemergency", "atlow.chemi.mymada.debug"))
    }
}
