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
import java.io.File

/** A blocking Instrumentation monitor intercepts approval: no SMS composer or transmission. */
class DispatchIntegrationTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
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
                scenario.onActivity { it.findViewById<ViewPager>(R.id.viewpager).setCurrentItem(2, false) }
                capture("custom", language)
                onView(withId(R.id.yourMsgBtnNew)).perform(click())
                val body = "  Fixture & #? +%\nשלום  "
                scenario.onActivity {
                    assertNotNull(it.findViewById<TextInputLayout>(R.id.freeMsg).error)
                    it.findViewById<EditText>(R.id.yourMsg).setText(body)
                }
                closeSoftKeyboard()
                onView(withId(R.id.yourMsgBtnNew)).perform(click())
                assertEquals(0, monitor.hits)
                preferences.edit().putInt("merhav", 0).commit()
                scenario.recreate()
                scenario.onActivity {
                    val fragment = it.supportFragmentManager.findFragmentByTag("dispatch-confirmation") as DispatchConfirmation
                    assertEquals(11, fragment.requireArguments().getInt("region"))
                    assertEquals(body, fragment.requireArguments().getString("body"))
                    val text = (fragment.requireDialog() as AlertDialog).findViewById<TextView>(android.R.id.message)!!.text.toString()
                    assertTrue(text.contains("מוקד 4")); assertTrue(text.contains(body))
                    val dialog = fragment.requireDialog() as AlertDialog
                    assertTrue(dialog.getButton(AlertDialog.BUTTON_POSITIVE).left > dialog.getButton(AlertDialog.BUTTON_NEGATIVE).left)
                }
                capture("confirmation", language)
                onView(withText(R.string.ret)).perform(click())
                assertEquals(0, monitor.hits)
                scenario.onActivity { assertEquals(body, it.findViewById<EditText>(R.id.yourMsg).text.toString()) }
                onView(withId(R.id.yourMsgBtnNew)).perform(click())
                onView(withText(R.string.ok)).perform(click())
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
