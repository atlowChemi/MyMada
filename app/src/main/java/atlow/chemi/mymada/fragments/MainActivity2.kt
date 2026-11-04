package atlow.chemi.mymada.fragments

import android.app.Dialog
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import atlow.chemi.mymada.R
import atlow.chemi.mymada.SettingsPage
import atlow.chemi.mymada.adapters.VolunteerAdapter
import atlow.chemi.mymada.integrations.DispatchConfirmation
import atlow.chemi.mymada.integrations.TeamContract
import atlow.chemi.mymada.integrations.TeamModel
import com.google.android.material.textfield.TextInputLayout

class MainActivity2 : Fragment(R.layout.activity_main2) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (SettingsPage.isRTL()) view.rotationY = 180f
        else view.findViewById<LinearLayout>(R.id.LinBtns).layoutDirection = View.LAYOUT_DIRECTION_LTR
        val model = ViewModelProvider(this)[TeamModel::class.java]
        val font = Typeface.createFromAsset(requireContext().assets, "fonts/opensanshebrew.ttf")
        val ambulance = view.findViewById<EditText>(R.id.M2txt1E).apply { typeface = font }
        val ambulanceLayout = view.findViewById<TextInputLayout>(R.id.M2ambulance).apply { typeface = font }
        ambulance.doAfterTextChanged { if (!it.isNullOrBlank()) ambulanceLayout.error = null }
        val explain = view.findViewById<TextView>(R.id.explain).apply { typeface = font }
        val add = view.findViewById<Button>(R.id.newManNew)
        (add.background.mutate() as GradientDrawable).setColor(requireContext().getColor(R.color.White))
        add.setTextColor(requireContext().getColor(R.color.ColorNice))
        fun showMember(index: Int) {
            if (childFragmentManager.findFragmentByTag("team-member") == null)
                MemberDialog().apply { arguments = Bundle().apply { putInt("index", index) } }.show(childFragmentManager, "team-member")
        }
        add.setOnClickListener { if (model.members.size < TeamContract.LIMIT) showMember(-1) }
        val adapter = VolunteerAdapter(::showMember, model::delete)
        view.findViewById<RecyclerView>(R.id.recycler_view).apply {
            layoutManager = GridLayoutManager(context, 2)
            rotationY = 180f
            itemAnimator = DefaultItemAnimator()
            this.adapter = adapter
            val spacing = (10 * resources.displayMetrics.density + .5f).toInt()
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(out: Rect, child: View, parent: RecyclerView, state: RecyclerView.State) {
                    val position = parent.getChildAdapterPosition(child)
                    if (position < 0) return
                    val column = position % 2
                    out.set(spacing - column * spacing / 2, if (position < 2) spacing else 0, (column + 1) * spacing / 2, spacing)
                }
            })
        }
        model.entries.observe(viewLifecycleOwner) {
            adapter.members = model.members
            adapter.notifyDataSetChanged()
            add.visibility = if (adapter.itemCount < TeamContract.LIMIT) View.VISIBLE else View.GONE
            explain.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
        }
        view.findViewById<Button>(R.id.M2btn1ENew).apply {
            setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_send, 0)
            setOnClickListener {
                when {
                    ambulance.text.toString().isBlank() -> ambulanceLayout.error = getString(R.string.err_amb)
                    model.members.isEmpty() -> AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle)
                        .setTitle(R.string.err).setMessage(R.string.SendNoNames).setNegativeButton(R.string.ok, null).show()
                    else -> {
                        val region = requireContext().getSharedPreferences("Settings", 0).getInt("merhav", 0)
                        DispatchConfirmation.show(this@MainActivity2, TeamContract.body(ambulance.text.toString(), region, model.members))
                    }
                }
            }
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
