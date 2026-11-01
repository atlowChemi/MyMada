package atlow.chemi.mymada

import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Build
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.permissions.NotificationPermission
import atlow.chemi.mymada.recieversAndServices.MetroService
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test

class MetronomePermissionTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private fun await(condition: () -> Boolean) {
        val end = SystemClock.elapsedRealtime() + 10000
        while (SystemClock.elapsedRealtime() < end) {
            if (condition()) return
            Thread.sleep(50)
        }
        fail("Timed out waiting for permission/playback state")
    }
    private fun find(node: AccessibilityNodeInfo?, suffix: String): AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.viewIdResourceName?.endsWith(suffix) == true) return node
        repeat(node.childCount) { find(node.getChild(it), suffix)?.let { result -> return result } }
        return null
    }

    /** Run separately with permissionRoute=grant/deny, revoked permission and cleared user-set flags. */
    @Test fun recreatedPromptStartsOnceAfterActualOsDecisionAndStillStops() {
        assumeTrue(Build.VERSION.SDK_INT >= 33)
        val context = instrumentation.targetContext
        context.getSharedPreferences("Settings", 0).edit().putStringSet("Tools", setOf("metro")).commit()
        assertFalse(NotificationPermission.granted(context))
        val grant = InstrumentationRegistry.getArguments().getString("permissionRoute", "grant") == "grant"
        instrumentation.uiAutomation.serviceInfo = instrumentation.uiAutomation.serviceInfo.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        }
        ActivityScenario.launch(UtillitiesAndTools::class.java).use { screen ->
            lateinit var activity: UtillitiesAndTools
            try {
                clickMetronome()
                screen.recreate()
                screen.onActivity { activity = it }
                fun promptButton(button: Int): Boolean {
                    var clicked = false
                    instrumentation.runOnMainSync {
                        val prompt = activity.supportFragmentManager.findFragmentByTag("notification-permission") as? DialogFragment
                        val dialog = prompt?.dialog as? AlertDialog
                        if (dialog != null) { dialog.getButton(button).performClick(); clicked = true }
                    }
                    return clicked
                }
                assertFalse("Permission dialog has not started audio", MetroService.running)
                await { promptButton(AlertDialog.BUTTON_POSITIVE) }
                val suffix = if (grant) ":id/permission_allow_button" else ":id/permission_deny_button"
                var osButton: AccessibilityNodeInfo? = null
                await {
                    osButton = find(instrumentation.uiAutomation.rootInActiveWindow, suffix)
                    osButton != null
                }
                assertTrue(osButton!!.performAction(AccessibilityNodeInfo.ACTION_CLICK))
                if (!grant) await { promptButton(AlertDialog.BUTTON_NEGATIVE) }
                await { MetroService.running }
                assertEquals(grant, NotificationPermission.granted(context))
                clickMetronome(running = true)
                await { !MetroService.running }
            } finally { instrumentation.runOnMainSync { MetroService.stop(context) } }
        }
    }
}
