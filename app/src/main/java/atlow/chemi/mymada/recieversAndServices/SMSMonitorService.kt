package atlow.chemi.mymada.recieversAndServices

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.PendingIntentCompat
import androidx.core.content.ContextCompat
import atlow.chemi.mymada.HomePage
import atlow.chemi.mymada.R
import atlow.chemi.mymada.permissions.NotificationPermission

/** Installed native monitor identity and channel/notification IDs stay stable. */
class SMSMonitorService : Service() {
    override fun onCreate() {
        super.onCreate()
        if (!enabled(this)) { stopSelf(); return }
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannelGroup(NotificationChannelGroup("grpOthers", "התראות שונות"))
            manager.createNotificationChannel(NotificationChannel("runInBg", "פעילות ברקע", NotificationManager.IMPORTANCE_MIN).apply {
                enableVibration(false); enableLights(false); setShowBadge(false)
                description = "חיוני כדי לאפשר זיהוי מקרים"
                group = "grpOthers"
            })
        }
        val home = PendingIntentCompat.getActivity(this, 1, Intent(this, HomePage::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK), 0, false)
        val stop = PendingIntentCompat.getBroadcast(this, 13, Intent(this, ActionReceiver::class.java)
            .putExtra("action", "runInBG"), PendingIntent.FLAG_UPDATE_CURRENT, false)
        startForeground(78, NotificationCompat.Builder(this, "runInBg")
            .setSmallIcon(R.drawable.ic_noti).setContentTitle(getString(R.string.BGServiceTtl))
            .setTicker(getString(R.string.BGServiceTicker)).setContentText(getString(R.string.BGServiceTxt))
            .setContentIntent(home).setGroup("BG").setPriority(NotificationCompat.PRIORITY_MIN)
            .addAction(R.drawable.ic_close, getString(R.string.MetroStop), stop)
            .setShowWhen(false).setColor(Color.argb(50, 76, 119, 247))
            .setSound(null).setVibrate(null).setOngoing(true).build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!enabled(this)) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    companion object {
        private fun enabled(context: Context) = context.getSharedPreferences("Settings", 0).getBoolean("Mapp", false) &&
            NotificationPermission.granted(context)

        /** Background callers never open permission UI and never crash on OS start restrictions. */
        @JvmStatic fun start(context: Context) {
            val intent = Intent(context, SMSMonitorService::class.java)
            if (!enabled(context)) { context.stopService(intent); return }
            try {
                ContextCompat.startForegroundService(context, intent)
            } catch (error: IllegalStateException) {
                Log.w("SMSMonitorService", "OS deferred monitor start; detection remains OS-listener bound", error)
            }
        }
    }
}
