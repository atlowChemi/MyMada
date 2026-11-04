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

class MedicalPickerTest {
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
            }
            androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.userListView))
                .check { view, error ->
                    if (error != null) throw error
                    assertEquals(161, (view as android.widget.ListView).adapter.count)
                }
            instrumentation.uiAutomation.waitForIdle(500, 5000)
            val image = instrumentation.uiAutomation.takeScreenshot()
            val directory = java.io.File(context.getExternalFilesDir(null), "medical-picker").apply { mkdirs() }
            java.io.File(directory, "catalog-$language.png").outputStream().use {
                image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
            }
            image.recycle()
            androidx.test.espresso.Espresso.onView(org.hamcrest.Matchers.allOf(androidx.test.espresso.matcher.ViewMatchers.withText("2000 גוף זר באוזניים"), androidx.test.espresso.matcher.ViewMatchers.withId(R.id.mcName)))
                .perform(androidx.test.espresso.action.ViewActions.click())
            androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.userSearch))
                .perform(androidx.test.espresso.action.ViewActions.replaceText("5000"))
            androidx.test.espresso.Espresso.onView(org.hamcrest.Matchers.allOf(androidx.test.espresso.matcher.ViewMatchers.withText("5000 בצקת ריאות / החמרה באי ספיקת לב"), androidx.test.espresso.matcher.ViewMatchers.withId(R.id.mcName)))
                .perform(androidx.test.espresso.action.ViewActions.click())
            androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withText(R.string.ok))
                .perform(androidx.test.espresso.action.ViewActions.click())
            scenario.onActivity {
                assertEquals("2000, 5000", it.findViewById<android.widget.EditText>(R.id.M1button2).text.toString())
            }
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
