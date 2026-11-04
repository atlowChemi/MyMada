package atlow.chemi.mymada.settings

import android.app.Dialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.os.BundleCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import atlow.chemi.mymada.NotificationService
import atlow.chemi.mymada.R
import atlow.chemi.mymada.recieversAndServices.NotificationServiceOld
import atlow.chemi.mymada.recieversAndServices.SMSMonitorService

/** Existing detection contract; current-target permission/service behavior remains #24. */
object DetectionControls {
    fun render(item: MenuItem, enabled: Boolean) {
        item.isChecked = enabled
        item.setIcon(if (enabled) R.drawable.ic_detect_on else R.drawable.ic_detect_off)
    }

    fun toggle(activity: FragmentActivity, item: MenuItem) {
        val preferences = activity.getSharedPreferences("Settings", 0)
        val enabled = !preferences.getBoolean("Mapp", false)
        val monitor = Intent(activity, SMSMonitorService::class.java)
        if (enabled) {
            ContextCompat.startForegroundService(activity, monitor)
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            if (intent.resolveActivity(activity.packageManager) != null) {
                if (activity.packageName !in NotificationManagerCompat.getEnabledListenerPackages(activity))
                    Prompt.show(activity, R.string.dialog_help, intent)
            } else {
                activity.stopService(Intent(activity, NotificationService::class.java))
                activity.startService(Intent(activity, NotificationServiceOld::class.java))
                if (!NotificationServiceOld.isAccessibilitySettingsOn(activity.applicationContext))
                    Prompt.show(activity, R.string.dialog_help, Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), request = 101)
            }
        } else activity.stopService(monitor)
        preferences.edit().putBoolean("Mapp", enabled).apply()
        render(item, enabled)
    }

    class Prompt : DialogFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            isCancelable = false
        }
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val args = requireArguments()
            return AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle)
                .setMessage(args.getInt("message"))
                .setTitle(args.getInt("title").takeIf { it != 0 }?.let(::getString))
                .setPositiveButton(args.getInt("positive")) { _, _ ->
                    val intent = requireNotNull(BundleCompat.getParcelable(args, "intent", Intent::class.java))
                    if (args.getInt("request") != 0) requireActivity().startActivityForResult(intent, args.getInt("request"))
                    else startActivity(intent)
                }.setNegativeButton(R.string.dialog_no, null).create()
        }
        override fun onStart() {
            super.onStart()
            val dialog = requireDialog() as AlertDialog
            val font = Typeface.createFromAsset(requireContext().assets, "fonts/opensanshebrew.ttf")
            dialog.findViewById<TextView>(android.R.id.message)?.typeface = font
            dialog.findViewById<TextView>(android.R.id.title)?.typeface = font
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(requireContext(),
                if (requireArguments().getInt("request") == 345) R.color.OldPrimary else R.color.ColorPrimary))
            if (requireArguments().getInt("request") == 345) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).typeface = font
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).typeface = font
            }
            (dialog.getButton(AlertDialog.BUTTON_POSITIVE).parent as View).layoutDirection = resources.configuration.layoutDirection
        }
        companion object {
            fun show(activity: FragmentActivity, message: Int, intent: Intent, title: Int = 0,
                     positive: Int = R.string.dialog_yes, request: Int = 0) {
                val tag = "detection-prompt:$message"
                if (activity.supportFragmentManager.findFragmentByTag(tag) != null) return
                Prompt().apply { arguments = Bundle().apply {
                    putInt("message", message); putInt("title", title); putInt("positive", positive)
                    putInt("request", request); putParcelable("intent", intent)
                } }.show(activity.supportFragmentManager, tag)
            }
        }
    }
}
