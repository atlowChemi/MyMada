package atlow.chemi.mymada.recieversAndServices

import android.animation.ValueAnimator
import android.app.Service
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.graphics.Point
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.os.Vibrator
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import atlow.chemi.mymada.MokedPage
import atlow.chemi.mymada.R
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp

/** Native/RN installed overlay identity. One owned alert, window pair and timer at a time. */
class FloatingWidgetService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private val screen = Point()
    private lateinit var windows: WindowManager
    private lateinit var audio: AudioManager
    private var vibrator: Vibrator? = null
    private var widget: View? = null
    private var removal: View? = null
    private var player: MediaPlayer? = null
    private var originalVolume: Int? = null
    private var animation: ValueAnimator? = null
    private var left = true

    override fun onCreate() {
        super.onCreate()
        windows = getSystemService(WindowManager::class.java)
        audio = getSystemService(AudioManager::class.java)
        vibrator = getSystemService(Vibrator::class.java)
    }
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        cleanup()
        if (intent?.extras == null || !Settings.canDrawOverlays(this)) {
            stopSelf(); return START_NOT_STICKY
        }
        windows.defaultDisplay.getSize(screen)
        try {
            val inflater = LayoutInflater.from(this)
            removal = inflater.inflate(R.layout.remove_floating_widget_layout, null).apply {
                visibility = View.GONE
                windows.addView(this, params(Gravity.TOP or Gravity.END, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS))
            }
            widget = inflater.inflate(R.layout.floating_widget_layout, null).apply {
                windows.addView(this, params(Gravity.TOP or Gravity.LEFT, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE).apply { y = 100 })
                findViewById<View>(R.id.close_floating_view).setOnClickListener { stopSelf() }
                findViewById<View>(R.id.close_expanded_view).setOnClickListener {
                    findViewById<View>(R.id.collapse_view).visibility = View.VISIBLE
                    findViewById<View>(R.id.expanded_container).visibility = View.GONE
                }
                findViewById<View>(R.id.open_activity_button).setOnClickListener {
                    startActivity(Intent(this@FloatingWidgetService, MokedPage::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    stopSelf()
                }
                val root = findViewById<View>(R.id.root_container)
                root.setOnClickListener {
                    findViewById<View>(R.id.collapse_view).visibility = View.GONE
                    findViewById<View>(R.id.expanded_container).visibility = View.VISIBLE
                }
                dragging(root)
            }
        } catch (error: RuntimeException) {
            // The user may revoke overlay access between the check and WindowManager.addView.
            Log.w("FloatingWidgetService", "Cannot attach overlay", error)
            cleanup(); stopSelf(); return START_NOT_STICKY
        }
        val loop = intent.getBooleanExtra("loop", false)
        if (intent.getBooleanExtra("vib", true)) vibrator?.vibrate(longArrayOf(0, 900, 200, 900, 200, 900, 200, 900, 200, 900, 200), if (loop) 0 else -1)
        if (intent.getBooleanExtra("sou", true)) intent.getStringExtra("uri")?.let { play(Uri.parse(it), loop) }
        if (intent.getBooleanExtra("ShabbatM", false)) {
            val seconds = intent.getStringExtra("timeK")?.toLongOrNull()?.coerceIn(0, Int.MAX_VALUE.toLong()) ?: 6
            handler.postDelayed({ cleanup(); stopSelf() }, seconds * 1000)
        }
        return START_REDELIVER_INTENT
    }

    private fun params(gravity: Int, flags: Int) = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
        if (android.os.Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
        flags, PixelFormat.TRANSLUCENT).apply { this.gravity = gravity }

    private fun play(uri: Uri, loop: Boolean) {
        val next = MediaPlayer()
        player = next
        try {
            val preferences = getSharedPreferences("Settings", 0)
            if (preferences.getBoolean("volumeApp", true)) {
                originalVolume = audio.getStreamVolume(AudioManager.STREAM_ALARM)
                audio.setStreamVolume(AudioManager.STREAM_ALARM, preferences.getInt("volumeAppNum",
                    audio.getStreamMaxVolume(AudioManager.STREAM_ALARM)).coerceIn(0, audio.getStreamMaxVolume(AudioManager.STREAM_ALARM)), 0)
            }
            next.setDataSource(this, uri)
            next.setAudioStreamType(AudioManager.STREAM_ALARM)
            next.isLooping = loop
            next.setOnCompletionListener { if (player === next) releaseSound() }
            next.setOnErrorListener { _, _, _ -> if (player === next) releaseSound(); true }
            next.prepare()
            next.start()
        } catch (error: Exception) {
            Log.w("FloatingWidgetService", "Cannot play alert", error)
            releaseSound()
        }
    }
    private fun releaseSound() {
        player?.release(); player = null
        originalVolume?.let { audio.setStreamVolume(AudioManager.STREAM_ALARM, it, 0) }
        originalVolume = null
    }
    private fun cleanup() {
        handler.removeCallbacksAndMessages(null)
        animation?.cancel(); animation = null
        vibrator?.cancel()
        releaseSound()
        for (view in listOfNotNull(widget, removal)) {
            if (view.isAttachedToWindow) windows.removeView(view)
        }
        widget = null; removal = null
    }
    override fun onDestroy() { cleanup(); super.onDestroy() }

    private fun dragging(root: View) {
        var startX = 0f; var startY = 0f; var initialX = 0; var initialY = 0
        var started = 0L; var longPress = false; var overRemoval = false
        val target = requireNotNull(removal)
        val icon = target.findViewById<View>(R.id.remove_img)
        val baseWidth = icon.layoutParams.width
        val baseHeight = icon.layoutParams.height
        val targetWidth = baseWidth + target.paddingLeft + target.paddingRight
        val targetHeight = baseHeight + target.paddingTop + target.paddingBottom
        fun sizeRemoval(highlighted: Boolean) {
            val width = if (highlighted) (baseWidth * 1.5).toInt() else baseWidth
            val height = if (highlighted) (baseHeight * 1.5).toInt() else baseHeight
            if (icon.layoutParams.width != width || icon.layoutParams.height != height) {
                icon.layoutParams = icon.layoutParams.apply { this.width = width; this.height = height }
            }
            val position = target.layoutParams as WindowManager.LayoutParams
            position.x = (screen.x - width) / 2
            position.y = screen.y - (if (highlighted) height else targetHeight) - statusBarHeight()
            update(target, position)
        }
        val showRemoval = Runnable { longPress = true; removal?.visibility = View.VISIBLE }
        root.setOnTouchListener { view, event ->
            val floating = widget ?: return@setOnTouchListener false
            if (removal == null) return@setOnTouchListener false
            val position = floating.layoutParams as WindowManager.LayoutParams
            val removePosition = target.layoutParams as WindowManager.LayoutParams
            val centerX = screen.x / 2
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    sizeRemoval(false)
                    animation?.cancel()
                    startX = event.rawX; startY = event.rawY
                    initialX = position.x; initialY = position.y
                    started = SystemClock.uptimeMillis(); longPress = false; overRemoval = false
                    handler.postDelayed(showRemoval, 600)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    overRemoval = longPress && abs(event.rawX - centerX) <= baseWidth * 1.5 && event.rawY >= screen.y - baseHeight * 1.5
                    // Resize the real WRAP_CONTENT window: scaling its child clips the enlarged ring.
                    sizeRemoval(overRemoval)
                    // Native entry placement uses the unexpanded target's measured bounds. Keep it
                    // stable on subsequent MOVE events rather than feeding enlarged dimensions back in.
                    position.x = if (overRemoval) removePosition.x + abs(targetWidth - floating.width) / 2
                        else initialX + (event.rawX - startX).toInt()
                    position.y = if (overRemoval) removePosition.y + abs(targetHeight - floating.height) / 2
                        else initialY + (event.rawY - startY).toInt()
                    update(floating, position)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    handler.removeCallbacks(showRemoval)
                    target.visibility = View.GONE
                    sizeRemoval(false)
                    if (event.actionMasked == MotionEvent.ACTION_UP && overRemoval) stopSelf()
                    else {
                        if (event.actionMasked == MotionEvent.ACTION_UP && abs(event.rawX - startX) < 20 && abs(event.rawY - startY) < 20 &&
                            SystemClock.uptimeMillis() - started < 300) view.performClick()
                        position.y = position.y.coerceIn(0, (screen.y - floating.height - statusBarHeight()).coerceAtLeast(0))
                        snap(event.rawX.toInt())
                    }
                    longPress = false; overRemoval = false
                    true
                }
                else -> false
            }
        }
    }
    private fun update(view: View, position: WindowManager.LayoutParams) {
        if (!view.isAttachedToWindow) return
        try { windows.updateViewLayout(view, position) }
        catch (error: RuntimeException) { Log.w("FloatingWidgetService", "Overlay detached", error); stopSelf() }
    }
    private fun snap(x: Int) {
        val floating = widget ?: return
        val position = floating.layoutParams as WindowManager.LayoutParams
        left = x <= screen.x / 2
        animation?.cancel()
        animation = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 500
            interpolator = android.view.animation.LinearInterpolator()
            addUpdateListener {
                val fraction = it.animatedValue as Float
                val step = fraction * 100
                val bounce = ((if (left) screen.x - x else x) * exp(-0.055 * step) * cos(0.08 * step)).toInt()
                position.x = if (fraction == 1f) { if (left) 0 else screen.x - floating.width }
                    else if (left) -bounce else screen.x + bounce - floating.width
                update(floating, position)
            }
            start()
        }
    }
    private fun statusBarHeight() = kotlin.math.ceil(25 * resources.displayMetrics.density.toDouble()).toInt()
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        windows.defaultDisplay.getSize(screen)
        val floating = widget ?: return
        val position = floating.layoutParams as WindowManager.LayoutParams
        position.y = position.y.coerceIn(0, (screen.y - floating.height - statusBarHeight()).coerceAtLeast(0))
        position.x = if (left) 0 else (screen.x - floating.width).coerceAtLeast(0)
        update(floating, position)
    }
}
