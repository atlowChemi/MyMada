package atlow.chemi.mymada.helpers

import org.junit.Assert.*
import org.junit.Test

class EventTest {
    @Test fun namedListenersKeepFirstIncludingNullAndSnapshotsAreIndependent() {
        val event = Event<String?>()
        event.addListener("named", null)
        event.addListener("named", "replacement")
        event.addListener(null, "null key")
        event.addListener("anonymous")
        event.addListener("anonymous")
        val snapshot = event.listeners()
        assertEquals(4, snapshot.size)
        assertTrue(snapshot.contains(null))
        assertEquals(listOf("anonymous", "anonymous"), snapshot.takeLast(2))
        event.removeListener("named")
        event.removeListener(null)
        assertEquals(listOf("anonymous", "anonymous"), event.listeners())
        snapshot.clear()
        assertEquals(2, event.listeners().size)
    }
}
