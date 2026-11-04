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
        val manager = context.getSystemService(NotificationManager::class.java)
        if (kind == "clear") { manager.cancelAll(); return }
        if (Build.VERSION.SDK_INT >= 26) manager.createNotificationChannel(NotificationChannel("fixture", "Fixture", NotificationManager.IMPORTANCE_DEFAULT))
        val details = PendingIntent.getActivity(context, id, Intent(context, Details::class.java).putExtra("id", id), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val builder = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(context, "fixture") else Notification.Builder(context)
        builder.setSmallIcon(android.R.drawable.ic_dialog_info).setContentIntent(details)
        if (kind == "title-only-summary") builder.setContentTitle("*אירוע חדש*")
        else if (kind == "expanded-summary") builder.setStyle(Notification.BigTextStyle().bigText("הודעת צוות מורחבת"))
        else if (kind != "empty-summary" && kind != "malformed") {
            builder.setContentTitle("*אירוע חדש*").setContentText("הודעת צוות בדיקה $id")
        }
        if (kind == "child" || kind.endsWith("summary")) builder.setGroup("calls-regression")
        if (kind.endsWith("summary")) builder.setGroupSummary(true)
        manager.notify(id, builder.build())
    }
}

class Details : Activity() {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        getSharedPreferences("fixture", 0).edit().putInt("opened", intent.getIntExtra("id", -1)).commit()
        finish()
    }
}
