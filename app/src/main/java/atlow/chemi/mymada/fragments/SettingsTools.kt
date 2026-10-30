package atlow.chemi.mymada.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.LifecycleResumeEffect
import atlow.chemi.mymada.R
import atlow.chemi.mymada.settings.ToolSettingsScreen
import atlow.chemi.mymada.settings.toolOptions
import com.google.android.material.snackbar.Snackbar

/** Retain the saved Fragment identity while Compose owns settings state and layout. */
class SettingsTools : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().setTitle(R.string.pref_header_tools)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        ComposeView(requireContext()).apply {
            id = R.id.compose_tools_settings
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            val preferences = requireContext().getSharedPreferences("Settings", 0)
            val defaults = toolOptions.map { it.first }.toSet()
            setContent {
                var selected by remember { mutableStateOf(preferences.getStringSet("Tools", defaults)!!.toSet()) }
                LifecycleResumeEffect(Unit) {
                    selected = preferences.getStringSet("Tools", defaults)!!.toSet()
                    onPauseOrDispose { }
                }
                ToolSettingsScreen(selected) { key ->
                    val updated = if (key in selected) selected - key else selected + key
                    if (updated.intersect(defaults).isEmpty()) {
                        Snackbar.make(this@apply, R.string.NoPicked, Snackbar.LENGTH_LONG)
                            .setTextColor(android.graphics.Color.WHITE).show()
                    } else {
                        selected = updated
                        preferences.edit().putStringSet("Tools", updated).apply()
                    }
                }
            }
        }
}
