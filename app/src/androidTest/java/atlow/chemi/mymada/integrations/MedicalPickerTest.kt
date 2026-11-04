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
                it.findViewById<androidx.viewpager.widget.ViewPager>(R.id.viewpager).setCurrentItem(0, false)
                it.findViewById<android.widget.EditText>(R.id.M1button2).performClick()
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
            compose.onNodeWithTag("medical-code-2000").performScrollTo().performClick()
            compose.onNodeWithTag("medical-query").performTextReplacement("5000")
            compose.onNodeWithTag("medical-code-5000").performClick()
            scenario.recreate()
            compose.onNodeWithTag("medical-query").assertTextContains("5000")
            compose.onNodeWithTag("medical-code-5000").assertIsOn()
            compose.onNodeWithTag("medical-query").performTextReplacement("לבבי")
            compose.onAllNodesWithTag("medical-code-5000").assertCountEquals(1)
            compose.onNodeWithTag("medical-done").performClick()
            scenario.recreate()
            scenario.onActivity {
                assertEquals("2000, 5000", it.findViewById<android.widget.EditText>(R.id.M1button2).text.toString())
                for ((id, value) in listOf(R.id.M1txt1E to "1234", R.id.M1txt2E to "56", R.id.M1txt4E to "First", R.id.M1txt5 to "Last", R.id.M1txt3 to "78", R.id.M1txt6E to "90")) {
                    it.findViewById<android.widget.EditText>(id).setText(value)
                }
                it.findViewById<android.widget.Button>(R.id.M1btnN).performClick()
            }
            instrumentation.waitForIdleSync()
            scenario.onActivity {
                val confirmation = it.supportFragmentManager.findFragmentByTag("dispatch-confirmation") as DispatchConfirmation
                assertEquals("אמב' 1234\nחולה נפגע 56\nהתחייבות 78\nסכום 90\nקוד 2000, 5000\nשם: First Last", confirmation.requireArguments().getString("body"))
            }
            compose.onNodeWithTag("dispatch-cancel").performClick()
            instrumentation.waitForIdleSync()
            scenario.onActivity {
                it.findViewById<android.widget.EditText>(R.id.M1txt3).setText("")
                it.findViewById<android.widget.EditText>(R.id.M1txt6E).setText("")
                it.findViewById<android.widget.Button>(R.id.M1btnN).performClick()
            }
            instrumentation.waitForIdleSync()
            scenario.onActivity {
                val confirmation = it.supportFragmentManager.findFragmentByTag("dispatch-confirmation") as DispatchConfirmation
                assertEquals("אמב' 1234\nחולה נפגע 56\nקוד 2000, 5000\nשם: First Last", confirmation.requireArguments().getString("body"))
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
