package atlow.mymadaadmin

import android.app.*
import android.content.*
import android.os.Build
import android.os.Bundle

/** Offline test package; no network permission or production message writes. */
class Source : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("id", 1)
        val kind = intent.getStringExtra("kind") ?: "event"
        if (kind == "negative-batch") {
            val now = System.currentTimeMillis()
            onReceive(context, Intent().putExtra("id", 8104).putExtra("kind", "child").putExtra("when", now - 60_000))
            onReceive(context, Intent().putExtra("id", 8105))
            onReceive(context, Intent().putExtra("id", 8105).putExtra("kind", "cancel"))
            onReceive(context, Intent().putExtra("id", 8106).putExtra("kind", "empty-summary"))
            onReceive(context, Intent().putExtra("id", 8107).putExtra("when", now + 60_000))
            onReceive(context, Intent().putExtra("id", 8108).putExtra("when", 0L))
            return
        }
        if (kind == "probe-unbind") { SnapshotProbe.current?.requestUnbind(); return }
        if (kind == "probe-rebind") {
            android.service.notification.NotificationListenerService.requestRebind(ComponentName(context, SnapshotProbe::class.java))
            return
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        if (kind == "clear") { manager.cancelAll(); return }
        val tag = intent.getStringExtra("tag")
        if (kind == "cancel") { manager.cancel(tag, id); return }
        if (Build.VERSION.SDK_INT >= 26) manager.createNotificationChannel(NotificationChannel("fixture", "Fixture", NotificationManager.IMPORTANCE_DEFAULT))
        val details = PendingIntent.getActivity(context, id, Intent(context, Details::class.java).putExtra("id", id), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val builder = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(context, "fixture") else Notification.Builder(context)
        builder.setSmallIcon(android.R.drawable.ic_dialog_info).setContentIntent(details)
        if (intent.hasExtra("when")) builder.setWhen(intent.getLongExtra("when", 0))
        if (kind == "title-only-summary") builder.setContentTitle("*אירוע חדש*")
        else if (kind == "expanded-summary") builder.setStyle(Notification.BigTextStyle().bigText("הודעת צוות מורחבת"))
        else if (kind != "empty-summary" && kind != "malformed") {
            builder.setContentTitle("*אירוע חדש*").setContentText(intent.getStringExtra("text") ?: "הודעת צוות בדיקה $id")
        }
        if (kind == "child" || kind.endsWith("summary")) builder.setGroup("calls-regression")
        if (kind.endsWith("summary")) builder.setGroupSummary(true)
        manager.notify(tag, id, builder.build())
    }
}

class Details : Activity() {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        getSharedPreferences("fixture", 0).edit().putInt("opened", intent.getIntExtra("id", -1))
            .putLong("openedAt", System.currentTimeMillis()).commit()
        finish()
    }
}
