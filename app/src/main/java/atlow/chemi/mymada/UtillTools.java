package atlow.chemi.mymada;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import java.util.List;

public class UtillTools extends AppCompatActivity {

    private ContracTimer contracTimer = new ContracTimer();
    private PulseCalc pulseCalc = new PulseCalc();
    private MetroActivity metroActivity = new MetroActivity();
    private VitalsChecker vitalsChecker= new VitalsChecker();
    private OxyCalc oxyCalc = new OxyCalc();
    private ApgarCalc apgarCalc = new ApgarCalc();
    private GlazgoCalc glazgoCalc = new GlazgoCalc();
    private BlsProtocoles blsProtocoles = new BlsProtocoles();

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resetTitle();
        setContentView(R.layout.activity_utill_tools);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(2);
        if (SettingsActivity.isRTL())
            mViewPager.setRotationY(180);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

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


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 0)
                return contracTimer;
            if (position == 1)
                return pulseCalc;
            if (position == 2)
                return metroActivity;
            if (position == 3)
                return vitalsChecker;
            if (position == 4)
                return oxyCalc;
            if (position == 5)
                return apgarCalc;
            if (position == 6)
                return glazgoCalc;
            if (position == 7)
                return blsProtocoles;
            return metroActivity;
        }
        @Override
        public int getCount() {
            // Show 8 total pages.
            return 8;
        }
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getApplicationContext().getResources().getString(R.string.button_f);
                case 1:
                    return getApplicationContext().getResources().getString(R.string.button_c);
                case 2:
                    return getApplicationContext().getResources().getString(R.string.button_d);
                case 3:
                    return getApplicationContext().getResources().getString(R.string.button_e);
                case 4:
                    return getApplicationContext().getResources().getString(R.string.button_g);
                case 5:
                    return getApplicationContext().getResources().getString(R.string.button_ag);
                case 6:
                    return getApplicationContext().getResources().getString(R.string.button_gl);
                case 7:
                    return getApplicationContext().getResources().getString(R.string.button_p);
            }
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        SharedPreferences sp = getSharedPreferences("Settings", 0);
        getDelegate().getMenuInflater().inflate(R.menu.popup_menu, menu);
        SpannableString reco = new SpannableString(getResources().getString(R.string.pref_title_enable));
        reco.setSpan(new ForegroundColorSpan(Color.BLACK), 0, reco.length(), 0);
        SpannableString sett = new SpannableString(getResources().getString(R.string.action_settings));
        sett.setSpan(new ForegroundColorSpan(Color.BLACK), 0, sett.length(), 0);
        menu.findItem(R.id.one).setCheckable(true).setChecked(sp.getBoolean("Mapp", false)).setTitle(reco);
        menu.findItem(R.id.action_settings).setTitle(sett);
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
                }
                s.apply();
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
}
