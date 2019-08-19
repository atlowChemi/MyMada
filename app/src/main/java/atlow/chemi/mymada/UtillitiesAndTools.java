package atlow.chemi.mymada;

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
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.material.navigation.NavigationView;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import atlow.chemi.mymada.fragments.ApgarCalc;
import atlow.chemi.mymada.fragments.BlsProtocoles;
import atlow.chemi.mymada.fragments.ContracTimer;
import atlow.chemi.mymada.fragments.DictionaryAdmin;
import atlow.chemi.mymada.fragments.GlazgoCalc;
import atlow.chemi.mymada.fragments.MetroActivity;
import atlow.chemi.mymada.fragments.OxyCalc;
import atlow.chemi.mymada.fragments.PulseCalc;
import atlow.chemi.mymada.fragments.VitalsChecker;
import atlow.chemi.mymada.helpers.LocalHelper;
import atlow.chemi.mymada.helpers.MyDrawerLayout;
import atlow.chemi.mymada.recieversAndServices.NotificationService;
import atlow.chemi.mymada.recieversAndServices.NotificationServiceOld;
import atlow.chemi.mymada.recieversAndServices.SMSMonitorService;

public class UtillitiesAndTools extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private MyDrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private boolean isDrawerLocked = false;
    private MenuItem Detect;
    private Typeface myTypeface;
    private FragmentTransaction transaction;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private Fragment contracTimer = new ContracTimer(), pulseCalc = new PulseCalc(), metroActivity = new MetroActivity(),
            vitalsChecker= new VitalsChecker(), oxyCalc = new OxyCalc(), apgarCalc = new ApgarCalc(),
            glazgoCalc = new GlazgoCalc(), dictionaryAdmin = new DictionaryAdmin(), blsProtocoles = new BlsProtocoles();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resetTitle();
        setContentView(R.layout.activity_utillities_and_tools);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        myTypeface = Typeface.createFromAsset(getAssets(), "fonts/opensanshebrew.ttf");

        drawerLayout = findViewById(R.id.activity_Utils);
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

        navigationView = findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.inflateMenu(R.menu.navigation_menu);
        Menu navMenu = navigationView.getMenu();

        String[] toolsS =  {"contrac", "pulse", "metro", "vital", "oxy", "apgar", "glaz", "prot", "dict"};
        SharedPreferences preferences = getSharedPreferences("Settings", 0);
        Set<String> toolsToShow = preferences.getStringSet("Tools", new LinkedHashSet<>(Arrays.asList(toolsS)));

        Fragment f = metroActivity;
        if (!toolsToShow.contains("dict"))
            navMenu.removeItem(R.id.dictionary);
        if (!toolsToShow.contains("prot"))
            navMenu.removeItem(R.id.protocoles);
        if (!toolsToShow.contains("glaz"))
            navMenu.removeItem(R.id.glazgowCalc);
        if (!toolsToShow.contains("apgar"))
            navMenu.removeItem(R.id.apgarCalc);
        if (!toolsToShow.contains("oxy"))
            navMenu.removeItem(R.id.oxygenCalc);
        if (!toolsToShow.contains("vital"))
            navMenu.removeItem(R.id.vitalCalc);
        if (!toolsToShow.contains("pulse"))
            navMenu.removeItem(R.id.pulseCalc);
        if (!toolsToShow.contains("contrac"))
            navMenu.removeItem(R.id.contractionCalc);
        if (!toolsToShow.contains("metro"))
            navMenu.removeItem(R.id.metronome);

        if (!toolsToShow.contains("metro")) {
            navigationView.setCheckedItem(navMenu.getItem(0));
            switch (navMenu.getItem(0).getItemId()) {
                case R.id.dictionary:
                    f = dictionaryAdmin;
                    break;
                case R.id.protocoles:
                    f = blsProtocoles;
                    break;
                case R.id.glazgowCalc:
                    f = glazgoCalc;
                    break;
                case R.id.apgarCalc:
                    f = apgarCalc;
                    break;
                case R.id.oxygenCalc:
                    f = oxyCalc;
                    break;
                case R.id.vitalCalc:
                    f = vitalsChecker;
                    break;
                case R.id.pulseCalc:
                    f = pulseCalc;
                    break;
                case R.id.contractionCalc:
                    f = contracTimer;
                    break;
            }
        }
        else {
            navigationView.setCheckedItem(R.id.metronome);
            f = metroActivity;
        }
        if (savedInstanceState == null)
            displayFragment(f, false);
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
    public void onPostCreate(Bundle bundle) {
        super.onPostCreate(bundle);
        drawerToggle.syncState();
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Fragment fragment = metroActivity;
        switch (id) {
            case R.id.contractionCalc:
                fragment = contracTimer;
                break;
            case R.id.pulseCalc:
                fragment = pulseCalc;
                break;
            case R.id.metronome:
                fragment = metroActivity;
                break;
            case R.id.vitalCalc:
                fragment = vitalsChecker;
                break;
            case R.id.oxygenCalc:
                fragment = oxyCalc;
                break;
            case R.id.apgarCalc:
                fragment = apgarCalc;
                break;
            case R.id.glazgowCalc:
                fragment = glazgoCalc;
                break;
            case R.id.bls:
                fragment = blsProtocoles;
                ((BlsProtocoles)blsProtocoles).setCurrentPage(0);
                break;
            case R.id.als:
                fragment = blsProtocoles;
                ((BlsProtocoles)blsProtocoles).setCurrentPage(1);
                break;
            case R.id.death:
                fragment = blsProtocoles;
                ((BlsProtocoles)blsProtocoles).setCurrentPage(2);
                break;
            case R.id.dictionary:
                fragment = dictionaryAdmin;
                break;
        }
        if (!isDrawerLocked)
            drawerLayout.closeDrawer(GravityCompat.START, true);
        displayFragment(fragment, true);
        return true;
    }
    private void displayFragment(Fragment fragment, boolean animate) {
        transaction = getSupportFragmentManager().beginTransaction();
        if (animate) {
            transaction.setCustomAnimations(R.anim.enter_from_top, R.anim.exit_to_top);
        }
        transaction.replace(R.id.frame, fragment);
        transaction.commit();
    }
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START) && !isDrawerLocked)
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        SharedPreferences sp = getSharedPreferences("Settings", 0);
        getMenuInflater().inflate(R.menu.popup_menu, menu);
        int icon = sp.getBoolean("Mapp", false) ? R.drawable.ic_detect_on : R.drawable.ic_detect_off;
        Detect = menu.findItem(R.id.one);
        menu.findItem(R.id.one).setCheckable(true).setChecked(sp.getBoolean("Mapp", false)).setIcon(icon);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (drawerToggle != null && drawerToggle.onOptionsItemSelected(item) && !isDrawerLocked)
            return true;
        else {
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
    }
    private boolean isCallable(Intent intent) {
        try {
            List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            return list.size() > 0;
        } catch (Exception e) {
            e.printStackTrace();
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
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
