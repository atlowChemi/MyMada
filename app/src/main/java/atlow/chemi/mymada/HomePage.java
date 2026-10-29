package atlow.chemi.mymada;

import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;

import atlow.chemi.mymada.home.HomeContent;
import atlow.chemi.mymada.helpers.LocalHelper;
import atlow.chemi.mymada.helpers.MyFirebaseMessagingService;
import atlow.chemi.mymada.helpers.rateUs;
import atlow.chemi.mymada.recieversAndServices.NotificationServiceOld;
import atlow.chemi.mymada.recieversAndServices.SMSMonitorService;


/** Installed launcher identity and temporary native onboarding/detection bridge for the Compose home. */
public class HomePage extends AppCompatActivity {
    private Typeface myTypeface;
    public NotificationManager manager;
    private final int OVERLAY_PER_REQ = 345;
    public final String Grp2 = "grpOthers", Grp3 = "grpTog", CnlBg = "runInBg", CnlWin = "notWithWin";
    private MenuItem Detect;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resetTitle();
        setNotifChannels();
        rateUs.app_launched(HomePage.this);
        Context context = HomePage.this;
        MyFirebaseMessagingService.getToken(getApplicationContext());
        String token = getSharedPreferences("Settings", 0).getString("fcm", "empty");
//        Crashlytics.setUserIdentifier(token);
        myTypeface = Typeface.createFromAsset(getAssets(), "fonts/opensanshebrew.ttf");
        final SharedPreferences sharedPreferences = getSharedPreferences("Settings", 0);
        boolean Mapp = sharedPreferences.getBoolean("Mapp", false);
        try {
            Log.e("FCM token: ", token);
        }
        catch (Exception e)    {
            e.printStackTrace();
        }
        FirebaseMessaging.getInstance().subscribeToTopic("news");
        FirebaseAnalytics.getInstance(getApplicationContext());
        HomeContent.install(this);
        boolean Privacy = sharedPreferences.getBoolean("privacy", false);
        boolean oldU = sharedPreferences.getBoolean("oldU", false);
        //If the user wasn't in settings ever, ask him to go there
        if (!oldU)
            ShowDialog(getString(R.string.newSettings),
                    getString(R.string.title_activity_settings),
                    new Intent(getApplicationContext(), SettingsPage.class));

