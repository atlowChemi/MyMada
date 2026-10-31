package atlow.chemi.mymada.helpers;
import androidx.appcompat.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import atlow.chemi.mymada.R;

public class rateUs {

    private final static String APP_PACKAGE_NAME = "atlow.chemi.mymada";

    //	Initialized to 0 and 3 only for test purposes. In real app change this
    private final static int DAYS_UNTIL_PROMPT = 3;
    private final static int LAUNCH_UNTIL_PROMPT = 5;

    public static void app_launched(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("rate_app", 0);
        if(prefs.getBoolean("dontshowagain", false)){
            return;
        }
        SharedPreferences.Editor editor = prefs.edit();
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);
        long date_firstLaunch = prefs.getLong("date_first_launch", 0);
        if(date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_first_launch", date_firstLaunch);
        }

        if(launch_count >= LAUNCH_UNTIL_PROMPT) {
            atlow.chemi.mymada.integrations.SdkAnalytics.ratePrompt(context, launch_count, date_firstLaunch);
            Log.e("Launch:", "number: " + launch_count);
            if(System.currentTimeMillis() >= date_firstLaunch + (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)){
                showRateDialog(context, editor);
            }
        }
        editor.apply();
    }

    private static void showRateDialog(final Context context, final SharedPreferences.Editor editor) {

        Dialog dialog;

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogStyle);
        String APP_TITLE = context.getResources().getString(R.string.app_name);
        String message = "אם אתם נהנים מ"
                + APP_TITLE
                + ", נשמח שתקחו רגע אחד ותדרגו אותה בחנות. "
                + "תודה לכם על תמיכתכם, היא חשובה לנו מאוד!";

        builder.setMessage(message)
                .setTitle("דרג את " + APP_TITLE)
                .setIcon(context.getApplicationInfo().icon)
                .setCancelable(false)
                .setPositiveButton(context.getResources().getString(R.string.dialog_rate_yes), (dialog1, which) -> {
                    editor.putBoolean("dontshowagain", true);
                    editor.commit();
                    try {
                        context.startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=" + APP_PACKAGE_NAME)));
                    }catch(ActivityNotFoundException e) {
//                        Crashlytics.logException(e);
                    }

                    dialog1.dismiss();
                })
                .setNeutralButton(context.getResources().getString(R.string.dialog_no), (dialog12, which) -> dialog12.dismiss())
                .setNegativeButton(context.getResources().getString(R.string.dialog_never), (dialog13, which) -> {
                    if(editor != null) {
                        editor.putBoolean("dontshowagain", true);
                        editor.commit();
                    }
                    dialog13.dismiss();
                });
        dialog = builder.create();
        dialog.show();
    }
}
