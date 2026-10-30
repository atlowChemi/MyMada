package atlow.chemi.mymada.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import atlow.chemi.mymada.R
import atlow.chemi.mymada.classes.Contraction
import atlow.chemi.mymada.contractions.ContractionEntry
import atlow.chemi.mymada.contractions.ContractionScreen
import atlow.chemi.mymada.contractions.ContractionSession
import atlow.chemi.mymada.settings.ContractionHistory
import java.util.Date

/** Keep the installed Fragment name and the #45 migration writer. */
class ContracTimer : Fragment() {
    private lateinit var history: ContractionHistory
    private lateinit var session: ContractionSession
    private var recovery: List<ContractionEntry> = emptyList()
    private var dialog by mutableIntStateOf(0)
    private var revision by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().setTitle(R.string.button_f)
        @Suppress("DEPRECATION")
        setHasOptionsMenu(true)
        // The tools drawer reuses removed Fragment instances when returning to a tool.
        if (::session.isInitialized && savedInstanceState == null) return
        history = ContractionHistory(requireContext().getSharedPreferences("Settings", 0))
        val saved = history.read()
        recovery = saved?.let { value ->
            (value.completed + listOfNotNull(value.active)).map {
                ContractionEntry(it.number, it.start.time, it.end?.time, it.oldEnd?.time)
            }
        }.orEmpty()
        session = ContractionSession(history.reactNative)
        if (savedInstanceState != null) {
            val rows = (savedInstanceState.getLongArray("contractions") ?: longArrayOf()).toList().chunked(4).map {
                ContractionEntry(it[0].toInt(), it[1], it[2].takeIf { time -> time >= 0 }, it[3].takeIf { time -> time >= 0 })
            }
            session = ContractionSession(savedInstanceState.getBoolean("rn"), rows)
            dialog = savedInstanceState.getInt("dialog")
        } else if (recovery.isNotEmpty() && history.eligible(System.currentTimeMillis())) {
            dialog = R.string.ContracRecover
        } else if (saved != null) clear()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                revision
                ContractionScreen(session.entries, session.reactNative, dialog, onToggle = {
                    session.toggle(System.currentTimeMillis())
                    revision++
                    // RN's eligibility timestamp belongs to disposal, not Start/End.
                    if (!session.reactNative) persist()
                }, onDialog = { confirm ->
                    when (dialog) {
                        R.string.ContracRecover -> if (confirm) {
                            session = ContractionSession(history.reactNative, recovery)
                        } else clear()
                        R.string.ContracDelete -> if (confirm) clear()
                    }
                    dialog = 0
                    revision++
                })
            }
        }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLongArray("contractions", session.entries.flatMap {
            listOf(it.number.toLong(), it.start, it.end ?: -1, it.previous ?: -1)
        }.toLongArray())
        outState.putBoolean("rn", session.reactNative)
        outState.putInt("dialog", dialog)
        super.onSaveInstanceState(outState)
    }

    private fun persist(disposed: Boolean = false) {
        if (dialog == R.string.ContracRecover) return
        val completed = session.entries.filter { it.end != null }.map {
            Contraction(it.number, Date(it.start), Date(it.end!!), it.previous?.let(::Date))
        }
        history.save(completed, session.active?.start?.let(::Date),
            session.active?.number?.minus(1) ?: completed.lastOrNull()?.number ?: 0,
            if (session.reactNative && !disposed) requireContext().getSharedPreferences("Settings", 0)
                .getLong("contractLong", 0) else System.currentTimeMillis())
    }

    private fun clear() {
        history.clear()
        // Discarded history has no provenance: a fresh session uses the native default.
        // Re-open the bridge too, so recreation cannot change the new session's format.
        history = ContractionHistory(requireContext().getSharedPreferences("Settings", 0))
        history.read()
        session = ContractionSession(history.reactNative)
    }

    override fun onStop() { persist(); super.onStop() }

    override fun onDestroy() { persist(disposed = true); super.onDestroy() }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.button_f)
    }

    @Deprecated("Fragment menu bridge")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.contractions_menu, menu)
    }

    @Deprecated("Fragment menu bridge")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId != R.id.action_delete) return super.onOptionsItemSelected(item)
        if (dialog == 0) dialog = if (session.entries.isEmpty()) R.string.ContracDeleteEmpty else R.string.ContracDelete
        return true
    }
}
