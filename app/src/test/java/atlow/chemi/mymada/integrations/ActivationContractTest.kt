package atlow.chemi.mymada.integrations

import org.junit.Assert.*
import org.junit.Test

class ActivationContractTest {
    @Test fun payloadPreservesSchemaAndLiteralText() {
        assertEquals(mapOf("email" to "a@example.org", "name" to "שם", "reason" to "A&B#\nעזרה",
            "code" to "0012", "token" to "fixture-token", "allowed" to false, "used" to false, "time" to "1700000000"),
            ActivationContract.request(" a@example.org ", " שם ", "A&B#\nעזרה", "0012", "fixture-token", 1700000000123))
    }
    @Test fun absentMalformedUnapprovedAndUsedRecordsNeverActivate() {
        val ready = mapOf("code" to "0012", "allowed" to true, "used" to false)
        assertEquals(ActivationContract.Result.READY, ActivationContract.validate("0012", ready))
        assertEquals(ActivationContract.Result.INVALID, ActivationContract.validate("1234", ready))
        assertEquals(ActivationContract.Result.INVALID, ActivationContract.validate("0012", null))
        assertEquals(ActivationContract.Result.USED, ActivationContract.validate("0012", ready - "used"))
        assertEquals(ActivationContract.Result.USED, ActivationContract.validate("0012", ready + ("used" to "false")))
        assertEquals(ActivationContract.Result.NOT_ALLOWED, ActivationContract.validate("0012", ready + ("allowed" to "true")))
        for (code in listOf(null, "", "123", "12345", "١٢٣٤", "abcd")) assertFalse(ActivationContract.validCode(code))
        for (key in listOf(null, "", " ", "a/b", "a.b", "a#b", "a\$b", "a[b]", "a\nb")) assertFalse(ActivationContract.validKey(key))
        assertTrue(ActivationContract.validKey("-fixture_key-123"))
    }
    @Test fun allLegacyAndRnRoutesAreNullSafeAndKeepLeadingZeroes() {
        assertNull(PushRoute.parse(emptyMap()))
        assertNull(PushRoute.parse(mapOf("type" to "codeApproval")))
        assertNull(PushRoute.parse(mapOf("code" to "abcd")))
        assertNull(PushRoute.parse(mapOf("code" to "0012", "key" to "a/b")))
        assertEquals(PushRoute.Kind.CHAT, PushRoute.parse(mapOf("type" to "newChat"))?.kind)
        assertEquals(PushRoute.Kind.REQUEST, PushRoute.parse(mapOf("type" to "newC", "code" to "0012"))?.kind)
        assertEquals(PushRoute.Kind.GENERIC, PushRoute.parse(mapOf("title" to "title", "msg" to "A&B#\nשלום"))?.kind)
        for (type in listOf("codeApproval", "old-sender")) {
            val route = PushRoute.parse(mapOf("type" to type, "code" to "0012", "key" to "-fixture"))!!
            assertEquals(PushRoute.Kind.ACTIVATION, route.kind)
            assertEquals("0012", route.code)
            assertEquals("-fixture", route.key)
        }
    }
}
