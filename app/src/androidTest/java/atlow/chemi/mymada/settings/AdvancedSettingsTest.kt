package atlow.chemi.mymada.settings

import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.R
import atlow.chemi.mymada.SettingsPage
import atlow.chemi.mymada.fragments.SettingsAdvanced
import atlow.chemi.mymada.helpers.LocalHelper
import com.google.android.material.navigation.NavigationView
import org.junit.Assert.*
import org.junit.Test
import android.graphics.Bitmap
import java.io.File

class AdvancedSettingsTest {
    @Test fun bilingualTogglesAndDialogDraftsSurviveRecreationWithoutSavingOnCancel() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val preferences = context.getSharedPreferences("Settings", 0)
        for (language in listOf("en", "he")) {
            instrumentation.runOnMainSync { LocalHelper.setLocale(context, language) }
            preferences.edit().putBoolean("Mapp", false).putBoolean("not", true).putBoolean("win", true)
                .putBoolean("ride", false).putBoolean("loop", false).putBoolean("killS", false)
                .putString("timeK", "6").putBoolean("volumeApp", false).putInt("volumeAppNum", 2).commit()
            ActivityScenario.launch(SettingsPage::class.java).use { scenario ->
                scenario.onActivity {
                    it.onNavigationItemSelected(it.findViewById<NavigationView>(R.id.navigation).menu.findItem(R.id.advanced))
                }
                capture("advanced-$language")
                for ((row, key) in listOf(R.id.not_pref to "not", R.id.newWin_pref to "win",
                    R.id.ride_pref to "ride", R.id.loop_pref to "loop", R.id.shabat_pref to "killS")) {
                    val old = preferences.getBoolean(key, false)
                    onView(withId(row)).perform(scrollTo(), click())
                    assertEquals(!old, preferences.getBoolean(key, old))
                }
                scenario.recreate()
                onView(withId(R.id.shabatTime_pref)).perform(scrollTo(), click())
                capture("advanced-delay-$language")
                onView(withId(R.id.shabatT)).inRoot(isDialog()).perform(replaceText("999999999999"), closeSoftKeyboard())
                onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click())
                assertEquals("6", preferences.getString("timeK", null))
                onView(withId(R.id.shabatT)).inRoot(isDialog()).perform(replaceText("12"), closeSoftKeyboard())
                scenario.recreate()
                onView(withId(R.id.shabatT)).inRoot(isDialog()).check(matches(withText("12")))
                onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click())
                assertEquals("12", preferences.getString("timeK", null))
                for (confirm in listOf(false, true)) {
                    onView(withId(R.id.volume_pref)).perform(scrollTo(), click())
                    if (!confirm) capture("advanced-volume-$language")
                    assertFalse(preferences.getBoolean("volumeApp", true))
                    onView(withId(R.id.volume_app_pref)).inRoot(isDialog()).perform(click())
                    scenario.onActivity {
                        val parent = it.supportFragmentManager.findFragmentById(R.id.frame) as SettingsAdvanced
                        val dialog = (parent.childFragmentManager.findFragmentByTag("volume") as SettingsAdvanced.VolumeDialog)
                            .requireDialog() as AlertDialog
                        dialog.findViewById<SeekBar>(R.id.VolumeSeekBar)!!.progress = 1
                    }
                    scenario.recreate()
                    onView(withId(R.id.volume_app)).inRoot(isDialog()).check(matches(isChecked()))
                    onView(withId(if (confirm) android.R.id.button1 else android.R.id.button2)).inRoot(isDialog()).perform(click())
                    assertEquals(confirm, preferences.getBoolean("volumeApp", false))
                    assertEquals(if (confirm) 1 else 2, preferences.getInt("volumeAppNum", -1))
                }
                scenario.recreate()
                onView(withId(R.id.ampm_card)).perform(scrollTo(), click())
                onView(withId(R.id.sleepDay_pref)).check(matches(isDisplayed()))
            }
        }
    }

    private fun capture(name: String) {
        if (InstrumentationRegistry.getArguments().getString("capture") != "true") return
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.uiAutomation.waitForIdle(500, 5_000)
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        File(instrumentation.targetContext.filesDir, "$name.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
    }
}
