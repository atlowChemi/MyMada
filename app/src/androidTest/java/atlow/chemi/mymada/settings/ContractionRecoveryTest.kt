package atlow.chemi.mymada.settings

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.R
import atlow.chemi.mymada.UtillitiesAndTools
import atlow.chemi.mymada.contractions.ContractionEntry
import atlow.chemi.mymada.contractions.ContractionSession
import atlow.chemi.mymada.contractions.ContractionStore
import atlow.chemi.mymada.contractions.ContractionAway
import atlow.chemi.mymada.helpers.LocalHelper
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

class ContractionRecoveryTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val preferences = context.getSharedPreferences("Settings", 0)
    private var labels = mapOf<Int, String>()
    private fun prepare(raw: String = "", language: String = "en") {
        instrumentation.uiAutomation.setRotation(android.app.UiAutomation.ROTATION_FREEZE_0)
        instrumentation.runOnMainSync { LocalHelper.setLocale(context, language) }
        preferences.edit().putBoolean("Mapp", false).putStringSet("Tools", setOf("contrac"))
            .putString("contractList", raw).putLong("contractLong", System.currentTimeMillis())
            .remove("contarctNum").remove("migration.original.contractList").remove(ContractionStore.KEY).commit()
    }
    private fun labels(scenario: ActivityScenario<UtillitiesAndTools>) {
        scenario.onActivity { activity ->
            val actualScale = activity.resources.configuration.fontScale
            File(context.filesDir, "contractions-environment.txt").appendText("locale=${activity.resources.configuration.locales} fontScale=$actualScale density=${activity.resources.displayMetrics.density}\n")
            InstrumentationRegistry.getArguments().getString("expectedFontScale")?.toFloat()?.let {
                assertEquals("Actual activity font scale", it, actualScale, .01f)
            }
            labels = listOf(R.string.ok, R.string.dialog_no,
            R.string.ContracRecover, R.string.ContracDelete, R.string.startContract, R.string.endContract)
            .associateWith { activity.getString(it) } }
    }
    private fun choose(id: Int) = onView(withId(if (id == R.string.ok) android.R.id.button1 else android.R.id.button2))
        .inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).perform(click())
    private fun toggle() = compose.onNodeWithTag("contraction-toggle")
    private fun expect(id: Int) {
        toggle().assertTextContains(labels.getValue(id))
        val layouts = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
        toggle().performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult) { it(layouts) }
        val density = layouts.single().layoutInput.density
        File(context.filesDir, "contractions-environment.txt").appendText("Compose fontScale=${density.fontScale} text=${layouts.single().layoutInput.style.fontSize}\n")
        InstrumentationRegistry.getArguments().getString("expectedFontScale")?.toFloat()?.let {
            assertEquals("Actual Compose font scale", it, density.fontScale, .01f)
        }
    }
    private fun delete(scenario: ActivityScenario<UtillitiesAndTools>) {
        // A dismissed dialog can still own input focus when the underlying tree is visible.
        compose.waitUntil(5_000) {
            var focused = false
            scenario.onActivity { focused = it.hasWindowFocus() }
            focused
        }
        instrumentation.uiAutomation.waitForIdle(500, 5_000)
        onView(withId(R.id.action_delete)).perform(click())
        instrumentation.uiAutomation.waitForIdle(500, 5_000)
        try {
            onView(withId(android.R.id.message)).inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog())
                .check(androidx.test.espresso.assertion.ViewAssertions.matches(androidx.test.espresso.matcher.ViewMatchers.withText(labels.getValue(R.string.ContracDelete))))
        } catch (failure: AssertionError) {
            capture("delete-failure", scenario)
            throw failure
        }
    }
    private fun rotate(scenario: ActivityScenario<UtillitiesAndTools>, landscape: Boolean) {
        scenario.onActivity { it.requestedOrientation = if (landscape)
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
        // Wait for the replacement window, rather than awaiting a frame on the destroyed one.
        compose.waitUntil(10_000) {
            var ready = false
            scenario.onActivity { ready = it.hasWindowFocus() &&
                (it.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) == landscape }
            ready
        }
        instrumentation.waitForIdleSync()
    }

    @Test fun recoveryDeleteAndEmptyDeleteUseNativeAlertWidth() {
        prepare("""[{"number":1,"start":${System.currentTimeMillis() - 10000}}]""")
        ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
            labels(scenario)
            for ((index, message) in listOf(R.string.ContracRecover, R.string.ContracDelete, R.string.ContracDeleteEmpty).withIndex()) {
                if (index > 0) {
                    compose.waitUntil(5_000) {
                        var focused = false
                        scenario.onActivity { focused = it.hasWindowFocus() }
                        focused
                    }
                    onView(withId(R.id.action_delete)).perform(click())
                }
                onView(withId(android.R.id.message)).inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog())
                    .check { view, error ->
                        if (error != null) throw error
                        val text = view as android.widget.TextView
                        assertEquals(text.context.getString(message), text.text.toString())
                        val metrics = text.resources.displayMetrics
                        if (metrics.widthPixels == 1080 && metrics.density == 2.625f) {
                            val panel = text.rootView.findViewById<android.view.View>(androidx.appcompat.R.id.parentPanel)
                            assertEquals("Native alert content width", 940, panel.width)
                            if (message == R.string.ContracDeleteEmpty && text.resources.configuration.fontScale == 1f)
                                assertEquals("Empty-delete message must not gain a wrapped line", 1, text.lineCount)
                        }
                    }
                capture("dialog-$index-en", scenario)
                choose(R.string.ok)
                compose.waitForIdle()
            }
        }
    }

    @Test fun activeOnlyRecoveryCanCancelThenConfirmDeleteAndStayDeletedOnReopen() {
        val raw = """[{"number":1,"start":${System.currentTimeMillis() - 10000}}]"""
        prepare(raw)
        ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
            labels(scenario)
            scenario.recreate() // The unanswered recovery dialog survives recreation.
            choose(R.string.ok)
            expect(R.string.endContract)
            delete(scenario)
            choose(R.string.dialog_no)
            expect(R.string.endContract)
            assertEquals(raw, preferences.getString("contractList", null))
            delete(scenario)
            scenario.recreate() // The unanswered delete dialog survives recreation.
            choose(R.string.ok)
            expect(R.string.startContract)
        }
        ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
            labels(scenario)
            onView(withId(android.R.id.message)).check(androidx.test.espresso.assertion.ViewAssertions.doesNotExist())
            expect(R.string.startContract)
            toggle().performClick()
            scenario.recreate()
            expect(R.string.endContract)
            toggle().performClick()
            scenario.recreate()
            expect(R.string.startContract)
            val fresh = ContractionStore(preferences).read()
            assertEquals(1, fresh.entries.single().number)
            assertFalse(fresh.reactNative)
            delete(scenario)
            choose(R.string.ok)
        }
        assertTrue(ContractionStore(preferences).read().entries.isEmpty())
        assertEquals(raw, preferences.getString("contractList", null))
        assertEquals(raw, preferences.getString("migration.original.contractList", null))
    }

    @Test fun recentRnActiveTailCompletesWithOriginalNumberAndStartInBothLocales() {
        for (language in listOf("en", "he")) {
            val now = System.currentTimeMillis()
            val raw = """[{"number":1,"start":${now - 180000},"end":${now - 150000}},
                {"number":2,"start":${now - 100000},"end":${now - 70000}},
                {"number":3,"start":${now - 10000}}]"""
            prepare(raw, language)
            ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
                labels(scenario)
                choose(R.string.ok)
                expect(R.string.endContract)
                scenario.moveToState(Lifecycle.State.CREATED)
                scenario.moveToState(Lifecycle.State.RESUMED)
                scenario.recreate()
                expect(R.string.endContract)
                capture("active-$language", scenario)
                toggle().performClick()
                expect(R.string.startContract)
                scenario.recreate()
                expect(R.string.startContract)
                capture("completed-$language", scenario)
            }
            ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
                labels(scenario)
                choose(R.string.ok)
                expect(R.string.startContract)
            }
            val saved = ContractionStore(preferences).read()
            assertNotNull(saved.entries.last().end)
            assertEquals(listOf(1, 2, 3), saved.entries.map { it.number })
            assertEquals(now - 10000, saved.entries.last().start)
            assertEquals(now - 100000, saved.entries.last().previous)
            assertEquals(raw, preferences.getString("migration.original.contractList", null))
        }
    }

    @Test fun nativeIncompleteSurvivesBackgroundAndRecreationAndRecoveryCanBeDeclined() {
        prepare()
        ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
            labels(scenario)
            capture("default-en", scenario)
            toggle().performClick()
            scenario.moveToState(Lifecycle.State.CREATED)
            scenario.moveToState(Lifecycle.State.RESUMED)
            scenario.recreate()
            expect(R.string.endContract)
            capture("native-active-en", scenario)
            toggle().performClick()
            expect(R.string.startContract)
        }
        ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
            labels(scenario)
            choose(R.string.dialog_no)
            expect(R.string.startContract)
        }
        assertTrue(ContractionStore(preferences).read().entries.isEmpty())
    }

    @Test fun timekeepingPreservesProvenanceNumberingAndClockRollbackBoundaries() {
        for (rn in listOf(false, true)) {
            val session = ContractionSession(rn, listOf(ContractionEntry(7, 1000, 2000, null)))
            session.toggle(3000)
            assertEquals(8, session.active!!.number)
            assertEquals(1000L, session.active!!.previous)
            session.toggle(2999)
            assertEquals(3000L, session.entries.last().end)
            session.clear()
            session.toggle(5000)
            assertEquals(1, session.active!!.number)
        }
    }

    @Test fun activeOnlyColdReopenAndObservedAwayExpiryArchiveBeforeNewSession() {
        prepare()
        val store = ContractionStore(preferences)
        ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
            labels(scenario)
            toggle().performClick()
            scenario.recreate()
            expect(R.string.endContract)
            assertNull(store.read().away) // Rotation/resume does not leave an expiry anchor.
        }
        val active = store.read().entries.single()
        assertNull(active.end)
        ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
            labels(scenario)
            choose(R.string.ok)
            expect(R.string.endContract)
            assertEquals(active, store.read().entries.single())
            for (minutes in listOf(15L, 21L)) {
                scenario.moveToState(Lifecycle.State.CREATED)
                val saved = store.read()
                val away = saved.away!!
                store.write(saved.copy(away = ContractionAway(away.wall - minutes * 60000,
                    away.elapsed - minutes * 60000, away.boot)))
                val retiredPayload = preferences.getString(ContractionStore.KEY, null)
                scenario.moveToState(Lifecycle.State.RESUMED)
                if (minutes == 15L) {
                    expect(R.string.endContract)
                    assertEquals(active, store.read().entries.single())
                } else {
                    expect(R.string.startContract)
                    assertTrue(store.read().entries.isEmpty())
                    val count = preferences.getLong("${ContractionStore.KEY}.archive.count", 0)
                    assertEquals(retiredPayload, preferences.getString("${ContractionStore.KEY}.archive.$count", null))
                    toggle().performClick()
                    assertEquals(1, store.read().entries.single().number)
                    assertNull(store.read().entries.single().previous)
                }
            }
        }
    }

    @Test fun nativeCompletedHistoryPreservesEndToStartIntervalsInBothLocalesAndLandscape() {
        for (language in listOf("en", "he")) {
            val now = System.currentTimeMillis()
            val records = listOf(
                atlow.chemi.mymada.classes.Contraction(1, java.util.Date(now - 180000), java.util.Date(now - 150000), null),
                atlow.chemi.mymada.classes.Contraction(2, java.util.Date(now - 100000), java.util.Date(now - 70000), java.util.Date(now - 150000)))
            prepare(com.google.gson.Gson().toJson(records), language)
            preferences.edit().putLong("contractLong", now - 70000).commit()
            ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
                labels(scenario)
                capture("native-recover-$language", scenario)
                choose(R.string.ok)
                expect(R.string.startContract)
                capture("native-history-$language", scenario)
                rotate(scenario, landscape = true)
                expect(R.string.startContract)
                capture("native-landscape-$language", scenario)
                rotate(scenario, landscape = false)
                toggle().performClick()
                expect(R.string.endContract)
                capture("native-history-active-$language", scenario)
                toggle().performClick()
                expect(R.string.startContract)
                capture("native-history-completed-$language", scenario)
            }
            val saved = ContractionStore(preferences).read()
            assertFalse(saved.reactNative)
            assertEquals(50_000, saved.entries[1].start - saved.entries[1].previous!!)
            assertEquals(saved.entries[1].start, saved.entries.last().previous)
        }
    }

    private fun capture(name: String, scenario: ActivityScenario<UtillitiesAndTools>) {
        if (InstrumentationRegistry.getArguments().getString("capture") != "true") return
        compose.waitUntil(10_000) {
            var ready = false
            scenario.onActivity { ready = it.hasWindowFocus() }
            // Recovery dialogs own focus instead of the Activity.
            ready || name.startsWith("native-recover") || name.startsWith("dialog-") || name == "delete-failure"
        }
        compose.waitForIdle()
        instrumentation.waitForIdleSync()
        instrumentation.uiAutomation.waitForIdle(500, 5_000)
        val drawn = java.util.concurrent.CountDownLatch(1)
        scenario.onActivity { activity ->
            activity.window.decorView.postOnAnimation {
                activity.window.decorView.postOnAnimation { drawn.countDown() }
            }
        }
        assertTrue("Current Activity rendered before $name", drawn.await(5, java.util.concurrent.TimeUnit.SECONDS))
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        File(context.filesDir, "contractions-$name.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
    }
}
