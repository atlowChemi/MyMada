package atlow.chemi.mymada.calls

import android.content.ComponentName
import android.app.NotificationManager
import android.os.Build
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.service.notification.NotificationListenerService

/** Grants belong to components, not packages. Preserve both installed identities. */
object ListenerAccess {
    fun components(context: Context) = listOf(
        ComponentName(context, atlow.chemi.mymada.NotificationService::class.java),
        ComponentName(context, atlow.chemi.mymada.recieversAndServices.NotificationService::class.java),
    )

    @JvmStatic fun selected(context: Context): ComponentName? {
        if (Build.VERSION.SDK_INT >= 27) {
            val manager = context.getSystemService(NotificationManager::class.java)
            return components(context).firstOrNull(manager::isNotificationListenerAccessGranted)
        }
        val enabled = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            ?.split(':')?.mapNotNull(ComponentName::unflattenFromString).orEmpty()
        return components(context).firstOrNull { it in enabled }
    }

    @JvmStatic fun rebind(context: Context) {
        selected(context)?.let(NotificationListenerService::requestRebind)
    }

    fun stopIfRevoked(context: Context) {
        if (selected(context) != null) return
        context.getSharedPreferences("Settings", 0).edit().putBoolean("Mapp", false).apply()
        context.stopService(Intent(context, atlow.chemi.mymada.recieversAndServices.SMSMonitorService::class.java))
    }
}
