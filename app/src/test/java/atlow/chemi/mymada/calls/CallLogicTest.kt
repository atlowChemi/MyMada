package atlow.chemi.mymada.calls

import org.junit.Assert.*
import org.junit.Test

class CallLogicTest {
    private fun parse(text: String?, title: String? = "*אירוע חדש* 12:34", pack: String? = "com.groboot.mdaemergency") =
        CallParser.parse(true, pack, title, text)

    @Test fun sanitizedFormatsAndNativePrecedence() {
        val examples = mapOf(
            "אירוע חדש. 12:34 מונה: 000 כתובת: רחוב לדוגמה קוד הזנקה: TEST קוד רפואי: TEST" to 1,
            "הודעת קבוצת בדיקה" to 2, "הודעת צוות בדיקה" to 3,
            "מנהלתי בדיקה" to 4, "מינהלתי בדיקה" to 4, "משמרת בנה בדיקה" to 4,
            "בדיקה על מידע זה חל חיסיון רפואי" to 5,
            "שירות בדיקה" to 6, "כלכלה בדיקה" to 6, "תפילה בדיקה" to 6,
            "שירות על מידע זה חל חיסיון רפואי" to 6,
            "אירוע חדש הודעת צוות" to 1,
        )
        for ((text, type) in examples) {
            assertEquals(text, type, parse(text)!!.type)
            assertEquals(" 12:34", parse(text)!!.time)
            assertEquals(text, parse(text)!!.text)
        }
        assertEquals(1, parse("Address: Example Dispatch Code: TEST", "*New Event* 12:34")!!.type)
        assertEquals(4, parse("בדיקה", "*הודעה מנהלתית חדשה*")!!.type)
        assertEquals(5, parse("שירות על מידע זה חל חיסיון רפואי", "*הודעה מנהלתית חדשה*")!!.type)
    }

    @Test fun filtersMalformedAndRepeatedPayloads() {
        assertNull(CallParser.parse(false, "com.groboot.mdaemergency", null, null))
        for (pack in listOf(null, "other.app", "atlow.mymadaadmin.fake")) assertNull(parse("בדיקה", pack = pack))
        for (title in listOf("מגן דוד אדום", "מערך הכוננים הלאומי", "unknown", "")) assertNull(parse("בדיקה", title))
        assertEquals(ParsedCall(1, "תקלה", null), parse(null, null))
        assertEquals(1, parse("", "*New Event*")!!.type)
        assertNotNull(parse("בדיקה", pack = "atlow.mymadaadmin"))
        assertNotNull(parse("בדיקה", pack = "com.mda.health"))
        // No text-based deduplication: repeat alerts may be intentional source events.
        assertEquals(parse("הודעת צוות בדיקה"), parse("הודעת צוות בדיקה"))
    }

    @Test fun perTypeRoutingAndTestOverride() {
        val settings = mutableMapOf<String, Any>("cut" to true)
        for (suffix in listOf("T1", "T2", "T3", "T4")) {
            settings["NuRi$suffix"] = suffix
            settings["Napp$suffix"] = true
        }
        for ((type, ringtone) in mapOf(1 to "T1", 2 to "T2", 3 to "T3", 4 to "T4", 5 to "T3", 6 to "T4", 99 to "T1")) {
            val decision = CallAlerts.decide(settings, type, 12, 2, false)
            assertEquals(ringtone, decision.ringtone)
            assertTrue(decision.customSound)
        }
        settings["callT3"] = false
        assertFalse(CallAlerts.decide(settings, 5, 12, 2, false).enabled)
        assertTrue(CallAlerts.decide(settings, 5, 12, 2, true).enabled)
        settings["cut"] = false
        assertTrue(CallAlerts.decide(settings, 5, 12, 2, false).enabled)
        settings["callT6"] = false
        assertFalse(CallAlerts.decide(settings, 6, 12, 2, false).enabled)
        assertTrue(CallAlerts.decide(settings, 4, 12, 2, false).enabled)
    }

    @Test fun allDayNightLevelsBoundariesAndRingerPaths() {
        for (day in 0..2) for (night in 0..2) for (hour in listOf(0, 7, 8, 12, 22, 23)) {
            val level = if (hour <= 7 || hour >= 23) night else day
            val settings = mapOf("sleep" to day * 3 + night)
            val decision = CallAlerts.decide(settings, 1, hour, 2, false)
            assertEquals(level == 0, decision.sound)
            assertEquals(level != 2, decision.vibrate)
        }
        for (mode in 0..2) {
            val decision = CallAlerts.decide(mapOf("stat" to true, "statVib" to true), 1, 12, mode, false)
            assertEquals(mode == 2, decision.sound)
            assertEquals(mode != 0, decision.popupVibrate)
            assertEquals(mode == 0, decision.overlayVibrate)
        }
        val disabled = CallAlerts.decide(mapOf("Nsou" to false, "Nvib" to false), 1, 12, 2, false)
        assertFalse(disabled.sound)
        assertFalse(disabled.popupVibrate)
        assertFalse(disabled.overlayVibrate)
    }
}