        //If the user is android M or above, make sure he permitted app overlay.
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this))
            ShowDialog(getString(R.string.permDesOverlay),
                    getString(R.string.permTitle),
                    getString(R.string.permYes),
                    new Intent(getApplicationContext(), SettingsPage.class),
                    true, OVERLAY_PER_REQ);
        //If user was never in privacy page, ask him to visit there.
        else if (!Privacy)
            ShowDialog(getString(R.string.privacyMsg),
                    getString(R.string.title_activity_privacy),
                    new Intent(HomePage.this, PrivacyActivity.class));

        if(Mapp && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager = context.getSystemService(NotificationManager.class);
            int importance = 1;
            if (manager != null) {
                Log.e("importance", "is" + manager.getNotificationChannel(CnlBg).getImportance());
                importance = manager.getNotificationChannel(CnlBg).getImportance();
            }
            Log.d("importance", "is " + importance);
            if (importance != NotificationManager.IMPORTANCE_MIN) {
                Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                intent.putExtra(Settings.EXTRA_CHANNEL_ID, CnlBg);
                ShowDialog(getString(R.string.importanceMsg), getString(R.string.importance), intent);
            }
        }
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocalHelper.onAttach(base));
    }
    private void resetTitle() {
        try {
            int label = getPackageManager().getActivityInfo(getComponentName(), PackageManager.GET_META_DATA).labelRes;
            if (label != 0)
                setTitle(label);
        }
        catch (PackageManager.NameNotFoundException e) {
//            Crashlytics.log(e.getMessage());
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = getSharedPreferences("Settings", 0);
        boolean newLang = sp.getBoolean("WasLangChanged", false);
        if (newLang)
            recreate();
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("WasLangChanged", false);
        editor.apply();
        if (Detect != null) {
            int icon = sp.getBoolean("Mapp", false) ? R.drawable.ic_detect_on : R.drawable.ic_detect_off;
            Detect.setIcon(icon);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        SharedPreferences sp = getSharedPreferences("Settings", 0);
        getMenuInflater().inflate(R.menu.popup_menu, menu);
        int icon = sp.getBoolean("Mapp", false) ? R.drawable.ic_detect_on : R.drawable.ic_detect_off;
        Detect = menu.findItem(R.id.one);
        menu.findItem(R.id.one).setCheckable(true).setChecked(sp.getBoolean("Mapp", false)).setIcon(icon);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.one:
                detectionBtn(item);
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsPage.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private boolean isCallable(Intent intent) {
        try {
            return getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0;
        }
        catch (Exception e) {
//            Crashlytics.logException(e);
            return false;
        }
    }
    private void setNotifChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannelGroup group3 = new NotificationChannelGroup(Grp3, "התראות מקרים");
            NotificationChannelGroup group2 = new NotificationChannelGroup(Grp2, "התראות שונות");
            NotificationChannel serviceChannel = new NotificationChannel(CnlBg, "פעילות ברקע", NotificationManager.IMPORTANCE_MIN);
            serviceChannel.enableVibration(false);
            serviceChannel.enableLights(false);
            serviceChannel.setShowBadge(false);
            serviceChannel.setImportance(NotificationManager.IMPORTANCE_MIN);
            serviceChannel.setDescription("חיוני כדי לאפשר זיהוי מקרים");
            serviceChannel.setGroup(Grp2);
            NotificationChannel serviceChannel2 = new NotificationChannel("Push", "הודעות פוש", NotificationManager.IMPORTANCE_HIGH);
            serviceChannel2.enableVibration(true);
            serviceChannel2.setBypassDnd(true);
            serviceChannel2.setDescription("התראות להודעות פוש שישלחו כגון בקשות קוד וכדו'");
            serviceChannel2.enableLights(true);
            serviceChannel2.setShowBadge(true);
            serviceChannel2.setLightColor(Color.RED);
            serviceChannel2.setGroup(Grp2);

            NotificationChannel serviceChannel4 = new NotificationChannel(CnlWin, "זיהוי הודעות", NotificationManager.IMPORTANCE_LOW);
            serviceChannel4.enableVibration(false);
            serviceChannel4.setBypassDnd(false);
            serviceChannel4.setDescription("התראות עבור הודעות כאשר מוגדר להקפיץ גם חלון");
            serviceChannel4.enableLights(false);
            serviceChannel4.setShowBadge(true);
            serviceChannel4.setLightColor(Color.RED);
            serviceChannel4.setGroup(Grp3);

            manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannelGroup(group3);
                manager.createNotificationChannelGroup(group2);
                manager.createNotificationChannel(serviceChannel);
                manager.createNotificationChannel(serviceChannel2);
                manager.createNotificationChannel(serviceChannel4);
            }
        }
    }
    private void detectionBtn(MenuItem item) {
        SharedPreferences sp = getSharedPreferences("Settings", 0);
        SharedPreferences.Editor s = sp.edit();
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        String enabledNotificationListeners = android.provider.Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = getApplicationContext().getPackageName();
        if (!item.isChecked()) {
            item.setChecked(true);
            item.setIcon(R.drawable.ic_detect_on);
            /*new*/
            Intent serviceIntent = new Intent(this, SMSMonitorService.class);
            ContextCompat.startForegroundService(this, serviceIntent);
            /*end new*/
            Intent test = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            boolean activityExists = isCallable(test);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                int REQUEST_PERMISSION_SETTING = 101;
                if (activityExists) {
                    try {
                        if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName)) {
                            ShowDialog(getString(R.string.dialog_help),
                                    getString(R.string.dialog_yes),
                                    new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                        }
                        s.putBoolean("Mapp", true);
                    }
                    catch (ActivityNotFoundException e) {
                        Intent intent = new Intent(this, NotificationService.class);
                        Intent old = new Intent(this, NotificationServiceOld.class);
                        stopService(intent);
                        startService(old);
                        if (!NotificationServiceOld.isAccessibilitySettingsOn(getApplicationContext())) {
                            ShowDialog(getString(R.string.dialog_help),
                                    null,
                                    getString(R.string.dialog_yes),
                                    new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS),
                                    true, REQUEST_PERMISSION_SETTING);
                        }
                        s.putBoolean("Mapp", true);
                    }
                }
                else {
                    Intent intent = new Intent(this, NotificationService.class);
                    Intent old = new Intent(this, NotificationServiceOld.class);
                    stopService(intent);
                    startService(old);
                    if (!NotificationServiceOld.isAccessibilitySettingsOn(getApplicationContext())) {
                        ShowDialog(getString(R.string.dialog_help),
                                null,
                                getString(R.string.dialog_yes),
                                new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS),
                                true, REQUEST_PERMISSION_SETTING);
                    }
                    s.putBoolean("Mapp", true);
                }
            }
            else {
                s.putBoolean("Mapp", true);
            }
        }
        else {
            Intent serviceIntent = new Intent(this, SMSMonitorService.class);
            stopService(serviceIntent);
            item.setChecked(false);
            s.putBoolean("Mapp", false);
            item.setIcon(R.drawable.ic_detect_off);
        }
        s.apply();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PER_REQ &&
                resultCode != RESULT_OK &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                !Settings.canDrawOverlays(this))
                ShowDialog(getString(R.string.permDesOverlay),
                        getString(R.string.permTitle),
                        getString(R.string.permYes),
                        new Intent(getApplicationContext(), SettingsPage.class),
                        true, OVERLAY_PER_REQ);
    }

    /***
     * Show a designed Alert Dialog.
     * @param msg message to show in dialog.
     * @param btnPos string for positive button.
     * @param intent Intent to open up when positive button clicked.
     */
    private void ShowDialog(String msg, String btnPos, Intent intent) {
        ShowDialog(msg, null , btnPos, intent, false, 0);
    }

    /***
     * Show a designed Alert Dialog.
     * @param msg message to show in dialog.
     * @param title message to show in dialog's title.
     * @param btnPos string for positive button.
     * @param intent Intent to open up when positive button clicked.
     * @param resultActivity is the activity a result activity or not
     * @param result result of intent
     */
    private void ShowDialog(String msg, String title, String btnPos, Intent intent, boolean resultActivity, int result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        builder.setMessage(msg).setCancelable(false)
                .setPositiveButton(btnPos, (dialog, id) ->
                {
                    if (resultActivity)
                        startActivityForResult(intent, result);
                    else
                        startActivity(intent);
                })
                .setNegativeButton(getResources().getString(R.string.dialog_no), (dialog, id) -> dialog.cancel());
        if (title != null)
            builder.setTitle(title);
        final AlertDialog alert = builder.create();
        alert.setOnShowListener(dialogInterface -> DialogOnShowListener(alert));
        alert.show();
    }

    private void DialogOnShowListener(AlertDialog alert) {
        TextView textView = alert.findViewById(android.R.id.message);
        if (textView != null)
            textView.setTypeface(myTypeface);
        textView = alert.findViewById(android.R.id.title);
        if (textView != null)
            textView.setTypeface(myTypeface);
        Button cancelButton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        Button ok = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        ok.setTypeface(myTypeface);
        cancelButton.setTypeface(myTypeface);
        cancelButton.setTextColor(getResources().getColor(R.color.OldPrimary));
    }
}
