package atlow.chemi.mymada.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import atlow.chemi.mymada.R
import atlow.chemi.mymada.documents.DocumentKind
import atlow.chemi.mymada.documents.DocumentsScreen

/** Keep the existing drawer routes and restored Fragment identity. */
class BlsProtocoles : Fragment() {
    private var current by mutableIntStateOf(0)

    fun setCurrentPage(type: Int) {
        require(type in DocumentKind.entries.indices)
        current = type
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        current = savedInstanceState?.getInt("document", current) ?: current
        requireActivity().setTitle(R.string.button_p)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("document", current)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        ComposeView(requireContext()).apply {
            id = R.id.compose_documents
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { key(current) { DocumentsScreen(DocumentKind.entries[current]) } }
        }
}
