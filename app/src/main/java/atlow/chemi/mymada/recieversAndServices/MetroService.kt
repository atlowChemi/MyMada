package atlow.chemi.mymada.recieversAndServices

import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.os.*
import android.provider.Settings
import android.service.quicksettings.TileService
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.PendingIntentCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import atlow.chemi.mymada.R
import atlow.chemi.mymada.UtillitiesAndTools
import atlow.chemi.mymada.classes.Metronome
import atlow.chemi.mymada.helpers.Event
import atlow.chemi.mymada.helpers.MetronomeEventCallback

class MetroService : Service() {
    val MetronomeStartEvent = Event<MetronomeEventCallback>()
    val MetronomeStopEvent = Event<MetronomeEventCallback>()
    var timestamp = 0L
        private set
    private var engine: Metronome? = null
    private val main = Handler(Looper.getMainLooper())
    private val session by lazy { getSharedPreferences("metronome", MODE_PRIVATE) }
    private val audioManager by lazy { getSystemService(AudioManager::class.java) }
    private var originalVolume: Int? = null
    private var raisedVolume: Int? = null

    inner class MetroBinder : Binder() {
        fun getService() = this@MetroService
    }
    override fun onBind(intent: Intent?) = MetroBinder()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == STOP) {
            metroStop()
            stopSelf()
            return START_NOT_STICKY
        }
        try {
            ensureChannel(this)
            val content = PendingIntentCompat.getActivity(this, 7,
                Intent(this, UtillitiesAndTools::class.java).putExtra("running", true),
                PendingIntent.FLAG_UPDATE_CURRENT, false)
            val stop = PendingIntentCompat.getBroadcast(this, 9,
                Intent(this, ActionReceiver::class.java).putExtra("action", "stopM"),
                PendingIntent.FLAG_UPDATE_CURRENT, false)
            val notification = NotificationCompat.Builder(this, "runInBg")
                .setContentTitle(getString(R.string.MetroServiceTtl))
                .setContentText(getString(R.string.MetroServiceTxt))
                .setContentIntent(content).setSmallIcon(R.drawable.ic_metronome_tile)
                .setShowWhen(false).setColor(0x32ff3932).setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_LOW).setOngoing(true)
                .addAction(R.drawable.ic_close, getString(R.string.MetroStop), stop).build()
            ServiceCompat.startForeground(this, 13, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            if (engine != null) return START_STICKY

            val now = SystemClock.elapsedRealtime()
            val boot = Settings.Global.getInt(contentResolver, Settings.Global.BOOT_COUNT, -1)
            val sameBoot = session.getInt("boot", -2) == boot
            val savedStart = session.getLong("start", 0)
            timestamp = if (intent == null && sameBoot && savedStart in 1..now) savedStart else now
            val current = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            originalVolume = if (sameBoot && session.contains("volume") && current == max)
                session.getInt("volume", current) else current
            session.edit().putInt("boot", boot).putLong("start", timestamp)
                .putInt("volume", originalVolume!!).commit()
            if (current != max) audioManager.setStreamVolume(AudioManager.STREAM_ALARM, max, 0)
            raisedVolume = if (originalVolume != max) max else null
            val player = Metronome()
            engine = player
            running = true
            publishState()
            MetronomeStartEvent.listeners().forEach { it.metronomeEventHandler(timestamp) }
            Thread({
                try { player.play() }
                catch (error: Exception) { Log.e("MetroService", "Playback failed", error) }
                finally {
                    main.post {
                        if (engine === player) {
                            metroStop()
                            stopSelf()
                        }
                    }
                }
            }, "metronome-audio").start()
            return START_STICKY
        } catch (error: Exception) {
            Log.e("MetroService", "Cannot start metronome", error)
            metroStop()
            stopSelf()
            return START_NOT_STICKY
        }
    }

    fun metroStop() {
        val player = engine
        engine = null
        val wasRunning = running
        running = false
        timestamp = 0
        try { player?.stop() }
        catch (error: Exception) { Log.w("MetroService", "Audio cleanup failed", error) }
        try {
            // Do not undo a later user/call volume change.
            if (raisedVolume != null && audioManager.getStreamVolume(AudioManager.STREAM_ALARM) == raisedVolume)
                originalVolume?.let { audioManager.setStreamVolume(AudioManager.STREAM_ALARM, it, 0) }
        } catch (error: Exception) { Log.w("MetroService", "Volume restore failed", error) }
        originalVolume = null
        raisedVolume = null
        session.edit().clear().commit()
        stopForeground(STOP_FOREGROUND_REMOVE)
        publishState()
        if (wasRunning) MetronomeStopEvent.listeners().forEach { it.metronomeEventHandler(0) }
    }

    private fun publishState() {
        MetroWidget.refresh(this)
        TileService.requestListeningState(this, ComponentName(this, MyTileService::class.java))
        TileService.requestListeningState(this, ComponentName(this, MetronomeTileService::class.java))
        sendBroadcast(Intent(STATE_CHANGED).setPackage(packageName))
    }

    override fun onDestroy() {
        metroStop()
        main.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    companion object {
        private const val STOP = "metronome.stop"
        const val STATE_CHANGED = "atlow.chemi.mymada.metronome.STATE_CHANGED"
        @JvmField @Volatile var running = false

        @JvmStatic fun start(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, MetroService::class.java))
        }
        @JvmStatic fun stop(context: Context) {
            if (running) context.startService(Intent(context, MetroService::class.java).setAction(STOP))
            else context.stopService(Intent(context, MetroService::class.java))
        }
        private fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT < 26) return
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannelGroup(NotificationChannelGroup("grpOthers", "התראות שונות"))
            manager.createNotificationChannel(NotificationChannel("runInBg", "פעילות ברקע",
                NotificationManager.IMPORTANCE_LOW).apply {
                group = "grpOthers"
                enableVibration(false)
                setSound(null, null)
                setShowBadge(false)
            })
        }
    }
}
