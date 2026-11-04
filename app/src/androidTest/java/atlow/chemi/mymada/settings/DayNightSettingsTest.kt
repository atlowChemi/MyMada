package atlow.chemi.mymada.settings

import android.graphics.Bitmap
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.R
import atlow.chemi.mymada.SettingsPage
import atlow.chemi.mymada.fragments.SettingsAmpm
import atlow.chemi.mymada.helpers.LocalHelper
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import org.junit.Rule
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class DayNightSettingsTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    @Test fun allNineModeCombinationsPreserveReceiverEncodingInEitherEditOrder() {
        val preferences = context.getSharedPreferences("day-night-test", 0)
        for (day in 0..2) for (night in 0..2) {
            preferences.edit().clear().commit()
            DayNightSettings.setMode(preferences, false, day)
            DayNightSettings.setMode(preferences, true, night)
            assertEquals(day * 3 + night, preferences.getInt("sleep", -1))
            preferences.edit().clear().commit()
            DayNightSettings.setMode(preferences, true, night)
            DayNightSettings.setMode(preferences, false, day)
            assertEquals(day * 3 + night, preferences.getInt("sleep", -1))
            assertEquals(day, preferences.getInt("sleepDay", -1))
            assertEquals(night, preferences.getInt("sleepNight", -1))
        }
    }

    @Test fun bilingualModeAndHourDialogsRestoreTheirDestinationAndSelection() {
        val preferences = context.getSharedPreferences("Settings", 0)
        instrumentation.uiAutomation.executeShellCommand("appops set ${context.packageName} SYSTEM_ALERT_WINDOW allow").use {
            android.os.ParcelFileDescriptor.AutoCloseInputStream(it).readBytes()
        }
        val rounds = InstrumentationRegistry.getArguments().getString("rounds")?.toInt() ?: 1
        for (round in 1..rounds) for (language in listOf("en", "he")) {
            android.util.Log.i("DayNightDiagnosis", "round=$round language=$language")
            instrumentation.runOnMainSync { LocalHelper.setLocale(context, language) }
            preferences.edit().putInt("sleepDay", 0).putInt("sleepNight", 0).putInt("sleep", 0)
                .putInt("nightStart", 23).putInt("nightEnd", 7).putBoolean("Mapp", false).commit()
            ActivityScenario.launch(SettingsPage::class.java).use { scenario ->
                var choices = emptyArray<String>()
                scenario.onActivity {
                    choices = it.resources.getStringArray(R.array.ampm)
                    it.displayFragment(SettingsAmpm(), R.id.ampm)
                }
                compose.onNodeWithTag("sleepDay").assertIsDisplayed()
                capture("settings-day-night-$language")
                compose.onNodeWithTag("sleepDay").performScrollTo().performClick()
                compose.onNodeWithTag("mode-0").assertIsDisplayed()
                capture("settings-day-mode-$language")
                onView(withId(androidx.appcompat.R.id.alertTitle)).inRoot(isDialog()).check(matches(isDisplayed()))
                scenario.onActivity {
                    val parent = it.supportFragmentManager.findFragmentById(R.id.frame) as SettingsAmpm
                    val dialog = parent.childFragmentManager.fragments.filterIsInstance<SettingsAmpm.ModeDialog>().single().requireDialog()
                    val title = dialog.findViewById<android.widget.TextView>(androidx.appcompat.R.id.alertTitle)
                    assertEquals(it.getString(R.string.notifDay), title.text.toString())
                    android.util.Log.i("ModeTitleGeometry", "$language size=${title.textSize} height=${title.height} lines=${title.lineCount} scale=${it.resources.configuration.fontScale}")
                    if (it.resources.displayMetrics.density == 2.625f && it.resources.configuration.fontScale == 1f) {
                        val expected = if (language == "en") 53 else if (android.os.Build.VERSION.SDK_INT >= 28) 65 else 59
                        assertEquals("native12822 title: $language", expected, title.height)
                    }
                }
                compose.onNodeWithTag("dialog-cancel").performScrollTo().assertIsDisplayed()
                scenario.recreate()
                compose.onNodeWithTag("mode-1").performScrollTo().performClick()
                compose.onNodeWithTag("sleepDay").assertTextContains(choices[1])
                assertEquals(3, preferences.getInt("sleep", -1))
                // API37's disappearing system Dim Layer can drop this injected tap as
                // "untrusted". Run instrumentation with --no-window-animation; keep
                // click→recreate adjacent so this still checks dialog restoration.
                compose.onNodeWithTag("sleepNight").performScrollTo().performClick()
                scenario.recreate()
                compose.onNodeWithTag("mode-2").performScrollTo().performClick()
                compose.onNodeWithTag("sleepNight").assertTextContains(choices[2])
                assertEquals(5, preferences.getInt("sleep", -1))
                for (key in listOf("nightStart", "nightEnd")) {
                    compose.onNodeWithTag(key).performScrollTo().performClick()
                    onView(withId(com.wdullaer.materialdatetimepicker.R.id.mdtp_ok)).inRoot(isDialog()).check(matches(isDisplayed()))
                    if (key == "nightStart") capture("settings-time-picker-$language")
                    scenario.onActivity {
                        val parent = it.supportFragmentManager.findFragmentById(R.id.frame) as SettingsAmpm
                        val picker = parent.childFragmentManager.findFragmentByTag(key) as TimePickerDialog
                        picker.requireView().findViewById<com.wdullaer.materialdatetimepicker.time.RadialPickerLayout>(
                            com.wdullaer.materialdatetimepicker.R.id.mdtp_time_picker)
                            .setTime(com.wdullaer.materialdatetimepicker.time.Timepoint(19, 0, 0))
                    }
                    scenario.recreate()
                    scenario.onActivity {
                        val parent = it.supportFragmentManager.findFragmentById(R.id.frame) as SettingsAmpm
                        val picker = parent.childFragmentManager.findFragmentByTag(key) as TimePickerDialog
                        assertNotNull(picker.onTimeSetListener)
                    }
                    // Use the actual picker confirmation callback after recreation, not the parent method.
                    onView(withId(com.wdullaer.materialdatetimepicker.R.id.mdtp_ok)).inRoot(isDialog()).perform(click())
                    assertEquals(19, preferences.getInt(key, -1))
                }
                scenario.recreate()
                compose.onNodeWithTag("sleepNight").assertTextContains(choices[2])
            }
        }
    }

    private fun capture(name: String) {
        if (InstrumentationRegistry.getArguments().getString("capture") != "true") return
        instrumentation.uiAutomation.waitForIdle(500, 5_000)
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        File(context.filesDir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
}
