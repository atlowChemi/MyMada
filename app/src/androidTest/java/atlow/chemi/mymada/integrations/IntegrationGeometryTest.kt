package atlow.chemi.mymada.integrations

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.MokedPage
import atlow.chemi.mymada.codeC
import atlow.chemi.mymada.codeRequest
import atlow.chemi.mymada.helpers.LocalHelper
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class IntegrationGeometryTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun nativeMetricsAndScaledControlsRemainReachable() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val language = InstrumentationRegistry.getArguments().getString("captureLanguage", "en")
        instrumentation.runOnMainSync { LocalHelper.setLocale(context, language) }
        context.getSharedPreferences("Settings", 0).edit().putBoolean("Mapp", false)
            .putBoolean("realM", true).putBoolean("pickedMoked", true).commit()
        ActivityScenario.launch(codeC::class.java).use { scenario ->
            var normal = false
            scenario.onActivity { normal = it.resources.configuration.fontScale <= 1.01f }
            if (normal) compose.onNodeWithTag("activation-digit-0").assertHeightIsEqualTo(41.dp)
            compose.onNodeWithTag("activation-digit-3").performTextReplacement("7")
            closeSoftKeyboard()
            compose.onNodeWithTag("activation-submit").assertIsDisplayed()
        }
        ActivityScenario.launch(codeRequest::class.java).use { scenario ->
            compose.onNodeWithTag("activation-request-confirm").assertIsDisplayed()
            scenario.onActivity {
                val view = it.supportFragmentManager.findFragmentByTag("request-disclaimer")!!.requireView()
                assertTrue("Alert content must use native alert width", view.width > it.resources.displayMetrics.widthPixels * .85f)
            }
            compose.onNodeWithTag("activation-request-confirm").performClick()
            compose.onNodeWithTag("activation-request-2").performScrollTo().performTextReplacement("Local fixture")
            closeSoftKeyboard()
            compose.onNodeWithTag("activation-request-submit").assertIsDisplayed()
        }
        ActivityScenario.launch(MokedPage::class.java).use {
            compose.onNodeWithTag("dispatch-tab-0").performClick()
            compose.onNodeWithTag("form-field-7").performScrollTo().performTextReplacement("Fixture")
            closeSoftKeyboard()
            compose.onNodeWithTag("form-send").assertIsDisplayed()
            compose.onNodeWithTag("dispatch-tab-1").performClick()
            compose.onNodeWithTag("team-ambulance").performTextReplacement("1234")
            closeSoftKeyboard()
            compose.onNodeWithTag("team-add").assertIsDisplayed()
            compose.onNodeWithTag("team-send").assertIsDisplayed()
        }
    }
}
