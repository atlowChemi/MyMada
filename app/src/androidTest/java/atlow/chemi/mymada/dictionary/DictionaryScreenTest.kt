package atlow.chemi.mymada.dictionary

import android.graphics.Bitmap
import android.os.Bundle
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.Espresso.pressBackUnconditionally
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.R
import atlow.chemi.mymada.UtillitiesAndTools
import atlow.chemi.mymada.fragments.DictionaryAdmin
import atlow.chemi.mymada.helpers.LocalHelper
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

class DictionaryScreenTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    @Test fun fixtureSearchEditAddRecoveryAndFailures() {
        val languages = InstrumentationRegistry.getArguments().getString("language")?.let { listOf(it) } ?: listOf("en", "he")
        for (language in languages) {
            instrumentation.runOnMainSync { LocalHelper.setLocale(context, language) }
            context.getSharedPreferences("Settings", 0).edit().putBoolean("Mapp", false)
                .putStringSet("Tools", setOf("dict")).commit()
            val source = FixtureSource()
            val model = DictionaryModel(source)
            ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
                scenario.onActivity { activity ->
                    activity.supportFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
                        override fun onFragmentPreCreated(fm: FragmentManager, fragment: Fragment, state: Bundle?) {
                            if (fragment is DictionaryAdmin) ViewModelProvider(fragment, object : ViewModelProvider.Factory {
                                @Suppress("UNCHECKED_CAST")
                                override fun <T : ViewModel> create(modelClass: Class<T>): T = model as T
                            })[DictionaryModel::class.java]
                        }
                    }, false)
                    activity.supportFragmentManager.beginTransaction().replace(R.id.frame, DictionaryAdmin()).commitNow()
                }
                compose.waitForIdle()
                assertTrue(model.loading)
                capture("loading-$language")
                scenario.onActivity { source.emit(emptyList()) }
                capture("empty-$language")
                onView(withId(R.id.app_bar_word_add)).perform(click())
                capture("add-$language")
                onView(withId(android.R.id.button1)).perform(click())
                assertEquals(0, source.writes.size)
                onView(withId(R.id.wordName)).perform(replaceText("  New term  "))
                onView(withId(R.id.wordDefinition)).perform(replaceText("  New meaning  "), closeSoftKeyboard())
                scenario.recreate()
                onView(withId(R.id.wordName)).check(matches(withText("  New term  ")))
                onView(withId(R.id.wordDefinition)).check(matches(withText("  New meaning  ")))
                onView(withId(android.R.id.button1)).perform(click())
                onView(withId(android.R.id.button1)).check(matches(isNotEnabled()))
                assertEquals(1, source.writes.size)
                val addition = source.writes.single()
                assertFalse(addition.edit)
                assertEquals(mapOf("key" to addition.key, "name" to "New term", "definition" to "New meaning", "approved" to false), addition.fields)
                capture("pending-$language")
                scenario.recreate()
                onView(withId(android.R.id.button1)).check(matches(isNotEnabled()))
                scenario.onActivity { source.complete(Exception("fixture permission denied")) }
                onView(withId(android.R.id.button1)).check(matches(isEnabled()))
                onView(withId(R.id.wordName)).check(matches(withText("  New term  ")))
                capture("write-error-$language")
                onView(withId(android.R.id.button1)).perform(click())
                assertEquals(addition, source.writes.last())
                scenario.onActivity { source.complete(null) }
                compose.waitForIdle()
                scenario.onActivity { source.emit(listOf(beta, alpha), true) }
                capture("cached-$language")
                scenario.onActivity { source.emit(listOf(beta, alpha)) }
                capture("list-$language")
                onView(withId(R.id.app_bar_search)).perform(click())
                onView(withId(androidx.appcompat.R.id.search_src_text)).perform(replaceText("meaning"), pressImeActionButton())
                compose.onNodeWithText("Alpha").assertExists()
                compose.onNodeWithText("beta").assertDoesNotExist()
                scenario.recreate()
                onView(withId(androidx.appcompat.R.id.search_src_text)).check(matches(withText("meaning")))
                compose.onNodeWithText("Alpha").performClick()
                capture("details-$language")
                onView(withId(android.R.id.button1)).perform(click())
                capture("edit-$language")
                onView(withId(android.R.id.button1)).perform(click())
                assertEquals(2, source.writes.size) // unchanged edit is rejected
                onView(withId(R.id.wordName)).perform(replaceText("Changed"), closeSoftKeyboard())
                onView(withId(android.R.id.button1)).perform(click())
                assertEquals(Write("a", true, mapOf("newName" to "Changed")), source.writes.last())
                scenario.onActivity { source.complete(null); source.emit(listOf(beta)) }
                compose.onNodeWithText("Alpha").assertDoesNotExist()
                scenario.onActivity { source.emit(emptyList()) }
                assertTrue(model.entries.isEmpty())
                scenario.onActivity { source.emit(listOf(alpha)); source.error() }
                assertTrue(model.readError)
                assertEquals(listOf(alpha), model.entries)
                capture("read-error-$language")
                scenario.onActivity { model.retry(); source.emit(listOf(beta)) }
                assertFalse(model.readError)
                assertTrue(source.removals > 0)
                scenario.onActivity {
                    it.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).menu.findItem(R.id.app_bar_search).collapseActionView()
                    it.findViewById<androidx.drawerlayout.widget.DrawerLayout>(R.id.activity_Utils)
                        .openDrawer(androidx.core.view.GravityCompat.START, false)
                }
                pressBack()
                compose.waitUntil(5_000) {
                    var closed = false
                    scenario.onActivity { closed = !it.findViewById<androidx.drawerlayout.widget.DrawerLayout>(R.id.activity_Utils)
                        .isDrawerVisible(androidx.core.view.GravityCompat.START) }
                    closed
                }
                pressBackUnconditionally()
                compose.waitUntil(5_000) { scenario.state == androidx.lifecycle.Lifecycle.State.DESTROYED }
            }
            assertTrue(source.removals > 0)
        }
    }

    @Test fun listenerCancellationRejectsLateSnapshots() {
        val source = FixtureSource()
        val model = DictionaryModel(source)
        instrumentation.runOnMainSync {
            model.start()
            val old = source.listener!!
            source.emit(listOf(alpha))
            model.stop()
            old(listOf(beta), false, null)
            assertEquals(listOf(alpha), model.entries)
            model.start()
            source.emit(emptyList())
            assertTrue(model.entries.isEmpty())
            model.stop()
        }
    }

    private fun capture(name: String) {
        if (InstrumentationRegistry.getArguments().getString("capture") != "true") return
        instrumentation.uiAutomation.waitForIdle(400, 5_000)
        val image = instrumentation.uiAutomation.takeScreenshot()
        File(context.filesDir, "dictionary-$name.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
        image.recycle()
    }

    private data class Write(val key: String, val edit: Boolean, val fields: Map<String, Any>)
    private class FixtureSource : DictionarySource {
        private var nextKey = 0
        override fun newKey(): String = "fixture-${nextKey++}"
        var listener: ((List<DictionaryEntry>?, Boolean, Exception?) -> Unit)? = null
        var completion: ((Exception?) -> Unit)? = null
        var removals = 0
        val writes = mutableListOf<Write>()
        override fun listen(result: (List<DictionaryEntry>?, Boolean, Exception?) -> Unit): () -> Unit {
            listener = result
            return { removals++ }
        }
        override fun submit(key: String, edit: Boolean, fields: Map<String, Any>, result: (Exception?) -> Unit) {
            writes += Write(key, edit, fields)
            completion = result
        }
        fun emit(entries: List<DictionaryEntry>, cached: Boolean = false) { listener!!(entries, cached, null) }
        fun error() { listener!!(null, false, Exception("fixture read denied")) }
        fun complete(error: Exception?) { completion!!(error) }
    }

    companion object {
        private val alpha = DictionaryEntry("a", "Alpha", "meaning עברית")
        private val beta = DictionaryEntry("b", "beta", "second")
    }
}
