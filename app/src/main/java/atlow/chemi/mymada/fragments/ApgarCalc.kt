package atlow.chemi.mymada.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import atlow.chemi.mymada.R
import atlow.chemi.mymada.tools.apgar.ApgarScreen

/** Existing Tools destination and saved Fragment identity; no navigation replacement. */
class ApgarCalc : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        ComposeView(requireContext()).apply {
            id = R.id.compose_apgar
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { ApgarScreen() }
        }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.button_ag)
    }
}
