package atlow.chemi.mymada;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.splitinstall.SplitInstallManager;
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory;
import com.google.android.play.core.splitinstall.SplitInstallRequest;
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener;
import com.google.android.play.core.splitinstall.model.SplitInstallErrorCode;
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import atlow.chemi.mymada.fragments.SettingsAdvanced;
import atlow.chemi.mymada.fragments.SettingsAmpm;
import atlow.chemi.mymada.fragments.SettingsNotifications;
import atlow.chemi.mymada.fragments.SettingsNotificationsSounds;
import atlow.chemi.mymada.fragments.SettingsTools;
import atlow.chemi.mymada.helpers.LocalHelper;
import atlow.chemi.mymada.helpers.MyDrawerLayout;
import atlow.chemi.mymada.recieversAndServices.NotificationServiceOld;
import atlow.chemi.mymada.recieversAndServices.SMSMonitorService;

public class SettingsPage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private MyDrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private MenuItem Detect;
    private Typeface myTypeface;
    private SharedPreferences sp;
    private int mySessionId = 0;
    private NavigationView navigationView;
    private boolean isDrawerLocked = false;
    private ProgressBar progressBar;
    private TextView progressText;
    private Button posBtn, negBtn;
    private SplitInstallManager splitInstallManager;
    private String[] Langs = new String[] {"עברית", "English"},
            Mokdim = new String[] {"ירושלים","איילון","אשר","גלבוע","דן","ירדן","ירקון","כרמל","לכיש","נגב","שרון","מוקד 4"};
    private static final int REQUEST_PERMISSION_SETTING = 101, OVERLAY_PER_REQ = 345;
    private Fragment settingsNotifications = new SettingsNotifications(), settingsNotifSou = new SettingsNotificationsSounds(),
    settingsAdvanced = new SettingsAdvanced(), settingsAmPm = new SettingsAmpm(), settingsTools = new SettingsTools();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resetTitle();
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        myTypeface = Typeface.createFromAsset(getAssets(), "fonts/opensanshebrew.ttf");

        sp = getSharedPreferences("Settings", 0);
        boolean MappBool = sp.getBoolean("Mapp", false);

        drawerLayout = findViewById(R.id.activity_Utils);
        navigationView = findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.inflateMenu(R.menu.settings_menu);
        if (isDeviceTablet()) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
            drawerLayout.setScrimColor(Color.TRANSPARENT);
            isDrawerLocked = true;
        }
        else {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            drawerLayout.setScrimColor(0x99000000);
            isDrawerLocked = false;
        }
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        Menu navMenu = navigationView.getMenu();
        Detect = navMenu.findItem(R.id.detect);
        Detect.setIcon(MappBool ? R.drawable.ic_detect_on : R.drawable.ic_detect_off).setChecked(MappBool);
        if (savedInstanceState == null) {
            displayFragment(settingsNotifications, false);
            navigationView.setCheckedItem(R.id.notification);
        }
        CreateOverlayPermissionDialog();
        if (getIntent().getBooleanExtra("pickMoked", false))
            PickMokedDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (drawerLayout != null && !isDeviceTablet()) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            drawerLayout.setScrimColor(0x99000000);
            isDrawerLocked = false;
            drawerLayout.closeDrawer(navigationView);
            drawerToggle.syncState();
        }
        else if (drawerLayout != null) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
            drawerLayout.setScrimColor(Color.TRANSPARENT);
            isDrawerLocked = true;
        }
        SharedPreferences sp = getSharedPreferences("Settings", 0);
        boolean newLang = sp.getBoolean("WasLangChanged", false);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("WasLangChanged", false);
        editor.apply();
        if (newLang)
            recreate();
        if (Detect != null) {
            int icon = sp.getBoolean("Mapp", false) ? R.drawable.ic_detect_on : R.drawable.ic_detect_off;
            Detect.setIcon(icon);
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
        } catch (PackageManager.NameNotFoundException e) {
//            Crashlytics.log(e.getMessage());
        }
    }
    @Override
    public void onPostCreate(Bundle bundle) {
        super.onPostCreate(bundle);
        if(drawerToggle != null)
            drawerToggle.syncState();
        SharedPreferences.Editor sp1 = sp.edit();
        sp1.putBoolean("oldU", true);
        sp1.apply();
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Fragment fragment = settingsNotifications;
        boolean home = false;
        switch (id) {
            case R.id.homeDrawer:
                home = true;
                break;
            case R.id.notification:
                fragment = settingsNotifications;
                break;
            case R.id.soundAndVib:
                fragment = settingsNotifSou;
                break;
            case R.id.advanced:
                fragment = settingsAdvanced;
                break;
            case R.id.ampm:
                fragment = settingsAmPm;
                break;
            case R.id.tools:
                fragment = settingsTools;
                break;
            case R.id.detect:
                detectionBtn(item);
                return true;
            case R.id.language:
                CreateLangDialog();
                return true;
            case R.id.mokedPick:
                PickMokedDialog();
                return true;
        }
        if (home)
            finish();
        if (!isDrawerLocked)
            drawerLayout.closeDrawer(GravityCompat.START, true);
        displayFragment(fragment, true);
        return true;
    }
    private void displayFragment(Fragment fragment, boolean animate) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (animate) {
            transaction.setCustomAnimations(R.anim.enter_from_top, R.anim.exit_to_top);
        }
        transaction.replace(R.id.frame, fragment);
        transaction.commit();
    }
    public void displayFragment(Fragment fragment, int toCheck) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_top, R.anim.exit_to_top);
        transaction.replace(R.id.frame, fragment);
        transaction.commit();
        navigationView.setCheckedItem(toCheck);
    }
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START) && !isDrawerLocked)
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }
    private void CreateLangDialog() {
        final int langToPick = LocalHelper.getLanguage(SettingsPage.this).equals("iw") ? 0 : 1;
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsPage.this, R.style.AlertDialogStyle);
        builder.setTitle(getString(R.string.pref_title_lang));
        builder.setNegativeButton(getString(R.string.ret), (dialog, which) -> dialog.dismiss());
        builder.setSingleChoiceItems(Langs, langToPick, (dialog, which) -> {
            String locale = which == 0 ? "iw" : "en";
            if (langToPick != which) {
                SplitInstallStateUpdatedListener listener = state -> {
                    Log.e("SPLIT MANAGER", "error code is: " + state.errorCode());
                    Log.e("SPLIT MANAGER", "status code is: " + state.status());
                    if (state.status() == SplitInstallSessionStatus.FAILED &&
                            (state.errorCode() == SplitInstallErrorCode.SERVICE_DIED ||
                                    state.errorCode() == SplitInstallErrorCode.NETWORK_ERROR)) {
                        // Retry the request.
                        HideProgressDialog(1);
                        return;
                    }
                    if (state.sessionId() == mySessionId) {
                        Log.e("SPLIT MANAGER", "status session is: " + state.sessionId());
                        int totalBytes = (int)state.totalBytesToDownload();
                        int progress = (int)state.bytesDownloaded();
                        long percent = (state.totalBytesToDownload() / 100) * state.bytesDownloaded();
                        switch (state.status()) {
                            case SplitInstallSessionStatus.DOWNLOADING:
                                Log.e("SPLIT MANAGER", "downloading");
                                progressBar.setMax(totalBytes);
                                progressBar.setProgress(progress);
                                progressText.setText(getString(R.string.langLoad, percent + "%"));
                                //TODO:Update progress bar.
                                break;
                            case SplitInstallSessionStatus.INSTALLED:
                                //Module is downloaded successfully
                                progressBar.setMax(1);
                                progressBar.setProgress(1);
                                HideProgressDialog(0);
                                LocalHelper.setLocale(getApplicationContext(), locale);
                                break;
                            case SplitInstallSessionStatus.CANCELED:
                            case SplitInstallSessionStatus.CANCELING:
                            case SplitInstallSessionStatus.DOWNLOADED:
                            case SplitInstallSessionStatus.FAILED:
                            case SplitInstallSessionStatus.INSTALLING:
                            case SplitInstallSessionStatus.PENDING:
                            case SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION:
                            case SplitInstallSessionStatus.UNKNOWN:
                                HideProgressDialog(1);
                                break;
                        }
                    }
                };
                splitInstallManager = SplitInstallManagerFactory.create(getApplicationContext());
                Set<String> langs = splitInstallManager.getInstalledLanguages();
                StringBuilder languages = new StringBuilder();
                for (String lng : langs) {
                    languages.append(lng).append(", ");
                }
                Log.e("SPLIT MANAGER", "available lang " + languages);
                Log.e("SPLIT MANAGER", "requested lang " + locale);
                splitInstallManager.registerListener(listener);
                SplitInstallRequest request = SplitInstallRequest.newBuilder().addLanguage(Locale.forLanguageTag(locale)).build();
                ShowProgressDialog();
                splitInstallManager.startInstall(request)
                        .addOnSuccessListener(sessionId -> mySessionId = sessionId)
                        .addOnFailureListener(e -> {
                            HideProgressDialog(0);
                            Log.e("SPLIT MANAGER", "error");
                            try {
                                Snackbar bar = Snackbar.make(findViewById(android.R.id.content), R.string.langBad, Snackbar.LENGTH_LONG);
                                View viewSnk = bar.getView();
                                TextView tv = viewSnk.findViewById(com.google.android.material.R.id.snackbar_text);
                                tv.setTextColor(Color.WHITE);
                                bar.show();
                            } catch (IllegalArgumentException il) {
//                                    Crashlytics.logException(il);
                            }
                        });
                //splitInstallManager.unregisterListener(listener);
            }
            dialog.dismiss();
        });
        final AlertDialog langPicker = builder.create();
        langPicker.setOnShowListener(dialogInterface -> {
            TextView textView = langPicker.findViewById(android.R.id.title);
            if (textView != null)
                textView.setTypeface(myTypeface);
            Button cancelButton = langPicker.getButton(DialogInterface.BUTTON_NEGATIVE);
            cancelButton.setTypeface(myTypeface);
        });
        langPicker.show();
    }
    public void ShowProgressDialog() {
        AlertDialog.Builder progressDialogBuilder = new AlertDialog.Builder(SettingsPage.this, R.style.AlertDialogStyle);
        LayoutInflater inflater = (LayoutInflater) getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View dialogView = inflater.inflate(R.layout.progress_bar_layout, null);
        progressDialogBuilder.setView(dialogView).setTitle("מוריד שפה").setCancelable(false)
                .setNegativeButton(R.string.ret, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    if (splitInstallManager != null)
                        splitInstallManager.cancelInstall(mySessionId);
                })
                .setPositiveButton(R.string.apply, (dialogInterface, i) -> recreate());
        progressBar = dialogView.findViewById(R.id.progressBar2);
        progressText = dialogView.findViewById(R.id.loadTxt);
        progressText.setText(getString(R.string.langLoad, "0%"));
        AlertDialog progDialog = progressDialogBuilder.create();
        progDialog.show();
        posBtn = progDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        posBtn.setVisibility(View.GONE);
        negBtn = progDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
    }
    public void HideProgressDialog(int type){
        //worked properly
        if (type == 0) {
            progressText.setText(getString(R.string.langLoad, "100%"));
            if (posBtn != null)
                posBtn.setVisibility(View.VISIBLE);
            if (negBtn != null)
                negBtn.setVisibility(View.GONE);
        }
        else if (type == 1) {
            progressBar.setVisibility(View.GONE);
            progressText.setText(R.string.langBad);
        }
    }
    private void CreateOverlayPermissionDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
            builder.setMessage(getResources().getString(R.string.permDesOverlay)).setCancelable(false)
                    .setTitle(getString(R.string.permTitle))
                    .setPositiveButton(getResources().getString(R.string.permYes), (dialog, id) -> {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, OVERLAY_PER_REQ);
                    }).setNegativeButton(getResources().getString(R.string.dialog_no), (dialog, id) -> dialog.cancel());
            final AlertDialog alert = builder.create();
            alert.setOnShowListener(dialogInterface -> {
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
                cancelButton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.OldPrimary));
            });
            alert.show();
        }
    }
    private void PickMokedDialog() {
        final int MokedToPick = sp.getInt("merhav", 0);
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsPage.this, R.style.AlertDialogStyle);
        builder.setTitle(getString(R.string.pref_title_region));
        builder.setNegativeButton(getString(R.string.ret), (dialog, which) -> dialog.dismiss());
        builder.setSingleChoiceItems(Mokdim, MokedToPick, (dialog, which) -> {
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("pickedMoked", true);
            editor.putInt("merhav", which);
            editor.apply();
            dialog.dismiss();
        });
        AlertDialog langPicker = builder.create();
        langPicker.show();
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull  MenuItem item) {
        if (drawerToggle != null && drawerToggle.onOptionsItemSelected(item) && !isDrawerLocked)
            return true;
        else if (isDrawerLocked && item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("result", "activity result");
        if (requestCode == OVERLAY_PER_REQ && resultCode != RESULT_OK) {
                CreateOverlayPermissionDialog();
        }
        super.onActivityResult(requestCode, resultCode, data);
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
            if (activityExists) {
                try {
                    if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
                        builder.setMessage(getResources().getString(R.string.dialog_help)).setCancelable(false)
                                .setPositiveButton(getResources().getString(R.string.dialog_yes), (dialog, id) -> {
                                    Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                                    startActivity(intent);
                                })
                                .setNegativeButton(getResources().getString(R.string.dialog_no), (dialog, id) -> dialog.cancel());
                        final AlertDialog alert = builder.create();
                        alert.setOnShowListener(dialogInterface -> {
                            TextView textView = alert.findViewById(android.R.id.message);
                            if (textView != null)
                                textView.setTypeface(myTypeface);
                            Button canc = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                            canc.setTextColor(getApplicationContext().getResources().getColor(R.color.ColorPrimary));
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
                                .setPositiveButton(getResources().getString(R.string.dialog_yes), (dialog, id) -> {
                                    Intent intent1 = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                                    startActivityForResult(intent1, REQUEST_PERMISSION_SETTING);
                                })
                                .setNegativeButton(getResources().getString(R.string.dialog_no), (dialog, id) -> dialog.cancel());
                        final AlertDialog alert = builder.create();
                        alert.setOnShowListener(dialogInterface -> {
                            TextView textView = alert.findViewById(android.R.id.message);
                            if (textView != null)
                                textView.setTypeface(myTypeface);
                            textView = alert.findViewById(android.R.id.title);
                            if (textView != null)
                                textView.setTypeface(myTypeface);
                            Button canc = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                            canc.setTextColor(getApplicationContext().getResources().getColor(R.color.ColorPrimary));
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
                            .setPositiveButton(getResources().getString(R.string.dialog_yes), (dialog, id) -> {
                                Intent intent12 = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                                startActivityForResult(intent12, REQUEST_PERMISSION_SETTING);
                            })
                            .setNegativeButton(getResources().getString(R.string.dialog_no), (dialog, id) -> dialog.cancel());
                    final AlertDialog alert = builder.create();
                    alert.setOnShowListener(dialogInterface -> {
                        TextView textView = alert.findViewById(android.R.id.message);
                        if (textView != null)
                            textView.setTypeface(myTypeface);
                        textView = alert.findViewById(android.R.id.title);
                        if (textView != null)
                            textView.setTypeface(myTypeface);
                        Button cancelButton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                        cancelButton.setTextColor(getApplicationContext().getResources().getColor(R.color.ColorPrimary));
                    });
                    alert.show();
                }
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
    }
    public boolean isDeviceTablet(){
        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float yInches= metrics.heightPixels/metrics.ydpi;
        float xInches= metrics.widthPixels/metrics.xdpi;
        double diagonalInches = Math.sqrt(xInches*xInches + yInches*yInches);
        boolean land = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        return land && diagonalInches >= 6.5;
    }
}