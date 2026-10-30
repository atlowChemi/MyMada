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
import com.google.android.material.navigation.NavigationView
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class DayNightSettingsTest {
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
        for (language in listOf("en", "he")) {
            instrumentation.runOnMainSync { LocalHelper.setLocale(context, language) }
            preferences.edit().putInt("sleepDay", 0).putInt("sleepNight", 0).putInt("sleep", 0)
                .putInt("nightStart", 23).putInt("nightEnd", 7).putBoolean("Mapp", false).commit()
            ActivityScenario.launch(SettingsPage::class.java).use { scenario ->
                var choices = emptyArray<String>()
                scenario.onActivity {
                    choices = it.resources.getStringArray(R.array.ampm)
                    it.onNavigationItemSelected(it.findViewById<NavigationView>(R.id.navigation).menu.findItem(R.id.ampm))
                }
                onView(withId(R.id.sleepDay_pref)).check(matches(isDisplayed()))
                capture("settings-day-night-$language")
                onView(withId(R.id.sleepDay_pref)).perform(scrollTo(), click())
                onView(withText(choices[0])).inRoot(isDialog()).check(matches(isDisplayed()))
                capture("settings-day-mode-$language")
                scenario.recreate()
                onView(withText(choices[1])).inRoot(isDialog()).perform(click())
                onView(withId(R.id.sleepDay_Desc)).check(matches(withText(choices[1])))
                assertEquals(3, preferences.getInt("sleep", -1))
                onView(withId(R.id.sleepNight_pref)).perform(scrollTo(), click())
                onView(withText(choices[2])).inRoot(isDialog()).check(matches(isDisplayed()))
                scenario.recreate()
                onView(withText(choices[2])).inRoot(isDialog()).perform(click())
                onView(withId(R.id.sleepNight_Desc)).check(matches(withText(choices[2])))
                assertEquals(5, preferences.getInt("sleep", -1))
                for ((id, key) in listOf(R.id.start_night_pref to "nightStart", R.id.end_night_pref to "nightEnd")) {
                    onView(withId(id)).perform(scrollTo(), click())
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
                onView(withId(R.id.sleepNight_Desc)).check(matches(withText(choices[2])))
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
