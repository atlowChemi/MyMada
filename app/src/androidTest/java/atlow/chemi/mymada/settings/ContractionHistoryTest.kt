package atlow.chemi.mymada.settings

import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.classes.Contraction
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class ContractionHistoryTest {
    private val preferences = InstrumentationRegistry.getInstrumentation().targetContext
        .getSharedPreferences("contraction-history-test", 0)
    private val now = 1_800_000_000_000L

    @Test fun rnActiveAndCompletedDeriveStateWithoutNativeCounterOrExitAsEnd() {
        val raw = """[{"number":1,"start":${now - 180000},"end":${now - 150000}},
            {"number":2,"start":${now - 100000},"end":${now - 70000}},
            {"number":3,"start":${now - 10000}}]"""
        preferences.edit().clear().putString("contractList", raw).putLong("contractLong", now - 1000).commit()
        val history = ContractionHistory(preferences)
        val saved = history.read()!!
        assertTrue(history.eligible(now))
        assertTrue(history.reactNative)
        assertEquals(2, saved.number)
        assertEquals(now - 70000, saved.completed.last().end.time)
        assertEquals(now - 180000, saved.completed.last().oldEnd.time) // RN start-to-start retained.
        assertEquals(3, saved.active!!.number)
        assertEquals(now - 10000, saved.active.start.time)
        history.save(saved.completed, saved.active.start, saved.number, now)
        val reopened = ContractionHistory(preferences).read()!!
        assertEquals(saved.active.start, reopened.active!!.start)
        assertEquals(3, reopened.active.number)
        assertEquals(now, preferences.getLong("contractLong", 0))
        assertEquals(raw, preferences.getString("migration.original.contractList", null))
        // Complete the restored tail and reopen: no duplicate number, no phantom active record.
        saved.completed.add(Contraction(3, saved.active.start, Date(now + 5000), saved.completed.last().start))
        history.save(saved.completed, null, 3, now + 10000)
        val completed = ContractionHistory(preferences).read()!!
        assertNull(completed.active)
        assertEquals(listOf(1, 2, 3), completed.completed.map { it.number })
        assertEquals(3, completed.number)
        assertEquals(now + 5000, completed.completed.last().end.time)
        assertEquals(raw, preferences.getString("migration.original.contractList", null))
    }

    @Test fun nativeDateRecordsKeepEndToStartAndLastCompletionTimestamp() {
        val entries = listOf(Contraction(1, Date(now - 180000), Date(now - 150000), null),
            Contraction(2, Date(now - 100000), Date(now - 70000), Date(now - 150000)))
        val raw = Gson().toJson(entries)
        preferences.edit().clear().putString("contractList", raw).putInt("contarctNum", 0)
            .putLong("contractLong", now - 70000).commit()
        val history = ContractionHistory(preferences)
        val saved = history.read()!!
        assertFalse(history.reactNative)
        assertEquals(2, saved.number)
        assertEquals(entries.last().oldEnd, saved.completed.last().oldEnd)
        history.save(saved.completed, null, saved.number, now)
        assertEquals(now - 70000, preferences.getLong("contractLong", 0))
        assertEquals(0, preferences.getInt("migration.original.contarctNum", -1))
        assertTrue(history.eligible(now - 70000 + 20 * 60000))
        assertFalse(history.eligible(now - 70000 + 20 * 60000 + 1))
    }

    @Test fun nativeJsonSecondPrecisionDoesNotTruncateCompletionMillisecondsOnReopen() {
        val preciseEnd = now - 70_000 + 67
        val raw = Gson().toJson(listOf(Contraction(1, Date(now - 100_000), Date(preciseEnd), null)))
        preferences.edit().clear().putString("contractList", raw).putLong("contractLong", preciseEnd).commit()
        val history = ContractionHistory(preferences)
        val saved = history.read()!!
        assertEquals(preciseEnd, saved.completed.last().end.time)
        history.save(saved.completed, null, saved.number, now)
        assertEquals(preciseEnd, preferences.getLong("contractLong", 0))
        assertEquals(raw, preferences.getString("contractList", null))
        assertEquals(preciseEnd, ContractionHistory(preferences).read()!!.completed.last().end.time)
        // A stale/mismatched timestamp must not rewrite a different record's end.
        preferences.edit().putLong("contractLong", preciseEnd + 10_000).commit()
        assertEquals(preciseEnd / 1000 * 1000, ContractionHistory(preferences).read()!!.completed.last().end.time)
    }

    @Test fun expirationAndMalformedDataKeepOriginalBeforeAnyReplacement() {
        val raw = """[{"number":1,"start":${now - 2000000},"end":${now - 1900000}}]"""
        preferences.edit().clear().putString("contractList", raw).putLong("contractLong", now - 1800000).commit()
        val history = ContractionHistory(preferences)
        assertNotNull(history.read())
        assertFalse(history.eligible(now))
        history.clear()
        assertEquals(raw, preferences.getString("migration.original.contractList", null))
        assertEquals(now - 1800000, preferences.getLong("migration.original.contractLong", 0))
        for (invalid in listOf("not json", "null", "{}", "[null]",
            """[{"number":1,"start":10,"end":9}]""",
            """[{"number":1,"start":10},{"number":2,"start":20,"end":30}]""",
            """[{"number":1,"start":10.5,"end":20}]""")) {
            preferences.edit().clear().putString("contractList", invalid).commit()
            assertNull(ContractionHistory(preferences).read())
            assertEquals(invalid, preferences.getString("contractList", null))
            assertEquals(invalid, preferences.getString("migration.original.contractList", null))
        }
    }
}
