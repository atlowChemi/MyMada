package atlow.chemi.mymada.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import atlow.chemi.mymada.R
import atlow.chemi.mymada.SettingsPage

/** Keep published notification controls while moving their persistence to Kotlin. */
class SettingsNotifications : Fragment(R.layout.settings_notifications) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().setTitle(R.string.pref_header_notifications)
        val preferences = requireContext().getSharedPreferences("Settings", 0)

        fun bind(rowId: Int, switchId: Int, descriptionId: Int, key: String,
                 default: Boolean, on: Int, off: Int) {
            val toggle = view.findViewById<SwitchCompat>(switchId)
            val description = view.findViewById<TextView>(descriptionId)
            fun render() {
                toggle.isChecked = preferences.getBoolean(key, default)
                description.setText(if (toggle.isChecked) on else off)
            }
            render()
            view.findViewById<View>(rowId).setOnClickListener {
                preferences.edit().putBoolean(key, !preferences.getBoolean(key, default)).apply()
                render()
            }
        }
        bind(R.id.cutA_pref, R.id.cutAlarm, R.id.cutA_Desc, "cut", false,
            R.string.pref_description_cutA_on, R.string.pref_description_cutA_off)
        bind(R.id.stat_pref, R.id.stat, R.id.stat_Desc, "stat", false,
            R.string.pref_description_state_on, R.string.pref_description_state_off)
        bind(R.id.statVib_pref, R.id.statVib, R.id.statVib_Desc, "statVib", false,
            R.string.pref_description_stateVib_on, R.string.pref_description_stateVib_off)
        bind(R.id.T6_pref, R.id.T6, R.id.T6_Desc, "callT6", true,
            R.string.findT6_on, R.string.findT6_off)
        bind(R.id.findT4_pref, R.id.findT4, R.id.findT4_Desc, "callT4", true,
            R.string.findT4_on, R.string.findT4_off)
        view.findViewById<View>(R.id.sound_vib_card).setOnClickListener {
            (requireActivity() as SettingsPage).displayFragment(SettingsNotificationsSounds(), R.id.soundAndVib)
        }
    }
}
