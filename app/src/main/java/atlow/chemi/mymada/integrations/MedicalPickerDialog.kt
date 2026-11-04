package atlow.chemi.mymada.integrations

import android.app.Dialog
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import atlow.chemi.mymada.R
import atlow.chemi.mymada.adapters.mCodeAdapt
import atlow.chemi.mymada.classes.mCodeKid

/** Catalog order is retained; selection and search belong to this restorable dialog. */
class MedicalPickerDialog : DialogFragment() {
    private lateinit var adapter: mCodeAdapt
    private lateinit var search: EditText
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = requireArguments()
        val names = requireNotNull(args.getCharSequenceArray("names"))
        val ids = requireNotNull(args.getCharSequenceArray("ids"))
        val categories = requireNotNull(args.getStringArray("categories"))
        require(names.size == ids.size && ids.size == categories.size)
        val selected = (savedInstanceState?.getString("selected") ?: args.getString("selected")).orEmpty().split(", ").toSet()
        adapter = mCodeAdapt(requireContext(), names.indices.map { index ->
            mCodeKid(names[index].toString(), ids[index].toString(), categories[index], index).apply {
                isChecked = id != "sep" && id in selected
            }
        })
        val content = layoutInflater.inflate(R.layout.list_medical, null)
        content.findViewById<ListView>(R.id.userListView).apply {
            adapter = this@MedicalPickerDialog.adapter
            setOnItemClickListener { _, row, position, _ ->
                if (this@MedicalPickerDialog.adapter.isEnabled(position)) {
                    row.findViewById<CheckBox>(R.id.mcName).apply { isChecked = !isChecked }
                    publish()
                }
            }
        }
        search = content.findViewById(R.id.userSearch)
        search.setText(savedInstanceState?.getString("query").orEmpty())
        adapter.filter(search.text.toString())
        search.doAfterTextChanged { adapter.filter(it.toString()) }
        return AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle)
            .setTitle("בחר קודים רפואיים").setView(content)
            .setPositiveButton(R.string.ok) { _, _ -> publish() }.create()
    }
    override fun onStart() {
        super.onStart()
        (requireDialog() as AlertDialog).apply {
            findViewById<TextView>(androidx.appcompat.R.id.alertTitle)?.setTypeface(Typeface.DEFAULT, Typeface.NORMAL)
            (getButton(AlertDialog.BUTTON_POSITIVE).parent as View).layoutDirection = View.LAYOUT_DIRECTION_LTR
        }
    }
    private fun publish() {
        parentFragmentManager.setFragmentResult("medical-selection", Bundle().apply { putString("selected", adapter.getChecked()) })
    }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("selected", adapter.getChecked())
        outState.putString("query", search.text.toString())
        super.onSaveInstanceState(outState)
    }
    companion object {
        @JvmStatic fun show(parent: Fragment, names: Array<CharSequence>, ids: Array<CharSequence>, categories: Array<String>, selected: String) {
            if (parent.childFragmentManager.findFragmentByTag("medical-picker") != null) return
            MedicalPickerDialog().apply {
                arguments = Bundle().apply {
                    putCharSequenceArray("names", names); putCharSequenceArray("ids", ids)
                    putStringArray("categories", categories); putString("selected", selected)
                }
            }.show(parent.childFragmentManager, "medical-picker")
        }
    }
}
