package atlow.chemi.mymada

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationManager
import android.content.Intent
import android.os.SystemClock
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import atlow.chemi.mymada.fragments.MetroActivity
import atlow.chemi.mymada.recieversAndServices.MetroService
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 33)
class MetronomeNotificationTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val notifications = context.getSystemService(NotificationManager::class.java)

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
        instrumentation.uiAutomation.grantRuntimePermission(
            context.packageName, Manifest.permission.POST_NOTIFICATIONS
        )
        val service = Intent(context, MetroService::class.java)
        try {
            ActivityScenario.launch(UtillitiesAndTools::class.java).use { screen ->
                awaitState("metronome binding") { MetroActivity.bound }
                lateinit var toggle: View
                screen.onActivity { toggle = it.findViewById(R.id.bpmW) }
                // ActivityScenario.onActivity waits for idle; the running clock never idles.
                instrumentation.runOnMainSync { toggle.performClick() }
                awaitState("foreground notification") {
                    MetroService.running && notifications.activeNotifications.any {
                        it.id == 13 && it.notification.flags and Notification.FLAG_FOREGROUND_SERVICE != 0
                    }
                }
                instrumentation.runOnMainSync { toggle.performClick() }
                awaitState("UI stop") { !MetroService.running }

                awaitState("rebound metronome") { MetroActivity.bound }
                instrumentation.runOnMainSync { toggle.performClick() }
                awaitState("restart notification") {
                    MetroService.running && notifications.activeNotifications.any { it.id == 13 }
                }
                val notification = notifications.activeNotifications.single { it.id == 13 }.notification
                assertTrue(notification.contentIntent.isImmutable)
                assertTrue(notification.actions.single().actionIntent.isImmutable)

                assertTrue(instrumentation.uiAutomation.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME))
                awaitState("background playback") { !MetroActivity.bound && MetroService.running }
                notification.contentIntent.send()
                awaitState("notification returns to tools") {
                    MetroActivity.bound && ActivityLifecycleMonitorRegistry.getInstance()
                        .getActivitiesInStage(Stage.RESUMED).any { it is UtillitiesAndTools }
                }
                assertTrue(instrumentation.uiAutomation.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME))
                awaitState("unbound notification stop") { !MetroActivity.bound }
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
