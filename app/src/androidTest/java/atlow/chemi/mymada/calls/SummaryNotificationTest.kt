package atlow.chemi.mymada.calls

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.provider.Settings
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import atlow.chemi.mymada.InfoActivityKt
import atlow.chemi.mymada.R
import atlow.chemi.mymada.smsRes
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.CopyOnWriteArrayList

/** Real OS source app; run separately with native-only, RN-only, then both listener grants. */
class SummaryNotificationTest {
    @org.junit.Before fun fixture() = configureCallFixture()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private fun shell(command: String) = ParcelFileDescriptor.AutoCloseInputStream(
        instrumentation.uiAutomation.executeShellCommand(command)).bufferedReader().use { it.readText() }
    private fun await(check: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + 10000
        while (!check() && SystemClock.uptimeMillis() < deadline) SystemClock.sleep(30)
        assertTrue(check())
    }
    private fun popup(): smsRes? {
        var result: smsRes? = null
        instrumentation.runOnMainSync {
            result = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
                .filterIsInstance<smsRes>().singleOrNull()
        }
        return result
    }
    private fun text(activity: smsRes?): String? {
        var result: String? = null
        instrumentation.runOnMainSync { result = activity?.findViewById<TextView>(R.id.Address)?.text?.toString() }
        return result
    }
    private fun source(kind: String, id: Int = 4100) = context.sendBroadcast(Intent()
        .setComponent(ComponentName("atlow.mymadaadmin", "atlow.mymadaadmin.Source"))
        .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES).putExtra("kind", kind).putExtra("id", id))

    @Test fun osSummariesDoNotReplaceCallsButMeaningfulSummariesAndRepeatsStillAlert() {
        assertTrue("Harness must grant overlay access", Settings.canDrawOverlays(context))
        if (Build.VERSION.SDK_INT >= 33) shell("pm grant ${context.packageName} android.permission.POST_NOTIFICATIONS")
        // Harness revokes before instrumentation. Grant only now, after its force-stop;
        // killing a pending API24 bind can leave system_server's registration stuck.
        InstrumentationRegistry.getArguments().getString("listeners")?.let { grants ->
            if (Build.VERSION.SDK_INT >= 27) {
                grants.split(':').forEach { shell("cmd notification allow_listener $it") }
            } else shell("settings put secure enabled_notification_listeners $grants")
        }
        val selected = requireNotNull(ListenerAccess.selected(context))
        ListenerAccess.rebind(context)
        await { shell("dumpsys notification").substringAfter("Live notification listeners")
            .substringBefore("Snoozed notification listeners").contains(selected.flattenToString()) }
        source("clear")
        await { shell("dumpsys notification").lineSequence().none {
            it.trimStart().startsWith("NotificationRecord(") && it.contains("pkg=atlow.mymadaadmin ")
        } }
        val preferences = context.getSharedPreferences("Settings", 0)
        preferences.edit().putBoolean("Mapp", true).putBoolean("win", true).putBoolean("not", true)
            .putBoolean("cut", false).putBoolean("Nsou", false).putBoolean("Nvib", false)
            .putBoolean("loop", false).putBoolean("killS", false).commit()
        val received = CopyOnWriteArrayList<String>()
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.getStringExtra("pack") == "atlow.mymadaadmin") received += intent.getStringExtra("text")!!
            }
        }
        val broadcasts = LocalBroadcastManager.getInstance(context)
        broadcasts.registerReceiver(receiver, android.content.IntentFilter("MADA_APP"))
        ActivityScenario.launch(InfoActivityKt::class.java).use {
            try {
                shell("input keyevent KEYCODE_HOME")
                val child = "הודעת צוות בדיקה 4101"
                source("child", 4101)
                await { received.size == 1 && text(popup()) == child }
                val original = requireNotNull(popup())
                source("empty-summary")
                await { shell("dumpsys notification").lineSequence().any {
                    it.trimStart().startsWith("NotificationRecord(") && it.contains("pkg=atlow.mymadaadmin ") && it.contains("id=4100 ")
                } }
                SystemClock.sleep(700)
                assertEquals(listOf(child), received.toList())
                assertSame("Summary must not recreate/replace the real popup", original, popup())
                assertEquals(child, text(popup()))

                source("meaningful-summary")
                await { received.size == 2 && text(popup()) == "הודעת צוות בדיקה 4100" }
                repeat(2) { index ->
                    source("child", 4101)
                    await { received.size == index + 3 && text(popup()) == child }
                }
                source("malformed", 4102)
                await { received.size == 5 && text(popup()) == "תקלה" }
                source("title-only-summary")
                await { received.size == 6 && text(popup()) == "תקלה" }
                source("expanded-summary")
                await { received.size == 7 && text(popup()) == "הודעת צוות מורחבת" }

                source("clear")
                SystemClock.sleep(700)
                received.clear()
                // Enough independent children for Android's own auto-group header on both runtimes.
                for (id in 4201..4208) {
                    source("event", id)
                    await { text(popup()) == "הודעת צוות בדיקה $id" }
                }
                SystemClock.sleep(1000)
                val grouped = shell("dumpsys notification --noredact")
                context.openFileOutput("summary-regression-notifications.txt", Context.MODE_PRIVATE)
                    .bufferedWriter().use { it.write(grouped) }
                assertTrue("Android must actually generate the source auto-group summary", grouped.lineSequence().any {
                    it.contains("NotificationRecord(") && it.contains("pkg=atlow.mymadaadmin ") &&
                        (it.contains("AUTOGROUP_SUMMARY") || it.contains("tag=group_key_override"))
                })
                assertEquals((4201..4208).map { "הודעת צוות בדיקה $it" }, received.toList())
                assertEquals("הודעת צוות בדיקה 4208", text(popup()))
            } finally {
                broadcasts.unregisterReceiver(receiver)
                preferences.edit().putBoolean("Mapp", false).commit()
                popup()?.let { activity -> instrumentation.runOnMainSync { activity.finish() } }
                source("clear")
                DetectionStatus.stop(context)
                // Unbind while this process is alive, before instrumentation kills it.
                if (Build.VERSION.SDK_INT >= 27) ListenerAccess.components(context).forEach {
                    shell("cmd notification disallow_listener ${it.flattenToString()}")
                } else shell("settings delete secure enabled_notification_listeners")
                await { !shell("dumpsys notification").substringAfter("Live notification listeners")
                    .substringBefore("Snoozed notification listeners").contains("${context.packageName}/") }
            }
        }
    }
}
