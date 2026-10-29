package atlow.chemi.mymada.home

import android.app.Activity
import android.view.KeyEvent
import android.view.View
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.HomePage
import atlow.chemi.mymada.InfoActivityKt
import atlow.chemi.mymada.PrivacyActivity
import atlow.chemi.mymada.R
import atlow.chemi.mymada.SettingsPage
import atlow.chemi.mymada.UtillitiesAndTools
import atlow.chemi.mymada.lockPage
import com.google.android.material.navigation.NavigationView
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Requires a disposable emulator with IPv4/IPv6 OUTPUT blocked before app startup. */
class HomeScreenTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val preferences = context.getSharedPreferences("Settings", 0)

    private fun shell(command: String) {
        instrumentation.uiAutomation.executeShellCommand(command).use {
            android.os.ParcelFileDescriptor.AutoCloseInputStream(it).readBytes()
        }
    }

    private fun prepare(language: String, scale: Float = 1f) {
        shell("settings put system font_scale $scale")
        shell("appops set ${context.packageName} SYSTEM_ALERT_WINDOW allow")
        preferences.edit().putString("lng", language).putBoolean("oldU", true)
            .putBoolean("privacy", true).putBoolean("Mapp", false)
            .putBoolean("WasLangChanged", false).putBoolean("realM", false)
            .putStringSet("Tools", setOf("metro", "oxy")).commit()
        context.getSharedPreferences("rate_app", 0).edit().putBoolean("dontshowagain", true).commit()
        instrumentation.waitForIdleSync()
    }

    @After fun restoreFontScale() { shell("settings put system font_scale 1.0") }

    @Test fun englishNavigationBackAndRecreation() = verifyHome("en", 1f)
    @Test fun hebrewRtlLargeTextNavigationBackAndRecreation() = verifyHome("he", 1.5f)

    private fun verifyHome(language: String, scale: Float) {
        prepare(language, scale)
        ActivityScenario.launch(HomePage::class.java).use { scenario ->
            compose.waitForIdle()
            val labels = mutableMapOf<Int, String>()
            var topInset = 0
            var bottomLimit = 0
            scenario.onActivity { activity ->
                assertEquals("atlow.chemi.mymada.HomePage", activity.componentName.className)
                assertEquals(if (language == "he") View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR,
                    activity.resources.configuration.layoutDirection)
                assertEquals(scale, activity.resources.configuration.fontScale, 0.01f)
                listOf(R.string.app_name, R.string.title_activity_utills_and_tools,
                    R.string.title_activity_moked_page, R.string.test_beep, R.string.title_activity_contact,
                    R.string.title_activity_info, R.string.action_settings, R.string.pref_title_enable,
                    R.string.TestCallMsg, R.string.plsNoteCon).forEach { labels[it] = activity.getString(it) }
                val insets = ViewCompat.getRootWindowInsets(activity.window.decorView)!!
                    .getInsets(WindowInsetsCompat.Type.systemBars())
                topInset = insets.top
                bottomLimit = activity.window.decorView.height - insets.bottom
            }
            val settings = compose.onNodeWithContentDescription(labels.getValue(R.string.action_settings))
            val detection = compose.onNodeWithContentDescription(labels.getValue(R.string.pref_title_enable))
            compose.onNodeWithTag("homeBanner").assertIsDisplayed().assertHeightIsEqualTo(50.dp)
            detection.assertIsOff()
            assertEquals(language == "en", detection.fetchSemanticsNode().boundsInRoot.center.x <
                settings.fetchSemanticsNode().boundsInRoot.center.x)
            assertTrue(settings.fetchSemanticsNode().boundsInRoot.top >= topInset)

            // The resume hook refreshes the MenuItem too: clicking must turn detection OFF, not start a service.
            scenario.moveToState(Lifecycle.State.CREATED)
            preferences.edit().putBoolean("Mapp", true).commit()
            scenario.moveToState(Lifecycle.State.RESUMED)
            detection.assertIsOn().performClick().assertIsOff()
            assertFalse(preferences.getBoolean("Mapp", true))

            for (id in listOf(R.string.title_activity_utills_and_tools, R.string.title_activity_moked_page,
                R.string.test_beep, R.string.title_activity_contact, R.string.title_activity_info)) {
                val node = compose.onNodeWithText(labels.getValue(id)).performScrollTo().assertIsDisplayed()
                assertTrue(node.fetchSemanticsNode().boundsInRoot.bottom <= bottomLimit)
            }
            scenario.recreate()
            compose.onNodeWithText(labels.getValue(R.string.title_activity_info)).assertIsDisplayed()
            detection.assertIsOff()

            visit(UtillitiesAndTools::class.java, {
                compose.onNodeWithText(labels.getValue(R.string.title_activity_utills_and_tools)).performScrollTo().performClick()
            }) { activity ->
                val menu = activity.findViewById<NavigationView>(R.id.navigation).menu
                assertNotNull(menu.findItem(R.id.oxygenCalc))
                assertNull(menu.findItem(R.id.apgarCalc))
                assertTrue(menu.findItem(R.id.metronome).isChecked)
            }
            visit(SettingsPage::class.java, { settings.performClick() })
            val openAbout = {
                compose.onNodeWithText(labels.getValue(R.string.title_activity_info)).performScrollTo().performClick()
                Unit
            }
            if (InstrumentationRegistry.getArguments().getString("isolateAbout", "false").toBoolean()) {
                // Optional Home-only check for the stock 48-MB emulator while About's bitmap OOM is investigated.
                val monitor = instrumentation.addMonitor(InfoActivityKt::class.java.name,
                    android.app.Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null), true)
                try {
                    openAbout()
                    assertEquals(1, monitor.hits)
                } finally { instrumentation.removeMonitor(monitor) }
            } else visit(InfoActivityKt::class.java, openAbout)
            compose.onNodeWithText(labels.getValue(R.string.title_activity_moked_page)).performScrollTo().performClick()
            visit(lockPage::class.java, { onView(withText("עמוד חסימה")).perform(click()) })
            assertFalse(preferences.getBoolean("realM", true))
            compose.onNodeWithText(labels.getValue(R.string.test_beep)).performScrollTo().performClick()
            onView(withText(labels.getValue(R.string.TestCallMsg))).check(matches(isDisplayed()))
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            compose.onNodeWithText(labels.getValue(R.string.title_activity_contact)).performScrollTo().performClick()
            onView(withText(labels.getValue(R.string.plsNoteCon))).check(matches(isDisplayed()))
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            compose.onNodeWithText(labels.getValue(R.string.app_name)).assertIsDisplayed()
            assertEquals(setOf("metro", "oxy"), preferences.getStringSet("Tools", emptySet()))
            compose.waitUntil(5_000) {
                var focused = false
                scenario.onActivity { focused = it.hasWindowFocus() }
                focused
            }
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            compose.waitUntil(5_000) { scenario.state != Lifecycle.State.RESUMED }
        }
    }

    private fun visit(destination: Class<out Activity>, click: () -> Unit, verify: (Activity) -> Unit = {}) {
        val monitor = instrumentation.addMonitor(destination.name, null, false)
        try {
            click()
            val activity = instrumentation.waitForMonitorWithTimeout(monitor, 5_000)
            assertNotNull("Navigation to ${destination.simpleName}", activity)
            compose.waitUntil(5_000) {
                var focused = false
                instrumentation.runOnMainSync { focused = activity!!.hasWindowFocus() }
                focused
            }
            instrumentation.runOnMainSync { verify(activity!!) }
            // Use the destination's actual back behavior, rather than finishing it from the test.
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            compose.waitUntil(5_000) { activity!!.isDestroyed }
            compose.waitForIdle()
        } finally { instrumentation.removeMonitor(monitor) }
    }

    @Test fun onboardingDestinationsAndLocaleReturn() {
        prepare("en")
        preferences.edit().putBoolean("oldU", false).commit()
        ActivityScenario.launch(HomePage::class.java).use { scenario ->
            onView(withText(R.string.newSettings)).check(matches(isDisplayed()))
            visit(SettingsPage::class.java, { onView(withText(R.string.title_activity_settings)).perform(click()) })
            // Settings' locale contract: Home recreates on return using the persisted selection.
            scenario.moveToState(Lifecycle.State.CREATED)
            preferences.edit().putString("lng", "he").putBoolean("WasLangChanged", true).commit()
            scenario.moveToState(Lifecycle.State.RESUMED)
            compose.onNodeWithText("ניסוי דיווחי מד\"א").assertIsDisplayed()
            assertFalse(preferences.getBoolean("WasLangChanged", true))
        }
        prepare("en")
        preferences.edit().putBoolean("privacy", false).commit()
        ActivityScenario.launch(HomePage::class.java).use {
            onView(withText(R.string.privacyMsg)).check(matches(isDisplayed()))
            visit(PrivacyActivity::class.java, { onView(withText(R.string.title_activity_privacy)).perform(click()) })
        }
    }
}
