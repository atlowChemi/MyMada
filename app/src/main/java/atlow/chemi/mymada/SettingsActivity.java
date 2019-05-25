package atlow.chemi.mymada;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;

import java.util.List;
import java.util.Locale;

public class SettingsActivity extends AppCompatPreferenceActivity {
    private static int type = 0;
    String[] types = {"הגדרות", "הגדרות התראות", "הגדרות יום ולילה", "הגדרות הכתבה", "הגדרות מתקדמות", "הגדרות שפה"};
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocalHelper.onAttach(base));
    }
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.headers_preference, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName)  {
        return SettingsFragemnt.class.getName().equals(fragmentName);
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        // Allow super to try and create a view first
        final View result = super.onCreateView(name, context, attrs);
        int ho = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (int)context.getResources().getDimension(R.dimen.activity_horizontal_margin)+90, context.getResources().getDisplayMetrics());

        if (result != null) {
            return result;
        }
        return null;
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);


        SharedPreferences sp = getSharedPreferences("Settings", 0);
        SharedPreferences.Editor sp1 = sp.edit();
        sp1.putBoolean("oldU", true);
        sp1.apply();
        String settings = SettingsActivity.this.getResources().getString(R.string.title_activity_settings);
        String notification = SettingsActivity.this.getResources().getString(R.string.pref_header_notifications);
        String ampm = SettingsActivity.this.getResources().getString(R.string.pref_header_ampm);
        String moked = SettingsActivity.this.getResources().getString(R.string.pref_header_moked);
        String advanced = SettingsActivity.this.getResources().getString(R.string.pref_header_advanced);
        String lang = SettingsActivity.this.getResources().getString(R.string.pref_header_language);
        types[0] = settings;
        types[1] = notification;
        types[2] = ampm;
        types[3] = moked;
        types[4] = advanced;
        types[5] = lang;
        Log.d("title is", getSupportActionBar().getTitle().toString());
        getSupportActionBar().setTitle(types[type]);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
    }

    public static boolean isRTL() {
        return isRTL(Locale.getDefault());
    }
    public static boolean isRTL(Locale locale) {
        final int directionality = Character.getDirectionality(locale.getDisplayName().charAt(0));
        return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT || directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
    }
    @Override
    public void onHeaderClick(Header header, int position) {
        super.onHeaderClick(header, position);
        if (header.id == R.id.notifId)
            type = 1;
        if (header.id == R.id.dayNightId)
            type = 2;
        if (header.id == R.id.regionId)
            type = 3;
        if (header.id == R.id.advaId)
            type = 4;
        if (header.id == R.id.langId)
            type = 5;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            getSupportActionBar().setTitle(types[type]);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                type = 0;
                getSupportActionBar().setTitle(types[0]);
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        type = 0;
        getSupportActionBar().setTitle(types[type]);
    }
    @Override
    public void onDestroy() {
        type = 0;
        super.onDestroy();
    }
}
