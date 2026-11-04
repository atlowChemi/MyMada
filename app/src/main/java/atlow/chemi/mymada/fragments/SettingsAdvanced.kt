package atlow.chemi.mymada.fragments

import android.app.Dialog
import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.os.Build
import android.text.InputType
import android.view.LayoutInflater
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import atlow.chemi.mymada.R
import atlow.chemi.mymada.SettingsPage
import atlow.chemi.mymada.helpers.FixedTextInputEditText
import atlow.chemi.mymada.settings.*
import com.google.android.material.textfield.TextInputLayout

class SettingsAdvanced : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?) = settingsContent(R.id.compose_advanced) {
        val preferences = requireContext().getSharedPreferences("Settings", 0)
        val values = preferences.observeSettings()
        @Composable fun toggle(key: String, title: Int, default: Boolean, on: Int, off: Int, top: Int = 3) {
            val checked = values[key] as? Boolean ?: default
            SettingsRow(key, title, stringResource(if (checked) on else off), checked, top = top, minLines = 2) {
                preferences.edit().putBoolean(key, !checked).apply()
            }
        }
        SettingsList {
            SettingsHeading(R.string.title_notif)
            toggle("not", R.string.pref_title_not, true, R.string.pref_description_not_on, R.string.pref_description_not_off, 8)
            toggle("win", R.string.pref_title_newWin, true, R.string.pref_description_newWin_on, R.string.pref_description_newWin_off)
            toggle("ride", R.string.pref_title_ride, false, R.string.pref_description_ride_on, R.string.pref_description_ride_off)
            toggle("loop", R.string.pref_title_loop, false, R.string.pref_description_loop_on, R.string.pref_description_loop_off)
            toggle("killS", R.string.pref_title_shabat, false, R.string.pref_description_shabat_on, R.string.pref_description_shabat_off)
            SettingsRow("timeK", R.string.pref_title_shabatT, enabled = values["killS"] as? Boolean ?: false) { DelayDialog().show(childFragmentManager, "delay") }
            SettingsRow("volume", R.string.pref_title_volume, divider = false) { VolumeDialog().show(childFragmentManager, "volume") }
            SettingsHeading(R.string.pref_header_more)
            SettingsLink("day-night-settings", R.string.pref_header_ampm) { (requireActivity() as SettingsPage).displayFragment(SettingsAmpm(), R.id.ampm) }
        }
    }

    override fun onViewCreated(view: View, state: Bundle?) { requireActivity().setTitle(R.string.pref_header_advanced) }

    class DelayDialog : DialogFragment() {
        private var draft by mutableStateOf("")
        private var invalid by mutableStateOf(false)
        override fun onStart() {
            super.onStart()
            // AlertDialog inspects its view before Compose creates the numeric editor.
            requireDialog().window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        }
        override fun onCreate(state: Bundle?) {
            super.onCreate(state)
            draft = state?.getString("draft") ?: requireContext().getSharedPreferences("Settings", 0).getString("timeK", "6")!!
            invalid = state?.getBoolean("invalid") ?: false
        }
        override fun onSaveInstanceState(out: Bundle) {
            super.onSaveInstanceState(out)
            out.putString("draft", draft); out.putBoolean("invalid", invalid)
        }
        override fun onCreateDialog(state: Bundle?): Dialog = settingsDialog(R.id.compose_delay_dialog) {
            SettingsDialogContent(cancel = { dismiss() }, confirmLabel = R.string.ok, confirm = {
                val seconds = draft.trim().toIntOrNull()
                // Alarm consumers multiply seconds by 1000 as Int; preserve native zero.
                if (seconds == null || seconds !in 0..Int.MAX_VALUE / 1000) invalid = true
                else {
                    requireContext().getSharedPreferences("Settings", 0).edit().putString("timeK", seconds.toString()).apply()
                    dismiss()
                }
            }) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    // Keep published floating hint/underline/IME geometry; Compose owns the draft and validation.
                    AndroidView(factory = {
                        // Native inflates shabat_t with the activity theme, before creating the dialog.
                        TextInputLayout(requireActivity()).apply {
                            id = R.id.shabatTil
                            addView(FixedTextInputEditText(this.context, null).apply {
                                id = R.id.shabatT; setSingleLine(); inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
                                gravity = Gravity.START
                                // Native target29 does not add the target35 locale minimum line height.
                                if (Build.VERSION.SDK_INT >= 35) setLocalePreferredLineHeightForMinimumUsed(false)
                                hint = getString(R.string.pref_title_shabatT)
                                setText(draft); doAfterTextChanged { draft = it.toString() }
                            })
                        }
                    }, update = { it.error = if (invalid) getString(R.string.numberNone) else null },
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 15.dp))
                }
            }
        }
    }

    class VolumeDialog : DialogFragment() {
        private var override by mutableStateOf(true)
        private var volume by mutableIntStateOf(0)
        override fun onCreate(state: Bundle?) {
            super.onCreate(state)
            val preferences = requireContext().getSharedPreferences("Settings", 0)
            val max = (requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager).getStreamMaxVolume(AudioManager.STREAM_ALARM)
            override = state?.getBoolean("override") ?: preferences.getBoolean("volumeApp", true)
            volume = state?.getInt("volume") ?: preferences.getInt("volumeAppNum", max).coerceIn(0, max)
        }
        override fun onSaveInstanceState(out: Bundle) {
            super.onSaveInstanceState(out)
            out.putBoolean("override", override); out.putInt("volume", volume)
        }
        override fun onCreateDialog(state: Bundle?): Dialog = settingsDialog(R.id.compose_volume_dialog) {
            SettingsDialogContent(cancel = { dismiss() }, confirmLabel = R.string.ok, confirm = {
                requireContext().getSharedPreferences("Settings", 0).edit().putBoolean("volumeApp", override).putInt("volumeAppNum", volume).apply()
                dismiss()
            }) {
                Column(Modifier.padding(top = 10.dp, bottom = 15.dp)) {
                    Box(Modifier.padding(horizontal = 8.dp)) {
                        SettingsRow("volumeApp", R.string.pref_title_volume_app,
                            stringResource(if (override) R.string.pref_description_volume_app_on else R.string.pref_description_volume_app_off),
                            override, top = 8, minLines = 2) { override = !override }
                    }
                    SettingsText(stringResource(R.string.pref_title_volume_pick), 14, Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp))
                    AndroidView(factory = { context ->
                        AppCompatSeekBar(ContextThemeWrapper(context, R.style.SettingsVolumeSliderTheme)).apply {
                            id = R.id.VolumeSeekBar
                            max = (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager).getStreamMaxVolume(AudioManager.STREAM_ALARM)
                            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                                override fun onProgressChanged(bar: SeekBar, progress: Int, fromUser: Boolean) { if (fromUser) volume = progress }
                                override fun onStartTrackingTouch(bar: SeekBar) = Unit
                                override fun onStopTrackingTouch(bar: SeekBar) = Unit
                            })
                        }
                    }, update = { it.progress = volume }, modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 8.dp))
                }
            }
        }
    }
}
