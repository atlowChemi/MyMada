package atlow.chemi.mymada.helpers

import atlow.chemi.mymada.classes.Contraction
import com.google.gson.Gson
import com.google.gson.JsonParser
import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class ContractionContractTest {
    @Test fun legacyConstructorsAccessorsAndDateJsonKeepTheirContract() {
        val type = Contraction::class.java
        val text = type.getConstructor(Int::class.javaPrimitiveType, String::class.java, String::class.java)
            .newInstance(7, "length", "previous")
        assertNull(type.getMethod("getStart").invoke(text))
        assertEquals("length", type.getMethod("getLength").invoke(text))
        assertEquals("previous", type.getMethod("getPrevious").invoke(text))
        val start = Date(1_800_000_000_000)
        val end = Date(start.time + 5000)
        val entry = type.getConstructor(Int::class.javaPrimitiveType, Date::class.java, Date::class.java, Date::class.java)
            .newInstance(8, start, end, null)
        type.getMethod("setOldEnd", Date::class.java).invoke(entry, start)
        assertSame(start, entry.oldEnd)
        val gson = Gson()
        val json = gson.toJson(entry)
        val fields = JsonParser.parseString(json).asJsonObject
        assertEquals(setOf("start", "end", "oldEnd", "number"), fields.keySet())
        assertTrue(fields["start"].asJsonPrimitive.isString)
        val restored = gson.fromJson(json, type)
        assertEquals(start, restored.start)
        assertEquals(end, restored.end)
        assertEquals(start, restored.oldEnd)
        assertEquals(8, restored.number)
        val missing = gson.fromJson("""{"number":9}""", type)
        assertNull(missing.start)
        assertNull(missing.end)
        assertNull(missing.oldEnd)
        assertEquals(setOf("number", "previous", "length"), JsonParser.parseString(gson.toJson(text)).asJsonObject.keySet())
    }
}
