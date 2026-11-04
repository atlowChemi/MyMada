package atlow.chemi.mymada.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import atlow.chemi.mymada.R
import atlow.chemi.mymada.SettingsPage
import atlow.chemi.mymada.settings.*

class SettingsNotifications : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?) = settingsContent(R.id.compose_notifications) {
        val preferences = requireContext().getSharedPreferences("Settings", 0)
        val values = preferences.observeSettings()
        @Composable fun toggle(key: String, title: Int, default: Boolean, on: Int, off: Int, top: Int = 3) {
            val checked = values[key] as? Boolean ?: default
            SettingsRow(key, title, stringResource(if (checked) on else off), checked, top = top, minLines = 2) {
                preferences.edit().putBoolean(key, !checked).apply()
            }
        }
        SettingsList {
            toggle("cut", R.string.pref_title_cutA, false, R.string.pref_description_cutA_on, R.string.pref_description_cutA_off, 8)
            toggle("stat", R.string.pref_title_state, false, R.string.pref_description_state_on, R.string.pref_description_state_off)
            toggle("statVib", R.string.pref_title_stateVib, false, R.string.pref_description_stateVib_on, R.string.pref_description_stateVib_off)
            SettingsHeading(R.string.title_low_calls)
            toggle("callT6", R.string.findT6, true, R.string.findT6_on, R.string.findT6_off, 8)
            toggle("callT4", R.string.findT4, true, R.string.findT4_on, R.string.findT4_off)
            SettingsLink("sound-settings", R.string.pref_header_sound_and_vib) {
                (requireActivity() as SettingsPage).displayFragment(SettingsNotificationsSounds(), R.id.soundAndVib)
            }
        }
    }

    override fun onViewCreated(view: android.view.View, state: Bundle?) {
        requireActivity().setTitle(R.string.pref_header_notifications)
    }
}
