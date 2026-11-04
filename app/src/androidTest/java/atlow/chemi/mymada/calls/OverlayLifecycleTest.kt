package atlow.chemi.mymada.calls

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.media.AudioManager
import android.os.SystemClock
import android.os.ParcelFileDescriptor
import android.provider.Settings
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.InfoActivityKt
import atlow.chemi.mymada.recieversAndServices.FloatingWidgetService
import org.junit.Assert.*
import org.junit.Test

/** Disposable emulator with SYSTEM_ALERT_WINDOW explicitly granted by the harness. */
class OverlayLifecycleTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private fun find(node: AccessibilityNodeInfo?, id: String): AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.viewIdResourceName?.endsWith(":id/$id") == true) return node
        for (index in 0 until node.childCount) find(node.getChild(index), id)?.let { return it }
        return null
    }
    private fun node(id: String) = instrumentation.uiAutomation.windows.firstNotNullOfOrNull { find(it.root, id) }
    private fun await(predicate: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + 5000
        while (!predicate() && SystemClock.uptimeMillis() < deadline) SystemClock.sleep(30)
        assertTrue(predicate())
    }
    private fun intent(seconds: String) = Intent(context, FloatingWidgetService::class.java)
        .putExtra("sou", false).putExtra("vib", false).putExtra("ShabbatM", true).putExtra("timeK", seconds)

    @Test fun deniedOverlayStopsWithoutWindows() {
        // Run independently after the harness resets SYSTEM_ALERT_WINDOW to default.
        assertFalse(Settings.canDrawOverlays(context))
        ActivityScenario.launch(InfoActivityKt::class.java).use { scenario ->
            scenario.onActivity { it.startService(intent("10")) }
            instrumentation.waitForIdleSync()
            await {
                val dump = ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(
                    "dumpsys activity services ${context.packageName}")).bufferedReader().use { it.readText() }
                !dump.contains("ServiceRecord{") || !dump.contains("FloatingWidgetService")
            }
        }
    }

    @Test fun replacementCancelsOldTimerAndCloseRemovesWindows() {
        assertTrue("Harness must grant overlay access", Settings.canDrawOverlays(context))
        instrumentation.uiAutomation.serviceInfo = instrumentation.uiAutomation.serviceInfo.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
        ActivityScenario.launch(InfoActivityKt::class.java).use { scenario ->
            try {
                scenario.onActivity { it.startService(intent("1")) }
                await { node("close_floating_view") != null }
                scenario.onActivity { it.startService(intent("10")) }
                SystemClock.sleep(1300)
                assertTrue(requireNotNull(node("root_container")).performAction(AccessibilityNodeInfo.ACTION_CLICK))
                await { node("close_expanded_view") != null }
                assertTrue(requireNotNull(node("close_expanded_view")).performAction(AccessibilityNodeInfo.ACTION_CLICK))
                await { node("close_floating_view") != null }
                val close = requireNotNull(node("close_floating_view"))
                assertTrue("Superseded alert timer must not close the replacement", close.performAction(AccessibilityNodeInfo.ACTION_CLICK))
                await { node("close_floating_view") == null }
            } finally { context.stopService(intent("0")) }
        }
    }

    @Test fun failedPlaybackRestoresAlarmVolumeAndMalformedTimerDoesNotCrash() {
        assertTrue(Settings.canDrawOverlays(context))
        val audio = context.getSystemService(AudioManager::class.java)
        val original = audio.getStreamVolume(AudioManager.STREAM_ALARM)
        val preferences = context.getSharedPreferences("Settings", 0)
        val override = preferences.getBoolean("volumeApp", true)
        val volume = preferences.getInt("volumeAppNum", audio.getStreamMaxVolume(AudioManager.STREAM_ALARM))
        preferences.edit().putBoolean("volumeApp", true).putInt("volumeAppNum", if (original == 1) 2 else 1).commit()
        ActivityScenario.launch(InfoActivityKt::class.java).use { scenario ->
            try {
                scenario.onActivity { it.startService(intent("invalid").putExtra("sou", true).putExtra("uri", "file:///missing-offline-alert")) }
                instrumentation.waitForIdleSync()
                SystemClock.sleep(300)
                assertEquals(original, audio.getStreamVolume(AudioManager.STREAM_ALARM))
                scenario.onActivity { it.stopService(intent("0")) }
                assertEquals(original, audio.getStreamVolume(AudioManager.STREAM_ALARM))
            } finally {
                context.stopService(intent("0"))
                preferences.edit().putBoolean("volumeApp", override).putInt("volumeAppNum", volume).commit()
                audio.setStreamVolume(AudioManager.STREAM_ALARM, original, 0)
            }
        }
    }
}
