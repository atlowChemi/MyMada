package atlow.chemi.mymada;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;

import com.crashlytics.android.Crashlytics;

import java.util.List;
import java.util.Locale;

import atlow.chemi.mymada.helpers.LocalHelper;
import atlow.chemi.mymada.recieversAndServices.NotificationServiceOld;
import atlow.chemi.mymada.recieversAndServices.SMSMonitorService;

public class PrivacyActivity extends AppCompatActivity {
    private ProgressBar spinner;
    private WebView webview;
    private String ShowOrHideWebViewInitialUse = "show";
    private boolean isConnected = true;
    private MenuItem Detect;
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resetTitle();
        setContentView(R.layout.activity_privacy);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null) {
            NetworkInfo info = manager.getActiveNetworkInfo();
            if (info == null || !info.isConnectedOrConnecting())
                isConnected = false;
        }
        webview =  findViewById(R.id.webview);
        spinner = findViewById(R.id.progressBar1);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setWebViewClient(new CustomWebViewClient());
        webview.clearHistory();
        webview.getSettings().setBuiltInZoomControls(false);
        String url, heb, en;
        heb = "https://firebasestorage.googleapis.com/v0/b/project-3918384647339420127.appspot.com/o/privacy.html?alt=media&token=a758a10d-14d0-431f-a9bc-4b61b6e25ee0";
        en = "https://firebasestorage.googleapis.com/v0/b/project-3918384647339420127.appspot.com/o/privacy-en.html?alt=media&token=20a59b60-69e2-45b5-b274-4b311442aa0d";
        if(Locale.getDefault().getLanguage().toLowerCase().contains("en"))
            url = en;
        else url = heb;
        webview.loadUrl(url);
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
    private class CustomWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView webview, String url, Bitmap favicon) {
            if (ShowOrHideWebViewInitialUse.equals("show")) {
                webview.setVisibility(webview.INVISIBLE);
            }
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            ShowOrHideWebViewInitialUse = "hide";
            spinner.setVisibility(View.GONE);
            view.setVisibility(webview.VISIBLE);
            SharedPreferences sp = getSharedPreferences("Settings", 0);
            SharedPreferences.Editor sp1 = sp.edit();
            sp1.putBoolean("privacy", true);
            sp1.apply();
            super.onPageFinished(view, url);
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (isConnected)
                return false;
            else {
                String er = "file:///android_asset/err.html";
                if(Locale.getDefault().getLanguage().toLowerCase().contains("en"))
                    er += "?lng=en";
                view.loadUrl(er);
                return true;
            }
        }
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String FailingUrl) {
            String er = "file:///android_asset/err.html";
            if(Locale.getDefault().getLanguage().equals("en"))
                er += "?lng=en";
            view.loadUrl(er);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = getSharedPreferences("Settings", 0);
        if (Detect != null) {
            int icon = sp.getBoolean("Mapp", false) ? R.drawable.ic_detect_on : R.drawable.ic_detect_off;
            Detect.setIcon(icon);
        }
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
    private static final int REQUEST_PERMISSION_SETTING = 101;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences sp = getSharedPreferences("Settings", 0);
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.one:
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
                        if (activityExists) {
                            try {
                                if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName)) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
                                    builder.setMessage(getResources().getString(R.string.dialog_help)).setCancelable(false)
                                            .setPositiveButton(getResources().getString(R.string.dialog_yes), new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                                                    startActivity(intent);
                                                }
                                            })
                                            .setNegativeButton(getResources().getString(R.string.dialog_no), new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            });
                                    final AlertDialog alert = builder.create();
                                    alert.setOnShowListener(new DialogInterface.OnShowListener() {
                                        @Override
                                        public void onShow(DialogInterface dialogInterface) {
                                            Button canc = alert.getButton(DialogInterface.BUTTON_NEGATIVE);

                                            canc.setTextColor(getApplicationContext().getResources().getColor(R.color.ColorPrimary));
                                        }
                                    });
                                    alert.show();
                                }
                                s.putBoolean("Mapp", true);
                            } catch (ActivityNotFoundException e) {
                                Intent intent = new Intent(this, NotificationService.class);
                                Intent old = new Intent(this, NotificationServiceOld.class);
                                stopService(intent);
                                startService(old);
                                if (!NotificationServiceOld.isAccessibilitySettingsOn(getApplicationContext())) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
                                    builder.setMessage(getResources().getString(R.string.dialog_help)).setCancelable(false)
                                            .setPositiveButton(getResources().getString(R.string.dialog_yes), new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                                                    startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                                                }
                                            })
                                            .setNegativeButton(getResources().getString(R.string.dialog_no), new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            });
                                    final AlertDialog alert = builder.create();
                                    alert.setOnShowListener(new DialogInterface.OnShowListener() {
                                        @Override
                                        public void onShow(DialogInterface dialogInterface) {
                                            Button canc = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                                            canc.setTextColor(getApplicationContext().getResources().getColor(R.color.ColorPrimary));
                                        }
                                    });
                                    alert.show();
                                }
                                s.putBoolean("Mapp", true);
                            }
                        } else {
                            Intent intent = new Intent(this, NotificationService.class);
                            Intent old = new Intent(this, NotificationServiceOld.class);
                            stopService(intent);
                            startService(old);
                            if (!NotificationServiceOld.isAccessibilitySettingsOn(getApplicationContext())) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
                                builder.setMessage(getResources().getString(R.string.dialog_help)).setCancelable(false)
                                        .setPositiveButton(getResources().getString(R.string.dialog_yes), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                                                startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                                            }
                                        })
                                        .setNegativeButton(getResources().getString(R.string.dialog_no), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });
                                final AlertDialog alert = builder.create();
                                alert.setOnShowListener(new DialogInterface.OnShowListener() {
                                    @Override
                                    public void onShow(DialogInterface dialogInterface) {
                                        Button canc = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                                        canc.setTextColor(getApplicationContext().getResources().getColor(R.color.ColorPrimary));
                                    }
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
                    item.setChecked(false);
                    s.putBoolean("Mapp", false);
                    item.setIcon(R.drawable.ic_detect_off);
                }
                s.apply();
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
            List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            return list.size() > 0;
        }catch (Exception e)    {
            e.printStackTrace();
            return false;
        }
    }
}
