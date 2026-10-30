package atlow.chemi.mymada.calls

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.core.app.PendingIntentCompat
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.MokedPage
import atlow.chemi.mymada.NotificationService
import atlow.chemi.mymada.R
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
        if (Build.VERSION.SDK_INT >= 26) manager.createNotificationChannel(
            NotificationChannel("notWithWin", "Calls test", NotificationManager.IMPORTANCE_LOW))
        settings.edit().clear().putBoolean("Mapp", true).putBoolean("not", true)
            .putBoolean("win", false).putBoolean("Nsou", false).putBoolean("Nvib", false)
            .putBoolean("cut", true).commit()
        manager.cancelAll()
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
        while (manager.activeNotifications.size != count && SystemClock.uptimeMillis() < deadline) SystemClock.sleep(20)
        assertEquals(count, manager.activeNotifications.size)
        return manager.activeNotifications.map { it.notification }
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
        settings.edit().putBoolean("ride", true).commit()
        instrumentation.runOnMainSync { post("הודעת צוות בדיקה ג", null, 3) }
        val replacement = notifications(1).single()
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
}
