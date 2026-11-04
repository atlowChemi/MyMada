package atlow.chemi.mymada.settings

import android.app.Dialog
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
        return AlertDialog.Builder(activity, R.style.SettingsNativeAlertTheme)
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
        restoreNativeAlertTextMetrics(alert)
        // AppCompat's button bar uses layoutDirection="locale" (process locale), not the app selection.
        (alert.getButton(AlertDialog.BUTTON_NEGATIVE).parent as android.view.View).layoutDirection =
            resources.configuration.layoutDirection
    }

    companion object {
        @JvmStatic fun show(activity: FragmentActivity, region: Boolean) {
            SettingsChoiceDialog().apply {
                arguments = Bundle().apply { putBoolean("region", region) }
            }.show(activity.supportFragmentManager, "settings-choice")
        }
    }
}

/** Target29 used locale-independent minimums and fallback spacing only for wrapped text. */
internal fun restoreNativeAlertTextMetrics(alert: AlertDialog) {
    if (Build.VERSION.SDK_INT >= 35) {
        // Target35 counts system bars in screenHeightDp, selecting AppCompat's taller h720dp actions.
        val context = alert.context
        val metrics = context.getSystemService(android.view.WindowManager::class.java).currentWindowMetrics
        val bars = metrics.windowInsets.getInsetsIgnoringVisibility(
            android.view.WindowInsets.Type.systemBars() or android.view.WindowInsets.Type.displayCutout())
        val legacy = android.content.res.Configuration(context.resources.configuration).apply {
            screenHeightDp = ((metrics.bounds.height() - bars.top - bars.bottom) / context.resources.displayMetrics.density).toInt()
        }
        val theme = androidx.appcompat.view.ContextThemeWrapper(context.createConfigurationContext(legacy), R.style.SettingsNativeAlertTheme)
        val attributes = theme.obtainStyledAttributes(null, intArrayOf(android.R.attr.minHeight), androidx.appcompat.R.attr.buttonBarButtonStyle, 0)
        val minimum = attributes.getDimensionPixelSize(0, 0)
        attributes.recycle()
        listOfNotNull(alert.getButton(AlertDialog.BUTTON_NEGATIVE), alert.getButton(AlertDialog.BUTTON_POSITIVE)).forEach {
            it.minHeight = minimum
            it.minimumHeight = minimum
        }
    }
    listOfNotNull(alert.findViewById<TextView>(androidx.appcompat.R.id.alertTitle),
        alert.getButton(AlertDialog.BUTTON_NEGATIVE), alert.getButton(AlertDialog.BUTTON_POSITIVE)).forEach { text ->
        if (Build.VERSION.SDK_INT >= 35) text.setLocalePreferredLineHeightForMinimumUsed(false)
        if (Build.VERSION.SDK_INT >= 33) {
            fun updateSpacing() = text.setFallbackLineSpacing(
                android.text.BoringLayout.isBoring(text.text, text.paint) == null || text.lineCount > 1)
            updateSpacing()
            text.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> updateSpacing() }
        }
    }
}
