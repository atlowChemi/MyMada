package atlow.chemi.mymada;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.Preference;
import android.view.MenuItem;

import java.util.Locale;

public class LangFragment extends PreferenceFragment {
    String oldI = "6";
    SharedPreferences sharedPreferences;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_lang);
        sharedPreferences = getActivity().getSharedPreferences("Settings", 0);
        String settings = getArguments().getString("type");
        ListPreference lang = (ListPreference)findPreference("Lang");
        lang.setOnPreferenceChangeListener(list2);
    }

    Preference.OnPreferenceChangeListener list2 = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            switch (preference.getKey()) {
                case "Lang":
                    OLangC(newValue);
                    break;
            }
            return true;
        }
    };
    public void OLangC(Object newValue) {
        SharedPreferences preferences = getActivity().getSharedPreferences("Settings", 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("lng", (String) newValue);
        editor.putBoolean("WasLangChanged", true);
        editor.apply();
        Resources resources = getActivity().getApplicationContext().getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = new Locale((String)newValue);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        getActivity().recreate();
    }
}
