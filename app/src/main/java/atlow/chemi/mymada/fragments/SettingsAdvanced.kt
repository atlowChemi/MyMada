package atlow.chemi.mymada.fragments

import android.app.Dialog
import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import atlow.chemi.mymada.R
import atlow.chemi.mymada.SettingsPage
import com.google.android.material.textfield.TextInputLayout

class SettingsAdvanced : Fragment(R.layout.settings_advanced) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().setTitle(R.string.pref_header_advanced)
        val preferences = requireContext().getSharedPreferences("Settings", 0)
        fun bind(rowId: Int, switchId: Int, descriptionId: Int, key: String,
                 default: Boolean, on: Int, off: Int) {
            val toggle = view.findViewById<SwitchCompat>(switchId)
            fun render() {
                toggle.isChecked = preferences.getBoolean(key, default)
                view.findViewById<TextView>(descriptionId).setText(if (toggle.isChecked) on else off)
                if (key == "killS") view.findViewById<View>(R.id.shabatTime_pref).apply {
                    isEnabled = toggle.isChecked
                    alpha = if (toggle.isChecked) 1f else 0.4f
                }
            }
            view.findViewById<View>(rowId).setOnClickListener {
                preferences.edit().putBoolean(key, !preferences.getBoolean(key, default)).apply()
                render()
            }
            render()
        }
        bind(R.id.not_pref, R.id.not, R.id.not_Desc, "not", true,
            R.string.pref_description_not_on, R.string.pref_description_not_off)
        bind(R.id.newWin_pref, R.id.newWin, R.id.newWin_Desc, "win", true,
            R.string.pref_description_newWin_on, R.string.pref_description_newWin_off)
        bind(R.id.ride_pref, R.id.ride, R.id.ride_Desc, "ride", false,
            R.string.pref_description_ride_on, R.string.pref_description_ride_off)
        bind(R.id.loop_pref, R.id.loop, R.id.loop_Desc, "loop", false,
            R.string.pref_description_loop_on, R.string.pref_description_loop_off)
        bind(R.id.shabat_pref, R.id.shabat, R.id.shabat_Desc, "killS", false,
            R.string.pref_description_shabat_on, R.string.pref_description_shabat_off)
        view.findViewById<View>(R.id.shabatTime_pref).setOnClickListener {
            DelayDialog().show(childFragmentManager, "delay")
        }
        view.findViewById<View>(R.id.volume_pref).setOnClickListener {
            VolumeDialog().show(childFragmentManager, "volume")
        }
        view.findViewById<View>(R.id.ampm_card).setOnClickListener {
            (requireActivity() as SettingsPage).displayFragment(SettingsAmpm(), R.id.ampm)
        }
    }

    class DelayDialog : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val content = layoutInflater.inflate(R.layout.shabat_t, null)
            val preferences = requireContext().getSharedPreferences("Settings", 0)
            content.findViewById<EditText>(R.id.shabatT).setText(preferences.getString("timeK", "6"))
            return AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle).setView(content)
                .setNegativeButton(R.string.ret, null).setPositiveButton(R.string.ok, null).create()
        }

        override fun onStart() {
            super.onStart()
            val dialog = requireDialog() as AlertDialog
            val ok = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            (ok.parent as View).layoutDirection = resources.configuration.layoutDirection
            ok.setOnClickListener {
                val text = dialog.findViewById<EditText>(R.id.shabatT)!!.text.toString().trim()
                val seconds = text.toIntOrNull()
                // Both existing alarm consumers multiply seconds by 1000 as an Int.
                if (seconds == null || seconds !in 0..Int.MAX_VALUE / 1000) {
                    dialog.findViewById<TextInputLayout>(R.id.shabatTil)!!.error = getString(R.string.numberNone)
                } else {
                    requireContext().getSharedPreferences("Settings", 0).edit()
                        .putString("timeK", seconds.toString()).apply()
                    dismiss()
                }
            }
        }
    }

    class VolumeDialog : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val content = layoutInflater.inflate(R.layout.volume_dialog, null)
            val preferences = requireContext().getSharedPreferences("Settings", 0)
            val toggle = content.findViewById<SwitchCompat>(R.id.volume_app)
            val slider = content.findViewById<SeekBar>(R.id.VolumeSeekBar)
            slider.max = (requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager)
                .getStreamMaxVolume(AudioManager.STREAM_ALARM)
            slider.progress = savedInstanceState?.getInt("volume")
                ?: preferences.getInt("volumeAppNum", slider.max)
            toggle.isChecked = savedInstanceState?.getBoolean("override")
                ?: preferences.getBoolean("volumeApp", true)
            fun render() {
                content.findViewById<TextView>(R.id.volume_app_Desc).setText(if (toggle.isChecked)
                    R.string.pref_description_volume_app_on else R.string.pref_description_volume_app_off)
            }
            render()
            content.findViewById<View>(R.id.volume_app_pref).setOnClickListener {
                toggle.isChecked = !toggle.isChecked
                render()
            }
            return AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle).setView(content)
                .setNegativeButton(R.string.ret, null)
                .setPositiveButton(R.string.ok) { _, _ ->
                    preferences.edit().putBoolean("volumeApp", toggle.isChecked)
                        .putInt("volumeAppNum", slider.progress).apply()
                }.create()
        }

        override fun onSaveInstanceState(outState: Bundle) {
            super.onSaveInstanceState(outState)
            val dialog = requireDialog() as AlertDialog
            outState.putBoolean("override", dialog.findViewById<SwitchCompat>(R.id.volume_app)!!.isChecked)
            outState.putInt("volume", dialog.findViewById<SeekBar>(R.id.VolumeSeekBar)!!.progress)
        }

        override fun onStart() {
            super.onStart()
            val button = (requireDialog() as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            (button.parent as View).layoutDirection = resources.configuration.layoutDirection
        }
    }
}
