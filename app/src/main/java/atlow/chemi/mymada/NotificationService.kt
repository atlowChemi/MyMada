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
import atlow.chemi.mymada.calls.DetectionStatus
import atlow.chemi.mymada.calls.CallRecovery
import atlow.chemi.mymada.calls.CallDiagnostics

/** Keep the installed native listener identity. */
open class NotificationService : NotificationListenerService() {
    // Active source identities only; never borrow content or suppress valid repeat deliveries.
    private val validSources = linkedSetOf<String>()
    protected open val listenerClass: Class<out NotificationService> get() = NotificationService::class.java
    override fun onNotificationPosted(posted: StatusBarNotification?) {
        deliver(posted, false)
    }

    private fun deliver(posted: StatusBarNotification?, recovered: Boolean) {
        val trace = if (BuildConfig.DEBUG && posted != null && CallDiagnostics.relevant(posted.packageName, packageName)) CallDiagnostics.next() else 0L
        if (BuildConfig.DEBUG && trace != 0L) CallDiagnostics.record(trace, if (recovered) "snapshot" else "posted",
            "listener=${listenerClass.name} ${CallDiagnostics.source(posted!!)}")
        // Both grants may survive an upgrade. Select a component, never deduplicate call content.
        val selected = ListenerAccess.selected(this)
        if (selected != null && selected != ComponentName(this, listenerClass)) {
            if (BuildConfig.DEBUG) CallDiagnostics.record(trace, "guard", "decision=other_listener")
            return
        }
        val notification = posted?.notification ?: return
        val extras = notification.extras
        // OS auto-group headers carry metadata and a navigation PI, neither is call content.
        // Keep renderable summaries on the normal parser path, regardless of their identity.
        if (notification.flags and Notification.FLAG_GROUP_SUMMARY != 0 &&
            notification.tickerText.isNullOrEmpty() && notification.actions.isNullOrEmpty() &&
            notification.fullScreenIntent == null && notification.contentView == null &&
            notification.bigContentView == null && notification.headsUpContentView == null &&
            listOf(Notification.EXTRA_TITLE, Notification.EXTRA_TITLE_BIG, Notification.EXTRA_TEXT,
                Notification.EXTRA_BIG_TEXT, Notification.EXTRA_SUB_TEXT, Notification.EXTRA_SUMMARY_TEXT,
                Notification.EXTRA_INFO_TEXT).none { !extras?.getCharSequence(it).isNullOrEmpty() } &&
            extras?.getCharSequenceArray(Notification.EXTRA_TEXT_LINES).orEmpty().none { !it.isNullOrEmpty() } &&
            extras?.getParcelableArray(Notification.EXTRA_MESSAGES).isNullOrEmpty() &&
            extras?.getParcelableArray(Notification.EXTRA_HISTORIC_MESSAGES).isNullOrEmpty() &&
            extras?.get(Notification.EXTRA_PICTURE) == null && extras?.get("android.pictureIcon") == null) {
            if (BuildConfig.DEBUG) CallDiagnostics.record(trace, "guard", "decision=empty_summary")
            return
        }
        val title = extras?.getCharSequence(Notification.EXTRA_TITLE)
        // BigTextStyle updates/summaries can carry the call only in the expanded body.
        val body = extras?.getCharSequence(Notification.EXTRA_TEXT)?.takeIf { it.isNotEmpty() }
            ?: extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?: extras?.getCharSequence(Notification.EXTRA_TEXT)
        if (BuildConfig.DEBUG) CallDiagnostics.record(trace, "extract", "branch=${when {
            !extras?.getCharSequence(Notification.EXTRA_TEXT).isNullOrEmpty() -> "text"
            extras?.getCharSequence(Notification.EXTRA_BIG_TEXT) != null -> "bigText"
            body != null -> "empty_text"
            else -> "missing"
        }} bodyLen=${body?.length ?: -1} blank=${body.isNullOrBlank()} knownValid=${posted.key in validSources}")
        if (body.isNullOrBlank() && posted.key in validSources) {
            if (BuildConfig.DEBUG) CallDiagnostics.record(trace, "guard", "decision=retain_valid_source")
            return
        }
        val call = CallParser.parse(getSharedPreferences("Settings", 0).getBoolean("Mapp", false),
            posted.packageName, title, body)
        if (BuildConfig.DEBUG) CallDiagnostics.record(trace, "parse", "enabled=${getSharedPreferences("Settings", 0).getBoolean("Mapp", false)} accepted=${call != null} type=${call?.type ?: -1} fallback=${call != null && body == null}")
        if (call == null) return
        val event = Intent("MADA_APP")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra("pack", posted.packageName)
            .putExtra("ticker", notification.tickerText?.toString() ?: "")
            .putExtra("title", title?.toString()).putExtra("text", call.text)
            .putExtra("callT", call.type).putExtra("time", call.time)
            .putExtra("sourceIntent", notification.contentIntent)
        if (BuildConfig.DEBUG && trace != 0L) event.putExtra(CallDiagnostics.EXTRA, trace)
        if (!CallRecovery.track(this, posted, title, body, event, recovered)) {
            if (BuildConfig.DEBUG) CallDiagnostics.record(trace, "guard", "decision=recovery_rejected")
            return
        }
        if (!body.isNullOrBlank()) {
            validSources.remove(posted.key)
            validSources.add(posted.key)
            // ponytail: bounded in-process protection; evicted/disconnected keys use fresh-event policy.
            if (validSources.size > 1024) validSources.remove(validSources.first())
        }
        DetectionStatus.refresh(this)
        if (BuildConfig.DEBUG) CallDiagnostics.record(trace, "dispatch", "type=${call.type} bodyLen=${call.text.length} sourcePI=${notification.contentIntent != null} recovered=$recovered")
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(event)
    }

    override fun onNotificationRemoved(posted: StatusBarNotification?) {
        if (BuildConfig.DEBUG && posted != null && CallDiagnostics.relevant(posted.packageName, packageName)) {
            CallDiagnostics.record(CallDiagnostics.next(), "removed", "knownValid=${posted.key in validSources} ${CallDiagnostics.source(posted)}")
        }
        validSources.remove(posted?.key)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || posted?.packageName != packageName) return
        val channel = posted.notification?.channelId ?: return
        if (channel.contains("generated")) getSystemService(NotificationManager::class.java)?.deleteNotificationChannel(channel)
    }

    override fun onListenerConnected() {
        if (BuildConfig.DEBUG) CallDiagnostics.record(CallDiagnostics.next(), "connected", "validKeysCleared=${validSources.size}")
        validSources.clear()
        DetectionStatus.refresh(this)
        if (ListenerAccess.selected(this) != ComponentName(this, listenerClass)) return
        try {
            if (CallRecovery.connected(this)) activeNotifications.sortedBy { it.postTime }.forEach { deliver(it, true) }
        } catch (error: Exception) {
            Log.e("NotificationService", "Cannot reconcile active notifications", error)
        }
    }

    override fun onListenerDisconnected() {
        if (BuildConfig.DEBUG) CallDiagnostics.record(CallDiagnostics.next(), "disconnected", "validKeysCleared=${validSources.size}")
        validSources.clear()
        CallRecovery.disconnected(ComponentName(this, listenerClass))
        ListenerAccess.stopIfRevoked(this)
        ListenerAccess.rebind(this)
    }

    override fun onDestroy() {
        if (BuildConfig.DEBUG) CallDiagnostics.record(CallDiagnostics.next(), "listener_destroy", "validKeysLost=${validSources.size}")
        CallRecovery.disconnected(ComponentName(this, listenerClass))
        ListenerAccess.stopIfRevoked(this)
        super.onDestroy()
    }

}
