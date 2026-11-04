package atlow.chemi.mymada.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import atlow.chemi.mymada.integrations.IntegrationTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import atlow.chemi.mymada.R
import atlow.chemi.mymada.SettingsPage
import atlow.chemi.mymada.integrations.DispatchConfirmation
import atlow.chemi.mymada.integrations.FormScreen
import atlow.chemi.mymada.integrations.MedicalCatalog
import atlow.chemi.mymada.integrations.MedicalPickerDialog

class MainActivity1 : Fragment() {
    private var values by mutableStateOf(List(9) { "" })
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        values = savedInstanceState?.getStringArrayList("form-values")?.takeIf { it.size == 9 }
            ?: List(9) { if (it == 8) savedInstanceState?.getString("medical-selection").orEmpty() else "" }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            id = R.id.rap
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                IntegrationTheme {
                    FormScreen(values, { index, text -> update(index, text) }, {
                        MedicalPickerDialog.show(this@MainActivity1, MedicalCatalog.names, MedicalCatalog.ids, MedicalCatalog.categories, values[8])
                    }, { DispatchConfirmation.show(this@MainActivity1, it) })
                }
            }
        }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        childFragmentManager.setFragmentResultListener("medical-selection", viewLifecycleOwner) { _, result ->
            update(8, result.getString("selected").orEmpty())
        }
    }
    private fun update(index: Int, text: String) { values = values.toMutableList().also { it[index] = text } }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putStringArrayList("form-values", ArrayList(values))
        outState.putString("medical-selection", values[8])
        super.onSaveInstanceState(outState)
    }
}
