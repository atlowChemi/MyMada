package atlow.chemi.mymada.settings

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.R
import atlow.chemi.mymada.SettingsPage
import atlow.chemi.mymada.fragments.SettingsAdvanced
import atlow.chemi.mymada.fragments.SettingsAmpm
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class SettingsComposeTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun volumeDraftAndNightSelectionSurviveRecreation() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferences = context.getSharedPreferences("Settings", 0)
        val keys = setOf("volumeApp", "volumeAppNum", "sleepNight", "sleepDay", "sleep")
        val original = preferences.all.filterKeys { it in keys }
        try {
            preferences.edit().putBoolean("volumeApp", false).putInt("sleepNight", 0).putInt("sleepDay", 0).commit()
            ActivityScenario.launch(SettingsPage::class.java).use { scenario ->
                scenario.onActivity { it.displayFragment(SettingsAdvanced(), R.id.advanced) }
                click("volume")
                click("volumeApp")
                assertFalse(preferences.getBoolean("volumeApp", true))
                scenario.recreate()
                compose.onNodeWithTag("volumeApp").assertIsOn()
                click("dialog-cancel")
                assertFalse(preferences.getBoolean("volumeApp", true))
                click("volume")
                click("volumeApp")
                click("dialog-confirm")
                assertTrue(preferences.getBoolean("volumeApp", false))
                scenario.onActivity { it.displayFragment(SettingsAmpm(), R.id.ampm) }
                click("sleepNight")
                scenario.recreate()
                click("mode-1")
                assertEquals(1, preferences.getInt("sleepNight", -1))
                assertEquals(1, preferences.getInt("sleep", -1))
            }
        } finally {
            preferences.edit().apply {
                keys.forEach { remove(it) }
                original.forEach { (key, value) ->
                    when (value) {
                        is Boolean -> putBoolean(key, value)
                        is Int -> putInt(key, value)
                    }
                }
            }.commit()
        }
    }

    private fun click(tag: String) {
        compose.onNodeWithTag(tag).performScrollTo().performClick()
        compose.waitForIdle()
    }
}
