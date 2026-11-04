package atlow.chemi.mymada

import android.app.Notification
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import atlow.chemi.mymada.calls.CallParser
import atlow.chemi.mymada.calls.ListenerAccess

/** Keep the installed native listener identity. */
open class NotificationService : NotificationListenerService() {
    protected open val listenerClass: Class<out NotificationService> get() = NotificationService::class.java
    override fun onNotificationPosted(posted: StatusBarNotification?) {
        // Both grants may survive an upgrade. Select a component, never deduplicate call content.
        val selected = ListenerAccess.selected(this)
        if (selected != null && selected != ComponentName(this, listenerClass)) return
        val notification = posted?.notification ?: return
        val extras = notification.extras
        val title = extras?.getCharSequence(Notification.EXTRA_TITLE)
        val call = CallParser.parse(getSharedPreferences("Settings", 0).getBoolean("Mapp", false),
            posted.packageName, title, extras?.getCharSequence(Notification.EXTRA_TEXT)) ?: return
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(Intent("MADA_APP")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra("pack", posted.packageName)
            .putExtra("ticker", notification.tickerText?.toString() ?: "")
            .putExtra("title", title?.toString()).putExtra("text", call.text)
            .putExtra("callT", call.type).putExtra("time", call.time)
            .putExtra("sourceIntent", notification.contentIntent))
    }

    override fun onNotificationRemoved(posted: StatusBarNotification?) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || posted?.packageName != packageName) return
        val channel = posted.notification?.channelId ?: return
        if (channel.contains("generated")) getSystemService(NotificationManager::class.java)?.deleteNotificationChannel(channel)
    }

    override fun onListenerConnected() {
        try { activeNotifications } catch (error: Exception) {
            Log.e("NotificationService", "Cannot read active notifications", error)
        }
    }

    override fun onListenerDisconnected() {
        ListenerAccess.rebind(this)
    }
}
