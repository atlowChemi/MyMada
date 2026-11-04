package atlow.chemi.mymada.home

import android.app.Dialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.BundleCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import atlow.chemi.mymada.R

/** Framework-restored arguments keep the native prompt and its destination across recreation. */
class HomePrompt : DialogFragment() {
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
                if (args.getBoolean("forResult")) {
                    // Keep the installed Home activity's overlay return contract during migration.
                    requireActivity().startActivityForResult(intent, args.getInt("request"))
                } else startActivity(intent)
            }
            .setNegativeButton(R.string.dialog_no) { _, _ -> dismiss() }
            .create()
    }

    override fun onStart() {
        super.onStart()
        val alert = requireDialog() as AlertDialog
        alert.restoreHomeActionMinimumHeight(requireActivity())
        val font = Typeface.createFromAsset(requireContext().assets, "fonts/opensanshebrew.ttf")
        alert.findViewById<TextView>(android.R.id.message)?.typeface = font
        alert.findViewById<TextView>(android.R.id.title)?.typeface = font
        alert.getButton(AlertDialog.BUTTON_POSITIVE).typeface = font
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).apply {
            typeface = font
            setTextColor(ContextCompat.getColor(context, R.color.OldPrimary))
        }
    }

    companion object {
        @JvmStatic fun show(activity: FragmentActivity, message: Int, title: Int, positive: Int,
                            intent: Intent, forResult: Boolean, request: Int) {
            HomePrompt().apply {
                arguments = Bundle().apply {
                    putInt("message", message)
                    putInt("title", title)
                    putInt("positive", positive)
                    putParcelable("intent", intent)
                    putBoolean("forResult", forResult)
                    putInt("request", request)
                }
            }.show(activity.supportFragmentManager, "home-prompt")
        }
    }
}

/** Resolve the same native button qualifier as target29, without changing text size or padding. */
internal fun AlertDialog.restoreHomeActionMinimumHeight(activity: FragmentActivity) {
    if (android.os.Build.VERSION.SDK_INT < 35) return
    val metrics = activity.getSystemService(android.view.WindowManager::class.java).currentWindowMetrics
    val bars = metrics.windowInsets.getInsetsIgnoringVisibility(
        android.view.WindowInsets.Type.systemBars() or android.view.WindowInsets.Type.displayCutout())
    val legacy = android.content.res.Configuration(activity.resources.configuration).apply {
        // Target35 counts system bars in screenHeightDp, selecting AppCompat's taller h720dp minimum.
        screenHeightDp = ((metrics.bounds.height() - bars.top - bars.bottom) /
            activity.resources.displayMetrics.density).toInt()
    }
    val theme = androidx.appcompat.view.ContextThemeWrapper(
        activity.createConfigurationContext(legacy), R.style.AlertDialogStyle)
    val attributes = theme.obtainStyledAttributes(null, intArrayOf(android.R.attr.minHeight),
        androidx.appcompat.R.attr.buttonBarButtonStyle, 0)
    val minimum = attributes.getDimensionPixelSize(0, getButton(AlertDialog.BUTTON_POSITIVE).minHeight)
    attributes.recycle()
    for (which in intArrayOf(AlertDialog.BUTTON_POSITIVE, AlertDialog.BUTTON_NEGATIVE)) {
        getButton(which)?.apply {
            minHeight = minimum
            minimumHeight = minimum
        }
    }
}
