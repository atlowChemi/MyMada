package atlow.chemi.mymada.helpers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import atlow.chemi.mymada.R
import atlow.chemi.mymada.codeC
import atlow.chemi.mymada.contactChatActivity
import atlow.chemi.mymada.integrations.PushRoute
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        PushRoute.parse(message.data)?.let { publish(this, it) }
    }

    override fun onNewToken(token: String) { saveToken(this, token) }

    companion object {
        @JvmStatic fun getToken(context: Context): Task<String> {
            val app = context.applicationContext
            return FirebaseMessaging.getInstance().token.addOnSuccessListener { saveToken(app, it) }
        }

        internal fun saveToken(context: Context, token: String) {
            val preferences = context.getSharedPreferences("Settings", 0)
            // Preserve the existing chats/{token} identity before updating the delivery token.
            val identity = preferences.getString("chatIdentity", null)
                ?: preferences.getString("fcm", null)?.takeUnless { it.isBlank() || it == "empty" }
                ?: token
            preferences.edit().putString("chatIdentity", identity).putString("fcm", token).apply()
            com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().setUserId(token)
        }

        @JvmStatic fun chatIdentity(context: Context): String {
            val preferences = context.getSharedPreferences("Settings", 0)
            return preferences.getString("chatIdentity", null) ?: preferences.getString("fcm", "empty").orEmpty()
        }

        fun destination(context: Context, route: PushRoute): Intent? = when (route.kind) {
            PushRoute.Kind.ACTIVATION -> Intent(context, codeC::class.java)
                .putExtra("toCode", true).putExtra("code", route.code).putExtra("fireKey", route.key)
                // Extras do not distinguish PendingIntents. Keep simultaneous approvals independent.
                .setAction("atlow.chemi.mymada.ACTIVATE:${route.key}:${route.code}")
            PushRoute.Kind.CHAT -> Intent(context, contactChatActivity::class.java)
            else -> null
        }?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

        fun publish(context: Context, route: PushRoute) {
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            // FCM can start the process before Home has ever created channels.
            if (Build.VERSION.SDK_INT >= 26) {
                manager.createNotificationChannelGroup(NotificationChannelGroup("grpOthers", "התראות שונות"))
                manager.createNotificationChannel(NotificationChannel("Push", "הודעות פוש", NotificationManager.IMPORTANCE_HIGH).apply {
                    group = "grpOthers"; enableVibration(true); enableLights(true); setBypassDnd(true); setShowBadge(true)
                    description = "התראות להודעות פוש שישלחו כגון בקשות קוד וכדו'"
                    lightColor = Color.RED
                })
            }
            if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
            val pending = destination(context, route)?.let {
                PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            }
            val notification = NotificationCompat.Builder(context, "Push")
                .setSmallIcon(R.drawable.ic_siren).setContentTitle(route.title)
                .setContentText(route.message).setStyle(NotificationCompat.BigTextStyle().bigText(route.message))
                .setDefaults(NotificationCompat.DEFAULT_ALL).setAutoCancel(true).setContentIntent(pending).build()
            val id = if (route.kind == PushRoute.Kind.ACTIVATION) (route.key + route.code).hashCode()
                else System.currentTimeMillis().toInt()
            manager.notify(id, notification)
        }

        /** Notification+data messages use the launcher instead of onMessageReceived when cold. */
        fun routeLauncher(context: Context, intent: Intent): Boolean {
            val data = intent.extras?.keySet()?.mapNotNull { key ->
                (intent.extras?.get(key) as? String)?.let { key to it }
            }?.toMap().orEmpty()
            // RN notification PendingIntents retain route names across install-r.
            val target = when (data["target"]) {
                "Dispatch" -> Intent(context, atlow.chemi.mymada.MokedPage::class.java)
                    .putExtra("msg", data["page"] == "Form")
                "MetronomeActivity" -> Intent(context, atlow.chemi.mymada.UtillitiesAndTools::class.java)
                    .putExtra("running", true)
                else -> PushRoute.parse(data)?.let { destination(context, it) }
            } ?: return false
            target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(target)
            return true
        }
    }
}
