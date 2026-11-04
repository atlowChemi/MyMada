package atlow.chemi.mymada.fragments

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import atlow.chemi.mymada.R
import atlow.chemi.mymada.settings.*

class SettingsNotificationsSounds : Fragment() {
    private val preferences get() = requireContext().getSharedPreferences("Settings", 0)
    private val permission get() = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE
    private fun suffix(type: Int) = if (type == 0) "" else "T$type"
    private fun hasPermission() = ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().setTitle(R.string.pref_header_sound_and_vib)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?) = settingsContent(R.id.compose_sounds) {
        val values = preferences.observeSettings()
        fun checked(key: String, default: Boolean) = values[key] as? Boolean ?: default
        @Composable fun group(type: Int, heading: Int) {
            SettingsHeading(heading)
            val active = type !in 1..3 || checked("callT$type", true)
            if (type in 1..3) {
                val titles = intArrayOf(R.string.findT1, R.string.findT2, R.string.findT3)
                val on = intArrayOf(R.string.findT1_on, R.string.findT2_on, R.string.findT3_on)
                val off = intArrayOf(R.string.findT1_off, R.string.findT2_off, R.string.findT3_off)
                SettingsRow("callT$type", titles[type - 1], stringResource(if (active) on[type - 1] else off[type - 1]), active) {
                    preferences.edit().putBoolean("callT$type", !active).apply()
                }
            }
            if (active) {
                val sound = checked("Nsou" + suffix(type), true)
                val custom = checked("Napp" + suffix(type), false)
                val titles = intArrayOf(R.string.pref_title_vibrate, R.string.pref_title_ringtone, R.string.pref_title_app)
                val on = intArrayOf(R.string.pref_description_vibrate_on, R.string.pref_description_ringtone_on, R.string.pref_description_app_on)
                val off = intArrayOf(R.string.pref_description_vibrate_off, R.string.pref_description_ringtone_off, R.string.pref_description_app_off)
                listOf("Nvib", "Nsou", "Napp").forEachIndexed { index, prefix ->
                    val key = prefix + suffix(type)
                    val enabled = checked(key, index != 2)
                    SettingsRow(key, titles[index], stringResource(if (enabled) on[index] else off[index]), enabled,
                        enabled = index != 2 || sound, top = if (index == 0) 2 else 3, minLines = if (index == 2) 2 else 1) {
                        preferences.edit().putBoolean(key, !enabled).apply()
                        if (index == 2 && !enabled) requestAccess(type)
                    }
                }
                SettingsRow("NuRi" + suffix(type), R.string.pref_title_ringtone, enabled = sound && custom, top = 0, divider = false) { pickRingtone(type) }
            }
        }
        SettingsList {
            if (checked("cut", false)) {
                group(1, R.string.callT1); group(2, R.string.callT2); group(3, R.string.callT3)
            } else group(0, R.string.callT)
            if (checked("callT4", true) || checked("callT6", true)) group(4, R.string.callT4)
        }
    }

    private fun requestAccess(type: Int) {
        if (hasPermission()) return
        val history = requireContext().getSharedPreferences("permissionStatus", 0)
        val rationale = shouldShowRequestPermissionRationale(permission)
        if (rationale || history.getBoolean(permission, false)) {
            AccessDialog().apply { arguments = Bundle().apply {
                putInt("type", type)
                putBoolean("settings", !rationale)
            } }.show(childFragmentManager, "audio-access")
        } else requestPermissions(arrayOf(permission), 100 + type)
        history.edit().putBoolean(permission, true).apply()
    }

    private fun disableCustom(type: Int) {
        preferences.edit().putBoolean("Napp" + suffix(type), false).apply()
    }

    private fun pickRingtone(type: Int) {
        if (!hasPermission()) { requestAccess(type); return }
        val uri = Uri.parse(preferences.getString("NuRi" + suffix(type), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALL).toString()))
        val picker = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            .putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.pick_sound))
            .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
            .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
            .putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, uri)
        try { startActivityForResult(picker, type) } catch (_: ActivityNotFoundException) {
            // Devices without a system picker retain the previously selected URI.
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode in 100..104) {
            if (!hasPermission()) disableCustom(requestCode - 100)
        } else if (requestCode in 0..4 && resultCode == Activity.RESULT_OK) {
            val uri = data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            if (uri != null) preferences.edit().putString("NuRi" + suffix(requestCode), uri.toString()).apply()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode !in 100..104) return
        if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED)
            preferences.edit().putBoolean("allowed", true).apply()
        else disableCustom(requestCode - 100)
    }

    class AccessDialog : DialogFragment() {
        override fun onStart() {
            super.onStart()
            val alert = requireDialog() as AlertDialog
            // Native message TextAppearance selects the system family, bypassing FontsOverride.
            alert.findViewById<android.widget.TextView>(android.R.id.message)?.typeface =
                android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
            restoreNativeAlertTextMetrics(alert)
        }
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val owner = requireParentFragment() as SettingsNotificationsSounds
            val type = requireArguments().getInt("type")
            return AlertDialog.Builder(requireActivity(), R.style.SettingsNativeAlertTheme)
                .setTitle(R.string.permTitle)
                .setMessage(R.string.permDesSD)
                .setNegativeButton(R.string.ret) { _, _ -> owner.disableCustom(type) }
                .setPositiveButton(R.string.permYes) { _, _ ->
                    if (requireArguments().getBoolean("settings")) owner.startActivityForResult(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", requireContext().packageName, null)), 100 + type)
                    else owner.requestPermissions(arrayOf(owner.permission), 100 + type)
                }.create()
        }
        override fun onCancel(dialog: android.content.DialogInterface) {
            super.onCancel(dialog)
            (requireParentFragment() as SettingsNotificationsSounds).disableCustom(requireArguments().getInt("type"))
        }
    }
}
