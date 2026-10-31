package atlow.chemi.mymada.calls

import android.app.NotificationChannel
import android.app.Activity
import android.app.Application
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.PendingIntentCompat
import atlow.chemi.mymada.HomePage
import atlow.chemi.mymada.R
import atlow.chemi.mymada.permissions.NotificationPermission
import atlow.chemi.mymada.recieversAndServices.ActionReceiver
import atlow.chemi.mymada.recieversAndServices.SMSMonitorService

/** Ordinary controls only; Android owns listener binding and process lifetime. */
object DetectionStatus : Application.ActivityLifecycleCallbacks {
    override fun onActivityResumed(activity: Activity) { refresh(activity) }
    override fun onActivityCreated(activity: Activity, state: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, state: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit

    @JvmStatic fun refresh(context: Context) = with(context) {
        if (!getSharedPreferences("Settings", 0).getBoolean("Mapp", false) ||
            !NotificationPermission.granted(this) || ListenerAccess.selected(this) == null) {
            getSystemService(NotificationManager::class.java).cancel(78)
            return@with
        }
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannelGroup(NotificationChannelGroup("grpOthers", "התראות שונות"))
            manager.createNotificationChannel(NotificationChannel("runInBg", "פעילות ברקע", NotificationManager.IMPORTANCE_MIN).apply {
                enableVibration(false); enableLights(false); setShowBadge(false)
                group = "grpOthers"
            })
        }
        val home = PendingIntentCompat.getActivity(this, 1, Intent(this, HomePage::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK), 0, false)
        val stop = PendingIntentCompat.getBroadcast(this, 13, Intent(this, ActionReceiver::class.java)
            .putExtra("action", "runInBG"), PendingIntent.FLAG_UPDATE_CURRENT, false)
        manager.notify(78, NotificationCompat.Builder(this, "runInBg")
            .setSmallIcon(R.drawable.ic_noti).setContentTitle(getString(R.string.BGServiceTtl))
            .setTicker(getString(R.string.BGServiceTicker)).setContentText(getString(R.string.BGServiceTxt))
            .setContentIntent(home).setGroup("BG").setPriority(NotificationCompat.PRIORITY_MIN)
            .addAction(R.drawable.ic_close, getString(R.string.MetroStop), stop)
            .setShowWhen(false).setColor(Color.argb(50, 76, 119, 247))
            .setSound(null).setVibrate(null).setOngoing(true).build())
    }

    @JvmStatic fun stop(context: Context) {
        CallRecovery.stop(context)
        context.getSharedPreferences("Settings", 0).edit().putBoolean("Mapp", false).apply()
        context.stopService(Intent(context, SMSMonitorService::class.java))
        context.getSystemService(NotificationManager::class.java).cancel(78)
    }
}
