package atlow.chemi.mymada.settings

import android.graphics.Bitmap
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.R
import atlow.chemi.mymada.UtillitiesAndTools
import atlow.chemi.mymada.helpers.LocalHelper
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ContractionRecoveryTest {
    @Test fun activeOnlyRecoveryCanCancelThenConfirmDeleteAndStayDeletedOnReopen() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val preferences = context.getSharedPreferences("Settings", 0)
        instrumentation.runOnMainSync { LocalHelper.setLocale(context, "en") }
        val now = System.currentTimeMillis()
        val raw = """[{"number":1,"start":${now - 10000}}]"""
        preferences.edit().putBoolean("Mapp", false).putStringSet("Tools", setOf("contrac"))
            .putString("contractList", raw).putLong("contractLong", now).remove("contarctNum")
            .remove("migration.original.contractList").commit()
        ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
            onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click())
            onView(withId(R.id.contra_btn)).check(matches(withText(R.string.endContract)))
            onView(withId(R.id.action_delete)).perform(click())
            onView(withText(R.string.ContracDelete)).inRoot(isDialog()).check(matches(isDisplayed()))
            onView(withId(android.R.id.button2)).inRoot(isDialog()).perform(click())
            onView(withId(R.id.contra_btn)).check(matches(withText(R.string.endContract)))
            assertEquals(raw, preferences.getString("contractList", null))
            onView(withId(R.id.action_delete)).perform(click())
            onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click())
            onView(withId(R.id.contra_btn)).check(matches(withText(R.string.startContract)))
            scenario.recreate()
            onView(withText(R.string.ContracRecover)).check(doesNotExist())
            onView(withId(R.id.contra_btn)).check(matches(withText(R.string.startContract)))
        }
        assertEquals("", preferences.getString("contractList", null))
        assertEquals(raw, preferences.getString("migration.original.contractList", null))
    }

    @Test fun recentRnActiveTailCompletesWithOriginalNumberAndStartInBothLocales() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val preferences = context.getSharedPreferences("Settings", 0)
        for (language in listOf("en", "he")) {
            instrumentation.runOnMainSync { LocalHelper.setLocale(context, language) }
            val now = System.currentTimeMillis()
            val raw = """[{"number":1,"start":${now - 180000},"end":${now - 150000}},
                {"number":2,"start":${now - 100000},"end":${now - 70000}},
                {"number":3,"start":${now - 10000}}]"""
            preferences.edit().putBoolean("Mapp", false).putStringSet("Tools", setOf("contrac"))
                .putString("contractList", raw).putLong("contractLong", now).remove("contarctNum")
                .remove("migration.original.contractList").commit()
            ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
                onView(withText(R.string.ContracRecover)).check(matches(isDisplayed()))
                onView(withId(android.R.id.button1)).perform(click())
                onView(withId(R.id.contra_btn)).check(matches(withText(R.string.endContract)))
                if (InstrumentationRegistry.getArguments().getString("capture") == "true") {
                    // The ring animates continuously; wait for the main queue rather than UIAutomator idle.
                    instrumentation.waitForIdleSync()
                    val bitmap = instrumentation.uiAutomation.takeScreenshot()
                    File(context.filesDir, "contractions-recovered-$language.png").outputStream().use {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                    }
                    bitmap.recycle()
                }
                onView(withId(R.id.contra_btn)).perform(click())
                onView(withId(R.id.contra_btn)).check(matches(withText(R.string.startContract)))
                scenario.recreate()
                onView(withText(R.string.ContracRecover)).check(matches(isDisplayed()))
                onView(withId(android.R.id.button1)).perform(click())
                onView(withId(R.id.contra_btn)).check(matches(withText(R.string.startContract)))
            }
            val saved = ContractionHistory(preferences).read()!!
            assertNull(saved.active)
            assertEquals(listOf(1, 2, 3), saved.completed.map { it.number })
            assertEquals(now - 10000, saved.completed.last().start.time)
            assertEquals(now - 100000, saved.completed.last().oldEnd.time)
            assertEquals(raw, preferences.getString("migration.original.contractList", null))
        }
    }
}
