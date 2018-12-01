package atlow.chemi.mymada;
import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.Toast;
public class rateUs {

    private final static String APP_TITLE = "דיווחי מד\"א ירושלים";
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

        Long date_firstLaunch = prefs.getLong("date_first_launch", 0);
        if(date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_first_launch", date_firstLaunch);
        }

        if(launch_count >= LAUNCH_UNTIL_PROMPT) {

            if(System.currentTimeMillis() >= date_firstLaunch + (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)){
                showRateDialog(context, editor);
            }
        }
        editor.apply();
    }

    public static void showRateDialog(final Context context, final SharedPreferences.Editor editor) {

        Dialog dialog = new Dialog(context);

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogStyle);

        String message = "אם אתם נהנים מ"
                + APP_TITLE
                + ", נשמח שתקחו רגע אחד ותדרגו אותה בחנות. "
                + "תודה לכם על תמיכתכם, היא חשובה לנו מאוד!";

        builder.setMessage(message)
                .setTitle("דרג את " + APP_TITLE)
                .setIcon(context.getApplicationInfo().icon)
                .setCancelable(false)
                .setPositiveButton("דרג כעת", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editor.putBoolean("dontshowagain", true);
                        editor.commit();

//					If your app hasn't been uploaded to market you'll get an exception.
//					For test purposes we catch it here and show some text.
                        try {
                            context.startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=" + APP_PACKAGE_NAME)));
                        }catch(ActivityNotFoundException e) {
                            //Toast.makeText(context, "You have pressed Rate Now button", Toast.LENGTH_SHORT).show();
                        }

                        dialog.dismiss();
                    }
                })
                .setNeutralButton(context.getResources().getString(R.string.dialog_no), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText(context, "You have pressed Later button", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("לא תודה", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(editor != null) {
                            editor.putBoolean("dontshowagain", true);
                            editor.commit();
                        }

                        //Toast.makeText(context, "You have pressed No, Thanks button", Toast.LENGTH_SHORT).show();

                        dialog.dismiss();
                    }
                });

        dialog = builder.create();
        dialog.show();
    }

}
