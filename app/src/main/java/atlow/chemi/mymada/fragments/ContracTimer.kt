package atlow.chemi.mymada.fragments

import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
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
import atlow.chemi.mymada.contractions.ContractionAway
import atlow.chemi.mymada.contractions.ContractionEntry
import atlow.chemi.mymada.contractions.ContractionScreen
import atlow.chemi.mymada.contractions.ContractionSession
import atlow.chemi.mymada.contractions.ContractionStore

/** Keep the installed Fragment name; import #45 history without rewriting it. */
class ContracTimer : Fragment() {
    private lateinit var store: ContractionStore
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
        store = ContractionStore(requireContext().getSharedPreferences("Settings", 0))
        val saved = store.read()
        val expired = saved.away?.expired(now()) == true
        recovery = if (expired) store.retire("expired").entries else saved.entries
        session = ContractionSession(if (expired) false else saved.reactNative)
        if (savedInstanceState != null && !expired) {
            val rows = (savedInstanceState.getLongArray("contractions") ?: longArrayOf()).toList().chunked(4).map {
                ContractionEntry(it[0].toInt(), it[1], it[2].takeIf { time -> time >= 0 }, it[3].takeIf { time -> time >= 0 })
            }
            session = ContractionSession(savedInstanceState.getBoolean("rn"), rows)
            dialog = savedInstanceState.getInt("dialog")
        } else if (recovery.isNotEmpty()) {
            dialog = R.string.ContracRecover
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                revision
                ContractionScreen(session.entries, dialog, onToggle = {
                    session.toggle(System.currentTimeMillis())
                    revision++
                    persist()
                }, onDialog = { confirm ->
                    when (dialog) {
                        R.string.ContracRecover -> if (confirm) {
                            session = ContractionSession(session.reactNative, recovery)
                        } else clear("discarded")
                        R.string.ContracDelete -> if (confirm) clear("deleted")
                    }
                    dialog = 0
                    persist()
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

    private fun now() = ContractionAway(System.currentTimeMillis(), SystemClock.elapsedRealtime(),
        Settings.Global.getInt(requireContext().contentResolver, Settings.Global.BOOT_COUNT, -1))

    private fun persist(away: ContractionAway? = null) {
        store.write(ContractionStore.Saved(
            if (dialog == R.string.ContracRecover) recovery else session.entries, session.reactNative, away))
    }

    private fun clear(reason: String) {
        store.retire(reason)
        recovery = emptyList()
        session = ContractionSession(false)
    }

    override fun onPause() { persist(now()); super.onPause() }

    override fun onStop() {
        // Rotation is not time away; destruction must never advance a departure anchor.
        if (requireActivity().isChangingConfigurations) persist()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        if (store.read().away?.expired(now()) == true) {
            clear("expired")
            dialog = 0
            revision++
        }
        persist() // No observed departure if the foreground process is killed abruptly.
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
