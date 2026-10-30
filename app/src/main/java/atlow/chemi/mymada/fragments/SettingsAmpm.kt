package atlow.chemi.mymada.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import atlow.chemi.mymada.R
import atlow.chemi.mymada.settings.DayNightSettings
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog

class SettingsAmpm : Fragment(R.layout.settings_ampm), TimePickerDialog.OnTimeSetListener {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().setTitle(R.string.pref_header_ampm)
        childFragmentManager.setFragmentResultListener("mode", viewLifecycleOwner) { _, _ -> render() }
        view.findViewById<View>(R.id.start_night_pref).setOnClickListener { time("nightStart", 23, R.string.nightStart) }
        view.findViewById<View>(R.id.end_night_pref).setOnClickListener { time("nightEnd", 7, R.string.nightEnd) }
        view.findViewById<View>(R.id.sleepDay_pref).setOnClickListener { mode(false) }
        view.findViewById<View>(R.id.sleepNight_pref).setOnClickListener { mode(true) }
        render()
    }

    override fun onResume() {
        super.onResume()
        // The picker restores its dial state, but its callback is not serialized.
        childFragmentManager.fragments.filterIsInstance<TimePickerDialog>().forEach { it.setOnTimeSetListener(this) }
        render()
    }

    private fun render() {
        val preferences = requireContext().getSharedPreferences("Settings", 0)
        val sounds = resources.getStringArray(R.array.ampm)
        view?.findViewById<TextView>(R.id.sleepDay_Desc)?.text = sounds[preferences.getInt("sleepDay", 0)]
        view?.findViewById<TextView>(R.id.sleepNight_Desc)?.text = sounds[preferences.getInt("sleepNight", 0)]
    }

    private fun mode(night: Boolean) {
        ModeDialog().apply { arguments = Bundle().apply { putBoolean("night", night) } }
            .show(childFragmentManager, "mode")
    }

    private fun time(key: String, default: Int, title: Int) {
        val hour = requireContext().getSharedPreferences("Settings", 0).getInt(key, default)
        TimePickerDialog.newInstance(this, hour, 25, 0, true).apply {
            enableMinutes(false)
            setTitle(this@SettingsAmpm.getString(title))
        }.show(childFragmentManager, key)
    }

    override fun onTimeSet(dialog: TimePickerDialog, hourOfDay: Int, minute: Int, second: Int) {
        val key = dialog.tag
        require(key == "nightStart" || key == "nightEnd")
        require(hourOfDay in 0..23)
        requireContext().getSharedPreferences("Settings", 0).edit().putInt(key, hourOfDay).apply()
    }

    class ModeDialog : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val night = requireArguments().getBoolean("night")
            val preferences = requireContext().getSharedPreferences("Settings", 0)
            return AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle)
                .setTitle(if (night) R.string.notifNight else R.string.notifDay)
                .setNegativeButton(R.string.ret) { _, _ -> dismiss() }
                .setSingleChoiceItems(resources.getStringArray(R.array.ampm),
                    preferences.getInt(if (night) "sleepNight" else "sleepDay", 0)) { _, which ->
                    DayNightSettings.setMode(preferences, night, which)
                    parentFragmentManager.setFragmentResult("mode", Bundle())
                    dismiss()
                }.create()
        }

        override fun onStart() {
            super.onStart()
            val button = (requireDialog() as AlertDialog).getButton(AlertDialog.BUTTON_NEGATIVE)
            (button.parent as View).layoutDirection = resources.configuration.layoutDirection
        }
    }
}
