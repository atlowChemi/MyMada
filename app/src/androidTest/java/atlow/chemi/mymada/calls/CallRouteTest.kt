package atlow.chemi.mymada.calls

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.media.AudioManager
import android.widget.TextView
import android.widget.Button
import android.os.Build
import android.os.Process
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.core.app.PendingIntentCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.test.core.app.ActivityScenario
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.MokedPage
import atlow.chemi.mymada.NotificationService
import atlow.chemi.mymada.R
import atlow.chemi.mymada.smsRes
import atlow.chemi.mymada.home.HomeActions
import atlow.chemi.mymada.recieversAndServices.GrobootRec
import atlow.chemi.mymada.recieversAndServices.SMSMonitorService
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/** Offline disposable emulator: invokes the actual listener callback and Application receiver. */
class CallRouteTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val settings = context.getSharedPreferences("Settings", 0)
    private val manager = context.getSystemService(NotificationManager::class.java)
    private val listener = object : NotificationService() {
        fun attach(context: Context) = attachBaseContext(context)
    }

    @Before fun setup() {
        if (Build.VERSION.SDK_INT >= 33) {
            ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(
                "pm grant ${context.packageName} ${Manifest.permission.POST_NOTIFICATIONS}")).use { it.readBytes() }
        }
        settings.edit().clear().putBoolean("Mapp", true).putBoolean("not", true)
            .putBoolean("win", false).putBoolean("Nsou", false).putBoolean("Nvib", false)
            .putBoolean("cut", true).commit()
        context.stopService(Intent(context, SMSMonitorService::class.java))
        context.getSystemService(NotificationManager::class.java).cancel(78)
        manager.cancelAll()
        if (Build.VERSION.SDK_INT >= 26) manager.deleteNotificationChannel("notWithWin")
        notifications(0)
        listener.attach(context)
    }

    @After fun cleanup() {
        settings.edit().putBoolean("Mapp", false).commit()
        manager.cancelAll()
    }

    private fun pending(id: Int): PendingIntent = requireNotNull(PendingIntentCompat.getActivity(context, id,
        Intent(context, MokedPage::class.java).putExtra("fixture", id), PendingIntent.FLAG_UPDATE_CURRENT, false))

    private fun post(text: CharSequence?, source: PendingIntent?, id: Int = 1, title: CharSequence? = "*אירוע חדש*") {
        val notification = NotificationCompat.Builder(context, "source-fixture").setSmallIcon(R.drawable.ic_siren)
            .setContentTitle(title).setContentText(text).setContentIntent(source).build()
        listener.onNotificationPosted(StatusBarNotification("com.groboot.mdaemergency", "com.groboot.mdaemergency",
            id, null, Process.myUid(), Process.myPid(), 0, notification, Process.myUserHandle(), System.currentTimeMillis()))
    }

    private fun notifications(count: Int): List<Notification> {
        instrumentation.waitForIdleSync()
        val deadline = SystemClock.uptimeMillis() + 3000
        // Status78 now refreshes on real source delivery/resume; its controls have a separate suite.
        fun alerts() = manager.activeNotifications.filter { it.id != 78 }
        while (alerts().size != count && SystemClock.uptimeMillis() < deadline) SystemClock.sleep(20)
        assertEquals(count, alerts().size)
        return alerts().map { it.notification }
    }

    @Test fun queuedRepeatCallsKeepTheirOwnSourceActionsAndReplacementSetting() {
        val first = pending(1)
        val second = pending(2)
        instrumentation.runOnMainSync {
            post("הודעת צוות בדיקה א", first)
            post("הודעת צוות בדיקה ב", second, 2)
        }
        val calls = notifications(2).associateBy { it.extras.getCharSequence(Notification.EXTRA_BIG_TEXT).toString() }
        assertEquals(first, calls.getValue("הודעת צוות בדיקה א").actions.last().actionIntent)
        assertEquals(second, calls.getValue("הודעת צוות בדיקה ב").actions.last().actionIntent)
        // Re-posting the same source key/body still alerts, preserving native repeat behavior.
        instrumentation.runOnMainSync { post("הודעת צוות בדיקה ב", second, 2) }
        notifications(3)
        manager.notify(7999, NotificationCompat.Builder(context, "notWithWin")
            .setSmallIcon(R.drawable.ic_siren).setContentTitle("Unrelated control").setGroup("Other").build())
        notifications(4)
        settings.edit().putBoolean("ride", true).commit()
        instrumentation.runOnMainSync { post("הודעת צוות בדיקה ג", null, 3) }
        val remaining = notifications(2)
        assertTrue(remaining.any { it.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() == "Unrelated control" })
        val replacement = remaining.single { it.group == "Calls" }
        assertEquals("הודעת צוות בדיקה ג", replacement.extras.getCharSequence(Notification.EXTRA_BIG_TEXT).toString())
        assertEquals(1, replacement.actions.size) // dictation only; no stale details action
        assertNotNull(replacement.contentIntent)
    }

    @Test fun realRouteHonorsFilteringLegacyTypeAndMalformedPayload() {
        settings.edit().putBoolean("callT3", false).commit()
        instrumentation.runOnMainSync {
            listener.onNotificationPosted(null)
            post("בדיקה", null, title = "unknown")
            post("בדיקה על מידע זה חל חיסיון רפואי", null)
        }
        notifications(0)
        settings.edit().putBoolean("callT3", true).commit()
        instrumentation.runOnMainSync { post(null, null, title = null) }
        assertEquals("תקלה", notifications(1).single().extras.getCharSequence(Notification.EXTRA_BIG_TEXT).toString())
        settings.edit().putBoolean("Mapp", false).commit()
        instrumentation.runOnMainSync { post("הודעת צוות בדיקה", pending(4)) }
        notifications(1)
    }

    @Test fun blockedPopupLeavesPerCallTapFallbackUntilPopupActuallyOpens() {
        settings.edit().putBoolean("win", true).putBoolean("NsouT1", false).putBoolean("NvibT1", false).commit()
        var attempts = 0
        val blocked = object : ContextWrapper(context) {
            override fun startActivity(intent: Intent) { attempts++ } // Android BAL returns without launching or throwing.
        }
        val source = pending(85)
        instrumentation.runOnMainSync {
            GrobootRec().onReceive(blocked, Intent("MADA_APP").putExtra("callT", 1)
                .putExtra("text", "בדיקת רקע").putExtra("sourceIntent", source))
        }
        assertEquals(1, attempts)
        val fallback = notifications(1).single()
        ActivityScenario.launch<smsRes>(CallEvent(1, "בדיקת פתיחה", null).popupIntent(context, -1, false)).use { scenario ->
            fallback.contentIntent.send()
            val deadline = SystemClock.uptimeMillis() + 3000
            var opened = false
            while (!opened && SystemClock.uptimeMillis() < deadline) {
                scenario.onActivity {
                    opened = CallEvent.from(it.intent).text == "בדיקת רקע"
                    if (opened) assertEquals(source, CallEvent.from(it.intent).source)
                }
                if (!opened) SystemClock.sleep(20)
            }
            assertTrue(opened)
            notifications(0)
        }
        settings.edit().putBoolean("not", false).commit()
        instrumentation.runOnMainSync { GrobootRec().onReceive(blocked, Intent("MADA_APP").putExtra("text", "מושבת")) }
        notifications(0) // Explicit notification opt-out is still respected.
    }

    @Test fun popupColdWarmRecreationAndDetailsUsePerCallState() {
        settings.edit().putBoolean("win", true).putBoolean("NsouT1", false).putBoolean("NvibT1", false)
            .putBoolean("killS", true).putString("timeK", "2").commit()
        val delivered = CountDownLatch(1)
        val action = "${context.packageName}.CALL_DETAILS_FIXTURE"
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) { delivered.countDown() }
        }
        ContextCompat.registerReceiver(context, receiver, IntentFilter(action), ContextCompat.RECEIVER_NOT_EXPORTED)
        val details = PendingIntentCompat.getBroadcast(context, 71, Intent(action).setPackage(context.packageName),
            PendingIntent.FLAG_UPDATE_CURRENT, false)
        try {
            ActivityScenario.launch<smsRes>(CallEvent(1, "בדיקה ראשונה", pending(70)).popupIntent(context, 0, false)).use { scenario ->
                scenario.onActivity { assertEquals("בדיקה ראשונה", it.findViewById<TextView>(R.id.Address).text.toString()) }
                settings.edit().putBoolean("killS", false).commit()
                instrumentation.runOnMainSync { post("בדיקה שנייה", details, 71) }
                val deadline = SystemClock.uptimeMillis() + 3000
                var replaced = false
                while (!replaced && SystemClock.uptimeMillis() < deadline) {
                    instrumentation.runOnMainSync {
                        replaced = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
                            .filterIsInstance<smsRes>().any { it.findViewById<TextView>(R.id.Address).text.toString() == "בדיקה שנייה" }
                    }
                    if (!replaced) SystemClock.sleep(20)
                }
                assertTrue("Warm listener delivery must reach the resumed popup", replaced)
                scenario.onActivity { assertEquals("בדיקה שנייה", it.findViewById<TextView>(R.id.Address).text.toString()) }
                scenario.recreate()
                // The first call's auto-stop must not close/cancel the replacement.
                SystemClock.sleep(2100)
                scenario.onActivity {
                    assertEquals("בדיקה שנייה", it.findViewById<TextView>(R.id.Address).text.toString())
                    assertEquals(details, CallEvent.from(it.intent).source)
                    it.findViewById<Button>(R.id.buttonI).performClick()
                }
                assertTrue(delivered.await(3, TimeUnit.SECONDS))
            }
        } finally { context.unregisterReceiver(receiver) }
    }

    @Test fun failedAudioRestoresVolumeImmediatelyAndAcrossRecreation() {
        val audio = context.getSystemService(AudioManager::class.java)
        val original = audio.getStreamVolume(AudioManager.STREAM_ALARM)
        // API37 enforces a non-zero minimum alarm volume; use a valid distinct level.
        val changed = if (original == 1) 2 else 1
        settings.edit().putBoolean("win", true).putBoolean("NsouT1", true).putBoolean("NvibT1", false)
            .putBoolean("NappT1", true).putString("NuRiT1", "file:///missing-call-fixture.ogg")
            .putBoolean("volumeApp", true).putInt("volumeAppNum", changed).commit()
        try {
            ActivityScenario.launch<smsRes>(CallEvent(1, "בדיקת שמע", null).popupIntent(context, 0, false)).use { scenario ->
                scenario.onActivity { assertEquals(original, audio.getStreamVolume(AudioManager.STREAM_ALARM)) }
                scenario.recreate()
                scenario.onActivity { assertEquals(original, audio.getStreamVolume(AudioManager.STREAM_ALARM)) }
            }
            assertEquals(original, audio.getStreamVolume(AudioManager.STREAM_ALARM))
        } finally { audio.setStreamVolume(AudioManager.STREAM_ALARM, original, 0) }
    }

    @Test fun homeTestCallDialogUsesRealReceiverAndPerTypeFilters() {
        settings.edit().putBoolean("NsouT1", false).putBoolean("NvibT1", false).commit()
        // Use a foreground FragmentActivity to host the unchanged Home dialog.
        ActivityScenario.launch<smsRes>(CallEvent(1, "בדיקה", null).popupIntent(context, 0, false)).use { scenario ->
            fun choose(button: Int) {
                scenario.onActivity {
                    HomeActions.ActionPrompt.show(it, "test")
                    it.supportFragmentManager.executePendingTransactions()
                    val prompt = it.supportFragmentManager.findFragmentByTag("home-action:test") as HomeActions.ActionPrompt
                    (prompt.requireDialog() as AlertDialog).getButton(button).performClick()
                }
                instrumentation.waitForIdleSync()
            }
            choose(AlertDialog.BUTTON_POSITIVE)
            assertEquals(context.getString(R.string.TEAMTest), notifications(1).single().extras.getCharSequence(Notification.EXTRA_BIG_TEXT).toString())
            settings.edit().putBoolean("callT3", false).commit()
            choose(AlertDialog.BUTTON_POSITIVE)
            notifications(1)
            choose(AlertDialog.BUTTON_NEGATIVE)
            assertTrue(notifications(2).any { it.extras.getCharSequence(Notification.EXTRA_BIG_TEXT).toString() == context.getString(R.string.CONANTest) })
        }
    }
}
