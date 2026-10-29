package atlow.chemi.mymada.about

import android.content.Intent
import android.os.Debug
import android.util.Log
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.HomePage
import atlow.chemi.mymada.InfoActivityKt
import atlow.chemi.mymada.PrivacyActivity
import atlow.chemi.mymada.R
import atlow.chemi.mymada.SettingsPage
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import java.io.File

/**
 * Stock-48MiB regression: #36 exhausts the heap decoding About artwork after this route.
 * Run on a firewalled disposable emulator; pending offline ads exercise Activity retention.
 * Leave dumpHeap unset for regression runs so heap-dump GC cannot mask the failure.
 */
class AboutMemoryTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    @Test fun aboutRecreationPrivacySettingsThenHome() {
        context.getSharedPreferences("Settings", 0).edit().clear().putString("lng", "en")
            .putBoolean("privacy", true).putBoolean("oldU", true).putBoolean("Mapp", false).commit()
        instrumentation.uiAutomation.executeShellCommand(
            "appops set ${context.packageName} SYSTEM_ALERT_WINDOW allow"
        ).use { descriptor -> java.io.FileInputStream(descriptor.fileDescriptor).use { it.readBytes() } }
        memory("start")
        ActivityScenario.launch(InfoActivityKt::class.java).use { about ->
            compose.waitForIdle()
            memory("about")
            about.recreate()
            compose.waitForIdle()
            memory("recreated")
            var privacy = ""
            about.onActivity { privacy = it.getString(R.string.title_activity_privacy) }
            val monitor = instrumentation.addMonitor(PrivacyActivity::class.java.name, null, false)
            try {
                compose.onNode(hasText(privacy) or hasContentDescription(privacy)).performScrollTo().performClick()
                val activity = instrumentation.waitForMonitorWithTimeout(monitor, 5_000)
                assertNotNull(activity)
                instrumentation.runOnMainSync { activity!!.finish() }
                compose.waitUntil(10_000) { about.state == Lifecycle.State.RESUMED }
            } finally { instrumentation.removeMonitor(monitor) }
            memory("privacy returned")
            val settingsMonitor = instrumentation.addMonitor(SettingsPage::class.java.name, null, false)
            try {
                about.onActivity { it.startActivity(Intent(it, SettingsPage::class.java)) }
                val activity = instrumentation.waitForMonitorWithTimeout(settingsMonitor, 5_000)
                assertNotNull(activity)
                instrumentation.runOnMainSync { activity!!.finish() }
                compose.waitUntil(10_000) { about.state == Lifecycle.State.RESUMED }
            } finally { instrumentation.removeMonitor(settingsMonitor) }
            memory("settings returned")
        }
        memory("about closed")
        ActivityScenario.launch(InfoActivityKt::class.java).use { compose.waitForIdle() }
        memory("second about closed")
        if (InstrumentationRegistry.getArguments().getString("dumpHeap") == "true") {
            Debug.dumpHprofData(File(context.filesDir, "before-home.hprof").path)
            memory("after heap dump GC")
        }
        ActivityScenario.launch(HomePage::class.java).use { home ->
            home.onActivity { it.findViewById<RecyclerView>(R.id.rv).scrollToPosition(4) }
            compose.waitForIdle()
            memory("home")
            val monitor = instrumentation.addMonitor(InfoActivityKt::class.java.name, null, false)
            try {
                home.onActivity {
                    val holder = it.findViewById<RecyclerView>(R.id.rv).findViewHolderForAdapterPosition(4)
                    assertNotNull(holder)
                    holder!!.itemView.findViewById<android.view.View>(R.id.buttonChange).performClick()
                }
                val about = instrumentation.waitForMonitorWithTimeout(monitor, 5_000)
                assertNotNull(about)
                compose.waitForIdle()
                memory("about from home")
                instrumentation.runOnMainSync { about!!.finish() }
            } finally { instrumentation.removeMonitor(monitor) }
        }
    }

    private fun memory(stage: String) {
        val runtime = Runtime.getRuntime()
        Log.i("AboutMemory", "$stage used=${runtime.totalMemory() - runtime.freeMemory()} max=${runtime.maxMemory()}")
    }
}
