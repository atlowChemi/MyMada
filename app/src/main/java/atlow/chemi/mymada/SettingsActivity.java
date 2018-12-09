package atlow.chemi.mymada;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceActivity;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatCheckedTextView;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.List;
import java.util.Locale;

public class SettingsActivity extends AppCompatPreferenceActivity {
    private static int type = 0;
    String[] types = {"הגדרות", "הגדרות התראות", "הגדרות יום ולילה", "הגדרות הכתבה", "הגדרות מתקדמות", "הגדרות שפה"};
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.headers_preference, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName)  {
        if (fragmentName.contains("LangFragment"))
            return LangFragment.class.getName().equals(fragmentName);
        return SettingsFragemnt.class.getName().equals(fragmentName);
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        // Allow super to try and create a view first
        final View result = super.onCreateView(name, context, attrs);
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
