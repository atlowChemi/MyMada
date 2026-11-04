package atlow.chemi.mymada.calls

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.service.notification.NotificationListenerService

/** Grants belong to components, not packages. Preserve both installed identities. */
object ListenerAccess {
    fun components(context: Context) = listOf(
        ComponentName(context, atlow.chemi.mymada.NotificationService::class.java),
        ComponentName(context, atlow.chemi.mymada.recieversAndServices.NotificationService::class.java),
    )

    @JvmStatic fun selected(context: Context): ComponentName? {
        val enabled = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            ?.split(':')?.mapNotNull(ComponentName::unflattenFromString).orEmpty()
        return components(context).firstOrNull { it in enabled }
    }

    @JvmStatic fun rebind(context: Context) {
        selected(context)?.let(NotificationListenerService::requestRebind)
    }
}
