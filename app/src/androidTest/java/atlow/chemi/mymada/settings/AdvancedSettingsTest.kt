package atlow.chemi.mymada.settings

import android.widget.SeekBar
import android.widget.EditText
import android.view.Gravity
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import androidx.appcompat.app.AlertDialog
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.R
import atlow.chemi.mymada.SettingsPage
import atlow.chemi.mymada.fragments.SettingsAdvanced
import atlow.chemi.mymada.helpers.LocalHelper
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import org.junit.Rule
import org.junit.Assert.*
import org.junit.Test
import android.graphics.Bitmap
import java.io.File

class AdvancedSettingsTest {
    @get:Rule val compose = createEmptyComposeRule()
    private fun clickRow(tag: String) = compose.onNodeWithTag(tag).performScrollTo().performClick()
    @Test fun volumeSliderRetainsActivityColorsAndDiscreteStyle() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        instrumentation.runOnMainSync { LocalHelper.setLocale(context, "en") }
        val preferences = context.getSharedPreferences("Settings", 0)
        preferences.edit().putBoolean("Mapp", false).putBoolean("volumeApp", false).putInt("volumeAppNum", 2).commit()
        ActivityScenario.launch(SettingsPage::class.java).use { scenario ->
            scenario.onActivity { it.displayFragment(SettingsAdvanced(), R.id.advanced) }
            clickRow("volume")
            scenario.onActivity { activity ->
                val owner = activity.supportFragmentManager.findFragmentById(R.id.frame) as SettingsAdvanced
                val dialog = (owner.childFragmentManager.findFragmentByTag("volume") as SettingsAdvanced.VolumeDialog).requireDialog()
                val slider = dialog.findViewById<SeekBar>(R.id.VolumeSeekBar)
                val native = activity.layoutInflater.inflate(R.layout.volume_dialog, null).findViewById<SeekBar>(R.id.VolumeSeekBar)
                for (attribute in intArrayOf(androidx.appcompat.R.attr.colorControlNormal,
                        androidx.appcompat.R.attr.colorControlActivated, androidx.appcompat.R.attr.colorAccent)) {
                    val expected = native.context.obtainStyledAttributes(intArrayOf(attribute))
                    val actual = slider.context.obtainStyledAttributes(intArrayOf(attribute))
                    assertEquals(expected.getColor(0, 0), actual.getColor(0, 0))
                    expected.recycle(); actual.recycle()
                }
                val style = slider.context.obtainStyledAttributes(intArrayOf(androidx.appcompat.R.attr.seekBarStyle))
                assertEquals(androidx.appcompat.R.style.Widget_AppCompat_SeekBar_Discrete, style.getResourceId(0, 0))
                style.recycle()
                assertEquals(2, slider.progress)
            }
            capture("volume-activity-theme-en")
            clickRow("volumeApp")
            clickRow("dialog-cancel")
            assertFalse(preferences.getBoolean("volumeApp", true))
            assertEquals(2, preferences.getInt("volumeAppNum", -1))
        }
    }

    @Test fun bilingualTogglesAndDialogDraftsSurviveRecreationWithoutSavingOnCancel() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val preferences = context.getSharedPreferences("Settings", 0)
        for (language in listOf("en", "he")) {
            instrumentation.runOnMainSync { LocalHelper.setLocale(context, language) }
            preferences.edit().putBoolean("Mapp", false).putBoolean("not", true).putBoolean("win", true)
                .putBoolean("ride", false).putBoolean("loop", false).putBoolean("killS", false)
                .putString("timeK", "6").putBoolean("volumeApp", false).putInt("volumeAppNum", 2).commit()
            ActivityScenario.launch(SettingsPage::class.java).use { scenario ->
                scenario.onActivity {
                    it.displayFragment(SettingsAdvanced(), R.id.advanced)
                }
                capture("advanced-$language")
                for (key in listOf("not", "win", "ride", "loop", "killS")) {
                    val old = preferences.getBoolean(key, false)
                    clickRow(key)
                    assertEquals(!old, preferences.getBoolean(key, old))
                }
                scenario.recreate()
                clickRow("timeK")
                compose.onNodeWithTag("dialog-cancel").assertIsDisplayed()
                scenario.onActivity {
                    val parent = it.supportFragmentManager.findFragmentById(R.id.frame) as SettingsAdvanced
                    val dialog = (parent.childFragmentManager.findFragmentByTag("delay") as SettingsAdvanced.DelayDialog).requireDialog()
                    val field = dialog.findViewById<EditText>(R.id.shabatT)
                    val layout = dialog.findViewById<TextInputLayout>(R.id.shabatTil)
                    // Native shabat_t is inflated with the activity theme, not AlertDialogStyle.
                    assertEquals(Gravity.START or Gravity.TOP, field.gravity)
                    assertEquals(ContextCompat.getColor(it, R.color.ColorAqua), layout.defaultHintTextColor!!.defaultColor)
                    if (it.resources.displayMetrics.density == 2.625f && it.resources.configuration.fontScale == 1f) {
                        // Measured native12822 EN/HE on the owned API24/API37 references.
                        if (android.os.Build.VERSION.SDK_INT == 24) assertEquals(108, field.height)
                        if (android.os.Build.VERSION.SDK_INT == 37) assertEquals(113, field.height)
                    }
                    val metrics = field.paint.fontMetricsInt
                    Log.i("SettingsDelayGeometry", "$language focused=${field.hasFocus()} fieldHeight=${field.height} layoutHeight=${layout.height} textSize=${field.textSize} top=${metrics.top} ascent=${metrics.ascent} descent=${metrics.descent} bottom=${metrics.bottom} padding=${field.paddingTop},${field.paddingBottom}")
                }
                capture("advanced-delay-$language")
                onView(withId(R.id.shabatT)).inRoot(isDialog()).perform(replaceText("999999999999"), closeSoftKeyboard())
                clickRow("dialog-confirm")
                assertEquals("6", preferences.getString("timeK", null))
                onView(withId(R.id.shabatT)).inRoot(isDialog()).perform(replaceText("12"), closeSoftKeyboard())
                scenario.recreate()
                onView(withId(R.id.shabatT)).inRoot(isDialog()).check(matches(withText("12")))
                clickRow("dialog-confirm")
                assertEquals("12", preferences.getString("timeK", null))
                for (confirm in listOf(false, true)) {
                    clickRow("timeK")
                    onView(withId(R.id.shabatT)).inRoot(isDialog()).perform(replaceText("0"), closeSoftKeyboard())
                    scenario.recreate()
                    onView(withId(R.id.shabatT)).inRoot(isDialog()).check(matches(withText("0")))
                    clickRow(if (confirm) "dialog-confirm" else "dialog-cancel")
                    assertEquals(if (confirm) "0" else "12", preferences.getString("timeK", null))
                }
                for (confirm in listOf(false, true)) {
                    clickRow("volume")
                    compose.onNodeWithTag("dialog-cancel").assertIsDisplayed()
                    if (!confirm) capture("advanced-volume-$language")
                    assertFalse(preferences.getBoolean("volumeApp", true))
                    clickRow("volumeApp")
                    scenario.onActivity {
                        val parent = it.supportFragmentManager.findFragmentById(R.id.frame) as SettingsAdvanced
                        val dialog = (parent.childFragmentManager.findFragmentByTag("volume") as SettingsAdvanced.VolumeDialog)
                            .requireDialog() as AlertDialog
                        val slider = dialog.findViewById<SeekBar>(R.id.VolumeSeekBar)!!
                        slider.performAccessibilityAction(android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_PROGRESS.id,
                            android.os.Bundle().apply { putFloat(android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_PROGRESS_VALUE, 0f) })
                    }
                    scenario.recreate()
                    compose.onNodeWithTag("volumeApp").assertIsOn()
                    clickRow(if (confirm) "dialog-confirm" else "dialog-cancel")
                    assertEquals(confirm, preferences.getBoolean("volumeApp", false))
                    assertEquals(if (confirm) 0 else 2, preferences.getInt("volumeAppNum", -1))
                }
                scenario.recreate()
                clickRow("day-night-settings")
                compose.onNodeWithTag("sleepDay").assertIsDisplayed()
            }
        }
    }

    private fun capture(name: String) {
        if (InstrumentationRegistry.getArguments().getString("capture") != "true") return
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.uiAutomation.waitForIdle(500, 5_000)
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        File(instrumentation.targetContext.filesDir, "$name.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
    }
}
