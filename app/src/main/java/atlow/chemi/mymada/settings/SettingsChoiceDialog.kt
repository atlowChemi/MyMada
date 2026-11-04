package atlow.chemi.mymada.settings

import android.app.Dialog
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import atlow.chemi.mymada.R
import atlow.chemi.mymada.helpers.LocalHelper
import java.util.Locale

/** Native published choice controls, with FragmentManager-owned restoration. */
class SettingsChoiceDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        val region = requireArguments().getBoolean("region")
        val preferences = activity.getSharedPreferences("Settings", 0)
        val language = Locale.forLanguageTag(LocalHelper.getLanguage(activity)).language
        val selected = if (region) preferences.getInt("merhav", 0) else if (language == "he" || language == "iw") 0 else 1
        val choices = if (region) arrayOf("ירושלים", "איילון", "אשר", "גלבוע", "דן", "ירדן", "ירקון", "כרמל", "לכיש", "נגב", "שרון", "מוקד 4")
            else arrayOf("עברית", "English")
        return AlertDialog.Builder(activity, R.style.AlertDialogStyle)
            .setTitle(if (region) R.string.pref_title_region else R.string.pref_title_lang)
            .setNegativeButton(R.string.ret) { _, _ -> dismiss() }
            .setSingleChoiceItems(choices, selected) { _, which ->
                dismiss()
                if (region) {
                    preferences.edit().putBoolean("pickedMoked", true).putInt("merhav", which).apply()
                } else if (selected != which) {
                    LocalHelper.setLocale(activity, if (which == 0) "he" else "en")
                    if (Build.VERSION.SDK_INT < 33) activity.recreate()
                }
            }.create()
    }

    override fun onStart() {
        super.onStart()
        val alert = requireDialog() as AlertDialog
        // AppCompat's button bar uses layoutDirection="locale" (process locale), not the app selection.
        (alert.getButton(AlertDialog.BUTTON_NEGATIVE).parent as android.view.View).layoutDirection =
            resources.configuration.layoutDirection
        if (requireArguments().getBoolean("region")) return
        val font = Typeface.createFromAsset(requireContext().assets, "fonts/opensanshebrew.ttf")
        alert.findViewById<TextView>(android.R.id.title)?.typeface = font
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).typeface = font
    }

    companion object {
        @JvmStatic fun show(activity: FragmentActivity, region: Boolean) {
            SettingsChoiceDialog().apply {
                arguments = Bundle().apply { putBoolean("region", region) }
            }.show(activity.supportFragmentManager, "settings-choice")
        }
    }
}
