package atlow.chemi.mymada.fragments

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import atlow.chemi.mymada.R
import atlow.chemi.mymada.classes.AudioGenerator
import atlow.chemi.mymada.tools.pulse.PulseScreen
import atlow.chemi.mymada.tools.pulse.pulsePerMinute
import atlow.chemi.mymada.tools.pulse.pulseSeconds

class PulseCalc : Fragment() {
    private var input by mutableStateOf("")
    private var result by mutableStateOf("")
    private var invalid by mutableStateOf(false)
    private var seconds by mutableStateOf(15)
    private var deadline = 0L
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            input = it.getString("pulse.input", "")
            result = it.getString("pulse.result", "")
            invalid = it.getBoolean("pulse.invalid")
            seconds = it.getInt("pulse.seconds", 15)
            deadline = it.getLong("pulse.deadline")
            if (deadline != 0L && it.getInt("pulse.boot", -1) != bootCount()) {
                deadline = 0
                seconds = 0
            }
        }
        if (deadline != 0L) startTimer()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            id = R.id.pulse_compose
            isFocusableInTouchMode = true
            requestFocus()
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PulseScreen(input, result, invalid, seconds, onInput = {
                    input = it
                    val bpm = pulsePerMinute(it)
                    invalid = bpm == null
                    if (bpm != null) result = bpm.toString()
                }, onTimer = {
                    if (deadline != 0L) {
                        timer?.cancel()
                        deadline = 0
                        seconds = 15
                    } else {
                        deadline = SystemClock.elapsedRealtime() + 15_000
                        startTimer()
                    }
                })
            }
        }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.button_c)
    }

    private fun startTimer() {
        timer?.cancel()
        val remaining = deadline - SystemClock.elapsedRealtime()
        if (remaining <= 0) {
            // Restored expired timers show completion without replaying an old alarm.
            deadline = 0
            seconds = 0
            return
        }
        timer = object : CountDownTimer(remaining, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                seconds = pulseSeconds(deadline, SystemClock.elapsedRealtime())
            }
            override fun onFinish() {
                deadline = 0
                seconds = 0
                playCompletion()
            }
        }.start()
    }

    private fun playCompletion() {
        val audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
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
        } catch (error: Exception) {
            Log.w("PulseCalc", "Unable to play pulse completion", error)
        } finally {
            try {
                if (created) audio.destroyAudioTrack()
            } catch (error: Exception) {
                Log.w("PulseCalc", "Unable to release pulse audio", error)
            } finally {
                try {
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, volume, 0)
                } catch (error: Exception) {
                    Log.w("PulseCalc", "Unable to restore alarm volume", error)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("pulse.input", input)
        outState.putString("pulse.result", result)
        outState.putBoolean("pulse.invalid", invalid)
        outState.putInt("pulse.seconds", seconds)
        outState.putLong("pulse.deadline", deadline)
        outState.putInt("pulse.boot", bootCount())
    }

    private fun bootCount(): Int = Settings.Global.getInt(requireContext().contentResolver, Settings.Global.BOOT_COUNT, -1)

    override fun onDestroy() {
        timer?.cancel()
        timer = null
        deadline = 0
        seconds = 15
        super.onDestroy()
    }
}
