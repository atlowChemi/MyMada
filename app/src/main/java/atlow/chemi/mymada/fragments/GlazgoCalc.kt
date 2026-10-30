package atlow.chemi.mymada.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import atlow.chemi.mymada.R
import atlow.chemi.mymada.tools.glasgow.GlasgowScreen

/** Keep the existing Tools destination and Fragment restoration identity. */
class GlazgoCalc : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        ComposeView(requireContext()).apply {
            id = R.id.compose_glasgow
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { GlasgowScreen() }
        }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.button_gl)
    }
}
