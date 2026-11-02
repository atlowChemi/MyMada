package atlow.chemi.mymada.fragments

import android.os.Bundle
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import atlow.chemi.mymada.R
import atlow.chemi.mymada.SettingsPage
import atlow.chemi.mymada.integrations.DispatchConfirmation
import atlow.chemi.mymada.integrations.TeamContract
import atlow.chemi.mymada.integrations.TeamModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import atlow.chemi.mymada.integrations.IntegrationTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import atlow.chemi.mymada.classes.Volunteer
import atlow.chemi.mymada.integrations.TeamScreen

class MainActivity2 : Fragment() {
    private var members by mutableStateOf<List<Volunteer>>(emptyList())
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        ComposeView(requireContext()).apply {
            id = R.id.recycler_view
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { IntegrationTheme {
                var emptyError by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
                TeamScreen(members, onEdit = { index ->
                    if (childFragmentManager.findFragmentByTag("team-member") == null)
                        MemberDialog().apply { arguments = Bundle().apply { putInt("index", index) } }.show(childFragmentManager, "team-member")
                }, onDelete = { ViewModelProvider(this@MainActivity2)[TeamModel::class.java].delete(it) }, onPreview = { ambulance ->
                    if (members.isEmpty()) emptyError = true
                    else {
                        val region = requireContext().getSharedPreferences("Settings", 0).getInt("merhav", 0)
                        DispatchConfirmation.show(this@MainActivity2, TeamContract.body(ambulance, region, members))
                    }
                })
                if (emptyError) atlow.chemi.mymada.integrations.IntegrationErrorDialog(getString(R.string.SendNoNames)) {
                    emptyError = false
                }
            } }
        }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val model = ViewModelProvider(this)[TeamModel::class.java]
        model.entries.observe(viewLifecycleOwner) {
            members = model.members
        }
    }

    class MemberDialog : DialogFragment() {
        private var name by mutableStateOf("")
        private var role by mutableStateOf(0)
        private var invalid by mutableStateOf(false)
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            isCancelable = false
            setStyle(STYLE_NORMAL, R.style.AlertDialogStyle)
            val model = ViewModelProvider(requireParentFragment())[TeamModel::class.java]
            val index = requireArguments().getInt("index", -1)
            val member = model.members.getOrNull(index)
            name = savedInstanceState?.getString("name") ?: member?.name.orEmpty()
            role = savedInstanceState?.getInt("role") ?: member?.job ?: 0
        }
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            ComposeView(requireContext()).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    IntegrationTheme {
                        atlow.chemi.mymada.integrations.TeamMemberScreen(name, { name = it; if (it.isNotBlank()) invalid = false },
                            role, { role = it }, invalid, onCancel = { dismiss() }, onSave = {
                            invalid = name.isBlank()
                            if (!invalid && ViewModelProvider(requireParentFragment())[TeamModel::class.java]
                                    .save(requireArguments().getInt("index", -1), name, role)) dismiss()
                        })
                        }
                    }
                }
        override fun onSaveInstanceState(outState: Bundle) {
            outState.putString("name", name)
            outState.putInt("role", role)
            super.onSaveInstanceState(outState)
        }
    }
}
