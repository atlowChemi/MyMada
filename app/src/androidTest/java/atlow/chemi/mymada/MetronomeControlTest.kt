package atlow.chemi.mymada

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Intent
import android.media.AudioManager
import android.os.SystemClock
import android.view.View
import android.widget.TextView
import android.accessibilityservice.AccessibilityService
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import atlow.chemi.mymada.recieversAndServices.*
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MetronomeControlTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    private fun await(message: String, condition: () -> Boolean) {
        val deadline = SystemClock.elapsedRealtime() + 8000
        while (!condition() && SystemClock.elapsedRealtime() < deadline) Thread.sleep(25)
        assertTrue(message, condition())
    }
    private fun scenario(): ActivityScenario<UtillitiesAndTools> {
        context.getSharedPreferences("Settings", 0).edit().putBoolean("Mapp", false).commit()
        return ActivityScenario.launch(Intent(context, UtillitiesAndTools::class.java))
    }
    private fun stop() {
        instrumentation.runOnMainSync { MetroService.stop(context) }
        await("Stopped", { !MetroService.running })
    }

    @Test fun repeatedStartRecreationBackgroundAndAccessibleStop() {
        val audio = context.getSystemService(AudioManager::class.java)
        val before = audio.getStreamVolume(AudioManager.STREAM_ALARM)
        scenario().use { screen ->
            lateinit var activity: UtillitiesAndTools
            screen.onActivity { activity = it }
            try {
                instrumentation.runOnMainSync { activity.findViewById<View>(R.id.bpmW).performClick() }
                await("Running") { MetroService.running }
                val prefs = context.getSharedPreferences("metronome", 0)
                val start = prefs.getLong("start", 0)
                assertTrue(start in 1..SystemClock.elapsedRealtime())
                // The production pulse animation never idles. Use main-thread barriers, not onActivity's idle wait.
                instrumentation.runOnMainSync { MetroService.start(activity); MetroService.start(activity) }
                instrumentation.runOnMainSync { }
                assertEquals("Repeated start retains origin", start, prefs.getLong("start", 0))
                Thread.sleep(1100)
                val old = activity
                instrumentation.runOnMainSync { old.recreate() }
                await("New Activity after recreation") {
                    var recreated = false
                    instrumentation.runOnMainSync {
                        val resumed = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
                            .filterIsInstance<UtillitiesAndTools>().firstOrNull { it !== old }
                        if (resumed != null) { activity = resumed; recreated = true }
                    }
                    recreated
                }
                assertTrue(instrumentation.uiAutomation.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME))
                Thread.sleep(300)
                assertTrue("Background still plays", MetroService.running)
                context.startActivity(Intent(context, UtillitiesAndTools::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                Thread.sleep(600)
                instrumentation.runOnMainSync {
                    val it = activity
                    assertNotEquals("00:00", it.findViewById<TextView>(R.id.metro_Clock).text.toString())
                    val button = it.findViewById<View>(R.id.bpmW)
                    assertEquals(it.getString(R.string.MetroStop), button.contentDescription)
                    assertTrue(button.isFocusable)
                    button.performClick()
                }
                await("UI Stop works even while bound / notifications denied") { !MetroService.running }
                instrumentation.runOnMainSync { }
                assertFalse(prefs.contains("start"))
                assertEquals(before, audio.getStreamVolume(AudioManager.STREAM_ALARM))
            } finally { stop() }
        }
    }

    @Test fun notificationAndWidgetActionsStopWhileUiBound() {
        val manager = context.getSystemService(NotificationManager::class.java)
        assumeTrue("Notification denied route covered by UI test", manager.areNotificationsEnabled())
        scenario().use { screen ->
            try {
                screen.onActivity { MetroService.start(it) }
                await("Notification posted") { manager.activeNotifications.any { it.id == 13 } }
                val notification = manager.activeNotifications.first { it.id == 13 }.notification
                notification.actions.single().actionIntent.send()
                await("Notification Stop while bound") { !MetroService.running }
                context.sendBroadcast(Intent(context, ActionReceiver::class.java).putExtra("action", "widget"))
                await("Widget start") { MetroService.running }
                context.sendBroadcast(Intent(context, ActionReceiver::class.java).putExtra("action", "widget"))
                await("Widget Stop while bound") { !MetroService.running }
                val packageManager = context.packageManager
                listOf(MyTileService::class.java, MetronomeTileService::class.java).forEach {
                    val info = packageManager.getServiceInfo(ComponentName(context, it), 0)
                    assertEquals("android.permission.BIND_QUICK_SETTINGS_TILE", info.permission)
                }
            } finally { stop() }
        }
    }
}
