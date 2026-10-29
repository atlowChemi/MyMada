package atlow.chemi.mymada.about

import android.view.KeyEvent
import android.view.View
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.BuildConfig
import atlow.chemi.mymada.HomePage
import atlow.chemi.mymada.InfoActivityKt
import atlow.chemi.mymada.PrivacyActivity
import atlow.chemi.mymada.R
import atlow.chemi.mymada.SettingsPage
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Disposable rootable emulator only, with IPv4/IPv6 egress blocked before app startup. */
class AboutScreenTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val preferences = instrumentation.targetContext.getSharedPreferences("Settings", 0)

    @Test fun englishAboutNavigationAndState() = verifyScreen("en", "Info")
    @Test fun hebrewAboutNavigationAndState() = verifyScreen("he", "אודות")

    private fun verifyScreen(language: String, title: String) {
        preferences.edit().putString("lng", language).putBoolean("privacy", true)
            .putBoolean("oldU", true).putBoolean("Mapp", true).commit()
        ActivityScenario.launch(InfoActivityKt::class.java).use { scenario ->
            var back = ""
            var settings = ""
            var detection = ""
            var privacy = ""
            var body = ""
            scenario.onActivity {
                assertEquals("atlow.chemi.mymada.InfoActivityKt", it.componentName.className)
                assertEquals(if (language == "en") View.LAYOUT_DIRECTION_LTR else View.LAYOUT_DIRECTION_RTL,
                    it.resources.configuration.layoutDirection)
                back = it.getString(androidx.appcompat.R.string.abc_action_bar_up_description)
                settings = it.getString(R.string.action_settings)
                detection = it.getString(R.string.pref_title_enable)
                privacy = it.getString(R.string.title_activity_privacy)
                body = it.getString(R.string.TversionNumber, BuildConfig.VERSION_NAME)
            }
            compose.onNodeWithText(title).assertIsDisplayed()
            compose.onNodeWithText(body).assertIsDisplayed()
            compose.onNodeWithContentDescription(detection).assertIsOn().performClick().assertIsOff()
            assertFalse(preferences.getBoolean("Mapp", true))
            val backBounds = compose.onNodeWithContentDescription(back).assertHasClickAction()
                .fetchSemanticsNode().touchBoundsInRoot
            val settingsBounds = compose.onNodeWithContentDescription(settings).assertHasClickAction()
                .fetchSemanticsNode().touchBoundsInRoot
            assertEquals(language == "en", backBounds.center.x < settingsBounds.center.x)
            scenario.onActivity {
                val minimum = 48 * it.resources.displayMetrics.density
                assertTrue("Back touch target: $backBounds, minimum $minimum",
                    backBounds.width >= minimum && backBounds.height >= minimum)
                assertTrue("Settings touch target: $settingsBounds, minimum $minimum",
                    settingsBounds.width >= minimum && settingsBounds.height >= minimum)
            }
            compose.onNodeWithText(privacy).performScrollTo().assertIsDisplayed().assertHasClickAction()
            scenario.recreate()
            // rememberScrollState must retain the reachable privacy action after recreation.
            compose.onNodeWithText(privacy).assertIsDisplayed()
            compose.onNodeWithContentDescription(detection).assertIsOff()
            compose.onNodeWithText(body).assertExists()

            val privacyMonitor = instrumentation.addMonitor(PrivacyActivity::class.java.name, null, false)
            try {
                compose.onNodeWithText(privacy).performClick()
                val activity = instrumentation.waitForMonitorWithTimeout(privacyMonitor, 5_000)
                assertNotNull(activity)
                compose.onNodeWithText(privacy).assertIsDisplayed()
                compose.onNodeWithContentDescription(back).performClick()
                compose.waitUntil(10_000) { scenario.state == Lifecycle.State.RESUMED }
                compose.onNodeWithText(title).assertIsDisplayed()
            } finally { instrumentation.removeMonitor(privacyMonitor) }

            val settingsMonitor = instrumentation.addMonitor(SettingsPage::class.java.name, null, false)
            try {
                compose.onNodeWithContentDescription(settings).performClick()
                val activity = instrumentation.waitForMonitorWithTimeout(settingsMonitor, 5_000)
                assertNotNull(activity)
                // Settings permission dialogs are outside this slice; simulate a changed detection preference.
                preferences.edit().putBoolean("Mapp", true).commit()
                instrumentation.runOnMainSync { activity!!.finish() }
                compose.waitUntil(10_000) { scenario.state == Lifecycle.State.RESUMED }
                compose.onNodeWithContentDescription(detection).assertIsOn().performClick().assertIsOff()
            } finally { instrumentation.removeMonitor(settingsMonitor) }
            compose.onNodeWithContentDescription(back).performClick()
            compose.waitUntil(10_000) { scenario.state == Lifecycle.State.DESTROYED }
        }
        ActivityScenario.launch(InfoActivityKt::class.java).use { scenario ->
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            compose.waitUntil(10_000) { scenario.state == Lifecycle.State.DESTROYED }
        }
        verifyHomeEntry(title)
    }

    private fun verifyHomeEntry(title: String) {
        // Avoid the unrelated first-run overlay prompt on this disposable test installation.
        instrumentation.uiAutomation.executeShellCommand(
            "appops set ${instrumentation.targetContext.packageName} SYSTEM_ALERT_WINDOW allow"
        ).use { descriptor -> java.io.FileInputStream(descriptor.fileDescriptor).use { it.readBytes() } }
        val monitor = instrumentation.addMonitor(InfoActivityKt::class.java.name, null, false)
        try {
            ActivityScenario.launch(HomePage::class.java).use { home ->
                home.onActivity { it.findViewById<RecyclerView>(R.id.rv).scrollToPosition(4) }
                instrumentation.waitForIdleSync()
                home.onActivity {
                    val holder = it.findViewById<RecyclerView>(R.id.rv).findViewHolderForAdapterPosition(4)
                    assertNotNull(holder)
                    holder!!.itemView.findViewById<View>(R.id.buttonChange).performClick()
                }
                assertNotNull(instrumentation.waitForMonitorWithTimeout(monitor, 5_000))
                compose.onNodeWithText(title).assertIsDisplayed()
                instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
                compose.waitUntil(10_000) { home.state == Lifecycle.State.RESUMED }
            }
        } finally { instrumentation.removeMonitor(monitor) }
    }
}
