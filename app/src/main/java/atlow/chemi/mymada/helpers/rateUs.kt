package atlow.chemi.mymada.helpers

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AlertDialog
import atlow.chemi.mymada.R
import atlow.chemi.mymada.integrations.SdkAnalytics

open class rateUs {
    companion object {
        private const val APP_PACKAGE_NAME = "atlow.chemi.mymada"
        private const val DAYS_UNTIL_PROMPT = 3
        private const val LAUNCH_UNTIL_PROMPT = 5

        @JvmStatic fun app_launched(context: Context) {
            val prefs = context.getSharedPreferences("rate_app", 0)
            if (prefs.getBoolean("dontshowagain", false)) return
            val editor = prefs.edit()
            val launchCount = prefs.getLong("launch_count", 0) + 1
            editor.putLong("launch_count", launchCount)
            var firstLaunch = prefs.getLong("date_first_launch", 0)
            if (firstLaunch == 0L) {
                firstLaunch = System.currentTimeMillis()
                editor.putLong("date_first_launch", firstLaunch)
            }
            if (launchCount >= LAUNCH_UNTIL_PROMPT) {
                SdkAnalytics.ratePrompt(context, launchCount, firstLaunch)
                Log.e("Launch:", "number: $launchCount")
                if (System.currentTimeMillis() >= firstLaunch + DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000) {
                    showRateDialog(context, editor)
                }
            }
            editor.apply()
        }

        private fun showRateDialog(context: Context, editor: SharedPreferences.Editor) {
            val title = context.getString(R.string.app_name)
            AlertDialog.Builder(context, R.style.AlertDialogStyle)
                .setMessage("אם אתם נהנים מ$title, נשמח שתקחו רגע אחד ותדרגו אותה בחנות. " +
                    "תודה לכם על תמיכתכם, היא חשובה לנו מאוד!")
                .setTitle("דרג את $title")
                .setIcon(context.applicationInfo.icon)
                .setCancelable(false)
                .setPositiveButton(context.getString(R.string.dialog_rate_yes)) { dialog, _ ->
                    editor.putBoolean("dontshowagain", true)
                    editor.commit()
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$APP_PACKAGE_NAME")))
                    } catch (_: ActivityNotFoundException) {
                        // Preserve the installed behavior when no market handler is available.
                    }
                    dialog.dismiss()
                }
                .setNeutralButton(context.getString(R.string.dialog_no)) { dialog, _ -> dialog.dismiss() }
                .setNegativeButton(context.getString(R.string.dialog_never)) { dialog, _ ->
                    editor.putBoolean("dontshowagain", true)
                    editor.commit()
                    dialog.dismiss()
                }
                .create().show()
        }
    }
}
