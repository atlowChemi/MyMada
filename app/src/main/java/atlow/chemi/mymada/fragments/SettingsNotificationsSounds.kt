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
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import atlow.chemi.mymada.R

class SettingsNotificationsSounds : Fragment(R.layout.settings_notifications_sounds) {
    private val preferences get() = requireContext().getSharedPreferences("Settings", 0)
    private val permission get() = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE
    private fun suffix(type: Int) = if (type == 0) "" else "T$type"
    private fun hasPermission() = ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().setTitle(R.string.pref_header_sound_and_vib)
        renderView(view)
    }

    private fun renderView(view: View) {
        val cut = preferences.getBoolean("cut", false)
        view.findViewById<View>(R.id.true_cut).visibility = if (cut) View.VISIBLE else View.GONE
        view.findViewById<View>(R.id.false_cut).visibility = if (cut) View.GONE else View.VISIBLE
        view.findViewById<View>(R.id.non_critical).apply {
            visibility = if (preferences.getBoolean("callT6", true) || preferences.getBoolean("callT4", true)) View.VISIBLE else View.GONE
            (layoutParams as ConstraintLayout.LayoutParams).topToBottom = if (cut) R.id.true_cut else R.id.false_cut
        }
        bind(view, 0, intArrayOf(R.id.NvibT_pref, R.id.NsouT_pref, R.id.NappT_pref, R.id.NrinT_pref),
            intArrayOf(R.id.NvibT, R.id.NsouT, R.id.NappT), intArrayOf(R.id.NvibT_Desc, R.id.NsouT_Desc, R.id.NappT_Desc))
        bind(view, 1, intArrayOf(R.id.NvibT1_pref, R.id.NsouT1_pref, R.id.NappT1_pref, R.id.NrinT1_pref),
            intArrayOf(R.id.NvibT1, R.id.NsouT1, R.id.NappT1), intArrayOf(R.id.NvibT1_Desc, R.id.NsouT1_Desc, R.id.NappT1_Desc),
            R.id.findT1_pref, R.id.findT1, R.id.findT1_Desc, R.string.findT1_on, R.string.findT1_off)
        bind(view, 2, intArrayOf(R.id.NvibT2_pref, R.id.NsouT2_pref, R.id.NappT2_pref, R.id.NrinT2_pref),
            intArrayOf(R.id.NvibT2, R.id.NsouT2, R.id.NappT2), intArrayOf(R.id.NvibT2_Desc, R.id.NsouT2_Desc, R.id.NappT2_Desc),
            R.id.findT2_pref, R.id.findT2, R.id.findT2_Desc, R.string.findT2_on, R.string.findT2_off)
        bind(view, 3, intArrayOf(R.id.NvibT3_pref, R.id.NsouT3_pref, R.id.NappT3_pref, R.id.NrinT3_pref),
            intArrayOf(R.id.NvibT3, R.id.NsouT3, R.id.NappT3), intArrayOf(R.id.NvibT3_Desc, R.id.NsouT3_Desc, R.id.NappT3_Desc),
            R.id.findT3_pref, R.id.findT3, R.id.findT3_Desc, R.string.findT3_on, R.string.findT3_off)
        bind(view, 4, intArrayOf(R.id.NvibT4_pref, R.id.NsouT4_pref, R.id.NappT4_pref, R.id.NrinT4_pref),
            intArrayOf(R.id.NvibT4, R.id.NsouT4, R.id.NappT4), intArrayOf(R.id.NvibT4_Desc, R.id.NsouT4_Desc, R.id.NappT4_Desc))
    }

    private fun bind(view: View, type: Int, rows: IntArray, switches: IntArray, descriptions: IntArray,
                     callRow: Int = 0, callSwitch: Int = 0, callDescription: Int = 0, callOn: Int = 0, callOff: Int = 0) {
        val keys = listOf("Nvib", "Nsou", "Napp").map { it + suffix(type) }
        val on = intArrayOf(R.string.pref_description_vibrate_on, R.string.pref_description_ringtone_on, R.string.pref_description_app_on)
        val off = intArrayOf(R.string.pref_description_vibrate_off, R.string.pref_description_ringtone_off, R.string.pref_description_app_off)
        fun render() {
            val enabled = callRow == 0 || preferences.getBoolean("callT$type", true)
            val sound = preferences.getBoolean(keys[1], true)
            val custom = preferences.getBoolean(keys[2], false)
            for (i in 0..2) {
                val checked = preferences.getBoolean(keys[i], i != 2)
                view.findViewById<SwitchCompat>(switches[i]).isChecked = checked
                view.findViewById<TextView>(descriptions[i]).setText(if (checked) on[i] else off[i])
            }
            rows.forEachIndexed { i, id -> view.findViewById<View>(id).apply {
                visibility = if (enabled) View.VISIBLE else View.GONE
                isEnabled = when (i) { 2 -> sound; 3 -> sound && custom; else -> true }
                alpha = if (isEnabled) 1f else 0.4f
            } }
            if (callRow != 0) {
                view.findViewById<SwitchCompat>(callSwitch).isChecked = enabled
                view.findViewById<TextView>(callDescription).setText(if (enabled) callOn else callOff)
            }
        }
        for (i in 0..2) view.findViewById<View>(rows[i]).setOnClickListener {
            val checked = !preferences.getBoolean(keys[i], i != 2)
            preferences.edit().putBoolean(keys[i], checked).apply()
            render()
            if (i == 2 && checked) requestAccess(type)
        }
        if (callRow != 0) view.findViewById<View>(callRow).setOnClickListener {
            preferences.edit().putBoolean("callT$type", !preferences.getBoolean("callT$type", true)).apply()
            render()
        }
        view.findViewById<View>(rows[3]).setOnClickListener { pickRingtone(type) }
        render()
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
        view?.let(::renderView)
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
            val button = (requireDialog() as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            (button.parent as View).layoutDirection = resources.configuration.layoutDirection
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val owner = requireParentFragment() as SettingsNotificationsSounds
            val type = requireArguments().getInt("type")
            return AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle)
                .setTitle(R.string.permTitle).setMessage(R.string.permDesSD)
                .setPositiveButton(R.string.permYes) { _, _ ->
                    if (requireArguments().getBoolean("settings")) owner.startActivityForResult(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", requireContext().packageName, null)), 100 + type)
                    else owner.requestPermissions(arrayOf(owner.permission), 100 + type)
                }.setNegativeButton(R.string.ret) { _, _ -> owner.disableCustom(type) }.create()
        }
        override fun onCancel(dialog: android.content.DialogInterface) {
            super.onCancel(dialog)
            (requireParentFragment() as SettingsNotificationsSounds).disableCustom(requireArguments().getInt("type"))
        }
    }
}
