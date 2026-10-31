package atlow.chemi.mymada.fragments

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Typeface
import android.os.*
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import atlow.chemi.mymada.R
import atlow.chemi.mymada.permissions.NotificationPermission
import atlow.chemi.mymada.recieversAndServices.MetroService
import com.google.firebase.analytics.FirebaseAnalytics
import java.util.Locale

/** Keeps the production layout; views, callbacks and binding belong to the visible view lifecycle. */
class MetroActivity : Fragment(R.layout.activity_metro) {
    private var service: MetroService? = null
    private var binding = false
    private var animated = false
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
        view.findViewById<TextView>(R.id.metroExp).typeface =
            Typeface.createFromAsset(requireContext().assets, "fonts/opensanshebrew.ttf")
        view.isFocusable = true
        view.accessibilityDelegate = object : View.AccessibilityDelegate() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.className = "android.widget.Button"
            }
        }
        view.setOnClickListener {
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
        if (savedInstanceState == null && requireActivity().intent.getBooleanExtra("start", false)) {
            requireActivity().intent.removeExtra("start")
            if (!MetroService.running) view.performClick()
        }
        render()
    }

    override fun onStart() {
        super.onStart()
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
        stopAnimation()
        super.onStop()
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        stopAnimation()
        super.onDestroyView()
    }

    private fun render() {
        val root = view ?: return
        val running = MetroService.running
        val start = service?.timestamp ?: 0L
        val seconds = if (running && start > 0) ((SystemClock.elapsedRealtime() - start) / 1000).coerceAtLeast(0) else 0
        val time = if (seconds >= 3600) String.format(Locale.ROOT, "%02d\n%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60)
            else String.format(Locale.ROOT, "%02d:%02d", seconds / 60, seconds % 60)
        root.findViewById<TextView>(R.id.metro_Clock).text = SpannableString(time).apply {
            if ('\n' in time) setSpan(RelativeSizeSpan(0.6f), time.indexOf('\n'), length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        root.findViewById<ProgressBar>(R.id.metro_progressBar).setProgress((seconds % 120).toInt(), true)
        root.contentDescription = getString(if (running) R.string.MetroStop else R.string.startT)
        if (running && !animated) startAnimation() else if (!running) stopAnimation()
    }

    private fun startAnimation() {
        val circle = view?.findViewById<View>(R.id.startstop) ?: return
        animated = true
        val small = ScaleAnimation(1.05f, 1f, 1.05f, 1f, Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f).apply { duration = 650 }
        val big = ScaleAnimation(1f, 1.05f, 1f, 1.05f, Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f).apply { duration = 350 }
        fun repeat(next: Animation) = object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) { if (animated) circle.startAnimation(next) }
        }
        small.setAnimationListener(repeat(big))
        big.setAnimationListener(repeat(small))
        circle.startAnimation(small)
    }

    private fun stopAnimation() {
        animated = false
        view?.findViewById<View>(R.id.startstop)?.clearAnimation()
    }
}
