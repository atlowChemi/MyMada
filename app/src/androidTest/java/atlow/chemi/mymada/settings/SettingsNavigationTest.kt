package atlow.chemi.mymada.settings

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.Espresso.pressBackUnconditionally
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.R
import atlow.chemi.mymada.SettingsPage
import atlow.chemi.mymada.helpers.LocalHelper
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class SettingsNavigationTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun eachDestinationRestoresAndDrawerBackDoesNotFinishTheScreen() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        context.getSharedPreferences("Settings", 0).edit().putBoolean("Mapp", false).commit()
        for (language in listOf("en", "he")) {
            instrumentation.runOnMainSync { LocalHelper.setLocale(context, language) }
            ActivityScenario.launch(SettingsPage::class.java).use { scenario ->
                for (destination in listOf(R.id.notification, R.id.soundAndVib, R.id.advanced, R.id.ampm, R.id.tools)) {
                    compose.onNodeWithTag("shell-navigation").performClick()
                    compose.onNodeWithTag("shell-destination-$destination").performScrollTo().performClick()
                    compose.waitForIdle()
                    scenario.recreate()
                    compose.waitForIdle()
                    scenario.onActivity { activity ->
                        assertEquals("settings:$destination", activity.supportFragmentManager.findFragmentById(R.id.frame)!!.tag)
                    }
                    compose.onNodeWithTag("shell-navigation").performClick()
                    compose.onNodeWithTag("shell-destination-$destination").performScrollTo().assertIsSelected()
                    pressBack()
                    compose.waitForIdle()
                    scenario.onActivity { assertFalse(it.isFinishing) }
                }
                pressBackUnconditionally()
                compose.waitUntil(5000) { scenario.state == Lifecycle.State.DESTROYED || scenario.state == Lifecycle.State.CREATED }
            }
        }
    }
}
