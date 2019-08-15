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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
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

public class UtillitiesAndTools extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private MenuItem Detect;
    private Typeface myTypeface;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private Fragment contracTimer = new ContracTimer(), pulseCalc = new PulseCalc(), metroActivity = new MetroActivity(),
            vitalsChecker= new VitalsChecker(), oxyCalc = new OxyCalc(), apgarCalc = new ApgarCalc(),
            glazgoCalc = new GlazgoCalc(),  blsProtocoles = new BlsProtocoles(), dictionaryAdmin = new DictionaryAdmin();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_utillities_and_tools);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        myTypeface = Typeface.createFromAsset(getAssets(), "fonts/opensanshebrew.ttf");

        drawerLayout = findViewById(R.id.activity_Utils);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        navigationView = findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);
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
        displayFragment(f);
    }
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
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
            case R.id.protocoles:
                fragment = blsProtocoles;
                break;
            case R.id.dictionary:
                fragment = dictionaryAdmin;
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START, true);
        displayFragment(fragment);
        return true;
    }
    private void displayFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Random random = new Random();
        int enterAnim = random.nextBoolean() ? R.anim.enter_from_left : R.anim.enter_from_right;
        int exitAnim = enterAnim == R.anim.enter_from_left ? R.anim.exit_to_right : R.anim.exit_to_left;
        transaction.setCustomAnimations(enterAnim, exitAnim);
        transaction.replace(R.id.frame, fragment);
        transaction.commit();
    }
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }
    @Override
    protected void onResume() {
        super.onResume();
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
        if (drawerToggle.onOptionsItemSelected(item))
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
                    startActivity(new Intent(this, SettingsDesigned.class));
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
}
