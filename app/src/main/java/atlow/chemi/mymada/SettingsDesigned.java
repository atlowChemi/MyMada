package atlow.chemi.mymada;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import java.util.List;
import java.util.Locale;

public class SettingsDesigned extends AppCompatActivity implements View.OnClickListener {
    private SwitchCompat MappSwitch;
    private SharedPreferences sp;
    private static final int REQUEST_PERMISSION_SETTING = 101, OVERLAY_PER_REQ = 345;
    private String[] Langs = new String[] {"עברית", "English"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_designed);
        resetTitle();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        CardView notifications = findViewById(R.id.notif_card);
        CardView advanced = findViewById(R.id.advanced_card);
        CardView tools = findViewById(R.id.tools_card);
        CardView lang = findViewById(R.id.lang_card);
        TextView langArrow = findViewById(R.id.lang_arrow_tv);
        CardView info = findViewById(R.id.info_card);
        notifications.setOnClickListener(this);
        advanced.setOnClickListener(this);
        tools.setOnClickListener(this);
        lang.setOnClickListener(this);
        info.setOnClickListener(this);
        sp = getSharedPreferences("Settings", 0);
        boolean MappBool = sp.getBoolean("Mapp", false);
        MappSwitch = findViewById(R.id.detect_switch);
        MappSwitch.setChecked(MappBool);
        CardView mappCard = findViewById(R.id.detect_card);
        mappCard.setOnClickListener(this);
        if (!isRTL())
            langArrow.setRotation(90);
        CreateOverlayPermissionDialog();
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        SharedPreferences.Editor sp1 = sp.edit();
        sp1.putBoolean("oldU", true);
        sp1.apply();
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
        } catch (PackageManager.NameNotFoundException e) {
            Crashlytics.log(e.getMessage());
        }
    }
    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.notif_card:
                intent = new Intent(SettingsDesigned.this, SettingsDesignedNotifications.class);
                break;
            case R.id.advanced_card:
                intent = new Intent(SettingsDesigned.this, SettingsDesignedAdvanced.class);
                break;
            case R.id.tools_card:
                intent = new Intent(SettingsDesigned.this, SettingsDesignedTools.class);
                break;
            case R.id.detect_card:
                intent = null;
                //start pr stop detection;
                SharedPreferences.Editor s = sp.edit();
                ContentResolver contentResolver = getApplicationContext().getContentResolver();
                String enabledNotificationListeners = android.provider.Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
                String packageName = getApplicationContext().getPackageName();
                if (!MappSwitch.isChecked()) {
                    MappSwitch.setChecked(true);
                    /*new*/
                    Intent serviceIntent = new Intent(this, SMSMonitorService.class);
                    ContextCompat.startForegroundService(this, serviceIntent);
                    /*end new*/
                    Intent test = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                    boolean activityExists = isCallable(test);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        if (activityExists) {
                            try {
                                if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName)) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
                                    builder.setMessage(getResources().getString(R.string.dialog_help)).setCancelable(false)
                                            .setPositiveButton(getResources().getString(R.string.dialog_yes), (dialog, id) -> {
                                                Intent intent1 = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                                                startActivityForResult(intent1, REQUEST_PERMISSION_SETTING);
                                            })
                                            .setNegativeButton(getResources().getString(R.string.dialog_no), (dialog, id) -> dialog.cancel());
                                    final AlertDialog alert = builder.create();
                                    alert.setOnShowListener(dialogInterface -> {
                                        Button canc = alert.getButton(DialogInterface.BUTTON_NEGATIVE);

                                        canc.setTextColor(getApplicationContext().getResources().getColor(R.color.ColorPrimary));
                                    });
                                    alert.show();
                                }
                                s.putBoolean("Mapp", true);
                            } catch (ActivityNotFoundException e) {
                                Intent intenta = new Intent(this, NotificationService.class);
                                Intent old = new Intent(this, NotificationServiceOld.class);
                                stopService(intenta);
                                startService(old);
                                if (!NotificationServiceOld.isAccessibilitySettingsOn(getApplicationContext())) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
                                    builder.setMessage(getResources().getString(R.string.dialog_help)).setCancelable(false)
                                            .setPositiveButton(getResources().getString(R.string.dialog_yes), (dialog, id) -> {
                                                Intent intent12 = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                                                startActivityForResult(intent12, REQUEST_PERMISSION_SETTING);
                                            })
                                            .setNegativeButton(getResources().getString(R.string.dialog_no), (dialog, id) -> dialog.cancel());
                                    final AlertDialog alert = builder.create();
                                    alert.setOnShowListener(dialogInterface -> {
                                        Button canc = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                                        canc.setTextColor(getApplicationContext().getResources().getColor(R.color.ColorPrimary));
                                    });
                                    alert.show();
                                }
                                s.putBoolean("Mapp", true);
                            }
                        } else {
                            Intent intenta = new Intent(this, NotificationService.class);
                            Intent old = new Intent(this, NotificationServiceOld.class);
                            stopService(intenta);
                            startService(old);
                            if (!NotificationServiceOld.isAccessibilitySettingsOn(getApplicationContext())) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
                                builder.setMessage(getResources().getString(R.string.dialog_help)).setCancelable(false)
                                        .setPositiveButton(getResources().getString(R.string.dialog_yes), (dialog, id) -> {
                                            Intent intent13 = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                                            startActivityForResult(intent13, REQUEST_PERMISSION_SETTING);
                                        })
                                        .setNegativeButton(getResources().getString(R.string.dialog_no), (dialog, id) -> dialog.cancel());
                                final AlertDialog alert = builder.create();
                                alert.setOnShowListener(dialogInterface -> {
                                    Button canc = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                                    canc.setTextColor(getApplicationContext().getResources().getColor(R.color.ColorPrimary));
                                });
                                alert.show();
                            }
                            s.putBoolean("Mapp", true);
                        }
                    } else {
                        s.putBoolean("Mapp", true);
                    }
                } else {
                    Intent serviceIntent = new Intent(this, SMSMonitorService.class);
                    stopService(serviceIntent);
                    MappSwitch.setChecked(false);
                    s.putBoolean("Mapp", false);
                }
                s.apply();
                break;
            case R.id.lang_card:
                CreateLangDialog();
                break;
            case R.id.info_card:
                CreateInfoDialog();
                break;
        }
        if (intent != null)
            nextPageMover(intent);
    }
    private void nextPageMover(Intent intent) {
        //Snackbar.make(findViewById(android.R.id.content), "click", Snackbar.LENGTH_LONG).show();
        if (intent!=null)
            startActivity(intent);
    }
    private void CreateLangDialog() {
        final int langToPick = LocalHelper.getLanguage(SettingsDesigned.this).equals("iw") ? 0 : 1;
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsDesigned.this, R.style.AlertDialogStyle);
        builder.setTitle(getString(R.string.pref_title_lang));
        builder.setNegativeButton(getString(R.string.ret), (dialog, which) -> dialog.dismiss());
        builder.setSingleChoiceItems(Langs, langToPick, (dialog, which) -> {
            String locale = which == 0 ? "iw" : "en";
            if (langToPick != which) {
                LocalHelper.setLocale(getApplicationContext(), locale);
                recreate();
            }
        });
        AlertDialog langPicker = builder.create();
        langPicker.show();
    }
    private void CreateInfoDialog() {
        String text = getString(R.string.pref_notifictions_explain_content);
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsDesigned.this, R.style.AlertDialogStyle);
        builder.setTitle(getString(R.string.pref_notifictions_explain)).setMessage(text);
        builder.setNegativeButton(getString(R.string.ok), (dialog, which) -> dialog.dismiss()).create();
        builder.show();
    }
    private void CreateOverlayPermissionDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/opensanshebrew.ttf");
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
            builder.setMessage(getResources().getString(R.string.permDesOverlay)).setCancelable(false)
                    .setTitle(getString(R.string.permTitle))
                    .setPositiveButton(getResources().getString(R.string.permYes), (dialog, id) -> {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, OVERLAY_PER_REQ);
                    }).setNegativeButton(getResources().getString(R.string.dialog_no), (dialog, id) -> dialog.cancel());
            final AlertDialog alert = builder.create();
            TextView textView = alert.findViewById(android.R.id.message);
            if (textView != null)
                textView.setTypeface(myTypeface);
            textView = alert.findViewById(android.R.id.title);
            if (textView != null)
                textView.setTypeface(myTypeface);
            alert.setOnShowListener(dialogInterface -> {
                Button canc = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                Button ok = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                ok.setTypeface(myTypeface);
                canc.setTypeface(myTypeface);
                canc.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.OldPrimary));
            });
            alert.show();
        }
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull  MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private boolean isCallable(Intent intent) {
        try {
            List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            return list.size() > 0;
        }catch (Exception e)    {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean isRTL() {
        return isRTL(Locale.getDefault());
    }
    public static boolean isRTL(Locale locale) {
        final int directionality = Character.getDirectionality(locale.getDisplayName().charAt(0));
        return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT || directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PER_REQ)
            if (resultCode != RESULT_OK)
                CreateOverlayPermissionDialog();
    }
}