package atlow.chemi.mymada.settings

import android.graphics.Bitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.R
import atlow.chemi.mymada.SettingsPage
import atlow.chemi.mymada.helpers.LocalHelper
import atlow.chemi.mymada.selectShellDestination
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

class ToolSettingsTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    @Test fun bilingualSelectionLastToolGuardAndRecreation() {
        val previousLocales = androidx.core.app.LocaleManagerCompat.getApplicationLocales(context).toLanguageTags()
        val preferences = context.getSharedPreferences("Settings", 0)
        instrumentation.uiAutomation.executeShellCommand("appops set ${context.packageName} SYSTEM_ALERT_WINDOW allow").use {
            android.os.ParcelFileDescriptor.AutoCloseInputStream(it).readBytes()
        }
        try {
            for (language in listOf("en", "he")) {
                instrumentation.runOnMainSync { LocalHelper.setLocale(context, language) }
                preferences.edit().putBoolean("Mapp", false).putStringSet("Tools", toolOptions.map { it.first }.toSet() + "futureTool").commit()
                ActivityScenario.launch(SettingsPage::class.java).use { scenario ->
                    val labels = mutableMapOf<String, String>()
                    var warning = ""
                    scenario.onActivity {
                        toolOptions.forEach { (key, label) -> labels[key] = it.getString(label) }
                        warning = it.getString(R.string.NoPicked)
                    }
                    compose.selectShellDestination(R.id.tools)
                    compose.onNodeWithText(labels.getValue("contrac")).assertIsOn().assertIsDisplayed()
                    if (language == "en") onView(withText(labels.getValue("contrac"))).check { view, error ->
                        if (error != null) throw error
                        if (android.os.Build.VERSION.SDK_INT == 37 && view.resources.displayMetrics.density == 2.625f &&
                            view.resources.configuration.fontScale == 1f) assertEquals(61, view.height)
                    }
                    if (InstrumentationRegistry.getArguments().getString("capture") == "true") {
                        instrumentation.uiAutomation.waitForIdle(500, 5_000)
                        val bitmap = instrumentation.uiAutomation.takeScreenshot()
                        File(context.filesDir, "settings-tools-$language.png").outputStream().use {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                        }
                        bitmap.recycle()
                    }
                    for ((key, _) in toolOptions) {
                        val node = compose.onNodeWithText(labels.getValue(key)).performScrollTo().assertIsDisplayed().assertIsOn()
                        if (key != "metro") node.performClick().assertIsOff()
                    }
                    val metro = compose.onNodeWithText(labels.getValue("metro")).performScrollTo()
                    metro.performClick().assertIsOn()
                    onView(withText(warning)).check(matches(isDisplayed()))
                    assertEquals(setOf("metro", "futureTool"), preferences.getStringSet("Tools", null))
                    compose.onNodeWithText(labels.getValue("oxy")).performScrollTo().performClick().assertIsOn()
                    scenario.recreate()
                    compose.onNodeWithText(labels.getValue("oxy")).assertIsDisplayed().assertIsOn()
                    assertEquals(setOf("metro", "oxy", "futureTool"), preferences.getStringSet("Tools", null))
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
}
