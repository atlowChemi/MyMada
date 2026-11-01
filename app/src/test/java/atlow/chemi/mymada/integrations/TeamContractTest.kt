package atlow.chemi.mymada.integrations

import atlow.chemi.mymada.classes.Volunteer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TeamContractTest {
    @Test fun publishedNativePayloadPreservesRolesWhitespaceAndRegionalHeader() {
        assertEquals("צוות אמבולנס ירושלים 1234\nנהג - דן כהן\nרופא -  שם ",
            TeamContract.body("1234", -1, listOf(Volunteer("דן כהן", 0), Volunteer(" שם ", 3))))
        TeamContract.roles.forEachIndexed { job, name ->
            assertEquals("צוות אמבולנס מוקד 4 12\n$name - Fixture", TeamContract.body("12", 11, listOf(Volunteer("Fixture", job))))
        }
        assertThrows(IllegalArgumentException::class.java) { TeamContract.body(" ", 0, listOf(Volunteer("X", 0))) }
        assertThrows(IllegalArgumentException::class.java) { TeamContract.body("1", 0, List(5) { Volunteer("X", 0) }) }
        assertThrows(IllegalArgumentException::class.java) { TeamContract.body("1", 0, listOf(Volunteer("X", 7))) }
    }
    @Test fun everyRegionUsesFullHebrewNameInBothLocales() {
        val names = listOf("ירושלים", "איילון", "אשר", "גלבוע", "דן", "ירדן", "ירקון", "כרמל", "לכיש", "נגב", "שרון", "מוקד 4")
        val original = java.util.Locale.getDefault()
        try {
            for (language in listOf("en", "he")) {
                java.util.Locale.setDefault(java.util.Locale.forLanguageTag(language))
                names.forEachIndexed { region, name ->
                    assertEquals("צוות אמבולנס $name 1234\nנהג - דן כהן", TeamContract.body("1234", region, listOf(Volunteer("דן כהן", 0))))
                }
                assertEquals("צוות אמבולנס ירושלים 1234\nנהג - דן כהן", TeamContract.body("1234", 12, listOf(Volunteer("דן כהן", 0))))
            }
        } finally { java.util.Locale.setDefault(original) }
    }
}
