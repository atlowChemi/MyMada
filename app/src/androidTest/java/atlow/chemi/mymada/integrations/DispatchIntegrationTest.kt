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
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.GeneralSwipeAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Swipe
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
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
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import java.io.File

/** A blocking Instrumentation monitor intercepts approval: no SMS composer or transmission. */
class DispatchIntegrationTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    @Test fun previewUsesEachParagraphDirectionWithoutChangingPayload() {
        val language = InstrumentationRegistry.getArguments().getString("captureLanguage", "en")
        context.getSharedPreferences("Settings", 0).edit().putBoolean("pickedMoked", true)
            .putBoolean("realM", true).putBoolean("Mapp", false).commit()
        instrumentation.runOnMainSync { LocalHelper.setLocale(context, language) }
        val team = TeamContract.body("123", 0, listOf(atlow.chemi.mymada.classes.Volunteer("A", 2),
            atlow.chemi.mymada.classes.Volunteer("B", 0), atlow.chemi.mymada.classes.Volunteer("C", 0),
            atlow.chemi.mymada.classes.Volunteer("D", 0)))
        val form = FormContract.body(listOf("123", "", "Fixture Street 1", "456", "789", "100", "Fixture", "Patient", "2000"))
        val bodies = listOf("form" to form,
            "team" to team, "custom" to "01 Local visual fixture\nשלום Fixture\n02 Final line")
        val monitor = instrumentation.addMonitor(IntentFilter(Intent.ACTION_VIEW).apply { addDataScheme("sms") },
            Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null), true)
        try {
            ActivityScenario.launch(MokedPage::class.java).use { scenario ->
                for ((kind, body) in bodies) {
                    scenario.onActivity { activity ->
                        DispatchConfirmation().apply {
                            arguments = android.os.Bundle().apply { putString("body", body); putInt("region", 0) }
                        }.showNow(activity.supportFragmentManager, "dispatch-confirmation")
                    }
                    capture("paragraph-$kind", language)
                    val results = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
                    compose.onNodeWithTag("dispatch-summary").performSemanticsAction(
                        androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult) { it(results) }
                    val layout = results.single()
                    assertFalse("Preview text must not overflow its layout", layout.hasVisualOverflow)
                    val actions = listOf("dispatch-cancel", "dispatch-confirm").map {
                        val node = compose.onNodeWithTag(it)
                        node.fetchSemanticsNode().boundsInRoot to node.getUnclippedBoundsInRoot()
                    }
                    scenario.onActivity { activity ->
                        val dialog = activity.supportFragmentManager.findFragmentByTag("dispatch-confirmation")!!
                        val configuration = dialog.resources.configuration
                        assertEquals("Require effective font scale, not just a settings-database write",
                            InstrumentationRegistry.getArguments().getString("captureFontScale", "1.0").toFloat(),
                            configuration.fontScale, 0.01f)
                        File(context.getExternalFilesDir(null), "dispatch").apply { mkdirs() }
                            .resolve("paragraph-$kind-config-$language.txt").writeText(configuration.toString())
                        assertEquals("Paragraph alignment must use the dialog content width, not a short intrinsic text box",
                            dialog.requireView().width - 48 * activity.resources.displayMetrics.density,
                            layout.size.width.toFloat(), 1f)
                        val frame = android.graphics.Rect()
                        dialog.requireView().getWindowVisibleDisplayFrame(frame)
                        val location = IntArray(2)
                        dialog.requireView().getLocationOnScreen(location)
                        for ((bounds, unclipped) in actions) {
                            assertTrue("Complete action must be visible: $bounds / $unclipped / $frame at ${location[1]}", bounds.top + location[1] >= frame.top &&
                                bounds.bottom + location[1] <= frame.bottom &&
                                bounds.left + location[0] >= frame.left && bounds.right + location[0] <= frame.right &&
                                bounds.width >= (unclipped.right - unclipped.left).value * activity.resources.displayMetrics.density - 1 &&
                                bounds.height >= (unclipped.bottom - unclipped.top).value * activity.resources.displayMetrics.density - 1 && bounds.height > 0)
                        }
                    }
                    val text = layout.layoutInput.text.text
                    assertTrue(text.contains("\n\n$body\n\n"))
                    val localized = LocalHelper.onAttach(context)
                    assertTrue(text.endsWith("${body.length} ${localized.getString(R.string.smsCountChar)} " +
                        "${1 + body.length / 70} ${localized.getString(R.string.smsCountMsg)}"))
                    var offset = text.indexOf(body)
                    for (paragraph in body.lines()) {
                        val rtl = paragraph.first() in '\u0590'..'\u05ff'
                        val expected = if (rtl) androidx.compose.ui.text.style.ResolvedTextDirection.Rtl
                            else androidx.compose.ui.text.style.ResolvedTextDirection.Ltr
                        assertEquals("$kind paragraph $paragraph", expected, layout.getParagraphDirection(offset))
                        val line = layout.getLineForOffset(offset)
                        if (rtl) assertEquals(layout.size.width.toFloat(), layout.getLineRight(line), 1f)
                        else assertEquals(0f, layout.getLineLeft(line), 1f)
                        offset += paragraph.length + 1
                    }
                    if (kind == "team") {
                        val role = text.indexOf("פראמדיק")
                        val name = text.indexOf("A", role)
                        assertTrue("Latin name must remain to the left of its Hebrew role",
                            layout.getBoundingBox(name).center.x < layout.getBoundingBox(role).center.x)
                    }
                    scenario.recreate()
                    scenario.onActivity { activity ->
                        val dialog = activity.supportFragmentManager.findFragmentByTag("dispatch-confirmation")!!
                        assertEquals(body, dialog.requireArguments().getString("body"))
                    }
                    assertEquals(body, DispatchConfirmation.intent(0, body).getStringExtra("sms_body"))
                    compose.onNodeWithTag("dispatch-cancel").performClick()
                    assertEquals(0, monitor.hits)
                }
            }
        } finally { instrumentation.removeMonitor(monitor) }
    }
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
            val automation = instrumentation.uiAutomation
            val oldFlags = automation.serviceInfo.flags
            try {
                // API24 can retain a stale active-window lookup after earlier dialog tests.
                // Query the focused application window explicitly; keep the real bounds check.
                automation.serviceInfo = automation.serviceInfo.apply {
                    flags = flags or android.accessibilityservice.AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
                }
                val window = automation.windows.single {
                    it.type == android.view.accessibility.AccessibilityWindowInfo.TYPE_APPLICATION && it.isActive && it.isFocused
                }
                val root = window.root
                assertEquals(context.packageName, root?.packageName?.toString())
                assertTrue("Form inputs must expose usable screen bounds to accessibility", visibleInput(root))
            } finally {
                automation.serviceInfo = automation.serviceInfo.apply { flags = oldFlags }
            }
            val rtl = atlow.chemi.mymada.SettingsPage.isRTL()
            fun swipe(forward: Boolean) {
                // Each page can focus an input and reopen the IME. Pager bounds extend
                // behind it on API24: otherwise injected drags hit the keyboard.
                closeSoftKeyboard()
                // Edge-origin swipes invoke system Back with API37 gesture navigation.
                val startOffset = if (rtl == forward) -.25f else .25f
                // Center crosses horizontally scrollable form inputs, which own
                // the drag and open the IME. Swipe through the bottom action area.
                onView(withId(R.id.viewpager)).perform(GeneralSwipeAction(Swipe.FAST,
                    GeneralLocation.translate(GeneralLocation.CENTER, startOffset, .4f),
                    GeneralLocation.translate(GeneralLocation.CENTER, -startOffset, .4f), Press.FINGER))
                instrumentation.uiAutomation.waitForIdle(500, 5000)
            }
            swipe(true)
            compose.onNodeWithTag("dispatch-tab-1").assertIsSelected()
            swipe(true)
            compose.onNodeWithTag("dispatch-tab-2").assertIsSelected()
            swipe(false)
            compose.onNodeWithTag("dispatch-tab-1").assertIsSelected()
            swipe(true)
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
