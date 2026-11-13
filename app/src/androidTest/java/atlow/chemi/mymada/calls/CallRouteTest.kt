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
import androidx.test.runner.lifecycle.ActivityLifecycleCallback
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.MokedPage
import atlow.chemi.mymada.NotificationService
import atlow.chemi.mymada.R
import atlow.chemi.mymada.smsRes
import atlow.chemi.mymada.home.HomeActions
import atlow.chemi.mymada.recieversAndServices.GrobootRec
import atlow.chemi.mymada.recieversAndServices.FloatingWidgetService
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
        // Non-popup notifications also start overlays when the device grants that permission.
        // Drain their starts, then destroy their player before the next fixture changes volume.
        instrumentation.runOnMainSync { context.stopService(Intent(context, FloatingWidgetService::class.java)) }
        instrumentation.waitForIdleSync()
    }

    private fun pending(id: Int): PendingIntent = requireNotNull(PendingIntentCompat.getActivity(context, id,
        Intent(context, MokedPage::class.java).putExtra("fixture", id), PendingIntent.FLAG_UPDATE_CURRENT, false))

    private fun post(text: CharSequence?, source: PendingIntent?, id: Int = 1, title: CharSequence? = "*אירוע חדש*",
                     expanded: CharSequence? = null, summary: Boolean = false, tag: String? = null,
                     pack: String = "com.groboot.mdaemergency"): StatusBarNotification {
        val notification = NotificationCompat.Builder(context, "source-fixture").setSmallIcon(R.drawable.ic_siren)
            .setContentTitle(title).setContentText(text).setContentIntent(source)
            .apply {
                if (expanded != null) setStyle(NotificationCompat.BigTextStyle().bigText(expanded))
                if (summary) setGroup("source-calls").setGroupSummary(true)
            }.build()
        val posted = StatusBarNotification(pack, pack,
            id, tag, Process.myUid(), Process.myPid(), 0, notification, Process.myUserHandle(), System.currentTimeMillis())
        listener.onNotificationPosted(posted)
        return posted
    }

    private fun notifications(count: Int, ready: (List<Notification>) -> Boolean = { true }): List<Notification> {
        instrumentation.waitForIdleSync()
        val deadline = SystemClock.uptimeMillis() + 3000
        // Status78 now refreshes on real source delivery/resume; its controls have a separate suite.
        // This fixture posts untagged notifications. Android's tagged aggregate
        // summary is system-owned and is not an additional delivered call.
        fun alerts() = manager.activeNotifications.filter { it.id != 78 && it.tag == null }.map { it.notification }
        var current = alerts()
        while ((current.size != count || !ready(current)) && SystemClock.uptimeMillis() < deadline) {
            SystemClock.sleep(20)
            current = alerts()
        }
        assertEquals(count, current.size)
        assertTrue("Expected notification content; received ${current.map { it.extras.getCharSequence(Notification.EXTRA_BIG_TEXT) }}", ready(current))
        return current
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
        // A new malformed identity still falls back; the filtered valid source above is a different key.
        instrumentation.runOnMainSync { post(null, null, id = 9, title = null) }
        assertEquals("תקלה", notifications(1).single().extras.getCharSequence(Notification.EXTRA_BIG_TEXT).toString())
        settings.edit().putBoolean("Mapp", false).commit()
        instrumentation.runOnMainSync { post("הודעת צוות בדיקה", pending(4)) }
        notifications(1)
    }

    @Test fun expandedUpdatesAndSummariesKeepTheirBodyAndLiveRepeats() {
        settings.edit().putBoolean("ride", true).commit()
        val original = pending(40)
        val updated = pending(41)
        fun delivered(text: String, source: PendingIntent, count: Int = 1) {
            val calls = notifications(count) { current -> current.any {
                it.extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() == text &&
                    it.actions.last().actionIntent == source
            } }
            assertTrue("Expected $text; received ${calls.map { it.extras.getCharSequence(Notification.EXTRA_BIG_TEXT) }}", calls.any {
                it.extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() == text &&
                    it.actions.last().actionIntent == source
            })
            // These are independent payload variants, not a burst-throughput test.
            // Android sheds rapid same-ID updates above its notification rate limit.
            SystemClock.sleep(1000)
        }
        instrumentation.runOnMainSync { post("הודעת צוות ראשונה", original, 40) }
        delivered("הודעת צוות ראשונה", original)
        // Same source key, now BigTextStyle only: this is content, not a malformed event.
        instrumentation.runOnMainSync { post(null, updated, 40, expanded = "הודעת צוות מורחבת") }
        delivered("הודעת צוות מורחבת", updated)
        instrumentation.runOnMainSync { post("", updated, 40, expanded = "הודעת צוות עדכון") }
        delivered("הודעת צוות עדכון", updated)
        // Preserve the existing nonempty short-body precedence when both fields exist.
        instrumentation.runOnMainSync { post("הודעת צוות קצרה", original, 40, expanded = "טקסט מורחב") }
        delivered("הודעת צוות קצרה", original)
        instrumentation.runOnMainSync { post(null, updated, 42, title = null, expanded = "הודעת צוות סיכום", summary = true) }
        delivered("הודעת צוות סיכום", updated)
        // Meaningful summaries and identical live key/body callbacks must still alert each time.
        settings.edit().putBoolean("ride", false).commit()
        repeat(2) { index ->
            instrumentation.runOnMainSync { post(null, updated, 42, title = null, expanded = "הודעת צוות סיכום", summary = true) }
            delivered("הודעת צוות סיכום", updated, index + 2)
        }
        settings.edit().putBoolean("ride", true).commit()
        // No blanket title-only/summary suppression: the existing malformed fallback remains.
        instrumentation.runOnMainSync { post(null, original, 43, summary = true) }
        delivered("תקלה", original)
    }

    @Test fun metadataOnlyAutoSummaryDoesNotCreateCallsButLiveRepeatsStillDo() {
        val source = pending(81062)
        val body = "הודעת צוות בדיקת סיכום"
        instrumentation.runOnMainSync { post(body, source, 81062) }
        notifications(1)
        // Phone events 29/34: same summary identity, flags 1794 then 1792, navigation PI only.
        // The two salted extra names are unknown; synthetic metadata reproduces their guard effect.
        for (flags in listOf(1794, 1792)) {
            val summary = NotificationCompat.Builder(context, "source-fixture")
                .setSmallIcon(R.drawable.ic_siren).setGroup("fixture-autogroup")
                .setGroupSummary(true).setContentIntent(pending(Int.MAX_VALUE)).build().apply {
                    this.flags = flags
                    extras.clear()
                    extras.putParcelable("android.appInfo", context.applicationInfo)
                    extras.putBoolean("android.reduced.images", true)
                    extras.putBoolean("fixture.metadata.one", true)
                    extras.putInt("fixture.metadata.two", 1)
                }
            val posted = StatusBarNotification("com.groboot.mdaemergency", "com.groboot.mdaemergency",
                Int.MAX_VALUE, "fixture-autogroup", Process.myUid(), Process.myPid(), 0,
                summary, Process.myUserHandle(), System.currentTimeMillis())
            instrumentation.runOnMainSync { listener.onNotificationPosted(posted) }
            val unchanged = notifications(1).single()
            assertEquals(body, unchanged.extras.getCharSequence(Notification.EXTRA_BIG_TEXT).toString())
            assertEquals(source, unchanged.actions.last().actionIntent)
        }
        val retained = notifications(1).single()
        assertEquals(body, retained.extras.getCharSequence(Notification.EXTRA_BIG_TEXT).toString())
        assertEquals(source, retained.actions.last().actionIntent)
        instrumentation.runOnMainSync { post(body, source, 81062) }
        assertTrue(notifications(2).all { it.extras.getCharSequence(Notification.EXTRA_BIG_TEXT).toString() == body &&
            it.actions.last().actionIntent == source })
    }

    @Test fun renderableSummaryFieldsRetainExistingFallbackPolicy() {
        val source = pending(82000)
        val content: List<(Notification) -> Unit> = listOf(
            { it.extras.putCharSequence(Notification.EXTRA_TITLE, "*אירוע חדש*") },
            { it.extras.putCharSequence(Notification.EXTRA_TITLE_BIG, "fixture title") },
            { it.extras.putCharSequence(Notification.EXTRA_SUB_TEXT, "fixture subtext") },
            { it.extras.putCharSequence(Notification.EXTRA_SUMMARY_TEXT, "fixture summary") },
            { it.extras.putCharSequenceArray(Notification.EXTRA_TEXT_LINES, arrayOf("fixture line")) },
            { it.extras.putParcelableArray(Notification.EXTRA_MESSAGES, arrayOf(android.os.Bundle().apply {
                putCharSequence("text", "fixture message")
            })) },
            { it.tickerText = "fixture ticker" },
            { it.actions = arrayOf(Notification.Action.Builder(R.drawable.ic_siren, "fixture action", source).build()) },
            { it.contentView = android.widget.RemoteViews(context.packageName, android.R.layout.simple_list_item_1) },
            { it.bigContentView = android.widget.RemoteViews(context.packageName, android.R.layout.simple_list_item_1) },
            { it.headsUpContentView = android.widget.RemoteViews(context.packageName, android.R.layout.simple_list_item_1) },
            { it.fullScreenIntent = source }
        )
        content.forEachIndexed { index, populate ->
            val summary = NotificationCompat.Builder(context, "source-fixture")
                .setSmallIcon(R.drawable.ic_siren).setGroup("fixture-content").setGroupSummary(true)
                .setContentIntent(source).build().apply { extras.clear(); populate(this) }
            instrumentation.runOnMainSync {
                listener.onNotificationPosted(StatusBarNotification("com.groboot.mdaemergency", "com.groboot.mdaemergency",
                    82000 + index, null, Process.myUid(), Process.myPid(), 0, summary,
                    Process.myUserHandle(), System.currentTimeMillis()))
            }
            assertEquals(index + 1, notifications(index + 1).count {
                it.extras.getCharSequence(Notification.EXTRA_BIG_TEXT).toString() == "תקלה" &&
                    it.actions.last().actionIntent == source
            })
        }
        val empty = NotificationCompat.Builder(context, "source-fixture")
            .setSmallIcon(R.drawable.ic_siren).setGroup("fixture-content").setGroupSummary(true)
            .setContentIntent(source).build().apply {
                extras.clear()
                extras.putCharSequence(Notification.EXTRA_TITLE, "")
                extras.putCharSequence(Notification.EXTRA_TEXT, "")
                extras.putCharSequence(Notification.EXTRA_BIG_TEXT, "")
                extras.putCharSequenceArray(Notification.EXTRA_TEXT_LINES, arrayOf("", ""))
                tickerText = ""
            }
        instrumentation.runOnMainSync {
            listener.onNotificationPosted(StatusBarNotification("com.groboot.mdaemergency", "com.groboot.mdaemergency",
                83000, null, Process.myUid(), Process.myPid(), 0, empty,
                Process.myUserHandle(), System.currentTimeMillis()))
        }
        notifications(content.size)
    }

    @Test fun expandedSummaryKeepsBodyInsteadOfMalformedFallback() {
        instrumentation.runOnMainSync { post(null, pending(44), 44, title = null, expanded = "הודעת צוות סיכום", summary = true) }
        assertEquals("הודעת צוות סיכום", notifications(1).single().extras.getCharSequence(Notification.EXTRA_BIG_TEXT).toString())
    }

    @Test fun malformedUpdatesPreserveOnlyTheirActiveSourceAndValidRepeatsStillAlert() {
        val original = pending(46)
        lateinit var posted: StatusBarNotification
        instrumentation.runOnMainSync { posted = post("הודעת צוות מקורית", original, 46) }
        val first = notifications(1).single()
        for (body in listOf(null, "", "  ")) {
            instrumentation.runOnMainSync { post(body, pending(47), 46) }
            val retained = notifications(1).single()
            assertEquals("הודעת צוות מקורית", retained.extras.getCharSequence(Notification.EXTRA_BIG_TEXT).toString())
            assertEquals(original, retained.actions.last().actionIntent)
            assertEquals(first.contentIntent, retained.contentIntent)
        }
        // Neither another id, another tag nor another package may inherit this call's protection/content.
        instrumentation.runOnMainSync { post(null, null, 47) }
        assertEquals(1, notifications(2).count { it.extras.getCharSequence(Notification.EXTRA_BIG_TEXT).toString() == "תקלה" })
        instrumentation.runOnMainSync { post(null, null, 46, tag = "other") }
        notifications(3)
        instrumentation.runOnMainSync { post(null, null, 46, pack = "com.mda.health") }
        notifications(4)
        instrumentation.runOnMainSync { post("הודעת צוות מקורית", original, 46) }
        assertEquals(2, notifications(5).count { it.extras.getCharSequence(Notification.EXTRA_BIG_TEXT).toString() == "הודעת צוות מקורית" })
        instrumentation.runOnMainSync {
            listener.onNotificationRemoved(posted)
            post(null, null, 46)
        }
        assertEquals(4, notifications(6).count { it.extras.getCharSequence(Notification.EXTRA_BIG_TEXT).toString() == "תקלה" })
    }

    @Test fun blockedPopupRetainsNotificationUntilExplicitShadeTap() {
        settings.edit().putBoolean("win", true).putBoolean("NsouT1", false).putBoolean("NvibT1", false).commit()
        var attempts = 0
        val blocked = object : ContextWrapper(context) {
            override fun startActivity(intent: Intent) { attempts++ } // Android BAL returns without launching or throwing.
        }
        instrumentation.runOnMainSync {
            GrobootRec().onReceive(blocked, Intent("MADA_APP").putExtra("callT", 1)
                .putExtra("text", "בדיקת רקע"))
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
                    if (opened) assertNull(CallEvent.from(it.intent).source)
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

    @Test fun automaticPopupCloseAndTimeoutRetainNotificationsUntilExplicitActions() {
        settings.edit().putBoolean("win", true).putBoolean("NsouT1", false).putBoolean("NvibT1", false).commit()
        val launches = mutableListOf<Intent>()
        val blocked = object : ContextWrapper(context) {
            override fun startActivity(intent: Intent) { launches += intent }
        }
        val received = java.util.concurrent.CopyOnWriteArrayList<Int>()
        val action = "${context.packageName}.RETAINED_SOURCE_FIXTURE"
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) { received += intent.getIntExtra("source", -1) }
        }
        ContextCompat.registerReceiver(context, receiver, IntentFilter(action), ContextCompat.RECEIVER_NOT_EXPORTED)
        try {
            for (id in 1..3) {
                val source = PendingIntentCompat.getBroadcast(context, 950 + id,
                    Intent(action).setPackage(context.packageName).putExtra("source", id), PendingIntent.FLAG_UPDATE_CURRENT, false)
                instrumentation.runOnMainSync {
                    GrobootRec().onReceive(blocked, Intent("MADA_APP").putExtra("callT", 1)
                        .putExtra("text", "מקרה שמור $id").putExtra("sourceIntent", source))
                }
                notifications(id)
            }
            manager.notify(7999, NotificationCompat.Builder(context, "notWithWin")
                .setSmallIcon(R.drawable.ic_siren).setContentTitle("Unrelated control").setGroup("Other").build())
            notifications(4)
            // Use the exact automatic launch Intent emitted after notify, without the shade flag.
            ActivityScenario.launch<smsRes>(launches[0]).use { scenario ->
                scenario.onActivity { assertEquals("מקרה שמור 1", it.findViewById<TextView>(R.id.Address).text.toString()) }
                notifications(4)
                scenario.recreate()
                notifications(4)
            }
            notifications(4) // Ordinary close is not notification acknowledgment.
            settings.edit().putBoolean("killS", true).putString("timeK", "1").commit()
            ActivityScenario.launch<smsRes>(launches[1]).use { scenario ->
                val deadline = SystemClock.uptimeMillis() + 3000
                while (scenario.state != androidx.lifecycle.Lifecycle.State.DESTROYED && SystemClock.uptimeMillis() < deadline) SystemClock.sleep(20)
                assertEquals(androidx.lifecycle.Lifecycle.State.DESTROYED, scenario.state)
            }
            notifications(4) // Auto-stop closes the popup only.
            settings.edit().putBoolean("killS", false).commit()
            ActivityScenario.launch<smsRes>(launches[0]).use { scenario ->
                scenario.onActivity { it.findViewById<Button>(R.id.buttonI).performClick() }
            }
            notifications(3)
            assertEquals(listOf(1), received.toList())
            assertFalse(manager.activeNotifications.any { it.id == launches[0].getIntExtra("notNum", -1) })
            val second = manager.activeNotifications.single { it.id == launches[1].getIntExtra("notNum", -1) }
            second.notification.contentIntent.send()
            val deadline = SystemClock.uptimeMillis() + 3000
            while (received.size < 2 && SystemClock.uptimeMillis() < deadline) SystemClock.sleep(20)
            assertEquals(listOf(1, 2), received.toList())
            val remaining = notifications(2)
            assertTrue(remaining.any { it.group == "Other" })
            assertEquals("מקרה שמור 3", remaining.single { it.group == "Calls" }.extras.getCharSequence(Notification.EXTRA_BIG_TEXT).toString())
        } finally { context.unregisterReceiver(receiver) }
    }

    @Test fun shadeTapSendsEachSourceOnceWithoutRestartingAlertColdAndWarm() {
        settings.edit().putBoolean("win", true).putBoolean("Nsou", true).putBoolean("Nvib", true)
            .putBoolean("NsouT1", true).putBoolean("NvibT1", true).putBoolean("volumeApp", false).commit()
        val delivered = java.util.concurrent.CopyOnWriteArrayList<Int>()
        val tapWindows = java.util.concurrent.CopyOnWriteArrayList<Boolean>()
        val destroyedTaps = java.util.concurrent.atomic.AtomicInteger()
        var incomingPlaybackRequested = false
        var incomingVibrationInitialized = false
        val action = "${context.packageName}.SHADE_SOURCE_FIXTURE"
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) { delivered += intent.getIntExtra("source", -1) }
        }
        fun field(activity: smsRes, name: String): Any? = smsRes::class.java.getDeclaredField(name)
            .apply { isAccessible = true }.get(activity)
        val lifecycle = ActivityLifecycleCallback { activity, stage ->
            if (activity is smsRes && stage == Stage.STARTED && CallEvent.from(activity.intent).text == "התראה חדשה") {
                // Observe initialization before an emulator audio-device error can release the player.
                incomingPlaybackRequested = field(activity, "player") != null
                incomingVibrationInitialized = field(activity, "vibrator") != null
            }
            if (activity is smsRes && stage == Stage.RESUMED && CallEvent.from(activity.intent).text == "בדיקת לחיצה") {
                tapWindows += listOf("player", "audio", "vibrator").all { field(activity, it) == null }
            }
            if (activity is smsRes && stage == Stage.DESTROYED && CallEvent.from(activity.intent).text == "בדיקת לחיצה") {
                destroyedTaps.incrementAndGet()
            }
        }
        val blocked = object : ContextWrapper(context) {
            override fun startActivity(intent: Intent) {} // Retain shade fallback when automatic launch is blocked.
        }
        fun tap(id: Int) {
            val source = PendingIntentCompat.getBroadcast(context, 900 + id,
                Intent(action).setPackage(context.packageName).putExtra("source", id), PendingIntent.FLAG_UPDATE_CURRENT, false)
            instrumentation.runOnMainSync {
                GrobootRec().onReceive(blocked, Intent("MADA_APP").putExtra("callT", 1)
                    .putExtra("text", "בדיקת לחיצה").putExtra("sourceIntent", source))
            }
            val alert = notifications(1).single()
            assertEquals(source, alert.actions.last().actionIntent)
            alert.contentIntent.send()
            val deadline = SystemClock.uptimeMillis() + 3000
            while ((delivered.size < id || destroyedTaps.get() < id) && SystemClock.uptimeMillis() < deadline) SystemClock.sleep(20)
            instrumentation.waitForIdleSync()
            assertEquals((1..id).toList(), delivered.toList())
            assertEquals("Successful source handoff must finish its silent owner", id, destroyedTaps.get())
            assertEquals(id, tapWindows.size)
            assertTrue("Shade entry must not initialize alert audio/vibration", tapWindows.all { it })
            notifications(0)
        }
        ContextCompat.registerReceiver(context, receiver, IntentFilter(action), ContextCompat.RECEIVER_NOT_EXPORTED)
        ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback(lifecycle)
        try {
            tap(1)
            ActivityScenario.launch<smsRes>(CallEvent(1, "התראה חדשה", null).popupIntent(context, -1, false)).use { scenario ->
                scenario.onActivity {
                    assertTrue("New incoming alerts still request playback", incomingPlaybackRequested)
                    assertTrue("New incoming alerts retain vibration", incomingVibrationInitialized)
                }
                tap(2) // Existing singleTop popup receives onNewIntent, rather than a second audible alert.
            }
        } finally {
            ActivityLifecycleMonitorRegistry.getInstance().removeLifecycleCallback(lifecycle)
            context.unregisterReceiver(receiver)
        }
    }

    @Test fun unavailableShadeSourcesStayReadableAndSilentAcrossRecreation() {
        settings.edit().putBoolean("win", true).putBoolean("NsouT1", true).putBoolean("NvibT1", true)
            .putBoolean("Nsou", true).putBoolean("Nvib", true).commit()
        val canceled = pending(92).apply { cancel() }
        for (source in listOf(null, canceled)) for (warm in listOf(false, true)) {
            val tap = CallEvent(1, "פרטי מקרה שמורים", source).popupIntent(context, -1, false).putExtra("openSource", true)
            val launch = if (warm) CallEvent(1, "מקרה קודם", null).popupIntent(context, -1, false) else tap
            ActivityScenario.launch<smsRes>(launch).use { scenario ->
                if (warm) {
                    scenario.onActivity { it.startActivity(tap) }
                    // Main-loop idleness does not await the system's onNewIntent transaction.
                    val deadline = SystemClock.uptimeMillis() + 3000
                    var delivered = false
                    while (!delivered && SystemClock.uptimeMillis() < deadline) {
                        scenario.onActivity {
                            delivered = it.intent.getBooleanExtra("openSource", false) &&
                                it.findViewById<TextView>(R.id.Address).text.toString() == "פרטי מקרה שמורים"
                        }
                        if (!delivered) SystemClock.sleep(20)
                    }
                    assertTrue("Warm shade intent must reach the existing popup", delivered)
                }
                repeat(2) {
                    scenario.onActivity { activity ->
                        assertEquals("פרטי מקרה שמורים", activity.findViewById<TextView>(R.id.Address).text.toString())
                        for (name in listOf("player", "audio", "vibrator")) {
                            assertNull(name, smsRes::class.java.getDeclaredField(name).apply { isAccessible = true }.get(activity))
                        }
                        assertTrue(smsRes::class.java.getDeclaredField("sourceAttempted").apply { isAccessible = true }.getBoolean(activity))
                    }
                    if (it == 0) scenario.recreate()
                }
            }
        }
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
                            .filterIsInstance<smsRes>().any { it.findViewById<TextView>(R.id.Address)?.text?.toString() == "בדיקה שנייה" }
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
