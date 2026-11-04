package atlow.chemi.mymada.settings

import android.graphics.Bitmap
import androidx.test.core.app.ActivityScenario
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.R
import atlow.chemi.mymada.SettingsPage
import atlow.chemi.mymada.fragments.SettingsNotificationsSounds
import atlow.chemi.mymada.fragments.SettingsNotifications
import atlow.chemi.mymada.helpers.LocalHelper
import org.junit.Assert.*
import org.junit.Test
import org.junit.Rule
import java.io.File

class NotificationSettingsTest {
    @get:Rule val compose = createEmptyComposeRule()
    @Test fun bilingualDefaultsToggleRecreationAndSoundRoute() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val preferences = context.getSharedPreferences("Settings", 0)
        val previousLocales = androidx.core.app.LocaleManagerCompat.getApplicationLocales(context).toLanguageTags()
        instrumentation.uiAutomation.executeShellCommand("appops set ${context.packageName} SYSTEM_ALERT_WINDOW allow").use {
            android.os.ParcelFileDescriptor.AutoCloseInputStream(it).readBytes()
        }
        val keys = listOf("cut", "stat", "statVib", "callT6", "callT4")
        val toggledText = listOf(R.string.pref_description_cutA_on, R.string.pref_description_state_on,
            R.string.pref_description_stateVib_on, R.string.findT6_off, R.string.findT4_off)
        try {
            for (language in listOf("en", "he")) {
                instrumentation.runOnMainSync { LocalHelper.setLocale(context, language) }
                preferences.edit().also { editor -> keys.forEach { editor.remove(it) } }
                    .putBoolean("Mapp", false).commit()
                ActivityScenario.launch(SettingsPage::class.java).use { scenario ->
                    scenario.onActivity { it.displayFragment(SettingsNotifications(), R.id.notification) }
                    compose.onNodeWithTag("cut").assertIsDisplayed()
                    if (InstrumentationRegistry.getArguments().getString("capture") == "true") {
                        instrumentation.uiAutomation.waitForIdle(500, 5_000)
                        val bitmap = instrumentation.uiAutomation.takeScreenshot()
                        File(context.filesDir, "settings-notifications-$language.png").outputStream().use {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                        }
                        bitmap.recycle()
                    }
                    for (index in keys.indices) {
                        val row = compose.onNodeWithTag(keys[index])
                        if (index >= 3) row.assertIsOn() else row.assertIsOff()
                        row.performScrollTo().performClick()
                        if (index < 3) row.assertIsOn() else row.assertIsOff()
                        var expected = ""
                        scenario.onActivity { expected = it.getString(toggledText[index]) }
                        row.assertTextContains(expected)
                        assertEquals(index < 3, preferences.getBoolean(keys[index], index >= 3))
                    }
                    scenario.recreate()
                    for (index in keys.indices) {
                        val row = compose.onNodeWithTag(keys[index])
                        if (index < 3) row.assertIsOn() else row.assertIsOff()
                    }
                    compose.onNodeWithTag("sound-settings").performScrollTo().performClick()
                    instrumentation.waitForIdleSync()
                    scenario.onActivity {
                        assertTrue(it.supportFragmentManager.findFragmentById(R.id.frame) is SettingsNotificationsSounds)
                        it.displayFragment(SettingsNotifications(), R.id.notification)
                    }
                    compose.onNodeWithTag("cut").assertIsOn()
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
