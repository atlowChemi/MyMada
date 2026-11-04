package atlow.chemi.mymada.settings

import android.graphics.Bitmap
import android.view.KeyEvent
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.HomePage
import atlow.chemi.mymada.R
import atlow.chemi.mymada.SettingsPage
import atlow.chemi.mymada.helpers.LocalHelper
import atlow.chemi.mymada.selectShellDestination
import atlow.chemi.mymada.waitForShellDrawer
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

class SettingsChoiceTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    @Test fun nativeChoiceFontAndCancelSurviveRecreation() {
        val preferences = context.getSharedPreferences("Settings", 0)
        preferences.edit().putBoolean("Mapp", false).putInt("merhav", 4).commit()
        val language = preferences.getString("lng", null)
        ActivityScenario.launch(SettingsPage::class.java).use { scenario ->
            for (region in listOf(true, false)) {
                scenario.onActivity { SettingsChoiceDialog.show(it, region) }
                scenario.recreate()
                if (!region && android.os.Build.VERSION.SDK_INT >= 33) onView(withText("English"))
                    .inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).check { view, error ->
                        if (error != null) throw error
                        assertFalse((view as android.widget.TextView).isFallbackLineSpacing)
                    }
                onView(androidx.test.espresso.matcher.ViewMatchers.withId(androidx.appcompat.R.id.alertTitle))
                    .inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).check { view, error ->
                        if (error != null) throw error
                        val title = view as android.widget.TextView
                        assertEquals(androidx.core.content.res.ResourcesCompat.getFont(title.context, R.font.opensans_hebrew), title.typeface)
                    }
                onView(androidx.test.espresso.matcher.ViewMatchers.withId(android.R.id.button2))
                    .inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).check { view, error ->
                        if (error != null) throw error
                        if (android.os.Build.VERSION.SDK_INT == 37 && view.resources.displayMetrics.density == 2.625f &&
                            view.resources.configuration.fontScale == 1f) assertEquals(126, view.height)
                    }.perform(click())
                assertEquals(4, preferences.getInt("merhav", -1))
                assertEquals(language, preferences.getString("lng", null))
            }
        }
    }

    @Test fun offlineLocaleSelectionRefreshesSettingsAndHomeAndRestoresRegionDialog() {
        val previousLocales = androidx.core.app.LocaleManagerCompat.getApplicationLocales(context).toLanguageTags()
        instrumentation.uiAutomation.executeShellCommand("appops set ${context.packageName} SYSTEM_ALERT_WINDOW allow").use {
            android.os.ParcelFileDescriptor.AutoCloseInputStream(it).readBytes()
        }
        context.getSharedPreferences("Settings", 0).edit().putBoolean("Mapp", false)
            .putBoolean("privacy", true).putBoolean("oldU", true).putBoolean("WasLangChanged", false).commit()
        context.getSharedPreferences("rate_app", 0).edit().putBoolean("dontshowagain", true).commit()
        instrumentation.runOnMainSync { LocalHelper.setLocale(context, "en") }
        try {
            ActivityScenario.launch(HomePage::class.java).use { home ->
                for ((before, target) in listOf("en" to "he", "he" to "en")) {
                    home.onActivity { it.startActivity(android.content.Intent(it, SettingsPage::class.java)) }
                    compose.waitUntil(5_000) { withSettings { } }
                    open(R.id.language)
                    awaitDialog()
                    capture("settings-language-$before")
                    onView(withText(if (target == "he") "עברית" else "English")).perform(click())
                    compose.waitUntil(5_000) {
                        var language = ""
                        withSettings { language = it.resources.configuration.locales[0].language }
                        language == target || target == "he" && language == "iw"
                    }
                    assertEquals(target, context.getSharedPreferences("Settings", 0).getString("lng", null))
                    assertEquals(java.util.Locale.forLanguageTag(target).language,
                        context.applicationContext.resources.configuration.locales[0].language)
                    assertEquals(java.util.Locale.forLanguageTag(target).language, java.util.Locale.getDefault().language)
                    // A fresh themed dialog must work without a per-screen button-direction patch.
                    // Dictionary and other Tools dialogs use this same AppCompat button panel.
                    withSettings {
                        LocaleProbeDialog().show(it.supportFragmentManager, "locale-probe")
                    }
                    onView(androidx.test.espresso.matcher.ViewMatchers.withId(android.R.id.button1))
                        .inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).check { button, error ->
                        if (error != null) throw error
                        assertEquals(button.resources.configuration.layoutDirection,
                            (button.parent as android.view.View).layoutDirection)
                    }
                    onView(androidx.test.espresso.matcher.ViewMatchers.withId(android.R.id.button1))
                        .inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).perform(click())
                    context.getSharedPreferences("Settings", 0).edit().putInt("merhav", 0).commit()
                    open(R.id.mokedPick)
                    awaitDialog()
                    withSettings { it.recreate() }
                    awaitDialog()
                    capture("settings-region-$target")
                    onView(withText("דן")).perform(click())
                    assertEquals(4, context.getSharedPreferences("Settings", 0).getInt("merhav", -1))
                    assertTrue(context.getSharedPreferences("Settings", 0).getBoolean("pickedMoked", false))
                    // Language/region actions retain the drawer, unlike the old direct menu callback fixture.
                    compose.waitForShellDrawer(true)
                    instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
                    compose.waitForShellDrawer(false)
                    instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
                    compose.waitUntil(5_000) { !withSettings { } }
                    compose.waitUntil(5_000) {
                        var language = ""
                        home.onActivity { language = it.resources.configuration.locales[0].language }
                        language == target || target == "he" && language == "iw"
                    }
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

    private fun withSettings(action: (SettingsPage) -> Unit): Boolean {
        var found = false
        instrumentation.runOnMainSync {
            androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry.getInstance()
                .getActivitiesInStage(androidx.test.runner.lifecycle.Stage.RESUMED)
                .filterIsInstance<SettingsPage>().singleOrNull()?.let {
                    action(it)
                    found = true
                }
        }
        return found
    }

    class LocaleProbeDialog : androidx.fragment.app.DialogFragment() {
        override fun onCreateDialog(state: android.os.Bundle?): android.app.Dialog =
            androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle)
                .setMessage("Locale regression").setPositiveButton(android.R.string.ok, null).create()
    }

    private fun open(item: Int) {
        assertTrue(withSettings { })
        compose.selectShellDestination(item)
    }

    private fun awaitDialog() {
        compose.waitUntil(5_000) {
            var focused = false
            withSettings {
                focused = it.supportFragmentManager.fragments.filterIsInstance<SettingsChoiceDialog>()
                    .singleOrNull()?.dialog?.window?.decorView?.hasWindowFocus() == true
            }
            focused
        }
        withSettings {
            val dialog = it.supportFragmentManager.fragments.filterIsInstance<SettingsChoiceDialog>().single()
                .requireDialog() as androidx.appcompat.app.AlertDialog
            val bounds = android.graphics.Rect()
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).getGlobalVisibleRect(bounds)
            assertEquals(it.resources.configuration.layoutDirection == android.view.View.LAYOUT_DIRECTION_RTL,
                bounds.centerX() < it.resources.displayMetrics.widthPixels / 2)
        }
    }

    private fun capture(name: String) {
        if (InstrumentationRegistry.getArguments().getString("capture") != "true") return
        instrumentation.uiAutomation.waitForIdle(500, 5_000)
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        File(context.filesDir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
}
