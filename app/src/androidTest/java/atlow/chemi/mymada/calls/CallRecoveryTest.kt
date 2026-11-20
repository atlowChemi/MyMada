package atlow.chemi.mymada.calls

import android.app.Notification
import android.content.Intent
import android.os.Process
import android.os.Build
import android.os.ParcelFileDescriptor
import android.service.notification.StatusBarNotification
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test

/** Persistence/queue policy; external owned-device harness separately kills the actual listener. */
class CallRecoveryTest {
    @Test fun baselinePendingAckStopAndRebootPolicies() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val settings = context.getSharedPreferences("Settings", 0)
        val ledger = context.getSharedPreferences("CallRecovery", 0)
        fun shell(command: String) = ParcelFileDescriptor.AutoCloseInputStream(
            InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command)).use { it.readBytes() }
        val component = ListenerAccess.components(context).first().flattenToString()
        if (Build.VERSION.SDK_INT >= 27) shell("cmd notification allow_listener $component")
        else shell("settings put secure enabled_notification_listeners $component")
        if (Build.VERSION.SDK_INT >= 33) shell("pm grant ${context.packageName} android.permission.POST_NOTIFICATIONS")
        assertNotNull("Harness grants a listener", ListenerAccess.selected(context))
        try {
            // Recovery state is main-thread owned. Keep the policy sequence on that
            // thread so a real listener bind cannot baseline between assertions.
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                settings.edit().putBoolean("Mapp", true).commit()
                CallRecovery.stop(context)
                try {
                    assertFalse("First connection baselines", CallRecovery.connected(context))
                    assertTrue("Same boot/grants reconnects", CallRecovery.connected(context))
                    // Deterministic recent revision after the baseline, without sleeping at an age boundary.
                    val now = System.currentTimeMillis()
                    ledger.edit().putLong("floor", now - 10).commit()
                    val notification = Notification.Builder(context).setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setWhen(now).setContentTitle("*אירוע חדש*").setContentText("הודעת צוות בדיקה").build()
                    val posted = StatusBarNotification("atlow.mymadaadmin", "atlow.mymadaadmin", 1, "tag",
                        Process.myUid(), 1, 0, notification, Process.myUserHandle(), now)
                    val live = Intent("MADA_APP")
                    assertTrue(CallRecovery.track(context, posted, "title", "body", live, false))
                    assertFalse("Queued live callback must not also reconcile", CallRecovery.track(context, posted, "title", "body", Intent(), true))
                    CallRecovery.handled(context, live)
                    assertEquals(1, ledger.getStringSet("handled", emptySet())!!.size)
                    assertTrue(CallRecovery.connected(context))
                    assertFalse("Durable handled revision", CallRecovery.track(context, posted, "title", "body", Intent(), true))
                    assertTrue("Live identical repeat still alerts", CallRecovery.track(context, posted, "title", "body", Intent(), false))
                    val update = Intent()
                    assertTrue("Same ID/post-time but changed content is a different revision", CallRecovery.track(context, posted, "title", "updated", update, true))
                    assertTrue(CallRecovery.allow(context, update))
                    CallRecovery.stop(context)
                    assertFalse("Stop invalidates queued recovery", CallRecovery.allow(context, update))
                    assertFalse(CallRecovery.connected(context))
                    ledger.edit().putInt("boot", -1).commit()
                    assertFalse("Reboot does not replay history", CallRecovery.connected(context))
                    assertFalse("Existing revision predates new baseline", CallRecovery.track(context, posted, "title", "updated", Intent(), true))
                } finally {
                    settings.edit().putBoolean("Mapp", false).commit()
                    CallRecovery.stop(context)
                }
            }
        } finally {
            if (Build.VERSION.SDK_INT >= 27) shell("cmd notification disallow_listener $component")
            else shell("settings delete secure enabled_notification_listeners")
        }
    }
}
