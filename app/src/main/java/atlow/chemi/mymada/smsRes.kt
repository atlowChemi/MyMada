package atlow.chemi.mymada

import android.os.Build

import android.app.PendingIntent
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import atlow.chemi.mymada.calls.CallAlerts
import atlow.chemi.mymada.calls.CallEvent
import atlow.chemi.mymada.calls.CallText
import atlow.chemi.mymada.calls.CallPopupScreen
import atlow.chemi.mymada.calls.CallDiagnostics
import atlow.chemi.mymada.helpers.LocalHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** Keep the installed popup activity identity. Payload belongs to its Intent. */
class smsRes : AppCompatActivity() {
    private val handler = Handler(Looper.getMainLooper())
    private var player: MediaPlayer? = null
    private var originalVolume: Int? = null
    private var audio: AudioManager? = null
    private var vibrator: Vibrator? = null
    private var sourceAttempted = false

    override fun attachBaseContext(base: Context) = super.attachBaseContext(LocalHelper.onAttach(base))
    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTION_BAR)
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        window.statusBarColor = ContextCompat.getColor(this, R.color.OldPrimary)
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        val event = CallEvent.from(intent)
        if (BuildConfig.DEBUG) CallDiagnostics.record(intent.getLongExtra(CallDiagnostics.EXTRA, 0), "popup_create",
            "ourId=${intent.getIntExtra("notNum", -1)} sourceTap=${intent.getBooleanExtra("openSource", false)} restored=${savedInstanceState != null} sourcePI=${event.source != null} bodyLen=${event.text.length}")
        sourceAttempted = savedInstanceState?.getBoolean("sourceAttempted") ?: false
        if (intent.getBooleanExtra("openSource", false)) {
            dismissNotification()
            if (sourceAttempted) showCall(event)
            return
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        val preferences = getSharedPreferences("Settings", 0)
        val now = Calendar.getInstance()
        audio = getSystemService(AudioManager::class.java)
        vibrator = getSystemService(Vibrator::class.java)
        val decision = CallAlerts.decide(preferences.all, event.type, now.get(Calendar.HOUR_OF_DAY), audio?.ringerMode ?: -1, false)
        val loop = preferences.getBoolean("loop", false)
        if (decision.popupVibrate) vibrator?.vibrate(longArrayOf(0, 900, 200, 900, 200, 900, 200, 900, 200, 900, 200), if (loop) 0 else -1)
        if (decision.sound) play(Uri.parse(if (decision.customSound) decision.ringtone else "android.resource://$packageName/${R.raw.alert}"), loop)
        if (preferences.getBoolean("killS", false)) {
            val seconds = preferences.getString("timeK", "6")?.toLongOrNull()?.coerceIn(0, Int.MAX_VALUE.toLong()) ?: 6
            handler.postDelayed({ releaseAlert(); finish() }, seconds * 1000)
        }

        showCall(event)
    }

    private fun showCall(event: CallEvent) {
        val text = CallText.parse(event.type, event.text, SimpleDateFormat("HH:mm", Locale.getDefault()).format(Calendar.getInstance().time))
        setContent { CallPopupScreen(event, text, onClose = { finish() }, onDetails = { openDetails(event) }) }
    }

    private fun dismissNotification() {
        val notifications = getSystemService(NotificationManager::class.java)
        val notificationId = intent.getIntExtra("notNum", -1)
        notifications.activeNotifications.filter { it.id == notificationId && it.notification.group == "Calls" }
            .forEach {
                notifications.cancel(it.tag, it.id)
                if (BuildConfig.DEBUG) CallDiagnostics.record(intent.getLongExtra(CallDiagnostics.EXTRA, 0), "cancel", "ourId=${it.id} reason=explicit_source_action")
            }
    }

    override fun onPostResume() {
        super.onPostResume()
        if (intent.getBooleanExtra("openSource", false) && !sourceAttempted) {
            sourceAttempted = true
            val event = CallEvent.from(intent)
            if (sendSource(event)) finish() else showCall(event)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("sourceAttempted", sourceAttempted)
        super.onSaveInstanceState(outState)
    }

    private fun sendSource(event: CallEvent): Boolean {
        if (BuildConfig.DEBUG) CallDiagnostics.record(intent.getLongExtra(CallDiagnostics.EXTRA, 0), "source_send", "ourId=${intent.getIntExtra("notNum", -1)} sourcePI=${event.source != null}")
        val source = event.source ?: return false
        // Explicit user click: modern targets must opt in to lending the visible sender's BAL privilege.
        val options = if (Build.VERSION.SDK_INT >= 34) android.app.ActivityOptions.makeBasic()
            .setPendingIntentBackgroundActivityStartMode(if (Build.VERSION.SDK_INT >= 36)
                android.app.ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOW_IF_VISIBLE
            else android.app.ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED).toBundle() else null
        try {
            source.send(this, 0, null, null, null, null, options)
            if (BuildConfig.DEBUG) CallDiagnostics.record(intent.getLongExtra(CallDiagnostics.EXTRA, 0), "source_send_returned", "result=returned_not_launch_proof")
            return true
        }
        catch (error: PendingIntent.CanceledException) { Log.w("smsRes", "Source action canceled", error) }
        catch (error: SecurityException) { Log.w("smsRes", "Source action unavailable", error) }
        return false
    }

    private fun openDetails(event: CallEvent) {
        dismissNotification()
        sendSource(event)
        finish()
    }

    private fun play(uri: Uri, loop: Boolean) {
        val next = MediaPlayer()
        player = next
        try {
            val manager = requireNotNull(audio)
            val preferences = getSharedPreferences("Settings", 0)
            if (preferences.getBoolean("volumeApp", true)) {
                originalVolume = manager.getStreamVolume(AudioManager.STREAM_ALARM)
                val max = manager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                manager.setStreamVolume(AudioManager.STREAM_ALARM, preferences.getInt("volumeAppNum", max).coerceIn(0, max), 0)
            }
            next.setDataSource(applicationContext, uri)
            next.setAudioStreamType(AudioManager.STREAM_ALARM)
            next.isLooping = loop
            next.setOnCompletionListener { if (player === next) releaseSound() }
            next.setOnErrorListener { _, _, _ -> if (player === next) releaseSound(); true }
            next.prepare(); next.start()
        } catch (error: Exception) {
            Log.w("smsRes", "Cannot play call alert", error)
            releaseSound()
        }
    }
    private fun releaseSound() {
        player?.release(); player = null
        originalVolume?.let { audio?.setStreamVolume(AudioManager.STREAM_ALARM, it, 0) }
        originalVolume = null
    }
    private fun releaseAlert() {
        handler.removeCallbacksAndMessages(null)
        vibrator?.cancel()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        releaseSound()
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (BuildConfig.DEBUG) CallDiagnostics.record(intent.getLongExtra(CallDiagnostics.EXTRA, 0), "popup_new_intent", "ourId=${intent.getIntExtra("notNum", -1)} sourceTap=${intent.getBooleanExtra("openSource", false)}")
        sourceAttempted = false
        if (intent.getBooleanExtra("openSource", false)) {
            // A singleTop tap resumes this owner before any scheduled recreation.
            releaseAlert()
            audio = null
            vibrator = null
            dismissNotification()
            setContent {}
        } else recreate()
    }
    override fun onDestroy() { releaseAlert(); super.onDestroy() }
}
