package atlow.chemi.mymada.oxygen

import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.R
import atlow.chemi.mymada.UtillitiesAndTools
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputLayout
import org.junit.Assert.*
import org.junit.Test
import org.junit.Rule
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import java.io.File
import android.graphics.Bitmap
import atlow.chemi.mymada.helpers.LocalHelper

/** Offline emulator only: the application initializes production SDKs. */
class OxygenTest {
    @get:Rule val compose = createEmptyComposeRule()
    @Test fun conversionBoundariesAndInvalidInput() {
        assertEquals(24, OxygenCalculation.minutes("100", false))
        assertEquals(200, OxygenCalculation.minutes("100", true))
        assertEquals(198, OxygenCalculation.minutes(OxygenCalculation.atmFromPsi("1499")!!, true))
        assertEquals("99", OxygenCalculation.atmFromPsi("1499"))
        assertEquals("100", OxygenCalculation.atmFromPsi("1500"))
        assertEquals("0", OxygenCalculation.atmFromPsi("14"))
        assertEquals("1", OxygenCalculation.atmFromPsi("15"))
        assertEquals("999", OxygenCalculation.atmFromPsi("14985"))
        assertNull(OxygenCalculation.atmFromPsi("14986"))
        assertNull(OxygenCalculation.psiFromAtm("1000"))
        assertEquals("14985", OxygenCalculation.psiFromAtm("999"))
        assertEquals(239, OxygenCalculation.minutes("999", false))
        assertEquals(1998, OxygenCalculation.minutes("999", true))
        assertEquals(0, OxygenCalculation.minutes("", true))
        assertEquals(0, OxygenCalculation.minutes("0", false))
        assertNull(OxygenCalculation.atmFromPsi(""))
        listOf("-1", "1.5", "١", "1000", "x", "999999999999999999999").forEach {
            assertFalse(OxygenCalculation.validInput(it, OxygenCalculation.MAX_ATM))
        }
        assertTrue(OxygenCalculation.validInput("999", OxygenCalculation.MAX_ATM))
        assertTrue(OxygenCalculation.validInput("14985", OxygenCalculation.MAX_PSI))
        assertFalse(OxygenCalculation.validInput("14986", OxygenCalculation.MAX_PSI))
        assertTrue(OxygenCalculation.validInput("000", OxygenCalculation.MAX_ATM))
    }

