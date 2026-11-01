package atlow.chemi.mymada.calls

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.graphics.Rect
import android.media.AudioManager
import android.os.SystemClock
import android.os.ParcelFileDescriptor
import android.provider.Settings
import android.view.accessibility.AccessibilityNodeInfo
import android.view.MotionEvent
import android.view.InputDevice
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

    @Test fun removalResizesRestoresAndCancelsWithoutDismissing() {
        assertTrue("Harness must grant overlay access", Settings.canDrawOverlays(context))
        instrumentation.uiAutomation.serviceInfo = instrumentation.uiAutomation.serviceInfo.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        }
        fun bounds(id: String) = Rect().also { requireNotNull(node(id)) { "Missing overlay node $id" }.getBoundsInScreen(it) }
        var down = 0L
        fun touch(action: Int, x: Float, y: Float) {
            if (action == MotionEvent.ACTION_DOWN) down = SystemClock.uptimeMillis()
            val event = MotionEvent.obtain(down, SystemClock.uptimeMillis(), action, x, y, 0)
            event.source = InputDevice.SOURCE_TOUCHSCREEN
            try { assertTrue(instrumentation.uiAutomation.injectInputEvent(event, true)) }
            finally { event.recycle() }
            instrumentation.waitForIdleSync()
        }
        ActivityScenario.launch(InfoActivityKt::class.java).use { scenario ->
            try {
                scenario.onActivity { it.startService(intent("60")) }
                await { node("root_container") != null }
                val start = bounds("root_container")
                touch(MotionEvent.ACTION_DOWN, start.exactCenterX(), start.exactCenterY())
                SystemClock.sleep(650)
                await { node("remove_img") != null }
                val normal = bounds("remove_img")
                val normalWindow = bounds("remove_relativelayout")
                val x = normal.exactCenterX()
                val y = normal.exactCenterY()
                touch(MotionEvent.ACTION_MOVE, x, y)
                await { bounds("remove_img").width() == (normal.width() * 1.5).toInt() }
                val enlarged = bounds("remove_img")
                assertEquals((normal.height() * 1.5).toInt(), enlarged.height())
                val enlargedWindow = bounds("remove_relativelayout")
                assertEquals("The window must grow, not just a scaled child",
                    normalWindow.width() + enlarged.width() - normal.width(), enlargedWindow.width())
                assertEquals(normalWindow.height() + enlarged.height() - normal.height(), enlargedWindow.height())
                val bubble = bounds("root_container")
                touch(MotionEvent.ACTION_MOVE, x, y)
                assertEquals("Repeated MOVE must not shift the snapped bubble", bubble, bounds("root_container"))
                touch(MotionEvent.ACTION_MOVE, start.exactCenterX(), start.exactCenterY())
                await { bounds("remove_img").width() == normal.width() }
                touch(MotionEvent.ACTION_MOVE, x, y)
                await { bounds("remove_img").width() == enlarged.width() }
                touch(MotionEvent.ACTION_CANCEL, x, y)
                assertNotNull("Cancel must retain the alert", node("close_floating_view"))
                SystemClock.sleep(600) // Let the existing edge-snap animation finish.
                val retained = bounds("root_container")
                touch(MotionEvent.ACTION_DOWN, retained.exactCenterX(), retained.exactCenterY())
                SystemClock.sleep(650)
                await { node("remove_img") != null }
                assertEquals("New drag must start at base size", normal.width(), bounds("remove_img").width())
                touch(MotionEvent.ACTION_MOVE, x, y)
                touch(MotionEvent.ACTION_UP, x, y)
                await { node("close_floating_view") == null }
            } finally { context.stopService(intent("0")) }
        }
    }
}
