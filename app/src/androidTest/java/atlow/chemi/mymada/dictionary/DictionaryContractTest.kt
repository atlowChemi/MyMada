package atlow.chemi.mymada.dictionary

import org.junit.Assert.*
import org.junit.Test

/** Pure Kotlin/JVM; can also run through AndroidJUnitRunner without Firebase. */
class DictionaryContractTest {
    @Test fun schemasSearchAndModeration() {
        val a = DictionaryEntry("actual", "Alpha", "Hebrew עברית meaning")
        val b = DictionaryEntry("b", "beta", "Second")
        assertEquals(listOf(a, b), dictionarySearch(listOf(b, a), ""))
        assertEquals(listOf(a), dictionarySearch(listOf(a, b), "ALPHA"))
        assertEquals(listOf(a), dictionarySearch(listOf(a, b), "meaning"))
        assertEquals(listOf(a), dictionarySearch(listOf(a, b), "עברית"))
        assertTrue(dictionarySearch(listOf(a, b), "absent").isEmpty())
        assertEquals(a, dictionaryEntry("actual", mapOf("key" to "forged", "name" to a.name, "definition" to a.definition, "approved" to true)))
        for (data in listOf(emptyMap(), mapOf("approved" to "true"), mapOf("approved" to true, "name" to 5),
            mapOf("approved" to true, "name" to "bad", "definition" to null),
            mapOf("approved" to false, "name" to a.name, "definition" to a.definition))) assertNull(dictionaryEntry("id", data))
        assertEquals(mapOf("key" to "id", "name" to "term", "definition" to "meaning", "approved" to false),
            dictionarySubmission("id", null, " term ", " meaning\n"))
        assertEquals(mapOf("newName" to "Changed"), dictionarySubmission(a.key, a, " Changed ", a.definition))
        assertEquals(mapOf("newDefinition" to "Changed"), dictionarySubmission(a.key, a, a.name, "Changed"))
        assertEquals(setOf("newName", "newDefinition"), dictionarySubmission(a.key, a, "new", "meaning").keys)
        for (operation in listOf<() -> Unit>(
            { dictionarySubmission("id", null, "  ", "definition") },
            { dictionarySubmission("id", null, "name", "\n") },
            { dictionarySubmission(a.key, a, a.name, a.definition) },
            { dictionarySubmission("wrong", a, "new", a.definition) }
        )) {
            try { operation(); fail("Invalid submission accepted") } catch (_: IllegalArgumentException) { }
        }
    }
}
