package atlow.chemi.mymada.permissions

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import atlow.chemi.mymada.R

/** Shared foreground UI contract: FragmentResult(resultKey), boolean "granted". */
object NotificationPermission {
    @JvmStatic fun granted(context: Context): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled() &&
            (Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context,
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)

    @JvmStatic fun request(activity: FragmentActivity, resultKey: String) {
        val manager = activity.supportFragmentManager
        if (granted(activity)) {
            manager.setFragmentResult(resultKey, bundleOf("granted" to true))
        } else if (manager.findFragmentByTag("notification-permission") == null) {
            Prompt().apply { arguments = bundleOf("resultKey" to resultKey) }
                .show(manager, "notification-permission")
        }
    }

    class Prompt : DialogFragment() {
        private var denied = false
        private val permission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (granted(requireContext())) finish(true) else { denied = true; render() }
        }
        private val settings = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            finish(granted(requireContext()))
        }
        override fun onCreate(state: Bundle?) {
            super.onCreate(state)
            denied = state?.getBoolean("denied") ?: false
        }
        override fun onSaveInstanceState(out: Bundle) {
            out.putBoolean("denied", denied)
            super.onSaveInstanceState(out)
        }
        override fun onCreateDialog(state: Bundle?): Dialog = AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle)
            .setTitle(R.string.notification_permission_title)
            .setMessage(R.string.notification_permission_explain)
            .setPositiveButton(R.string.notification_permission_allow, null)
            .setNegativeButton(R.string.dialog_no) { _, _ -> finish(false) }.create()

        override fun onStart() { super.onStart(); render() }
        private fun render() {
            val dialog = dialog as? AlertDialog ?: return
            dialog.setMessage(getString(if (denied) R.string.notification_permission_denied else R.string.notification_permission_explain))
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).apply {
                setText(if (denied || Build.VERSION.SDK_INT < 33) R.string.notification_permission_settings else R.string.notification_permission_allow)
                setOnClickListener {
                    if (denied || Build.VERSION.SDK_INT < 33) {
                        settings.launch(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName))
                    } else permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
        override fun onCancel(dialog: DialogInterface) { super.onCancel(dialog); finish(false) }
        private fun finish(allowed: Boolean) {
            parentFragmentManager.setFragmentResult(requireArguments().getString("resultKey")!!, bundleOf("granted" to allowed))
            dismiss()
        }
    }
}
