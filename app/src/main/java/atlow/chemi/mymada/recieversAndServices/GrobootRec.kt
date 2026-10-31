package atlow.chemi.mymada.recieversAndServices

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.PendingIntentCompat
import androidx.core.content.IntentCompat
import atlow.chemi.mymada.MokedPage
import atlow.chemi.mymada.R
import atlow.chemi.mymada.calls.CallAlerts
import atlow.chemi.mymada.calls.CallEvent
import atlow.chemi.mymada.calls.CallRecovery
import atlow.chemi.mymada.permissions.NotificationPermission
import java.util.Calendar

/** Installed receiver identity. Every callback carries its own payload and source action. */
class GrobootRec : BroadcastReceiver() {
    private var notificationId = 0

    @SuppressLint("MissingPermission") // Current OS grant is checked immediately before notify.
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        if (!CallRecovery.allow(context, intent)) {
            CallRecovery.handled(context, intent)
            return
        }
        val preferences = context.getSharedPreferences("Settings", 0)
        val event = CallEvent(intent.getIntExtra("callT", 1), intent.getCharSequenceExtra("text")?.toString() ?: "",
            IntentCompat.getParcelableExtra(intent, "sourceIntent", PendingIntent::class.java))
        val audio = context.getSystemService(AudioManager::class.java)
        val decision = CallAlerts.decide(preferences.all, event.type, Calendar.getInstance().get(Calendar.HOUR_OF_DAY), audio?.ringerMode ?: -1, false)
        if (!decision.enabled) {
            CallRecovery.handled(context, intent)
            return
        }
        val popup = preferences.getBoolean("win", true)
        val loop = preferences.getBoolean("loop", false)
        if (preferences.getBoolean("not", true)) {
            val manager = context.getSystemService(NotificationManager::class.java)
            ensureChannel(manager)
            val dictation = PendingIntentCompat.getActivity(context, System.currentTimeMillis().toInt(),
                Intent(context, MokedPage::class.java).putExtra("msg", true)
                    .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or Intent.FLAG_ACTIVITY_NEW_TASK), 0, false)
            val message = event.type == 4 || event.type == 6
            val notification = NotificationCompat.Builder(context, "notWithWin")
                .setContentTitle(if (message) "הודעה חדשה באפליקציה!" else "אירוע חדש באפליקציה!")
                .setContentText(if (popup) context.getString(R.string.call_open_alert) else "לחץ כאן בכדי להכתיב טפסים.").setSmallIcon(R.drawable.ic_siren)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
                .setVibrate(LongArray(9)).setSound(null).setContentIntent(dictation).setAutoCancel(true)
                .addAction(R.drawable.ic_edit, "הכתב", dictation)
                .setTicker(if (message) "התקבלה הודעה חדש באפליקציה!" else "התקבל אירוע חדש באפליקציה!")
                .setStyle(NotificationCompat.BigTextStyle().bigText(event.text)).setGroup("Calls").setGroupSummary(true)
            event.source?.let { notification.addAction(R.drawable.ic_info, "פרטים", it) }
            if (NotificationPermission.granted(context)) {
                if (preferences.getBoolean("ride", false)) {
                    // Replace calls only: metronome and background-control notifications are unrelated.
                    manager.activeNotifications.filter { it.notification.group == "Calls" }.forEach { manager.cancel(it.tag, it.id) }
                } else {
                    nextNotificationId = maxOf(nextNotificationId, manager.activeNotifications
                        .filter { it.notification.group == "Calls" }.maxOfOrNull { it.id } ?: 0) + 1
                    notificationId = nextNotificationId
                }
                if (popup) notification.setContentIntent(PendingIntentCompat.getActivity(context, notificationId,
                    event.popupIntent(context, notificationId, loop), PendingIntent.FLAG_UPDATE_CURRENT, false))
                manager.notify(notificationId, notification.build())
            }
            if (!popup && Settings.canDrawOverlays(context)) {
                val overlay = Intent(context, FloatingWidgetService::class.java)
                    .putExtra("vib", decision.overlayVibrate).putExtra("sou", decision.sound).putExtra("loop", loop)
                    .putExtra("uri", if (decision.customSound) decision.ringtone else "android.resource://${context.packageName}/${R.raw.alert}")
                    .putExtra("timeK", preferences.getString("timeK", "6")).putExtra("ShabbatM", preferences.getBoolean("killS", false))
                try { context.startService(overlay) }
                catch (error: IllegalStateException) { Log.w("GrobootRec", "Cannot start call overlay", error) }
                catch (error: SecurityException) { Log.w("GrobootRec", "Cannot start call overlay", error) }
            }
        }
        if (popup) context.startActivity(event.popupIntent(context, notificationId, loop))
        CallRecovery.handled(context, intent, dispatched = true)
    }

    private fun ensureChannel(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT < 26 || manager.getNotificationChannel("notWithWin") != null) return
        // Same native Home defaults; background delivery must not depend on opening Home first.
        manager.createNotificationChannelGroup(NotificationChannelGroup("grpTog", "התראות מקרים"))
        manager.createNotificationChannel(NotificationChannel("notWithWin", "זיהוי הודעות", NotificationManager.IMPORTANCE_LOW).apply {
            enableVibration(false); setBypassDnd(false); enableLights(false); setShowBadge(true)
            description = "התראות עבור הודעות כאשר מוגדר להקפיץ גם חלון"
            lightColor = Color.RED; group = "grpTog"
        })
    }
    companion object { private var nextNotificationId = 5000 }
}
