package atlow.chemi.mymada

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.SystemClock
import android.os.ParcelFileDescriptor
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.calls.CallEvent
import atlow.chemi.mymada.helpers.LocalHelper
import org.junit.Test
import org.junit.Assert.assertTrue
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Same fixtures run against the retained XML APK and Compose APK on isolated emulators. */
@org.junit.runner.RunWith(org.junit.runners.Parameterized::class)
class MetronomeCallVisualTest(private val screen: String, private val language: String, private val landscape: Boolean) {
    companion object {
        @JvmStatic
        @org.junit.runners.Parameterized.Parameters(name = "{0}-{1}-landscape={2}")
        fun fixtures(): List<Array<Any>> {
            val arguments = InstrumentationRegistry.getArguments()
            fun values(key: String, defaults: List<String>) = arguments.getString(key)?.let {
                require(it in defaults) { "Unknown $key: $it" }
                listOf(it)
            } ?: defaults
            return values("screen", listOf("metro", "call")).flatMap { screen ->
                values("language", listOf("en", "he")).flatMap { language ->
                    values("landscape", listOf("false", "true")).map { landscape ->
                        arrayOf<Any>(screen, language, landscape.toBoolean())
                    }
                }
            }
        }
    }
    @Test fun captureProductionFixtures() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        instrumentation.runOnMainSync { LocalHelper.setLocale(context, language) }
        context.getSharedPreferences("Settings", 0).edit().putBoolean("Mapp", false).putBoolean("cut", true)
            .putStringSet("Tools", setOf("metro")).putBoolean("NsouT1", false)
            .putBoolean("NvibT1", false).putBoolean("killS", false).commit()
        val intent = if (screen == "metro") Intent(context, UtillitiesAndTools::class.java)
            else CallEvent(1, "אירוע חדש. 12:34 מונה: 42 כתובת: תל אביב רחוב הבדיקה 12 קוד הזנקה: 123 קוד רפואי: 456", null)
                .popupIntent(context, -1, false)
        ActivityScenario.launch<android.app.Activity>(intent).use { scenario ->
            scenario.onActivity { it.requestedOrientation = if (landscape)
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
            val deadline = SystemClock.elapsedRealtime() + 15000
            var ready = false
            while (!ready && SystemClock.elapsedRealtime() < deadline) {
                scenario.onActivity {
                    val text = it.findViewById<TextView>(if (screen == "metro") R.id.metro_Clock else R.id.Address)
                    ready = it.hasWindowFocus() && text != null && text.isShown && text.width > 0 &&
                        (it.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) == landscape
                }
                if (!ready) Thread.sleep(50)
            }
            assertTrue("Focused, laid-out content in requested orientation", ready)
            val drawn = CountDownLatch(1)
            scenario.onActivity { activity -> activity.window.decorView.postOnAnimation {
                activity.window.decorView.postOnAnimation { drawn.countDown() }
            } }
            assertTrue(drawn.await(5, TimeUnit.SECONDS))
            var settled = false
            while (!settled && SystemClock.elapsedRealtime() < deadline) {
                val windows = ParcelFileDescriptor.AutoCloseInputStream(
                    instrumentation.uiAutomation.executeShellCommand("dumpsys window windows"))
                    .bufferedReader().use { it.readText() }
                settled = "Splash Screen ${context.packageName}" !in windows &&
                    "mAnimationType=starting_reveal" !in windows
                if (!settled) Thread.sleep(50)
            }
            assertTrue("Starting window removed before capturing system icons", settled)
            val image = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
            File(context.getExternalFilesDir("ui-parity"), "$screen-$language-$landscape.png")
                .outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
            image.recycle()
        }
    }
}
