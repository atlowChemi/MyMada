package atlow.chemi.mymada.tools.pulse

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import androidx.annotation.MainThread
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import atlow.chemi.mymada.classes.AudioGenerator

/** One process-owned measurement, independent of tool views and Activity instances. */
@MainThread
internal object PulseTimer {
    var seconds by mutableStateOf(15)
        private set
    var deadline = 0L
        private set
    private var initialized = false
    private var timer: CountDownTimer? = null

    fun restore(context: Context, saved: Bundle?) {
        // A recreated/new Fragment must not restart a live timer or replay a consumed snapshot.
        if (initialized) return
        initialized = true
        if (saved == null) return
        seconds = saved.getInt("pulse.seconds", 15)
        val end = saved.getLong("pulse.deadline")
        if (end == 0L) return
        if (saved.getInt("pulse.boot", -1) != bootCount(context) || end <= SystemClock.elapsedRealtime()) {
            seconds = 0
            return
        }
        start(context.applicationContext, end)
    }

    fun toggle(context: Context) {
        initialized = true
        if (deadline != 0L) {
            timer?.cancel()
            timer = null
            deadline = 0
            seconds = 15
        } else {
            start(context.applicationContext, SystemClock.elapsedRealtime() + 15_000)
        }
    }

    private fun start(application: Context, end: Long) {
        deadline = end
        seconds = pulseSeconds(end, SystemClock.elapsedRealtime())
        timer = object : CountDownTimer(end - SystemClock.elapsedRealtime(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                seconds = pulseSeconds(end, SystemClock.elapsedRealtime())
            }
            override fun onFinish() {
                if (deadline != end) return
                deadline = 0
                seconds = 0
                timer = null
                playCompletion(application, end)
            }
        }.start()
    }

    private fun playCompletion(application: Context, end: Long) {
        val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val volume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
        val audio = AudioGenerator(8000)
        var created = false
        try {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM) / 2, 0)
            audio.createPlayer()
            created = true
            audio.writeSound(audio.getSineWave(3000, 8000, 9000.25))
            audio.writeSound(audio.getSineWave(3000, 8000, 0.0))
            Log.i("PulseTimer", "Completion audio written for deadline $end")
        } catch (error: Exception) {
            Log.w("PulseTimer", "Unable to play pulse completion", error)
        } finally {
            try {
                if (created) audio.destroyAudioTrack()
            } catch (error: Exception) {
                Log.w("PulseTimer", "Unable to release pulse audio", error)
            } finally {
                try {
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, volume, 0)
                } catch (error: Exception) {
                    Log.w("PulseTimer", "Unable to restore alarm volume", error)
                }
            }
        }
    }

    fun save(context: Context, out: Bundle) {
        out.putInt("pulse.seconds", seconds)
        out.putLong("pulse.deadline", deadline)
        out.putInt("pulse.boot", bootCount(context))
    }

    private fun bootCount(context: Context): Int =
        Settings.Global.getInt(context.contentResolver, Settings.Global.BOOT_COUNT, -1)
}
