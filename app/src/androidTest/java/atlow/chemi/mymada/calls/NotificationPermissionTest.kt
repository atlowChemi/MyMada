package atlow.chemi.mymada.calls

import android.os.Build
import android.os.SystemClock
import android.os.ParcelFileDescriptor
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.R
import atlow.chemi.mymada.InfoActivityKt
import atlow.chemi.mymada.smsRes
import atlow.chemi.mymada.permissions.NotificationPermission
import atlow.chemi.mymada.calls.ListenerAccess
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Run each route on a fresh, unpregranted disposable install; never grants via adb. */
class NotificationPermissionTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private fun find(node: AccessibilityNodeInfo?, predicate: (AccessibilityNodeInfo) -> Boolean): AccessibilityNodeInfo? {
        if (node == null) return null
        if (predicate(node)) return node
        for (i in 0 until node.childCount) find(node.getChild(i), predicate)?.let { return it }
        return null
    }
    private fun awaitNode(predicate: (AccessibilityNodeInfo) -> Boolean): AccessibilityNodeInfo {
        val end = SystemClock.uptimeMillis() + 10000
        while (SystemClock.uptimeMillis() < end) {
            find(instrumentation.uiAutomation.rootInActiveWindow, predicate)?.let { return it }
            SystemClock.sleep(50)
        }
        throw AssertionError("Expected UI control not present")
    }
    private fun text(id: Int) = awaitNode { it.text?.toString()?.equals(context.getString(id), ignoreCase = true) == true }
    private fun click(node: AccessibilityNodeInfo) {
        var target = node
        while (!target.isClickable && target.parent != null) target = target.parent
        assertTrue(target.performAction(AccessibilityNodeInfo.ACTION_CLICK))
    }

    /** Notification permission is seeded here; the listener grant itself is exercised in Android Settings. */
    @Test fun listenerSettingsGrantReturnsToCheckedDetectionControl() {
        assumeTrue(Build.VERSION.SDK_INT >= 33)
        instrumentation.uiAutomation.serviceInfo = instrumentation.uiAutomation.serviceInfo.apply {
            flags = flags or android.accessibilityservice.AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        }
        ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(
            "pm grant ${context.packageName} android.permission.POST_NOTIFICATIONS")).use { it.readBytes() }
        assertNull("Run with no listener grant", ListenerAccess.selected(context))
        val preferences = context.getSharedPreferences("Settings", 0)
        preferences.edit().putBoolean("Mapp", false).commit()
        ActivityScenario.launch(InfoActivityKt::class.java).use { scenario ->
            click(awaitNode { it.contentDescription?.toString() == context.getString(R.string.pref_title_enable) })
            text(R.string.dialog_help)
            scenario.recreate()
            click(text(R.string.dialog_yes))
            click(awaitNode { it.isCheckable && !it.isChecked && it.className?.toString()?.contains("Switch") == true })
            click(awaitNode { it.viewIdResourceName == "com.android.settings:id/allow_button" || it.viewIdResourceName == "android:id/button1" })
            awaitNode { it.isCheckable && it.isChecked && it.className?.toString()?.contains("Switch") == true }
            instrumentation.uiAutomation.waitForIdle(300, 5000)
            assertTrue(instrumentation.uiAutomation.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK))
            awaitNode {
                it.contentDescription?.toString() == context.getString(R.string.pref_title_enable) &&
                    generateSequence(it) { node -> node.parent }.any { node -> node.isChecked }
            }
            assertNotNull(ListenerAccess.selected(context))
            assertTrue(preferences.getBoolean("Mapp", false))
            val notifications = context.getSystemService(android.app.NotificationManager::class.java)
            assertTrue(notifications.activeNotifications.any { it.id == 78 })
            // Revoke using the same actual Settings UI, not a cached application flag.
            scenario.onActivity { it.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS)
                .putExtra(Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME, ListenerAccess.selected(context)!!.flattenToString())) }
            click(awaitNode { it.isCheckable && it.isChecked && it.className?.toString()?.contains("Switch") == true })
            click(awaitNode { it.viewIdResourceName == "android:id/button1" })
            awaitNode { it.isCheckable && !it.isChecked && it.className?.toString()?.contains("Switch") == true }
            instrumentation.uiAutomation.waitForIdle(300, 5000)
            instrumentation.uiAutomation.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK)
            val deadline = SystemClock.uptimeMillis() + 5000
            while (preferences.getBoolean("Mapp", false) && SystemClock.uptimeMillis() < deadline) SystemClock.sleep(30)
            assertNull(ListenerAccess.selected(context))
            assertFalse(preferences.getBoolean("Mapp", true))
            assertTrue(notifications.activeNotifications.none { it.id == 78 })
        }
    }

    @Test fun aboutDetectionSurvivesRecreationAndStaysOffWhenListenerAccessIsCanceled() {
        assumeTrue(Build.VERSION.SDK_INT >= 33)
        instrumentation.uiAutomation.serviceInfo = instrumentation.uiAutomation.serviceInfo.apply {
            flags = flags or android.accessibilityservice.AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        }
        assertFalse(NotificationPermission.granted(context))
        val preferences = context.getSharedPreferences("Settings", 0)
        preferences.edit().putBoolean("Mapp", false).commit()
        ActivityScenario.launch(InfoActivityKt::class.java).use { scenario ->
            fun detection() = awaitNode { it.contentDescription?.toString() == context.getString(R.string.pref_title_enable) }
            click(detection())
            text(R.string.notification_permission_explain)
            scenario.recreate()
            click(text(R.string.dialog_no))
            instrumentation.waitForIdleSync()
            assertFalse(preferences.getBoolean("Mapp", false))
            click(detection())
            click(text(R.string.notification_permission_allow))
            click(awaitNode { it.viewIdResourceName?.endsWith(":id/permission_allow_button") == true })
            // Listener access is a separate grant. Cancel its explanation, then inspect the real toolbar.
            click(text(R.string.dialog_no))
            assertFalse(preferences.getBoolean("Mapp", false))
        }
    }

    @Test fun freshInstallPermissionRoute() {
        assumeTrue(Build.VERSION.SDK_INT >= 33)
        instrumentation.uiAutomation.serviceInfo = instrumentation.uiAutomation.serviceInfo.apply {
            flags = flags or android.accessibilityservice.AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        }
        assertFalse("Run on a fresh install without granting notifications", NotificationPermission.granted(context))
        val route = InstrumentationRegistry.getArguments().getString("permissionRoute", "grant")
        context.getSharedPreferences("Settings", 0).edit().putBoolean("Nsou", false).putBoolean("Nvib", false).commit()
        var granted: Boolean? = null
        val result = CountDownLatch(1)
        ActivityScenario.launch<smsRes>(CallEvent(1, "בדיקת הרשאות", null).popupIntent(context, 0, false)).use { scenario ->
            scenario.onActivity { activity ->
                activity.supportFragmentManager.setFragmentResultListener("test-permission", activity) { _, data ->
                    granted = data.getBoolean("granted"); result.countDown()
                }
                NotificationPermission.request(activity, "test-permission")
            }
            text(R.string.notification_permission_explain)
            click(text(R.string.notification_permission_allow))
            val allow = awaitNode { it.viewIdResourceName?.endsWith(":id/permission_allow_button") == true }
            when (route) {
                "grant" -> click(allow)
                "dismiss" -> {
                    instrumentation.uiAutomation.waitForIdle(300, 5000)
                    assertTrue(instrumentation.uiAutomation.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK))
                }
                "deny", "settings" -> click(awaitNode { it.viewIdResourceName?.endsWith(":id/permission_deny_button") == true })
                else -> error("Unknown route $route")
            }
            if (route != "grant") {
                text(R.string.notification_permission_denied)
                if (route == "settings") {
                    click(text(R.string.notification_permission_settings))
                    click(awaitNode { it.isCheckable && !it.isChecked && it.className?.toString()?.contains("Switch") == true })
                    instrumentation.uiAutomation.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK)
                } else click(text(R.string.dialog_no))
            }
            assertTrue("Permission result not delivered", result.await(10, TimeUnit.SECONDS))
            assertEquals(route == "grant" || route == "settings", granted)
            assertEquals(granted, NotificationPermission.granted(context))
        }
    }
}
