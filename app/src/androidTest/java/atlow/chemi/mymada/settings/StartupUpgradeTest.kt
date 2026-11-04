package atlow.chemi.mymada.settings

import android.app.LocaleManager
import android.os.Build
import android.os.LocaleList
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test

/** Run seed, force-stop, then verify in a new process; never run against a personal installation. */
class StartupUpgradeTest {
    @Test fun coldStartFromRnFixture() {
        val mode = InstrumentationRegistry.getArguments().getString("startupFixture")
        assumeTrue(mode == "seed" || mode == "verify")
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val settings = context.getSharedPreferences("Settings", 0)
        if (mode == "seed") {
            settings.edit().clear().putString("Mapp", "false").putString("oldU", "true")
                .putString("privacy", "true").putString("realM", "true")
                .putString("Tools", "[metro, oxy]").putString("merhav", "4")
                .putString("lng", "en").putString("first_time_migration", "status_done")
                .putString("firebaseGenratedKey", "offline-fixture-key").commit()
            context.getSharedPreferences("rate_app", 0).edit().clear()
                .putString("dontshowagain", "true").putString("launch_count", "12").commit()
            if (Build.VERSION.SDK_INT >= 33) {
                context.getSystemService(LocaleManager::class.java).applicationLocales = LocaleList.forLanguageTags("he")
            } else {
                context.openFileOutput("androidx.appcompat.app.AppCompatDelegate.application_locales_record_file", 0).use {
                    it.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><locales application_locales=\"he\"/>".toByteArray())
                }
            }
        } else {
            assertFalse(settings.getBoolean("Mapp", true))
            assertTrue(settings.getBoolean("realM", false))
            assertEquals("iw", settings.getString("lng", null))
            assertEquals("en", settings.getString("migration.original.lng", null))
            assertEquals(setOf("metro", "oxy"), settings.getStringSet("Tools", null))
            assertEquals(4, settings.getInt("merhav", -1))
            assertEquals("offline-fixture-key", settings.getString("firebaseGenratedKey", null))
            assertTrue(context.getSharedPreferences("rate_app", 0).getBoolean("dontshowagain", false))
            assertEquals("false", settings.getString("migration.original.Mapp", null))
        }
    }
}
