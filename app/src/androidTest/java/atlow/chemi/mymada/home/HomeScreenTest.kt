package atlow.chemi.mymada.home

import android.app.Activity
import android.view.KeyEvent
import android.view.View
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
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
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.HomePage
import atlow.chemi.mymada.InfoActivityKt
import atlow.chemi.mymada.PrivacyActivity
import atlow.chemi.mymada.R
import atlow.chemi.mymada.SettingsPage
import atlow.chemi.mymada.UtillitiesAndTools
import atlow.chemi.mymada.lockPage
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
        // API 24 restarts its launcher on font changes; let that transition finish before launching Home.
        instrumentation.uiAutomation.waitForIdle(500, 5_000)
        shell("appops set ${context.packageName} SYSTEM_ALERT_WINDOW allow")
        instrumentation.runOnMainSync {
            atlow.chemi.mymada.helpers.LocalHelper.setLocale(context, language)
        }
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

    @Test fun testAndContactPromptsRestoreTheirActionsAcrossRecreation() {
        for (language in listOf("en", "he")) {
            prepare(language)
            ActivityScenario.launch(HomePage::class.java).use { scenario ->
                val labels = mutableMapOf<Int, String>()
                scenario.onActivity { activity ->
                    listOf(R.string.test_beep, R.string.TestCallMsg, R.string.title_activity_contact,
                        R.string.plsNoteCon).forEach { labels[it] = activity.getString(it) }
                }
                for ((action, message) in listOf(R.string.test_beep to R.string.TestCallMsg,
                    R.string.title_activity_contact to R.string.plsNoteCon)) {
                    compose.onNodeWithText(labels.getValue(action)).performScrollTo().performClick()
                    scenario.recreate()
                    onView(withText(labels.getValue(message))).inRoot(isDialog()).check(matches(isDisplayed()))
                    instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
                    waitForHomeFocus(scenario)
                }
            }
        }
    }

    @Test fun repeatedTestCallThenContactDialogTargetsFocusedWindow() {
        for (language in listOf("en", "he")) {
            prepare(language)
            ActivityScenario.launch(HomePage::class.java).use { scenario ->
                val labels = mutableMapOf<Int, String>()
                scenario.onActivity { activity ->
                    listOf(R.string.test_beep, R.string.TestCallMsg, R.string.title_activity_contact,
                        R.string.plsNoteCon).forEach { labels[it] = activity.getString(it) }
                }
                repeat(20) { round ->
                    for ((action, message) in listOf(R.string.test_beep to R.string.TestCallMsg,
                        R.string.title_activity_contact to R.string.plsNoteCon)) {
                        compose.onNodeWithText(labels.getValue(action)).performScrollTo().performClick()
                        onView(withText(labels.getValue(message))).inRoot(isDialog()).check { view, failure ->
                            if (failure != null) throw failure
                            val params = view.rootView.layoutParams as android.view.WindowManager.LayoutParams
                            assertTrue(view.hasWindowFocus())
                            assertEquals(android.view.WindowManager.LayoutParams.TYPE_APPLICATION, params.type)
                        }
                        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
                        waitForHomeFocus(scenario)
                    }
                    if (round % 5 == 4) scenario.recreate()
                }
            }
        }
    }

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
            }) {
                compose.onNodeWithTag("shell-navigation").performClick()
                compose.onNodeWithTag("shell-destination-${R.id.oxygenCalc}").assertExists()
                compose.onNodeWithTag("shell-destination-${R.id.apgarCalc}").assertDoesNotExist()
                compose.onNodeWithTag("shell-destination-${R.id.metronome}").assertIsSelected()
                compose.onNodeWithTag("shell-navigation").performClick()
            }
            visit(SettingsPage::class.java, { settings.performClick() })
            if (InstrumentationRegistry.getArguments().getString("dumpHeap") == "true") {
                android.os.Debug.dumpHprofData(java.io.File(context.filesDir, "home-$language-before-about.hprof").path)
            }
            visit(InfoActivityKt::class.java, {
                compose.onNodeWithText(labels.getValue(R.string.title_activity_info)).performScrollTo().performClick()
            })
            compose.onNodeWithText(labels.getValue(R.string.title_activity_moked_page)).performScrollTo().performClick()
            visit(lockPage::class.java, { onView(withText("עמוד חסימה")).inRoot(isDialog()).perform(click()) })
            assertFalse(preferences.getBoolean("realM", true))
            compose.onNodeWithText(labels.getValue(R.string.test_beep)).performScrollTo().performClick()
            onView(withText(labels.getValue(R.string.TestCallMsg))).inRoot(isDialog()).check(matches(isDisplayed()))
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            waitForHomeFocus(scenario)
            compose.onNodeWithText(labels.getValue(R.string.title_activity_contact)).performScrollTo().performClick()
            onView(withText(labels.getValue(R.string.plsNoteCon))).inRoot(isDialog()).check(matches(isDisplayed()))
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            compose.onNodeWithText(labels.getValue(R.string.app_name)).assertIsDisplayed()
            assertEquals(setOf("metro", "oxy"), preferences.getStringSet("Tools", emptySet()))
            waitForHomeFocus(scenario)
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            compose.waitUntil(5_000) { scenario.state != Lifecycle.State.RESUMED }
        }
    }

    private fun waitForHomeFocus(scenario: ActivityScenario<HomePage>) {
        compose.waitUntil(5_000) {
            var focused = false
            scenario.onActivity { focused = it.hasWindowFocus() }
            focused
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
            verify(activity!!)
            // Use the destination's actual back behavior, rather than finishing it from the test.
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            compose.waitUntil(5_000) { activity!!.isDestroyed }
            compose.waitForIdle()
        } finally { instrumentation.removeMonitor(monitor) }
    }

    @Test fun onboardingPromptRestoresOnceAndStaysDismissedAcrossRecreation() {
        for (language in listOf("en", "he")) {
            prepare(language)
            preferences.edit().putBoolean("oldU", false).commit()
            ActivityScenario.launch(HomePage::class.java).use { scenario ->
                var message = ""
                var cancel = ""
                scenario.onActivity {
                    message = it.getString(R.string.newSettings)
                    cancel = it.getString(R.string.dialog_no)
                }
                scenario.recreate()
                compose.waitUntil(5_000) {
                    var focused = false
                    scenario.onActivity {
                        focused = it.supportFragmentManager.fragments.filterIsInstance<HomePrompt>()
                            .singleOrNull()?.dialog?.window?.decorView?.hasWindowFocus() == true
                    }
                    focused
                }
                onView(withText(message)).inRoot(isDialog()).check(matches(isDisplayed()))
                scenario.onActivity {
                    assertEquals(1, it.supportFragmentManager.fragments.filterIsInstance<HomePrompt>().size)
                }
                onView(withText(cancel)).inRoot(isDialog()).perform(click())
                scenario.recreate()
                onView(withText(message)).check(doesNotExist())
            }
        }
    }

    @Test fun multiplePromptsRestoreInOrderUsingTheNewLocale() {
        prepare("en")
        preferences.edit().putBoolean("oldU", false).commit()
        ActivityScenario.launch(HomePage::class.java).use { scenario ->
            // Home can show onboarding and channel-importance prompts in one creation.
            // Exercise both saved dialogs without enabling a detection service or modifying OS channels.
            scenario.onActivity {
                HomePrompt.show(it, R.string.importanceMsg, 0, R.string.importance,
                    android.content.Intent(it, SettingsPage::class.java), false, 0)
                it.supportFragmentManager.executePendingTransactions()
            }
            preferences.edit().putString("lng", "he").commit()
            scenario.recreate()
            var importance = ""
            var onboarding = ""
            var cancel = ""
            scenario.onActivity {
                importance = it.getString(R.string.importanceMsg)
                onboarding = it.getString(R.string.newSettings)
                cancel = it.getString(R.string.dialog_no)
                val prompts = it.supportFragmentManager.fragments.filterIsInstance<HomePrompt>()
                assertEquals(2, prompts.size)
                assertEquals(listOf(onboarding, importance), prompts.map { prompt ->
                    prompt.requireDialog().findViewById<android.widget.TextView>(android.R.id.message).text.toString()
                })
            }
            compose.waitUntil(5_000) {
                var focused = false
                scenario.onActivity {
                    focused = it.supportFragmentManager.fragments.filterIsInstance<HomePrompt>().last()
                        .requireDialog().window!!.decorView.hasWindowFocus()
                }
                focused
            }
            onView(withText(importance)).inRoot(isDialog()).check(matches(isDisplayed()))
            onView(withText(cancel)).inRoot(isDialog()).perform(click())
            onView(withText(onboarding)).inRoot(isDialog()).check(matches(isDisplayed()))
            onView(withText(cancel)).inRoot(isDialog()).perform(click())
        }
    }

    @Test fun onboardingDestinationsAndLocaleReturn() {
        prepare("en")
        preferences.edit().putBoolean("oldU", false).commit()
        ActivityScenario.launch(HomePage::class.java).use { scenario ->
            onView(withText(R.string.newSettings)).inRoot(isDialog()).check(matches(isDisplayed()))
            visit(SettingsPage::class.java, { onView(withText(R.string.title_activity_settings)).inRoot(isDialog()).perform(click()) })
            // Settings' locale contract: Home recreates on return using the persisted selection.
            scenario.moveToState(Lifecycle.State.CREATED)
            instrumentation.runOnMainSync { atlow.chemi.mymada.helpers.LocalHelper.setLocale(context, "he") }
            preferences.edit().putString("lng", "he").putBoolean("WasLangChanged", true).commit()
            scenario.moveToState(Lifecycle.State.RESUMED)
            compose.onNodeWithText("ניסוי דיווחי מד\"א").assertIsDisplayed()
            assertFalse(preferences.getBoolean("WasLangChanged", true))
        }
        prepare("en")
        preferences.edit().putBoolean("privacy", false).commit()
        ActivityScenario.launch(HomePage::class.java).use {
            onView(withText(R.string.privacyMsg)).inRoot(isDialog()).check(matches(isDisplayed()))
            visit(PrivacyActivity::class.java, { onView(withText(R.string.title_activity_privacy)).inRoot(isDialog()).perform(click()) })
        }
    }
}
