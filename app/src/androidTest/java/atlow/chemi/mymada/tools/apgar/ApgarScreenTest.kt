package atlow.chemi.mymada.tools.apgar

import android.graphics.Bitmap
import android.widget.Spinner
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.*
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.R
import atlow.chemi.mymada.UtillitiesAndTools
import atlow.chemi.mymada.fragments.ApgarCalc
import atlow.chemi.mymada.helpers.LocalHelper
import org.hamcrest.Matchers.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

class ApgarScreenTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    @Test fun choicesRecreationAndBackInBothLanguages() {
        for (language in listOf("en", "he")) {
            instrumentation.runOnMainSync { LocalHelper.setLocale(context, language) }
            context.getSharedPreferences("Settings", 0).edit().putBoolean("Mapp", false)
                .putStringSet("Tools", setOf("apgar")).commit()
            ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
                compose.waitForIdle()
                var format = ""
                var title = ""
                lateinit var labels: List<Array<String>>
                scenario.onActivity {
                    assertTrue(it.supportFragmentManager.findFragmentById(R.id.frame) is ApgarCalc)
                    format = it.getString(R.string.Apgar)
                    title = it.getString(R.string.button_ag)
                    labels = apgarFields.map { field -> it.resources.getStringArray(field.second) }
                }
                capture("default-$language")
                assertScore(format, 0)
                // Every localized choice is selected through the real popup, not by mutating state.
                val values = IntArray(5)
                for (field in 0..4) {
                    for (choice in 0..2) {
                        compose.onNodeWithTag("apgar-choice-$field").performScrollTo()
                        onView(withTagValue(`is`("apgar-choice-$field"))).perform(click())
                        if (field == 0 && choice == 0) capture("options-$language")
                        onData(equalTo(labels[field][choice])).perform(click())
                        values[field] = choice
                        assertScore(format, values.sum())
                    }
                }
                capture("score10-$language")
                val restoredChoices = intArrayOf(2, 1, 0, 2, 1)
                for (field in 0..4) {
                    compose.onNodeWithTag("apgar-choice-$field").performScrollTo()
                    onView(withTagValue(`is`("apgar-choice-$field"))).perform(click())
                    onData(equalTo(labels[field][restoredChoices[field]])).perform(click())
                }
                assertScore(format, 6)
                scenario.recreate()
                assertScore(format, 6)
                compose.onNode(hasText(title) and !hasTestTag("shell-destination-${R.id.apgarCalc}")).assertIsDisplayed()
                for (field in 0..4) onView(withTagValue(`is`("apgar-choice-$field"))).check { view, error ->
                    if (error != null) throw error
                    assertEquals(restoredChoices[field], (view as Spinner).selectedItemPosition)
                }
                // Toolbar drawer back dismisses only the drawer, preserving all choices.
                compose.onNodeWithTag("shell-navigation").performClick()
                pressBack()
                compose.onNodeWithTag("shell-destination-${R.id.apgarCalc}").assertIsNotDisplayed()
                assertScore(format, 6)
                pressBackUnconditionally()
                compose.waitUntil(5_000) { scenario.state == androidx.lifecycle.Lifecycle.State.DESTROYED }
            }
        }
    }

    private fun assertScore(format: String, score: Int) {
        if (context.resources.configuration.fontScale > 1.2f ||
            context.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
            compose.onNodeWithTag("apgar-score").performScrollTo()
        }
        onView(withText(String.format(format, score))).check(matches(isDisplayed()))
    }

    private fun capture(name: String) {
        if (InstrumentationRegistry.getArguments().getString("capture") != "true") return
        instrumentation.uiAutomation.waitForIdle(500, 5_000)
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        File(context.filesDir, "apgar-$name.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
    }
}
