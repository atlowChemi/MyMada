package atlow.chemi.mymada.settings

import android.app.Dialog
import android.content.Intent
import android.content.ActivityNotFoundException
import android.os.Build
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
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import atlow.chemi.mymada.calls.ListenerAccess
import atlow.chemi.mymada.permissions.NotificationPermission
import atlow.chemi.mymada.NotificationService
import atlow.chemi.mymada.R
import atlow.chemi.mymada.recieversAndServices.SMSMonitorService

/** Existing detection contract; current-target permission/service behavior remains #24. */
object DetectionControls {
    fun render(item: MenuItem, enabled: Boolean) {
        item.isChecked = enabled
        item.setIcon(if (enabled) R.drawable.ic_detect_on else R.drawable.ic_detect_off)
    }

    @JvmStatic @JvmOverloads fun toggle(activity: FragmentActivity, item: MenuItem, style: String = "settings") {
        val preferences = activity.getSharedPreferences("Settings", 0)
        val enabled = !(preferences.getBoolean("Mapp", false) && NotificationPermission.granted(activity) && ListenerAccess.selected(activity) != null)
        if (enabled) {
            preferences.edit().putBoolean("Mapp", false).apply()
            activity.stopService(Intent(activity, SMSMonitorService::class.java))
            render(item, false)
            if (activity.supportFragmentManager.findFragmentByTag("enable-detection") == null) {
                activity.supportFragmentManager.beginTransaction().add(Enable().apply {
                    arguments = Bundle().apply { putString("style", style) }
                }, "enable-detection").commit()
            }
        } else {
            activity.stopService(Intent(activity, SMSMonitorService::class.java))
            preferences.edit().putBoolean("Mapp", false).apply()
            render(item, false)
        }
    }

    class Enable : Fragment() {
        private var requested = false
        private val settings = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            complete(NotificationPermission.granted(requireContext()) && ListenerAccess.selected(requireContext()) != null)
        }
        private fun complete(granted: Boolean) {
            if (granted) enable(requireActivity())
            parentFragmentManager.beginTransaction().remove(this).commit()
        }
        override fun onCreate(state: Bundle?) {
            super.onCreate(state)
            requested = state?.getBoolean("requested") ?: false
            parentFragmentManager.setFragmentResultListener("enable-detection-permission", this) { _, result ->
                if (!result.getBoolean("granted")) complete(false)
                else if (ListenerAccess.selected(requireContext()) != null) complete(true)
                else Prompt.show(requireActivity(), R.string.dialog_help, Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS),
                    style = requireArguments().getString("style", "settings"), resultKey = "enable-listener-choice")
            }
            parentFragmentManager.setFragmentResultListener("enable-listener-choice", this) { _, result ->
                if (!result.getBoolean("accepted")) complete(false)
                else {
                    val general = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    val detail = if (Build.VERSION.SDK_INT >= 30) Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS)
                        .putExtra(Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME, ListenerAccess.components(requireContext()).first().flattenToString()) else general
                    try { settings.launch(if (detail.resolveActivity(requireContext().packageManager) != null) detail else general) }
                    catch (_: ActivityNotFoundException) { complete(false) }
                }
            }
        }
        override fun onResume() {
            super.onResume()
            if (!requested) {
                requested = true
                NotificationPermission.request(requireActivity(), "enable-detection-permission")
            }
        }
        override fun onSaveInstanceState(out: Bundle) {
            out.putBoolean("requested", requested)
            super.onSaveInstanceState(out)
        }
    }

    private fun enable(activity: FragmentActivity) {
        if (!NotificationPermission.granted(activity) || ListenerAccess.selected(activity) == null) return
        val preferences = activity.getSharedPreferences("Settings", 0)
        preferences.edit().putBoolean("Mapp", true).apply()
        SMSMonitorService.start(activity)
        activity.invalidateOptionsMenu()
        activity.findViewById<com.google.android.material.navigation.NavigationView>(R.id.navigation)
            ?.menu?.findItem(R.id.detect)?.let { render(it, true) }
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
                    val resultKey = args.getString("resultKey")
                    if (resultKey != null) {
                        parentFragmentManager.setFragmentResult(resultKey, bundleOf("accepted" to true))
                        return@setPositiveButton
                    }
                    val intent = requireNotNull(BundleCompat.getParcelable(args, "intent", Intent::class.java))
                    if (args.getInt("request") != 0) requireActivity().startActivityForResult(intent, args.getInt("request"))
                    else startActivity(intent)
                }.setNegativeButton(R.string.dialog_no) { _, _ ->
                    args.getString("resultKey")?.let { parentFragmentManager.setFragmentResult(it, bundleOf("accepted" to false)) }
                }.create()
        }
        override fun onStart() {
            super.onStart()
            val dialog = requireDialog() as AlertDialog
            val font = Typeface.createFromAsset(requireContext().assets, "fonts/opensanshebrew.ttf")
            val style = requireArguments().getString("style", "settings")
            if (style != "privacy") {
                dialog.findViewById<TextView>(android.R.id.message)?.typeface = font
                dialog.findViewById<TextView>(android.R.id.title)?.typeface = font
            }
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(requireContext(),
                if (style == "home" || requireArguments().getInt("request") == 345) R.color.OldPrimary else R.color.ColorPrimary))
            if (style == "home" || requireArguments().getInt("request") == 345) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).typeface = font
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).typeface = font
            }
            (dialog.getButton(AlertDialog.BUTTON_POSITIVE).parent as View).layoutDirection = resources.configuration.layoutDirection
        }
        companion object {
            fun show(activity: FragmentActivity, message: Int, intent: Intent, title: Int = 0,
                     positive: Int = R.string.dialog_yes, request: Int = 0, style: String = "settings", resultKey: String? = null) {
                val tag = "detection-prompt:$message"
                if (activity.supportFragmentManager.findFragmentByTag(tag) != null) return
                Prompt().apply { arguments = Bundle().apply {
                    putInt("message", message); putInt("title", title); putInt("positive", positive)
                    putInt("request", request); putParcelable("intent", intent)
                    putString("style", style)
                    putString("resultKey", resultKey)
                } }.show(activity.supportFragmentManager, tag)
            }
        }
    }
}
