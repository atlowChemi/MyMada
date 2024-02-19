package atlow.chemi.mymada;

import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import atlow.chemi.mymada.adapters.homeBAdapter;
import atlow.chemi.mymada.classes.HomeB;
import atlow.chemi.mymada.recieversAndServices.NotificationServiceOld;
import atlow.chemi.mymada.recieversAndServices.SMSMonitorService;


public class HomePage extends AppCompatActivity {
    private homeBAdapter adapter;
    private final int OVERLAY_PER_REQ = 345;
    private MenuItem Detect;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        // #region
        RecyclerView rv = findViewById(R.id.rv);
        GridLayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 1);
        List<HomeB> albumList = new ArrayList<>();
        adapter = new homeBAdapter(HomePage.this, albumList);
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(adapter);
        HomeB Tool = new HomeB("tol", R.drawable.new_tol);
        albumList.add(Tool);
        adapter.notifyItemRangeInserted(0, albumList.size());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        // #endregion
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
        alert.show();
    }
}