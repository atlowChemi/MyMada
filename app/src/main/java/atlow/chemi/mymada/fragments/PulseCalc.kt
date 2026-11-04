package atlow.chemi.mymada.fragments

import android.os.Bundle
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
import atlow.chemi.mymada.tools.pulse.PulseScreen
import atlow.chemi.mymada.tools.pulse.PulseTimer
import atlow.chemi.mymada.tools.pulse.pulsePerMinute

class PulseCalc : Fragment() {
    private var input by mutableStateOf("")
    private var result by mutableStateOf("")
    private var invalid by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            input = it.getString("pulse.input", "")
            result = it.getString("pulse.result", "")
            invalid = it.getBoolean("pulse.invalid")
        }
        PulseTimer.restore(requireContext(), savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            id = R.id.pulse_compose
            isFocusableInTouchMode = true
            requestFocus()
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PulseScreen(input, result, invalid, PulseTimer.seconds, onInput = {
                    input = it
                    val bpm = pulsePerMinute(it)
                    invalid = bpm == null
                    if (bpm != null) result = bpm.toString()
                }, onTimer = { PulseTimer.toggle(context) })
            }
        }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.button_c)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("pulse.input", input)
        outState.putString("pulse.result", result)
        outState.putBoolean("pulse.invalid", invalid)
        PulseTimer.save(requireContext(), outState)
    }
}
