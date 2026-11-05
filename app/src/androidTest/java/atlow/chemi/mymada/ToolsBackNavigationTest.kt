package atlow.chemi.mymada

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.Espresso.pressBackUnconditionally
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.fragments.*
import atlow.chemi.mymada.helpers.LocalHelper
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

/** Actual Compose drawer routes with restored platform Fragment content. */
class ToolsBackNavigationTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val prefs get() = context.getSharedPreferences("Settings", 0)
    private fun destination(id: Int) = compose.onNodeWithTag("shell-destination-$id")
    private fun open() = compose.onNodeWithTag("shell-navigation").performClick()
    private fun shell(command: String) = ParcelFileDescriptor.AutoCloseInputStream(
        instrumentation.uiAutomation.executeShellCommand(command)).use { it.readBytes() }
    private fun capture(name: String) {
        compose.waitForIdle()
        instrumentation.uiAutomation.takeScreenshot()!!.let { bitmap ->
            File(context.getExternalFilesDir("tools-shell"), "$name.png").outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            bitmap.recycle()
        }
    }

    @Test fun routesRecreateAndSystemBackInBothLocales() {
        for (language in listOf("en", "he")) {
            instrumentation.runOnMainSync { LocalHelper.setLocale(context, language) }
            prefs.edit().putBoolean("Mapp", false).putStringSet("Tools", setOf("pulse", "oxy", "apgar", "prot")).commit()
            ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
                compose.waitForIdle()
                scenario.onActivity { assertTrue(it.supportFragmentManager.findFragmentById(R.id.frame) is PulseCalc) }
                val metrics = context.resources.displayMetrics
                val start = (48 * metrics.density).toInt()
                val from = if (language == "he") metrics.widthPixels - start else start
                val to = if (language == "he") metrics.widthPixels / 4 else metrics.widthPixels * 3 / 4
                shell("input swipe $from ${metrics.heightPixels / 3} $to ${metrics.heightPixels / 3} 400")
                compose.waitForIdle()
                destination(R.id.pulseCalc).assertIsDisplayed()
                destination(R.id.pulseCalc).assertIsSelected()
                capture("tools-$language-drawer")
                destination(R.id.apgarCalc).performClick()
                compose.waitForIdle()
                scenario.onActivity {
                    assertTrue(it.supportFragmentManager.findFragmentById(R.id.frame) is ApgarCalc)
                    it.window.decorView.findViewWithTag<android.widget.Spinner>("apgar-choice-0").setSelection(2)
                }
                compose.waitForIdle()
                scenario.recreate()
                compose.waitForIdle()
                scenario.onActivity {
                    assertTrue(it.supportFragmentManager.findFragmentById(R.id.frame) is ApgarCalc)
                    assertEquals(2, it.window.decorView.findViewWithTag<android.widget.Spinner>("apgar-choice-0").selectedItemPosition)
                }
                capture("tools-$language-restored")
                open()
                destination(R.id.apgarCalc).assertIsSelected()
                pressBack()
                compose.waitForIdle()
                scenario.onActivity { assertFalse(it.isFinishing) }
                // Drawer state itself is saveable, independently from the active fragment.
                open()
                compose.waitForIdle()
                scenario.recreate()
                compose.waitForIdle()
                destination(R.id.apgarCalc).assertIsDisplayed().assertIsSelected()
                pressBack()
                compose.waitForIdle()
                pressBackUnconditionally()
                compose.waitUntil(5000) { scenario.state == Lifecycle.State.CREATED || scenario.state == Lifecycle.State.DESTROYED }
            }
        }
    }

    @Test fun settingsChildRouteRestoreAndAsyncDetectionState() {
        instrumentation.runOnMainSync { LocalHelper.setLocale(context, "en") }
        prefs.edit().putBoolean("Mapp", false).commit()
        ActivityScenario.launch(SettingsPage::class.java).use { scenario ->
            compose.waitForIdle()
            // The emulator runner grants overlay before launch to avoid unrelated permission UI.
            scenario.onActivity { it.displayFragment(SettingsNotificationsSounds(), R.id.soundAndVib) }
            compose.waitForIdle()
            scenario.recreate()
            compose.waitForIdle()
            scenario.onActivity { assertTrue(it.supportFragmentManager.findFragmentById(R.id.frame) is SettingsNotificationsSounds) }
            open()
            destination(R.id.soundAndVib).assertIsSelected()
            destination(R.id.detect).performScrollTo().assertIsOff()
            instrumentation.runOnMainSync { prefs.edit().putBoolean("Mapp", true).apply() }
            destination(R.id.detect).assertIsOn()
            instrumentation.runOnMainSync { prefs.edit().putBoolean("Mapp", false).apply() }
            destination(R.id.detect).assertIsOff()
            capture("settings-en-async-detection")
        }
    }

    @Test fun toolbarObservesAsyncDetectionAndLandscapeKeepsFragment() {
        prefs.edit().putBoolean("Mapp", false).putStringSet("Tools", setOf("oxy")).commit()
        ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
            compose.onNodeWithTag("shell-detection").assertIsOff()
            instrumentation.runOnMainSync { prefs.edit().putBoolean("Mapp", true).apply() }
            compose.onNodeWithTag("shell-detection").assertIsOn()
            instrumentation.runOnMainSync { prefs.edit().putBoolean("Mapp", false).apply() }
            scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
            compose.waitForIdle()
            scenario.onActivity { assertTrue(it.supportFragmentManager.findFragmentById(R.id.frame) is OxyCalc) }
            capture("tools-landscape")
        }
    }

    @Test fun lockedLandscapeDrawerLeavesOnBack() {
        shell("wm size 1600x2560")
        if (android.os.Build.VERSION.SDK_INT >= 29) shell("wm user-rotation lock 1")
        else {
            shell("settings put system accelerometer_rotation 0")
            shell("settings put system user_rotation 1")
        }
        try {
            prefs.edit().putBoolean("Mapp", false).putStringSet("Tools", setOf("oxy")).commit()
            ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
                compose.waitUntil(10000) {
                    var focused = false
                    scenario.onActivity { focused = it.hasWindowFocus() && it.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE }
                    focused
                }
                compose.waitForIdle()
                scenario.onActivity { assertTrue("Physical landscape tablet", it.isDeviceTablet()) }
                destination(R.id.oxygenCalc).assertIsDisplayed().assertIsSelected()
                androidx.test.espresso.Espresso.closeSoftKeyboard()
                pressBackUnconditionally()
                compose.waitUntil(5000) { scenario.state == Lifecycle.State.CREATED || scenario.state == Lifecycle.State.DESTROYED }
            }
        } finally {
            shell("wm size reset")
            if (android.os.Build.VERSION.SDK_INT >= 29) shell("wm user-rotation lock 0")
            else shell("settings put system user_rotation 0")
        }
    }

    @Test fun systemEdgeGestureClosesDrawerBeforeLeaving() {
        if (android.os.Build.VERSION.SDK_INT < 29) return
        val wasGestural = android.provider.Settings.Secure.getInt(context.contentResolver, "navigation_mode", 0) == 2
        shell("cmd overlay enable-exclusive --category com.android.internal.systemui.navbar.gestural")
        try {
            instrumentation.runOnMainSync { LocalHelper.setLocale(context, "en") }
            prefs.edit().putBoolean("Mapp", false).putStringSet("Tools", setOf("oxy")).commit()
            ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
                compose.waitForIdle()
                open()
                compose.waitForIdle()
                val metrics = context.resources.displayMetrics
                val x = metrics.widthPixels - 1
                val y = metrics.heightPixels / 2
                shell("input swipe $x $y ${x - 300} $y 400")
                compose.waitForIdle()
                scenario.onActivity { assertFalse(it.isFinishing) }
                destination(R.id.oxygenCalc).assertIsNotDisplayed()
                shell("input swipe $x $y ${x - 300} $y 400")
                compose.waitUntil(5000) { scenario.state == Lifecycle.State.CREATED || scenario.state == Lifecycle.State.DESTROYED }
            }
        } finally {
            shell("cmd overlay enable-exclusive --category com.android.internal.systemui.navbar.${if (wasGestural) "gestural" else "threebutton"}")
        }
    }

    @Test fun childToolbarActionsAndSearchRestoreRemainAvailable() {
        instrumentation.runOnMainSync { LocalHelper.setLocale(context, "en") }
        prefs.edit().putBoolean("Mapp", false).putStringSet("Tools", setOf("dict", "contrac")).commit()
        ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
            compose.onNodeWithTag("shell-action-${R.id.action_delete}").performClick()
            androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withText(context.getString(R.string.ContracDeleteEmpty)))
                .inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog())
                .check(androidx.test.espresso.assertion.ViewAssertions.matches(androidx.test.espresso.matcher.ViewMatchers.isDisplayed()))
            androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(android.R.id.button1))
                .inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).perform(androidx.test.espresso.action.ViewActions.click())
            open()
            destination(R.id.dictionary).performScrollTo().performClick()
            compose.onNodeWithTag("shell-action-${R.id.app_bar_search}").performClick()
            compose.waitForIdle()
            scenario.onActivity { it.findViewById<android.widget.TextView>(androidx.appcompat.R.id.search_src_text).text = "fixture query" }
            compose.waitForIdle()
            scenario.recreate()
            compose.waitForIdle()
            scenario.onActivity {
                assertEquals("fixture query", it.findViewById<android.widget.TextView>(androidx.appcompat.R.id.search_src_text).text.toString())
            }
            compose.onNodeWithTag("shell-navigation").performClick()
            compose.onNodeWithTag("shell-action-${R.id.app_bar_word_add}").performClick()
            androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.wordName))
                .check(androidx.test.espresso.assertion.ViewAssertions.matches(androidx.test.espresso.matcher.ViewMatchers.isDisplayed()))
            // No submit: all checks are isolated and never write dictionary data.
        }
    }

    @Test fun runningMetronomeIntentOverridesHiddenPreference() {
        prefs.edit().putBoolean("Mapp", false).putStringSet("Tools", setOf("oxy")).commit()
        val intent = android.content.Intent(context, UtillitiesAndTools::class.java).putExtra("running", true)
        ActivityScenario.launch<UtillitiesAndTools>(intent).use { scenario ->
            compose.waitForIdle()
            scenario.onActivity { assertTrue(it.supportFragmentManager.findFragmentById(R.id.frame) is MetroActivity) }
            scenario.recreate()
            compose.waitForIdle()
            scenario.onActivity { assertTrue(it.supportFragmentManager.findFragmentById(R.id.frame) is MetroActivity) }
            open()
            destination(R.id.metronome).assertDoesNotExist()
            destination(R.id.oxygenCalc).assertIsDisplayed()
        }
    }

    @Test fun permissionCompletionUpdatesToolbarWithoutAnotherResume() {
        instrumentation.runOnMainSync { LocalHelper.setLocale(context, "en") }
        val component = "${context.packageName}/atlow.chemi.mymada.NotificationService"
        val old = android.provider.Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        if (android.os.Build.VERSION.SDK_INT >= 33) shell("pm grant ${context.packageName} android.permission.POST_NOTIFICATIONS")
        if (android.os.Build.VERSION.SDK_INT >= 27) shell("cmd notification disallow_listener $component")
        else shell("settings delete secure enabled_notification_listeners")
        prefs.edit().putBoolean("Mapp", false).putStringSet("Tools", setOf("oxy")).commit()
        try {
            ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
                compose.onNodeWithTag("shell-detection").performClick()
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(android.R.id.button1))
                    .perform(androidx.test.espresso.action.ViewActions.click())
                instrumentation.uiAutomation.waitForIdle(500, 5000)
                // Idle alone can occur before the external Settings window opens.
                compose.waitUntil(10000) {
                    instrumentation.uiAutomation.rootInActiveWindow?.packageName == "com.android.settings"
                }
                if (android.os.Build.VERSION.SDK_INT >= 27) shell("cmd notification allow_listener $component")
                else shell("settings put secure enabled_notification_listeners $component")
                shell("input keyevent 4")
                compose.waitUntil(10000) { prefs.getBoolean("Mapp", false) }
                compose.onNodeWithTag("shell-detection").assertIsOn()
                scenario.onActivity { assertFalse(it.isFinishing) }
                compose.onNodeWithTag("shell-detection").performClick()
                compose.onNodeWithTag("shell-detection").assertIsOff()
            }
        } finally {
            if (android.os.Build.VERSION.SDK_INT >= 27) shell("cmd notification disallow_listener $component")
            else if (old == null) shell("settings delete secure enabled_notification_listeners")
            else shell("settings put secure enabled_notification_listeners $old")
            prefs.edit().putBoolean("Mapp", false).commit()
        }
    }

    @Test fun legacyFragmentStateWithoutDestinationKeepsTheRestoredRoute() {
        prefs.edit().putBoolean("Mapp", false).putStringSet("Tools", setOf("metro", "apgar")).commit()
        val application = context.applicationContext as android.app.Application
        val callback = object : android.app.Application.ActivityLifecycleCallbacks {
            override fun onActivitySaveInstanceState(activity: android.app.Activity, state: android.os.Bundle) {
                if (activity is UtillitiesAndTools || activity is SettingsPage) state.remove("destination")
            }
            override fun onActivityCreated(activity: android.app.Activity, state: android.os.Bundle?) = Unit
            override fun onActivityStarted(activity: android.app.Activity) = Unit
            override fun onActivityResumed(activity: android.app.Activity) = Unit
            override fun onActivityPaused(activity: android.app.Activity) = Unit
            override fun onActivityStopped(activity: android.app.Activity) = Unit
            override fun onActivityDestroyed(activity: android.app.Activity) = Unit
        }
        application.registerActivityLifecycleCallbacks(callback)
        try {
            ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
                open()
                destination(R.id.apgarCalc).performClick()
                compose.waitForIdle()
                scenario.recreate()
                compose.waitForIdle()
                scenario.onActivity { assertTrue(it.supportFragmentManager.findFragmentById(R.id.frame) is ApgarCalc) }
                open()
                destination(R.id.apgarCalc).assertIsSelected()
                destination(R.id.metronome).performClick()
                compose.waitForIdle()
                scenario.onActivity { assertTrue(it.supportFragmentManager.findFragmentById(R.id.frame) is MetroActivity) }
            }
            ActivityScenario.launch(SettingsPage::class.java).use { scenario ->
                open()
                destination(R.id.advanced).performClick()
                compose.waitForIdle()
                scenario.recreate()
                compose.waitForIdle()
                scenario.onActivity { assertTrue(it.supportFragmentManager.findFragmentById(R.id.frame) is SettingsAdvanced) }
                open()
                destination(R.id.advanced).assertIsSelected()
            }
        } finally { application.unregisterActivityLifecycleCallbacks(callback) }
    }

    @Test fun changedLocaleRecreatesThePausedShellWithoutLosingItsDestination() {
        instrumentation.runOnMainSync { LocalHelper.setLocale(context, "en") }
        prefs.edit().putBoolean("Mapp", false).putStringSet("Tools", setOf("apgar")).commit()
        ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
            compose.waitForIdle()
            scenario.moveToState(Lifecycle.State.CREATED)
            instrumentation.runOnMainSync { LocalHelper.setLocale(context, "he") }
            scenario.moveToState(Lifecycle.State.RESUMED)
            compose.waitUntil(10000) {
                var ready = false
                scenario.onActivity { ready = it.hasWindowFocus() && it.resources.configuration.locales[0].language in listOf("he", "iw") }
                ready
            }
            compose.waitForIdle()
            scenario.onActivity { assertTrue(it.supportFragmentManager.findFragmentById(R.id.frame) is ApgarCalc) }
            open()
            destination(R.id.apgarCalc).assertIsSelected()
        }
    }
}
