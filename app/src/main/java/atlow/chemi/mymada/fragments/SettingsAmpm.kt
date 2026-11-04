package atlow.chemi.mymada.fragments

import android.app.Dialog
import android.os.Bundle
import android.os.Build
import android.text.BoringLayout
import android.widget.TextView
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import atlow.chemi.mymada.R
import atlow.chemi.mymada.settings.DayNightSettings
import atlow.chemi.mymada.settings.*
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog

class SettingsAmpm : Fragment(), TimePickerDialog.OnTimeSetListener {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?) = settingsContent(R.id.compose_day_night) {
        val values = requireContext().getSharedPreferences("Settings", 0).observeSettings()
        val sounds = resources.getStringArray(R.array.ampm)
        SettingsList {
            SettingsHeading(R.string.pref_header_ampm)
            SettingsRow("nightStart", R.string.nightStart, getString(R.string.nightStartDesc), top = 8) { time("nightStart", 23, R.string.nightStart) }
            SettingsRow("nightEnd", R.string.nightEnd, getString(R.string.nightEndDesc), top = 8) { time("nightEnd", 7, R.string.nightEnd) }
            SettingsRow("sleepDay", R.string.notifDay, sounds[values["sleepDay"] as? Int ?: 0], top = 8) { mode(false) }
            SettingsRow("sleepNight", R.string.notifNight, sounds[values["sleepNight"] as? Int ?: 0], top = 8, divider = false) { mode(true) }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().setTitle(R.string.pref_header_ampm)
    }

    override fun onResume() {
        super.onResume()
        // The picker restores its dial state, but its callback is not serialized.
        childFragmentManager.fragments.filterIsInstance<TimePickerDialog>().forEach { it.setOnTimeSetListener(this) }
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
        override fun onStart() {
            super.onStart()
            // AlertController shows this native spacer for a list, but not custom Compose content.
            requireDialog().findViewById<View>(androidx.appcompat.R.id.titleDividerNoCustom).visibility = View.VISIBLE
            val title = requireDialog().findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
            if (Build.VERSION.SDK_INT >= 35) title.setLocalePreferredLineHeightForMinimumUsed(false)
            if (Build.VERSION.SDK_INT >= 33) {
                fun updateSpacing() = title.setFallbackLineSpacing(
                    BoringLayout.isBoring(title.text, title.paint) == null || title.lineCount > 1)
                // Keep native target29 line boxes without replacing DialogTitle's fitting.
                updateSpacing()
                title.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> updateSpacing() }
            }
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val night = requireArguments().getBoolean("night")
            val preferences = requireContext().getSharedPreferences("Settings", 0)
            return settingsDialog(R.id.compose_mode_dialog, getString(if (night) R.string.notifNight else R.string.notifDay),
                R.style.SettingsModeDialogTheme) {
                SettingsDialogContent(cancel = { dismiss() }) {
                    resources.getStringArray(R.array.ampm).forEachIndexed { index, text ->
                        val selected = preferences.getInt(if (night) "sleepNight" else "sleepDay", 0) == index
                        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("mode-$index")
                            .selectable(selected, role = Role.RadioButton) {
                                DayNightSettings.setMode(preferences, night, index)
                                dismiss()
                            }.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                            AndroidView(factory = { AppCompatRadioButton(androidx.appcompat.view.ContextThemeWrapper(it, R.style.AlertDialogStyle)).apply {
                                isClickable = false; isFocusable = false
                                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                            } }, update = { it.isChecked = selected })
                            SettingsText(text, 18, Modifier.padding(start = 20.dp))
                        }
                    }
                }
            }
        }
    }
}
