package atlow.chemi.mymada

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.PendingIntent
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.text.SpannableString
import android.text.method.ScrollingMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import atlow.chemi.mymada.calls.CallAlerts
import atlow.chemi.mymada.calls.CallEvent
import atlow.chemi.mymada.calls.CallText
import atlow.chemi.mymada.helpers.LocalHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

/** Keep the installed popup activity identity and native layout. Payload belongs to its Intent. */
class smsRes : AppCompatActivity() {
    private val handler = Handler(Looper.getMainLooper())
    private var player: MediaPlayer? = null
    private var originalVolume: Int? = null
    private var audio: AudioManager? = null
    private var vibrator: Vibrator? = null
    private var colorAnimation: ObjectAnimator? = null

    override fun attachBaseContext(base: Context) = super.attachBaseContext(LocalHelper.onAttach(base))
    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTION_BAR)
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        window.statusBarColor = ContextCompat.getColor(this, R.color.OldPrimary)
        setContentView(R.layout.activity_sms_res)
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        val event = CallEvent.from(intent)
        // Android may reject an automatic background launch. Keep its fallback until this UI opens.
        val notifications = getSystemService(NotificationManager::class.java)
        val notificationId = intent.getIntExtra("notNum", -1)
        notifications.activeNotifications.filter { it.id == notificationId && it.notification.group == "Calls" }
            .forEach { notifications.cancel(it.tag, it.id) }
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

        val font = Typeface.createFromAsset(assets, "fonts/opensanshebrew.ttf")
        for (id in intArrayOf(R.id.msgType, R.id.Address, R.id.KodR, R.id.SMS, R.id.newC, R.id.name)) findViewById<TextView>(id).typeface = font
        findViewById<TextView>(R.id.newC).text = if (event.type == 4 || event.type == 6) "הודעה חדשה!" else "אירוע חדש!"
        val typeView = findViewById<TextView>(R.id.msgType)
        typeView.text = when (event.type) { 2 -> "קבוצה"; 3, 5 -> "משמרת"; 4 -> "הודעה מנהלתית"; 6 -> "שירות"; else -> "כונן" }
        val text = CallText.parse(event.type, event.text, SimpleDateFormat("HH:mm", Locale.getDefault()).format(now.time))
        findViewById<TextView>(R.id.Address).apply {
            this.text = text.address
            if (text.plain) maxLines = 5
            movementMethod = ScrollingMovementMethod()
        }
        findViewById<TextView>(R.id.SMS).text = getString(R.string.time, text.time)
        if (!text.plain) findViewById<TextView>(R.id.KodR).text = SpannableString("${text.dispatchCode}\n${text.medicalCode}").apply {
            setSpan(ForegroundColorSpan(ContextCompat.getColor(this@smsRes, R.color.ColorPrimary)), text.dispatchCode.length, length, 0)
            setSpan(StyleSpan(Typeface.BOLD), text.dispatchCode.length, length, 0)
        }
        if (event.type == 4) {
            typeView.startAnimation(AlphaAnimation(0f, 1f).apply {
                duration = 150; startOffset = 100; repeatMode = Animation.REVERSE; repeatCount = Animation.INFINITE
            })
            colorAnimation = ObjectAnimator.ofInt(typeView, "textColor", Color.RED, Color.BLUE).apply {
                duration = 350; repeatMode = ValueAnimator.REVERSE; repeatCount = ValueAnimator.INFINITE
                setEvaluator(ArgbEvaluator()); start()
            }
        }
        findViewById<Button>(R.id.button).setOnClickListener { finish() }
        findViewById<Button>(R.id.buttonI).setOnClickListener {
            try { event.source?.send() } catch (error: PendingIntent.CanceledException) { Log.w("smsRes", "Source action canceled", error) }
            finish()
        }
        for (id in intArrayOf(R.id.button, R.id.buttonI)) touchFeedback(findViewById(id))
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
        colorAnimation?.cancel(); colorAnimation = null
        findViewById<TextView>(R.id.msgType)?.clearAnimation()
    }
    override fun onNewIntent(intent: Intent) { super.onNewIntent(intent); setIntent(intent); recreate() }
    override fun onDestroy() { releaseAlert(); super.onDestroy() }

    private fun touchFeedback(button: View) {
        var x = 0f; var y = 0f; var moved = false
        button.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> { x = event.x; y = event.y; moved = false; view.alpha = 0.4f; true }
                MotionEvent.ACTION_MOVE -> { if (abs(x - event.x) > 140 || abs(y - event.y) > 50) { moved = true; view.alpha = 1f }; true }
                MotionEvent.ACTION_UP -> { view.alpha = 1f; if (!moved) view.performClick(); true }
                MotionEvent.ACTION_CANCEL -> { moved = true; view.alpha = 1f; true }
                else -> false
            }
        }
    }
}
