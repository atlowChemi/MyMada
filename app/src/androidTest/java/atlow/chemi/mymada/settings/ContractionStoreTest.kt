package atlow.chemi.mymada.settings

import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.contractions.ContractionAway
import atlow.chemi.mymada.contractions.ContractionSession
import atlow.chemi.mymada.contractions.ContractionStore
import org.junit.Assert.*
import org.junit.Test

class ContractionStoreTest {
    @Test fun legacyImportIsLosslessAndRetirementArchivesExactPayloadForBothOrigins() {
        val preferences = InstrumentationRegistry.getInstrumentation().targetContext
            .getSharedPreferences("contractions-policy-test", 0)
        try {
            for (raw in listOf(
                """[{"number":1,"start":1000,"end":2000},{"number":2,"start":3000}]""",
                com.google.gson.Gson().toJson(listOf(atlow.chemi.mymada.classes.Contraction(
                    1, java.util.Date(1000), java.util.Date(2000), null)))
            )) {
                preferences.edit().clear().putString("contractList", raw).putLong("contractLong", 2000).commit()
                val store = ContractionStore(preferences)
                val imported = store.read()
                assertNull(imported.away) // Legacy completion/disposal is not a common away anchor.
                assertEquals(raw, preferences.getString("migration.original.contractList", null))
                val session = ContractionSession(imported.reactNative, imported.entries)
                if (session.active == null) session.toggle(3000)
                store.write(imported.copy(entries = session.entries))
                val cold = ContractionStore(preferences).read()
                assertEquals(session.entries, cold.entries)
                assertNull(cold.entries.last().end)
                assertNull(cold.away) // Abrupt foreground death preserves, without invented expiry.
                store.write(cold.copy(away = ContractionAway(10000, 9000, 1)))
                assertEquals(ContractionAway(10000, 9000, 1), ContractionStore(preferences).read().away)
                for (reason in listOf("expired", "discarded", "deleted")) {
                    val payload = preferences.getString(ContractionStore.KEY, null)
                    store.retire(reason)
                    val count = preferences.getLong("${ContractionStore.KEY}.archive.count", 0)
                    assertEquals(payload, preferences.getString("${ContractionStore.KEY}.archive.$count", null))
                    assertEquals(reason, preferences.getString("${ContractionStore.KEY}.archive.$count.reason", null))
                    val fresh = ContractionStore(preferences).read()
                    assertTrue(fresh.entries.isEmpty())
                    assertFalse(fresh.reactNative)
                    assertNull(fresh.away)
                }
                assertEquals(raw, preferences.getString("contractList", null))
                assertEquals(raw, preferences.getString("migration.original.contractList", null))
                assertEquals(2000L, preferences.getLong("contractLong", 0))
            }
        } finally {
            preferences.edit().clear().commit()
        }
    }
}
