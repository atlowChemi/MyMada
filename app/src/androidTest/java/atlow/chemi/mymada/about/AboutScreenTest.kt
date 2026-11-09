package atlow.chemi.mymada.about

import android.app.Activity
import android.view.KeyEvent
import android.view.View
import android.graphics.Color
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import atlow.chemi.mymada.BuildConfig
import atlow.chemi.mymada.HomePage
import atlow.chemi.mymada.InfoActivityKt
import atlow.chemi.mymada.PrivacyActivity
import atlow.chemi.mymada.R
import atlow.chemi.mymada.SettingsPage
import atlow.chemi.mymada.home.homeDestination
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
            val toolbarBounds = compose.onNodeWithTag("information-toolbar").fetchSemanticsNode().boundsInRoot
            scenario.onActivity {
                assertEquals(56 * it.resources.displayMetrics.density, toolbarBounds.height, 1f)
            }
            compose.onNodeWithContentDescription(detection).assertIsOn().performClick().assertIsOff()
            declineMissingDetectionAccess(scenario)
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
            val tile = compose.onNodeWithContentDescription(privacy).performScrollTo().assertIsDisplayed().assertHasClickAction()
            val tileBounds = tile.fetchSemanticsNode().boundsInRoot
            scenario.onActivity {
                val density = it.resources.displayMetrics.density
                assertEquals(85 * density, tileBounds.height, 1f)
                assertEquals(minOf(340 * density, toolbarBounds.width - 32 * density), tileBounds.width, 1f)
            }
            verifyArtwork(scenario, tileBounds)
            scenario.recreate()
            // rememberScrollState must retain the reachable privacy action after recreation.
            compose.onNodeWithContentDescription(privacy).assertIsDisplayed()
            compose.onNodeWithContentDescription(detection).assertIsOff()
            compose.onNodeWithText(body).assertExists()
            lateinit var owner: InfoActivityKt
            scenario.onActivity { owner = it }

            val privacyMonitor = instrumentation.addMonitor(PrivacyActivity::class.java.name, null, false)
            try {
                compose.onNodeWithContentDescription(privacy).performClick()
                val activity = instrumentation.waitForMonitorWithTimeout(privacyMonitor, 5_000)
                assertNotNull(activity)
                compose.onNodeWithText(privacy).assertIsDisplayed()
                compose.onNodeWithContentDescription(back).performClick()
                awaitStage(owner, Stage.RESUMED)
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
                awaitStage(owner, Stage.RESUMED)
                compose.onNodeWithContentDescription(detection).assertIsOn().performClick().assertIsOff()
                declineMissingDetectionAccess(scenario)
            } finally { instrumentation.removeMonitor(settingsMonitor) }
            compose.onNodeWithContentDescription(back).performClick()
            awaitStage(owner, Stage.DESTROYED)
        }
        ActivityScenario.launch(InfoActivityKt::class.java).use { scenario ->
            lateinit var owner: InfoActivityKt
            scenario.onActivity { owner = it }
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            awaitStage(owner, Stage.DESTROYED)
        }
        verifyHomeEntry(title)
    }

    private fun declineMissingDetectionAccess(scenario: ActivityScenario<InfoActivityKt>) {
        val context = instrumentation.targetContext
        // A stale enabled preference without OS access opens a permission prompt.
        // Decline it before sampling artwork or interacting with the underlying screen.
        if (!atlow.chemi.mymada.permissions.NotificationPermission.granted(context) ||
            atlow.chemi.mymada.calls.ListenerAccess.selected(context) == null) {
            onView(withText(R.string.dialog_no)).inRoot(isDialog()).perform(click())
            compose.waitUntil(5_000) {
                var focused = false
                scenario.onActivity { focused = it.hasWindowFocus() }
                focused
            }
        }
    }

    private fun verifyHomeEntry(title: String) {
        // Avoid the unrelated first-run overlay prompt on this disposable test installation.
        instrumentation.uiAutomation.executeShellCommand(
            "appops set ${instrumentation.targetContext.packageName} SYSTEM_ALERT_WINDOW allow"
        ).use { descriptor -> java.io.FileInputStream(descriptor.fileDescriptor).use { it.readBytes() } }
        val monitor = instrumentation.addMonitor(InfoActivityKt::class.java.name, null, false)
        try {
            ActivityScenario.launch(HomePage::class.java).use { home ->
                lateinit var owner: HomePage
                home.onActivity { owner = it }
                compose.homeDestination(title).performClick()
                assertNotNull(instrumentation.waitForMonitorWithTimeout(monitor, 5_000))
                compose.onNodeWithText(title).assertIsDisplayed()
                instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
                awaitStage(owner, Stage.RESUMED)
            }
        } finally { instrumentation.removeMonitor(monitor) }
    }

    private fun awaitStage(activity: Activity, expected: Stage) {
        compose.waitUntil(10_000) {
            var reached = false
            // The monitor supports transitional stages and must be read on main.
            instrumentation.runOnMainSync {
                reached = ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(activity) == expected
            }
            reached
        }
    }

    private fun verifyArtwork(scenario: ActivityScenario<InfoActivityKt>, tile: Rect) {
        val origin = IntArray(2)
        scenario.onActivity { it.findViewById<View>(android.R.id.content).getLocationOnScreen(origin) }
        // Semantics can settle before SurfaceFlinger presents a navigation/scroll frame.
        compose.waitUntil(5_000) {
            val bitmap = instrumentation.uiAutomation.takeScreenshot()
            try {
                // The original red mask must render, not just a correctly sized generic Material button.
                var redPixels = 0
                for (y in (tile.top.toInt() + origin[1]) until (tile.bottom.toInt() + origin[1])) {
                    for (x in tile.left.toInt() until tile.right.toInt()) {
                        val color = bitmap.getPixel(x, y)
                        if (Color.red(color) > 100 && Color.red(color) > 2 * Color.green(color)
                            && Color.red(color) > 2 * Color.blue(color)) redPixels++
                    }
                }
                // Sample the empty left margin: the published window is gray and radially shaded.
                val upper = bitmap.getPixel(2, bitmap.height / 4)
                val middle = bitmap.getPixel(2, bitmap.height / 2)
                redPixels > tile.width * tile.height * 0.01 && Color.red(upper) < 240 &&
                    kotlin.math.abs(Color.red(upper) - Color.blue(upper)) < 8 &&
                    kotlin.math.abs(Color.red(upper) - Color.red(middle)) > 5
            } finally { bitmap.recycle() }
        }
    }
}
