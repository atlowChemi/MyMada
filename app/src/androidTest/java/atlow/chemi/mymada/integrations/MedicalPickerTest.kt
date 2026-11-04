package atlow.chemi.mymada.integrations

import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.TextView
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.R
import atlow.chemi.mymada.adapters.mCodeAdapt
import atlow.chemi.mymada.classes.mCodeKid
import org.junit.Assert.*
import org.junit.Test
import org.junit.Rule
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule

class MedicalPickerTest {
    @get:Rule val compose = createEmptyComposeRule()
    @Test fun realFormOpensCatalogAndRetainsSelectionAcrossSearch() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val language = InstrumentationRegistry.getArguments().getString("captureLanguage", "en")
        context.getSharedPreferences("Settings", 0).edit().putBoolean("realM", true)
            .putBoolean("pickedMoked", true).putBoolean("Mapp", false).commit()
        instrumentation.runOnMainSync { atlow.chemi.mymada.helpers.LocalHelper.setLocale(context, language) }
        androidx.test.core.app.ActivityScenario.launch(atlow.chemi.mymada.MokedPage::class.java).use { scenario ->
            scenario.onActivity {
                it.selectPage(0)
            }
            androidx.test.espresso.Espresso.closeSoftKeyboard()
            val codes = compose.onNodeWithTag("form-codes").performScrollTo().assertIsDisplayed()
            val fullBounds = codes.getUnclippedBoundsInRoot()
            val sendBounds = compose.onNodeWithTag("form-send").getUnclippedBoundsInRoot()
            assertTrue("Scrolled picker must be wholly above Send", fullBounds.bottom <= sendBounds.top)
            val clipped = codes.fetchSemanticsNode().boundsInRoot
            var density = 1f
            scenario.onActivity { density = it.resources.displayMetrics.density }
            assertEquals("Picker must not be clipped after scrolling", (fullBounds.bottom - fullBounds.top).value * density, clipped.height, 1f)
            val scrolled = instrumentation.uiAutomation.takeScreenshot()
            val evidence = java.io.File(context.getExternalFilesDir(null), "medical-picker").apply { mkdirs() }
            java.io.File(evidence, "scrolled-$language.png").outputStream().use {
                scrolled.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
            }
            scrolled.recycle()
            codes.performTouchInput { click() }
            scenario.onActivity {
                it.supportFragmentManager.fragments.filterIsInstance<atlow.chemi.mymada.fragments.MainActivity1>()
                    .single().childFragmentManager.executePendingTransactions()
            }
            compose.onNodeWithTag("medical-catalog").assertExists()
            instrumentation.uiAutomation.waitForIdle(500, 5000)
            val image = instrumentation.uiAutomation.takeScreenshot()
            val directory = java.io.File(context.getExternalFilesDir(null), "medical-picker").apply { mkdirs() }
            java.io.File(directory, "catalog-$language.png").outputStream().use {
                image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
            }
            image.recycle()
            compose.onNodeWithTag("medical-query").performTextReplacement("5000")
            compose.onNodeWithTag("medical-query").performImeAction()
            compose.onNodeWithTag("medical-code-5000").performScrollTo().performClick().assertIsOn()
            // Reverse selection order must still produce the native catalog order.
            compose.onNodeWithTag("medical-query").performTextReplacement("2000")
            compose.onNodeWithTag("medical-query").performImeAction()
            compose.onNodeWithTag("medical-code-2000").performScrollTo().performClick().assertIsOn()
            compose.onNodeWithTag("medical-query").performTextReplacement("5000")
            scenario.recreate()
            compose.onNodeWithTag("medical-query").assertTextContains("5000")
            compose.onNodeWithTag("medical-code-5000").assertIsOn()
            compose.onNodeWithTag("medical-query").performTextReplacement("לבבי")
            compose.onAllNodesWithTag("medical-code-5000").assertCountEquals(1)
            compose.onNodeWithTag("medical-done").performClick()
            scenario.recreate()
            compose.onNodeWithTag("form-codes").assertTextContains("2000, 5000")
            for ((index, value) in listOf(0 to "1234", 3 to "56", 6 to "First", 7 to "Last", 4 to "78", 5 to "90")) {
                compose.onNodeWithTag("form-field-$index").performScrollTo().performTextReplacement(value)
            }
            scenario.recreate()
            compose.onNodeWithTag("form-field-0").assertTextContains("1234")
            compose.onNodeWithTag("form-field-6").assertTextContains("First")
            compose.onNodeWithTag("form-send").performClick()
            instrumentation.waitForIdleSync()
            scenario.onActivity {
                val confirmation = it.supportFragmentManager.findFragmentByTag("dispatch-confirmation") as DispatchConfirmation
                assertEquals("אמב: 1234\nחולה נפגע: 56\nהתחייבות: 78\nסכום: 90\nקוד: 2000, 5000\nשם: First Last", confirmation.requireArguments().getString("body"))
            }
            compose.onNodeWithTag("dispatch-cancel").performClick()
            instrumentation.waitForIdleSync()
            compose.onNodeWithTag("form-field-4").performScrollTo().performTextReplacement("")
            compose.onNodeWithTag("form-field-5").performScrollTo().performTextReplacement("")
            compose.onNodeWithTag("form-send").performClick()
            instrumentation.waitForIdleSync()
            scenario.onActivity {
                val confirmation = it.supportFragmentManager.findFragmentByTag("dispatch-confirmation") as DispatchConfirmation
                assertEquals("אמב: 1234\nחולה נפגע: 56\nקוד: 2000, 5000\nשם: First Last", confirmation.requireArguments().getString("body"))
            }
            compose.onNodeWithTag("dispatch-cancel").performClick()
        }
    }
    @Test fun filteredSelectionAndRecycledHeadersKeepCatalogIdentity() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val context = android.view.ContextThemeWrapper(instrumentation.targetContext, R.style.MyMaterialTheme)
            val adapter = mCodeAdapt(context, listOf(
                mCodeKid("סביבה", "sep", "area", 0), mCodeKid("2000 גוף זר", "2000", "area", 1),
                mCodeKid("לבבי", "sep", "vasc", 2), mCodeKid("5000 לבבי", "5000", "vasc", 3)
            ))
            val parent = FrameLayout(context)
            adapter.filter("5000")
            val row = adapter.getView(0, null, parent)
            row.findViewById<CheckBox>(R.id.mcName).isChecked = true
            assertEquals("5000", adapter.getChecked())
            adapter.filter("")
            adapter.getView(1, row, parent)
            assertFalse(row.findViewById<CheckBox>(R.id.mcName).isChecked)
            assertEquals("5000", adapter.getChecked())
            adapter.filter("לבבי")
            assertEquals(2, adapter.count) // Match + cardiac alias must not duplicate a row.
            assertFalse(adapter.isEnabled(0))
            adapter.getView(0, row, parent)
            assertEquals("לבבי", row.findViewById<TextView>(R.id.mcSep).text.toString())
            adapter.getView(1, row, parent)
            assertTrue(row.findViewById<CheckBox>(R.id.mcName).isChecked)
            row.findViewById<CheckBox>(R.id.mcName).isChecked = false
            assertEquals("", adapter.getChecked())
        }
    }
}
