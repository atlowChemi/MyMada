package atlow.chemi.mymada.tools.vitals

import android.graphics.Color
import android.widget.EditText
import android.widget.RadioButton
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.R
import atlow.chemi.mymada.UtillitiesAndTools
import atlow.chemi.mymada.helpers.LocalHelper
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

/** Offline disposable emulator only. Tests use the real Tools route and production Fragment. */
class VitalsScreenTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    @Test fun english() = verify("en")
    @Test fun hebrew() = verify("he")

    private fun verify(language: String) {
        instrumentation.targetContext.getSharedPreferences("Settings", 0).edit()
            .putString("lng", language).putBoolean("privacy", true).putBoolean("oldU", true)
            .putBoolean("Mapp", false).putStringSet("Tools", setOf("vital")).commit()
        instrumentation.runOnMainSync { LocalHelper.setLocale(instrumentation.targetContext, language) }
        ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
            compose.waitForIdle()
            scenario.onActivity {
                assertEquals(it.getString(R.string.button_e), it.title.toString())
                assertEquals(if (language == "en") android.view.View.LAYOUT_DIRECTION_LTR else android.view.View.LAYOUT_DIRECTION_RTL,
                    it.resources.configuration.layoutDirection)
                assertTrue(it.findViewById<RadioButton>(R.id.old).isChecked)
            }
            capture("candidate-$language-default")
            onView(withId(R.id.pulsa)).perform(replaceText("60"), closeSoftKeyboard())
            onView(withId(R.id.breathing)).perform(replaceText("21"), closeSoftKeyboard())
            onView(withId(R.id.systolic)).perform(replaceText("120"), closeSoftKeyboard())
            onView(withId(R.id.diastolic)).perform(replaceText("90"), closeSoftKeyboard())
            compose.waitForIdle()
            scenario.onActivity {
                assertEquals(Color.rgb(70,178,58), it.findViewById<EditText>(R.id.pulsa).currentTextColor)
                assertEquals(Color.rgb(191,21,22), it.findViewById<EditText>(R.id.breathing).currentTextColor)
                assertEquals(Color.rgb(70,178,58), it.findViewById<EditText>(R.id.diastolic).currentTextColor)
            }
            onView(withId(R.id.diastolic)).perform(replaceText("91"), closeSoftKeyboard())
            compose.waitForIdle()
            scenario.onActivity { assertEquals(Color.rgb(191,21,22), it.findViewById<EditText>(R.id.systolic).currentTextColor) }
            onView(withId(R.id.baby)).perform(click())
            onView(withId(R.id.pulsa)).perform(replaceText("125"), closeSoftKeyboard())
            compose.waitForIdle()
            capture("candidate-$language-filled")
            scenario.recreate()
            compose.waitForIdle()
            scenario.onActivity {
                assertTrue(it.findViewById<RadioButton>(R.id.baby).isChecked)
                assertEquals("125", it.findViewById<EditText>(R.id.pulsa).text.toString())
                assertEquals("91", it.findViewById<EditText>(R.id.diastolic).text.toString())
            }
            scenario.moveToState(androidx.lifecycle.Lifecycle.State.CREATED)
            scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED)
            compose.waitForIdle()
            scenario.onActivity { assertEquals("125", it.findViewById<EditText>(R.id.pulsa).text.toString()) }
            onView(withId(R.id.pulsa)).perform(clearText(), closeSoftKeyboard())
            compose.waitForIdle()
            scenario.onActivity { assertEquals(Color.rgb(50,167,195), it.findViewById<EditText>(R.id.pulsa).currentTextColor) }
        }
    }
    private fun capture(name: String) {
        val scale = instrumentation.targetContext.resources.configuration.fontScale
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        File(instrumentation.targetContext.getExternalFilesDir(null), "$name-$scale.png").outputStream().use {
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
    }
}
