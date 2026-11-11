package atlow.chemi.mymada

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Typeface
import android.widget.Spinner
import android.widget.TextView
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.*
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.text.TextLayoutResult
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.helpers.LocalHelper
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class Target37VisualTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    @Test fun activationLayoutsKeepToolbarBelowStatusBar() {
        for (layout in listOf(R.layout.activity_not_mada, R.layout.activity_code_c, R.layout.activity_code_request)) {
            ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
                compose.waitForIdle()
                scenario.onActivity { it.setContentView(layout) }
                compose.waitForIdle()
                capture(scenario, "activation-layout-$layout")
            }
        }
    }

    @Test fun themeFontAndStatusProtectionMatchInBothLocalesAndOrientations() {
        val font = Typeface.createFromAsset(context.assets, "fonts/opensanshebrew.ttf")
        assertArrayEquals(context.assets.open("fonts/opensanshebrew.ttf").use { it.readBytes() },
            context.resources.openRawResource(R.font.opensans_hebrew).use { it.readBytes() })
        for (language in listOf("en", "he")) {
            instrumentation.runOnMainSync { LocalHelper.setLocale(context, language) }
            context.getSharedPreferences("Settings", 0).edit().putBoolean("Mapp", false)
                .putBoolean("WasLangChanged", false).putString("lng", language).putStringSet("Tools", setOf("oxy")).commit()
            ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
                // Compose installs the fragment host after Activity.onCreate. Rotate the
                // rendered screen, not a host whose initial fragment is still queued.
                compose.waitForIdle()
                scenario.onActivity {
                    assertTrue("Oxygen must be installed before visual rotation",
                        it.supportFragmentManager.findFragmentById(R.id.frame) is atlow.chemi.mymada.fragments.OxyCalc)
                }
                for (landscape in listOf(false, true)) {
                    scenario.onActivity { it.requestedOrientation = if (landscape)
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
                    compose.waitUntil(5000) {
                        var ready = false
                        scenario.onActivity { ready = (it.resources.configuration.orientation ==
                            Configuration.ORIENTATION_LANDSCAPE) == landscape && it.hasWindowFocus() }
                        ready
                    }
                    compose.waitForIdle()
                    compose.waitUntil(5000) {
                        var laidOut = false
                        scenario.onActivity {
                            val result = it.findViewById<TextView>(R.id.txtOxyMin)
                            laidOut = result != null && result.width > 0 && result.height > 0
                        }
                        laidOut
                    }
                    var title = ""
                    var titleSize = 0f
                    scenario.onActivity { activity ->
                        assertEquals("Fresh oxygen fixture ($language, landscape=$landscape)",
                            "24", activity.findViewById<TextView>(R.id.txtOxyMin).text.toString())
                        title = activity.title.toString()
                        titleSize = activity.resources.getDimension(androidx.appcompat.R.dimen.abc_text_size_title_material_toolbar)
                        val spinner = activity.findViewById<Spinner>(R.id.OxyTypeS)
                        val selected = spinner.selectedView as TextView
                        val dropdown = spinner.adapter.getDropDownView(0, null, spinner) as TextView
                        for (view in listOf(selected, dropdown)) {
                            val expected = Paint(view.paint).apply { typeface = font }
                            val sample = "Small oxygen tank בלון חמצן קטן"
                            assertEquals("Selector/dropdown use bundled glyphs", expected.measureText(sample),
                                view.paint.measureText(sample), .01f)
                        }
                    }
                    val layouts = mutableListOf<TextLayoutResult>()
                    compose.onNode(hasText(title) and !hasTestTag("shell-destination-${R.id.oxygenCalc}"))
                        .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
                    // Native text and Compose both enable antialiasing. Bare Paint changes
                    // hinted glyph advances on API24; subpixel positioning changes them too.
                    // Compare full shaped text, not an ellipsized toolbar line.
                    val expectedTitle = android.text.TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                        textSize = titleSize
                        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                    }
                    val layout = layouts.single()
                    assertEquals("Compose title keeps native medium glyph metrics ($language, landscape=$landscape, ellipsized=${layout.isLineEllipsized(0)})",
                        android.text.Layout.getDesiredWidth(title, expectedTitle),
                        layout.multiParagraph.intrinsics.maxIntrinsicWidth, 1f)
                    capture(scenario, "oxygen-$language-${if (landscape) "landscape" else "portrait"}")
                    if (!landscape) {
                        lateinit var dialog: androidx.appcompat.app.AlertDialog
                        scenario.onActivity { activity ->
                            dialog = androidx.appcompat.app.AlertDialog.Builder(activity, R.style.AlertDialogStyle)
                                .setTitle(R.string.plsNote).setMessage(R.string.plsNoteT)
                                .setPositiveButton(R.string.ok, null).show()
                        }
                        instrumentation.uiAutomation.waitForIdle(500, 5000)
                        compose.waitUntil(5000) {
                            var ready = false
                            scenario.onActivity { ready = dialog.window!!.decorView.hasWindowFocus() &&
                                dialog.findViewById<TextView>(android.R.id.message)!!.height > 0 }
                            ready
                        }
                        scenario.onActivity {
                            val message = dialog.findViewById<TextView>(android.R.id.message)!!
                            val expected = Paint(message.paint).apply {
                                typeface = Typeface.create("sans-serif", Typeface.NORMAL)
                            }
                            assertEquals(expected.measureText(message.text.toString()),
                                message.paint.measureText(message.text.toString()), .01f)
                        }
                        val image = instrumentation.uiAutomation.takeScreenshot()!!
                        File(context.getExternalFilesDir("target37-visuals"), "request-disclaimer-$language.png")
                            .outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
                        image.recycle()
                        scenario.onActivity { dialog.dismiss() }
                    }
                }
            }
        }
        // This checks the Settings window, not the separately tested overlay-permission dialog.
        android.os.ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(
            "appops set ${context.packageName} SYSTEM_ALERT_WINDOW allow")).use { it.readBytes() }
        assertTrue(android.provider.Settings.canDrawOverlays(context))
        ActivityScenario.launch(SettingsPage::class.java).use { capture(it, "settings-status") }
    }

    private fun <T : Activity> capture(scenario: ActivityScenario<T>, name: String) {
        compose.waitUntil(5000) {
            var focused = false
            scenario.onActivity { focused = it.hasWindowFocus() }
            focused
        }
        instrumentation.uiAutomation.waitForIdle(500, 5000)
        val drawn = CountDownLatch(1)
        var top = 0
        var color = 0
        var composeToolbar = false
        scenario.onActivity { activity ->
            top = ViewCompat.getRootWindowInsets(activity.window.decorView)!!
                .getInsets(WindowInsetsCompat.Type.statusBars()).top
            color = activity.getColor(R.color.ColorPrimary)
            val toolbar = activity.findViewById<android.view.View>(R.id.toolbar)
            composeToolbar = toolbar == null
            if (toolbar != null) {
                val location = IntArray(2)
                toolbar.getLocationOnScreen(location)
                assertTrue("Toolbar remains below status inset", location[1] >= top)
            }
            activity.window.decorView.postOnAnimation {
                activity.window.decorView.postOnAnimation { drawn.countDown() }
            }
        }
        if (composeToolbar) {
            assertTrue("Compose navigation remains below status inset",
                compose.onNodeWithTag("shell-navigation").fetchSemanticsNode().boundsInWindow.top >= top)
        }
        assertTrue(drawn.await(5, TimeUnit.SECONDS))
        val image = instrumentation.uiAutomation.takeScreenshot()!!
        File(context.getExternalFilesDir("target37-visuals"), "$name.png").outputStream().use {
            image.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        try {
            assertTrue("Visible status inset", top > 0)
            assertEquals("Primary color behind status bar", color, image.getPixel(image.width / 2, top / 2))
        } finally { image.recycle() }
    }
}
