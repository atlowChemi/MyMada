package atlow.chemi.mymada.settings

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test

class PreferenceMigrationTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun nativeRnMalformedAndInterruptedFilesRetainOriginals() {
        val settings = context.getSharedPreferences("migration-fixture", 0)
        try {
            settings.edit().clear().putString("Mapp", "false").putBoolean("realM", true)
                .putString("privacy", "true").putString("merhav", "11")
                .putString("nightStart", "24").putString("Tools", "[metro, oxy, futureTool]")
                .putString("contractLong", "1793376000123").putString("contarctNum", "2147483648")
                .putString("timeK", "not-a-number")
                .putString("firebaseGenratedKey", "original-key").putString("UserName", "שם")
                .putString("NuRiT1", "content://media/external/audio/media/19")
                .putString("contractList", "[{\"start\":1793376000000}]").commit()
            assertTrue(PreferenceMigration.migrateFile(settings, "Settings"))
            assertFalse(settings.getBoolean("Mapp", true))
            assertTrue(settings.getBoolean("realM", false))
            assertTrue(settings.getBoolean("privacy", false))
            assertEquals(11, settings.getInt("merhav", 0))
            assertEquals(23, settings.getInt("nightStart", 23))
            assertEquals("24", settings.getString("migration.original.nightStart", null))
            assertEquals(setOf("metro", "oxy", "futureTool"), settings.getStringSet("Tools", null))
            assertEquals(1793376000123L, settings.getLong("contractLong", 0))
            assertFalse(settings.contains("contarctNum"))
            assertEquals("6", settings.getString("timeK", "6"))
            assertEquals("not-a-number", settings.getString("migration.original.timeK", null))
            assertEquals("original-key", settings.getString("firebaseGenratedKey", null))
            assertEquals("שם", settings.getString("UserName", null))
            assertEquals("content://media/external/audio/media/19", settings.getString("NuRiT1", null))
            assertEquals("[{\"start\":1793376000000}]", settings.getString("contractList", null))
            val once = settings.all
            assertTrue(PreferenceMigration.migrateFile(settings, "Settings"))
            assertEquals(once, settings.all)
            // Simulate a stop between files, then resume independently without a global done marker.
            settings.edit().clear().putString("launch_count", "17").putString("dontshowagain", "garbage").commit()
            assertTrue(PreferenceMigration.migrateFile(settings, "rate_app"))
            assertEquals(17L, settings.getLong("launch_count", 0))
            assertFalse(settings.getBoolean("dontshowagain", false))
            settings.edit().clear().putString("android.permission.READ_EXTERNAL_STORAGE", "true").commit()
            assertTrue(PreferenceMigration.migrateFile(settings, "permissionStatus"))
            assertTrue(settings.getBoolean("android.permission.READ_EXTERNAL_STORAGE", false))
        } finally { settings.edit().clear().commit() }
    }

    @Test fun absentAndEmptySelectionKeepTheirDifferentDefaults() {
        val preferences = context.getSharedPreferences("migration-empty-fixture", 0)
        try {
            preferences.edit().clear().commit()
            assertTrue(PreferenceMigration.migrateFile(preferences, "Settings"))
            assertTrue(preferences.all.isEmpty())
            preferences.edit().putString("Tools", "[]").commit()
            PreferenceMigration.migrateFile(preferences, "Settings")
            assertEquals(emptySet<String>(), preferences.getStringSet("Tools", setOf("metro")))
        } finally { preferences.edit().clear().commit() }
    }
}
