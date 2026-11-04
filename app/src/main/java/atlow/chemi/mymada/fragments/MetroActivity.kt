package atlow.chemi.mymada.fragments

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import atlow.chemi.mymada.R
import atlow.chemi.mymada.permissions.NotificationPermission
import atlow.chemi.mymada.recieversAndServices.MetroService
import atlow.chemi.mymada.tools.metronome.MetronomeScreen
import com.google.firebase.analytics.FirebaseAnalytics

/** Content belongs to the visible view lifecycle; the service still owns playback and time. */
class MetroActivity : Fragment() {
    private var service: MetroService? = null
    private var binding = false
    private var running by mutableStateOf(false)
    private var seconds by mutableStateOf(0L)
    private var visible by mutableStateOf(false)
    private val listenerKey = "metro-ui-${System.identityHashCode(this)}"
    private val handler = Handler(Looper.getMainLooper())
    private val refresh = object : Runnable {
        override fun run() {
            render()
            handler.postDelayed(this, 500)
        }
    }
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            service = (binder as MetroService.MetroBinder).getService().also {
                it.MetronomeStartEvent.addListener(listenerKey) { render() }
                it.MetronomeStopEvent.addListener(listenerKey) { render() }
            }
            render()
        }
        override fun onServiceDisconnected(name: ComponentName) {
            service = null
            render()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { MetronomeScreen(seconds, running, visible, ::toggle) }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().setTitle(R.string.button_d)
        requireActivity().supportFragmentManager.setFragmentResultListener("metronome.notifications", viewLifecycleOwner) { _, _ ->
            // Denial must not remove CPR playback: the screen, widget and tiles still provide Stop.
            if (!MetroService.running) {
                try { MetroService.start(requireContext()) }
                catch (error: RuntimeException) {
                    android.util.Log.w("MetroActivity", "Cannot start metronome", error)
                    Toast.makeText(requireContext(), error.localizedMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
        if (savedInstanceState == null && requireActivity().intent.getBooleanExtra("start", false)) {
            requireActivity().intent.removeExtra("start")
            if (!MetroService.running) toggle()
        }
        render()
    }

    private fun toggle() {
        if (MetroService.running) MetroService.stop(requireContext()) else {
            FirebaseAnalytics.getInstance(requireContext()).logEvent(FirebaseAnalytics.Event.SELECT_CONTENT,
                Bundle().apply {
                    putString(FirebaseAnalytics.Param.ITEM_ID, "metro")
                    putString(FirebaseAnalytics.Param.ITEM_NAME, "metronome")
                    putString(FirebaseAnalytics.Param.CONTENT_TYPE, "CPR")
                })
            NotificationPermission.request(requireActivity(), "metronome.notifications")
        }
    }

    override fun onStart() {
        super.onStart()
        visible = true
        binding = requireContext().bindService(Intent(requireContext(), MetroService::class.java),
            connection, Context.BIND_AUTO_CREATE)
        handler.post(refresh)
    }

    override fun onStop() {
        handler.removeCallbacksAndMessages(null)
        service?.MetronomeStartEvent?.removeListener(listenerKey)
        service?.MetronomeStopEvent?.removeListener(listenerKey)
        service = null
        if (binding) requireContext().unbindService(connection)
        binding = false
        visible = false
        super.onStop()
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        visible = false
        super.onDestroyView()
    }

    private fun render() {
        if (view == null) return
        running = MetroService.running
        val start = service?.timestamp ?: 0L
        seconds = if (running && start > 0) ((SystemClock.elapsedRealtime() - start) / 1000).coerceAtLeast(0) else 0
    }
}
