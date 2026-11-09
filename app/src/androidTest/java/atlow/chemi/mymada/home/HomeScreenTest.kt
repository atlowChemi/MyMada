package atlow.chemi.mymada.home

import android.app.Activity
import android.view.KeyEvent
import android.view.View
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed as isComposeDisplayed
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
import atlow.chemi.mymada.waitForShellDrawer
import atlow.chemi.mymada.waitForShellCondition
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
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            shell("pm grant ${context.packageName} android.permission.POST_NOTIFICATIONS")
        }
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

    @Test fun detectionIconTracksAsynchronousPreferenceChangesWithoutNavigation() {
        prepare("en")
        ActivityScenario.launch(HomePage::class.java).use { scenario ->
            var label = ""
            scenario.onActivity { label = it.getString(R.string.pref_title_enable) }
            val detection = compose.onNodeWithContentDescription(label)
            detection.assertIsOff()
            // The permission fragment completes after the click handler has returned.
            // Update its persisted result while Home stays resumed, without a menu click.
            scenario.onActivity { preferences.edit().putBoolean("Mapp", true).apply() }
            detection.assertIsOn()
            scenario.onActivity { preferences.edit().putBoolean("Mapp", false).apply() }
            detection.assertIsOff()
            scenario.moveToState(Lifecycle.State.CREATED)
            preferences.edit().putBoolean("Mapp", true).commit()
            scenario.moveToState(Lifecycle.State.RESUMED)
            detection.assertIsOn()
            scenario.recreate()
            detection.assertIsOn()
            scenario.onActivity { preferences.edit().putBoolean("Mapp", false).apply() }
            detection.assertIsOff()
        }
    }

    @Test fun homePromptActionsKeepNativeMinimumAfterRecreationAndFontScaling() {
        for ((language, scale) in listOf("en" to 1f, "he" to 1.5f)) {
            prepare(language, scale)
            ActivityScenario.launch(HomePage::class.java).use { scenario ->
                for (kind in listOf("privacy", "test", "contact")) {
                    scenario.onActivity { activity ->
                        if (kind == "privacy") HomePrompt.show(activity, R.string.privacyMsg, 0,
                            R.string.title_activity_privacy, android.content.Intent(activity, PrivacyActivity::class.java), false, 0)
                        else HomeActions.ActionPrompt.show(activity, kind)
                        activity.supportFragmentManager.executePendingTransactions()
                    }
                    repeat(2) { pass ->
                        if (pass == 1) scenario.recreate()
                        waitForPromptFocus(scenario)
                        scenario.onActivity { activity ->
                            val alert = activity.supportFragmentManager.fragments
                                .filterIsInstance<androidx.fragment.app.DialogFragment>().single()
                                .requireDialog() as androidx.appcompat.app.AlertDialog
                            // Independent reference: inflate the native button style in the pre-edge-to-edge viewport.
                            val config = android.content.res.Configuration(activity.resources.configuration)
                            if (android.os.Build.VERSION.SDK_INT >= 35) {
                                val decor = activity.window.decorView
                                val bars = ViewCompat.getRootWindowInsets(decor)!!.getInsetsIgnoringVisibility(
                                    WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
                                config.screenHeightDp = ((decor.height - bars.top - bars.bottom) /
                                    activity.resources.displayMetrics.density).toInt()
                            }
                            val theme = androidx.appcompat.view.ContextThemeWrapper(
                                activity.createConfigurationContext(config), R.style.AlertDialogStyle)
                            val reference = androidx.appcompat.widget.AppCompatButton(theme, null,
                                androidx.appcompat.R.attr.buttonBarButtonStyle)
                            for (which in intArrayOf(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE,
                                androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)) {
                                val button = alert.getButton(which)
                                assertEquals(reference.minHeight, button.minHeight)
                                assertEquals(reference.minimumHeight, button.minimumHeight)
                                assertTrue(button.height >= button.minHeight)
                                assertTrue(button.isShown)
                                assertTrue(button.isClickable)
                            }
                            assertEquals(scale, alert.context.resources.configuration.fontScale, .01f)
                        }
                    }
                    // Dismiss without navigating or emitting a test call; callbacks/order remain covered above.
                    scenario.onActivity { activity ->
                        activity.supportFragmentManager.fragments
                            .filterIsInstance<androidx.fragment.app.DialogFragment>().single().dismiss()
                        activity.supportFragmentManager.executePendingTransactions()
                    }
                    waitForHomeFocus(scenario)
                }
            }
        }
    }
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
            val banner = compose.onNodeWithTag("homeBanner").fetchSemanticsNode().boundsInWindow
            // Approved offline policy, intentionally different from production12822 on API24.
            // Run with external IPv4/IPv6 traffic rejected; never accept navy based on ad height.
            compose.waitUntil(5_000) {
                val bitmap = instrumentation.uiAutomation.takeScreenshot()
                try {
                    listOf(0.1f, 0.5f, 0.9f).all { fraction ->
                        val color = bitmap.getPixel((banner.left + banner.width * fraction).toInt(), banner.center.y.toInt())
                        android.graphics.Color.red(color) > 100 &&
                            kotlin.math.abs(android.graphics.Color.blue(color) - android.graphics.Color.red(color)) < 5
                    }
                } finally { bitmap.recycle() }
            }
            detection.assertIsOff()
            assertEquals(language == "en", detection.fetchSemanticsNode().boundsInRoot.center.x <
                settings.fetchSemanticsNode().boundsInRoot.center.x)
            assertTrue(settings.fetchSemanticsNode().boundsInRoot.top >= topInset)

            // A stale enabled preference without listener access requests permission;
            // decline that request here instead of leaving it beneath later dialogs.
            scenario.moveToState(Lifecycle.State.CREATED)
            preferences.edit().putBoolean("Mapp", true).commit()
            scenario.moveToState(Lifecycle.State.RESUMED)
            detection.assertIsOn().performClick().assertIsOff()
            if (atlow.chemi.mymada.calls.ListenerAccess.selected(context) == null) {
                onView(withText(R.string.dialog_no)).inRoot(isDialog()).perform(click())
                waitForHomeFocus(scenario)
            }
            assertFalse(preferences.getBoolean("Mapp", true))

            for (id in listOf(R.string.title_activity_utills_and_tools, R.string.title_activity_moked_page,
                R.string.test_beep, R.string.title_activity_contact, R.string.title_activity_info)) {
                val node = compose.onNodeWithText(labels.getValue(id)).performScrollTo()
                // Lazy-grid semantics can move before Android's visible viewport
                // catches up. Still require the real displayed-state predicate.
                compose.waitUntil(5_000) { node.isComposeDisplayed() }
                try {
                    node.assertIsDisplayed()
                } catch (failure: AssertionError) {
                    var window = ""
                    instrumentation.runOnMainSync {
                        val resumed = androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry.getInstance()
                            .getActivitiesInStage(androidx.test.runner.lifecycle.Stage.RESUMED)
                        window = resumed.joinToString { "${it.javaClass.simpleName}:focus=${it.hasWindowFocus()}" }
                    }
                    throw AssertionError("Home scroll $language/$scale id=$id: " +
                        "bounds=${node.fetchSemanticsNode().boundsInWindow}, bottomLimit=$bottomLimit, " +
                        "scenario=${scenario.state}, resumed=[$window]", failure)
                }
                assertTrue(node.fetchSemanticsNode().boundsInRoot.bottom <= bottomLimit)
            }
            scenario.recreate()
            compose.onNodeWithText(labels.getValue(R.string.title_activity_info)).assertIsDisplayed()
            detection.assertIsOff()

            visit(UtillitiesAndTools::class.java, {
                compose.onNodeWithText(labels.getValue(R.string.title_activity_utills_and_tools)).performScrollTo().performClick()
            }) {
                compose.onNodeWithTag("shell-navigation").performClick()
                compose.waitForShellDrawer(open = true)
                compose.onNodeWithTag("shell-destination-${R.id.oxygenCalc}").assertExists()
                compose.onNodeWithTag("shell-destination-${R.id.apgarCalc}").assertDoesNotExist()
                compose.onNodeWithTag("shell-destination-${R.id.metronome}").assertIsSelected()
                compose.onNodeWithTag("shell-navigation").performClick()
                compose.waitForShellDrawer(open = false)
            }
            visit(SettingsPage::class.java, { settings.performClick() })
            if (InstrumentationRegistry.getArguments().getString("dumpHeap") == "true") {
                android.os.Debug.dumpHprofData(java.io.File(context.filesDir, "home-$language-before-about.hprof").path)
            }
            visit(InfoActivityKt::class.java, {
                compose.onNodeWithText(labels.getValue(R.string.title_activity_info)).performScrollTo().performClick()
            })
            compose.onNodeWithText(labels.getValue(R.string.title_activity_moked_page)).performScrollTo().performClick()
            visit(lockPage::class.java, {
                // A native destination can pause the Compose owner before Espresso's
                // post-click idle wait completes. Invoke the real dialog button on main.
                waitForPromptFocus(scenario)
                scenario.onActivity { activity ->
                    val dialog = activity.supportFragmentManager.fragments
                        .filterIsInstance<HomeActions.ActionPrompt>().single().requireDialog() as androidx.appcompat.app.AlertDialog
                    val button = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                    assertEquals("עמוד חסימה", button.text.toString())
                    assertTrue(button.performClick())
                }
            })
            assertFalse(preferences.getBoolean("realM", true))
            compose.onNodeWithText(labels.getValue(R.string.test_beep)).performScrollTo().performClick()
            onView(withText(labels.getValue(R.string.TestCallMsg))).inRoot(isDialog()).check(matches(isDisplayed()))
            waitForPromptFocus(scenario)
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            waitForHomeFocus(scenario)
            compose.onNodeWithText(labels.getValue(R.string.title_activity_contact)).performScrollTo().performClick()
            onView(withText(labels.getValue(R.string.plsNoteCon))).inRoot(isDialog()).check(matches(isDisplayed()))
            waitForPromptFocus(scenario)
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            compose.onNodeWithText(labels.getValue(R.string.app_name)).assertIsDisplayed()
            assertEquals(setOf("metro", "oxy"), preferences.getStringSet("Tools", emptySet()))
            waitForHomeFocus(scenario)
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            compose.waitUntil(5_000) { scenario.state != Lifecycle.State.RESUMED }
        }
    }

    private fun waitForHomeFocus(scenario: ActivityScenario<HomePage>) {
        compose.waitForShellCondition("home-focus") {
            var focused = false
            scenario.onActivity { focused = it.hasWindowFocus() }
            focused
        }
    }

    private fun waitForPromptFocus(scenario: ActivityScenario<HomePage>) {
        compose.waitForShellCondition("home-prompt-focus") {
            var focused = false
            scenario.onActivity { activity ->
                focused = activity.supportFragmentManager.fragments
                    .filterIsInstance<androidx.fragment.app.DialogFragment>()
                    .any { it.dialog?.window?.decorView?.hasWindowFocus() == true }
            }
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
            compose.waitUntil(5_000) {
                var focused = false
                instrumentation.runOnMainSync { focused = activity.hasWindowFocus() }
                focused
            }
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
