package atlow.chemi.mymada.integrations

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.viewpager.widget.ViewPager
import atlow.chemi.mymada.MokedPage
import atlow.chemi.mymada.R
import atlow.chemi.mymada.helpers.LocalHelper
import com.google.android.material.textfield.TextInputLayout
import org.junit.Assert.*
import org.junit.Test
import org.junit.Rule
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertIsSelected
import java.io.File

/** A blocking Instrumentation monitor intercepts approval: no SMS composer or transmission. */
class DispatchIntegrationTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    @Test fun hostTabsFollowPagerAndRetainSelectionAfterRecreation() {
        context.getSharedPreferences("Settings", 0).edit().putBoolean("realM", true)
            .putBoolean("pickedMoked", false).putBoolean("Mapp", false).commit()
        instrumentation.runOnMainSync { LocalHelper.setLocale(context, InstrumentationRegistry.getArguments().getString("captureLanguage", "en")) }
        ActivityScenario.launch(MokedPage::class.java).use { scenario ->
            compose.onNodeWithTag("dispatch-region-skip").performClick()
            compose.onNodeWithTag("dispatch-tab-1").assertIsSelected()
            compose.onNodeWithTag("dispatch-tab-0").performClick()
            scenario.onActivity { assertEquals(0, it.currentPage) }
            compose.waitForIdle()
            instrumentation.uiAutomation.waitForIdle(500, 5000)
            fun visibleInput(node: android.view.accessibility.AccessibilityNodeInfo?): Boolean {
                if (node == null) return false
                val bounds = android.graphics.Rect()
                node.getBoundsInScreen(bounds)
                if (node.className?.toString() == "android.widget.EditText" && node.isVisibleToUser && !bounds.isEmpty) return true
                return (0 until node.childCount).any { visibleInput(node.getChild(it)) }
            }
            assertTrue("Form inputs must expose usable screen bounds to accessibility", visibleInput(instrumentation.uiAutomation.rootInActiveWindow))
            scenario.onActivity { it.findViewById<ViewPager>(R.id.viewpager).setCurrentItem(if (atlow.chemi.mymada.SettingsPage.isRTL()) 0 else 2, false) }
            compose.onNodeWithTag("dispatch-tab-2").assertIsSelected()
            scenario.recreate()
            compose.onNodeWithTag("dispatch-tab-2").assertIsSelected()
            compose.onNodeWithTag("dispatch-region-skip").assertDoesNotExist()
        }
    }
    @Test fun everyRecipientKeepsLiteralBodyOutsideUri() {
        val body = "  שם & #? +%\nSecond line  "
        DispatchContract.phones.forEachIndexed { region, phone ->
            val intent = DispatchConfirmation.intent(region, body)
            assertEquals(Intent.ACTION_VIEW, intent.action)
            assertEquals("sms", intent.data!!.scheme)
            assertEquals(phone, intent.data!!.schemeSpecificPart)
            assertNull(intent.data!!.fragment)
            assertEquals(body, intent.getStringExtra("sms_body"))
        }
    }
    @Test fun customValidationConfirmationSnapshotRotationAndInterceptedApproval() {
        val preferences = context.getSharedPreferences("Settings", 0)
        preferences.edit().putBoolean("realM", true).putBoolean("pickedMoked", true)
            .putBoolean("Mapp", false).putInt("merhav", 11).commit()
        val language = InstrumentationRegistry.getArguments().getString("captureLanguage", "en")
        instrumentation.runOnMainSync { LocalHelper.setLocale(context, language) }
        val filter = IntentFilter(Intent.ACTION_VIEW).apply { addDataScheme("sms") }
        val monitor = instrumentation.addMonitor(filter, Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null), true)
        try {
            ActivityScenario.launch(MokedPage::class.java).use { scenario ->
                scenario.onActivity { it.selectPage(2) }
                compose.waitForIdle()
                closeSoftKeyboard()
                capture("custom", language)
                compose.onNodeWithTag("custom-send").performClick()
                val body = "  Fixture & #? +%\nשלום  "
                compose.onNodeWithTag("custom-error").assertExists()
                compose.onNodeWithTag("custom-body").performTextReplacement(body)
                closeSoftKeyboard()
                compose.onNodeWithTag("custom-send").performClick()
                scenario.onActivity { it.supportFragmentManager.executePendingTransactions() }
                assertEquals(0, monitor.hits)
                preferences.edit().putInt("merhav", 0).commit()
                scenario.recreate()
                scenario.onActivity {
                    val fragment = it.supportFragmentManager.findFragmentByTag("dispatch-confirmation") as DispatchConfirmation
                    assertEquals(11, fragment.requireArguments().getInt("region"))
                    assertEquals(body, fragment.requireArguments().getString("body"))
                }
                compose.onNodeWithTag("dispatch-summary").assertTextContains("מוקד 4", substring = true).assertTextContains(body, substring = true)
                assertTrue(compose.onNodeWithTag("dispatch-confirm").fetchSemanticsNode().boundsInRoot.left >
                    compose.onNodeWithTag("dispatch-cancel").fetchSemanticsNode().boundsInRoot.left)
                capture("confirmation", language)
                compose.onNodeWithTag("dispatch-cancel").performClick()
                scenario.onActivity { it.supportFragmentManager.executePendingTransactions() }
                assertEquals(0, monitor.hits)
                compose.onNodeWithTag("custom-body").assertTextEquals(body)
                compose.onNodeWithTag("custom-send").performClick()
                scenario.onActivity { it.supportFragmentManager.executePendingTransactions() }
                compose.onNodeWithTag("dispatch-confirm").performClick()
                assertEquals(1, monitor.hits)
            }
        } finally { instrumentation.removeMonitor(monitor) }
    }
    private fun capture(name: String, language: String) {
        instrumentation.waitForIdleSync()
        instrumentation.uiAutomation.waitForIdle(500, 5000)
        val image = instrumentation.uiAutomation.takeScreenshot()
        if (name == "custom" && android.os.Build.VERSION.SDK_INT >= 35)
            assertEquals(context.getColor(R.color.ColorPrimary), image.getPixel(image.width / 2, 2))
        val directory = File(context.getExternalFilesDir(null), "dispatch").apply { mkdirs() }
        File(directory, "$name-$language.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
        image.recycle()
    }
}
