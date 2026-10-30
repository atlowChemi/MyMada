package atlow.chemi.mymada.tools.pulse

import android.graphics.Bitmap
import android.os.SystemClock
import android.widget.TextView
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.R
import atlow.chemi.mymada.UtillitiesAndTools
import atlow.chemi.mymada.fragments.PulseCalc
import atlow.chemi.mymada.helpers.LocalHelper
import com.google.android.material.textfield.TextInputLayout
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

class PulseScreenTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    @Test fun calculationTimerRecreationAndNavigationInBothLanguages() {
        for (language in listOf("en", "he")) {
            instrumentation.runOnMainSync { LocalHelper.setLocale(context, language) }
            context.getSharedPreferences("Settings", 0).edit().putBoolean("Mapp", false)
                .putStringSet("Tools", setOf("pulse")).commit()
            ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
                scenario.onActivity {
                    assertTrue(it.supportFragmentManager.findFragmentById(R.id.frame) is PulseCalc)
                }
                onView(withId(R.id.fullPulseA)).check(matches(withText("")))
                onView(withId(R.id.fifteenPulseA)).check(matches(org.hamcrest.Matchers.not(hasFocus())))
                capture("default-$language")
                onView(withId(R.id.fifteenPulseA)).perform(replaceText("25"), closeSoftKeyboard())
                onView(withId(R.id.fullPulseA)).check(matches(withText("100")))
                capture("result-$language")
                onView(withId(R.id.fifteenPulseA)).perform(clearText(), closeSoftKeyboard())
                onView(withId(R.id.fullPulseA)).check(matches(withText("100")))
                onView(withId(R.id.shortPulseATIL)).check { view, error ->
                    if (error != null) throw error
                    assertEquals(view.context.getString(R.string.err_pulse), (view as TextInputLayout).error.toString())
                }
                capture("error-$language")
                scenario.recreate()
                onView(withId(R.id.fullPulseA)).check(matches(withText("100")))
                onView(withId(R.id.fifteenPulseA)).perform(replaceText("99"), closeSoftKeyboard())
                onView(withId(R.id.fullPulseA)).check(matches(withText("396")))
                val audio = context.getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager
                val alarmVolume = audio.getStreamVolume(android.media.AudioManager.STREAM_ALARM)
                compose.onNodeWithTag("pulse-timer").performClick()
                val start = SystemClock.elapsedRealtime()
                compose.waitUntil(5_000) { seconds(scenario) in 11..13 }
                scenario.recreate()
                assertTrue(seconds(scenario) in 1..13)
                onView(withId(R.id.fullPulseA)).check(matches(withText("396")))
                scenario.moveToState(androidx.lifecycle.Lifecycle.State.CREATED)
                SystemClock.sleep(1500)
                scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED)
                compose.waitUntil(17_000) { seconds(scenario) == 0 }
                assertTrue("Timer must not restart on recreation", SystemClock.elapsedRealtime() - start < 17_000)
                assertEquals(alarmVolume, audio.getStreamVolume(android.media.AudioManager.STREAM_ALARM))
                compose.onNodeWithTag("pulse-timer").performClick()
                compose.waitUntil(3_000) { seconds(scenario) in 13..15 }
                compose.onNodeWithTag("pulse-timer").performClick()
                onView(withId(R.id.txtTimerSecond)).check(matches(withText("15")))
                scenario.onActivity { it.findViewById<androidx.drawerlayout.widget.DrawerLayout>(R.id.activity_Utils)
                    .openDrawer(androidx.core.view.GravityCompat.START, false) }
                pressBack()
                compose.waitUntil(5_000) {
                    var closed = false
                    scenario.onActivity { closed = !it.findViewById<androidx.drawerlayout.widget.DrawerLayout>(R.id.activity_Utils)
                        .isDrawerVisible(androidx.core.view.GravityCompat.START) }
                    closed
                }
                onView(withId(R.id.fullPulseA)).check(matches(withText("396")))
                pressBackUnconditionally()
                compose.waitUntil(5_000) { scenario.state == androidx.lifecycle.Lifecycle.State.DESTROYED }
            }
        }
    }

    private fun seconds(scenario: ActivityScenario<UtillitiesAndTools>): Int {
        var value = -1
        scenario.onActivity { value = it.findViewById<TextView>(R.id.txtTimerSecond).text.toString().toInt() }
        return value
    }

    private fun capture(name: String) {
        if (InstrumentationRegistry.getArguments().getString("capture") != "true") return
        instrumentation.uiAutomation.waitForIdle(500, 5_000)
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        File(context.filesDir, "pulse-$name.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
    }
}
