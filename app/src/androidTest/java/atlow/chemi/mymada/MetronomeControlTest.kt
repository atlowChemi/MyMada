package atlow.chemi.mymada

import android.app.NotificationManager
import android.content.Intent
import android.media.AudioManager
import android.os.SystemClock
import android.widget.TextView
import android.accessibilityservice.AccessibilityService
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import atlow.chemi.mymada.recieversAndServices.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MetronomeControlTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    private fun await(message: String, condition: () -> Boolean) {
        val deadline = SystemClock.elapsedRealtime() + 8000
        while (SystemClock.elapsedRealtime() < deadline) {
            if (condition()) return
            Thread.sleep(25)
        }
        fail(message)
    }
    private fun scenario(): ActivityScenario<UtillitiesAndTools> {
        context.getSharedPreferences("Settings", 0).edit().putBoolean("Mapp", false)
            .putStringSet("Tools", setOf("metro")).commit()
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
                clickMetronome()
                await("Permission prompt or playback") {
                    var ready = MetroService.running
                    instrumentation.runOnMainSync {
                        val prompt = activity.supportFragmentManager.findFragmentByTag("notification-permission") as? DialogFragment
                        val dialog = prompt?.dialog as? AlertDialog
                        if (dialog != null) {
                            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).performClick()
                            ready = true
                        }
                    }
                    ready
                }
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
                await("Recreated Activity stopped after Home") {
                    var stopped = false
                    instrumentation.runOnMainSync {
                        stopped = activity in ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.STOPPED)
                    }
                    stopped
                }
                assertTrue("Background still plays", MetroService.running)
                context.startActivity(Intent(context, UtillitiesAndTools::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                await("Recreated Activity resumed and focused after returning") {
                    var focused = false
                    instrumentation.runOnMainSync {
                        focused = activity in ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED) &&
                            activity.hasWindowFocus()
                    }
                    focused
                }
                instrumentation.runOnMainSync {
                    val it = activity
                    assertNotEquals("00:00", it.findViewById<TextView>(R.id.metro_Clock).text.toString())
                }
                clickMetronome(running = true)
                await("UI Stop works even while bound / notifications denied") { !MetroService.running }
                instrumentation.runOnMainSync { }
                assertFalse(prefs.contains("start"))
                assertEquals(before, audio.getStreamVolume(AudioManager.STREAM_ALARM))
            } finally { stop() }
        }
    }

    @Test fun notificationAndWidgetActionsStopWhileUiBound() {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            instrumentation.uiAutomation.grantRuntimePermission(
                context.packageName, android.Manifest.permission.POST_NOTIFICATIONS)
        }
        assertTrue("Notification actions require notification access", manager.areNotificationsEnabled())
        scenario().use { screen ->
            try {
                screen.onActivity { MetroService.start(it) }
                // startForeground posts before the service marks playback running. Waiting only
                // for the notification can make the following Stop pass before Start is handled.
                await("Playback running and notification posted") {
                    MetroService.running && manager.activeNotifications.any { it.id == 13 }
                }
                val notification = manager.activeNotifications.first { it.id == 13 }.notification
                notification.actions.single().actionIntent.send()
                await("Notification Stop while bound") { !MetroService.running }
                context.sendBroadcast(Intent(context, ActionReceiver::class.java).putExtra("action", "widget"))
                await("Widget start") { MetroService.running }
                context.sendBroadcast(Intent(context, ActionReceiver::class.java).putExtra("action", "widget"))
                await("Widget Stop while bound") { !MetroService.running }
            } finally { stop() }
        }
    }

    @Test fun onlyNativeTileIsDiscoverableAndItsClicksTogglePlayback() {
        val services = context.packageManager.queryIntentServices(
            Intent(android.service.quicksettings.TileService.ACTION_QS_TILE).setPackage(context.packageName), 0)
        assertEquals(listOf(MyTileService::class.java.name), services.map { it.serviceInfo.name })
        val info = services.single().serviceInfo
        assertTrue(info.enabled && info.exported)
        assertEquals("android.permission.BIND_QUICK_SETTINGS_TILE", info.permission)
        val tile = object : MyTileService() {
            fun attach() = attachBaseContext(context)
        }
        scenario().use {
            try {
                stop()
                instrumentation.runOnMainSync { tile.attach(); tile.onClick() }
                await("Native tile starts playback") { MetroService.running }
                instrumentation.runOnMainSync { tile.onClick() }
                await("Native tile stops playback") { !MetroService.running }
            } finally { stop() }
        }
    }
}
