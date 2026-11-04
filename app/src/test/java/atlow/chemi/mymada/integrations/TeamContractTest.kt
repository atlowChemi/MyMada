package atlow.chemi.mymada.integrations

import atlow.chemi.mymada.classes.Volunteer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TeamContractTest {
    @Test fun publishedNativePayloadPreservesRolesWhitespaceAndRegionalHeader() {
        assertEquals("צוות אמבולנס י-ם 1234:\nנהג - דן כהן\nרופא -  שם ",
            TeamContract.body("1234", -1, listOf(Volunteer("דן כהן", 0), Volunteer(" שם ", 3))))
        TeamContract.roles.forEachIndexed { job, name ->
            assertEquals("צוות אמבולנס מוקד 4 12:\n$name - Fixture", TeamContract.body("12", 11, listOf(Volunteer("Fixture", job))))
        }
        assertThrows(IllegalArgumentException::class.java) { TeamContract.body(" ", 0, listOf(Volunteer("X", 0))) }
        assertThrows(IllegalArgumentException::class.java) { TeamContract.body("1", 0, List(5) { Volunteer("X", 0) }) }
        assertThrows(IllegalArgumentException::class.java) { TeamContract.body("1", 0, listOf(Volunteer("X", 7))) }
    }
}
