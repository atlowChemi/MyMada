package atlow.chemi.mymada.calls

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.IntentCompat
import atlow.chemi.mymada.smsRes

/** Per-call state survives queued activity starts and Android's saved launch intent. */
data class CallEvent(val type: Int, val text: String, val source: PendingIntent?) {
    fun popupIntent(context: Context, notificationId: Int, loop: Boolean): Intent =
        Intent(context, smsRes::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            .putExtra("callT", type).putExtra("text", text).putExtra("sourceIntent", source)
            .putExtra("notNum", notificationId).putExtra("loop", loop)

    companion object {
        @JvmStatic fun from(intent: Intent?): CallEvent = CallEvent(
            intent?.getIntExtra("callT", 1) ?: 1,
            intent?.getCharSequenceExtra("text")?.toString() ?: "מקרה חדש",
            intent?.let { IntentCompat.getParcelableExtra(it, "sourceIntent", PendingIntent::class.java) },
        )
    }
}
