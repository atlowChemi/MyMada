package atlow.chemi.mymada.integrations

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import atlow.chemi.mymada.R

/** Snapshot the approved recipient/body across rotation; only opens the user's SMS composer. */
class DispatchConfirmation : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false
        val body = requireArguments().getString("body").orEmpty()
        val region = DispatchContract.region(requireArguments().getInt("region"))
        val target = getString(R.string.toDispatch) + DispatchContract.regions[region] + "."
        val prefix = "$target\n\n$body\n\n"
        // ponytail: retain the published 70-character estimate; actual carrier segmentation is separate.
        val count = "${body.length} ${getString(R.string.smsCountChar)} ${1 + body.length / 70} ${getString(R.string.smsCountMsg)}"
        val text = SpannableString(prefix + count).apply {
            setSpan(ForegroundColorSpan(Color.BLUE), 0, target.length, 0)
            setSpan(ForegroundColorSpan(Color.GRAY), prefix.length, length, 0)
        }
        return AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle).setTitle(R.string.suc).setMessage(text)
            .setNegativeButton(R.string.ret, null).setPositiveButton(R.string.ok) { _, _ ->
                try { startActivity(intent(region, body)) }
                catch (_: ActivityNotFoundException) { Toast.makeText(requireContext(), R.string.smsAppUnavailable, Toast.LENGTH_LONG).show() }
            }.create()
    }
    override fun onStart() {
        super.onStart()
        val dialog = requireDialog() as AlertDialog
        val font = Typeface.createFromAsset(requireContext().assets, "fonts/opensanshebrew.ttf")
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).typeface = font
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).apply { typeface = font; setTextColor(requireContext().getColor(R.color.OldPrimary)) }
        (dialog.getButton(AlertDialog.BUTTON_POSITIVE).parent as android.view.View).layoutDirection = android.view.View.LAYOUT_DIRECTION_LTR
    }
    companion object {
        fun intent(region: Int, body: String): Intent {
            require(body.isNotBlank())
            return Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", DispatchContract.phones[DispatchContract.region(region)], null))
                .putExtra("sms_body", body)
        }
        @JvmStatic fun show(fragment: Fragment, body: String) {
            if (body.isBlank() || fragment.parentFragmentManager.findFragmentByTag("dispatch-confirmation") != null) return
            val region = fragment.requireContext().getSharedPreferences("Settings", Context.MODE_PRIVATE).getInt("merhav", 0)
            DispatchConfirmation().apply { arguments = Bundle().apply { putInt("region", region); putString("body", body) } }
                .show(fragment.parentFragmentManager, "dispatch-confirmation")
        }
    }
}
