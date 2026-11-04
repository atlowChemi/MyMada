package atlow.chemi.mymada.integrations

import org.junit.Assert.*
import org.junit.Test

class ChatContractTest {
    @Test fun preservesExactBackendPayloadAndRejectsInvalidIdentity() {
        assertEquals(mapOf("Name" to "שם", "LastSent" to 1234L, "SeenByAdmin" to false,
            "-key" to mapOf("sender" to "historical:token", "msg" to "hello\n& #", "time" to 1234L)),
            ChatContract.payload("-key", "historical:token", "שם", " hello\n& # ", 1234L))
        listOf(null, "", "empty", "bad/path", "bad.key", "bad\nkey").forEach { assertNull(ChatContract.identity(it)) }
        assertEquals("historical:token", ChatContract.identity("historical:token"))
    }
    @Test fun ignoresMetadataAndMalformedMessagesWithoutInventingDates() {
        assertNull(ChatContract.message("Name", "name"))
        assertNull(ChatContract.message("bad", mapOf("sender" to 42)))
        val message = ChatContract.message("-key", mapOf("sender" to "admin", "msg" to "שלום", "WhatsApp" to true))!!
        assertTrue(message.whatsApp)
        assertNull(message.time)
        assertEquals(123L, ChatContract.message("-key", mapOf("sender" to "admin", "msg" to "x", "time" to 123L))!!.time)
    }
}
