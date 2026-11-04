package atlow.mymadaadmin

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

/** Optional offline diagnostic identity; never parses, launches or replays calls. */
class SnapshotProbe : NotificationListenerService() {
    private fun record(route: String, posted: StatusBarNotification) {
        if (posted.packageName == packageName) Log.i("CallSnapshotProbe",
            "$route key=${posted.key} postTime=${posted.postTime} when=${posted.notification.`when`} flags=${posted.notification.flags}")
    }
    override fun onListenerConnected() {
        current = this
        Log.i("CallSnapshotProbe", "connected")
        activeNotifications.forEach { record("snapshot", it) }
    }
    override fun onListenerDisconnected() { current = null }
    override fun onNotificationPosted(posted: StatusBarNotification) = record("callback", posted)
    companion object { var current: SnapshotProbe? = null }
}
