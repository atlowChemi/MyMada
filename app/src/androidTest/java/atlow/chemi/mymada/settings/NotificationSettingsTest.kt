package atlow.chemi.mymada.settings

import android.graphics.Bitmap
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.R
import atlow.chemi.mymada.SettingsPage
import atlow.chemi.mymada.fragments.SettingsNotificationsSounds
import atlow.chemi.mymada.helpers.LocalHelper
import com.google.android.material.navigation.NavigationView
import org.hamcrest.Matchers.not
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class NotificationSettingsTest {
    @Test fun bilingualDefaultsToggleRecreationAndSoundRoute() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val preferences = context.getSharedPreferences("Settings", 0)
        val previousLocales = androidx.core.app.LocaleManagerCompat.getApplicationLocales(context).toLanguageTags()
        instrumentation.uiAutomation.executeShellCommand("appops set ${context.packageName} SYSTEM_ALERT_WINDOW allow").use {
            android.os.ParcelFileDescriptor.AutoCloseInputStream(it).readBytes()
        }
        val keys = listOf("cut", "stat", "statVib", "callT6", "callT4")
        val rows = listOf(R.id.cutA_pref, R.id.stat_pref, R.id.statVib_pref, R.id.T6_pref, R.id.findT4_pref)
        val switches = listOf(R.id.cutAlarm, R.id.stat, R.id.statVib, R.id.T6, R.id.findT4)
        val descriptions = listOf(R.id.cutA_Desc, R.id.stat_Desc, R.id.statVib_Desc, R.id.T6_Desc, R.id.findT4_Desc)
        val toggledText = listOf(R.string.pref_description_cutA_on, R.string.pref_description_state_on,
            R.string.pref_description_stateVib_on, R.string.findT6_off, R.string.findT4_off)
        try {
            for (language in listOf("en", "he")) {
                instrumentation.runOnMainSync { LocalHelper.setLocale(context, language) }
                preferences.edit().also { editor -> keys.forEach { editor.remove(it) } }
                    .putBoolean("Mapp", false).commit()
                ActivityScenario.launch(SettingsPage::class.java).use { scenario ->
                    onView(withId(R.id.cutA_pref)).check(matches(isDisplayed()))
                    if (InstrumentationRegistry.getArguments().getString("capture") == "true") {
                        instrumentation.uiAutomation.waitForIdle(500, 5_000)
                        val bitmap = instrumentation.uiAutomation.takeScreenshot()
                        File(context.filesDir, "settings-notifications-$language.png").outputStream().use {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                        }
                        bitmap.recycle()
                    }
                    for (index in keys.indices) {
                        onView(withId(switches[index])).check(matches(if (index >= 3) isChecked() else not(isChecked())))
                        onView(withId(rows[index])).perform(scrollTo(), click())
                        onView(withId(switches[index])).check(matches(if (index < 3) isChecked() else not(isChecked())))
                        onView(withId(descriptions[index])).check(matches(withText(toggledText[index])))
                        assertEquals(index < 3, preferences.getBoolean(keys[index], index >= 3))
                    }
                    scenario.recreate()
                    for (index in keys.indices) {
                        onView(withId(switches[index])).check(matches(if (index < 3) isChecked() else not(isChecked())))
                        onView(withId(descriptions[index])).check(matches(withText(toggledText[index])))
                    }
                    onView(withId(R.id.sound_vib_card)).perform(scrollTo(), click())
                    instrumentation.waitForIdleSync()
                    scenario.onActivity {
                        assertTrue(it.supportFragmentManager.findFragmentById(R.id.frame) is SettingsNotificationsSounds)
                        it.onNavigationItemSelected(it.findViewById<NavigationView>(R.id.navigation).menu.findItem(R.id.notification))
                    }
                    onView(withId(R.id.cutAlarm)).check(matches(isChecked()))
                }
            }
        } finally {
            instrumentation.runOnMainSync {
                if (android.os.Build.VERSION.SDK_INT >= 33) {
                    context.getSystemService(android.app.LocaleManager::class.java).applicationLocales =
                        android.os.LocaleList.forLanguageTags(previousLocales)
                }
            }
        }
    }
}
