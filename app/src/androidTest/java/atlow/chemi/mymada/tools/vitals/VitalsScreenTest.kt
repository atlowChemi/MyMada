package atlow.chemi.mymada.tools.vitals

import android.graphics.Color
import android.widget.EditText
import android.widget.RadioButton
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.assertion.ViewAssertions.matches
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
    @Test fun captionAndTintMetrics() = verify("en", metricsOnly = true)

    private fun verify(language: String, metricsOnly: Boolean = false) {
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
            fun fieldTop(): Int {
                var top = 0
                scenario.onActivity {
                    val location = IntArray(2)
                    it.findViewById<EditText>(R.id.breathing).getLocationOnScreen(location)
                    top = location[1]
                }
                return top
            }
            onView(withId(R.id.pulsa)).perform(replaceText("125"), closeSoftKeyboard())
            compose.waitForIdle()
            val initialTop = fieldTop()
            onView(withId(R.id.baby)).perform(click())
            compose.waitForIdle()
            val reservedTop = fieldTop()
            scenario.onActivity { activity ->
                val caption = activity.findViewById<android.widget.TextView>(com.google.android.material.R.id.textinput_error)
                val font = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
                assertEquals("Final caption uses native font after appearance", font, caption.typeface)
                val reference = android.widget.TextView(activity).apply {
                    typeface = font
                    setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, caption.textSize)
                    if (android.os.Build.VERSION.SDK_INT >= 28) setFallbackLineSpacing(false)
                    if (android.os.Build.VERSION.SDK_INT >= 35) setLocalePreferredLineHeightForMinimumUsed(false)
                }
                reference.measure(android.view.View.MeasureSpec.makeMeasureSpec(caption.width, android.view.View.MeasureSpec.EXACTLY),
                    android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED))
                assertEquals("Native natural caption metrics: ${caption.paint.fontMetricsInt}", reference.measuredHeight, caption.height)
                assertEquals("Reservation is caption only, without Material's new inset", caption.height, reservedTop - initialTop)
                if (activity.resources.configuration.fontScale == 1f && activity.resources.displayMetrics.densityDpi == 420)
                    assertEquals("Retained native API24/API37 normal-scale caption reference", 43, reservedTop - initialTop)
            }
            if (metricsOnly) capture("caption-baby")
            onView(withId(R.id.old)).perform(click())
            compose.waitForIdle()
            assertEquals("Invalid pulse retains the reservation", reservedTop, fieldTop())
            if (metricsOnly) capture("caption-return-adult")
            onView(withId(R.id.pulsa)).perform(clearText(), closeSoftKeyboard())
            compose.waitForIdle()
            assertEquals("Empty pulse retains the reservation", reservedTop, fieldTop())
            onView(withId(R.id.pulsa)).perform(replaceText("60"), closeSoftKeyboard())
            onView(withId(R.id.breathing)).perform(replaceText("21"), closeSoftKeyboard())
            onView(withId(R.id.systolic)).perform(replaceText("120"), closeSoftKeyboard())
            onView(withId(R.id.diastolic)).perform(replaceText("90"), closeSoftKeyboard())
            compose.waitForIdle()
            scenario.onActivity {
                assertEquals(Color.rgb(70,178,58), it.findViewById<EditText>(R.id.pulsa).currentTextColor)
                assertEquals(Color.rgb(191,21,22), it.findViewById<EditText>(R.id.breathing).currentTextColor)
                val breathing = it.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.breathing)
                assertEquals("Calculated underline survives focus changes", Color.rgb(191,21,22),
                    androidx.core.view.ViewCompat.getBackgroundTintList(breathing)?.getColorForState(breathing.drawableState, 0))
                val bitmap = android.graphics.Bitmap.createBitmap(breathing.width, breathing.height, android.graphics.Bitmap.Config.ARGB_8888)
                breathing.background.draw(android.graphics.Canvas(bitmap))
                val pixels = IntArray(bitmap.width * bitmap.height)
                bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
                bitmap.recycle()
                assertTrue("Rendered breathing underline is red, not stale theme aqua", pixels.any { pixel ->
                    Color.alpha(pixel) > 0 && (pixel and 0x00ffffff) == (Color.rgb(191,21,22) and 0x00ffffff)
                })
                assertEquals(Color.rgb(70,178,58), it.findViewById<EditText>(R.id.diastolic).currentTextColor)
            }
            if (metricsOnly) {
                capture("calculated-tint")
                return@use
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
                assertEquals(it.getString(R.string.button_e), it.title.toString())
                assertEquals("125", it.findViewById<EditText>(R.id.pulsa).text.toString())
                assertEquals("91", it.findViewById<EditText>(R.id.diastolic).text.toString())
            }
            scenario.moveToState(androidx.lifecycle.Lifecycle.State.CREATED)
            scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED)
            compose.waitForIdle()
            scenario.onActivity {
                assertEquals("125", it.findViewById<EditText>(R.id.pulsa).text.toString())
                assertEquals(it.getString(R.string.button_e), it.title.toString())
            }
            onView(withId(R.id.pulsa)).perform(clearText(), closeSoftKeyboard())
            compose.waitForIdle()
            scenario.onActivity { assertEquals(Color.rgb(50,167,195), it.findViewById<EditText>(R.id.pulsa).currentTextColor) }
            for (invalid in listOf("1.5", "-1", "1000")) {
                onView(withId(R.id.pulsa)).perform(replaceText(invalid), closeSoftKeyboard())
                scenario.onActivity { assertEquals("", it.findViewById<EditText>(R.id.pulsa).text.toString()) }
            }
            if (instrumentation.targetContext.resources.configuration.fontScale > 1.2f) {
                onView(withId(R.id.vitalsResult)).perform(swipeUp())
                onView(withText(R.string.vitalsAlert)).check(matches(isCompletelyDisplayed()))
                capture("candidate-$language-bottom")
            }
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
