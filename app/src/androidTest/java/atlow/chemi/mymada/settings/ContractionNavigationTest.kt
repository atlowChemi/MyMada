package atlow.chemi.mymada.settings

import android.app.Activity
import android.graphics.Bitmap
import android.view.View
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import atlow.chemi.mymada.HomePage
import atlow.chemi.mymada.R
import atlow.chemi.mymada.SettingsPage
import atlow.chemi.mymada.UtillitiesAndTools
import atlow.chemi.mymada.classes.Contraction
import atlow.chemi.mymada.helpers.LocalHelper
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.Date
import java.util.Locale

/** Exercise the shared locale boundary through the real Settings → Home → Tools route. */
class ContractionNavigationTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val preferences = context.getSharedPreferences("Settings", 0)

    @Test fun settingsLocaleRoundTripPreservesNativeAndRnRecovery() {
        instrumentation.uiAutomation.executeShellCommand("appops set ${context.packageName} SYSTEM_ALERT_WINDOW allow").use {
            android.os.ParcelFileDescriptor.AutoCloseInputStream(it).readBytes()
        }
        preferences.edit().putBoolean("Mapp", false).putBoolean("privacy", true)
            .putBoolean("oldU", true).putBoolean("WasLangChanged", false)
            .putStringSet("Tools", setOf("contrac")).commit()
        context.getSharedPreferences("rate_app", 0).edit().putBoolean("dontshowagain", true).commit()
        instrumentation.runOnMainSync { LocalHelper.setLocale(context, "en") }
        try {
            ActivityScenario.launch(HomePage::class.java).use {
                for ((language, rn) in listOf("he" to true, "en" to false)) {
                    val now = System.currentTimeMillis()
                    val raw = if (rn) """[{"number":1,"start":${now - 180000},"end":${now - 150000}},
                        {"number":2,"start":${now - 100000},"end":${now - 70000}},{"number":3,"start":${now - 10000}}]"""
                    else Gson().toJson(listOf(Contraction(1, Date(now - 180000), Date(now - 150000), null),
                        Contraction(2, Date(now - 100000), Date(now - 70000), Date(now - 150000))))
                    preferences.edit().remove(atlow.chemi.mymada.contractions.ContractionStore.KEY)
                        .putString("contractList", raw).putLong("contractLong", now - 70000)
                        .remove("migration.original.contractList").commit()
                    var settings = ""
                    await<HomePage>()
                    resumed<HomePage> { settings = it.getString(R.string.action_settings) }
                    compose.onNodeWithContentDescription(settings).performClick()
                    await<SettingsPage>()
                    resumed<SettingsPage> {
                        it.onNavigationItemSelected(it.findViewById<NavigationView>(R.id.navigation).menu.findItem(R.id.language))
                    }
                    compose.waitUntil(5_000) {
                        var focused = false
                        resumed<SettingsPage> {
                            focused = it.supportFragmentManager.fragments.filterIsInstance<SettingsChoiceDialog>()
                                .singleOrNull()?.dialog?.window?.decorView?.hasWindowFocus() == true
                        }
                        focused
                    }
                    onView(withText(if (rn) "עברית" else "English")).perform(click())
                    await<SettingsPage>(language)
                    Espresso.pressBack()
                    await<HomePage>(language)
                    var tools = ""
                    resumed<HomePage> { tools = it.getString(R.string.title_activity_utills_and_tools) }
                    compose.onNodeWithText(tools).performClick()
                    await<UtillitiesAndTools>(language, focus = false)
                    var ok = ""
                    var start = ""
                    var end = ""
                    resumed<UtillitiesAndTools> {
                        ok = it.getString(R.string.ok)
                        start = it.getString(R.string.startContract)
                        end = it.getString(R.string.endContract)
                    }
                    compose.onNodeWithText(ok).performClick()
                    await<UtillitiesAndTools>(language)
                    val toggle = compose.onNodeWithTag("contraction-toggle")
                    toggle.assertTextContains(if (rn) end else start)
                    val layouts = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
                    toggle.performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult) { it(layouts) }
                    assertEquals(if (rn) androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr,
                        layouts.single().layoutInput.layoutDirection)
                    assertEquals(1f, layouts.single().layoutInput.density.fontScale, .01f)
                    capture("recovered-$language")
                    if (!rn) toggle.performClick()
                    toggle.assertTextContains(end)
                    resumed<UtillitiesAndTools> { it.recreate() }
                    await<UtillitiesAndTools>(language)
                    toggle.assertTextContains(end)
                    toggle.performClick()
                    toggle.assertTextContains(start)
                    capture("completed-$language")
                    await<UtillitiesAndTools>(language)
                    Espresso.pressBack()
                    await<HomePage>(language)
                    compose.onNodeWithText(tools).performClick()
                    await<UtillitiesAndTools>(language, focus = false)
                    compose.onNodeWithText(ok).performClick()
                    toggle.assertTextContains(start)
                    await<UtillitiesAndTools>(language)
                    Espresso.pressBack()
                    await<HomePage>(language)
                    val saved = atlow.chemi.mymada.contractions.ContractionStore(preferences).read()
                    assertEquals(rn, saved.reactNative)
                    assertEquals(listOf(1, 2, 3), saved.entries.map { it.number })
                    // Native Gson date strings have second precision; RN epochs retain milliseconds.
                    assertEquals(if (rn) now - 100000 else (now - 100000) / 1000 * 1000, saved.entries.last().previous)
                    assertEquals(raw, preferences.getString("migration.original.contractList", null))
                }
            }
        } finally {
            instrumentation.runOnMainSync { LocalHelper.setLocale(context, "en") }
        }
    }

    private inline fun <reified T : Activity> resumed(crossinline action: (T) -> Unit): Boolean {
        var found = false
        instrumentation.runOnMainSync {
            ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
                .filterIsInstance<T>().singleOrNull()?.let { action(it); found = true }
        }
        return found
    }

    private inline fun <reified T : Activity> await(language: String? = null, focus: Boolean = true) {
        compose.waitUntil(10_000) {
            var ready = false
            resumed<T> {
                val config = it.resources.configuration
                ready = (!focus || it.hasWindowFocus()) && (language == null || config.locales[0].language == Locale.forLanguageTag(language).language)
                if (ready && language != null) {
                    assertEquals(Locale.forLanguageTag(language).language, context.applicationContext.resources.configuration.locales[0].language)
                    assertEquals(if (language == "he") View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR, config.layoutDirection)
                    assertEquals(1f, config.fontScale, .01f)
                }
            }
            ready
        }
    }

    private fun capture(name: String) {
        if (InstrumentationRegistry.getArguments().getString("capture") != "true") return
        compose.waitForIdle()
        instrumentation.uiAutomation.waitForIdle(500, 5_000)
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        File(context.filesDir, "contractions-navigation-$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
}
