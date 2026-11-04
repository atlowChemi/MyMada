package atlow.chemi.mymada

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import atlow.chemi.mymada.recieversAndServices.MetroService
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 24)
class MetronomeNotificationTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val notifications = context.getSystemService(NotificationManager::class.java)
    private fun toolsResumed() = ActivityLifecycleMonitorRegistry.getInstance()
        .getActivitiesInStage(Stage.RESUMED).any { it is UtillitiesAndTools }

    // Observe asynchronous service/lifecycle changes on their owning main thread.
    private fun awaitState(description: String, condition: () -> Boolean) {
        val deadline = SystemClock.elapsedRealtime() + 10_000
        while (SystemClock.elapsedRealtime() < deadline) {
            var ready = false
            instrumentation.runOnMainSync { ready = condition() }
            if (ready) return
            SystemClock.sleep(50)
        }
        throw AssertionError("Timed out: $description")
    }

    @Test
    fun startStopAndNotificationControls() {
        // Permission-flow UX belongs to #24/#25; test delivery with permission granted.
        val grantNotifications = InstrumentationRegistry.getArguments().getString("grantNotifications", "true") != "false"
        if (Build.VERSION.SDK_INT >= 33) {
            if (grantNotifications) {
                instrumentation.uiAutomation.grantRuntimePermission(
                    context.packageName, Manifest.permission.POST_NOTIFICATIONS
                )
            } else {
                assertTrue(context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED)
            }
        }
        // Follow normal startup, which creates notification channels on a fresh install.
        ActivityScenario.launch(HomePage::class.java).close()
        val service = Intent(context, MetroService::class.java)
        try {
            ActivityScenario.launch(UtillitiesAndTools::class.java).use { screen ->
                awaitState("metronome visible") { toolsResumed() }
                lateinit var toggle: View
                screen.onActivity { toggle = it.findViewById(R.id.bpmW) }
                // ActivityScenario.onActivity waits for idle; the running clock never idles.
                instrumentation.runOnMainSync { toggle.performClick() }
                if (!grantNotifications && Build.VERSION.SDK_INT >= 33) {
                    awaitState("playback with notification permission denied") { MetroService.running }
                    assertFalse(notifications.areNotificationsEnabled())
                    instrumentation.runOnMainSync { toggle.performClick() }
                    awaitState("UI stop with permission denied") { !MetroService.running }
                    return@use
                }
                awaitState("foreground notification") {
                    MetroService.running && notifications.activeNotifications.any {
                        it.id == 13 && it.notification.flags and Notification.FLAG_FOREGROUND_SERVICE != 0
                    }
                }
                instrumentation.runOnMainSync { toggle.performClick() }
                awaitState("UI stop") { !MetroService.running }

                awaitState("metronome visible") { toolsResumed() }
                instrumentation.runOnMainSync { toggle.performClick() }
                awaitState("restart notification") {
                    MetroService.running && notifications.activeNotifications.any { it.id == 13 }
                }
                val notification = notifications.activeNotifications.single { it.id == 13 }.notification
                if (Build.VERSION.SDK_INT >= 31) {
                    assertTrue(notification.contentIntent.isImmutable)
                    assertTrue(notification.actions.single().actionIntent.isImmutable)
                }

                assertTrue(instrumentation.uiAutomation.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME))
                awaitState("background playback") { !toolsResumed() && MetroService.running }
                notification.contentIntent.send()
                awaitState("notification returns to tools") {
                    toolsResumed()
                }
                assertTrue(instrumentation.uiAutomation.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME))
                awaitState("background notification stop") { !toolsResumed() }
                notification.actions.single().actionIntent.send()
                awaitState("notification action stops service") {
                    !MetroService.running && notifications.activeNotifications.none { it.id == 13 }
                }
                assertFalse(MetroService.running)
            }
        } finally {
            context.stopService(service)
        }
    }
}
