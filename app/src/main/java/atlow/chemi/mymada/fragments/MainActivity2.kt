package atlow.chemi.mymada.fragments

import android.app.Dialog
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import atlow.chemi.mymada.R
import atlow.chemi.mymada.SettingsPage
import atlow.chemi.mymada.integrations.DispatchConfirmation
import atlow.chemi.mymada.integrations.TeamContract
import atlow.chemi.mymada.integrations.TeamModel
import com.google.android.material.textfield.TextInputLayout
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import atlow.chemi.mymada.classes.Volunteer
import atlow.chemi.mymada.integrations.TeamScreen

class MainActivity2 : Fragment() {
    private var members by mutableStateOf<List<Volunteer>>(emptyList())
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        ComposeView(requireContext()).apply {
            id = R.id.recycler_view
            if (SettingsPage.isRTL()) rotationY = 180f
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { MaterialTheme {
                TeamScreen(members, onEdit = { index ->
                    if (childFragmentManager.findFragmentByTag("team-member") == null)
                        MemberDialog().apply { arguments = Bundle().apply { putInt("index", index) } }.show(childFragmentManager, "team-member")
                }, onDelete = { ViewModelProvider(this@MainActivity2)[TeamModel::class.java].delete(it) }, onPreview = { ambulance ->
                    if (members.isEmpty()) AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle)
                        .setTitle(R.string.err).setMessage(R.string.SendNoNames).setNegativeButton(R.string.ok, null).show()
                    else {
                        val region = requireContext().getSharedPreferences("Settings", 0).getInt("merhav", 0)
                        DispatchConfirmation.show(this@MainActivity2, TeamContract.body(ambulance, region, members))
                    }
                })
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
        private lateinit var name: EditText
        private lateinit var role: Spinner
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            isCancelable = false
            val model = ViewModelProvider(requireParentFragment())[TeamModel::class.java]
            val index = requireArguments().getInt("index", -1)
            val member = model.members.getOrNull(index)
            val content = layoutInflater.inflate(R.layout.add_human, null)
            name = content.findViewById(R.id.addManName)
            role = content.findViewById(R.id.addManSpinner)
            val field = content.findViewById<TextInputLayout>(R.id.addManTIL)
            val font = Typeface.createFromAsset(requireContext().assets, "fonts/opensanshebrew.ttf")
            name.typeface = font
            field.typeface = font
            name.setText(savedInstanceState?.getString("name") ?: member?.name.orEmpty())
            role.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, TeamContract.roles).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            role.setSelection(savedInstanceState?.getInt("role") ?: member?.job ?: 0)
            name.doAfterTextChanged { if (!it.isNullOrBlank()) field.error = null }
            return AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle).setTitle(R.string.Nteam).setView(content)
                .setNegativeButton(R.string.ret, null).setPositiveButton(R.string.ok, null).create().apply {
                    setOnShowListener {
                        getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(requireContext().getColor(R.color.OldPrimary))
                        (getButton(AlertDialog.BUTTON_POSITIVE).parent as View).layoutDirection = View.LAYOUT_DIRECTION_LTR
                        findViewById<TextView>(androidx.appcompat.R.id.alertTitle)?.setTypeface(Typeface.DEFAULT, Typeface.NORMAL)
                        getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                            if (name.text.toString().isBlank()) field.error = getString(R.string.emailNameNone)
                            else if (model.save(index, name.text.toString(), role.selectedItemPosition)) dismiss()
                        }
                    }
                }
        }
        override fun onSaveInstanceState(outState: Bundle) {
            outState.putString("name", name.text.toString())
            outState.putInt("role", role.selectedItemPosition)
            super.onSaveInstanceState(outState)
        }
    }
}
