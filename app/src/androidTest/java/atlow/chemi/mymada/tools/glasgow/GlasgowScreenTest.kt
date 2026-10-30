package atlow.chemi.mymada.tools.glasgow

import android.graphics.Bitmap
import android.widget.Spinner
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.R
import atlow.chemi.mymada.UtillitiesAndTools
import atlow.chemi.mymada.fragments.GlazgoCalc
import atlow.chemi.mymada.helpers.LocalHelper
import org.hamcrest.Matchers.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

class GlasgowScreenTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    @Test fun choicesRecreationAndBackInBothLanguages() {
        val languages = InstrumentationRegistry.getArguments().getString("language")?.let { listOf(it) }
            ?: listOf("en", "he")
        for (language in languages) {
            instrumentation.runOnMainSync { LocalHelper.setLocale(context, language) }
            context.getSharedPreferences("Settings", 0).edit().putBoolean("Mapp", false)
                .putStringSet("Tools", setOf("glaz")).commit()
            ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
                var format = ""
                var title = ""
                lateinit var bands: List<String>
                lateinit var labels: List<Array<String>>
                scenario.onActivity {
                    assertTrue(it.supportFragmentManager.findFragmentById(R.id.frame) is GlazgoCalc)
                    format = it.getString(R.string.Glasgow)
                    title = it.getString(R.string.button_gl)
                    bands = listOf(R.string.GlasgowHigh, R.string.GlasgowMed, R.string.GlasgowLow).map(it::getString)
                    labels = glasgowFields.map { field -> it.resources.getStringArray(field.second) }
                }
                fun assertScore(score: Int) {
                    if (context.resources.configuration.fontScale > 1.2f &&
                        context.resources.configuration.orientation != android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
                        compose.onNodeWithTag("glasgow-score").performScrollTo()
                    }
                    val band = bands[when { score < 9 -> 0; score > 12 -> 2; else -> 1 }]
                    onView(withText(String.format(format, "$score ($band)"))).check(matches(isDisplayed()))
                }
                assertScore(3)
                capture("default-$language")
                val values = IntArray(3)
                for (field in 0..2) {
                    for (choice in labels[field].indices) {
                        compose.onNodeWithTag("glasgow-choice-$field").performScrollTo()
                        onView(withTagValue(`is`("glasgow-choice-$field"))).perform(click())
                        if (field == 0 && choice == 0) capture("options-$language")
                        onData(equalTo(labels[field][choice])).perform(click())
                        values[field] = choice
                        assertScore(values.sum() + 3)
                    }
                }
                capture("score15-$language")
                val restoredChoices = intArrayOf(2, 1, 3)
                for (field in 0..2) {
                    compose.onNodeWithTag("glasgow-choice-$field").performScrollTo()
                    onView(withTagValue(`is`("glasgow-choice-$field"))).perform(click())
                    onData(equalTo(labels[field][restoredChoices[field]])).perform(click())
                }
                assertScore(9)
                scenario.recreate()
                assertScore(9)
                onView(allOf(withText(title), isDescendantOfA(withId(R.id.toolbar)))).check(matches(isDisplayed()))
                for (field in 0..2) onView(withTagValue(`is`("glasgow-choice-$field"))).check { view, error ->
                    if (error != null) throw error
                    assertEquals(restoredChoices[field], (view as Spinner).selectedItemPosition)
                }
                scenario.onActivity { it.findViewById<androidx.drawerlayout.widget.DrawerLayout>(R.id.activity_Utils)
                    .openDrawer(androidx.core.view.GravityCompat.START, false) }
                pressBack()
                compose.waitUntil(5_000) {
                    var closed = false
                    scenario.onActivity { closed = !it.findViewById<androidx.drawerlayout.widget.DrawerLayout>(R.id.activity_Utils)
                        .isDrawerVisible(androidx.core.view.GravityCompat.START) }
                    closed
                }
                assertScore(9)
                pressBackUnconditionally()
                compose.waitUntil(5_000) { scenario.state == androidx.lifecycle.Lifecycle.State.DESTROYED }
            }
        }
    }

    private fun capture(name: String) {
        if (InstrumentationRegistry.getArguments().getString("capture") != "true") return
        instrumentation.uiAutomation.waitForIdle(500, 5_000)
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        File(context.filesDir, "glasgow-$name.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
    }
}
