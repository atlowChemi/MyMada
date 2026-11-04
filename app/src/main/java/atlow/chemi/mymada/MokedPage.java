package atlow.chemi.mymada;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import atlow.chemi.mymada.fragments.MainActivity1;
import atlow.chemi.mymada.fragments.MainActivity2;
import atlow.chemi.mymada.fragments.MainActivity3;
import atlow.chemi.mymada.helpers.LocalHelper;
import atlow.chemi.mymada.recieversAndServices.NotificationServiceOld;
import atlow.chemi.mymada.recieversAndServices.SMSMonitorService;

public class MokedPage extends AppCompatActivity {
    private MenuItem Detect;
    private static final int REQUEST_PERMISSION_SETTING = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resetTitle();
        setContentView(R.layout.activity_moked_page);
        SharedPreferences sharedPreferences = getSharedPreferences("Settings", 0);
        boolean realM = sharedPreferences.getBoolean("realM", false);
        boolean isMADA;
        if (!realM) {
            isMADA = isPI(MokedPage.this);
            if (!isMADA) {
                Intent intent = new Intent(MokedPage.this, lockPage.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                finish();
                startActivity(intent);
            }
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ViewPager viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        if (SettingsPage.isRTL())
            viewPager.setRotationY(180);
        boolean msg = getIntent().getBooleanExtra("msg", false);
        if(!msg)
            viewPager.setCurrentItem(1);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        SharedPreferences sp = getSharedPreferences("Settings", 0);
        if (!sp.getBoolean("pickedMoked", false)) {
            Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/opensanshebrew.ttf");
            AlertDialog.Builder build = new AlertDialog.Builder(this, R.style.AlertDialogStyle)
                    .setTitle(getString(R.string.pickMokTtl))
                    .setMessage(getString(R.string.pickMokTxt))
                    .setPositiveButton(getString(R.string.goToSet), (dialogInterface, i) -> {
                        Intent intent = new Intent(MokedPage.this, SettingsPage.class);
                        intent.putExtra("pickMoked", true);
                        startActivity(intent);
                    })
                    .setNegativeButton(getString(R.string.skip), (dialogInterface, i) -> dialogInterface.dismiss());
            final AlertDialog alert = build.create();
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
                cancelButton.setTextColor(MokedPage.this.getResources().getColor(R.color.ColorPrimary));
            });
            build.show();
        }
    }
    private static boolean isPI(Context context) {
        final PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage("com.groboot.mdaemergency");
        if (intent == null)
            return false;
        return packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0;
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
//            Crashlytics.logException(e);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new MainActivity1(), getString(R.string.button_a));
        adapter.addFragment(new MainActivity2(), getString(R.string.button_b));
        adapter.addFragment(new MainActivity3(), getString(R.string.button_i));
        viewPager.setAdapter(adapter);
    }
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();
        ViewPagerAdapter(FragmentManager manager) {
            super(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }
        @NonNull
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }
        @Override
        public int getCount() {
            return mFragmentList.size();
        }
        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }
        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences sp = getSharedPreferences("Settings", 0);
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.one:
                atlow.chemi.mymada.settings.DetectionControls.toggle(this, item);
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
        catch (Exception e)    {
//            Crashlytics.logException(e);
            return false;
        }
    }
}