    @Test fun editsTankAndRecreation() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val arguments = InstrumentationRegistry.getArguments()
        val language = arguments.getString("language") ?: "en"
        instrumentation.runOnMainSync { LocalHelper.setLocale(context, language) }
        context.getSharedPreferences("Settings", 0).edit().putBoolean("Mapp", false)
            .putBoolean("WasLangChanged", false).putStringSet("Tools", setOf("metro", "oxy")).commit()
        ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
            scenario.onActivity {
                val menu = it.findViewById<NavigationView>(R.id.navigation).menu
                it.onNavigationItemSelected(menu.findItem(R.id.oxygenCalc))
                it.supportFragmentManager.executePendingTransactions()
            }
            compose.waitForIdle()
            instrumentation.uiAutomation.takeScreenshot().let { bitmap ->
                File(context.getExternalFilesDir(null), "oxygen-${language}-default.png").outputStream().use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
                bitmap.recycle()
            }
            scenario.onActivity {
                assertEquals("24", it.findViewById<TextView>(R.id.txtOxyMin).text.toString())
                it.findViewById<Spinner>(R.id.OxyTypeS).setSelection(1)
                it.findViewById<EditText>(R.id.PSI).apply { requestFocus(); setText("1499") }
            }
            compose.waitForIdle()
            scenario.onActivity {
                assertEquals("198", it.findViewById<TextView>(R.id.txtOxyMin).text.toString())
                assertEquals("99", it.findViewById<EditText>(R.id.ATM).text.toString())
            }
            scenario.recreate()
            compose.waitForIdle()
            scenario.onActivity {
                assertEquals("1499", it.findViewById<EditText>(R.id.PSI).text.toString())
                assertEquals("198", it.findViewById<TextView>(R.id.txtOxyMin).text.toString())
                assertEquals(1, it.findViewById<Spinner>(R.id.OxyTypeS).selectedItemPosition)
                it.findViewById<EditText>(R.id.ATM).apply { requestFocus(); setText("") }
            }
            compose.waitForIdle()
            scenario.onActivity {
                assertEquals("0", it.findViewById<TextView>(R.id.txtOxyMin).text.toString())
                it.findViewById<EditText>(R.id.ATM).setText("999")
            }
            compose.waitForIdle()
            scenario.onActivity {
                assertEquals("14985", it.findViewById<EditText>(R.id.PSI).text.toString())
                assertEquals("1998", it.findViewById<TextView>(R.id.txtOxyMin).text.toString())
                it.findViewById<EditText>(R.id.PSI).apply { requestFocus(); text!!.replace(0, text!!.length, "99999") }
            }
            compose.waitForIdle()
            scenario.onActivity {
                assertEquals("999", it.findViewById<EditText>(R.id.ATM).text.toString())
                assertEquals("14985", it.findViewById<EditText>(R.id.PSI).text.toString())
                assertNotNull((it.findViewById<EditText>(R.id.PSI).parent.parent as TextInputLayout).error)
                assertEquals("1998", it.findViewById<TextView>(R.id.txtOxyMin).text.toString())
                it.findViewById<EditText>(R.id.PSI).setText("")
            }
            compose.waitForIdle()
            scenario.onActivity {
                assertEquals("0", it.findViewById<TextView>(R.id.txtOxyMin).text.toString())
                assertEquals("", it.findViewById<EditText>(R.id.ATM).text.toString())
                assertEquals("", it.findViewById<EditText>(R.id.PSI).text.toString())
            }
        }
    }

    @Test fun clearingBothDirectionsAndRotationKeepTheSameInputContract() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        instrumentation.runOnMainSync { LocalHelper.setLocale(context, "en") }
        context.getSharedPreferences("Settings", 0).edit().putBoolean("Mapp", false)
            .putBoolean("WasLangChanged", false).putStringSet("Tools", setOf("metro", "oxy")).commit()
        ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
            scenario.onActivity {
                it.onNavigationItemSelected(it.findViewById<NavigationView>(R.id.navigation).menu.findItem(R.id.oxygenCalc))
                it.supportFragmentManager.executePendingTransactions()
            }
            compose.waitForIdle()
            fun check(atm: String, psi: String, minutes: String) {
                scenario.onActivity {
                    assertEquals(atm, it.findViewById<EditText>(R.id.ATM).text.toString())
                    assertEquals(psi, it.findViewById<EditText>(R.id.PSI).text.toString())
                    assertEquals(minutes, it.findViewById<TextView>(R.id.txtOxyMin).text.toString())
                    assertEquals(1, it.findViewById<Spinner>(R.id.OxyTypeS).selectedItemPosition)
                }
            }
            fun edit(id: Int, value: String) {
                scenario.onActivity { it.findViewById<EditText>(id).apply { requestFocus(); text!!.replace(0, text!!.length, value) } }
                compose.waitForIdle()
            }
            fun rotate(landscape: Boolean) {
                val expected = if (landscape) Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT
                scenario.onActivity { it.requestedOrientation = if (landscape) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
                compose.waitUntil(5_000) {
                    var ready = false
                    scenario.onActivity { ready = it.resources.configuration.orientation == expected }
                    ready
                }
                compose.waitForIdle()
            }
            scenario.onActivity { it.findViewById<Spinner>(R.id.OxyTypeS).setSelection(1) }
            compose.waitForIdle()
            for (landscape in listOf(false, true)) {
                rotate(landscape)
                edit(R.id.ATM, "100")
                check("100", "1500", "200")
                edit(R.id.ATM, "")
                check("", "", "0")
                rotate(!landscape)
                check("", "", "0")
                rotate(landscape)
                edit(R.id.PSI, "1499")
                check("99", "1499", "198")
                rotate(!landscape)
                check("99", "1499", "198")
                rotate(landscape)
                edit(R.id.PSI, "")
                check("", "", "0")
                // Reject the complete edit; never clip an overflowing paste into a different pressure.
                edit(R.id.ATM, "1234")
                check("", "", "0")
                scenario.onActivity { assertNotNull((it.findViewById<EditText>(R.id.ATM).parent.parent as TextInputLayout).error) }
                edit(R.id.ATM, "999")
                check("999", "14985", "1998")
                for (invalid in listOf("14986", "123456", "-1", "1.5", "١", "999999999999999999999")) {
                    edit(R.id.PSI, invalid)
                    check("999", "14985", "1998")
                    scenario.onActivity { assertNotNull((it.findViewById<EditText>(R.id.PSI).parent.parent as TextInputLayout).error) }
                }
                rotate(!landscape)
                check("999", "14985", "1998")
                edit(R.id.PSI, "0")
                check("0", "0", "0")
            }
        }
    }
}
